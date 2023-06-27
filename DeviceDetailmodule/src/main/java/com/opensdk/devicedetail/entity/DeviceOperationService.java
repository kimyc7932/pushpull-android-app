package com.opensdk.devicedetail.entity;

import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.AppConsume.BusinessRunnable;
import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.businesstip.BusinessErrorTip;
import com.mm.android.mobilecommon.businesstip.HandleMessageCode;
import com.mm.android.mobilecommon.pps.service.IDeviceOperationCallback;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.opensdk.devicedetail.callback.IGetDeviceInfoCallBack;
import com.opensdk.devicedetail.manager.DeviceOperationOpenApiManager;

public class DeviceOperationService implements IGetDeviceInfoCallBack.ISDCardCallback {
    public static final int pageSize=10;
    private IGetDeviceInfoCallBack.ISDCardCallback formatListener;

    @Override
    public void formatStatus(String response) {
        formatListener.formatStatus(response);
    }

    public static class Holder{
        private final static DeviceOperationService mInstance = new DeviceOperationService();
    }

    public static DeviceOperationService getInstance(){
        return Holder.mInstance;
    }

    public void setFormatListener(IGetDeviceInfoCallBack.ISDCardCallback formatListener) {
       this.formatListener = formatListener;
    }

    public void getDeviceTime(final String openid, final String deviceId, final IDeviceOperationCallback callBack){
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
                    getDeviceTime(handler, openid, deviceId);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getDeviceTime(Handler handler, String openid, String deviceId)throws BusinessException{
        String result = DeviceOperationOpenApiManager.getDeviceTime(deviceId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    public void deviceStorage(final String deviceId, final IDeviceOperationCallback callBack){
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
                    deviceStorage(handler, deviceId);
                    System.out.println("deviceStorage - deviceStorage - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void deviceStorage(Handler handler, String deviceId)throws BusinessException{
        String result = DeviceOperationOpenApiManager.deviceStorage(deviceId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    public void deviceSdcardStatus(final String openid, final String deviceId, final IDeviceOperationCallback callBack){
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
                    deviceSdcardStatus(handler, openid, deviceId);
//                    System.out.println("deviceSdcardStatus - deviceSdcardStatus - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void deviceSdcardStatus(Handler handler, String openid, String deviceId)throws BusinessException{
        String result = DeviceOperationOpenApiManager.deviceSdcardStatus(deviceId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    public void storageFormatCheck(final String deviceId, final IDeviceOperationCallback callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
//                System.out.println("storageFormatCheck - msg.what ::: " + msg.what);
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    JsonObject resultJson = new Gson().fromJson((String)msg.obj, JsonObject.class);
                    Log.d("storageFormatCheck", "00000000000000000 sdcard.storageFormatCheck.resultJson : " + resultJson);
                    //formatStatus(resultJson.get("status").getAsString());
//                    System.out.println("storageFormatCheck - msg.what is success ==> call onSuccess!!!!!!! " + msg.obj);
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
                    storageFormatCheck(handler, deviceId);
//                    System.out.println("storageFormatCheck - storageFormatCheck - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void storageFormatCheck(Handler handler, String deviceId)throws BusinessException{
        String result = DeviceOperationOpenApiManager.storageFormatCheck(deviceId);
//        System.out.println("storageFormatCheck.result - storageFormatCheck.result - ++: " + result);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

    public void formatStorage(final String deviceId, final IDeviceOperationCallback callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }

//                System.out.println("formatStorage - msg.what ::: " + msg.what);
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
//                    System.out.println("formatStorage - msg.what is success ==> call onSuccess!!!!!!! " + msg.obj);
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
                    String result = DeviceOperationOpenApiManager.formatStorage(deviceId);
                    handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
//                    System.out.println("formatStorage - formatStorage - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

//    private void formatStorage(Handler handler, String openid, String deviceId)throws BusinessException{
//        String result = DeviceOperationOpenApiManager.formatStorage(deviceId);
//        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
//    }


    public void calibrationDeviceTime(final String deviceId, final IDeviceOperationCallback callBack){
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
                    calibrationDeviceTime(handler, deviceId);
//                    System.out.println("calibrationDeviceTime - calibrationDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void calibrationDeviceTime(Handler handler, String deviceId)throws BusinessException{
        String result = DeviceOperationOpenApiManager.calibrationDeviceTime(deviceId);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }
}
