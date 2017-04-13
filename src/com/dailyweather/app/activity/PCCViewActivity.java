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
				if(select_now == SIGN_PROVINCE) {//�����ǰѡ�е���ʡһ�������ݣ���ȥ���Ҷ�Ӧ������Ϣ
					selectProvince = provinceList.get(position);
					queryCity();
				} else if(select_now == SIGN_CITY) {//�����ǰѡ�е�����һ�������ݣ���ȥ���Ҷ�Ӧ������Ϣ
					selectCity = cityList.get(position);
					queryCounty();
				} else if(select_now == SIGN_COUNTY) {//�����ǰѡ�е�����һ�������ݣ�����ת������������ʾѡ���ص�����
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
			if(select_now == SIGN_COUNTY) {//��������б���棬�򷵻�����Ϣ����
				queryCity();
			} else if(select_now == SIGN_CITY) {//��������б���棬�򷵻�ʡ��Ϣ����
				queryProvince();
			} else if(select_now == SIGN_PROVINCE) {//�����ʡ�б���棬����ת��������Ϣ���棬��ʾĬ��λ������
				Intent intent = new Intent(PCCViewActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}
	}
	
	//�����ݿ��в���ʡ������
	public void queryProvince() {
		provinceList = dwDB.loadProvince();
		if(provinceList.size() > 0) {
			dataList.clear();
			for(Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			city_list.setSelection(0);
			city_name.setText("�й�");
			select_now = SIGN_PROVINCE;
		} else {
			Log.d("PCCActivity", "ʡ����Ϣ��ȡʧ��");
		}
	}
	
	//�����ݿ��и���ѡ�е�ʡ�ݲ��Ҷ�Ӧ������Ϣ
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
			Log.d("PCCActivity", "������Ϣ��ȡʧ��");
		}
	}
	
	//�����ݿ��и���ѡ�е��в��Ҷ�Ӧ������Ϣ
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
			Log.d("PCCActivity", "����Ϣ��ȡʧ��");
		}
	}
	
	@Override
	public void onBackPressed() {
		if(select_now == SIGN_COUNTY) {//��������б���棬�򷵻�����Ϣ����
			queryCity();
		} else if(select_now == SIGN_CITY) {//��������б���棬�򷵻�ʡ��Ϣ����
			queryProvince();
		} else if(select_now == SIGN_PROVINCE) {//�����ʡ�б���棬����ת��������Ϣ���棬��ʾĬ��λ������
			Intent intent = new Intent(PCCViewActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

}
