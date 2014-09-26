package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RegisterStepOneActivity extends Activity implements OnClickListener {

	public View backView, next;
	public RelativeLayout rightContainer;
	public TextView titleText, tvTips;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_step_one);
		initView();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		tvTips = (TextView) findViewById(R.id.tvTips);
		backView = findViewById(R.id.backView);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_registeruser_header_title);
		next = LayoutInflater.from(this).inflate(R.layout.next_step, null);
		rightContainer.addView(next);

		SpannableStringBuilder style = new SpannableStringBuilder(this.getString(R.string.activity_registerfirststep_info));
		style.setSpan(new UnderlineSpan(), 9, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		tvTips.setText(style);

		next.setOnClickListener(this);
		backView.setOnClickListener(this);
		tvTips.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(next)) {
			startActivity(new Intent(RegisterStepOneActivity.this, RegisterStepTwoActivity.class));
		} else if (view.equals(tvTips)) {
			Intent intent = new Intent(RegisterStepOneActivity.this, ExplainActivity.class);
			intent.putExtra("type", "disclaimer");
			startActivity(intent);
		}

	}

}
