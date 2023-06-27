package com.mm.android.mobilecommon.pps.service;

import android.os.Handler;
import android.os.Message;

import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.AppConsume.BusinessRunnable;
import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.businesstip.BusinessErrorTip;
import com.mm.android.mobilecommon.businesstip.HandleMessageCode;
import com.mm.android.mobilecommon.pps.manager.DeviceTimeZoneApiManager;
import com.mm.android.mobilecommon.pps.model.TimeZoneInfo;

public class DeviceTimezoneService {
    public static final int pageSize=10;

    public static class Holder{
        private final static DeviceTimezoneService mInstance = new DeviceTimezoneService();
    }

    public static DeviceTimezoneService getInstance(){
        return Holder.mInstance;
    }

    public void getTimezone(final String openid, final String deviceId, final IDeviceOperationCallback callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }

                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onSuccess((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };

        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    getTimezone(handler, openid, deviceId);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getTimezone(Handler handler, String openid, String deviceId)throws BusinessException{
        String result = DeviceTimeZoneApiManager.timeZoneQueryByDay(deviceId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    public void setTimezone(final String deviceId, final TimeZoneInfo timeZoneInfo, final IDeviceOperationCallback callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }

                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onSuccess((String) msg.obj);
                } else {
                    //失败
                    callBack.onError(BusinessErrorTip.throwError(msg));
                }
            }
        };

        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                try {
                    setTimezone(handler, deviceId, timeZoneInfo);
//                    System.out.println("deviceSdcardStatus - deviceSdcardStatus - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void setTimezone(Handler handler, String deviceId, TimeZoneInfo timeZoneInfo)throws BusinessException{
        String result = DeviceTimeZoneApiManager.timeZoneConfigByDay(deviceId, timeZoneInfo);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

}
