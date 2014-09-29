package com.open.chitchat;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExplainActivity extends Activity implements OnClickListener {
	public View backView;
	public TextView titleText, content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_explain);
		backView = findViewById(R.id.backView);
		titleText = (TextView) findViewById(R.id.titleText);
		content = (TextView) findViewById(R.id.content);

		backView.setOnClickListener(this);

		String type = getIntent().getStringExtra("type");
		if ("disclaimer".equals(type)) {
			titleText.setText("用户协议");
		} else if ("about".equals(type)) {
			titleText.setText("关于“微型公社”");
		}
		getFromAssets(type + ".txt");
	}

	public void getFromAssets(String fileName) {
		try {
			InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
			BufferedReader bufReader = new BufferedReader(inputReader);
			String line = "";
			String Result = "";
			while ((line = bufReader.readLine()) != null)
				Result += line + "\n";
			content.setText(Result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		}
	}
}
