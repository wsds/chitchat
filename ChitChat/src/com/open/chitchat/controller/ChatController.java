package com.open.chitchat.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.chitchat.ChatActivity;
import com.open.chitchat.ImagesDirectoryActivity;
import com.open.chitchat.listener.AudioListener;
import com.open.chitchat.listener.OnUploadLoadingListener;
import com.open.chitchat.R;
import com.open.chitchat.model.API;
import com.open.chitchat.model.AudioHandlers;
import com.open.chitchat.model.Constant;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.FileHandlers;
import com.open.chitchat.model.Parser;
import com.open.chitchat.model.Data.Messages.Message;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.model.ResponseHandlers;
import com.open.chitchat.utils.ExpressionUtil;
import com.open.chitchat.utils.InputMethodManagerUtils;
import com.open.chitchat.view.ChatView;
import com.open.chitchat.view.ChatFaceView.OnFaceSeletedListener;

public class ChatController {
	public Data data = Data.getInstance();
	public Parser parser = Parser.getInstance();
	public FileHandlers fileHandlers = FileHandlers.getInstance();
	public UploadMultipartList uploadMultipartList = UploadMultipartList.getInstance();
	public AudioHandlers audiohandlers = AudioHandlers.getInstance();
	public InputMethodManagerUtils inputManager;
	public Gson gson = new Gson();

	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	public OnClickListener mOnClickListener;
	public OnTouchListener mOnTouchListener;
	public OnItemClickListener mItemClickListener;
	public OnFaceSeletedListener mOnFaceSeletedListener;
	public OnUploadLoadingListener uploadLoadingListener;
	public OnFocusChangeListener mOnFocusChangeListener;
	public TextWatcher mTextWatcher;
	public GestureDetector voiceGestureDetector;
	private AudioListener mAudioListener;

	public String key = "151", type = "point";
	public User user;

	public Map<String, Message> messageMap;

	public VoiceTimerTask timerTask;
	public Timer timer;
	public long voiceTime = 0;

	public boolean sendRecording = true;

	public ChatController(ChatActivity activity) {
		thisController = this;
		thisActivity = activity;
	}

	public void onCreate() {
		String key = thisActivity.getIntent().getStringExtra("key");
		if (key != null && !"".equals(key)) {
			this.key = key;
		}
		String type = thisActivity.getIntent().getStringExtra("type");
		if (type != null && !"".equals(type)) {
			this.type = type;
		}
		initData();
		initListeners();
	}

	private void initData() {
		user = data.userInformation.currentUser;
		messageMap = new HashMap<String, Message>();
		inputManager = new InputMethodManagerUtils(thisActivity);
	}

	public void initListeners() {
		mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (view.getTag(R.id.tag_first) != null) {
					String contentType = (String) view.getTag(R.id.tag_first);
					if ("voice".equals(contentType)) {
						thisView.changeVoice(view);
					}
				} else if (thisView.backView.equals(view)) {
					thisActivity.finish();
				} else if (thisView.titleImage.equals(view)) {
					thisView.changeChatMenu();
				} else if (thisView.chatAdd.equals(view)) {
					thisView.changeChatAdd();
				} else if (thisView.chatSmily.equals(view)) {
					thisView.changeChatSmily();
				} else if (thisView.chatRecord.equals(view)) {
					thisView.changeChatRecord();
				} else if (thisView.chatInput.equals(view)) {
					thisView.changeChatInput();
				} else if (thisView.chatSend.equals(view)) {
					createTextMessage();
				} else if (thisView.voiceLayout.equals(view)) {

				} else if (thisView.takePhoto.equals(view)) {

				} else if (thisView.ablum.equals(view)) {
					thisActivity.startActivityForResult(new Intent(thisActivity, ImagesDirectoryActivity.class), Constant.REQUESTCODE_ABLUM);
				} else if (thisView.location.equals(view)) {

				}

			}

		};
		voiceGestureDetector = new GestureDetector(thisActivity, new SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

		});
		mOnTouchListener = new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if (thisView.voiceLayout.equals(view)) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						if (timer == null) {
							timer = new Timer();
							timerTask = new VoiceTimerTask();
							voiceTime = System.currentTimeMillis();
							timer.schedule(timerTask, 0, 1000);
						}
						sendRecording = true;
						audiohandlers.startRecording();
						thisView.voicePopTime.setText(thisActivity.getText(R.string.seconds));
						thisView.voicePop.setVisibility(View.VISIBLE);
					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						float x = event.getRawX(), y = event.getRawY(), x1 = thisView.voicePop.getX(), y1 = thisView.voicePop.getY(), x2 = x1 + thisView.voicePop.getWidth(), y2 = y1 + thisView.voicePop.getHeight();
						if (x > x1 && x < x2 && y < y2 && y > y1) {
							if (sendRecording) {
								sendRecording = false;
								thisView.changeVoice(sendRecording);
							}
						} else {
							if (!sendRecording) {
								sendRecording = true;
								thisView.changeVoice(sendRecording);
							}
						}

					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						long time = System.currentTimeMillis();
						if (time - voiceTime < 60 * 1000) {
							if (time - voiceTime < 1000) {
								sendRecording = false;
							}
							completeVoiceRecording(sendRecording);
						}
					}
				}
				return false;
			}
		};

		mItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		};
		mTextWatcher = new TextWatcher() {
			String content = "";

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				content = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
				if ("".equals(s.toString())) {
					thisView.chatSend.setVisibility(View.GONE);
					thisView.chatRecord.setVisibility(View.VISIBLE);
				} else {
					thisView.chatSend.setVisibility(View.VISIBLE);
					thisView.chatRecord.setVisibility(View.GONE);
					int selectionIndex = thisView.chatInput.getSelectionStart();
					String afterContent = s.toString().trim();
					if (!afterContent.equals(content)) {
						SpannableString spannableString = ExpressionUtil.getExpressionString(thisActivity, afterContent, Constant.FACEREGX);
						thisView.chatInput.setText(spannableString);
						thisView.chatInput.setSelection(selectionIndex);
					}
				}

			}
		};
		mOnFaceSeletedListener = new OnFaceSeletedListener() {

			@Override
			public void onFaceSeleted(String faceName) {
				if ("delete".equals(faceName)) {
					int start2 = thisView.chatInput.getSelectionStart();
					String content2 = thisView.chatInput.getText().toString();
					if (start2 - 1 < 0)
						return;
					String faceEnd2 = content2.substring(start2 - 1, start2);
					if ("]".equals(faceEnd2)) {
						String str = content2.substring(0, start2);
						int index = str.lastIndexOf("[");
						if (index != -1) {
							String faceStr = content2.substring(index, start2);
							Pattern patten = Pattern.compile(Constant.FACEREGX, Pattern.CASE_INSENSITIVE);
							Matcher matcher = patten.matcher(faceStr);
							if (matcher.find()) {
								thisView.chatInput.setText(content2.substring(0, start2 - faceStr.length()) + content2.substring(start2));
								thisView.chatInput.setSelection(start2 - faceStr.length());
							} else {
								if (start2 - 1 >= 0) {
									thisView.chatInput.setText(content2.substring(0, start2 - 1) + content2.substring(start2));
									thisView.chatInput.setSelection(start2 - 1);
								}
							}
						}
					} else {
						if (start2 - 1 >= 0) {
							thisView.chatInput.setText(content2.substring(0, start2 - 1) + content2.substring(start2));
							thisView.chatInput.setSelection(start2 - 1);
						}
					}
				} else if (faceName.indexOf("[") != -1) {
					thisView.chatInput.setText(thisView.chatInput.getText() + faceName);
					thisView.chatInput.setSelection(thisView.chatInput.getText().length());
				} else {
					createGifMessage(faceName);
				}
			}

		};
		mOnFocusChangeListener = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if (view.equals(thisView.chatInput) && hasFocus) {
					thisView.changeChatInput();
				}

			}
		};
		uploadLoadingListener = new OnUploadLoadingListener() {
			@Override
			public void onSuccess(UploadMultipart instance, int time) {
				String fileName = (String) instance.view.getTag();
				Message message = messageMap.remove(fileName);
				if (message != null) {
					sendMessage(message);
				}
			}
		};
		mAudioListener = new AudioListener() {

			@Override
			public void onRecording(int volume) {
				if (sendRecording) {
					if (volume == 0) {
						thisView.changeVoice(R.drawable.image_chat_voice_talk);
					} else if (volume > 0 && volume <= 10) {
						thisView.changeVoice(R.drawable.image_chat_voice_talk_1);
					} else if (volume > 10 && volume <= 20) {
						thisView.changeVoice(R.drawable.image_chat_voice_talk_2);
					} else if (volume > 20 && volume <= 30) {
						thisView.changeVoice(R.drawable.image_chat_voice_talk_3);
					} else {
						thisView.changeVoice(R.drawable.image_chat_voice_talk_4);
					}

				}
			}

			@Override
			public void onPlayFail() {
				android.os.Message msg = new android.os.Message();
				msg.what = Constant.HANDLER_CHAT_STOPPLAY;
				thisView.handler.sendMessage(msg);
			}

			@Override
			public void onPlayComplete() {
				android.os.Message msg = new android.os.Message();
				msg.what = Constant.HANDLER_CHAT_STOPPLAY;
				thisView.handler.sendMessage(msg);
			}

			@Override
			public void onPrepared() {
				android.os.Message msg = new android.os.Message();
				msg.what = Constant.HANDLER_CHAT_STARTPLAY;
				thisView.handler.sendMessage(msg);
				audiohandlers.startPlay();
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
		thisView.chatInput.setOnClickListener(mOnClickListener);

		thisView.voiceLayout.setOnTouchListener(mOnTouchListener);

		thisView.faceLayout.setOnFaceSeletedListener(mOnFaceSeletedListener);

		thisView.chatInput.addTextChangedListener(mTextWatcher);
		thisView.chatInput.setOnFocusChangeListener(mOnFocusChangeListener);
		thisView.chatMenu.setOnItemClickListener(mItemClickListener);

		audiohandlers.setAudioListener(mAudioListener);
	}

	private void completeVoiceRecording(boolean weather) {
		android.os.Message msg = new android.os.Message();
		msg.what = Constant.HANDLER_CHAT_HIDEVOICEPOP;
		thisView.handler.sendMessage(msg);
		timer.cancel();
		timer.purge();
		voiceTime = 0;
		timer = null;
		if (weather) {
			String filePath = audiohandlers.stopRecording();
			if (!"".equals(filePath)) {
				createVoiceMessage(filePath);
			}
		} else {
			audiohandlers.cancelRecording();
		}
	}

	private void createTextMessage() {
		long time = new Date().getTime();
		String messageContent = thisView.chatInput.getText().toString().trim();
		thisView.chatInput.setText("");
		if ("".equals(messageContent))
			return;
		Message message = data.messages.new Message();
		message.content = messageContent;
		message.contentType = "text";
		message.phone = user.phone;
		message.nickName = user.nickName;
		message.sex = user.sex;
		message.time = String.valueOf(time);
		message.status = "sending";
		message.type = Constant.MESSAGE_TYPE_SEND;
		addMessageToLocation(message);
		sendMessage(message);
	}

	private void createImageMessage(String filePath) {
		long time = new Date().getTime();
		View view = new View(thisActivity);
		Map<String, Object> map = fileHandlers.processImagesInformation(filePath);
		String fileName = (String) map.get("fileName");
		view.setTag(fileName);
		Message message = data.messages.new Message();
		message.content = fileName;
		message.contentType = "image";
		message.phone = user.phone;
		message.nickName = user.nickName;
		message.sex = user.sex;
		message.time = String.valueOf(time);
		message.status = "sending";
		message.type = Constant.MESSAGE_TYPE_SEND;
		messageMap.put(fileName, message);
		UploadMultipart multipart = uploadFile(filePath, fileName, (byte[]) map.get("bytes"), view, UploadMultipart.UPLOAD_TYPE_IMAGE);
		uploadMultipartList.addMultipart(multipart);
		addMessageToLocation(message);
	}

	private void createVoiceMessage(String filePath) {
		long time = new Date().getTime();
		View view = new View(thisActivity);
		Map<String, Object> map = fileHandlers.processVoiceInformation(filePath);
		String fileName = (String) map.get("fileName");
		view.setTag(fileName);
		Message message = data.messages.new Message();
		message.content = fileName;
		message.contentType = "voice";
		message.phone = user.phone;
		message.nickName = user.nickName;
		message.sex = user.sex;
		message.time = String.valueOf(time);
		message.status = "sending";
		message.type = Constant.MESSAGE_TYPE_SEND;
		messageMap.put(fileName, message);
		UploadMultipart multipart = uploadFile(filePath, fileName, (byte[]) map.get("bytes"), view, UploadMultipart.UPLOAD_TYPE_VOICE);
		uploadMultipartList.addMultipart(multipart);
		addMessageToLocation(message);
	}

	private void createGifMessage(String faceName) {
		long time = new Date().getTime();
		Message message = data.messages.new Message();
		message.content = faceName;
		message.contentType = "gif";
		message.phone = user.phone;
		message.nickName = user.nickName;
		message.sex = user.sex;
		message.time = String.valueOf(time);
		message.status = "sending";
		message.type = Constant.MESSAGE_TYPE_SEND;
		addMessageToLocation(message);
		sendMessage(message);
	}

	private void addMessageToLocation(final Message message) {

		new Thread(new Runnable() {
			@Override
			public void run() {
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

	private UploadMultipart uploadFile(String filePath, String fileName, byte[] bytes, View view, int type) {
		UploadMultipart multipart = new UploadMultipart(filePath, fileName, bytes, type);
		multipart.view = view;
		multipart.setUploadLoadingListener(uploadLoadingListener);
		return multipart;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data2) {
		if (requestCode == Constant.REQUESTCODE_ABLUM && resultCode == Activity.RESULT_OK) {
			ArrayList<String> selectedImageList = data.tempData.selectedImageList;
			if (selectedImageList == null || selectedImageList.size() == 0) {
				return;
			}
			data.tempData.selectedImageList = null;
			for (String filePath : selectedImageList) {
				createImageMessage(filePath);
			}
		}

	}

	public void onDestroy() {
		thisActivity.mActivityManager.mChatActivity = null;
		audiohandlers.releasePlyer();
	}

	public void onResume() {

	}

	private class VoiceTimerTask extends TimerTask {

		@Override
		public void run() {
			long time = System.currentTimeMillis();
			if (time - voiceTime > 60 * 1000) {
				completeVoiceRecording(true);
			} else {
				thisView.changeVoice();
			}
		}
	}
}
