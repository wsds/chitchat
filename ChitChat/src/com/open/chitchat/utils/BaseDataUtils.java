package com.open.chitchat.utils;

import java.lang.reflect.Field;
import com.open.chitchat.model.Data;
import android.content.Context;
import android.app.Activity;
import android.util.DisplayMetrics;

public class BaseDataUtils {
	public static Data data = Data.getInstance();

	public static void initBaseData(Context context) {
		DisplayMetrics metric = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metric);

		data.baseData.screenWidth = metric.widthPixels;
		data.baseData.screenHeight = metric.heightPixels;
		data.baseData.density = metric.density;

		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		data.baseData.stateBar = statusBarHeight;
		data.baseData.appHeight = data.baseData.screenHeight - data.baseData.stateBar;

	}

	public static int dpToPx(int dp) {
		return (int) (dp * data.baseData.density + 0.5f);
	}

	public static int pxToDp(int px) {
		return (int) (px / data.baseData.density + 0.5f);
	}

	public static int dpToPx(float dp) {
		return (int) (dp * data.baseData.density + 0.5f);
	}

	public static int pxToDp(float px) {
		return (int) (px / data.baseData.density + 0.5f);
	}

	/**
	 * @param sex
	 * @return male return true , female return false
	 */
	public static boolean determineSex(String sex) {
		if ("男".equals(sex) || "male".equals(sex)) {
			return true;
		} else {
			return false;
		}

	}

	public static String generateUserName(String nickName, String alias) {
		String showName = "";
		if (alias != null && !"".equals(alias)) {
			showName = alias + "(" + nickName + ")";
		} else {
			showName = nickName;
		}

		return showName;
	}
}
