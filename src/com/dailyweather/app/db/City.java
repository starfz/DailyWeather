package com.dailyweather.app.db;

public class City {
	
	private int id;
	private String city_name;
	private String province_name;  //¹ØÁªProvince±íprovince_name×Ö¶Î
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setCityName(String city_name) {
		this.city_name = city_name;
	}
	
	public void setProvinceName(String province_name) {
		this.province_name = province_name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getCityName() {
		return city_name;
	}
	
	public String getProvinceName() {
		return province_name;
	}

}
