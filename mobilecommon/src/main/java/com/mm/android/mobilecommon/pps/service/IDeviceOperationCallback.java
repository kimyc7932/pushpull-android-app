package com.mm.android.mobilecommon.pps.service;

public interface IDeviceOperationCallback {
    void onSuccess(String response);
    void onError(Throwable throwable);
}
