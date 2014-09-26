package com.open.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class LoginActivity extends Activity implements OnClickListener {

	public View backView, login;
	public TextView titleText, forget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initView();
	}

	private void initView() {
		backView = findViewById(R.id.backView);
		login = findViewById(R.id.btnLogin);
		titleText = (TextView) findViewById(R.id.titleText);
		forget = (TextView) findViewById(R.id.tvForgetPW);

		titleText.setText(R.string.login);

		forget.setOnClickListener(this);
		login.setOnClickListener(this);
		backView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.equals(forget)) {
			startActivity(new Intent(LoginActivity.this, ResetPasswordStepOneActivity.class));
		} else if (view.equals(login)) {
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
		} else if (view.equals(backView)) {
			finish();
		}

	}
}
