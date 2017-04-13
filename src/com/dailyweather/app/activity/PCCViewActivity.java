package com.dailyweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.dailyweather.app.R;
import com.dailyweather.app.db.City;
import com.dailyweather.app.db.County;
import com.dailyweather.app.db.DailyWeatherDB;
import com.dailyweather.app.db.Province;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class PCCViewActivity extends Activity implements OnClickListener {
	
	private Button back;
	private TextView city_name;
	private ListView city_list;
	
	private static final int SIGN_PROVINCE = 0;
	private static final int SIGN_CITY = 1;
	private static final int SIGN_COUNTY = 2;
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private List<String> dataList = new ArrayList<String>();	
	
	private ArrayAdapter<String> adapter;
	private DailyWeatherDB dwDB;
	
	private Province selectProvince;
	private City selectCity;
	private County selectCounty;
	
	private int select_now;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_pcc);
		
		city_list = (ListView)findViewById(R.id.city_list);
		back = (Button)findViewById(R.id.back);
		city_name = (TextView)findViewById(R.id.city_name);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		city_list.setAdapter(adapter);
		
		dwDB = DailyWeatherDB.getInstance(this);
		
		city_list.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> arg, View view, int position, long id) {
				if(select_now == SIGN_PROVINCE) {//如果当前选中的是省一级的数据，则去查找对应的市信息
					selectProvince = provinceList.get(position);
					queryCity();
				} else if(select_now == SIGN_CITY) {//如果当前选中的是市一级的数据，则去查找对应的县信息
					selectCity = cityList.get(position);
					queryCounty();
				} else if(select_now == SIGN_COUNTY) {//如果当前选中的是县一级的数据，则跳转到天气界面显示选中县的天气
					selectCounty = countyList.get(position);
					String county_code = selectCounty.getCountyCode();
					Log.d("MainActivity", county_code);
					Intent intent = new Intent(PCCViewActivity.this, MainActivity.class);
					intent.putExtra("select_county", county_code);
					startActivity(intent);
					finish();
				}
			}
		});
		
		back.setOnClickListener(this);
		
		queryProvince();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.back:
			if(select_now == SIGN_COUNTY) {//如果在县列表界面，则返回市信息界面
				queryCity();
			} else if(select_now == SIGN_CITY) {//如果在市列表界面，则返回省信息界面
				queryProvince();
			} else if(select_now == SIGN_PROVINCE) {//如果在省列表界面，则跳转到天气信息界面，显示默认位置天气
				Intent intent = new Intent(PCCViewActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}
	}
	
	//从数据库中查找省份数据
	public void queryProvince() {
		provinceList = dwDB.loadProvince();
		if(provinceList.size() > 0) {
			dataList.clear();
			for(Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			city_list.setSelection(0);
			city_name.setText("中国");
			select_now = SIGN_PROVINCE;
		} else {
			Log.d("PCCActivity", "省份信息获取失败");
		}
	}
	
	//从数据库中根据选中的省份查找对应的市信息
	public void queryCity() {
		cityList = dwDB.loadCity(selectProvince.getProvinceName());
		if(cityList.size() > 0) {
			dataList.clear();
			for(City city:cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			city_list.setSelection(0);
			city_name.setText(selectProvince.getProvinceName());
			select_now  = SIGN_CITY;
		} else {
			Log.d("PCCActivity", "市区信息获取失败");
		}
	}
	
	//从数据库中根据选中的市查找对应的县信息
	public void queryCounty() {
		countyList = dwDB.loadCounty(selectCity.getCityName());
		if(countyList.size() > 0) {
			dataList.clear();
			for(County county:countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			city_list.setSelection(0);
			city_name.setText(selectCity.getCityName());
			select_now = SIGN_COUNTY;
		} else {
			Log.d("PCCActivity", "县信息获取失败");
		}
	}
	
	@Override
	public void onBackPressed() {
		if(select_now == SIGN_COUNTY) {//如果在县列表界面，则返回市信息界面
			queryCity();
		} else if(select_now == SIGN_CITY) {//如果在市列表界面，则返回省信息界面
			queryProvince();
		} else if(select_now == SIGN_PROVINCE) {//如果在省列表界面，则跳转到天气信息界面，显示默认位置天气
			Intent intent = new Intent(PCCViewActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

}
