package com.open.clib;

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.gl2jni.R;

public class AudioActivity extends Activity implements OnClickListener {
	Button button1, button2, button3, button4;
	String tag = "Speex";
	MyHttp myHttp;

	public static AudioActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.audio_activity);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);
		myHttp = new MyHttp();
	}

	@Override
	public void onClick(View view) {
		if (view == button1) {
			myHttp.myHttpJNI.test(("this is java!").getBytes(), myHttp.myHttpJNI);
			// myHttp.myHttpJNI.send(myHttp);
		} else if (view == button2) {
		} else if (view == button3) {
		} else if (view == button4) {
		}
	}
}
