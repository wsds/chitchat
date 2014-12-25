package com.open.clib;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.android.gl2jni.R;
import com.open.lib.MyLog;

public class AudioActivity extends Activity implements OnClickListener {

	public String tag = "AudioActivity";
	public MyLog log = new MyLog(tag, true);

	Button button1, button2, button3, button4, button5;
	MyHttp myHttp;

	public static AudioActivity instance;
	MyHttpHandler myHttpHandler = MyHttpHandler.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.audio_activity);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button5 = (Button) findViewById(R.id.button5);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);
		button5.setOnClickListener(this);
		myHttp = new MyHttp();
		// myHttp.myHttpJNI.test(("this is java!").getBytes(),
		// myHttp.myHttpJNI);
		// myHttpHandler.testOpenUpload();
		// myHttpHandler.testOpenSend();
		// myHttpHandler.testUpdateStatus();
	}

	@Override
	public void onClick(View view) {
		if (view == button1) {
			myHttp.myHttpJNI.test(("this is java!").getBytes(), myHttp.myHttpJNI);
			// myHttp.myHttpJNI.send(myHttp);
		} else if (view == button2) {
			// myHttpHandler.initUpload();
			myHttpHandler.testOpenUpload();
		} else if (view == button3) {
			myHttpHandler.test();
		} else if (view == button4) {
		} else if (view == button5) {
			myHttpHandler.testDownload();
		}
	}
}
