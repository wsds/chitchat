package com.open.chitchat;

import com.open.chitchat.controller.ChatController;
import com.open.chitchat.model.ActivityManager;
import com.open.chitchat.view.ChatView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;

public class ChatActivity extends Activity {

	public ActivityManager mActivityManager = ActivityManager.getInstance();

	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;
	public LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mActivityManager.mChatActivity = this;
		this.mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.thisActivity = this;
		this.thisView = new ChatView(thisActivity);
		this.thisController = new ChatController(thisActivity);

		this.thisView.thisController = this.thisController;
		this.thisController.thisView = this.thisView;

		this.thisView.initViews();
		this.thisController.onCreate();
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.thisController.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		this.thisController.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		this.thisController.onDestroy();
		super.onDestroy();
	}

}
