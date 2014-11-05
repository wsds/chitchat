package com.open.chitchat.utils;

import com.open.chitchat.model.Constant;

public class DistanceUtils {
	public static double getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
		if (lat_a == 0d || lng_a == 0d || lat_b == 0d || lng_b == 0d) {
			return 0d;
		} else {
			double radLat1 = (lat_a * Math.PI / 180.0);
			double radLat2 = (lat_b * Math.PI / 180.0);
			double a = radLat1 - radLat2;
			double b = (lng_a - lng_b) * Math.PI / 180.0;
			double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
			s = s * Constant.EARTH_RADIUS;
			s = Math.round(s * 10000) / 10000;
			return s;
		}
	}

	public static String getDistance(int distance) {
		if (distance < 1000) {
			return distance + "m";
		} else {
			return Math.round(distance / 1000 / 1.0) + "km";
		}
	}

	public static String getDistance(String distance) {
		String disString = "";
		try {
			if (Integer.valueOf(distance) < 1000) {
				disString = distance + "m";
			} else {
				disString = Math.round(Integer.valueOf(distance) / 1000 / 1.0) + "km";
			}
		} catch (NumberFormatException e) {
			disString = "火星";
		}
		return disString;

	}

	public static String getDistance(String longitude, String latitude, String longitude2, String latitude2) {
		String distance = getLongDistance(checkDoubleNumber(longitude), checkDoubleNumber(latitude), checkDoubleNumber(longitude2), checkDoubleNumber(latitude2)) + "";
		if (distance.indexOf(".") != -1) {
			if (distance.substring(distance.indexOf(".") + 1).length() > 2) {
				distance = distance.substring(0, distance.indexOf(".") + 3);
			}
		}
		return distance;
	}

	public static double getLongDistance(double lon1, double lat1, double lon2, double lat2) {
		if (lon1 == 0d || lon2 == 0d || lat1 == 0d || lat2 == 0d) {
			return 0d;
		} else {
			double ew1, ns1, ew2, ns2;
			double distance;
			ew1 = lon1 * Constant.DEF_PI180;
			ns1 = lat1 * Constant.DEF_PI180;
			ew2 = lon2 * Constant.DEF_PI180;
			ns2 = lat2 * Constant.DEF_PI180;
			distance = Math.sin(ns1) * Math.sin(ns2) + Math.cos(ns1) * Math.cos(ns2) * Math.cos(ew1 - ew2);
			if (distance > 1.0)
				distance = 1.0;
			else if (distance < -1.0)
				distance = -1.0;
			distance = Constant.DEF_R * Math.acos(distance);
			return distance / 1000;
		}
	}

	private static double checkDoubleNumber(String num) {
		if (num == null || num.equals("")) {
			num = "0";
		}
		return Double.valueOf(num);
	}
}
