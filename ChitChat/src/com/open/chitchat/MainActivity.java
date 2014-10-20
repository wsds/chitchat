package com.open.chitchat;

import com.open.chitchat.fragment.ChatListFragment;
import com.open.chitchat.fragment.FindFragment;
import com.open.chitchat.fragment.FriendFragment;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Parser;
import com.open.chitchat.service.PushService;
import com.open.chitchat.view.MainTabView;
import com.open.chitchat.view.MainTabView.OnTagClickListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
	public Data data = Data.getInstance();
	public Parser parser = Parser.getInstance();

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
		startPushService();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (chatFragment.isVisible()) {
				((ChatListFragment) chatFragment).changePopMenuView();
			} else if (friendFragment.isVisible()) {
				((FriendFragment) friendFragment).changePopMenuView();
			} else if (findFragment.isVisible()) {
				((FindFragment) findFragment).changePopMenuView();
			}
		}
		return false;
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

		chatFragment = new ChatListFragment();
		friendFragment = new FriendFragment();
		findFragment = new FindFragment();

		setDefaultFragment();
	}

	private void setDefaultFragment() {
		mTransaction.add(R.id.mainView, chatFragment);
		mTransaction.add(R.id.mainView, friendFragment);
		mTransaction.add(R.id.mainView, findFragment);

		mTransaction.show(chatFragment);
		mTransaction.hide(friendFragment);
		mTransaction.hide(findFragment);

		mTransaction.commit();
		mainTabView.setDefaultItem(1);
	}

	private void showFragment(int positions, int lastposition) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (positions == 1) {
			// transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			transaction.hide(friendFragment);
			transaction.hide(findFragment);
			transaction.show(chatFragment);
		} else if (positions == 2) {
			// transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			transaction.hide(chatFragment);
			transaction.hide(findFragment);
			transaction.show(friendFragment);
		} else if (positions == 3) {
			// transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			transaction.hide(friendFragment);
			transaction.hide(chatFragment);
			transaction.show(findFragment);
		}
		transaction.commit();
	}

	public void startPushService() {
		Intent service = new Intent(this, PushService.class);
		PushService.isRunning = false;
		parser.initialize(this);
		data = parser.check();
		service.putExtra("phone", "151");
		service.putExtra("accessKey", "lejoying");
		service.putExtra("operation", true);
		startService(service);
	}
}
