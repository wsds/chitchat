package com.open.chitchat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.cloud.model.AMapCloudException;
import com.amap.api.cloud.model.CloudItem;
import com.amap.api.cloud.model.CloudItemDetail;
import com.amap.api.cloud.model.LatLonPoint;
import com.amap.api.cloud.search.CloudResult;
import com.amap.api.cloud.search.CloudSearch;
import com.amap.api.cloud.search.CloudSearch.OnCloudSearchListener;
import com.amap.api.cloud.search.CloudSearch.Query;
import com.amap.api.cloud.search.CloudSearch.SearchBound;
import com.amap.api.cloud.search.CloudSearch.Sortingrules;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.model.ActivityManager;
import com.open.chitchat.model.Constant;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.Data.Relationship.Friend;
import com.open.chitchat.model.Data.UserInformation.User;
import com.open.chitchat.model.FileHandlers;
import com.open.chitchat.utils.BaseDataUtils;
import com.open.chitchat.utils.DateUtil;
import com.open.chitchat.utils.DistanceUtils;
import com.open.chitchat.view.MyListView;
import com.open.chitchat.view.MyListView.MyListViewListener;

public class FindListActivity extends Activity {

	private Data data = Data.getInstance();
	private FileHandlers fileHandlers = FileHandlers.getInstance();
	private User user;

	private LocationManagerProxy mLocationManagerProxy;
	private SearchBound bound;
	private Query mQuery;
	private Sortingrules mSortingrules;
	private CloudSearch mCloudSearch;
	private ArrayList<CloudItem> mCloudItems;
	private AMapLocation mAmapLocation;

	public OnCloudSearchListener mCloudSearchListener;
	public AMapLocationListener mAMapLocationListener;

	private DisplayImageOptions headOptions;

	private LayoutInflater mInflater;

	private View backView;
	private ViewGroup rightContainer;
	private EditText input;
	private TextView titleText, search;
	private MyListView list;

	private MyOnClickListener mOnClickListener;
	private MyListViewListener myListViewListener;

	private MyListAdapter mAdapter;

	private String type;
	private int listCount, nowPage;

	public String mTableId;
	private ArrayList<Map<String, Object>> mInfomations;

	private ActivityManager activityManager = ActivityManager.getInstance();

	private enum Status {
		localData, serverData, LbsData
	}

	private Status mStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityManager.mFindListActivity = this;
		setContentView(R.layout.activity_find_list);
		mInflater = getLayoutInflater();
		type = getIntent().getStringExtra("type");
		initListener();
		initViews();
		initData();
		bindEvent();
		search();
	}

	private void search() {
		if (mStatus == Status.LbsData) {
			mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, mAMapLocationListener);
			mLocationManagerProxy.setGpsEnable(true);
		} else if (mStatus == Status.serverData) {

		}

	}

	private void initListener() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.getTag(R.id.tag_first) != null) {
					String key = (String) view.getTag(R.id.tag_first);
					String type = (String) view.getTag(R.id.tag_second);
					Intent intent = new Intent(FindListActivity.this, BusinessActivity.class);
					intent.putExtra("key", key);
					intent.putExtra("type", type);
					startActivity(intent);
				} else if (view.equals(backView)) {
					finish();
				}
			}
		};
		myListViewListener = new MyListViewListener() {

			@Override
			public void onRefresh() {
				listCount = 20;
				list.setPullLoadEnable(true);
				if (mStatus == Status.LbsData) {
					nowPage = 0;
					searchNearByPolygon(nowPage);
				} else if (mStatus == Status.localData) {
					onLoaded();
				} else if (mStatus == Status.serverData) {

				}
			}

			@Override
			public void onLoadMore() {
				listCount += 20;
				if (mStatus == Status.LbsData) {
					searchNearByPolygon(++nowPage);
				} else if (mStatus == Status.localData) {
					onLoaded();
				} else if (mStatus == Status.serverData) {

				}
			}
		};
		mCloudSearchListener = new OnCloudSearchListener() {

			@SuppressWarnings("rawtypes")
			@Override
			public void onCloudSearched(CloudResult result, int rCode) {
				if (rCode == 0) {
					if (result != null && result.getQuery() != null) {
						if (result.getQuery().equals(mQuery)) {
							mCloudItems = result.getClouds();
							if (mAmapLocation == null) {
								return;
							}
							if (nowPage == 0) {
								mInfomations.clear();
							}
							if (mCloudItems.size() == 0) {
								nowPage--;
							}
							LatLng point = new LatLng(mAmapLocation.getLatitude(), mAmapLocation.getLongitude());
							for (CloudItem item : mCloudItems) {
								Map<String, Object> map = new HashMap<String, Object>();
								LatLng point2 = new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
								map.put("location", item.getLatLonPoint().toString());
								map.put("name", item.getTitle());
								map.put("address", item.getSnippet());
								map.put("distance", item.getDistance() == -1 ? (int) AMapUtils.calculateLineDistance(point, point2) : item.getDistance());
								Iterator iter = item.getCustomfield().entrySet().iterator();
								while (iter.hasNext()) {
									Map.Entry entry = (Map.Entry) iter.next();
									map.put(entry.getKey().toString(), entry.getValue());
								}
								if (!mInfomations.contains(map)) {
									mInfomations.add(map);
								}
							}
						}
					}
				}
				onLoaded();
			}

			@Override
			public void onCloudItemDetailSearched(CloudItemDetail arg0, int arg1) {

			}
		};
		mAMapLocationListener = new AMapLocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onLocationChanged(Location location) {
			}

			@Override
			public void onLocationChanged(AMapLocation amapLocation) {
				mLocationManagerProxy.removeUpdates(mAMapLocationListener);
				mLocationManagerProxy.destroy();
				if (amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
					mAmapLocation = amapLocation;
					data.userInformation.currentUser.latitude = String.valueOf(amapLocation.getLatitude());
					data.userInformation.currentUser.longitude = String.valueOf(amapLocation.getLongitude());
					nowPage = 0;
					searchNearByPolygon(nowPage);
				} else {

				}
			}
		};
	}

	private void bindEvent() {
		backView.setOnClickListener(mOnClickListener);
		list.setMyListViewListener(myListViewListener);
		if (mStatus == Status.LbsData) {
			mCloudSearch.setOnCloudSearchListener(mCloudSearchListener);
		}
	}

	public ArrayList<String> accounts;
	public HashMap<String, Friend> accountsMap;

	private void initData() {
		user = data.userInformation.currentUser;
		headOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).displayer(new RoundedBitmapDisplayer(40)).build();
		list.setPullLoadEnable(true);
		listCount = 20;
		if (mStatus == Status.LbsData) {
			mInfomations = new ArrayList<Map<String, Object>>();
			mCloudSearch = new CloudSearch(this);
			mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		} else if (mStatus == Status.localData) {

		} else if (mStatus == Status.serverData) {
			accounts = data.tempData.friends;
			data.tempData.friends = null;
			accountsMap = data.tempData.friendsMap;
			data.tempData.friendsMap = null;
		}
		mAdapter = new MyListAdapter();
		list.setAdapter(mAdapter);
	}

	private void initViews() {
		backView = findViewById(R.id.backView);
		rightContainer = (ViewGroup) findViewById(R.id.rightContainer);
		input = (EditText) findViewById(R.id.input);
		titleText = (TextView) findViewById(R.id.titleText);
		search = (TextView) findViewById(R.id.search);
		list = (MyListView) findViewById(R.id.list);

		SpannableStringBuilder style;
		String title = "";
		if ("friends".equals(type)) {
			title = getString(R.string.friends) + "(" + data.relationship.friends.size() + ")";
			style = new SpannableStringBuilder(title);
			style.setSpan(new ForegroundColorSpan(Color.RED), title.indexOf("(") + 1, title.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleText.setText(style);
			mStatus = Status.localData;
		} else if ("attention".equals(type)) {
			title = getString(R.string.attention) + "(" + data.relationship.attentions.size() + ")";
			style = new SpannableStringBuilder(title);
			style.setSpan(new ForegroundColorSpan(Color.RED), title.indexOf("(") + 1, title.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleText.setText(style);
			mStatus = Status.localData;
		} else if ("fans".equals(type)) {
			title = getString(R.string.fans) + "(" + data.relationship.fans.size() + ")";
			style = new SpannableStringBuilder(title);
			style.setSpan(new ForegroundColorSpan(Color.RED), title.indexOf("(") + 1, title.indexOf(")"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			titleText.setText(style);
			mStatus = Status.localData;
		} else if ("nearbyGroup".equals(type)) {
			titleText.setText(getString(R.string.nearbyGroup));
			mStatus = Status.LbsData;
			mTableId = Constant.GROUPTABLEID;
		} else if ("nearbyPeople".equals(type)) {
			titleText.setText(getString(R.string.nearbyPeople));
			mStatus = Status.LbsData;
			mTableId = Constant.ACCOUNTTABLEID;
		} else if ("searchGroup".equals(type)) {
			titleText.setText(getString(R.string.searchResults));
			mStatus = Status.serverData;
		} else if ("searchPeople".equals(type)) {
			titleText.setText(getString(R.string.searchResults));
			mStatus = Status.serverData;
		}
	}

	private void onLoaded() {
		list.stopRefresh();
		list.stopLoadMore();
		list.setRefreshTime(getString(R.string.justNow));
		mAdapter.notifyDataSetChanged();
	}

	private void searchNearByPolygon(int nowpage) {
		List<LatLonPoint> points = new ArrayList<LatLonPoint>();
		points.add(new LatLonPoint(5.965754, 70.136719));
		points.add(new LatLonPoint(56.170023, 140.097656));
		try {
			mQuery = new Query(mTableId, "", new SearchBound(points));
		} catch (AMapCloudException e) {
			e.printStackTrace();
		}
		mQuery.setPageSize(20);
		mQuery.setPageNum(nowpage);
		mSortingrules = new Sortingrules(1);// 0为权重降序排列，1为距离升序排列。
		mQuery.setSortingrules(mSortingrules);
		mCloudSearch.searchCloudAsyn(mQuery);

	}

	class MyListAdapter extends BaseAdapter {
		private ArrayList<Map<String, Object>> infomations = null;
		private List<String> friendList = null;

		public MyListAdapter() {
			if ("friends".equals(type)) {
				friendList = data.relationship.friends;
			} else if ("attention".equals(type)) {
				friendList = data.relationship.attentions;
			} else if ("fans".equals(type)) {
				friendList = data.relationship.fans;
			} else if ("nearbyGroup".equals(type) || "nearbyPeople".equals(type)) {
				infomations = mInfomations;
			} else if ("searchGroup".equals(type)) {

			} else if ("searchPeople".equals(type)) {

			}
		}

		@Override
		public void notifyDataSetChanged() {
			if (friendList != null) {
				if (friendList.size() == mAdapter.getCount()) {
					list.setPullLoadEnable(false);
				} else {
					list.setPullLoadEnable(true);
				}
			}
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			int count = 0;
			if (friendList != null) {
				count = friendList.size() < listCount ? friendList.size() : listCount;
			} else if (mInfomations != null) {
				count = mInfomations.size() < listCount ? mInfomations.size() : listCount;
			} else if (accounts != null) {
				count = accounts.size() < listCount ? accounts.size() : listCount;
			}
			return count;
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
			MyListHolder holder;
			String name = "", alias = "", head = "", sex = "", mainBusiness = "", age = "", time = "";
			float distance = 0;
			if (convertView == null) {
				holder = new MyListHolder();
				convertView = mInflater.inflate(R.layout.find_list_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.head = (ImageView) convertView.findViewById(R.id.head);
				holder.sex = (ImageView) convertView.findViewById(R.id.sex);
				holder.chat = (ImageView) convertView.findViewById(R.id.chat);
				holder.ageOrCount = (TextView) convertView.findViewById(R.id.ageOrCount);
				holder.distance = (TextView) convertView.findViewById(R.id.distance);
				holder.mainBusiness = (TextView) convertView.findViewById(R.id.mainBusiness);
				holder.recently = (TextView) convertView.findViewById(R.id.recently);
				convertView.setTag(holder);
			} else {
				holder = (MyListHolder) convertView.getTag();
			}
			if (friendList != null) {
				Friend friend = data.relationship.friendsMap.get(friendList.get(position));
				holder.chat.setVisibility(View.GONE);
				holder.sex.setVisibility(View.VISIBLE);

				name = friend.nickName;
				alias = friend.alias;
				head = friend.head;
				sex = friend.sex;
				mainBusiness = friend.mainBusiness;
				age = String.valueOf(friend.age);
				time = friend.lastLoginTime;
				distance = Float.valueOf(DistanceUtils.getDistance(user.longitude.equals("") ? "0" : user.longitude, user.latitude.equals("") ? "0" : user.latitude, friend.longitude, friend.latitude));
				convertView.setTag(R.id.tag_first, friend.phone);
				convertView.setTag(R.id.tag_second, "point");
			} else if (mInfomations != null) {
				Map<String, Object> infomation = infomations.get(position);

				if ("nearbyGroup".equals(type)) {
					holder.chat.setVisibility(View.VISIBLE);
					holder.sex.setVisibility(View.GONE);
					name = (String) infomation.get("name");
					head = (String) infomation.get("icon");
					mainBusiness = (String) infomation.get("description");
					distance = (Integer) infomation.get("distance");
					convertView.setTag(R.id.tag_first, (String) infomation.get("gid"));
					convertView.setTag(R.id.tag_second, "group");
				} else if ("nearbyPeople".equals(type)) {
					holder.chat.setVisibility(View.GONE);
					holder.sex.setVisibility(View.VISIBLE);
					name = (String) infomation.get("name");
					head = (String) infomation.get("head");
					sex = (String) infomation.get("sex");
					mainBusiness = (String) infomation.get("mainBusiness");
					age = (String) infomation.get("age");
					time = (String) infomation.get("lastlogintime");
					distance = (Integer) infomation.get("distance");
					convertView.setTag(R.id.tag_first, (String) infomation.get("phone"));
					convertView.setTag(R.id.tag_second, "point");
				}
			} else if (accounts != null) {
				Friend friend = accountsMap.get(accounts.get(position));
				holder.chat.setVisibility(View.GONE);
				holder.sex.setVisibility(View.VISIBLE);
				name = friend.nickName;
				head = friend.head;
				sex = friend.sex;
				mainBusiness = friend.mainBusiness;
				age = friend.age + "";
				time = friend.lastLoginTime;
				distance = friend.distance;
				convertView.setTag(R.id.tag_first, friend.phone);
				convertView.setTag(R.id.tag_second, "point");
			}
			fileHandlers.getHeadImage(head, holder.head, headOptions);
			holder.name.setText(BaseDataUtils.generateUserName(name, alias));
			holder.distance.setText(DistanceUtils.getDistance(distance));
			if (BaseDataUtils.determineSex(sex)) {
				holder.sex.setImageResource(R.drawable.personalinfo_male);
			} else {
				holder.sex.setImageResource(R.drawable.personalinfo_female);
			}
			if ("".equals(mainBusiness)) {
				if ("nearbyGroup".equals(type)) {
					holder.mainBusiness.setText(getString(R.string.groupNoDescription));
				} else if ("nearbyPeople".equals(type)) {
					holder.mainBusiness.setText(getString(R.string.userNoDescription));
				}
			} else {
				holder.mainBusiness.setText(mainBusiness);
			}
			if (age != null && !"".equals(age)) {
				holder.ageOrCount.setText(age);
			} else {
				holder.ageOrCount.setText("20");
			}
			if (!"".equals(time)) {
				try {
					holder.recently.setText(DateUtil.getMessageSequeceTime(Long.valueOf(time)));
				} catch (NumberFormatException e) {
					holder.recently.setText("");
					e.printStackTrace();
				}
			} else {
				holder.recently.setText("");
			}
			convertView.setOnClickListener(mOnClickListener);
			return convertView;
		}

		class MyListHolder {
			ImageView head, sex, chat;
			TextView name, ageOrCount, distance, mainBusiness, recently;
		}

	}
}
