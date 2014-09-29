package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ResetPasswordStepTwoActivity extends Activity implements OnClickListener {
	public View backView, next;
	public RelativeLayout rightContainer;
	public TextView titleText, tvTips, tvTime, tvResend;
	public Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reset_password_step_two);
		initView();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		tvTips = (TextView) findViewById(R.id.tvTips);
		tvTime = (TextView) findViewById(R.id.tvTime);
		tvResend = (TextView) findViewById(R.id.tvResend);
		backView = findViewById(R.id.backView);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_resetpasswordthirdstep_resty);
		next = LayoutInflater.from(this).inflate(R.layout.next_step, null);

		rightContainer.addView(next);

		String tip = "包含验证码的短信已经发送至";

		SpannableStringBuilder style = new SpannableStringBuilder(tip);
		style.setSpan(new ForegroundColorSpan(Color.parseColor("#ff44ddcb")), 2, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		tvTips.setText(style);
		next.setOnClickListener(this);
		backView.setOnClickListener(this);
		tvResend.setOnClickListener(this);

		handler = new Handler();
		countDown();
	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(next)) {
			startActivity(new Intent(ResetPasswordStepTwoActivity.this, ResetPasswordStepThreeActivity.class));
		} else if (view.equals(tvResend)) {
			countDown();
		}

	}

	public void countDown() {
		tvTime.setVisibility(View.VISIBLE);
		tvResend.setVisibility(View.GONE);
		new Thread() {
			@Override
			public void run() {
				for (int i = 60; i > 0; i--) {
					try {
						sleep(1000);
						final int time = i;
						handler.post(new Runnable() {

							@Override
							public void run() {
								tvTime.setText("接收短信大约需要" + time + "秒");
							}
						});
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				handler.post(new Runnable() {

					@Override
					public void run() {
						tvTime.setText("");
						tvTime.setVisibility(View.GONE);
						tvResend.setVisibility(View.VISIBLE);
					}
				});
			}
		}.start();

	}
}
