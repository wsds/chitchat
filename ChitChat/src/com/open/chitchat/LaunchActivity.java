package com.open.chitchat;

import com.open.chitchat.utils.BaseDataUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class LaunchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		BaseDataUtils.initBaseData(this);
		startActivity(new Intent(LaunchActivity.this, LoginMainActivity.class));
		finish();
	}

}
