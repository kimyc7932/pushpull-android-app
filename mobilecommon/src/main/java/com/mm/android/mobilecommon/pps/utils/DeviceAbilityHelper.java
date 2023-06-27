package com.mm.android.mobilecommon.pps.utils;

import com.mm.android.mobilecommon.pps.model.CameraStatusVO;

public class DeviceAbilityHelper {

    public static CameraStatusVO createCameraStatusVO(String deviceId, String channelId) {
        CameraStatusVO cameraStatusVO = new CameraStatusVO();
        cameraStatusVO.setDeviceId(deviceId);
        cameraStatusVO.setChannelId(channelId);

        return cameraStatusVO;
    }

    public static boolean isHasAbility(String deviceAbility, String channelAbility, String ability1, String ability2) {
        if (deviceAbility == null || channelAbility == null) return false;

        if (deviceAbility.contains(ability1) || deviceAbility.contains(ability2)) {
            return true;
        } else if (channelAbility.contains(ability1) || channelAbility.contains(ability2)) {
            return true;
        }

        return false;

    }

}
