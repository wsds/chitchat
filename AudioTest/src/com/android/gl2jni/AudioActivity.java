package com.android.gl2jni;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AudioActivity extends Activity implements OnClickListener {
	Button button1, button2, button3, button4;
	Speex speex;
	AudioHandlers audioHandlers = AudioHandlers.getInstance();
	String fileName;
	String tag = "Speex";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_activity);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);
		button4.setOnClickListener(this);
		speex = new Speex();
		audioHandlers.setAudioListener(new AudioListener() {

			@Override
			public void onRecording(int volume) {
				Log.e(tag, volume + "");

			}

			@Override
			public void onPrepared() {
				Log.e(tag, "onPrepared");
				// audioHandlers.startPlay(fileName);
			}

			@Override
			public void onPlayFail() {
				Log.e(tag, "onPlayFail");

			}

			@Override
			public void onPlayComplete() {
				Log.e(tag, "onPlayComplete");
			}
		});
	}

	@Override
	public void onClick(View view) {
		if (view == button1) {
			Log.e(tag, speex.getFrameSize() + "");
		} else if (view == button2) {
			audioHandlers.startRecording();
		} else if (view == button3) {
			fileName = audioHandlers.stopRecording();
			Log.e(tag, fileName);
		} else if (view == button4) {
			if (!"".equals(fileName)) {
				audioHandlers.startPlay(fileName);
				// audioHandlers.preparePlay(fileName);
			} else {
				Log.e(tag, "RecordFail");
			}
		}

	}
}
