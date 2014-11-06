package com.open.chitchat.view;

import com.open.chitchat.R;
import com.open.chitchat.R.color;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.utils.BaseDataUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class ChatFaceHorizontalScrollView extends HorizontalScrollView {
	private ChatFaceHorizontalScrollView thisView;
	private Context context;
	private MyOnClickListener mOnClickListener;
	private View currentView;
	private OnChatFaceHorizontalScrollViewItemClickListener mOnItemClickListener;

	public ChatFaceHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		thisView = this;
		this.context = context;
		initListener();
		addSpaceView();
	}

	private void initListener() {
		mOnClickListener = new MyOnClickListener() {

			@Override
			public void onClickEffective(View view) {
				if (currentView == null) {
					currentView = view;
					currentView.setSelected(true);
				} else {
					currentView.setSelected(false);
					currentView = view;
					currentView.setSelected(true);
				}
				if (mOnItemClickListener != null) {
					mOnItemClickListener.onItemClick((Integer) view.getTag());
				}
			}
		};

	}

	public void addFaceView(int resource) {
		ImageView image = new ImageView(context);
		LayoutParams params = new LayoutParams(BaseDataUtils.dpToPx(40), BaseDataUtils.dpToPx(40));
		image.setImageResource(resource);
		image.setLayoutParams(params);
		image.setBackgroundResource(R.drawable.selector_chat_face_item);
		thisView.addView(image);
		image.setOnClickListener(mOnClickListener);
		image.setTag((thisView.getChildCount() - 1) / 2);
		addSpaceView();
	}

	public void addFaceView(String filePath) {
		ImageView image = new ImageView(context);
		LayoutParams params = new LayoutParams(BaseDataUtils.dpToPx(40), BaseDataUtils.dpToPx(40));
		// TODO
		image.setLayoutParams(params);
		image.setBackgroundResource(R.drawable.selector_chat_face_item);
		thisView.addView(image);
		image.setOnClickListener(mOnClickListener);
		image.setTag((thisView.getChildCount() - 1) / 2);
		addSpaceView();
	}

	public void addFaceView(View view) {
		LayoutParams params = new LayoutParams(BaseDataUtils.dpToPx(40), BaseDataUtils.dpToPx(40));
		view.setLayoutParams(params);
		view.setBackgroundResource(R.drawable.selector_chat_face_item);
		thisView.addView(view);
		view.setOnClickListener(mOnClickListener);
		view.setTag((thisView.getChildCount() - 1) / 2);
		addSpaceView();
	}

	private void addSpaceView() {
		ImageView image = new ImageView(context);
		LayoutParams params = new LayoutParams(BaseDataUtils.dpToPx(1), LayoutParams.MATCH_PARENT);
		image.setBackgroundColor(color.black80);
		image.setLayoutParams(params);
		thisView.addView(image);
	}

	public void setOnItemClickListener(OnChatFaceHorizontalScrollViewItemClickListener mOnItemClickListener) {
		this.mOnItemClickListener = mOnItemClickListener;
	}

	public interface OnChatFaceHorizontalScrollViewItemClickListener {
		public void onItemClick(int position);
	}
}
