package com.dailyweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DailyWeatherOpenHelper extends SQLiteOpenHelper{
	
	private static final String CREATE_PROVINCE = "create table Province ("
			+ "id integer primary key autoincrement, "
			+ "province_name text)";
	
	private static final String CREATE_CITY = "create table City ("
			+ "id integer primary key autoincrement, "
			+ "city_name text,"
			+ "province_name text)";
	
	private static final String CREATE_COUNTY = "create table County (" 
			+ "id integer primary key autoincrement, "
			+ "county_code text,"
			+ "county_name text,"
			+ "city_name text,"
			+ "lat real,"
			+ "lon real)";
			
	public DailyWeatherOpenHelper(Context context, String dbName, CursorFactory factory, int version) {
		super(context, dbName, factory, version);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
