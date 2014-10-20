package com.open.chitchat.listener;

import com.open.chitchat.R;

import android.view.View;
import android.view.View.OnClickListener;

public class MyOnClickListener implements OnClickListener {

	public long defaultTime = 800;

	public MyOnClickListener() {
	}

	public MyOnClickListener(long time) {
		this.defaultTime = time;
	}

	@Override
	public void onClick(View view) {
		long currentTime = System.currentTimeMillis();
		if (view.getTag(R.id.time) != null) {
			try {
				long lastClickTime = (Long) view.getTag(R.id.time);
				long time = currentTime - lastClickTime;
				if (0 < time && time > defaultTime) {
					view.setTag(R.id.time, currentTime);
					onClickEffective(view);
				}
			} catch (Exception e) {
			}
		} else {
			view.setTag(R.id.time, currentTime);
			onClickEffective(view);
		}
	}

	public void onClickEffective(View view) {
	};
}
