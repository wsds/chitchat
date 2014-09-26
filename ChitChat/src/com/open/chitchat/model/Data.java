package com.open.chitchat.model;

public class Data {

	public static Data data;

	public static Data getInstance() {
		if (data == null) {
			data = new Data();
		}
		return data;
	}

	public BaseData baseData = new BaseData();

	public class BaseData {

		public float screenWidth;
		public float screenHeight;
		public float appHeight;
		public float stateBar;
		public float density;

	}
}