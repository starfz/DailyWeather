package com.dailyweather.app.db;

public class County {
	
	private int id;
	private String county_code;
	private String county_name;
	private String city_name;  //����City��city_name�ֶ�
	private float lat;  //γ�ȣ����ں�����λ����
	private float lon; //���ȣ����ں�����λ����
	
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
