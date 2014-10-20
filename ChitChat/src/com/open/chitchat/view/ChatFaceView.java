package com.open.chitchat.view;

import com.open.chitchat.R;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

public class ChatFaceView extends FrameLayout {
	private Context context;
	private ViewPager facePager;
	private PageControlView facePagerControl;
	private HorizontalScrollView faceList;

	private String type = "default";

	public ChatFaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		onCreate();
	}

	private void onCreate() {
		LayoutInflater.from(context).inflate(R.layout.chat_face, this);
		facePager = (ViewPager) this.findViewById(R.id.facePager);
		facePagerControl = (PageControlView) this.findViewById(R.id.facePagerControl);
		faceList = (HorizontalScrollView) this.findViewById(R.id.faceList);
	}

	private class ChatFaceAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 0;
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
			return super.instantiateItem(container, position);
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

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
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
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}
