package com.mm.android.mobilecommon.pps.provider;

public class RemoteProviderException extends Exception {

	/**
     * 
     */
    private static final long serialVersionUID = 2029296076971285155L;

	public RemoteProviderException(String detailMessage) {
		super(detailMessage);
	}

	public RemoteProviderException(Throwable throwable) {
		super(throwable);
	}

	public RemoteProviderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
