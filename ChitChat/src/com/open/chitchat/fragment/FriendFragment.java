package com.open.chitchat.fragment;

import com.open.chitchat.R;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.view.PopMenuView;

import android.app.Fragment;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class FriendFragment extends Fragment {
	View mContentView, backView;
	LayoutInflater mInflater;

	private TextView titleText;
	private RelativeLayout rightContainer;
	private ImageView titleImage;
	private View friends, fans, attention, addFriend;

	private PopMenuView mPopupWindowView;
	private PopupWindow mPopupWindow;

	private MyOnClickListener mOnClickListener;
	private OnKeyListener mOnKeyListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mContentView = mInflater.inflate(R.layout.fragment_friend, null);
		initViews();
		initListeners();
		return mContentView;
	}

	private void initListeners() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.equals(titleImage)) {
					changePopMenuView();
				}
			}
		};
		mOnKeyListener = new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_MENU) {
					changePopMenuView();
					return true;
				}
				return false;
			}
		};
		bindEvent();
	}

	private void bindEvent() {
		friends.setOnClickListener(mOnClickListener);
		fans.setOnClickListener(mOnClickListener);
		attention.setOnClickListener(mOnClickListener);
		addFriend.setOnClickListener(mOnClickListener);
		titleImage.setOnClickListener(mOnClickListener);
		
		mPopupWindowView.setOnKeyListener(mOnKeyListener);
	}

	private void initViews() {
		backView = mContentView.findViewById(R.id.backView);
		titleText = (TextView) mContentView.findViewById(R.id.titleText);
		rightContainer = (RelativeLayout) mContentView.findViewById(R.id.rightContainer);

		friends = mContentView.findViewById(R.id.friends);
		fans = mContentView.findViewById(R.id.fans);
		attention = mContentView.findViewById(R.id.attention);
		addFriend = mContentView.findViewById(R.id.addFriend);

		titleImage = new ImageView(getActivity());
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);

		backView.setVisibility(View.INVISIBLE);
		titleText.setText(R.string.friend_title);

		mPopupWindowView = new PopMenuView(getActivity());
		mPopupWindow = new PopupWindow(mPopupWindowView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);

	}

	public void changePopMenuView() {
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
			mPopupWindow.showAsDropDown(titleImage);
		}
	}
}
