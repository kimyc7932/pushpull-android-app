package com.mm.android.mobilecommon.pps.manager;

import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.AppConsume.BusinessException;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.model.CameraStatusVO;

import java.util.HashMap;

public class DeviceCameraStatusOpenApiManager {

    private static int TIME_OUT = 10 * 1000;
    private static int DMS_TIME_OUT = 45 * 1000;

    /**
     * getDeviceTime
     *
     * @param cameraStatusVO
     * @return
     * @throws BusinessException
     */
    public static String frameReverseStatus(CameraStatusVO cameraStatusVO) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", cameraStatusVO.getDeviceId());
        paramsMap.put("channelId", cameraStatusVO.getChannelId());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_FRAME_REVERSE_STATUS,TIME_OUT);
        return json.toString();
    }

    /**
     * getDeviceTime
     *
     * @param cameraStatusVO
     * @return
     * @throws BusinessException
     */
    public static String modifyFrameReverseStatus(CameraStatusVO cameraStatusVO) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", cameraStatusVO.getDeviceId());
        paramsMap.put("channelId", cameraStatusVO.getChannelId());
        paramsMap.put("direction", cameraStatusVO.getDirection());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_MODIFY_FRAME_REVERSE_STATUS,TIME_OUT);
        return json.toString();
    }

    /**
     * getDeviceTime
     *
     * @param cameraStatusVO
     * @return
     * @throws BusinessException
     */
    public static String setDeviceCameraStatus(CameraStatusVO cameraStatusVO) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", cameraStatusVO.getDeviceId());
        paramsMap.put("channelId", cameraStatusVO.getChannelId());
        paramsMap.put("enableType", cameraStatusVO.getEnableType());
        paramsMap.put("enable", cameraStatusVO.isEnableValue());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_SET_DEVICE_CAMERA_STATUS,TIME_OUT);
        return json.toString();
    }

    /**
     * getDeviceTime
     *
     * @param cameraStatusVO
     * @return
     * @throws BusinessException
     */
    public static String getDeviceCameraStatus(CameraStatusVO cameraStatusVO) throws BusinessException {
        // 解绑设备
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("token", TokenHelper.getInstance().subAccessToken);
        paramsMap.put("deviceId", cameraStatusVO.getDeviceId());
        paramsMap.put("channelId", cameraStatusVO.getChannelId());
        paramsMap.put("enableType", cameraStatusVO.getEnableType());
        JsonObject json = HttpSend.execute(paramsMap, MethodConst.METHOD_GET_DEVICE_CAMERA_STATUS,TIME_OUT);
        return json.toString();
    }
}
