package com.mm.android.mobilecommon.pps.model;

import android.os.Bundle;

import com.mm.android.mobilecommon.eventbus.event.BaseEvent;

public class MyEvent extends BaseEvent {
    //EVENT ACTION
    public static String ACTION_DEVICELIST_ACTIVITY_DESTROY="ACTION_DEVICELIST_ACTIVITY_DESTROY";
    public static String ACTION_LOGIN_ACTIVITY_DESTROY="ACTION_LOGIN_ACTIVITY_DESTROY";

    //EVENT KEY
    public interface KEY{
        String TITLE_MODE="title_mode";
    }

    Bundle bundle;
    public MyEvent(String code) {
        super(code);
    }

    public MyEvent(String code, Bundle bundle){
        super(code);
        this.bundle=bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}