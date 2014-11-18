package com.open.chitchat.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class ToastUtils {
	private static Handler handler;
	private static Context context;

	public static void init(Context context) {
		ToastUtils.context = context;
		handler = new Handler();
	}

	public static void showToast(final String text) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

			}
		});

	}

	public void showToast(final String text, final int duration) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(context, text, duration).show();
			}
		});

	}
}
