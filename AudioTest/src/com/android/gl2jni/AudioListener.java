package com.android.gl2jni;

public interface AudioListener {
	public void onRecording(int volume);

	public void onPrepared();

	public void onPlayComplete();

	public void onPlayFail();
}
