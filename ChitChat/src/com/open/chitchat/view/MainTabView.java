package com.open.chitchat.view;

import com.open.chitchat.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class MainTabView extends FrameLayout {

	private View chat, find, friend;
	private OnClickListener mOnClickListener;
	private OnTagClickListener mOnTagClickListener;
	private int lastposition = 1;

	public MainTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.maintab_view, this);
		chat = this.findViewById(R.id.chat);
		find = this.findViewById(R.id.find);
		friend = this.findViewById(R.id.friend);
		mOnClickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				if (view.equals(chat)) {
					if (mOnTagClickListener != null)
						mOnTagClickListener.OnTagClick(1, lastposition);
					onTagChanged(1);
				} else if (view.equals(friend)) {
					if (mOnTagClickListener != null)
						mOnTagClickListener.OnTagClick(2, lastposition);
					onTagChanged(2);
				} else if (view.equals(find)) {
					if (mOnTagClickListener != null)
						mOnTagClickListener.OnTagClick(3, lastposition);
					onTagChanged(3);
				}

			}
		};
		chat.setOnClickListener(mOnClickListener);
		find.setOnClickListener(mOnClickListener);
		friend.setOnClickListener(mOnClickListener);
	}

	private void onTagChanged(int position) {
		if (position == 1) {
			friend.setSelected(false);
			find.setSelected(false);
			chat.setSelected(true);
			chat.setClickable(false);
			friend.setClickable(true);
			find.setClickable(true);
		} else if (position == 2) {
			chat.setSelected(false);
			find.setSelected(false);
			friend.setSelected(true);
			friend.setClickable(false);
			chat.setClickable(true);
			find.setClickable(true);
		} else if (position == 3) {
			chat.setSelected(false);
			friend.setSelected(false);
			find.setSelected(true);
			find.setClickable(false);
			chat.setClickable(true);
			friend.setClickable(true);
		}
		lastposition = position;
	}

	public void setDefaultItem(int position) {
		onTagChanged(position);
	}

	public void setItem(int position) {
		if (position == 1) {
			chat.performClick();
		} else if (position == 2) {
			friend.performClick();
		} else if (position == 3) {
			find.performClick();
		}

	}

	public void setOnTagClickListener(OnTagClickListener mOnTagClickListener) {
		this.mOnTagClickListener = mOnTagClickListener;
	}

	public interface OnTagClickListener {
		public void OnTagClick(int positions, int lastpositions);
	}
}
