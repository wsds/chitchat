package com.open.chitchat.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.chitchat.FindListActivity;
import com.open.chitchat.MainActivity;
import com.open.chitchat.R;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.model.API;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.model.ResponseHandlers;
import com.open.chitchat.utils.InputMethodManagerUtils;
import com.open.chitchat.view.PopMenuView;

public class FindFragment extends Fragment {
	private View mContentView, backView;
	private LayoutInflater mInflater;

	private MainActivity thisActivity;

	private TextView titleText, search, searchText;
	private RelativeLayout rightContainer;
	private ImageView titleImage, searchImage;
	private EditText input;
	private View searchLayout, myLike, nearbyGroup, hotGroup, classifyGroup,
			nearbyPeople, mSearchPopupWindowView, searchGroup, searchPeople;

	private PopMenuView mPopupWindowView;
	private PopupWindow mPopupWindow, mSearchPopupWindow;

	private MyOnClickListener mOnClickListener;
	private OnKeyListener mOnKeyListener;
	private OnDismissListener mOnDismissListener;
	private TextWatcher mTextWatcher;

	private InputMethodManagerUtils mInputManager;

	private enum Status {
		searchGroup, searchPeople
	}

	private Status status = Status.searchGroup;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		thisActivity = (MainActivity) this.getActivity();
		mContentView = mInflater.inflate(R.layout.fragment_find, null);
		mSearchPopupWindowView = mInflater.inflate(
				R.layout.fragment_find_searchpop, null);
		initViews();
		initListeners();
		initData();
		return mContentView;
	}

	private void initData() {
		mInputManager = new InputMethodManagerUtils(thisActivity);
	}

	private void initViews() {
		backView = mContentView.findViewById(R.id.backView);
		searchLayout = mContentView.findViewById(R.id.searchLayout);
		titleText = (TextView) mContentView.findViewById(R.id.titleText);
		search = (TextView) mContentView.findViewById(R.id.search);
		searchText = (TextView) mContentView.findViewById(R.id.searchText);
		searchImage = (ImageView) mContentView.findViewById(R.id.searchImage);
		input = (EditText) mContentView.findViewById(R.id.input);
		rightContainer = (RelativeLayout) mContentView
				.findViewById(R.id.rightContainer);

		myLike = mContentView.findViewById(R.id.myLike);
		nearbyGroup = mContentView.findViewById(R.id.nearbyGroup);
		hotGroup = mContentView.findViewById(R.id.hotGroup);
		classifyGroup = mContentView.findViewById(R.id.classifyGroup);
		nearbyPeople = mContentView.findViewById(R.id.nearbyPeople);

		titleImage = new ImageView(thisActivity);
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);

		backView.setVisibility(View.INVISIBLE);
		titleText.setText(R.string.find_title);

		mPopupWindowView = new PopMenuView(thisActivity);
		mPopupWindow = new PopupWindow(mPopupWindowView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);

		searchGroup = mSearchPopupWindowView.findViewById(R.id.searchGroup);
		searchPeople = mSearchPopupWindowView.findViewById(R.id.searchPeople);
		mSearchPopupWindow = new PopupWindow(mSearchPopupWindowView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mSearchPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mSearchPopupWindow.setOutsideTouchable(true);
	}

	private void initListeners() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.equals(titleImage)) {
					changePopMenuView();
				} else if (view.equals(searchLayout)) {
					changeSearchPopMenuView(true, false);
				} else if (view.equals(searchGroup)) {
					status = Status.searchGroup;
					changeSearchPopMenuView(true, true);
				} else if (view.equals(searchPeople)) {
					status = Status.searchPeople;
					changeSearchPopMenuView(true, true);
				} else if (view.equals(nearbyGroup)) {
					Intent intent = new Intent(thisActivity,
							FindListActivity.class);
					intent.putExtra("type", "nearbyGroup");
					thisActivity.startActivity(intent);
				} else if (view.equals(nearbyPeople)) {
					Intent intent = new Intent(thisActivity,
							FindListActivity.class);
					intent.putExtra("type", "nearbyPeople");
					thisActivity.startActivity(intent);
				} else if (view.equals(search)) {
					Intent intent = new Intent(thisActivity,
							FindListActivity.class);
					if (status == Status.searchPeople) {
						intent.putExtra("type", "searchPeople");
						fuzzyQueryAccounts(input.getText().toString());
					} else if (status == Status.searchGroup) {
						intent.putExtra("type", "searchGroup");
						intent.putExtra("key", input.getText().toString());
						thisActivity.startActivity(intent);
					}
				}
			}

		};
		mOnKeyListener = new OnKeyListener() {

			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_MENU) {
					changePopMenuView();
					return true;
				}
				return false;
			}
		};
		mTextWatcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if ("".equals(s.toString())) {
					search.setVisibility(View.GONE);
				} else {
					search.setVisibility(View.VISIBLE);
				}

			}
		};
		mOnDismissListener = new OnDismissListener() {

			@Override
			public void onDismiss() {
				searchImage.setImageResource(R.drawable.activities_down);
			}
		};
		bindEvent();
	}

	private Data data = Data.getInstance();

	private ResponseHandlers responseHandlers = ResponseHandlers.getInstance();

	private void fuzzyQueryAccounts(String key) {
		User currentUser = data.userInformation.currentUser;
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone", currentUser.phone);
		params.addBodyParameter("accessKey", currentUser.accessKey);
		params.addBodyParameter("keyword", key);

		httpUtils.send(HttpMethod.POST, API.RELATION_FUZZYQUERY, params,
				responseHandlers.getFuzzyQuery);
	}

	private void bindEvent() {
		myLike.setOnClickListener(mOnClickListener);
		nearbyGroup.setOnClickListener(mOnClickListener);
		hotGroup.setOnClickListener(mOnClickListener);
		classifyGroup.setOnClickListener(mOnClickListener);
		nearbyPeople.setOnClickListener(mOnClickListener);
		titleImage.setOnClickListener(mOnClickListener);
		searchLayout.setOnClickListener(mOnClickListener);
		searchGroup.setOnClickListener(mOnClickListener);
		searchPeople.setOnClickListener(mOnClickListener);
		search.setOnClickListener(mOnClickListener);

		input.addTextChangedListener(mTextWatcher);

		mPopupWindowView.setOnKeyListener(mOnKeyListener);
		mSearchPopupWindow.setOnDismissListener(mOnDismissListener);
	}

	public void changePopMenuView() {
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
			mPopupWindow.showAsDropDown(titleImage);
		}
	}

	public void changedSearchPeople() {
		status = Status.searchPeople;
		changeSearchPopMenuView(false, true);
		mInputManager.show(input);
	}

	private void changeSearchPopMenuView(boolean showPop, boolean refresh) {
		if (showPop) {
			if (mSearchPopupWindow.isShowing()) {
				mSearchPopupWindow.dismiss();
				searchImage.setImageResource(R.drawable.activities_down);
			} else {
				mSearchPopupWindow.showAsDropDown(searchLayout);
				searchImage.setImageResource(R.drawable.activities_up);
			}
		}
		if (refresh) {
			if (status == Status.searchGroup) {
				searchText
						.setText(getResources().getText(R.string.searchGroup));
				input.setHint(R.string.searchGroupHint);
			} else if (status == Status.searchPeople) {
				searchText.setText(getResources()
						.getText(R.string.searchPeople));
				input.setHint(R.string.searchPeopleHint);
			}
		}
	}
}
