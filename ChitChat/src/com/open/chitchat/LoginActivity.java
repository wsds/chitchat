package com.open.chitchat;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.chitchat.model.API;
import com.open.chitchat.model.ActivityManager;
import com.open.chitchat.model.Data;
import com.open.chitchat.model.ResponseHandlers;
import com.open.chitchat.utils.SHA1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements OnClickListener {

	public ActivityManager activityManager = ActivityManager.getInstance();

	public View backView, login, selectCountry;
	public TextView titleText, forget;
	public EditText edtUserName, edtPassword;

	private SHA1 sha1 = new SHA1();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		activityManager.mLoginActivity = this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityManager.mLoginActivity = null;
	}

	private void initView() {
		backView = findViewById(R.id.backView);
		login = findViewById(R.id.btnLogin);
		titleText = (TextView) findViewById(R.id.titleText);
		forget = (TextView) findViewById(R.id.tvForgetPW);
		edtUserName = (EditText) findViewById(R.id.edtUserName);
		edtPassword = (EditText) findViewById(R.id.edtPassword);
		selectCountry = findViewById(R.id.llSelectCountry);
		titleText.setText(R.string.login);

		forget.setOnClickListener(this);
		login.setOnClickListener(this);
		backView.setOnClickListener(this);
		selectCountry.setOnClickListener(this);
	}

	private void loginUsePassWord() {
		String userName = edtUserName.getText().toString().trim();
		String userPassword = edtPassword.getText().toString().trim();
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("phone", userName);
		params.addBodyParameter("password", sha1.getDigestOfString(userPassword.getBytes()));
		ResponseHandlers responseHandlers = ResponseHandlers.getInstance();
		httpUtils.send(HttpMethod.POST, API.ACCOUNT_AUTH, params, responseHandlers.account_auth);
	}

	public void loginUsePassWordSuccess() {
		startActivity(new Intent(LoginActivity.this, MainActivity.class));
		finish();
	}

	public void loginUsePassWordFail(String 失败原因) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		if (view.equals(forget)) {
			startActivity(new Intent(LoginActivity.this, ResetPasswordStepOneActivity.class));
		} else if (view.equals(login)) {
			loginUsePassWord();
		} else if (view.equals(backView)) {
			finish();
		} else if (view.equals(selectCountry)) {
			startActivityForResult(new Intent(LoginActivity.this, ChooseCountryActivity.class), 0);
		}

	}

}
