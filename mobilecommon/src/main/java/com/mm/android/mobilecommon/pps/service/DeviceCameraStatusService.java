package com.mm.android.mobilecommon.pps.service;

import android.os.Handler;
import android.os.Message;

import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.AppConsume.BusinessRunnable;
import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.businesstip.BusinessErrorTip;
import com.mm.android.mobilecommon.businesstip.HandleMessageCode;
import com.mm.android.mobilecommon.pps.manager.DeviceCameraStatusOpenApiManager;
import com.mm.android.mobilecommon.pps.model.CameraStatusVO;
import com.mm.android.mobilecommon.pps.utils.Log;

public class DeviceCameraStatusService {
    public static final int pageSize=10;

    public static class Holder{
        private final static DeviceCameraStatusService mInstance = new DeviceCameraStatusService();
    }

    public static DeviceCameraStatusService getInstance(){
        return Holder.mInstance;
    }

    /** modifyFrameReverseStatus **/
    public void modifyFrameReverseStatus(final CameraStatusVO cameraStatusVO, final IDeviceCameraStatus.CommonCallback callBack){
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
                    modifyFrameReverseStatus(handler, cameraStatusVO);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void modifyFrameReverseStatus(Handler handler, CameraStatusVO cameraStatusVO)throws BusinessException{
        String result = DeviceCameraStatusOpenApiManager.modifyFrameReverseStatus(cameraStatusVO);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }


    /** frameReverseStatus **/
    public void frameReverseStatus(final CameraStatusVO cameraStatusVO, final IDeviceCameraStatus.CommonCallback callBack){
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
                    frameReverseStatus(handler, cameraStatusVO);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void frameReverseStatus(Handler handler, CameraStatusVO cameraStatusVO)throws BusinessException{
        String result = DeviceCameraStatusOpenApiManager.frameReverseStatus(cameraStatusVO);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }



    /** setDeviceCameraStatus **/
    public void setDeviceCameraStatus(final CameraStatusVO cameraStatusVO, final IDeviceCameraStatus.CommonCallback callBack){
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
                    setDeviceCameraStatus(handler, cameraStatusVO);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void setDeviceCameraStatus(Handler handler, CameraStatusVO cameraStatusVO)throws BusinessException{
        String result = DeviceCameraStatusOpenApiManager.setDeviceCameraStatus(cameraStatusVO);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

    /** getDeviceCameraStatus **/
    public void getDeviceCameraStatus(final CameraStatusVO cameraStatusVO, final IDeviceCameraStatus.CommonCallback callBack){
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
                    getDeviceCameraStatus(handler, cameraStatusVO);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getDeviceCameraStatus(Handler handler, CameraStatusVO cameraStatusVO)throws BusinessException{
        String result = DeviceCameraStatusOpenApiManager.getDeviceCameraStatus(cameraStatusVO);
        Log.d("DeviceCameraStatusService", "getDeviceCameraStatus.response.result:::" + result);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

}
