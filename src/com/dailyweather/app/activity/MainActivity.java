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
	
	private float lat_now;  //纬度
	private float lon_now; //经度
	private static final int DAY = 3;
	private DailyWeatherDB dwDB;
	private String county_code;  //当前所在地城市编号
	
	//三日天气显示所需变量
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
	
	//实况天气显示所需变量
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
		if(county_list.size() < 2560) {//当数据库数据不全时，进行城市数据初始化操作
			showPD_LoadCity(); //显示加载中
			SavePCC();  //解析城市数据，获取城市数据json文件
			try {
				count.await();  //等待子线程完成返回结果
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			closePD_LoadCity(); //城市数据初始化完毕
			showPD_GetLocation();
			getLatAndLon();  //定位当前经纬度
			county_code = getLocation(lat_now, lon_now);  //根据当前经纬度搜索符合的城市，获取城市编号
			closePD_GetLocation();
			//根据城市编号，确定API接口的地址
			String address_weather = WEATHER_URI + county_code + WEATHER_API_KEY;
			SaveThreeDayWeather(address_weather);  //获取当前所在地城市的三日天气信息并进行显示
			SaveNowWeather(address_weather);  //获取当前所在城市的实况天气信息并进行显示
		} else {
			String str = getIntent().getStringExtra("select_county");
			if(!TextUtils.isEmpty(str)) { //从城市列表中过来时，county_code就是选中城市的code
				county_code = str;
			} else {  //否则，就根据经纬度搜索
				showPD_GetLocation();
				getLatAndLon();  //定位当前经纬度
				county_code = getLocation(lat_now, lon_now);  //根据当前经纬度搜索符合的城市，获取城市编号
				closePD_GetLocation();
			}
			
			//根据城市编号，确定API接口的地址
			String address_weather = WEATHER_URI + county_code + WEATHER_API_KEY;
			SaveThreeDayWeather(address_weather);  //获取当前所在地城市的三日天气信息并进行显示
			SaveNowWeather(address_weather);  //获取当前所在城市的实况天气信息并进行显示
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
			//根据城市编号，确定API接口的地址
			String address_weather = WEATHER_URI + county_code + WEATHER_API_KEY;
			SaveThreeDayWeather(address_weather);  //获取当前所在地城市的三日天气信息并进行显示
			SaveNowWeather(address_weather);  //获取当前所在城市的实况天气信息并进行显示
			Animation anim = AnimationUtils.loadAnimation(this, R.anim.refresh_anim);
			refresh.startAnimation(anim);
			break;
		default:
			break;
		}
	}
	
	//将城市信息保存到数据库中
	public void SavePCC() {
		String address_city = "http://files.heweather.com/china-city-list.json";
		//解析城市代码并保存到数据库中
		HttpUtil.sendHttpRequest(address_city, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Utility.handlePCCResponse(dwDB, response);  //将json文件解析并存入数据库
				count.countDown();
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread (new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this, "城市数据存储失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//将三日天气存入SharedPreferences文件
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
						Toast.makeText(MainActivity.this, "三日天气数据存储失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//将实况天气存入SharedPreferences文件
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
						Toast.makeText(MainActivity.this, "实况天气数据存储失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//显示三日天气
	public void ShowThreeDayWeather() {
		SharedPreferences pre = getSharedPreferences("threeday", MODE_PRIVATE);
		String[] min_temp = new String[DAY];  //三天内每天的最低气温
		String[] max_temp = new String[DAY]; //三天内每天的最高气温
		String[] code_d = new String[DAY];  //三天内的天气情况代码（白天）
		String[] code_n = new String[DAY];  //三天内的天气情况代码（夜间）
		String[] txt_d = new String[DAY]; //三天内的天气情况描述（白天）
		String[] txt_n = new String[DAY];  //三天内的天气情况描述（夜间）
		String[] date = new String[DAY];  //三天的日期
		for(int i=0; i<DAY; i++) {
			min_temp[i] = pre.getString("min_temp_" + i, "");
			max_temp[i] = pre.getString("max_temp_" + i, "");
			code_d[i] = pre.getString("code_d_" + i, "");
			code_n[i] = pre.getString("code_n_" + i, "");
			txt_d[i] = pre.getString("txt_d_" + i, "");
			txt_n[i] = pre.getString("txt_n_" + i, "");
			date[i] = pre.getString("date_" + i, "");
		}
		date_1.setText("今天");
		date_2.setText(date[1]);
		date_3.setText(date[2]);
		int hour = getHour();
		if(hour>5 && hour<=17) {  //上午5点到下午5点，显示白天的天气情况
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
		} else {   //其余时间显示夜晚的天气情况
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
		min_max_temp_1.setText(min_temp[0] + "~" + max_temp[0] + "℃");
		min_max_temp_2.setText(min_temp[1] + "~" + max_temp[1] + "℃");
		min_max_temp_3.setText(min_temp[2] + "~" + max_temp[2] + "℃");
	}
	
	//显示实况天气
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
		now_temp.setText(temp + "℃");
		wind_dir.setText(w_dir);
		wind_lv.setText("风力：" + w_lv + "级");
		
		Resources resources = this.getResources();
		int image_id = resources.getIdentifier("com.dailyweather.app:drawable/w" + code, null, null);
		weather_pic.setImageResource(image_id);
	}
	
	//获取当前时间，24小时制
	public int getHour() {
		int hour;
		
		Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		
		return hour;
	}
	
	//获取当前位置所在的经纬度
	public void getLatAndLon() {
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//		List<String> providerList = locationManager.getProviders(true);
//		if(providerList.contains(LocationManager.GPS_PROVIDER)) {
//			provider = LocationManager.GPS_PROVIDER;
//		} else if(providerList.contains(LocationManager.NETWORK_PROVIDER)) {
//			provider = LocationManager.NETWORK_PROVIDER;
//		} else {
//			Toast.makeText(this, "无法获取地理位置信息", Toast.LENGTH_SHORT).show();
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
			Toast.makeText(this, "无法获取地理位置信息", Toast.LENGTH_SHORT).show();
		}
	}
	
	//根据当前的经纬度确定所在的城市，返回城市编号
	public String getLocation(float lat, float lon) {
		
		List<County> countyList;
		countyList = dwDB.loadAllCounty();
		
		String[] countyCode = new String[countyList.size()];
		float[] countyLat = new float[countyList.size()];
		float[] countyLon = new float[countyList.size()];
		float[] temp = new float[countyList.size()];
		
		if(countyList.size() > 0) {//获取所有县的编号、经纬度
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
		
		//遍历数据，取与当前所在经纬度差值和最小的城市作为当前城市，返回其编号
		for(int j=0; j < countyList.size(); j++) {
			if(temp[j] < mintemp) {
				mintemp = temp[j];
				k = j;
			}
		}
		
		if(mintemp > 5) {//与国内所有城市的经纬度差值和过大时，会返回第一个县的编号
			return countyCode[0];
		} else {
			return countyCode[k];  
		}
	}
	
	public void showPD_LoadCity() {
		if(pd_loadcity == null) {
			pd_loadcity = new ProgressDialog(this);
			pd_loadcity.setMessage("城市数据初始化中，请耐心等待");
			pd_loadcity.setCancelable(false);
			pd_loadcity.setCanceledOnTouchOutside(false);
		}
		pd_loadcity.show();
	}
	
	public void showPD_GetLocation() {
		if(pd_getlocation == null) {
			pd_getlocation = new ProgressDialog(this);
			pd_getlocation.setMessage("正在定位中……");
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
