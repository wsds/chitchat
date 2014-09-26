package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RegisterStepFourActivity extends Activity implements OnClickListener {
	public View backView, next;
	public RelativeLayout rightContainer;
	public TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_step_four);

		initView();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		backView = findViewById(R.id.backView);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_register_fourthstep_tvName);
		next = LayoutInflater.from(this).inflate(R.layout.next_step, null);

		rightContainer.addView(next);

		backView.setOnClickListener(this);
		next.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(next)) {
			startActivity(new Intent(RegisterStepFourActivity.this, RegisterStepFiveActivity.class));
		}

	}

}
