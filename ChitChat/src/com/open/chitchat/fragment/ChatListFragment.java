package com.open.chitchat.fragment;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.open.chitchat.ChatActivity;
import com.open.chitchat.MainActivity;
import com.open.chitchat.R;
import com.open.chitchat.listener.MyOnClickListener;
import com.open.chitchat.model.Data;
import com.open.chitchat.view.PopMenuView;

//import pl.droidsonroids.gif.GifDrawable;
//import pl.droidsonroids.gif.GifImageView;

@SuppressLint("InflateParams")
public class ChatListFragment extends Fragment {
	private Data date = Data.getInstance();

	private MainActivity thisActivity;

	private View mContentView, backView;
	private LayoutInflater mInflater;

	private TextView titleText;
	private RelativeLayout rightContainer;
	private ImageView titleImage;
	private ListView chatList;
	// private GifView gifView;

	public ChatListAdapter mChatListAdapter;

	private PopMenuView mPopupWindowView;
	private PopupWindow mPopupWindow;

	private MyOnClickListener mOnClickListener;
	private OnKeyListener mOnKeyListener;

	// private GifImageView gif;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		thisActivity = (MainActivity) this.getActivity();
		mContentView = mInflater.inflate(R.layout.fragment_chat, null);
		initViews();
		initListeners();
		// gifView = (GifView) mContentView.findViewById(R.id.gif);
		// File file = new File(Environment.getExternalStorageDirectory(),
		// "chitchat/1.gif");
		// gifView.setGifPath(file.getAbsolutePath());
		// gif = (GifImageView) mContentView.findViewById(R.id.gif);
		// try {
		// GifDrawable gifFromResource = new
		// GifDrawable(getActivity().getResources(), R.drawable.gif_test);
		// gif.setImageDrawable(gifFromResource);
		// } catch (NotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		return mContentView;
	}

	private void initListeners() {
		mOnClickListener = new MyOnClickListener() {
			@Override
			public void onClickEffective(View view) {
				if (view.equals(titleImage)) {
					// changePopMenuView(mPopupWindow,
					// titleImage);
					startActivity(new Intent(thisActivity, ChatActivity.class));
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
		titleImage.setOnClickListener(mOnClickListener);
		mPopupWindowView.setOnKeyListener(mOnKeyListener);
	}

	private void initViews() {
		backView = mContentView.findViewById(R.id.backView);
		titleText = (TextView) mContentView.findViewById(R.id.titleText);
		rightContainer = (RelativeLayout) mContentView.findViewById(R.id.rightContainer);
		chatList = (ListView) mContentView.findViewById(R.id.chatList);

		titleImage = new ImageView(thisActivity);
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);

		backView.setVisibility(View.INVISIBLE);
		titleText.setText(R.string.app_name);

		mPopupWindowView = new PopMenuView(thisActivity);
		mPopupWindow = new PopupWindow(mPopupWindowView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

		mChatListAdapter = new ChatListAdapter();
		chatList.setAdapter(mChatListAdapter);
	}

	private class ChatListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 0;
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
		public View getView(int position, View view, ViewGroup parent) {
			ChatListHolder holder;
			if (view == null) {
				holder = new ChatListHolder();
				view = mInflater.inflate(R.layout.chat_message_item, null);
				holder.userHeadView = (ImageView) view.findViewById(R.id.userHeadView);
				holder.nickname = (TextView) view.findViewById(R.id.nickname);
				holder.time = (TextView) view.findViewById(R.id.time);
				holder.groupIcon = (TextView) view.findViewById(R.id.groupIcon);
				holder.lastchatcontent = (TextView) view.findViewById(R.id.lastchatcontent);
				holder.notread = (TextView) view.findViewById(R.id.notread);
				view.setTag(holder);
			} else {
				holder = (ChatListHolder) view.getTag();
			}

			return view;
		}

		class ChatListHolder {
			public ImageView userHeadView;
			public TextView nickname, time, groupIcon, lastchatcontent, notread;
		}

	}

	public void changePopMenuView() {
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
			mPopupWindow.showAsDropDown(titleImage);
		}
	}

}
