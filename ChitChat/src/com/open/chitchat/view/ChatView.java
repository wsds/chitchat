package com.open.chitchat.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
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

import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.controller.ChatController;
import com.open.chitchat.model.Constant;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Data.Messages.Message;
import com.open.chitchat.model.Data.Relationship.Friend;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.Parser;

public class ChatView {
	public Data data = Data.getInstance();
	public Parser parser = Parser.getInstance();

	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	public View backView, chatMenuLayout, textLayout, voiceLayout, chatSmilyLayout, takePhoto, ablum, location;
	public RelativeLayout rightContainer;
	public TextView titleText, chatSend;
	public ListView chatContent;
	public ImageView chatAdd, chatSmily, chatRecord, titleImage;
	public EditText chatInput;
	public GridView chatMenu;

	public ChatAdapter mChatAdapter;
	public ChatMenuAdapter mChatMenuAdapter;

	private Animation inTranslateAnimation, outTranslateAnimation;

	public ChatView(ChatActivity activity) {
		thisActivity = activity;
	}

	public void initViews() {
		thisActivity.setContentView(R.layout.activity_chat);
		backView = thisActivity.findViewById(R.id.backView);
		chatMenuLayout = thisActivity.findViewById(R.id.chatMenuLayout);
		textLayout = thisActivity.findViewById(R.id.textLayout);
		voiceLayout = thisActivity.findViewById(R.id.voiceLayout);
		chatSmilyLayout = thisActivity.findViewById(R.id.chatSmilyLayout);
		takePhoto = thisActivity.findViewById(R.id.takePhoto);
		ablum = thisActivity.findViewById(R.id.ablum);
		location = thisActivity.findViewById(R.id.location);
		rightContainer = (RelativeLayout) thisActivity.findViewById(R.id.rightContainer);
		titleText = (TextView) thisActivity.findViewById(R.id.titleText);
		chatSend = (TextView) thisActivity.findViewById(R.id.chatSend);
		chatContent = (ListView) thisActivity.findViewById(R.id.chatContent);
		chatAdd = (ImageView) thisActivity.findViewById(R.id.chatAdd);
		chatSmily = (ImageView) thisActivity.findViewById(R.id.chatSmily);
		chatRecord = (ImageView) thisActivity.findViewById(R.id.chatRecord);
		chatInput = (EditText) thisActivity.findViewById(R.id.chatInput);
		chatMenu = (GridView) thisActivity.findViewById(R.id.chatMenu);

		chatRecord.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_record));
		chatAdd.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_chat_add));

		titleImage = new ImageView(thisActivity);
		titleImage.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_arrow_down));
		rightContainer.addView(titleImage);

		mChatAdapter = new ChatAdapter();
		chatContent.setAdapter(mChatAdapter);

		mChatMenuAdapter = new ChatMenuAdapter();
		chatMenu.setAdapter(mChatMenuAdapter);

		inTranslateAnimation = AnimationUtils.loadAnimation(thisActivity, R.anim.chat_menu_in);
		outTranslateAnimation = AnimationUtils.loadAnimation(thisActivity, R.anim.chat_menu_out);
	}

	public class ChatAdapter extends BaseAdapter {
		ArrayList<Message> messages;

		public ChatAdapter() {
			String type = thisController.type, key = thisController.key;
			ArrayList<Message> messages = null;
			parser.check();
			if ("group".equals(type)) {
				messages = data.messages.messageMap.get("g" + key);
				if (messages == null) {
					messages = new ArrayList<Data.Messages.Message>();
					data.messages.messageMap.put("g" + key, messages);
				}
			} else if ("point".equals(type)) {
				messages = data.messages.messageMap.get("p" + key);
				if (messages == null) {
					messages = new ArrayList<Data.Messages.Message>();
					data.messages.messageMap.put("p" + key, messages);
				}
			}
			this.messages = messages;
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Message message = messages.get(position);
			ChatHolder holder = new ChatHolder();
			if (message.type == Constant.MESSAGE_TYPE_SEND) {
				convertView = thisActivity.mInflater.inflate(R.layout.f_chat_item_send, null);
			} else if (message.type == Constant.MESSAGE_TYPE_RECEIVE) {
				convertView = thisActivity.mInflater.inflate(R.layout.f_chat_item_receive, null);
			}
			holder.voice = convertView.findViewById(R.id.voice);
			holder.images_layout = convertView.findViewById(R.id.images_layout);
			holder.voice_icon = (ImageView) convertView.findViewById(R.id.voice_icon);
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.images = (ImageView) convertView.findViewById(R.id.images);
			holder.head = (ImageView) convertView.findViewById(R.id.head);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.character = (TextView) convertView.findViewById(R.id.character);
			holder.voicetime = (TextView) convertView.findViewById(R.id.voicetime);
			holder.images_count = (TextView) convertView.findViewById(R.id.images_count);
			if (message.contentType.equals("text")) {
				holder.character.setVisibility(View.VISIBLE);
				holder.voice.setVisibility(View.GONE);
				holder.image.setVisibility(View.GONE);
				holder.character.setText(message.content);
			} else if (message.contentType.equals("voice")) {
				holder.character.setVisibility(View.GONE);
				holder.voice.setVisibility(View.VISIBLE);
				holder.image.setVisibility(View.GONE);
			} else if (message.contentType.equals("image")) {
				holder.character.setVisibility(View.GONE);
				holder.voice.setVisibility(View.GONE);
				holder.image.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		class ChatHolder {
			View voice, images_layout;
			ImageView voice_icon, image, images, head;
			TextView time, character, voicetime, images_count;
		}

	}

	@SuppressLint("ViewHolder")
	public class ChatMenuAdapter extends BaseAdapter {
		public List<String> menuString;
		public List<Integer> menuImage;

		public ChatMenuAdapter() {
			boolean weather = true;
			menuString = new ArrayList<String>();
			menuImage = new ArrayList<Integer>();

			if (weather) {
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

	public void changeChatMenu() {
		if (this.chatMenuLayout.getVisibility() == View.GONE) {
			this.titleImage.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_arrow_up));
			this.chatMenuLayout.startAnimation(inTranslateAnimation);
			this.chatMenuLayout.setVisibility(View.VISIBLE);
		} else {
			this.titleImage.setImageDrawable(thisActivity.getResources().getDrawable(R.drawable.selector_arrow_down));
			this.chatMenuLayout.startAnimation(outTranslateAnimation);
			this.chatMenuLayout.setVisibility(View.GONE);
		}
	}

}
