package com.mm.android.mobilecommon.pps.utils;

public class Log {
	
	public static int LOGLEVEL = android.util.Log.VERBOSE -1;

	public static void v(String tag, String message) {
		if (LOGLEVEL <= android.util.Log.VERBOSE) {
			android.util.Log.v(tag, message);
		}
	}

	public static void v(String tag, String message, Throwable t) {
		if (LOGLEVEL <= android.util.Log.VERBOSE) {
			android.util.Log.v(tag, message, t);
		}
	}

	public static void d(String tag, String message) {
		if (LOGLEVEL <= android.util.Log.DEBUG) {
			android.util.Log.d(tag, message);
		}
	}

	public static void d(String tag, String message, Throwable t) {
		if (LOGLEVEL <= android.util.Log.DEBUG) {
			android.util.Log.d(tag, message, t);
		}
	}

	public static void i(String tag, String message) {
		if (LOGLEVEL <= android.util.Log.INFO) {
			android.util.Log.i(tag, message);
		}
	}

	public static void i(String tag, String message, Throwable t) {
		if (LOGLEVEL <= android.util.Log.INFO) {
			android.util.Log.i(tag, message, t);
		}
	}

	public static void w(String tag, String message) {
		if (LOGLEVEL <= android.util.Log.WARN) {
			android.util.Log.w(tag, message);
		}
	}

	public static void w(String tag, String message, Throwable t) {
		if (LOGLEVEL <= android.util.Log.WARN) {
			android.util.Log.w(tag, message, t);
		}
	}

	public static void w(String tag, Throwable t) {
		if (LOGLEVEL <= android.util.Log.WARN) {
			android.util.Log.w(tag, t);
		}
	}

	public static void e(String tag, String message) {
		if (LOGLEVEL <= android.util.Log.ERROR) {
			android.util.Log.e(tag, message);
		}
	}

	public static void e(String tag, String message, Throwable t) {
		if (LOGLEVEL <= android.util.Log.ERROR) {
			android.util.Log.e(tag, message, t);
		}
	}
}