package com.opensdk.devicedetail.manager;

import android.os.Handler;
import android.os.Message;

import com.google.gson.JsonObject;
import com.mm.android.deviceaddmodule.openapi.DeviceAddOpenApiManager;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.AppConsume.BusinessRunnable;
import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.businesstip.BusinessErrorTip;
import com.mm.android.mobilecommon.businesstip.HandleMessageCode;
import com.mm.android.mobilecommon.entity.deviceadd.DeviceAddInfo;
import com.opensdk.devicedetail.net.DeviceShareService;

/**
 * 设备添加数据请求类,由于设备添加页面流程单一，这里设计为单例
 **/
public class DeviceShareManager {
    private static final int NET_ERROR_DEVICE_ALREADY_INIT = 1017;         // 设备已经初始化
    private volatile static DeviceShareManager deviceShareManager;
    DeviceAddInfo mDeviceAddInfo;
    final int DMS_TIMEOUT = 45 * 1000;
    int TIME_OUT = 10 * 1000;
    boolean loop = true;                 //获取设备信息轮询标志变量
    boolean middleTimeUp = false;      //设定的中间时间时间到，此时间之后如设备还是已在服务注册，且不在线以及仍为P2P类型，走绑定流程
    int LOOP_ONCE_TIME = 3 * 1000;            //轮询间隔时间3S

    DeviceShareService deviceShareService;

    private DeviceShareManager() {
        deviceShareService = new DeviceShareService();
    }



    public static DeviceShareManager newInstance() {
        if (deviceShareManager == null) {
            synchronized (DeviceShareManager.class) {
                if (deviceShareManager == null) {
                    deviceShareManager = new DeviceShareManager();
                }
            }
        }
        return deviceShareManager;
    }

    public void getOpenIdByAccount(final String userName, final IShareUserDataCallBack callBack){
        final LCBusinessHandler handler = new LCBusinessHandler() {
            @Override
            public void handleBusiness(Message msg) {
                if (callBack == null) {
                    return;
                }
                if (msg.what == HandleMessageCode.HMC_SUCCESS) {
                    //成功
                    callBack.onCallBackOpenId((String) msg.obj);
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
                    getOpenIdByAccountMethod(handler, userName);
                } catch (BusinessException e) {
                    throw e;
                }
            }
        };
    }

    private void getOpenIdByAccountMethod(Handler handler,String userName)throws BusinessException{
        String result = DeviceAddOpenApiManager.getUserOpenIdByAccout(userName);
        handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
    }

    public void listSubAccountDevice(final int pageNo, final int pageSize, final String openId, final Handler handler) {
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                JsonObject result = deviceShareService.listSubAccountDevice(pageNo, pageSize, openId);
                if (result != null){
                    handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
                }
            }
        };
    }

    public void addUserPolicy(final String openId, final String sn, final String permission, final Handler handler) {
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                boolean result = deviceShareService.addUserPolicyDevice(openId, sn, permission, DMS_TIMEOUT);
                if (result){
                    handler.obtainMessage(HandleMessageCode.HMC_SUCCESS).sendToTarget();
                }
            }
        };
    }

    public void clearUserPolicy(final String openId, final Handler handler) {
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                boolean result = deviceShareService.clearUserPolicy(openId);
                if (result){
                    handler.obtainMessage(HandleMessageCode.HMC_SUCCESS).sendToTarget();
                }
            }
        };
    }

    public void deleteUserPermission(final String openId, final String sn, final Handler handler) {
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                boolean result = deviceShareService.deleteDevicePermission(openId, sn);
                if (result){
                    handler.obtainMessage(HandleMessageCode.HMC_SUCCESS).sendToTarget();
                }
            }
        };
    }

    public void deleteDevicePermission(final String openId, final String sn, final Handler handler) {
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                boolean result = deviceShareService.deleteDevicePermission(openId, sn);
                if (result){
                    handler.obtainMessage(HandleMessageCode.HMC_SUCCESS).sendToTarget();
                }
            }
        };
    }

    public void createSubAccount(final String email, final Handler handler) {
        new BusinessRunnable(handler) {
            @Override
            public void doBusiness() throws BusinessException {
                String result = deviceShareService.createSubAccount(email);
                handler.obtainMessage(HandleMessageCode.HMC_SUCCESS, result).sendToTarget();
            }
        };
    }
}
