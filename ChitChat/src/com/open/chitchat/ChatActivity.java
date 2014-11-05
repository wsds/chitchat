package com.open.chitchat;

import com.open.chitchat.controller.ChatController;
import com.open.chitchat.model.ActivityManager;
import com.open.chitchat.view.ChatView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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
	protected void onDestroy() {
		this.mActivityManager.mChatActivity = null;
		super.onDestroy();
	}

}
