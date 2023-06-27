package com.mm.android.mobilecommon.pps.model;

import androidx.annotation.NonNull;

public class DeviceInfo {
    public int groupId;
    public String deviceId;
    public String deviceName;
    public int deviceType;
    public String deviceSerial;
    public String permission;
    public String ability;
    public boolean isShared; // 공유된 기기여부

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    @NonNull
    @Override
    public String toString() {
        return "groupId : " + groupId + ", deviceId : " + deviceId + ", deviceName : " + deviceName + ", deviceSerial : " + deviceSerial + ", permission : " + permission;
    }
}