from numpy import genfromtxt
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import datetime
from datetime import datetime
import utils
import numpy as np
from sklearn import preprocessing
from sklearn.tree import DecisionTreeRegressor
from sklearn.metrics import r2_score
from sklearn.linear_model import LinearRegression
import sys
from operator import itemgetter
from tigramite import data_processing as pp
import time
from tigramite.independence_tests import ParCorr, GPDC, CMIknn, CMIsymb
from tigramite.pcmci import PCMCI

def feature_list():
	return ["location", 
		"lat", 
		"lon", 
		"timestamp", 
		"dayOfYear", 
		"minuteOfDay", 
		"dayOfWeek", 
		"isWeekend",
		"pressure_1", 
		"altitude", 
		"pressure_sealevel", 
		"temperature", 
		"humidity_1",
		"p1", 
		"p2", 
		"p0", 
		"durP1", 
		"ratioP1", 
		"durP2", 
		"ratioP2",
		"apparent_temperature",
		"cloud_cover",
		"dew_point",
		"humidity",
		"ozone",
		"precip_intensity",
		"precip_probability",
		"precip_type",
		"pressure",
		"uv_index",
		"visibility",
		"wind_bearing",
		"wind_gust",
		"wind_speed"]

def sensor_family():
	return ["location", "lat", "lon","altitude", "pressure_sealevel", "temperature", 
			"humidity_1","p1", "p2", "p0","durP1", "ratioP1", "durP2", "ratioP2"]

def time_family():
	return ["timestamp", "dayOfYear", "minuteOfDay", "dayOfWeek", "isWeekend"]

def weather_family():
	return ["apparent_temperature","cloud_cover","dew_point","humidity","ozone",
		"precip_intensity","precip_probability","precip_type","pressure","uv_index","visibility",
		"wind_bearing","wind_gust","wind_speed"]

sensor_family=sensor_family()
time_family=time_family()
weather_family=weather_family()

def family_list():
	return [sensor_family,time_family,weather_family]

def load_data(path):
	sensor_data = pd.read_csv(path, sep=";", names=feature_list(), true_values=["true"], false_values=["false"])

	sensor_data["timestamp"] = pd.to_datetime(sensor_data["timestamp"])
	sensor_data["isWeekend"] = sensor_data["isWeekend"].astype(int)

	sensor_data = sensor_data.sort_values(by=["location", "timestamp"])
	return sensor_data

def subset(data,by_family=[],by_columns=[],start_date='',end_date=''):
	
	if by_family or by_columns:
		final_feature_list=['timestamp']
		if by_family:         
			for i in by_family:
				if i=='sensor_family':
					for j in sensor_family:
						final_feature_list.extend([j])
				if i=='time_family':
					for j in time_family:
						final_feature_list.extend([j])
				if i=='weather_family':
					for j in weather_family:
						final_feature_list.extend([j])
				else:
					sys.exit()
						
		if by_columns:
			for i in by_columns:
				if i in feature_list():
					final_feature_list.extend([i])
				else:
					sys.exit()

		final_feature_list=list(dict.fromkeys(final_feature_list))
		sensor_data = data[final_feature_list]
	
	else:
		sensor_data=data
	
	if start_date != '' and end_date != '':
	
		s=datetime.strptime(start_date, '%Y-%m-%d')
		e=datetime.strptime(end_date, '%Y-%m-%d')
		
		sensor_data=sensor_data.loc[(sensor_data['timestamp']<e) & (sensor_data['timestamp']>s)]
	else:
		pass
	return sensor_data.drop_duplicates()

def localize(data,lat,lon,results=1):
	must=['location','lat','lon']
	if all([i in list(data) for i in must]):
		locations_array=np.asarray(data[['location','lat','lon']].drop_duplicates())
		distances=[]
		for i in locations_array:
			distances.append([i[0],np.sqrt((i[1]-lat)**2+(i[2]-lon)**2)])
		distances=sorted(distances, key=itemgetter(1)) 
		slices=[i[0] for i in distances[0:results]]
		localized_data=data.loc[data['location'].isin(slices)]
	else:
		sys.exit()
	
	return localized_data

def input_na(data,columns,method=None,value=None):
	must=['location','timestamp']
	if all([i in list(data) for i in must]):
		data.sort_values(by=["location", "timestamp"],inplace=True)
		if 'precip_type' in columns and 'precip_type' in list(data):
			data['precip_type'].replace(np.nan, 'no precip', regex=True,inplace=True)
		else:
			pass

		if all([i in list(data) for i in columns]):
			for i in columns:
				if data[i].isnull().sum().sum()>0:
					if method:
						data[i].fillna(method=method,inplace=True)
					elif value:
						data[i].fillna(value,inplace=True)
		else:
			sys.exit()
		
		no_nulls_list=[]
		for j in list(data):
			if data[j].isnull().sum().sum()==0:
				no_nulls_list.extend([j])
		
		data[no_nulls_list].dropna(inplace=True)
		
	return data[no_nulls_list]

def create_tigramite_dataframe(dataset,exclude):
	must=['timestamp']
	var_list=list(dataset)
	if all([i in var_list for i in exclude]):
		for i in exclude:
			var_list.remove(i)
	else:
		sys.exit()
		
	data = dataset[var_list]
	
	if 'timestamp' in list(dataset):
		datatime = dataset["timestamp"]
	else:
		sys.exit()

	dataframe = pp.DataFrame(data.values, datatime = datatime.values, var_names=var_list)
	return dataframe,var_list