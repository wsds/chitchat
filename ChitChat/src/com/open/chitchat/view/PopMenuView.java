package com.open.chitchat.view;

import com.open.chitchat.R;
import com.open.chitchat.listener.MyOnClickListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class PopMenuView extends FrameLayout {
	private Context context;
	private View mainView, user, createGroup, themeShop, expressionStore, setting;
	private MyOnClickListener mOnClickListener;

	public PopMenuView(Context context) {
		super(context);
		this.context = context;
		onCreate();
	}

	public PopMenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		onCreate();
	}

	private void onCreate() {
		mainView = LayoutInflater.from(context).inflate(R.layout.pop_menu, this);
		user = mainView.findViewById(R.id.user);
		createGroup = mainView.findViewById(R.id.createGroup);
		themeShop = mainView.findViewById(R.id.themeShop);
		expressionStore = mainView.findViewById(R.id.expressionStore);
		setting = mainView.findViewById(R.id.setting);

		this.setFocusableInTouchMode(true);

		initListener();
	}

	private void initListener() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.equals(user)) {

				} else if (view.equals(createGroup)) {

				} else if (view.equals(themeShop)) {

				} else if (view.equals(expressionStore)) {

				} else if (view.equals(setting)) {

				}
			}
		};
		bindEvent();
	}

	private void bindEvent() {
		user.setOnClickListener(mOnClickListener);
		createGroup.setOnClickListener(mOnClickListener);
		themeShop.setOnClickListener(mOnClickListener);
		expressionStore.setOnClickListener(mOnClickListener);
		setting.setOnClickListener(mOnClickListener);

	}
}
