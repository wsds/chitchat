package com.open.clib;

import java.util.HashMap;

import com.open.lib.MyLog;
import com.open.lib.ResponseHandler;
import com.open.welinks.model.MyFile;



public class MyHttp {

	public static String tag = "MyHttp";
	public MyLog log = new MyLog(tag, true);
	MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();

	int method;
	String url;
	String header;
	HashMap<String, String> params;
	ResponseHandler<String>  responseHandler;
	MyFile myFile;
	int length = 0;
	int start = 0;
	
	void send(){
		myHttpJNI.send(this);
	}
	
}
