package com.common.openapi;

import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.model.AlarmVO;

import java.util.HashMap;

public class DeviceAlarmOpenApiManager {

    private static int TIME_OUT = 10 * 1000;
    private static int DMS_TIME_OUT = 45 * 1000;


    /**
     * getDeviceTime
     *
     * @param alarmVO
     * @return
     * @throws BusinessException
     */
    public static String getAlarmMessage(AlarmVO alarmVO) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", alarmVO.getDeviceId());
        paramsMap.put("channelId", alarmVO.getChannelId());
        paramsMap.put("beginTime", alarmVO.getBeginTime());
        paramsMap.put("endTime", alarmVO.getEndTime());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_GET_ALARM_MESSAGE,TIME_OUT);
        return json.toString();
    }

}
