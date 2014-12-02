package com.open.clib;

import java.util.HashMap;

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
	}

	private void load() {
		try {
			System.loadLibrary("speex");
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	HashMap<Integer, MyHttp> MyHttpPool = new HashMap<Integer, MyHttp>();
	int globalID = 0;

	public void send(MyHttp myHttp) {

		byte url[] = myHttp.url.getBytes();
		String headerStr = "GET /index.html HTTP/1.1\r\nHost: www.example.com";
		byte header[] = headerStr.getBytes();
		String ipStr="192.168.0.11";
		byte ip[]=ipStr.getBytes();
		nativeSend(ip, url, myHttp.method, header, myHttp.myFile.bytes, myHttp.start, myHttp.length, globalID);

		MyHttpPool.put(globalID, myHttp);
		globalID++;
	}

	public void callback(int type, byte data[], int id) {
		if (type == 1) {
			String result = new String(data);
			MyHttp myHttp = MyHttpPool.get(id);
			if (myHttp != null && result != null) {
				myHttp.responseHandler.onSuccess(result);
			}
		}

	}

	public native int nativeSend(byte ip[], byte url[], int method, byte header[], byte body[], int start, int length, int id);
	public native int test(byte message[]);

}
