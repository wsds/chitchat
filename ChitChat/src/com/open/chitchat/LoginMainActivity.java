package com.open.chitchat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.open.chitchat.view.PageControlView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class LoginMainActivity extends Activity implements OnClickListener, OnPageChangeListener {
	private ViewPager loginPager;
	private Button register, login;
	private PageControlView pageControlView;
	private LoginPagerAdapter loginPagerAdapter;
	private List<View> images;
	public ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loginmain);
		initView();
		initData();
	}

	private void initData() {
		images = new ArrayList<View>();
		List<Integer> imagesId = new ArrayList<Integer>();
		imagesId.add(R.drawable.turn_new_1);
		imagesId.add(R.drawable.turn_new_2);
		imagesId.add(R.drawable.turn_new_3);
		for (int i = 0; i < 3; i++) {
			ImageView view = new ImageView(LoginMainActivity.this);
			view.setBackgroundColor(Color.parseColor("#44ddc9"));
			view.setImageBitmap(BitmapFactory.decodeResource(getResources(), imagesId.get(i)));
			images.add(view);
		}

		this.loginPagerAdapter = new LoginPagerAdapter();
		this.loginPager.setAdapter(this.loginPagerAdapter);
		pageControlView.setCount(images.size(), 1);
		this.loginPager.setOnPageChangeListener(this);
		this.register.setOnClickListener(this);
		this.login.setOnClickListener(this);
	}

	private void initView() {
		this.loginPager = (ViewPager) findViewById(R.id.loginPager);
		this.pageControlView = (PageControlView) findViewById(R.id.pageControlView);
		this.register = (Button) findViewById(R.id.register);
		this.login = (Button) findViewById(R.id.login);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(this.register)) {
			startActivity(new Intent(LoginMainActivity.this, RegisterStepOneActivity.class));
		} else if (view.equals(this.login)) {
			startActivity(new Intent(LoginMainActivity.this, LoginActivity.class));
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		pageControlView.setSeleteItem(position);
	}

	public class LoginPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return images.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(images.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(images.get(position));
			return images.get(position);
		}

	}

}
