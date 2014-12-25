package com.open.clib;

import java.util.HashMap;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.open.lib.MyLog;
import com.open.welinks.model.MyLinkedListQueue;

public class MyHttpJNI {

	public String tag = "MyHttpJNI:>>>";
	public MyLog log = new MyLog(tag, true);

	public MyHttpHandler myHttpHandler = MyHttpHandler.getInstance();

	public MyLinkedListQueue<MyCallBack> myCallBackQueue;

	public CallBackRunnable callBackRunnable;

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

	@SuppressLint("UseSparseArrays")
	public void init() {
		load();
		openInitialize(this);
		callBackRunnable = new CallBackRunnable();
		myCallBackQueue = new MyLinkedListQueue<MyCallBack>();
		myCallBackQueue.currentRunnable = callBackRunnable;
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
		myHttpPool.put(globalID, myHttp);

		if (myHttp.type == 0) {

		} else if (myHttp.type == 1) {

			byte header[] = myHttp.header.getBytes();
			byte ip[] = myHttp.IP.getBytes();
			byte path[] = myHttp.myFile.uploadPath.getBytes();
			int length = openUpload(ip, myHttp.port, header, path, myHttp.start, myHttp.length, globalID);
			log.e(length + "");
		} else if (myHttp.type == 2) {

		} else if (myHttp.type == 3) {

		}
		globalID++;
	}

	public class Type {
		public int Queueing = 0, Started = 1, Connecting = 2, Connected = 3, Sending = 4, Sent = 5, Waiting = 5, Receiving = 6, Received = 7;
		public int Failed = 10;
		public int type = Queueing;
	}

	public Type type = new Type();

	class CallBackRunnable implements Runnable {

		@Override
		public void run() {
			while (myCallBackQueue.isRunning) {
				try {
					MyCallBack myCallBack = myCallBackQueue.takeE();
					if (myCallBack == null) {
						break;
					}
					log.e("************************Java CallBack**************************");
					// success
					MyHttp myHttp = myHttpPool.get(myCallBack.id);
					if (myHttp == null || myHttp.responseHandler == null) {
						continue;
					}

					if (myCallBack.type == type.Connected) {
						log.e("Connected");
					} else if (myCallBack.type == type.Sending) {
						log.e("Sending");
					} else if (myCallBack.type == type.Sent) {
						log.e("Sent");
					} else if (myCallBack.type == type.Receiving) {
						log.e("Receiving");
					} else if (myCallBack.type == type.Received) {
						myHttp.responseHandler.onSuccess(myCallBack.result, (int) myCallBack.param);
						log.e("Received");
					} else if (myCallBack.type == type.Failed) {
						log.e("Failed");
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.e(e.toString());
				}
			}
		}
	}

	class MyCallBack {
		public int type;
		public byte data[];
		public int id;
		public String result;
		public float param = 0;
	}

	public void callback(int type, byte data[], int id, float param) {
		MyCallBack myCallBack = new MyCallBack();
		myCallBack.type = type;
		myCallBack.data = data;
		myCallBack.result = new String(data);
		myCallBack.id = id;
		myCallBack.param = param;
		myCallBackQueue.offerE(myCallBack);
	}

	public native int nativeSend(byte ip[], int port, byte url[], int method, byte header[], byte body[], int start, int length, int id);

	public native int test(byte message[], MyHttpJNI thiz);

	public native int normalRequest(MyHttpJNI thiz, byte ip[], int port, byte body[], int id);

	public native int openInitialize(MyHttpJNI thiz);

	public native int openDownload(byte ip[], int port, byte body[], byte path[], int id);

	public native int openUpload(byte ip[], int port, byte head[], byte path[], int start, int length, int id);

	public native int openSend(byte ip[], int port, byte body[], int id);

	public native int openLongPull(byte ip[], int port, byte body[], int id);

	public native float updateStates(int id);

}
