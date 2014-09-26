package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RegisterStepSixActivity extends Activity implements OnClickListener {
	public View backView;
	public RelativeLayout rightContainer;
	public TextView titleText, commit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_step_six);

		initView();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		backView = findViewById(R.id.backView);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_register_fourthstep_tvName);
		commit = (TextView) LayoutInflater.from(this).inflate(R.layout.next_step, null);
		commit.setText(R.string.commit);
		rightContainer.addView(commit);

		commit.setOnClickListener(this);
		backView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(commit)) {
			startActivity(new Intent(RegisterStepSixActivity.this, MainActivity.class));
		}

	}

}
