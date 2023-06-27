package com.example.usermodule.net;

public interface IDeviceOperationCallback {
    void onSuccess(String response);
    void onError(Throwable throwable);
}
