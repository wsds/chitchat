package com.open.chitchat.model;

import com.open.chitchat.BusinessActivity;
import com.open.chitchat.ChatActivity;
import com.open.chitchat.FindListActivity;
import com.open.chitchat.LoginActivity;
import com.open.chitchat.MainActivity;

public class ActivityManager {

	public static ActivityManager manager;

	public static ActivityManager getInstance() {
		if (manager == null) {
			manager = new ActivityManager();
		}
		return manager;
	}

	public LoginActivity mLoginActivity;
	public MainActivity mMainActivity;
	public ChatActivity mChatActivity;
	public BusinessActivity mBusinessActivity;
	public FindListActivity mFindListActivity;

	public void newMessageCallBack(String key) {
		if (this.mChatActivity != null
				&& this.mChatActivity.thisController.key.equals(key)) {
			this.mChatActivity.thisView.mChatAdapter.notifyDataSetChanged();
		} else {

		}

	}
}
