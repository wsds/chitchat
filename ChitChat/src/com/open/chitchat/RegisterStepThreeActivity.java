package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RegisterStepThreeActivity extends Activity implements OnClickListener {
	public View backView, next;
	public RelativeLayout rightContainer;
	public TextView titleText;
	public Button passWord;
	public EditText input;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_step_three);

		initView();
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.titleText);
		backView = findViewById(R.id.backView);
		input = (EditText) findViewById(R.id.edtPassword);
		passWord = (Button) findViewById(R.id.btnPassword);
		rightContainer = (RelativeLayout) findViewById(R.id.rightContainer);
		titleText.setText(R.string.activity_register_thirdstep_tvPassword);
		next = LayoutInflater.from(this).inflate(R.layout.next_step, null);

		rightContainer.addView(next);

		passWord.setOnClickListener(this);
		backView.setOnClickListener(this);
		next.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(backView)) {
			finish();
		} else if (view.equals(next)) {
			startActivity(new Intent(RegisterStepThreeActivity.this, RegisterStepFourActivity.class));
		} else if (view.equals(passWord)) {
			if (getString(R.string.activity_register_thirdstep_btnPassword).equals(passWord.getText().toString())) {
				passWord.setText(R.string.activity_register_thirdstep_btnPassword_hidden);
				input.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				input.setSelection(input.getText().length());
			} else {
				passWord.setText(R.string.activity_register_thirdstep_btnPassword);
				input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				input.setSelection(input.getText().length());
			}
		}
	}

}
