package com.open.chitchat;

import com.open.chitchat.fragment.ChatFragment;
import com.open.chitchat.fragment.FindFragment;
import com.open.chitchat.fragment.FriendFragment;
import com.open.chitchat.view.MainTabView;
import com.open.chitchat.view.MainTabView.OnTagClickListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

	public FrameLayout mainView;

	public MainTabView mainTabView;

	public OnTagClickListener mOnTagClickListener;

	public Fragment chatFragment, friendFragment, findFragment;

	public FragmentManager mFragmentManager;
	public FragmentTransaction mTransaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		initListener();
	}

	private void initListener() {
		mOnTagClickListener = new OnTagClickListener() {

			@Override
			public void OnTagClick(int positions, int lastposition) {
				showFragment(positions, lastposition);
			}
		};
		bindEvent();
	}

	private void bindEvent() {
		mainTabView.setOnTagClickListener(mOnTagClickListener);
	}

	private void initViews() {
		mFragmentManager = this.getFragmentManager();
		mTransaction = mFragmentManager.beginTransaction();

		mainView = (FrameLayout) findViewById(R.id.mainView);
		mainTabView = (MainTabView) findViewById(R.id.mainTabView);

		chatFragment = new ChatFragment();
		friendFragment = new FriendFragment();
		findFragment = new FindFragment();

		setDefaultFragment();
	}

	private void setDefaultFragment() {
		mTransaction.replace(R.id.mainView, chatFragment);
		mTransaction.commit();
	}

	private void showFragment(int positions, int lastposition) {
		if (positions == 1) {
			mTransaction.replace(R.id.mainView, chatFragment);
		} else if (positions == 2) {
			mTransaction.replace(R.id.mainView, friendFragment);
		} else if (positions == 3) {
			mTransaction.replace(R.id.mainView, findFragment);
		}
		
		mTransaction.commit();
	}

}
