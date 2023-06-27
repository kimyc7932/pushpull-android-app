package com.mm.android.mobilecommon.pps.service;

public class IDeviceCameraStatus {

    public interface CommonCallback {
        void onSuccess(String response);
        void onError(Throwable throwable);
    }
}
