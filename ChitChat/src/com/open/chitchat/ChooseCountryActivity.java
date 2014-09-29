package com.open.chitchat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.open.chitchat.adapter.SortAdapter;
import com.open.chitchat.model.SortModel;
import com.open.chitchat.parser.CharacterParser;
import com.open.chitchat.view.SideBarView;
import com.open.chitchat.view.SideBarView.OnTouchingLetterChangedListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseCountryActivity extends Activity implements OnClickListener {
	private ListView sortListView;
	private SideBarView sideBar;
	private TextView dialog, titleText;
	private SortAdapter adapter;

	private View backView;

	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;

	private PinyinComparator pinyinComparator;

	private OnTouchingLetterChangedListener mOnTouchingLetterChangedListener;
	private OnItemClickListener mOnItemClickListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_country);

		initViews();
		initListeners();
		bindEvent();
	}

	private void initViews() {
		characterParser = CharacterParser.getInstance();

		pinyinComparator = new PinyinComparator();

		titleText = (TextView) findViewById(R.id.titleText);
		backView = findViewById(R.id.backView);

		sideBar = (SideBarView) findViewById(R.id.sideBar);
		dialog = (TextView) findViewById(R.id.dialog);
		sortListView = (ListView) findViewById(R.id.sort);
		sideBar.setTextView(dialog);

		SourceDateList = filledData(getResources().getStringArray(R.array.date));

		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);

		titleText.setText(R.string.activity_choosecountry_title);
	}

	private void initListeners() {
		mOnTouchingLetterChangedListener = new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}
			}
		};

		mOnItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getApplication(), ((SortModel) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();
			}
		};

	}

	private void bindEvent() {
		backView.setOnClickListener(this);
		sideBar.setOnTouchingLetterChangedListener(mOnTouchingLetterChangedListener);
		sortListView.setOnItemClickListener(mOnItemClickListener);
	}

	private List<SortModel> filledData(String[] date) {
		List<SortModel> mSortList = new ArrayList<SortModel>();

		for (int i = 0; i < date.length; i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(date[i]);
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();

			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	class PinyinComparator implements Comparator<SortModel> {

		public int compare(SortModel o1, SortModel o2) {
			if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals("#")) {
				return -1;
			} else if (o1.getSortLetters().equals("#") || o2.getSortLetters().equals("@")) {
				return 1;
			} else {
				return o1.getSortLetters().compareTo(o2.getSortLetters());
			}
		}

	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		}

	}
}
