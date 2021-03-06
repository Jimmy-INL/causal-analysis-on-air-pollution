package de.tuberlin.dima.bdapro.dataIntegration.sensor;

import org.apache.flink.api.java.io.TextOutputFormat;

/**
 * An Flink TextFormatter that formats {@link UnifiedSensorReading} based on a given {@link Type}.
 */
public class SensorReadingFormatter implements TextOutputFormat.TextFormatter<UnifiedSensorReading> {
    private final Type sensorType;

    /**
     * Creates a new SensorReadingFormatter for a given sensor type.
     *
     * @param sensorType The type of the sensor readings that shall be formatted.
     */
    public SensorReadingFormatter(Type sensorType) {
        this.sensorType = sensorType;
    }

    /**
     * Formats the sensor reading in a semicolon-separated format.
     *
     * @param value The sensor reading to format.
     * @return The string representation of the sensor reading.
     */
    @Override
    public String format(UnifiedSensorReading value) {
        return value.toString(sensorType);
    }
}
