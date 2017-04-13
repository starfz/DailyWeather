package com.dailyweather.app.util;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dailyweather.app.db.City;
import com.dailyweather.app.db.County;
import com.dailyweather.app.db.DailyWeatherDB;
import com.dailyweather.app.db.Province;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

//�Դ�ָ�������ַ��ȡ��response���н���
public class Utility {
	
	private static final int DAY = 3;
	
	//����������Ϣ���������ݿ�
	public  static void handlePCCResponse(DailyWeatherDB dailyweatherDB, String response) {
		String[] temp = response.split("\\[");  //json�ļ�ǰ�в��淶���ݣ�ȥ�����淶����
		String response_new = "[" + temp[1];
		if(!TextUtils.isEmpty(response_new)) {
			try {
				JSONArray jsonArray = new JSONArray(response_new);
				for(int i = 0; i<jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String id = jsonObject.getString("id");
					String province_name = jsonObject.getString("provinceZh");
					String city_name = jsonObject.getString("leaderZh");
					String county_name = jsonObject.getString("cityZh");
					float lat = Float.parseFloat(jsonObject.getString("lat"));
					float lon = Float.parseFloat(jsonObject.getString("lon"));
					
					//��ȡʡ��Ϣ����Province��
					Province province = new Province();
					province.setProvinceName(province_name);
					dailyweatherDB.saveProvince(province);
					
					//��ȡ����Ϣ����City��
					City city = new City();
					city.setCityName(city_name);
					city.setProvinceName(province_name);
					dailyweatherDB.saveCity(city);
					
					//��ȡ����Ϣ����County��
					County county = new County();
					county.setCountyCode(id);
					county.setCountyName(county_name);
					county.setCityName(city_name);
					county.setLat(lat);
					county.setLon(lon);
					dailyweatherDB.saveCounty(county);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.d("MainActivity", "������Ϣ��ȡʧ��");
		}
	}
	
	//����3��������Ԥ��������SharedPreferences�ļ���
	public static void handleThreeDayWeatherResponse(Context context, String response) {
		if(!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonObject1 = new JSONObject(response);
				JSONArray jsonArray1 = jsonObject1.getJSONArray("HeWeather5");
				JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
				
				String[] min_temp = new String[DAY];  //������ÿ����������
				String[] max_temp = new String[DAY]; //������ÿ����������
				String[] code_d = new String[DAY];  //�����ڵ�����������루���죩
				String[] code_n = new String[DAY];  //�����ڵ�����������루ҹ�䣩
				String[] txt_d = new String[DAY]; //�����ڵ�����������������죩
				String[] txt_n = new String[DAY];  //�����ڵ��������������ҹ�䣩
				String[] date = new String[DAY];  //���������
				JSONArray jsonArray2 = jsonObject2.getJSONArray("daily_forecast");
				for(int i = 0; i<DAY; i++) {//ȡ�����ڵ�����Ԥ����Ϣ
					JSONObject  jsonObject4 = jsonArray2.getJSONObject(i);
					date[i] = jsonObject4.getString("date");
					JSONObject jsonObject5 = jsonObject4.getJSONObject("cond");
					code_d[i] = jsonObject5.getString("code_d");
					code_n[i] = jsonObject5.getString("code_n");
					txt_d[i] = jsonObject5.getString("txt_d");
					txt_n[i] = jsonObject5.getString("txt_n");
					JSONObject jsonObject6 = jsonObject4.getJSONObject("tmp");
					min_temp[i] = jsonObject6.getString("min");
					max_temp[i] = jsonObject6.getString("max");
				}
				
				//��������Ϣ����SharedPreferences�ļ�
				SharedPreferences.Editor editor = context.getSharedPreferences("threeday", Context.MODE_PRIVATE).edit();
				//�ӵ�һ��ѭ���ύ��������
				for(int i = 0; i<DAY;  i++) {
					editor.putString("min_temp_"+ i, min_temp[i]);
					editor.putString("max_temp_"+ i, max_temp[i]);
					editor.putString("code_d_"+ i, code_d[i]);
					editor.putString("code_n_" + i, code_n[i]);
					editor.putString("txt_d_" + i, txt_d[i]);
					editor.putString("txt_n_" + i, txt_n[i]);
					editor.putString("date_" + i, date[i]);
				}
				editor.commit();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.d("ThreeDayWeather", "3����Ԥ����Ϣ��ȡʧ��");
		}
	}
	
	
	//������ǰʵ������Ԥ��������SharedPreferences�ļ���
	public static void handleNowWeatherResponse(Context context, String response) {
		if(!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonObject1 = new JSONObject(response);
				JSONArray jsonArray1 = jsonObject1.getJSONArray("HeWeather5");
				JSONObject jsonObject2 = jsonArray1.getJSONObject(0);
				JSONObject jsonObject3 = jsonObject2.getJSONObject("basic");
				String county_name = jsonObject3.getString("city");  //������
				JSONObject jsonObject4 = jsonObject2.getJSONObject("now");
				String now_temp = jsonObject4.getString("tmp");  //��ǰʵ������
				String now_body_temp = jsonObject4.getString("fl");  //��ǰ����¶�
				JSONObject jsonObject5 = jsonObject4.getJSONObject("cond");
				String now_code = jsonObject5.getString("code"); //��ǰ����״������
				String now_txt = jsonObject5.getString("txt");  //��ǰ����״������
				JSONObject jsonObject6 = jsonObject4.getJSONObject("wind");
				String wind_lv = jsonObject6.getString("sc");  //��ǰ�����ȼ�
				String wind_dir = jsonObject6.getString("dir");  //��ǰ����
				
				//��������Ϣ����SharedPreferences�ļ�
				SharedPreferences.Editor editor = context.getSharedPreferences("now", Context.MODE_PRIVATE).edit();
				editor.putString("county_name", county_name);
				editor.putString("now_temp", now_temp);
				editor.putString("now_body_temp", now_body_temp);
				editor.putString("now_code", now_code);
				editor.putString("now_txt", now_txt);
				editor.putString("wind_lv", wind_lv);
				editor.putString("wind_dir", wind_dir);
				editor.commit();
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else {
			Log.d("NowWeather", "��ǰ������Ϣ��ȡʧ��");
		}
	}

}
