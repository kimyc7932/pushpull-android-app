package com.mm.android.mobilecommon.pps.manager;

import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.model.TimeZoneInfo;
import com.mm.android.mobilecommon.utils.LogUtil;

import java.util.HashMap;

public class DeviceTimeZoneApiManager {

    private static int TIME_OUT = 10 * 1000;
    private static int DMS_TIME_OUT = 45 * 1000;


    /**
     * getDeviceTime
     *
     * @param deviceId
     * @return
     * @throws BusinessException
     */
    public static String timeZoneQueryByDay(String deviceId) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_TIMEZONE_QUERY_BY_DAY,TIME_OUT);
        return json.toString();
    }

    public static String timeZoneConfigByDay(String deviceId, TimeZoneInfo timeZoneInfo) throws BusinessException {
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", deviceId);
        paramsMap.put("areaIndex", timeZoneInfo.getAreaIndex());
        paramsMap.put("timeZone", timeZoneInfo.getTimeZone());
        LogUtil.debugLog("timeZoneConfigByDay", "timeZoneConfigByDay - paramsMap : " + paramsMap.toString());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_TIMEZONE_CONFIG_BY_DAY,TIME_OUT);
        return json.toString();
    }
}
