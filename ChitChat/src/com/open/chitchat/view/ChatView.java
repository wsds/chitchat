package com.open.chitchat.view;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.controller.ChatController;
import com.open.chitchat.model.Constant;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Data.Messages.Message;
import com.open.chitchat.utils.BaseDataUtils;

public class ChatView {

	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	public View currentView, backView, chatMenuLayout, textLayout, voiceLayout, chatAddLayout, takePhoto, ablum, location, voicePop;
	public RelativeLayout rightContainer;
	public TextView titleText, chatSend, voicePopTime, voicePopPrompt;
	public ListView chatContent;
	public ImageView chatAdd, chatSmily, chatRecord, titleImage, chatMenuBackground, voicePopImage;
	public EditText chatInput;
	public GridView chatMenu;
	public ChatFaceView faceLayout;

	public ChatAdapter mChatAdapter;
	public ChatMenuAdapter mChatMenuAdapter;

	private Animation inTranslateAnimation, inAlphaAnimation, outTranslateAnimation;

	private DisplayImageOptions headOptions;

	public Handler handler;

	public ChatView(ChatActivity activity) {
		thisView = this;
		thisActivity = activity;
	}

	@SuppressLint("HandlerLeak")
	public void initViews() {
		thisActivity.setContentView(R.layout.activity_chat);
		backView = thisActivity.findViewById(R.id.backView);
		chatMenuLayout = thisActivity.findViewById(R.id.chatMenuLayout);
		textLayout = thisActivity.findViewById(R.id.textLayout);
		voiceLayout = thisActivity.findViewById(R.id.voiceLayout);
		chatAddLayout = thisActivity.findViewById(R.id.chatSmilyLayout);
		takePhoto = thisActivity.findViewById(R.id.takePhoto);
		ablum = thisActivity.findViewById(R.id.ablum);
		location = thisActivity.findViewById(R.id.location);
		voicePop = thisActivity.findViewById(R.id.voicePop);
		rightContainer = (RelativeLayout) thisActivity.findViewById(R.id.rightContainer);
		titleText = (TextView) thisActivity.findViewById(R.id.titleText);
		chatSend = (TextView) thisActivity.findViewById(R.id.chatSend);
		voicePopTime = (TextView) thisActivity.findViewById(R.id.voicePopTime);
		voicePopPrompt = (TextView) thisActivity.findViewById(R.id.voicePopPrompt);
		chatContent = (ListView) thisActivity.findViewById(R.id.chatContent);
		chatAdd = (ImageView) thisActivity.findViewById(R.id.chatAdd);
		chatSmily = (ImageView) thisActivity.findViewById(R.id.chatSmily);
		chatRecord = (ImageView) thisActivity.findViewById(R.id.chatRecord);
		chatMenuBackground = (ImageView) thisActivity.findViewById(R.id.chatMenuBackground);
		voicePopImage = (ImageView) thisActivity.findViewById(R.id.voicePopImage);
		chatInput = (EditText) thisActivity.findViewById(R.id.chatInput);
		chatMenu = (GridView) thisActivity.findViewById(R.id.chatMenu);
		faceLayout = (ChatFaceView) thisActivity.findViewById(R.id.faceLayout);

		chatRecord.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_record));
		chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_add));

		titleImage = new ImageView(thisActivity);
		titleImage.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_arrow_down));
		rightContainer.addView(titleImage);

		headOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(40)).build();

		mChatAdapter = new ChatAdapter();
		chatContent.setAdapter(mChatAdapter);

		mChatMenuAdapter = new ChatMenuAdapter();
		chatMenu.setAdapter(mChatMenuAdapter);

		inTranslateAnimation = AnimationUtils.loadAnimation(thisActivity, R.anim.chat_menu_in);
		outTranslateAnimation = AnimationUtils.loadAnimation(thisActivity, R.anim.chat_menu_out);
		inAlphaAnimation = AnimationUtils.loadAnimation(thisActivity, R.anim.chat_menu_in_alpha);

		handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case Constant.HANDLER_CHAT_NOTIFY:
					mChatAdapter.notifyDataSetChanged();
					break;
				case Constant.HANDLER_CHAT_HIDEVOICEPOP:
					thisView.voicePop.setVisibility(View.GONE);
					break;
				case Constant.HANDLER_CHAT_STARTPLAY:
					if (thisView.currentView != null) {
						((ImageView) thisView.currentView.findViewById(R.id.voiceIcon)).setImageResource(R.drawable.icon_play_on);
					}
					break;
				case Constant.HANDLER_CHAT_STOPPLAY:
					if (thisView.currentView != null) {
						((ImageView) thisView.currentView.findViewById(R.id.voiceIcon)).setImageResource(R.drawable.icon_play_off);
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
	}

	public class ChatAdapter extends BaseAdapter {
		ArrayList<Message> messages;

		public ChatAdapter() {
			String type = thisController.type, key = thisController.key;
			ArrayList<Message> messages = null;
			thisController.parser.check();
			if ("group".equals(type)) {
				messages = thisController.data.messages.messageMap.get("g" + key);
				if (messages == null) {
					messages = new ArrayList<Data.Messages.Message>();
					thisController.data.messages.messageMap.put("g" + key, messages);
				}
			} else if ("point".equals(type)) {
				messages = thisController.data.messages.messageMap.get("p" + key);
				if (messages == null) {
					messages = new ArrayList<Data.Messages.Message>();
					thisController.data.messages.messageMap.put("p" + key, messages);
				}
			}
			this.messages = messages;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			chatContent.setSelection(messages.size() - 1);
		}

		@Override
		public int getCount() {
			return messages.size();
		}

		@Override
		public Object getItem(int position) {
			return messages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// @Override
		// public int getItemViewType(int position) {
		// Message message = messages.get(position);
		// if (message.type == Constant.MESSAGE_TYPE_SEND) {
		// return Constant.MESSAGE_TYPE_SEND;
		// } else {
		// return Constant.MESSAGE_TYPE_RECEIVE;
		// }
		// }
		//
		// @Override
		// public int getViewTypeCount() {
		// return super.getViewTypeCount();
		// }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ChatHolder holder = new ChatHolder();
			Message message = messages.get(position);
			int backgroundDrawableId = 0;
			String lastPhone = "", messageHead = "";
			if (position != 0) {
				lastPhone = messages.get(position - 1).phone;
			}
			if (message.type == Constant.MESSAGE_TYPE_SEND) {
				convertView = thisActivity.mInflater.inflate(R.layout.f_chat_item_send, null);
				if (message.phone.equals(lastPhone)) {
					backgroundDrawableId = R.drawable.myself_chat_order_bg;
				} else {
					backgroundDrawableId = R.drawable.myself_chat_bg;
					messageHead = "head";
				}
			} else if (message.type == Constant.MESSAGE_TYPE_RECEIVE) {
				convertView = thisActivity.mInflater.inflate(R.layout.f_chat_item_receive, null);
				if (message.phone.equals(lastPhone)) {
					backgroundDrawableId = R.drawable.man_chat_from_order_bg;
				} else {
					backgroundDrawableId = R.drawable.man_chat_from_bg;
					messageHead = "head";
				}
				// if (message.sex.equals("male") || message.sex.equals("男")) {
				// if (message.phone.equals(lastPhone)) {
				// backgroundDrawableId = R.drawable.man_chat_from_order_bg;
				// } else {
				// backgroundDrawableId = R.drawable.man_chat_from_bg;
				// }
				// } else {
				// if (message.phone.equals(lastPhone)) {
				// backgroundDrawableId = R.drawable.girl_chat_from_order_bg;
				// } else {
				// backgroundDrawableId = R.drawable.girl_chat_from_bg;
				// }
				// }
			}

			holder.chatLayout = convertView.findViewById(R.id.chatLayout);
			holder.voice = convertView.findViewById(R.id.voice);
			holder.voiceIcon = (ImageView) convertView.findViewById(R.id.voiceIcon);
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.head = (ImageView) convertView.findViewById(R.id.head);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.character = (TextView) convertView.findViewById(R.id.character);
			holder.voicetime = (TextView) convertView.findViewById(R.id.voicetime);
			holder.gif = (GifImageView) convertView.findViewById(R.id.gif);

			holder.chatLayout.setBackgroundResource(backgroundDrawableId);

			if (!"".equals(messageHead)) {
				thisController.fileHandlers.getHeadImage(messageHead, holder.head, headOptions);
			} else {
				holder.head.setVisibility(View.GONE);
			}

			if (message.contentType.equals("text")) {
				holder.character.setVisibility(View.VISIBLE);
				holder.voice.setVisibility(View.GONE);
				holder.image.setVisibility(View.GONE);
				holder.gif.setVisibility(View.GONE);
				holder.character.setText(message.content);
			} else if (message.contentType.equals("voice")) {
				holder.character.setVisibility(View.GONE);
				holder.voice.setVisibility(View.VISIBLE);
				holder.image.setVisibility(View.GONE);
				holder.gif.setVisibility(View.GONE);
				convertView.setTag(R.id.tag_second, message.content);
			} else if (message.contentType.equals("image")) {
				holder.character.setVisibility(View.GONE);
				holder.voice.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
				holder.gif.setVisibility(View.GONE);
				thisController.fileHandlers.getThumbleImage(message.content, holder.image, (int) BaseDataUtils.dpToPx(178), (int) BaseDataUtils.dpToPx(106), thisController.fileHandlers.defaultOptions);
			} else if (message.contentType.equals("gif")) {
				holder.character.setVisibility(View.GONE);
				holder.voice.setVisibility(View.GONE);
				holder.image.setVisibility(View.GONE);
				holder.gif.setVisibility(View.VISIBLE);
				thisController.fileHandlers.getGifImage(message.content, holder.gif);
			}
			convertView.setTag(R.id.tag_first, message.contentType);
			convertView.setOnClickListener(thisController.mOnClickListener);
			return convertView;
		}

		class ChatHolder {
			View voice, chatLayout;
			ImageView voiceIcon, image, head;
			TextView time, character, voicetime;
			GifImageView gif;
		}

	}

	@SuppressLint("ViewHolder")
	public class ChatMenuAdapter extends BaseAdapter {
		public List<String> menuString;
		public List<Integer> menuImage;

		public ChatMenuAdapter() {
			menuString = new ArrayList<String>();
			menuImage = new ArrayList<Integer>();
			if (thisController.type.equals("group")) {
				menuString.add(thisActivity.getString(R.string.groupDetails));
				menuString.add(thisActivity.getString(R.string.groupMembers));
				menuString.add(thisActivity.getString(R.string.groupAlbum));
				menuString.add(thisActivity.getString(R.string.closeNotice));
				menuString.add(thisActivity.getString(R.string.share));
				menuString.add(thisActivity.getString(R.string.sendCard));
				menuString.add(thisActivity.getString(R.string.tureOff));
				menuString.add(thisActivity.getString(R.string.setting));
				menuImage.add(R.drawable.chat_menu_item_details);
				menuImage.add(R.drawable.chat_menu_item_members);
				menuImage.add(R.drawable.chat_menu_item_albums);
				menuImage.add(R.drawable.chat_menu_item_tips_on);
				menuImage.add(R.drawable.chat_menu_item_share);
				menuImage.add(R.drawable.chat_menu_item_send);
				menuImage.add(R.drawable.chat_menu_item_light);
				menuImage.add(R.drawable.chat_menu_item_settings);
			} else {
				menuString.add(thisActivity.getString(R.string.personalDetails));
				menuString.add(thisActivity.getString(R.string.personalAlbum));
				menuString.add(thisActivity.getString(R.string.closeNotice));
				menuString.add(thisActivity.getString(R.string.sendCard));
				menuImage.add(R.drawable.chat_menu_item_details);
				menuImage.add(R.drawable.chat_menu_item_albums);
				menuImage.add(R.drawable.chat_menu_item_tips_on);
				menuImage.add(R.drawable.chat_menu_item_send);
			}
		}

		@Override
		public int getCount() {
			return menuString.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = thisActivity.mInflater.inflate(R.layout.chat_menu_item, null);
			ImageView image = (ImageView) convertView.findViewById(R.id.item_image);
			TextView text = (TextView) convertView.findViewById(R.id.item_text);
			image.setImageResource(menuImage.get(position));
			text.setText(menuString.get(position));
			return convertView;
		}

	}

	public void changeVoice() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				String time = thisView.voicePopTime.getText().toString(), seconds = thisActivity.getText(R.string.seconds).toString();
				if (seconds.equals(time)) {
					thisView.voicePopTime.setText("0" + seconds);
				} else {
					thisView.voicePopTime.setText((Integer.valueOf(time.substring(0, time.lastIndexOf(seconds))) + 1) + seconds);
				}

			}
		});
	}

	public void changeVoice(View view) {
		if (this.currentView != null && this.currentView.equals(view)) {
			if (thisController.audiohandlers.isPlaying()) {
				((ImageView) thisView.currentView.findViewById(R.id.voiceIcon)).setImageResource(R.drawable.icon_play_off);
				thisController.audiohandlers.stopPlay();
			} else {
				((ImageView) thisView.currentView.findViewById(R.id.voiceIcon)).setImageResource(R.drawable.icon_play_on);
				thisController.audiohandlers.preparePlay((String) view.getTag(R.id.tag_second));
			}
		} else {
			if (thisController.audiohandlers.isPlaying()) {
				thisController.audiohandlers.stopPlay();
				((ImageView) thisView.currentView.findViewById(R.id.voiceIcon)).setImageResource(R.drawable.icon_play_off);
			}
			this.currentView = view;
			((ImageView) thisView.currentView.findViewById(R.id.voiceIcon)).setImageResource(R.drawable.icon_play_on);
			thisController.audiohandlers.preparePlay((String) view.getTag(R.id.tag_second));
		}
	}

	public void changeVoice(final int resourceId) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				thisView.voicePopImage.setImageResource(resourceId);

			}
		});
	}

	public void changeVoice(boolean weather) {
		if (weather) {
			this.voicePopPrompt.setText(thisActivity.getString(R.string.slideFingers));
		} else {
			this.voicePopImage.setImageResource(R.drawable.image_chat_voice_cancel);
			this.voicePopPrompt.setText(thisActivity.getString(R.string.loosenFingers));
		}
	}

	public void changeChatMenu() {
		if (this.chatMenuLayout.getVisibility() == View.GONE) {
			this.titleImage.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_arrow_up));
			this.chatMenuLayout.startAnimation(inTranslateAnimation);
			this.chatMenuLayout.setVisibility(View.VISIBLE);
			this.chatMenuBackground.startAnimation(inAlphaAnimation);
			this.chatMenuBackground.setVisibility(View.VISIBLE);
		} else {
			this.titleImage.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_arrow_down));
			this.chatMenuLayout.startAnimation(outTranslateAnimation);
			this.chatMenuLayout.setVisibility(View.GONE);
			this.chatMenuBackground.setVisibility(View.GONE);
		}
	}

	public void changeChatRecord() {
		if (this.textLayout.getVisibility() == View.VISIBLE) {
			this.textLayout.setVisibility(View.GONE);
			this.voiceLayout.setVisibility(View.VISIBLE);
			this.chatRecord.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_keyboard));
			if (this.faceLayout.getVisibility() == View.VISIBLE) {
				this.faceLayout.setVisibility(View.GONE);
			}
			if (thisController.inputManager.isActive(chatInput)) {
				thisController.inputManager.hide(chatInput);
			}
		} else if (this.voiceLayout.getVisibility() == View.VISIBLE) {
			this.textLayout.setVisibility(View.VISIBLE);
			this.voiceLayout.setVisibility(View.GONE);
			this.chatRecord.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_record));
			this.chatInput.requestFocus();
		}
		if (this.chatAddLayout.getVisibility() == View.VISIBLE) {
			this.chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_add));
			this.chatAddLayout.setVisibility(View.GONE);
		}
	}

	public void changeChatAdd() {
		if (this.chatAddLayout.getVisibility() == View.VISIBLE) {
			this.chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_add));
			this.chatAddLayout.setVisibility(View.GONE);
		} else {
			this.chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_return));
			if (thisController.inputManager.isActive(chatInput)) {
				thisController.inputManager.hide(chatInput);
			}
			if (this.faceLayout.getVisibility() == View.VISIBLE) {
				this.faceLayout.setVisibility(View.GONE);
			}
			this.chatAddLayout.setVisibility(View.VISIBLE);
		}
	}

	public void changeChatSmily() {
		if (this.faceLayout.getVisibility() == View.VISIBLE) {
			this.faceLayout.setVisibility(View.GONE);
		} else {
			if (thisController.inputManager.isActive(chatInput)) {
				thisController.inputManager.hide(chatInput);
			}
			if (this.chatAddLayout.getVisibility() == View.VISIBLE) {
				this.chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_add));
				this.chatAddLayout.setVisibility(View.GONE);
			}
			this.faceLayout.setVisibility(View.VISIBLE);
		}

	}

	public void changeChatInput() {
		if (this.faceLayout.getVisibility() == View.VISIBLE) {
			this.faceLayout.setVisibility(View.GONE);
		}
		if (this.chatAddLayout.getVisibility() == View.VISIBLE) {
			this.chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_add));
			this.chatAddLayout.setVisibility(View.GONE);
		}
		if (this.faceLayout.getVisibility() == View.VISIBLE) {
			this.faceLayout.setVisibility(View.GONE);
		}

	}

}
