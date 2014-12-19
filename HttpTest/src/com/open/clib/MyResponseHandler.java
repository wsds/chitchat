package com.open.clib;

abstract public class MyResponseHandler {

	public void onStart() {
	};

	public void onLoading(long total, long current, boolean isUploading) {
	};

	abstract public void onSuccess(String data,int param);

	public void onFailure(int error, String message) {
	};
}
