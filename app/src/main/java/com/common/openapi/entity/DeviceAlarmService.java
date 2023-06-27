package com.common.openapi.entity;

import android.os.Handler;
import android.os.Message;

import com.common.openapi.DeviceAlarmOpenApiManager;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.AppConsume.BusinessRunnable;
import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.businesstip.BusinessErrorTip;
import com.mm.android.mobilecommon.businesstip.HandleMessageCode;
import com.mm.android.mobilecommon.pps.model.AlarmVO;
import com.mm.android.mobilecommon.pps.service.IDeviceOperationCallback;

public class DeviceAlarmService {
    public static final int pageSize=10;

    public static class Holder{
        private final static DeviceAlarmService mInstance = new DeviceAlarmService();
    }

    public static DeviceAlarmService getInstance(){
        return Holder.mInstance;
    }

    public void getAlarmMessage(final AlarmVO alarmVO, final IDeviceOperationCallback callBack){
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
                    getAlarmMessage(handler, alarmVO);
//                    System.out.println("getDeviceTime - getDeviceTime - deviceId : " + deviceId);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getAlarmMessage(Handler handler, AlarmVO alarmVO)throws BusinessException{
        String result = DeviceAlarmOpenApiManager.getAlarmMessage(alarmVO);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

}
