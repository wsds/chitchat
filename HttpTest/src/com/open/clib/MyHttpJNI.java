package com.open.clib;

import java.util.HashMap;

import android.annotation.SuppressLint;

import com.open.lib.MyLog;
import com.open.welinks.model.MyLinkedListQueue;

public class MyHttpJNI {

	public String tag = "MyHttpJNI:>>>";
	public MyLog log = new MyLog(tag, true);

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
		fileUploadRunnable = new FileUploadRunnable();
		myFileUploadQueue = new MyLinkedListQueue<MyCallBack>();
		myFileUploadQueue.currentRunnable = fileUploadRunnable;
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
		String url2 = "http://192.000";
		byte url[] = url2.getBytes();
		String headerStr = "GET /index.html HTTP/1.1\r\nHost: www.example.com";
		byte header[] = headerStr.getBytes();
		String ipStr = "192.168.1.7";
		byte ip[] = ipStr.getBytes();
		nativeSend(ip, 80, url, myHttp.method, header, myHttp.myFile.bytes, myHttp.start, myHttp.length, globalID);

		myHttpPool.put(globalID, myHttp);
		globalID++;
	}

	public class Type {
		public int Queueing = 0, Started = 1, Connecting = 2, Connected = 3, Sending = 4, Sent = 5, Waiting = 5, Receiving = 6, Received = 7;
		public int Failed = 10;
		public int type = Queueing;
	}

	public Type type = new Type();

	MyHttpHandler myHttpHandler = MyHttpHandler.getInstance();

	class FileUploadRunnable implements Runnable {

		@Override
		public void run() {
			while (myFileUploadQueue.isRunning) {
				try {
					MyCallBack myCallBack = myFileUploadQueue.takeE();
					if (myCallBack == null) {
						break;
					} else {
						// success
						log.e("type:" + myCallBack.type + "data:" + myCallBack.type + "data:" + myCallBack.type + "id:" + myCallBack.id + "param:" + myCallBack.param);
						if (myCallBack.type == type.Connected) {
							log.e("Connected");
						} else if (myCallBack.type == type.Sending) {
							log.e("Sending");
						} else if (myCallBack.type == type.Sent) {
							log.e("Sent");
						} else if (myCallBack.type == type.Receiving) {
							log.e("Receiving");
							// String result = new String(myCallBack.data);
							// MyHttp myHttp = myHttpPool.get(myCallBack.id);
							// if (myHttp != null && result != null) {
							// myHttp.responseHandler.onSuccess(result);
							// }
						} else if (myCallBack.type == type.Received) {
							log.e("Received");
							if (myHttpHandler.status.state == myHttpHandler.status.None) {
								myHttpHandler.status.state = myHttpHandler.status.Uploading;
								log.e("None");
								String result = new String(myCallBack.data);
								log.e(result);
								myHttpHandler.initiateMultipartUploadResult = myHttpHandler.parseXml(result);
								myHttpHandler.startUpload();
							} else if (myHttpHandler.status.state == myHttpHandler.status.Uploading) {
								myHttpHandler.status.state = myHttpHandler.status.UploadComplete;
								log.e("Uploading");
								String eTag = new String(myCallBack.eTag);
								log.e("ETag:>>>>>>>>>>>>>" + eTag);
								eTag = eTag.substring(3);
								eTag = eTag.substring(0, eTag.length() - 2);
								myHttpHandler.addPart(1, eTag);

							} else if (myHttpHandler.status.state == myHttpHandler.status.UploadComplete) {
								log.e("UploadComplete");
								String result = new String(myCallBack.data);
								log.e("上传成功:>>>>>>" + result);
							}
							// 824D0C8DB3FE4F258006503DE8E2411B
						} else if (myCallBack.type == type.Failed) {
							log.e("Failed");
						}
						// Log.e("Http", myCallBack.type + "--" + new String(myCallBack.data));
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
		public byte eTag[];
		public int id;
		public float param;
	}

	public MyLinkedListQueue<MyCallBack> myFileUploadQueue;

	FileUploadRunnable fileUploadRunnable;

	public void callback(int type, byte data[], byte eTag[], int id, float param) {
		MyCallBack myCallBack = new MyCallBack();
		myCallBack.type = type;
		myCallBack.data = data;
		myCallBack.eTag = eTag;
		myCallBack.id = id;
		myCallBack.param = param;
		myFileUploadQueue.offerE(myCallBack);
	}

	public native int nativeSend(byte ip[], int port, byte url[], int method, byte header[], byte body[], int start, int length, int id);

	public native int test(byte message[], MyHttpJNI thiz);

	public native int normalRequest(MyHttpJNI thiz, byte ip[], int port, byte body[], int id);

}
