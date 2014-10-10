package com.open.chitchat.fragment;

import com.open.chitchat.ChatActivity;
import com.open.chitchat.R;
import com.open.chitchat.model.Data;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("InflateParams")
public class ChatListFragment extends Fragment implements OnClickListener {
	private Data date = Data.getInstance();

	private View mContentView, backView;
	private LayoutInflater mInflater;

	private TextView titleText;
	private RelativeLayout rightContainer;
	private ImageView titleImage;
	private ListView chatList;

	public ChatListAdapter mChatListAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mContentView = mInflater.inflate(R.layout.fragment_chat, null);
		initViews();
		return mContentView;
	}

	private void initViews() {
		backView = mContentView.findViewById(R.id.backView);
		titleText = (TextView) mContentView.findViewById(R.id.titleText);
		rightContainer = (RelativeLayout) mContentView.findViewById(R.id.rightContainer);
		chatList = (ListView) mContentView.findViewById(R.id.chatList);

		titleImage = new ImageView(getActivity());
		titleImage.setImageResource(R.drawable.title_image);
		rightContainer.addView(titleImage);
		titleImage.setOnClickListener(this);

		backView.setVisibility(View.INVISIBLE);
		titleText.setText(R.string.chat_title);

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

	@Override
	public void onClick(View view) {
		if (view.equals(titleImage)) {
			startActivity(new Intent(getActivity(), ChatActivity.class));
		}

	}
}
