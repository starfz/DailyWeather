package com.dailyweather.app.db;

public class County {
	
	private int id;
	private String county_code;
	private String county_name;
	private String city_name;  //关联City表city_name字段
	private float lat;  //纬度，用于后续定位功能
	private float lon; //经度，用于后续定位功能
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setCountyCode(String county_code) {
		this.county_code = county_code;
	}
	
	public void setCountyName(String county_name) {
		this.county_name = county_name;
	}
	
	public void setCityName(String city_name) {
		this.city_name = city_name;
	}
	
	public void setLat(float lat) {
		this.lat = lat;
	}
	
	public void setLon(float lon) {
		this.lon = lon;
	}
	
	public int getId() {
		return id;
	}
	
	public String getCountyCode() {
		return county_code;
	}
	
	public String getCountyName() {
		return county_name;
	}
	
	public String getCityName() {
		return city_name;
	}
	
	public float getLat() {
		return lat;
	}
	
	public float getLon() {
		return lon;
	}

}
