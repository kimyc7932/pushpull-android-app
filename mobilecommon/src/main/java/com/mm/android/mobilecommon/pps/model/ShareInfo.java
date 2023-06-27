package com.mm.android.mobilecommon.pps.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareInfo {
    private int groupId;
    private int rights;
    private String deviceId;
    private String uid;
    private String tel;
    private String email;
    private String openId;
    private String insDt;

    public ShareInfo(JSONObject obj) throws JSONException {
        this.rights = obj.getInt("rights"); // 4: 공유받은자.
        this.groupId = obj.getInt("group_id");
        this.deviceId = obj.getString("device_id");
        this.uid = obj.getString("uid");
        this.tel = obj.getString("tel");
        this.email = obj.getString("email");
        this.openId = obj.getString("openid");
        this.insDt = obj.getString("ins_dt");
        this.insDt = this.insDt.substring(0,16).replace('T',' ');
    }

    public int getRights() { return rights; }
    public void setRights(int rights) { this.rights = rights; }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getInsDt() {
        return insDt;
    }

    public void setInsDt(String insDt) {
        this.insDt = insDt;
    }
}