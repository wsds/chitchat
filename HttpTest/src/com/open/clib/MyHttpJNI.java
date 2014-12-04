package com.open.clib;

import java.util.HashMap;

import android.util.Log;

import com.open.welinks.model.MyLinkedListQueue;

public class MyHttpJNI {

	public static MyHttpJNI instance;

	public static MyHttpJNI getInstance() {
		if (instance == null) {
			instance = new MyHttpJNI();
		}
		return instance;
	}

	MyHttpJNI() {
		init();
	}

	public void init() {
		load();
		fileUploadRunnable = new FileUploadRunnable();
		myFileUploadQueue = new MyLinkedListQueue<MyCallBack>();
		myHttpPool = new HashMap<Integer, MyHttp>();
	}

	private void load() {
		try {
			System.loadLibrary("speex");
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	HashMap<Integer, MyHttp> myHttpPool;
	int globalID = 0;

	public void send(MyHttp myHttp) {

		byte url[] = myHttp.url.getBytes();
		String headerStr = "GET /index.html HTTP/1.1\r\nHost: www.example.com";
		byte header[] = headerStr.getBytes();
		String ipStr = "192.168.0.11";
		byte ip[] = ipStr.getBytes();
		nativeSend(ip, url, myHttp.method, header, myHttp.myFile.bytes, myHttp.start, myHttp.length, globalID);

		myHttpPool.put(globalID, myHttp);
		globalID++;
	}

	public class Type {
		public int Queueing = 0, Connecting = 1, Connected = 2, Sending = 3, Sent = 4, Waiting = 4, receiving = 5, received = 6;
		public int Failed = 10;
		public int type = Queueing;
	}

	public Type type = new Type();

	class FileUploadRunnable implements Runnable {

		@Override
		public void run() {
			while (callBackIsRunning) {
				try {
					MyCallBack myCallBack = myFileUploadQueue.takeE();
					if (myCallBack == null) {
						callBackIsRunning = false;
						break;
					} else {
						// success
						Log.e("Http", "type:" + myCallBack.type + "data:" + myCallBack.type + "data:" + myCallBack.type + "id:" + myCallBack.id + "param:" + myCallBack.param);
						if (myCallBack.type == type.Connected) {

						} else if (myCallBack.type == type.Sending) {

						} else if (myCallBack.type == type.receiving) {
							String result = new String(myCallBack.data);
							MyHttp myHttp = myHttpPool.get(myCallBack.id);
							if (myHttp != null && result != null) {
								myHttp.responseHandler.onSuccess(result);
							}
						} else if (myCallBack.type == type.Failed) {

						}
						// Log.e("Http", myCallBack.type + "--" + new String(myCallBack.data));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class MyCallBack {
		public int type;
		public byte data[];
		public int id;
		public float param;
	}

	public MyLinkedListQueue<MyCallBack> myFileUploadQueue;

	FileUploadRunnable fileUploadRunnable;
	boolean callBackIsRunning = false;

	public void callback(int type, byte data[], int id, float param) {
		MyCallBack myCallBack = new MyCallBack();
		myCallBack.type = type;
		myCallBack.data = data;
		myCallBack.id = id;
		myCallBack.param = param;
		myFileUploadQueue.offerE(myCallBack);
		if (!callBackIsRunning) {
			new Thread(fileUploadRunnable).start();
			callBackIsRunning = true;
		}
	}

	public native int nativeSend(byte ip[], byte url[], int method, byte header[], byte body[], int start, int length, int id);

	public native int test(byte message[], MyHttpJNI thiz);

}
