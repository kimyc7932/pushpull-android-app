package com.mm.android.mobilecommon.pps.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StringUtils {

	private static final String EMPTY = "";

	private StringUtils() {
	}

	public static String empty() {
		return EMPTY;
	}

	public static String nullToEmpty(String string) {
		return null == string || "null".equalsIgnoreCase(string) ? EMPTY : string;
	}

	public static String nullToEmptyInt(String string) {
		return null == string || "null".equalsIgnoreCase(string) ? "0" : string;
	}

	public static String trim(String id) {
		return nullToEmpty(id).trim();
	}

	public static boolean isEmpty(String string) {
		return null == string || empty().equals(string);
	}

	// 자에 쉼표(콤마;Comma)넣기, 천(1000) 단위 구분
	public static String decimalFormat(double num) {
		DecimalFormat df = new DecimalFormat("#,##0.00");
		return df.format(num) + "";
	}

	public static int calcAgeOf(String yyyymmdd) {
		if (yyyymmdd.length() < 8) {
			return 0;
		}
		int y = Integer.parseInt(yyyymmdd.substring(0, 4));
		int m = Integer.parseInt(yyyymmdd.substring(4, 6));
		int d = Integer.parseInt(yyyymmdd.substring(6));

		Calendar now = Calendar.getInstance();
		Calendar dob = Calendar.getInstance();
		dob.set(y, m, d);
		if (dob.after(now)) {
			throw new IllegalArgumentException("Can't be born in the future");
		}
		int year1 = now.get(Calendar.YEAR);
		int year2 = dob.get(Calendar.YEAR);
		int age = year1 - year2;
		int month1 = now.get(Calendar.MONTH);
		int month2 = dob.get(Calendar.MONTH);
		if (month2 > month1) {
			age--;
		}
		else if (month1 == month2) {
			int day1 = now.get(Calendar.DAY_OF_MONTH);
			int day2 = dob.get(Calendar.DAY_OF_MONTH);
			if (day2 > day1) {
				age--;
			}
		}

		return age;
	}

	public static String byteCalculation(String bytes) {
		String retFormat = "0";
		Double size = Double.parseDouble(bytes);

		String[] s = { "bytes", "KB", "MB", "GB", "TB", "PB" };

		if (bytes != "0") {
			int idx = (int) Math.floor(Math.log(size) / Math.log(1024));
			DecimalFormat df = new DecimalFormat("#,###.##");
			double ret = ((size / Math.pow(1024, Math.floor(idx))));
			retFormat = df.format(ret) + " " + s[idx];
		} else {
			retFormat += " " + s[0];
		}

		return retFormat;
	}

	public static String nowFormatted(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Calendar c1 = Calendar.getInstance();
		return sdf.format(c1.getTime());
	}

	public static String abbreviate(String source, int byteSizeLimit, String abbreviationSymbol) {
		if (source == null)
			return "";
//		System.out.println("Original byte length = " + source.getBytes().length);
		StringBuilder result = new StringBuilder();
		if (source.getBytes().length > byteSizeLimit) {
			int byteSize = 0;
			for (int i = 0; i < source.length(); i++) {
				char c = source.charAt(i);
				int size = getByteSize(String.valueOf(c));
				if (byteSize + size > byteSizeLimit) {
					break;
				}
				byteSize += size;
				result.append(c);
			}
			result.append(abbreviationSymbol);
		}
		else {
			return source;
		}
		return result.toString();
	}

	public static int getByteSize(String c) {
		if (c == null || c.length() == 0)
			return 0;
		return c.getBytes().length;
	}

	/**
	 * <p>tving talk contents abbreviate method</p>
	 * @param array
	 * @param maxlength
	 * @param abbreviateSymbol
	 * @return
	 */
	public static JSONArray abbreviateTvingTalk(JSONArray array, int maxlength, String abbreviateSymbol) {

		try {
			if (array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject _obj = (JSONObject) array.get(i);
					String _contents = StringUtils.abbreviate(_obj.getString("CONTENTS"), maxlength, abbreviateSymbol);
					_obj.put("CONTENTS", _contents);
					array.put(i, _obj);
					_obj = null;
				}
			}
		}
		catch (Exception e) {
			e.getStackTrace();
		}

		return array;
	}

}
