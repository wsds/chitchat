package com.open.chitchat.model;

import com.open.chitchat.LoginActivity;

public class ActivityManager {

	public static ActivityManager manager;

	public static ActivityManager getInstance() {
		if (manager == null) {
			manager = new ActivityManager();
		}
		return manager;
	}

	public LoginActivity mLoginActivity;
}
