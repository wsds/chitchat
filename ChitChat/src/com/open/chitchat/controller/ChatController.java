package com.open.chitchat.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.model.API;
import com.open.chitchat.model.Constant;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Data.Messages.Message;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.model.ResponseHandlers;
import com.open.chitchat.view.ChatView;

public class ChatController {
	public Data data = Data.getInstance();

	public Gson gson = new Gson();

	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	public OnClickListener mOnClickListener;
	public OnItemClickListener mItemClickListener;
	public TextWatcher mTextWatcher;

	public String key = "151", type = "point";
	public User user;

	public ChatController(ChatActivity activity) {
		thisActivity = activity;
	}

	public void onCreate() {
		user = data.userInformation.currentUser;
		String key = thisActivity.getIntent().getStringExtra("key");
		if (key != null && !"".equals(key)) {
			this.key = key;
		}
		String type = thisActivity.getIntent().getStringExtra("type");
		if (type != null && !"".equals(type)) {
			this.type = type;
		}
		initListeners();
	}

	public void initListeners() {
		mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (thisView.backView.equals(view)) {
					thisActivity.finish();
				} else if (thisView.titleImage.equals(view)) {
					thisView.changeChatMenu();
				} else if (thisView.chatAdd.equals(view)) {
					thisView.changeChatAdd();
				} else if (thisView.chatSmily.equals(view)) {
					thisView.changeChatSmily();
				} else if (thisView.chatRecord.equals(view)) {
					thisView.changeChatRecord();
				} else if (thisView.chatSend.equals(view)) {
					addTextMessageToLocation();
				} else if (thisView.voiceLayout.equals(view)) {

				} else if (thisView.takePhoto.equals(view)) {

				} else if (thisView.ablum.equals(view)) {

				} else if (thisView.location.equals(view)) {

				}

			}

		};
		mItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		};
		mTextWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if ("".equals(s.toString())) {
					thisView.chatSend.setVisibility(View.GONE);
					thisView.chatRecord.setVisibility(View.VISIBLE);
				} else {
					thisView.chatSend.setVisibility(View.VISIBLE);
					thisView.chatRecord.setVisibility(View.GONE);
				}

			}
		};
		bindEvent();
	}

	public void bindEvent() {
		thisView.backView.setOnClickListener(mOnClickListener);
		thisView.chatAdd.setOnClickListener(mOnClickListener);
		thisView.chatSend.setOnClickListener(mOnClickListener);
		thisView.chatSmily.setOnClickListener(mOnClickListener);
		thisView.chatRecord.setOnClickListener(mOnClickListener);
		thisView.titleImage.setOnClickListener(mOnClickListener);
		thisView.voiceLayout.setOnClickListener(mOnClickListener);
		thisView.takePhoto.setOnClickListener(mOnClickListener);
		thisView.ablum.setOnClickListener(mOnClickListener);
		thisView.location.setOnClickListener(mOnClickListener);

		thisView.chatInput.addTextChangedListener(mTextWatcher);
		thisView.chatMenu.setOnItemClickListener(mItemClickListener);
	}

	private void addTextMessageToLocation() {
		final long time = new Date().getTime();
		final String messageContent = thisView.chatInput.getText().toString().trim();
		thisView.chatInput.setText("");
		if ("".equals(messageContent))
			return;
		new Thread(new Runnable() {
			@Override
			public void run() {
				Message message = data.messages.new Message();
				message.content = messageContent;
				message.contentType = "text";
				message.phone = user.phone;
				message.nickName = user.nickName;
				message.sex = user.sex;
				message.time = String.valueOf(time);
				message.status = "sending";
				message.type = Constant.MESSAGE_TYPE_SEND;

				List<String> messagesOrder = data.messages.messagesOrder;
				String orderKey = "";
				if ("point".equals(type)) {
					orderKey = "p" + key;
					if (messagesOrder.contains(orderKey)) {
						messagesOrder.remove(orderKey);
					}
					messagesOrder.add(0, orderKey);
					message.sendType = "point";
					message.phoneto = "[\"" + key + "\"]";
					Map<String, ArrayList<Message>> friendMessageMap = data.messages.messageMap;
					if (friendMessageMap == null) {
						friendMessageMap = new HashMap<String, ArrayList<Message>>();
						data.messages.messageMap = friendMessageMap;
					}
					ArrayList<Message> messages = friendMessageMap.get(orderKey);
					if (messages == null) {
						messages = new ArrayList<Message>();
						friendMessageMap.put(orderKey, messages);
					}
					messages.add(message);
				} else if ("group".equals(type)) {
					orderKey = "g" + key;
					if (messagesOrder.contains(orderKey)) {
						messagesOrder.remove(orderKey);
					}
					messagesOrder.add(0, orderKey);
					message.gid = key;
					message.sendType = "group";

					message.phoneto = data.relationship.groupsMap.get(key).members.toString();
					Map<String, ArrayList<Message>> groupMessageMap = data.messages.messageMap;
					if (groupMessageMap == null) {
						groupMessageMap = new HashMap<String, ArrayList<Message>>();
						data.messages.messageMap = groupMessageMap;
					}
					ArrayList<Message> messages = groupMessageMap.get(orderKey);
					if (messages == null) {
						messages = new ArrayList<Message>();
						groupMessageMap.put(orderKey, messages);
					}
					messages.add(message);
				}
				android.os.Message msg = new android.os.Message();
				msg.what = Constant.HANDLER_CHAT_NOTIFY;
				thisView.handler.sendMessage(msg);
				sendMessage(message);
			}
		}).start();

	}

	private void sendMessage(Message message) {
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();

		params.addBodyParameter("phone", user.phone);
		params.addBodyParameter("accessKey", user.accessKey);
		params.addBodyParameter("sendType", type);
		params.addBodyParameter("contentType", message.contentType);
		params.addBodyParameter("content", message.content);
		params.addBodyParameter("time", message.time);
		if ("group".equals(type)) {
			Group group = data.relationship.groupsMap.get(key);
			if (group == null) {
				group = data.relationship.new Group();
			}
			params.addBodyParameter("gid", key);
			params.addBodyParameter("phoneto", gson.toJson(group.members));
		} else if ("point".equals(type)) {
			List<String> phoneto = new ArrayList<String>();
			phoneto.add(key);
			params.addBodyParameter("phoneto", gson.toJson(phoneto));
			params.addBodyParameter("gid", "");
		}

		ResponseHandlers responseHandlers = ResponseHandlers.getInstance();
		httpUtils.send(HttpMethod.POST, API.MESSAGE_SEND, params, responseHandlers.message_sendMessageCallBack);
	}
}
