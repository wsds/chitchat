package com.open.chitchat;

import com.open.chitchat.controller.ChatController;
import com.open.chitchat.view.ChatView;

import android.app.Activity;
import android.os.Bundle;

public class ChatActivity extends Activity {

	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.thisActivity = this;
		this.thisView = new ChatView(thisActivity);
		this.thisController = new ChatController(thisActivity);

		this.thisView.thisController = this.thisController;
		this.thisController.thisView = this.thisView;

		this.thisView.initViews();
		this.thisController.onCreate();
	}

}
