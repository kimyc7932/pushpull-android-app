package com.example.usermodule.api;

public interface IAccountUserDataCallBack {
    void onSuccess(String response);
    /**
     * 错误回调
     *
     * @param throwable
     */
    void onError(Throwable throwable);
}
