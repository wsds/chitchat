package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RegisterStepTwoActivity extends Activity implements OnClickListener {

	public View backView, next;
	public RelativeLayout rightContainer;
	public TextView titleText;
	public Button verifyCode;
	public Handler handler;
	public boolean sended = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_step_two);
		handler = new Handler();
		initView();
		obtainSms();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		backView = findViewById(R.id.backView);
		verifyCode = (Button) findViewById(R.id.btnVerifyCodeSms);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_resetpasswordsecondstep_fillVerifyCode);
		next = LayoutInflater.from(this).inflate(R.layout.next_step, null);
		rightContainer.addView(next);

		backView.setOnClickListener(this);
		next.setOnClickListener(this);
		verifyCode.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(next)) {
			startActivity(new Intent(RegisterStepTwoActivity.this, RegisterStepThreeActivity.class));
		} else if (view.equals(verifyCode)) {
			obtainSms();
		}
	}

	public void obtainSms() {
		if (!sended) {
			sended = true;
			verifyCode.setTextColor(Color.parseColor("#ffcdcdcd"));
			new Thread() {
				@Override
				public void run() {
					for (int i = 60; i >= 0; i--) {
						try {
							sleep(1000);
							final int time = i;
							handler.post(new Runnable() {

								@Override
								public void run() {
									verifyCode.setText("重新获取短信(" + time + ")");
								}
							});
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.post(new Runnable() {

						@Override
						public void run() {
							sended = false;
							verifyCode.setText("重新获取短信");
							verifyCode.setTextColor(Color.parseColor("#ff3f4345"));
						}
					});
				}
			}.start();
		}
	}
}
