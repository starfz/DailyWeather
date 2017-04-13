package com.dailyweather.app.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.dailyweather.app.R;
import com.dailyweather.app.db.County;
import com.dailyweather.app.db.DailyWeatherDB;
import com.dailyweather.app.util.HttpCallbackListener;
import com.dailyweather.app.util.HttpUtil;
import com.dailyweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private float lat_now;  //γ��
	private float lon_now; //����
	private static final int DAY = 3;
	private DailyWeatherDB dwDB;
	private String county_code;  //��ǰ���ڵس��б��
	
	//����������ʾ�������
	private TextView date_1;
	private TextView date_2;
	private TextView date_3;
	private ImageView weather_pic_1;
	private ImageView weather_pic_2;
	private ImageView weather_pic_3;
	private TextView weather_txt_1;
	private TextView weather_txt_2;
	private TextView weather_txt_3;
	private TextView min_max_temp_1;
	private TextView min_max_temp_2;
	private TextView min_max_temp_3;
	
	//ʵ��������ʾ�������
	private TextView pcc_name;
	private ImageView weather_pic;
	private TextView weather_txt;
	private TextView now_temp;
	private TextView wind_dir;
	private TextView wind_lv;
	
	private Button home;
	private Button refresh;
	
	private ProgressDialog pd_loadcity;
	private ProgressDialog pd_getlocation;
	
	private CountDownLatch count;

	private String provider;
	private LocationManager locationManager;
	
	private final static String WEATHER_URI = "https://api.heweather.com/v5/weather?city=";
	private final static String WEATHER_API_KEY = "&key=c8e26f8a893a4f8a8ad72527b4d55e63&lang=zh-cn";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_main);
		
		dwDB = DailyWeatherDB.getInstance(this);
		date_1 = (TextView)findViewById(R.id.date_1);
		date_2 = (TextView)findViewById(R.id.date_2);
		date_3 = (TextView)findViewById(R.id.date_3);
		weather_pic_1 = (ImageView)findViewById(R.id.weather_pic_1);
		weather_pic_2 = (ImageView)findViewById(R.id.weather_pic_2);
		weather_pic_3 = (ImageView)findViewById(R.id.weather_pic_3);
		weather_txt_1 = (TextView)findViewById(R.id.weather_txt_1);
		weather_txt_2 = (TextView)findViewById(R.id.weather_txt_2);
		weather_txt_3 = (TextView)findViewById(R.id.weather_txt_3);
		min_max_temp_1 = (TextView)findViewById(R.id.min_max_temp_1);
		min_max_temp_2 = (TextView)findViewById(R.id.min_max_temp_2);
		min_max_temp_3 = (TextView)findViewById(R.id.min_max_temp_3);
		
		pcc_name = (TextView)findViewById(R.id.pcc_name);
		weather_pic = (ImageView)findViewById(R.id.weather_pic);
		weather_txt = (TextView)findViewById(R.id.weather_txt);
		now_temp = (TextView)findViewById(R.id.temp_now);
		wind_dir = (TextView)findViewById(R.id.wind_dir);
		wind_lv = (TextView)findViewById(R.id.wind_lv);
		
		home = (Button)findViewById(R.id.home);
		refresh = (Button)findViewById(R.id.refresh);
		
		count = new CountDownLatch(1);
		
		List<County> county_list = new ArrayList<County>();
		county_list = dwDB.loadAllCounty();
		if(county_list.size() < 2560) {//�����ݿ����ݲ�ȫʱ�����г������ݳ�ʼ������
			showPD_LoadCity(); //��ʾ������
			SavePCC();  //�����������ݣ���ȡ��������json�ļ�
			try {
				count.await();  //�ȴ����߳���ɷ��ؽ��
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			closePD_LoadCity(); //�������ݳ�ʼ�����
			showPD_GetLocation();
			getLatAndLon();  //��λ��ǰ��γ��
			county_code = getLocation(lat_now, lon_now);  //���ݵ�ǰ��γ���������ϵĳ��У���ȡ���б��
			closePD_GetLocation();
			//���ݳ��б�ţ�ȷ��API�ӿڵĵ�ַ
			String address_weather = WEATHER_URI + county_code + WEATHER_API_KEY;
			SaveThreeDayWeather(address_weather);  //��ȡ��ǰ���ڵس��е�����������Ϣ��������ʾ
			SaveNowWeather(address_weather);  //��ȡ��ǰ���ڳ��е�ʵ��������Ϣ��������ʾ
		} else {
			String str = getIntent().getStringExtra("select_county");
			if(!TextUtils.isEmpty(str)) { //�ӳ����б��й���ʱ��county_code����ѡ�г��е�code
				county_code = str;
			} else {  //���򣬾͸��ݾ�γ������
				showPD_GetLocation();
				getLatAndLon();  //��λ��ǰ��γ��
				county_code = getLocation(lat_now, lon_now);  //���ݵ�ǰ��γ���������ϵĳ��У���ȡ���б��
				closePD_GetLocation();
			}
			
			//���ݳ��б�ţ�ȷ��API�ӿڵĵ�ַ
			String address_weather = WEATHER_URI + county_code + WEATHER_API_KEY;
			SaveThreeDayWeather(address_weather);  //��ȡ��ǰ���ڵس��е�����������Ϣ��������ʾ
			SaveNowWeather(address_weather);  //��ȡ��ǰ���ڳ��е�ʵ��������Ϣ��������ʾ
		}
		
		home.setOnClickListener(this);
		refresh.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.home:
			Intent intent = new Intent(this, PCCViewActivity.class);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh:
			//���ݳ��б�ţ�ȷ��API�ӿڵĵ�ַ
			String address_weather = WEATHER_URI + county_code + WEATHER_API_KEY;
			SaveThreeDayWeather(address_weather);  //��ȡ��ǰ���ڵس��е�����������Ϣ��������ʾ
			SaveNowWeather(address_weather);  //��ȡ��ǰ���ڳ��е�ʵ��������Ϣ��������ʾ
			Animation anim = AnimationUtils.loadAnimation(this, R.anim.refresh_anim);
			refresh.startAnimation(anim);
			break;
		default:
			break;
		}
	}
	
	//��������Ϣ���浽���ݿ���
	public void SavePCC() {
		String address_city = "http://files.heweather.com/china-city-list.json";
		//�������д��벢���浽���ݿ���
		HttpUtil.sendHttpRequest(address_city, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Utility.handlePCCResponse(dwDB, response);  //��json�ļ��������������ݿ�
				count.countDown();
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread (new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "�������ݴ洢ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//��������������SharedPreferences�ļ�
	public void SaveThreeDayWeather(String address) {
		HttpUtil.sendHttpRequest(address,  new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Utility.handleThreeDayWeatherResponse(MainActivity.this, response);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ShowThreeDayWeather();
					}
				});
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread (new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "�����������ݴ洢ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//��ʵ����������SharedPreferences�ļ�
	public void SaveNowWeather(String address) {
		HttpUtil.sendHttpRequest(address,  new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				Utility.handleNowWeatherResponse(MainActivity.this, response);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ShowNowWeather();
					}
				});
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread (new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "ʵ���������ݴ洢ʧ��", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//��ʾ��������
	public void ShowThreeDayWeather() {
		SharedPreferences pre = getSharedPreferences("threeday", MODE_PRIVATE);
		String[] min_temp = new String[DAY];  //������ÿ����������
		String[] max_temp = new String[DAY]; //������ÿ����������
		String[] code_d = new String[DAY];  //�����ڵ�����������루���죩
		String[] code_n = new String[DAY];  //�����ڵ�����������루ҹ�䣩
		String[] txt_d = new String[DAY]; //�����ڵ�����������������죩
		String[] txt_n = new String[DAY];  //�����ڵ��������������ҹ�䣩
		String[] date = new String[DAY];  //���������
		for(int i=0; i<DAY; i++) {
			min_temp[i] = pre.getString("min_temp_" + i, "");
			max_temp[i] = pre.getString("max_temp_" + i, "");
			code_d[i] = pre.getString("code_d_" + i, "");
			code_n[i] = pre.getString("code_n_" + i, "");
			txt_d[i] = pre.getString("txt_d_" + i, "");
			txt_n[i] = pre.getString("txt_n_" + i, "");
			date[i] = pre.getString("date_" + i, "");
		}
		date_1.setText("����");
		date_2.setText(date[1]);
		date_3.setText(date[2]);
		int hour = getHour();
		if(hour>5 && hour<=17) {  //����5�㵽����5�㣬��ʾ������������
			Resources resources = this.getResources();
			int image_id_1 = resources.getIdentifier("com.dailyweather.app:drawable/w" + code_d[0], null, null);
			weather_pic_1.setImageResource(image_id_1);
			int image_id_2 = resources.getIdentifier("com.dailyweather.app:drawable/w" + code_d[1], null, null);
			weather_pic_2.setImageResource(image_id_2);
			int image_id_3 = resources.getIdentifier("com.dailyweather.app:drawable/w" + code_d[2], null, null);
			weather_pic_3.setImageResource(image_id_3);
			
			weather_txt_1.setText(txt_d[0]);
			weather_txt_2.setText(txt_d[1]);
			weather_txt_3.setText(txt_d[2]);
		} else {   //����ʱ����ʾҹ����������
			Resources resources = this.getResources();
			int image_id_1 = resources.getIdentifier("com.dailyweather.app:drawable/w" + code_n[0], null, null);
			weather_pic_1.setImageResource(image_id_1);
			int image_id_2 = resources.getIdentifier("com.dailyweather.app:drawable/w" + code_n[1], null, null);
			weather_pic_2.setImageResource(image_id_2);
			int image_id_3 = resources.getIdentifier("com.dailyweather.app:drawable/w" + code_n[2], null, null);
			weather_pic_3.setImageResource(image_id_3);

			weather_txt_1.setText(txt_n[0]);
			weather_txt_2.setText(txt_n[1]);
			weather_txt_3.setText(txt_n[2]);
		}
		min_max_temp_1.setText(min_temp[0] + "~" + max_temp[0] + "��");
		min_max_temp_2.setText(min_temp[1] + "~" + max_temp[1] + "��");
		min_max_temp_3.setText(min_temp[2] + "~" + max_temp[2] + "��");
	}
	
	//��ʾʵ������
	public void ShowNowWeather() {
		SharedPreferences pre = getSharedPreferences("now", MODE_PRIVATE);
		String county_name = pre.getString("county_name", "");
		String temp = pre.getString("now_temp", "");
		String code = pre.getString("now_code", "");
		String now_txt = pre.getString("now_txt", "");
		String w_dir = pre.getString("wind_dir", "");
		String w_lv = pre.getString("wind_lv", "");
		
		pcc_name.setText(county_name);
		weather_txt.setText(now_txt);
		now_temp.setText(temp + "��");
		wind_dir.setText(w_dir);
		wind_lv.setText("������" + w_lv + "��");
		
		Resources resources = this.getResources();
		int image_id = resources.getIdentifier("com.dailyweather.app:drawable/w" + code, null, null);
		weather_pic.setImageResource(image_id);
	}
	
	//��ȡ��ǰʱ�䣬24Сʱ��
	public int getHour() {
		int hour;
		
		Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		
		return hour;
	}
	
	//��ȡ��ǰλ�����ڵľ�γ��
	public void getLatAndLon() {
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		List<String> providerList = locationManager.getProviders(true);
//		if(providerList.contains(LocationManager.GPS_PROVIDER)) {
//			provider = LocationManager.GPS_PROVIDER;
//		} else if(providerList.contains(LocationManager.NETWORK_PROVIDER)) {
//			provider = LocationManager.NETWORK_PROVIDER;
//		} else {
//			Toast.makeText(this, "�޷���ȡ����λ����Ϣ", Toast.LENGTH_SHORT).show();
//			return;
//		}
		
		provider = locationManager.getBestProvider(new Criteria(), true);
		
		Location location = locationManager.getLastKnownLocation(provider);
		Toast.makeText(this, lat_now + "-" + lon_now, Toast.LENGTH_SHORT).show();
		if(location != null) {
			lat_now = (float)location.getLatitude();
			lon_now = (float)location.getLongitude();
			Log.d("MainActivity", lat_now + "x" + lon_now);
			Toast.makeText(this, lat_now + "x" + lon_now, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "�޷���ȡ����λ����Ϣ", Toast.LENGTH_SHORT).show();
		}
	}
	
	//���ݵ�ǰ�ľ�γ��ȷ�����ڵĳ��У����س��б��
	public String getLocation(float lat, float lon) {
		
		List<County> countyList;
		countyList = dwDB.loadAllCounty();
		
		String[] countyCode = new String[countyList.size()];
		float[] countyLat = new float[countyList.size()];
		float[] countyLon = new float[countyList.size()];
		float[] temp = new float[countyList.size()];
		
		if(countyList.size() > 0) {//��ȡ�����صı�š���γ��
			int i = 0;
			for(County county : countyList) {
				countyCode[i] = county.getCountyCode();
				countyLat[i] = county.getLat();
				countyLon[i] = county.getLon();
				temp[i] = Math.abs(lat - countyLat[i]) + Math.abs(lon - countyLon[i]);
				i++;
			}
		}
		float mintemp = temp[0];
		int k = 0;
		
		//�������ݣ�ȡ�뵱ǰ���ھ�γ�Ȳ�ֵ����С�ĳ�����Ϊ��ǰ���У���������
		for(int j=0; j < countyList.size(); j++) {
			if(temp[j] < mintemp) {
				mintemp = temp[j];
				k = j;
			}
		}
		
		if(mintemp > 5) {//��������г��еľ�γ�Ȳ�ֵ�͹���ʱ���᷵�ص�һ���صı��
			return countyCode[0];
		} else {
			return countyCode[k];  
		}
	}
	
	public void showPD_LoadCity() {
		if(pd_loadcity == null) {
			pd_loadcity = new ProgressDialog(this);
			pd_loadcity.setMessage("�������ݳ�ʼ���У������ĵȴ�");
			pd_loadcity.setCancelable(false);
			pd_loadcity.setCanceledOnTouchOutside(false);
		}
		pd_loadcity.show();
	}
	
	public void showPD_GetLocation() {
		if(pd_getlocation == null) {
			pd_getlocation = new ProgressDialog(this);
			pd_getlocation.setMessage("���ڶ�λ�С���");
			pd_getlocation.setCancelable(false);
			pd_getlocation.setCanceledOnTouchOutside(false);
		}
		pd_getlocation.show();
	}
	
	public void closePD_LoadCity() {
		if(pd_loadcity != null) {
			pd_loadcity.dismiss();
		}
	}
	
	public void closePD_GetLocation() {
		if(pd_getlocation != null) {
			pd_getlocation.dismiss();
		}
	}

}
