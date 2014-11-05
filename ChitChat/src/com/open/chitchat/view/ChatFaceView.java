package com.open.chitchat.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.open.chitchat.R;
import com.open.chitchat.model.Constant;
import com.open.chitchat.model.Data;
import com.open.chitchat.utils.BaseDataUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class ChatFaceView extends FrameLayout {
	private Data data = Data.getInstance();

	private Context context;
	private FaceViewPager facePager;
	private PageControlView facePagerControl;
	private HorizontalScrollView faceViewList;

	private String type = "default";
	private List<Integer> defaultEmojis;
	private List<View> pagerViews;
	private List<String> faceList;

	public ChatFaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		onCreate();
	}

	private void onCreate() {
		LayoutInflater.from(context).inflate(R.layout.chat_face, this);
		facePager = (FaceViewPager) this.findViewById(R.id.facePager);
		facePagerControl = (PageControlView) this.findViewById(R.id.facePagerControl);
		faceViewList = (HorizontalScrollView) this.findViewById(R.id.faceList);
		pagerViews = new ArrayList<View>();
		defaultEmojis = new ArrayList<Integer>(Arrays.asList(Constant.EMOJIS));
		faceList = data.userInformation.currentUser.faceList;
		fillFaces();
		facePager.setAdapter(new ChatFaceAdapter());
	}

	private void fillFaces() {
		int total, line, row, eachPageNum, pageTotal;
		for (int i = 0; i < (faceList.size() + 1); i++) {
			if (i == 0) {
				total = defaultEmojis.size();
				line = 3;
				row = 7;
				eachPageNum = (line * row) - 1;
				pageTotal = total / eachPageNum + 1;
				for (int j = 0; j < pageTotal; j++) {
					ChatFaceGridView defaultGridOne = new ChatFaceGridView(this.context, facePager, "default");
					defaultGridOne.setNumColumns(row);
					defaultGridOne.setAdapter(new ChatFaceGridItemAdapter("default", j, eachPageNum));
					pagerViews.add(defaultGridOne);
				}
			} else {
				String facesName = faceList.get(i - 1);
			}
		}
	}

	private class ChatFaceAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pagerViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(pagerViews.get(position));
			return pagerViews.get(position);
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	private class ChatFaceGridItemAdapter extends BaseAdapter {
		private int total, current;
		private String type;

		public ChatFaceGridItemAdapter(String type, int current, int eachPageNum) {
			this.total = eachPageNum;
			this.current = current;
			this.type = type;
		}

		@Override
		public int getCount() {
			if (type.equals("default")) {
				return total + 1;
			} else {
				return total;
			}
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint({ "ViewHolder", "InflateParams" })
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (type.equals("default")) {
				convertView = new ImageView(context);
				android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, BaseDataUtils.dpToPx(60));
				convertView.setLayoutParams(params);
				convertView.setPadding(BaseDataUtils.dpToPx(5), BaseDataUtils.dpToPx(5), BaseDataUtils.dpToPx(5), BaseDataUtils.dpToPx(5));
				if (position == total) {
					convertView.setBackgroundResource(R.drawable.selector_chat_face_item);
					((ImageView) convertView).setImageResource(R.drawable.emotion_del);
					convertView.setTag(R.id.tag_first, "delete");
				} else {
					int resource = total * current + position;
					if (resource < defaultEmojis.size()) {
						convertView.setBackgroundResource(R.drawable.selector_chat_face_item);
						((ImageView) convertView).setImageResource(defaultEmojis.get(resource));
					}
					convertView.setTag(R.id.tag_first, resource);
				}
			} else {

			}
			return convertView;
		}

	}

}
