package com.open.chitchat;

import com.open.chitchat.controller.BusinessController;
import com.open.chitchat.view.BusinessView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class BusinessActivity extends Activity {

	public BusinessActivity thisActivity;
	public BusinessView thisView;
	public BusinessController thisController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.thisActivity = this;
		this.thisView = new BusinessView(thisActivity);
		this.thisController = new BusinessController(thisActivity);

		this.thisView.thisController = this.thisController;
		this.thisController.thisView = this.thisView;

		this.thisView.onCreate();
		this.thisController.onCreate();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return thisController.onKeyDown(keyCode, event) ? true : super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		thisController.onDestroy();
		super.onDestroy();
	}
}
