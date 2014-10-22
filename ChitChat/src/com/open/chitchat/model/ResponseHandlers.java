package com.open.chitchat.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.chitchat.model.Data.Messages.Message;
import com.open.chitchat.model.Data.Relationship;
import com.open.chitchat.model.Data.Relationship.Friend;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.utils.RSAUtils;
import com.open.lib.HttpClient;
import com.open.lib.HttpClient.ResponseHandler;

public class ResponseHandlers {

	public String tag = "ResponseHandlers";

	public HttpClient httpClient = HttpClient.getInstance();
	public Data data = Data.getInstance();
	public Parser parser = Parser.getInstance();
	public ActivityManager activityManager = ActivityManager.getInstance();
	public ResponseEventHandlers responseEventHandlers = ResponseEventHandlers.getInstance();
	public Gson gson = new Gson();

	public static ResponseHandlers responseHandlers;

	public static ResponseHandlers getInstance() {
		if (responseHandlers == null) {
			responseHandlers = new ResponseHandlers();
		}
		return responseHandlers;
	}

	// Message
	public ResponseHandler<String> message_sendMessageCallBack = httpClient.new ResponseHandler<String>() {
		class Response {
			public String 提示信息;
			public String 失败原因;
			public String time;
			public String oldTime;
			public String sendType;
			public String gid;
			public String phoneTo;
		}

		public void onSuccess(com.lidroid.xutils.http.ResponseInfo<String> responseInfo) {
			Response response = gson.fromJson(responseInfo.result, Response.class);
			if (response.提示信息.equals("发送成功")) {
				if (response.sendType != null) {
					if ("point".equals(response.sendType)) {
						List<String> phones = gson.fromJson(response.phoneTo, new TypeToken<List<String>>() {
						}.getType());
						String key = phones.get(0);
						ArrayList<Message> messages = data.messages.messageMap.get("p" + key);
						if (messages != null) {
							Message message0 = null;
							for (int i = 0; i < messages.size(); i++) {
								Message message = messages.get(i);
								if (message.time.equals(response.oldTime)) {
									message0 = message;
									break;
								}
							}
							if (message0 != null) {
								Log.e(tag, "修改聊天数据成功point");
								message0.time = response.time;
							} else {
								Log.e(tag, "修改聊天数据失败point");
							}
						}
					} else if ("group".equals(response.sendType)) {
						ArrayList<Message> messages = data.messages.messageMap.get("g" + response.gid);
						if (messages != null) {
							Message message0 = null;
							for (int i = 0; i < messages.size(); i++) {
								Message message = messages.get(i);
								if (message.time.equals(response.oldTime)) {
									message0 = message;
									break;
								}
							}
							if (message0 != null) {
								Log.e(tag, "修改发送数据成功group");
								message0.time = response.time;
							} else {
								Log.e(tag, "修改发送数据失败group");
							}
						}
					}
				}
			} else if (response.提示信息.equals("发送失败")) {
				if (response.sendType != null) {
					if ("point".equals(response.sendType)) {

					} else if ("group".equals(response.sendType)) {

					}
				}
				Log.e(tag, response.提示信息 + "---------------------" + response.失败原因);
			} else {
				Log.e(tag, response.提示信息 + "---------------------" + response.失败原因);
			}
		};
	};
	public RequestCallBack<String> getMessageCallBack = httpClient.new ResponseHandler<String>() {
		class Response {
			public String 提示信息;
			public String 失败原因;
			public String flag;
			public List<String> messages;
		}

		public void onSuccess(ResponseInfo<String> responseInfo) {
			try {
				Response response = gson.fromJson(responseInfo.result, Response.class);
				if (response.提示信息.equals("获取成功")) {
					Log.e(tag, response.提示信息 + "---------------------获取消息成功" + response.flag);
					List<String> messages = response.messages;
					parser.check();
					User user = data.userInformation.currentUser;
					if (messages.size() == 0) {
						user.flag = "none";
					} else {
						user.flag = response.flag;
					}
					data.userInformation.isModified = true;
					data.messages.isModified = true;
					for (int i = 0; i < messages.size(); i++) {
						Message message = null;
						try {
							message = gson.fromJson(messages.get(i), Message.class);
						} catch (Exception e) {
							e.printStackTrace();
							Log.e(tag, "gson message Exception");
							continue;
						}
						String sendType = message.sendType;
						if ("event".equals(sendType)) {
							// if (!data.event.userEvents.contains(message)) {
							responseEventHandlers.handleEvent(message);
							// }
						} else if ("point".equals(sendType)) {
							String key = message.phone;
							message.type = Constant.MESSAGE_TYPE_RECEIVE;
							if (key.equals(user.phone)) {
								List<String> phones = gson.fromJson(message.phoneto, new TypeToken<List<String>>() {
								}.getType());
								key = phones.get(0);
								message.type = Constant.MESSAGE_TYPE_SEND;
							}
							String messageKey = "p" + key;
							ArrayList<Message> friendMessages = data.messages.messageMap.get(messageKey);
							if (friendMessages == null) {
								friendMessages = new ArrayList<Message>();
								data.messages.messageMap.put(messageKey, friendMessages);
							}
							if (!data.messages.messagesOrder.contains(messageKey)) {
								if (data.relationship.friends.contains(key)) {
									data.messages.messagesOrder.add(0, messageKey);
									if (!DataHandlers.contains(friendMessages, message)) {
										friendMessages.add(message);
										Friend friend = data.relationship.friendsMap.get(key);
										if (friend != null) {
											friend.notReadMessagesCount++;
										}
									}
								}
							} else {
								if (data.relationship.friends.contains(key)) {
									data.messages.messagesOrder.remove(messageKey);
									data.messages.messagesOrder.add(0, messageKey);
									if (!DataHandlers.contains(friendMessages, message)) {
										friendMessages.add(message);
										Friend friend = data.relationship.friendsMap.get(key);
										if (friend != null) {
											friend.notReadMessagesCount++;
										}
									}
								}
							}
						} else if ("group".equals(sendType)) {
							String key = message.gid;
							String messageKey = "g" + message.gid;
							if (message.phone.equals(user.phone)) {
								message.type = Constant.MESSAGE_TYPE_SEND;
							} else {
								message.type = Constant.MESSAGE_TYPE_RECEIVE;
							}
							ArrayList<Message> groupMessages = data.messages.messageMap.get(messageKey);
							if (groupMessages == null) {
								groupMessages = new ArrayList<Message>();
								data.messages.messageMap.put(messageKey, groupMessages);
							}
							if (!data.messages.messagesOrder.contains(messageKey)) {
								if (data.relationship.groups.contains(key)) {
									data.messages.messagesOrder.add(messageKey);
									if (!DataHandlers.contains(groupMessages, message)) {
										groupMessages.add(message);
										Group group = data.relationship.groupsMap.get(key);
										if (group != null) {
											group.notReadMessagesCount++;
										}
									}
								}
							} else {
								if (data.relationship.groups.contains(key)) {
									data.messages.messagesOrder.remove(messageKey);
									data.messages.messagesOrder.add(0, messageKey);
									if (!DataHandlers.contains(groupMessages, message)) {
										groupMessages.add(message);
										Group group = data.relationship.groupsMap.get(key);
										if (group != null) {
											group.notReadMessagesCount++;
										}
									}
								}
							}
						}
					}
					data.event.isModified = true;
					data.messages.isModified = true;
					// viewManage.messagesSubView.showMessagesSequence();
				} else {
					Log.e(tag, response.提示信息 + "---------------------" + response.失败原因);
				}
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				Log.e(tag, e.toString() + "");
			}
		};
	};
	// Relationship
	public ResponseHandler<String> account_auth = httpClient.new ResponseHandler<String>() {
		class Response {
			public String 提示信息;
			public String 失败原因;
			public String uid;
			public String accessKey;
			public String PbKey;
		}

		public void onSuccess(ResponseInfo<String> responseInfo) {
			Response response = gson.fromJson(responseInfo.result, Response.class);
			if (response.提示信息.equals("普通鉴权成功")) {
				String accessKey = "", phone = "";
				try {
					accessKey = RSAUtils.decrypt(response.PbKey, response.accessKey);
					phone = RSAUtils.decrypt(response.PbKey, response.uid);
				} catch (Exception e) {
					e.printStackTrace();
				}
				data.userInformation.currentUser.phone = phone;
				data.userInformation.currentUser.accessKey = accessKey;
				data.userInformation.isModified = true;
				if (activityManager.mLoginActivity != null) {
					activityManager.mLoginActivity.loginUsePassWordSuccess();
				}
				HttpUtils httpUtils = new HttpUtils();
				RequestParams params = new RequestParams();
				params.addBodyParameter("phone", phone);
				params.addBodyParameter("accessKey", accessKey);
				params.addBodyParameter("target", "[\"" + phone + "\"]");
				ResponseHandlers responseHandlers = getInstance();
				httpUtils.send(HttpMethod.POST, API.ACCOUNT_GET, params, responseHandlers.account_get);
			} else {
				activityManager.mLoginActivity.loginUsePassWordFail(response.失败原因);
			}
		};

	};
	public ResponseHandler<String> account_get = httpClient.new ResponseHandler<String>() {
		class Response {
			public String 提示信息;
			public String 失败原因;
			public List<Account> accounts;
		}

		class Account {
			public int ID;
			public String phone;
			public String nickName;
			public String mainBusiness;
			public String head;
			public String sex;
			public String age;
			// public String byPhone;
			public String createTime;
			public String lastLoginTime;
			public String userBackground;
		}

		public void onSuccess(ResponseInfo<String> responseInfo) {
			Response response = gson.fromJson(responseInfo.result, Response.class);
			if ("获取用户信息成功".equals(response.提示信息)) {
				User user = data.userInformation.currentUser;
				if (response.accounts.size() > 0) {
					Account account = response.accounts.get(0);
					if (user.phone.equals(account.phone)) {
						user.id = account.ID;
						user.head = account.head;
						user.mainBusiness = account.mainBusiness;
						user.nickName = account.nickName;
						user.sex = account.sex;
						user.age = account.age;
						user.createTime = account.createTime;
						user.lastLoginTime = account.lastLoginTime;
						user.userBackground = account.userBackground;
						data.userInformation.isModified = true;
					} else {
						// boolean isTemp = true;
						// List<String> circles = data.relationship.circles;
						// for (String circle : circles) {
						// List<String> friends =
						// data.relationship.circlesMap.get(circle).friends;
						// if (friends.contains(account.phone)) {
						// isTemp = false;
						// break;
						// }
						// }
						// if (data.relationship.circlesMap.get("8888888") !=
						// null) {
						// if
						// (data.relationship.circlesMap.get("8888888").friends.contains(account.phone))
						// {
						// isTemp = false;
						// }
						// }
						// if (isTemp) {
						// Friend friend = data.relationship.new Friend();
						// friend.phone = account.phone;
						// friend.head = account.head;
						// friend.nickName = account.nickName;
						// friend.mainBusiness = account.mainBusiness;
						// friend.sex = account.sex;
						// friend.sex = account.sex;
						// friend.age = Integer.valueOf(account.age);
						// friend.createTime = account.createTime;
						// friend.lastLoginTime = account.lastLoginTime;
						// friend.userBackground = account.userBackground;
						// friend.id = account.ID;
						//
						// data.tempData.tempFriend = friend;
						//
						// data.relationship.friendsMap.put(friend.phone,
						// friend);
						// } else {
						// Friend friend =
						// data.relationship.friendsMap.get(account.phone);
						// if (friend != null) {
						// friend.head = account.head;
						// friend.nickName = account.nickName;
						// friend.mainBusiness = account.mainBusiness;
						// friend.sex = account.sex;
						// friend.age = Integer.valueOf(account.age);
						// friend.createTime = account.createTime;
						// friend.lastLoginTime = account.lastLoginTime;
						// friend.userBackground = account.userBackground;
						// }
						// }
						// viewManage.searchFriendActivity.searchCallBack(account.phone,
						// isTemp);
					}
					// viewManage.postNotifyView("MeSubView");
				}
			} else {
				if ("获取用户信息失败".equals(response.提示信息) && "用户不存在".equals(response.失败原因)) {
					// viewManage.searchFriendActivity.searchCallBack("",
					// false);
				}
			}
		};
	};
	public ResponseHandler<String> getIntimateFriends = httpClient.new ResponseHandler<String>() {
		class Response {
			public String 提示信息;
			public String 失败原因;
			public Relationship relationship;

		}

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			Response response = gson.fromJson(responseInfo.result, Response.class);
		}
	};
}
