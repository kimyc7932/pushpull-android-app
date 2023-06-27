package com.mm.android.mobilecommon.pps.model;

import androidx.annotation.NonNull;

public class TimeZoneInfo {
    private String areaIndex;
    private String timeZone;
    private String city;
    private String utc;

    public String getAreaIndex() {
        return areaIndex;
    }

    public String getUtc() {
        return utc;
    }

    public void setUtc(String utc) {
        this.utc = utc;
    }

    public void setAreaIndex(String areaIndex) {
        this.areaIndex = areaIndex;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public TimeZoneInfo() {

    }

    public TimeZoneInfo(String areaIndex, String timeZone, String city, String utc) {
        this.areaIndex = areaIndex;
        this.timeZone = timeZone;
        this.city = city;
        this.utc = utc;
    }

    @NonNull
    @Override
    public String toString() {
        return utc +" "+ city;
    }
}