/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tuberlin.dima.bdapro.jobs;

import de.tuberlin.dima.bdapro.sensors.*;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.BatchTableEnvironment;
import org.apache.flink.types.Row;

import java.util.List;

/**
 * Skeleton for a Flink Batch Job.
 *
 * <p>For a tutorial how to write a Flink batch application, check the
 * tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution,
 * change the main class in the POM.xml file to this class (simply search for 'mainClass')
 * and run 'mvn clean package' on the command line.
 */
public class SensorStatistics extends SensorJob {
    private static String basePath = "data/raw/csv_per_month";

    public static void main(String[] args) throws Exception {
        // set up the batch execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        final BatchTableEnvironment tEnv = BatchTableEnvironment.create(env);
        // Datasets with loading issues:
        DataSet<BME280Reading> bme280ReadingDataSet = readSensors(env, basePath, "**/*_bme280.csv.gz", BME280Reading.class, BME280Reading.getFields());

        // Datasets without loading issues
        DataSet<HPMReading> hpmReadingDataSet = readSensors(env, basePath, "**/*_hpm.csv.gz", HPMReading.class, HPMReading.getFields());
        DataSet<PPD42NSReading> ppd42nsReadingDataSet = readSensors(env, basePath, "**/*_ppd42ns.csv.gz", PPD42NSReading.class, PPD42NSReading.getFields());
        DataSet<HTU21DReading> htu21DReadingDataSet = readSensors(env, basePath, "**/*_htu21d.csv.gz", HTU21DReading.class, HTU21DReading.getFields());
        DataSet<DS18B20Reading> ds18b20ReadingDataSet = readSensors(env, basePath, "**/*_ds18b20.csv.gz", DS18B20Reading.class, DS18B20Reading.getFields());
        DataSet<PMS3003Reading> pms3003ReadingDataSet = readSensors(env, basePath, "**/*_pms3003.csv.gz", PMS3003Reading.class, PMS3003Reading.getFields());
        DataSet<PMS5003Reading> pms5003ReadingDataSet = readSensors(env, basePath, "**/*_pms5003.csv.gz", PMS5003Reading.class, PMS5003Reading.getFields());
        DataSet<PMS7003Reading> pms7003ReadingDataSet = readSensors(env, basePath, "**/*_pms7003.csv.gz", PMS7003Reading.class, PMS7003Reading.getFields());
        DataSet<BMP180Reading> bmp180ReadingDataSet = readSensors(env, basePath, "**/*_bmp180.csv.gz", BMP180Reading.class, BMP180Reading.getFields());

        // TODO: Check datasets
        DataSet<DHT22Reading> dht22ReadingDataSet = readSensors(env, basePath, "**/*_dht22.csv.gz", DHT22Reading.class, DHT22Reading.getFields());
        DataSet<SDS011Reading> sds011ReadingDataSet = readSensors(env, basePath, "**/*_sds011.csv.gz", SDS011Reading.class, SDS011Reading.getFields());

        Table sensorStatistics = sensorStatistics(tEnv, dht22ReadingDataSet);
        DataSet<Row> sensorStatisticDataSet = tEnv.toDataSet(sensorStatistics, Row.class);
        List<Row> result  = sensorStatisticDataSet.collect();
        env.execute("Flink Batch Java API Skeleton");
    }

    private static <T extends SensorReading>  Table sensorStatistics(BatchTableEnvironment tEnv, DataSet<T> sensorData) {
        Table table = tEnv.fromDataSet(sensorData);
        Table result = table.groupBy("sensorId, sensorType, location, lat, lon")
                .select("sensorId, sensorType, location, lat, lon, timestamp.min as minTimestamp, timestamp.max as maxTimestamp, sensorId.count as readingCount");
        return result;
    }
}