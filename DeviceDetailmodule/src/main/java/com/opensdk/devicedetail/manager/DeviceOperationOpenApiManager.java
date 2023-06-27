package com.opensdk.devicedetail.manager;

import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.utils.LogUtil;

import java.util.HashMap;

public class DeviceOperationOpenApiManager {

    private static int TIME_OUT = 10 * 1000;
    private static int DMS_TIME_OUT = 45 * 1000;


    /**
     * getDeviceTime
     *
     * @param deviceId
     * @return
     * @throws BusinessException
     */
    public static String getDeviceTime(String deviceId) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_GET_DEVICE_TIME,TIME_OUT);
        return json.toString();
    }

    /**
     * getDeviceTime
     *
     * @param deviceId
     * @return
     * @throws BusinessException
     */
    public static String calibrationDeviceTime(String deviceId) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        LogUtil.debugLog("calibrationDeviceTime", "calibrationDeviceTime - paramsMap : " + paramsMap.toString());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_CALIBRATION_DEVICE_TIME,TIME_OUT);
        return json.toString();
    }

    public static String deviceSdcardStatus(String deviceId) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        LogUtil.debugLog("deviceSdcardStatus", "deviceSdcardStatus - paramsMap : " + paramsMap.toString());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_DEVICE_SD_CARD_STATUS,TIME_OUT);
        return json.toString();
    }
    public static String storageFormatCheck(String deviceId) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        LogUtil.debugLog("storageFormatCheck", "storageFormatCheck - paramsMap : " + paramsMap.toString());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_DEVICE_SD_CARD_STATUS,TIME_OUT);
        return json.toString();
    }

    // sd카드 포맷 기능 추가 - swkim
    public static String formatStorage(String deviceId) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        LogUtil.debugLog("deviceSdcardStatus", "deviceSdcardStatus - paramsMap : " + paramsMap.toString());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_RECOVER_SD_CARD,TIME_OUT);
        return json.toString();
    }

    public static String deviceStorage(String deviceId) throws BusinessException {
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        LogUtil.debugLog("deviceStorage", "deviceStorage - paramsMap : " + paramsMap.toString());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_DEVICE_STORAGE,TIME_OUT);
        return json.toString();
    }
}
