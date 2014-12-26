package com.open.clib;

import java.util.HashMap;
import java.util.Map.Entry;

import com.open.lib.MyLog;
import com.open.welinks.model.MyFile;

public class MyHttp {

	public static String tag = "MyHttp";
	public MyLog log = new MyLog(tag, true);

	MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();

	/*
	 * 0 API||1 UPLOAD||2 DOWNLOAD||3 LONGPULL
	 */
	int type = 0;

	int id;
	/**
	 * GET POST PUT
	 */
	String method = "PUT";// GET POST PUT
	String url;
	String IP;
	int port;

	String requestHeader;
	HashMap<String, String> urlParams;
	HashMap<String, String> headerParams;
	HashMap<String, String> bodyParams;

	MyResponseHandler responseHandler;
	MyFile myFile;
	int start = 0;
	int length = 0;

	void send() {
		boolean flag = this.splicingRequestHeaders();
		if (flag == true) {
			myHttpJNI.send(this);
		} else {
			log.e("request params incomplete.");
		}
	}

	void send(int type, String IP, int port, String method, String url) {
		this.type = type;
		this.IP = IP;
		this.port = port;
		this.method = method;
		this.url = url;
		send();
	}

	void send(int type, String IP, int port, String method, String url, MyResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
		send(type, IP, port, method, url);
	}

	/**
	 * upload part
	 */
	void send(int type, String IP, int port, String method, String url, int start, int length) {
		this.start = start;
		this.length = length;
		send(type, IP, port, method, url);
	}

	public void putUrlParam(String key, String value) {
		if (urlParams == null) {
			urlParams = new HashMap<String, String>();
		}
		urlParams.put(key, value);
	}

	// public void putUrlParam(String key, int value) {
	// this.putUrlParam(key, value);
	// }

	public void putHeaderParam(String key, String value) {
		if (headerParams == null) {
			headerParams = new HashMap<String, String>();
		}
		headerParams.put(key, value);
	}

	public void putBodyParam(String key, String value) {
		if (bodyParams == null) {
			bodyParams = new HashMap<String, String>();
		}
		bodyParams.put(key, value);
	}

	boolean splicingRequestHeaders() {
		this.requestHeader = method + " ";
		String temp = this.url;
		if (urlParams != null) {
			for (Entry<String, String> entity : urlParams.entrySet()) {
				String key = entity.getKey();
				String value = entity.getValue();
				if (this.url.equals(temp)) {
					temp += "?" + key + "=" + value;
				} else {
					temp += "&" + key + "=" + value;
				}
			}
			this.requestHeader += temp;
			this.requestHeader += " HTTP/1.1\r\nConnection: keep-alive\r\n";
		} else {
			return false;
		}

		if (headerParams != null) {
			temp = "";
			for (Entry<String, String> entity : headerParams.entrySet()) {
				String key = entity.getKey();
				String value = entity.getValue();
				temp += key + ":" + value + "\r\n";
			}
			this.requestHeader += temp;
			this.requestHeader += "\r\n";
		}
		if (bodyParams != null) {
			temp = "";
			for (Entry<String, String> entity : bodyParams.entrySet()) {
				String key = entity.getKey();
				String value = entity.getValue();
				if ("".equals(temp)) {
					temp += key + "=" + value;
				} else {
					temp += "&" + key + "=" + value;
				}
			}
			this.requestHeader += temp;
		}
		log.e(this.requestHeader);
		return true;
	}
}