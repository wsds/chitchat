package com.open.chitchat.controller;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.view.ChatView;

public class ChatController {
	public ChatView thisView;
	public ChatController thisController;
	public ChatActivity thisActivity;

	public OnClickListener mOnClickListener;
	public TextWatcher mTextWatcher;

	public ChatController(ChatActivity activity) {
		thisActivity = activity;
	}

	public void onCreate() {

		initListeners();
	}

	public void initListeners() {
		mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (thisView.backView.equals(view)) {
					thisActivity.finish();
				} else if (thisView.chatAdd.equals(view)) {

				} else if (thisView.chatSmily.equals(view)) {

				} else if (thisView.chatRecord.equals(view)) {

				} else if (thisView.chatSend.equals(view)) {

				}

			}
		};
		mTextWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

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

		thisView.chatInput.addTextChangedListener(mTextWatcher);
	}

	public void showSend(boolean whether) {

	}
}
