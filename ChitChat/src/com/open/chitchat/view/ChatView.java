package com.open.chitchat.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.controller.ChatController;

public class ChatView {
	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	public View backView;
	public RelativeLayout rightContainer;
	public TextView titleText, chatSend;
	public ListView chatContent;
	public ImageView chatAdd, chatSmily, chatRecord;
	public EditText chatInput;

	public ChatAdapter mChatAdapter;

	public ChatView(ChatActivity activity) {
		thisActivity = activity;
	}

	public void initViews() {
		thisActivity.setContentView(R.layout.activity_chat);
		backView = thisActivity.findViewById(R.id.backView);
		rightContainer = (RelativeLayout) thisActivity.findViewById(R.id.rightContainer);
		titleText = (TextView) thisActivity.findViewById(R.id.titleText);
		chatSend = (TextView) thisActivity.findViewById(R.id.chatSend);
		chatContent = (ListView) thisActivity.findViewById(R.id.chatContent);
		chatAdd = (ImageView) thisActivity.findViewById(R.id.chatAdd);
		chatSmily = (ImageView) thisActivity.findViewById(R.id.chatSmily);
		chatRecord = (ImageView) thisActivity.findViewById(R.id.chatRecord);
		chatInput = (EditText) thisActivity.findViewById(R.id.chatInput);

		mChatAdapter = new ChatAdapter();
		chatContent.setAdapter(mChatAdapter);
	}

	public class ChatAdapter extends BaseAdapter {

		public ChatAdapter() {

		}

		@Override
		public int getCount() {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			return null;
		}

	}

}
