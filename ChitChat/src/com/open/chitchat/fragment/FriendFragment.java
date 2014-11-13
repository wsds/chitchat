package com.open.chitchat.fragment;

import java.util.List;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.open.chitchat.BusinessActivity;
import com.open.chitchat.FindListActivity;
import com.open.chitchat.MainActivity;
import com.open.chitchat.R;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.model.API;
import com.open.chitchat.model.ActivityManager;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Data.Relationship.Group;
import com.open.chitchat.model.FileHandlers;
import com.open.chitchat.model.ResponseHandlers;
import com.open.chitchat.utils.BaseDataUtils;
import com.open.chitchat.view.PopMenuView;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

@SuppressLint("InflateParams")
public class FriendFragment extends Fragment {
	private Data data = Data.getInstance();
	private ActivityManager activityManager = ActivityManager.getInstance();
	private FileHandlers fileHandlers = FileHandlers.getInstance();

	private DisplayImageOptions headOptions;

	private MainActivity thisActivity;

	private View mContentView, backView;
	private LayoutInflater mInflater;

	private TextView titleText, myGroupNum, myJoinedGroupNum, friendsCount, fansCount, attentionCount;
	private RelativeLayout rightContainer;
	private ImageView titleImage;
	private View friends, fans, attention, addFriend;
	private ListView myGroupList, myJoinGroupList;
	private ScrollView scroll;

	private PopMenuView mPopupWindowView;
	private PopupWindow mPopupWindow;

	private MyOnClickListener mOnClickListener;
	private OnKeyListener mOnKeyListener;

	public CreatedGroupAdapter mCreatedGroupAdapter;
	public JoinedGroupAdapter mJoinedGroupAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mContentView = mInflater.inflate(R.layout.fragment_friend, null);
		thisActivity = (MainActivity) this.getActivity();
		initViews();
		initData();
		initListeners();
		showGroupsView();
		return mContentView;
	}

	private void initData() {
		getUserGroups();
		headOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(40)).build();
		mCreatedGroupAdapter = new CreatedGroupAdapter();
		mJoinedGroupAdapter = new JoinedGroupAdapter();
		myGroupList.setAdapter(mCreatedGroupAdapter);
		myJoinGroupList.setAdapter(mJoinedGroupAdapter);
	}

	private void initListeners() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.getTag(R.id.tag_first) != null) {
					int gid = (Integer) view.getTag(R.id.tag_first);
					Intent intent = new Intent(thisActivity, BusinessActivity.class);
					intent.putExtra("key", String.valueOf(gid));
					intent.putExtra("type", "group");
					thisActivity.startActivity(intent);
				} else if (view.equals(titleImage)) {
					changePopMenuView();
				} else if (view.equals(addFriend)) {
					activityManager.mMainActivity.mainTabView.setItem(3);
					((FindFragment) activityManager.mMainActivity.findFragment).changedSearchPeople();
				} else if (view.equals(friends)) {
					Intent intent = new Intent(thisActivity, FindListActivity.class);
					intent.putExtra("type", "friends");
					thisActivity.startActivity(intent);
				} else if (view.equals(attention)) {
					Intent intent = new Intent(thisActivity, FindListActivity.class);
					intent.putExtra("type", "attention");
					thisActivity.startActivity(intent);
				} else if (view.equals(fans)) {
					Intent intent = new Intent(thisActivity, FindListActivity.class);
					intent.putExtra("type", "fans");
					thisActivity.startActivity(intent);
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
		myGroupNum = (TextView) mContentView.findViewById(R.id.myGroupNum);
		myJoinedGroupNum = (TextView) mContentView.findViewById(R.id.myJoinedGroupNum);
		friendsCount = (TextView) mContentView.findViewById(R.id.friendsCount);
		fansCount = (TextView) mContentView.findViewById(R.id.fansCount);
		attentionCount = (TextView) mContentView.findViewById(R.id.attentionCount);

		rightContainer = (RelativeLayout) mContentView.findViewById(R.id.rightContainer);
		scroll = (ScrollView) mContentView.findViewById(R.id.scroll);

		friends = mContentView.findViewById(R.id.friends);
		fans = mContentView.findViewById(R.id.fans);
		attention = mContentView.findViewById(R.id.attention);
		addFriend = mContentView.findViewById(R.id.addFriend);

		myGroupList = (ListView) mContentView.findViewById(R.id.myGroupList);
		myJoinGroupList = (ListView) mContentView.findViewById(R.id.myJoinGroupList);

		titleImage = new ImageView(thisActivity);
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);

		backView.setVisibility(View.INVISIBLE);
		titleText.setText(R.string.friend_title);
		titleText.setFocusable(true);
		titleText.setFocusableInTouchMode(true);
		titleText.requestFocus();

		mPopupWindowView = new PopMenuView(thisActivity);
		mPopupWindow = new PopupWindow(mPopupWindowView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setOutsideTouchable(true);

	}

	public void getUserGroups() {
		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();
		params.addBodyParameter("phone", data.userInformation.currentUser.phone);
		params.addBodyParameter("accessKey", data.userInformation.currentUser.accessKey);
		ResponseHandlers responseHandlers = ResponseHandlers.getInstance();

		httpUtils.send(HttpMethod.POST, API.GROUP_GETGROUPMEMBERS, params, responseHandlers.getGroupsAndMembersCallBack);
	}

	public void showGroupsView() {
		friendsCount.setText(String.valueOf(data.relationship.friends.size()));
		fansCount.setText(String.valueOf(data.relationship.fans.size()));
		attentionCount.setText(String.valueOf(data.relationship.attentions.size()));
		mCreatedGroupAdapter.notifyDataSetChanged();
		mJoinedGroupAdapter.notifyDataSetChanged();
	}

	public void changePopMenuView() {
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
			mPopupWindow.showAsDropDown(titleImage);
		}
	}

	public class CreatedGroupAdapter extends BaseAdapter {
		private List<String> createdGroups;
		private ViewGroup.LayoutParams params;

		public CreatedGroupAdapter() {
			this.createdGroups = data.relationship.createdGroups;
			this.params = myGroupList.getLayoutParams();
		}

		@Override
		public void notifyDataSetChanged() {
			params.height = (int) BaseDataUtils.dpToPx(51 * createdGroups.size());
			myGroupNum.setText(String.valueOf(createdGroups.size()));
			super.notifyDataSetChanged();
			myGroupList.post(new Runnable() {
				@Override
				public void run() {
					scroll.scrollTo(0, 0);
				}
			});
		}

		@Override
		public int getCount() {
			return createdGroups.size();
		}

		@Override
		public Object getItem(int position) {
			return createdGroups.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Group group = data.relationship.groupsMap.get(createdGroups.get(position));
			GroupHolder holder;
			if (convertView == null) {
				holder = new GroupHolder();
				convertView = mInflater.inflate(R.layout.group_list_item, null);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.members = (TextView) convertView.findViewById(R.id.members);

				convertView.setTag(holder);
			} else {
				holder = (GroupHolder) convertView.getTag();
			}
			fileHandlers.getHeadImage(group.icon, holder.icon, headOptions);
			holder.name.setText(group.name);
			holder.members.setText(String.valueOf(group.members.size()));
			convertView.setTag(R.id.tag_first, group.gid);
			convertView.setOnClickListener(mOnClickListener);
			return convertView;
		}

		class GroupHolder {
			ImageView icon;
			TextView name, members;
		}
	}

	public class JoinedGroupAdapter extends BaseAdapter {
		private List<String> joinedGroups;
		private ViewGroup.LayoutParams params;

		public JoinedGroupAdapter() {
			this.joinedGroups = data.relationship.joinedGroups;
			this.params = myJoinGroupList.getLayoutParams();
		}

		@Override
		public void notifyDataSetChanged() {
			params.height = (int) BaseDataUtils.dpToPx(51 * joinedGroups.size());
			myJoinedGroupNum.setText(String.valueOf(joinedGroups.size()));
			super.notifyDataSetChanged();
			myJoinGroupList.post(new Runnable() {

				@Override
				public void run() {
					scroll.scrollTo(0, 0);
				}
			});
		}

		@Override
		public int getCount() {
			return joinedGroups.size();
		}

		@Override
		public Object getItem(int position) {
			return joinedGroups.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Group group = data.relationship.groupsMap.get(joinedGroups.get(position));
			GroupHolder holder;
			if (convertView == null) {
				holder = new GroupHolder();
				convertView = mInflater.inflate(R.layout.group_list_item, null);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.members = (TextView) convertView.findViewById(R.id.members);

				convertView.setTag(holder);
			} else {
				holder = (GroupHolder) convertView.getTag();
			}

			fileHandlers.getHeadImage(group.icon, holder.icon, headOptions);
			holder.name.setText(group.name);
			holder.members.setText(String.valueOf(group.members.size()));
			convertView.setTag(R.id.tag_first, group.gid);
			convertView.setOnClickListener(mOnClickListener);
			return convertView;
		}

		class GroupHolder {
			ImageView icon;
			TextView name, members;
		}
	}
}
