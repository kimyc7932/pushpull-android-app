package com.opensdk.devicedetail.manager;

public interface IShareUserDataCallBack {
    void onCallBackOpenId(String str);
    /**
     * 错误回调
     *
     * @param throwable
     */
    void onError(Throwable throwable);
}
