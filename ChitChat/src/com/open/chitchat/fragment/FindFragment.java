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

public class FindFragment extends Fragment implements OnClickListener {
	View mContentView, backView;
	LayoutInflater mInflater;

	private TextView titleText;
	private RelativeLayout rightContainer;
	private ImageView titleImage;
	private View myLike, nearbyGroup, hotGroup, classifyGroup, nearbyPeople;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mContentView = mInflater.inflate(R.layout.fragment_find, null);
		initViews();
		fillData();
		return mContentView;
	}

	private void fillData() {
		// TODO Auto-generated method stub

	}

	private void initViews() {
		backView = mContentView.findViewById(R.id.backView);
		titleText = (TextView) mContentView.findViewById(R.id.titleText);
		rightContainer = (RelativeLayout) mContentView.findViewById(R.id.rightContainer);

		myLike = mContentView.findViewById(R.id.myLike);
		nearbyGroup = mContentView.findViewById(R.id.nearbyGroup);
		hotGroup = mContentView.findViewById(R.id.hotGroup);
		classifyGroup = mContentView.findViewById(R.id.classifyGroup);
		nearbyPeople = mContentView.findViewById(R.id.nearbyPeople);

		titleImage = new ImageView(getActivity());
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);

		backView.setVisibility(View.INVISIBLE);
		titleText.setText(R.string.find_title);

		myLike.setOnClickListener(this);
		nearbyGroup.setOnClickListener(this);
		hotGroup.setOnClickListener(this);
		classifyGroup.setOnClickListener(this);
		nearbyPeople.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

	}
}
