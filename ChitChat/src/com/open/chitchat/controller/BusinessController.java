package com.open.chitchat.controller;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Toast;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.chitchat.BusinessActivity;
import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.model.API;
import com.open.chitchat.model.ActivityManager;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.FileHandlers;
import com.open.chitchat.model.ResponseHandlers;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.view.BusinessView;

public class BusinessController {
	public BusinessActivity thisActivity;
	public BusinessView thisView;
	public BusinessController thisController;

	public Data data = Data.getInstance();
	public ActivityManager mActivityManager = ActivityManager.getInstance();
	public FileHandlers fileHandlers = FileHandlers.getInstance();
	public ResponseHandlers responseHandlers = ResponseHandlers.getInstance();

	public String key, type;

	public Status status = Status.SELF;

	public MyOnClickListener mOnClickListener;
	public OnKeyListener mOnKeyListener;

	public enum Status {
		SELF, FRIEND, ATTENTIONS, FANS, TEMPFRIEND, JOINEDGROUP, NOTJOINGROUP
	}

	public BusinessController(BusinessActivity thisActivity) {
		this.thisController = this;
		this.thisActivity = thisActivity;
	}

	public void onCreate() {
		mActivityManager.mBusinessActivity = thisActivity;
		key = thisActivity.getIntent().getStringExtra("key");
		type = thisActivity.getIntent().getStringExtra("type");
		checkCardTypeAndRelation(type, key);
		initListener();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
			thisView.changePopMenuView();
			return true;
		}
		return false;
	}

	private void initListener() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.equals(thisView.backView)) {
					thisActivity.finish();
				} else if (view.equals(thisView.titleImage)) {
					thisView.changePopMenuView();
				} else if (view.equals(thisView.chat)) {
					String tag_class = (String) view.getTag(R.id.tag_class);
					if ("chat".equals(tag_class)) {
						Toast.makeText(thisActivity, "chat", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(thisActivity, ChatActivity.class);
						intent.putExtra("type", type);
						intent.putExtra("key", key);
						thisActivity.startActivity(intent);
					} else if ("joinGroup".equals(tag_class)) {
						joinGroup();
						view.setTag(R.id.tag_class, "chat");
						thisView.chatText.setText("聊天");
					} else {
						Toast.makeText(thisActivity, tag_class, Toast.LENGTH_SHORT).show();
					}
				} else if (view.equals(thisView.attention)) {
					Toast.makeText(thisActivity, "attention", Toast.LENGTH_SHORT).show();
					thisView.attention.setVisibility(View.GONE);
					followAccount();
				}
			}
		};

		mOnKeyListener = new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
					thisView.changePopMenuView();
					return true;
				}
				return false;
			}
		};
		bindEvent();
	}

	private void joinGroup() {
		User currentUser = data.userInformation.currentUser;
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone", currentUser.phone);
		params.addBodyParameter("accessKey", currentUser.accessKey);
		params.addBodyParameter("gid", key);
		params.addBodyParameter("members", "[\"" + currentUser.phone + "\"]");

		httpUtils.send(HttpMethod.POST, API.GROUP_ADDMEMBERS, params, responseHandlers.addMemberCallBack);
	}

	protected void followAccount() {
		User currentUser = data.userInformation.currentUser;
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone", currentUser.phone);
		params.addBodyParameter("accessKey", currentUser.accessKey);
		params.addBodyParameter("target", key);

		httpUtils.send(HttpMethod.POST, API.RELATION_FOLLOW, params, responseHandlers.followAccount);
	}

	private void bindEvent() {
		thisView.backView.setOnClickListener(mOnClickListener);
		thisView.titleImage.setOnClickListener(mOnClickListener);
		thisView.mPopupWindowView.setOnKeyListener(mOnKeyListener);
		thisView.chat.setOnClickListener(mOnClickListener);
		thisView.attention.setOnClickListener(mOnClickListener);
	}

	public void onDestroy() {
		mActivityManager.mBusinessActivity = null;

	}

	public void checkCardTypeAndRelation(String type, String key) {
		if ("point".equals(type)) {
			if (key.equals(data.userInformation.currentUser.phone)) {
				this.status = Status.SELF;
			} else if (data.relationship.friends != null) {
				if (data.relationship.friends.contains(key)) {
					this.status = Status.FRIEND;
				} else if (data.relationship.attentions.contains(key)) {
					this.status = Status.ATTENTIONS;
				} else if (data.relationship.fans.contains(key)) {
					this.status = Status.FANS;
				} else {
					this.status = Status.TEMPFRIEND;
				}
				if (data.relationship.friendsMap.get(key) != null) {
					thisView.fillData();
				} else {
					getAccountInfomation();
				}
			} else {
				this.status = Status.TEMPFRIEND;
				getAccountInfomation();
			}
		} else if ("group".equals(type)) {
			if (data.relationship.groups != null) {
				if (data.relationship.groups.contains(key)) {
					this.status = Status.JOINEDGROUP;
				} else {
					this.status = Status.NOTJOINGROUP;
				}
				if (data.relationship.groupsMap.get(key) != null) {
					thisView.fillData();
				} else {
					getGroupInfomation();
				}
			} else {
				this.status = Status.NOTJOINGROUP;
				getGroupInfomation();
			}
		}
	}

	private void getGroupInfomation() {
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone", data.userInformation.currentUser.phone);
		params.addBodyParameter("accessKey", data.userInformation.currentUser.accessKey);
		params.addBodyParameter("gid", key);

		httpUtils.send(HttpMethod.POST, API.GROUP_GET, params, responseHandlers.group_get);
	}

	public void getAccountInfomation() {
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone", data.userInformation.currentUser.phone);
		params.addBodyParameter("accessKey", data.userInformation.currentUser.accessKey);
		params.addBodyParameter("target", "[\"" + key + "\"]");
		httpUtils.send(HttpMethod.POST, API.ACCOUNT_GET, params, responseHandlers.account_get);
	}

}
