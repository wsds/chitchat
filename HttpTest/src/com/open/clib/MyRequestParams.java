package com.open.clib;

import java.util.ArrayList;
import java.util.HashMap;

import com.open.lib.MyLog;

public class MyRequestParams {

	private String tag = "MyRequestParams";
	private MyLog log = new MyLog(tag, true);

	public ArrayList<String> keys;
	public HashMap<String, String> keysMap;

	// Put
	public byte bytes[];

	MyRequestParams() {
		keys = new ArrayList<String>();
		keysMap = new HashMap<String, String>();
		bytes = null;
		log.e("initialize");
	}

	public void putParameter(String key, String value) {
		this.keys.add(key);
		this.keysMap.put(key, value);
	}

	public void putParameter(String key, boolean value) {
		this.keys.add(key);
		this.keysMap.put(key, value + "");
	}

	public void putParameter(String key, int value) {
		this.keys.add(key);
		this.keysMap.put(key, value + "");
	}

	public void putParameter(String key, byte bytes[]) {
		this.keys.add(key);
		this.keysMap.put(key, new String(bytes));
	}

	// Put
	public void putBodyEntity(byte bytes[]) {
		this.bytes = bytes;
	}
}
