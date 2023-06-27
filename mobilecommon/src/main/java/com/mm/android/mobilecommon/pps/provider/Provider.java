package com.mm.android.mobilecommon.pps.provider;

public interface Provider<T> {
	
	T provide() throws Exception;

}
