package com.mm.android.mobilecommon.pps.manager;

import android.content.Context;

import com.mm.android.mobilecommon.R;
import com.mm.android.mobilecommon.pps.model.TimeZoneInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeZoneManager {
    private Map<String, TimeZoneInfo> timeZoneInfoMap;
    private List<TimeZoneInfo> timeZoneInfoList;
    private Context context;

    public static class Holder {
        private final static TimeZoneManager mInstance = new TimeZoneManager();
    }

    public static TimeZoneManager getInstance(){
        return Holder.mInstance;
    }

    public List<TimeZoneInfo> getTimeZoneInfoList(Context context) {
        if(this.timeZoneInfoList == null || this.timeZoneInfoList.size() == 0) createTimeZoneList(context);
        return this.timeZoneInfoList;
    }

    public TimeZoneManager() {
        this.timeZoneInfoList = new ArrayList<>();
        this.timeZoneInfoMap = new HashMap<>();
    }

    public void createTimeZoneList(Context context) {
        if(context == null) return;
        String[] cityInfo = context.getResources().getStringArray(R.array.city_list);
        if(cityInfo == null || cityInfo.length == 0) return;
        for(String s : cityInfo) {
            String[] spl = s.split("::");
            TimeZoneInfo timeZoneInfo = new TimeZoneInfo(spl[0], spl[1], spl[2], spl[3]);
            this.timeZoneInfoList.add(timeZoneInfo);
            this.timeZoneInfoMap.put(spl[0], timeZoneInfo);
        }
    }

    public TimeZoneInfo getTimeZoneInfo(Context context, final String areaIndex) {
        if(this.timeZoneInfoList == null || this.timeZoneInfoList.size() == 0) createTimeZoneList(context);
        return this.timeZoneInfoMap.get(areaIndex);
    }
}