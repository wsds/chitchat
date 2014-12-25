package com.open.clib;

import java.util.HashMap;
import java.util.Map.Entry;

import com.open.lib.MyLog;
import com.open.welinks.model.MyFile;

public class MyHttp {

	public static String tag = "MyHttp";
	public MyLog log = new MyLog(tag, true);

	/*
	 * 0 API||1 UPLOAD||2 DOWNLOAD||3 LONGPULL
	 */
	int type = 0;
	MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();

	String id;
	String method;
	String url;
	String IP;
	int port;

	String header;
	HashMap<String, String> urlParams;
	HashMap<String, String> headerParams;
	HashMap<String, String> bodyParams;

	MyResponseHandler responseHandler;
	MyFile myFile;
	int length = 0;
	int start = 0;

	void send() {
		this.splicingRequestHeaders();
		myHttpJNI.send(this);
	}

	public void putUrlParam(String key, String value) {
		if (urlParams == null) {
			urlParams = new HashMap<String, String>();
		}
		urlParams.put(key, value);
	}

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

	public void splicingRequestHeaders() {
		method = "PUT";
		this.header = method + " ";
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
			this.header += temp;
			this.header += " HTTP/1.1\r\nConnection: keep-alive\r\n";
		} else {
			return;
		}

		if (headerParams != null) {
			temp = "";
			for (Entry<String, String> entity : headerParams.entrySet()) {
				String key = entity.getKey();
				String value = entity.getValue();
				temp += key + ":" + value + "\r\n";
			}
			this.header += temp;
			this.header += "\r\n";
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
			this.header += temp;
		}
		log.e(this.header);
	}
}
