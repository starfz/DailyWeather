package com.dailyweather.app.activity;

import com.dailyweather.app.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class LoadActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_load);
		
		new Handler().postDelayed(new Runnable() {
			@Override 
			public void run() {
				Intent intent = new Intent(LoadActivity.this, MainActivity.class);
				LoadActivity.this.startActivity(intent);
				LoadActivity.this.finish();
			}
		}, 2000);
	}
}
