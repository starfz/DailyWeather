package com.dailyweather.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DailyWeatherDB {
	
	private static final String DB_NAME = "city_info.db";
	private static final int DB_VERSION = 1;
	private static DailyWeatherDB dwDB;
	private SQLiteDatabase db;
	
	private DailyWeatherDB(Context context) {
		DailyWeatherOpenHelper dbHelper = new DailyWeatherOpenHelper(context, DB_NAME, null, DB_VERSION);
		db = dbHelper.getReadableDatabase();
	}
	
	//��ȡDailyWeatherDBʵ��
	public synchronized static DailyWeatherDB getInstance(Context context) {
		if(dwDB == null) {
			dwDB = new DailyWeatherDB(context);
		}
		
		return dwDB;
	}
	
	//��ʡ���ݱ��浽Province��
	public void saveProvince(Province province) {
		if(province != null) {
			List<Province> list = new ArrayList<Province>();
			list = loadProvince();
			List<String> temp_name = new ArrayList<String>();
			if(list.size()==0) {
				ContentValues values = new ContentValues();
				values.put("province_name", province.getProvinceName());
				db.insert("Province", null, values);
			} else {
				for(int i=0; i<list.size(); i++) {
					temp_name.add(list.get(i).getProvinceName());
				}
				if(!temp_name.contains(province.getProvinceName())) {//�ж��Ƿ����ظ�����
					ContentValues values = new ContentValues();
					values.put("province_name", province.getProvinceName());
					db.insert("Province", null, values);
				}
			}
		}
	}
	
	//��Province����ȡ��ʡ����
	public List<Province> loadProvince() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if(cursor.moveToFirst()) {
			do {
				int id = cursor.getInt(cursor.getColumnIndex("id"));
				String province_name = cursor.getString(cursor.getColumnIndex("province_name"));
				Province province = new Province();
				province.setId(id);
				province.setProvinceName(province_name);
				list.add(province);
			} while(cursor.moveToNext());
		}
		if(cursor != null) {
			cursor.close();
		}
		return list;
	}
	
	//�������ݱ��浽City��
	public void saveCity(City city) {
		if(city != null) {
			List<City> list = new ArrayList<City>();
			list = loadAllCity();
			List<String> temp_name = new ArrayList<String>();
			if(list.size() == 0) {
				ContentValues values = new ContentValues();
				values.put("city_name", city.getCityName());
				values.put("province_name", city.getProvinceName());
				db.insert("City", null, values);
			} else {
				for(int i=0; i<list.size(); i++) {
					temp_name.add(list.get(i).getCityName());
				}
				if(!temp_name.contains(city.getCityName())) {//�ж��Ƿ����ظ�����
					ContentValues values = new ContentValues();
					values.put("city_name", city.getCityName());
					values.put("province_name", city.getProvinceName());
					db.insert("City", null, values);
				}
			}
		}
	}
	
	//����province_name��City���в�ѯ��ѡ��ʡ�ݶ�Ӧ��������
	public List<City> loadCity(String province_name) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_name = ?", new String[] { province_name }, null, null, null);
		if(cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setProvinceName(province_name);
				
				list.add(city);
			} while(cursor.moveToNext());
		}
		
		if(cursor != null) {
			cursor.close();
		}
		
		return list;
	}
	
	//��ѯ�����е�����
		public List<City> loadAllCity() {
			List<City> list = new ArrayList<City>();
			Cursor cursor = db.query("City", null, null, null, null, null, null);
			if(cursor.moveToFirst()) {
				do {
					City city = new City();
					city.setId(cursor.getInt(cursor.getColumnIndex("id")));
					city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
					city.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
					
					list.add(city);
				} while(cursor.moveToNext());
			}
			
			if(cursor != null) {
				cursor.close();
			}
			
			return list;
		}

	//�������ݱ��浽County��
	public void saveCounty(County county) {
		if(county != null) {
			List<County> list = new ArrayList<County>();
			List<String> temp_code = new ArrayList<String>();
			list = loadAllCounty();
			if(list.size() == 0) {
				ContentValues values = new ContentValues();
				values.put("county_code", county.getCountyCode());
				values.put("county_name", county.getCountyName());
				values.put("city_name", county.getCityName());
				values.put("lat", county.getLat());
				values.put("lon", county.getLon());
				db.insert("County", null, values);
			} else {
				for(int i=0; i<list.size(); i++) {
					temp_code.add(list.get(i).getCountyCode());
				}
				if(!temp_code.contains(county.getCountyCode())) {
					ContentValues values = new ContentValues();
					values.put("county_code", county.getCountyCode());
					values.put("county_name", county.getCountyName());
					values.put("city_name", county.getCityName());
					values.put("lat", county.getLat());
					values.put("lon", county.getLon());
					db.insert("County", null, values);
				}
			}
		}
	}
	
	//����city_name��County���в�ѯ��ѡ���ж�Ӧ��������
	public List<County> loadCounty(String city_name) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_name = ?", new String[] { city_name }, null, null, null);
		
		if(cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCityName(city_name);
				county.setLat(cursor.getFloat(cursor.getColumnIndex("lat")));
				county.setLon(cursor.getFloat(cursor.getColumnIndex("lon")));
				list.add(county);
			} while(cursor.moveToNext());
		}
		
		if(cursor != null) {
			cursor.close();
		}
		
		return list;
	}
	
	//��ѯ�����ص���Ϣ
	public List<County> loadAllCounty() {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, null, null, null, null, null);
		
		if(cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				county.setLat(cursor.getFloat(cursor.getColumnIndex("lat")));
				county.setLon(cursor.getFloat(cursor.getColumnIndex("lon")));
				list.add(county);
			} while(cursor.moveToNext());
		}
		
		if(cursor != null) {
			cursor.close();
		}
		
		return list;
	}
}
