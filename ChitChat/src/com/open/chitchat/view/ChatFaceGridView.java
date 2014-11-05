package com.open.chitchat.view;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

import com.open.chitchat.R;
import com.open.chitchat.model.FileHandlers;
import com.open.chitchat.utils.BaseDataUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

public class ChatFaceGridView extends GridView implements android.widget.AdapterView.OnItemClickListener, android.widget.AdapterView.OnItemLongClickListener, android.view.View.OnTouchListener {
	private FileHandlers fileHandlers = FileHandlers.getInstance();

	private Context context;
	private ChatFaceGridView thisView;
	private FaceViewPager parent;
	private View currentView, popView;
	private int row, line, viewHeight, viewWidth, lastposition = -1;
	private float touchX, touchY;
	private PopupWindow mPopupWindow;
	private GifImageView imageView;
	private boolean isLongClick;
	private String type;

	public ChatFaceGridView(Context context, FaceViewPager parent, String type) {
		super(context);
		this.context = context;
		this.parent = parent;
		this.type = type;
		this.thisView = this;
		if (type.equals("default")) {
			this.row = 7;
			this.line = 3;
		} else {
			this.row = 4;
			this.line = 2;
		}
		onCreate();
	}

	@SuppressLint("ClickableViewAccessibility")
	private void onCreate() {
		thisView.setCacheColorHint(getResources().getColor(R.color.transparent));
		thisView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		thisView.setOnItemClickListener(this);
		thisView.setOnItemLongClickListener(this);
		thisView.setOnTouchListener(this);

		popView = LayoutInflater.from(context).inflate(R.layout.chat_face_pop, null);
		imageView = (GifImageView) popView.findViewById(R.id.gifImage);
		mPopupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		System.out.println("onItemClick::::::::::::::" + id + "," + position);

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (this.parent != null) {
			this.parent.setTouchAble(false);
		}
		if (!"default".equals(type)) {
			isLongClick = true;
			currentView = view;
			judgePosition();
		}
		return true;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		touchX = event.getX();
		touchY = event.getY();
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (viewHeight == 0) {
				viewHeight = thisView.getHeight() / line;
				viewWidth = thisView.getWidth() / row;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			}
			if (parent != null) {
				parent.setTouchAble(true);
			}
			if (currentView != null) {
				currentView.setSelected(false);
			}
			lastposition = -1;
			isLongClick = false;
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (isLongClick) {
				judgePosition();
			}
		}
		return false;
	}

	private void judgePosition() {
		int position = 0;
		if (touchY >= 0 && touchY < viewHeight) {
			position = 0;
		} else if (touchY >= viewHeight && touchY < viewHeight * 2) {
			position = 1;
		} else if (touchY >= viewHeight * 2 && touchY < viewHeight * 3) {
			position = 2;
		}
		if (touchX >= 0 && touchX < viewWidth) {
			position *= row;
		} else if (touchX >= viewWidth && touchX < viewWidth * 2) {
			position = position * row + 1;
		} else if (touchX >= viewWidth * 2 && touchX < viewWidth * 3) {
			position = position * row + 2;
		} else if (touchX >= viewWidth * 3 && touchX < viewWidth * 4) {
			position = position * row + 3;
		} else if (touchX >= viewWidth * 4 && touchX < viewWidth * 5) {
			position = position * row + 4;
		} else if (touchX >= viewWidth * 5 && touchX < viewWidth * 6) {
			position = position * row + 5;
		} else if (touchX >= viewWidth * 6 && touchX < viewWidth * 7) {
			position = position * row + 6;
		}
		if (currentView != null && position != lastposition && position < thisView.getChildCount()) {
			if (mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			}
			lastposition = position;
			currentView.setSelected(false);
			currentView = this.getChildAt(position);
			currentView.setSelected(true);
			File gifFile = new File(fileHandlers.sdcardFolder, "1.gif");
			GifDrawable gifFromFile = null;
			try {
				gifFromFile = new GifDrawable(gifFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			imageView.setImageDrawable(gifFromFile);
			mPopupWindow.showAsDropDown(currentView, -viewWidth / 2, -(viewHeight + BaseDataUtils.dpToPx(140)));
		}
	}
}
