package com.opensdk.devicedetail.net;

import com.google.gson.JsonObject;
import com.mm.android.deviceaddmodule.openapi.DeviceAddOpenApiManager;
import com.mm.android.deviceaddmodule.openapi.data.AddDevicePolicyData;
import com.mm.android.deviceaddmodule.openapi.data.PolicyData;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.opensdk.devicedetail.callback.IGetDeviceInfoCallBack;

import java.util.HashMap;

public class DeviceShareService {
    private static int TIME_OUT = 10 * 1000;
    public static final int pageSize=10;
    private IGetDeviceInfoCallBack.ISDCardCallback formatListener;

    public static class Holder{
        private final static DeviceShareService mInstance = new DeviceShareService();
    }

    public static DeviceShareService getInstance(){
        return Holder.mInstance;
    }

    public JsonObject listSubAccountDevice(int pageNo, int pageSize, String openid)throws BusinessException {
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().accessToken);
        paramsMap.put("pageNo", pageNo);
        paramsMap.put("pageSize",pageSize);
        paramsMap.put("openid",openid);
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.LIST_SUB_ACCOUNT_DEVICE,TIME_OUT);
        return  json;
    }

    public boolean addUserPolicyDevice(String openId, String deviceId, String permission, int timeout) throws BusinessException{
        AddDevicePolicyData req = new AddDevicePolicyData();
        req.params.openid = openId;
        req.params.token = TokenHelper.getInstance().accessToken;;

        PolicyData.StateMent stateMent = new PolicyData.StateMent();
        stateMent.permission= permission;
        StringBuffer paramStr = new StringBuffer();
        paramStr.append("dev:").append(deviceId);
        stateMent.resource.add(paramStr.toString());
        req.params.policy.statement.add(stateMent);
        boolean result = DeviceAddOpenApiManager.addPolicy(req);
        return result;
    }

    public boolean clearUserPolicy(String openId) throws BusinessException{
        AddDevicePolicyData  req = new AddDevicePolicyData();
        req.params.openid = openId;
        req.params.token = TokenHelper.getInstance().accessToken;;

        boolean result = DeviceAddOpenApiManager.clearPolicy(req);
        return result;
    }

    public boolean deleteUserPermission(String openId, String deviceId, String channelId, int timeout) throws BusinessException{
        StringBuffer paramStr = new StringBuffer();
        boolean result = DeviceAddOpenApiManager.deleteUserPermission(openId, deviceId, channelId);
        return result;
    }

    public boolean deleteDevicePermission(String openId, String deviceId) throws BusinessException{
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().accessToken);
        paramsMap.put("openid",openId);
        paramsMap.put("deviceId",deviceId);
        boolean result = DeviceAddOpenApiManager.deleteDevicePermission(openId, deviceId);
        return result;
    }

    public String createSubAccount(String email) throws BusinessException {
        String result = DeviceAddOpenApiManager.createSubAccountToken(email);
        return result;
    }
}
