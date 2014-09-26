package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RegisterStepFiveActivity extends Activity implements OnClickListener {

	public View backView, next;
	public RelativeLayout rightContainer;
	public TextView titleText, tvBoy, tvGirl;

	public Button btnBoy, btnGirl;
	public DatePicker birth;

	public boolean isboy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_step_five);

		initView();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		backView = findViewById(R.id.backView);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_register_fourthstep_tvName);
		next = LayoutInflater.from(this).inflate(R.layout.next_step, null);

		rightContainer.addView(next);

		btnBoy = (Button) findViewById(R.id.btnBoy);
		btnGirl = (Button) findViewById(R.id.btnGirl);
		tvBoy = (TextView) findViewById(R.id.tvBoy);
		tvGirl = (TextView) findViewById(R.id.tvGirl);

		backView.setOnClickListener(this);
		next.setOnClickListener(this);
		btnBoy.setOnClickListener(this);
		btnGirl.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(next)) {
			startActivity(new Intent(RegisterStepFiveActivity.this, RegisterStepSixActivity.class));
		} else if (view.equals(btnBoy)) {
			btnBoy.setSelected(true);
			tvBoy.setTextColor(Color.parseColor("#ff282828"));
			btnGirl.setSelected(false);
			tvGirl.setTextColor(Color.parseColor("#ffb3b3b3"));
		} else if (view.equals(btnGirl)) {
			btnGirl.setSelected(true);
			tvGirl.setTextColor(Color.parseColor("#ff282828"));
			btnBoy.setSelected(false);
			tvBoy.setTextColor(Color.parseColor("#ffb3b3b3"));
		}
	}

}
