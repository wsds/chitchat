package com.open.chitchat;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.open.chitchat.utils.BaseDataUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LaunchActivity extends Activity {
	public ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);
		imageLoader.init(ImageLoaderConfiguration.createDefault(LaunchActivity.this));
		BaseDataUtils.initBaseData(this);
		startActivity(new Intent(LaunchActivity.this, LoginMainActivity.class));
		finish();
	}

}
