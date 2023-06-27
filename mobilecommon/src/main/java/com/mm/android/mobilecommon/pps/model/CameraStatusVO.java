package com.mm.android.mobilecommon.pps.model;

import android.widget.ImageView;

public class CameraStatusVO {

    private String deviceId;
    private String channelId;
    private String enableType;
    private boolean enableValue;
    private String direction;
    private String saveKey;
    private ImageView targetIV;

    public String getSaveKey() {
        return saveKey;
    }

    public void setSaveKey(String saveKey) {
        this.saveKey = saveKey;
    }

    public ImageView getTargetIV() {
        return targetIV;
    }

    public void setTargetIV(ImageView targetIV) {
        this.targetIV = targetIV;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getEnableType() {
        return enableType;
    }

    public void setEnableType(String enableType) {
        this.enableType = enableType;
    }

    public boolean isEnableValue() {
        return enableValue;
    }

    public void setEnableValue(boolean enableValue) {
        this.enableValue = enableValue;
    }

    public String getDirection() {
        return this.direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }
}
