package com.dailyweather.app.db;

public class Province {
	
	private int id;
	private String province_name;
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setProvinceName(String province_name) {
		this.province_name = province_name;
	}
	
	public int getId() {
		return id;
	}
	
	public String getProvinceName() {
		return province_name;
	}

}
