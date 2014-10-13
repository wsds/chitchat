package com.open.chitchat.fragment;

import com.open.chitchat.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriendFragment extends Fragment implements OnClickListener {
	View mContentView, backView;
	LayoutInflater mInflater;

	private TextView titleText;
	private RelativeLayout rightContainer;
	private ImageView titleImage;
	private View friends, fans, attention, addFriend;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mContentView = mInflater.inflate(R.layout.fragment_friend, null);
		initViews();
		return mContentView;
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

		friends.setOnClickListener(this);
		fans.setOnClickListener(this);
		attention.setOnClickListener(this);
		addFriend.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

	}
}
