package com.mm.android.mobilecommon.pps.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

	private static final JSONObject EMPTY = new JSONObject();
	private static final String EMPTY_NOTATION = EMPTY.toString();
	private static final byte[] EMPTY_BYTES = EMPTY_NOTATION.getBytes();
	private static final JSONArray EMTPY_ARRAY = new JSONArray();

	private JsonUtils() {
		super();
	}

	public static JSONObject empty() {
		return EMPTY;
	}

	public static String emptyNotation() {
		return EMPTY_NOTATION;
	}

	public static byte[] emptyBytes() {
		return EMPTY_BYTES;
	}

	public static JSONObject jsonAs(byte[] provide) {
		if (null == provide) {
			return empty();
		}
		String string = new String(provide).trim();
		JSONObject jsonObject = empty();
		try {
			jsonObject = new JSONObject(string);
		} catch (Exception e) {
			Log.e("JsonUtils", String.format(
					"Failed bytes to the json-object[%s].", string));// , e);
		}
		return jsonObject;
	}

	public static String getString(JSONObject object, String name) {
		if (null == object) {
			return StringUtils.empty();
		}
		String string;
		try {
			string = object.isNull(name) ? StringUtils.empty() : object
					.getString(name);
			// Log.d("ChannelBinder", "object = "+object);
		} catch (JSONException e) {
			Log.w(JsonUtils.class.getName(), String.format(
					"Failed to get the string of key[%s] in %s, %s", name,
					object, e));
			return StringUtils.empty();
		}
		return string;
	}

	public static int getInt(JSONObject object, String name) {
		if (null == object) {
			return 0;
		}
		int value;
		try {
			value = object.isNull(name) ? 0 : object.getInt(name);
			// Log.d("ChannelBinder", "object = "+object);
		} catch (JSONException e) {
			Log.w(JsonUtils.class.getName(), String.format(
					"Failed to get the string of key[%s] in %s, %s", name,
					object, e));
			return 0;
		}
		return value;
	}

	public static long getLong(JSONObject object, String name) {
		if (null == object) {
			return 0;
		}
		long value;
		try {
			value = object.isNull(name) ? 0 : object.getLong(name);
			// Log.d("ChannelBinder", "object = "+object);
		} catch (JSONException e) {
			Log.w(JsonUtils.class.getName(), String.format(
					"Failed to get the string of key[%s] in %s, %s", name,
					object, e));
			return 0;
		}
		return value;
	}

	public static JSONArray getArray(JSONObject cast, String name) {
		if (null == cast) {
			return emptyArray();
		}
		JSONArray jsonArray;
		try {
			jsonArray = cast.isNull(name) ? emptyArray() : cast
					.getJSONArray(name);
		} catch (JSONException e) {
			Log.w(JsonUtils.class.getName(), String.format(
					"Failed to get the array of key[%s] in %s, %s", name, cast,
					e));
			return emptyArray();
		}
		return jsonArray;
	}

	public static JSONObject getObject(JSONObject cast, String name) {
		if (null == cast) {
			return new JSONObject();
		}
		JSONObject jsonObject;
		try {
			jsonObject = cast.isNull(name) ? new JSONObject() : cast
					.getJSONObject(name);
		} catch (JSONException e) {
			Log.w(JsonUtils.class.getName(), String.format(
					"Failed to get the object of key[%s] in %s, %s", name,
					cast, e));
			return new JSONObject();
		}
		return jsonObject;
	}

	public static JSONObject getObject(JSONArray cast, int index) {
		if (null == cast) {
			return empty();
		}
		JSONObject jsonObject;
		try {
			jsonObject = cast.isNull(index) ? new JSONObject() : cast
					.getJSONObject(index);
		} catch (JSONException e) {
			Log.w(JsonUtils.class.getName(), String.format(
					"Failed to get the object of array - index [%s/%s] %s",
					index, cast.length(), e));
			return new JSONObject();
		}
		return jsonObject;
	}

	public static JSONArray emptyArray() {
		return EMTPY_ARRAY;
	}

	public static JSONObject JOConversion(String jsonObject) {

		JSONObject JO = null;
		try {
			JO = new JSONObject(jsonObject);
		} catch (JSONException e) {
			Log.d("JOConversion", e.toString());

		}
		return JO;

	}

	public static JSONArray JAConversion(String jsonArray) {

		JSONArray Ja = null;
		try {
			Ja = new JSONArray(jsonArray);
		} catch (JSONException e) {
			Log.d("JAConversion", e.toString());

		}
		return Ja;

	}

	public static boolean isEmpty(JSONArray data) {
		return null == data || data.length() < 1;
	}

	public static boolean isEmpty(JSONObject jsonObject) {
		return null == jsonObject || 0 == jsonObject.length();
	}

	/**
	 * <p>
	 * remove JSON Data key name "talk"
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	public static JSONObject removeTalkData(JSONObject obj) {

		int DATA_CNT = 0;

		try {
			DATA_CNT = obj.has("DATA_CNT") ? obj.getInt("DATA_CNT") : 0;

			for (int i = 0; i < DATA_CNT; i++) {
				if (obj.getJSONObject("DATA_" + i).getJSONObject("DATA")
						.has("talk"))
					obj.getJSONObject("DATA_" + i).getJSONObject("DATA")
							.remove("talk");
			}
		} catch (Exception e) {
			Log.e("Failed to remove talk DATA", e.getStackTrace().toString());
		}

		return obj;
	}
}
