package com.mm.android.mobilecommon.pps;

import android.content.Context;

import com.mm.android.mobilecommon.pps.entity.LocalRecordsData;
import com.mm.android.mobilecommon.pps.model.DeviceInfo;
import com.mm.android.mobilecommon.pps.model.UserMyInfo;
import com.mm.android.mobilecommon.utils.PreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACONST {
    public static Map<String, List<LocalRecordsData.ResponseData.RecordsBean>> recordMap = new HashMap<>();
    public static final String PUSH_NOTICE_KEY = "BANGTAN_CCTV_PUSH_NOTICE";
    public static final CharSequence VERSION_CHECK_SEPERATE = "^";
    public static String ANDROID_LANG_TYPE;
    public final static String TIME_ZONE="time_zone";
    public final static String UTC_TIME="utc_time";
    public final static String PREF_EMAIL="pref_email";
    public final static String PREF_PASSWD="pref_passwd";
    public final static String PREF_AUTOLOGIN="pref_auto";
    public final static String PREF_SUBACCESSTOKEN="pref_subtoken";

    public static String APP_VERNAME = "";
    public static long APP_VERCODE = 0;
    public static String LOGIN_EMAIL="";
    public static String LOGIN_PW="";
    public static String LOGIN_UID="";

    public static String AUTH_API_URL="https://openapi.easy4ip.com:443";
    public static boolean AUTH_SERVER_LOGIN=false;

    // 회원가입 완료 후 자동 로그인처리 여부
    public static boolean REGISTER_FINISH_AUTO_LOGIN = false; // 회원가입 후 자동로그인 시 디바이스 추가시 오류 발생하여 추가 2022.06.21. swkim

    // deploy mode 설정
    public static int DEPLOY_QA = 0x01;
    public static int DEPLOY_OP = 0x02;
    public static String QA_API_URL="http://pushpull.iptime.org:9080";
//    public static String QA_API_URL="http://192.168.0.115:9081";
//    public static String OP_API_URL="https://bangtan.k-doorlock.com";
    public static String OP_API_URL="http://192.168.0.115:9081";

    public static int DEPLOY_MODE=DEPLOY_OP;

    public static boolean ENABLE_PPS_CCTV_REG=false; //pps 카메라인지 체크유무

    public static final String OVERSEAS_APP_ID_KEY = "OVERSEAS_APP_ID";
    public static final String OVERSEAS_APP_SECRET_KEY = "OVERSEAS_APP_SECRET";
    public static final String OVERSEAS_URL = "OVERSEAS_URL";
    public static final String DEV_APP_ID="lc4a960ae8897e4a74"; //ssang9087@gmail.com
    public static final String DEV_APP_SECRET="2218aead438046fca89c1a3386df74";

    /* pushpull API */
    public static final String API_BCHASH = "/api/v1/users/bchash";
    public static String API_METHOD_NEWEST_VERSION = "/api/v1/config/version/get";
    public static String API_UPDATE_USER_INFO = "/api/v1/users/update_user_info";
    public static String API_LOGIN= GET_API_URL() +"/api/v1/users/login";
    public static String API_JOIN= GET_API_URL() +"/api/v1/users/user_join";
    public static String API_WITHDRAWAL= GET_API_URL() +"/api/v1/users/withdrawal";
    public static String API_DEVICE_ADD = GET_API_URL() +"/api/v1/devices/thing/post/0";
    public static String API_DEVICE_DEL = GET_API_URL() +"/api/v1/devices/thing/delete/0";
    public static String API_DEVICE_GET = GET_API_URL() + "/api/v1/users/thing/get";
    public static String API_CHANGE_PW = GET_API_URL() + "/api/v1/users/change_pw";
    public static String API_TEL_AUTH = GET_API_URL() + "/api/v1/sms/authtel";
    public static String API_TEL_AUTHCHECK = GET_API_URL() + "/api/v1/sms/checkauthtel";
    public static String API_EMAIL_AUTH = GET_API_URL() + "/api/v1/mail/authmail";
    public static String API_EMAIL_AUTHCHECK = GET_API_URL() + "/api/v1/mail/checkauthmail";
    public static String API_RESET_PW = GET_API_URL() + "/api/v1/users/reset_pw";
    public static String API_CHANGE_TEL = GET_API_URL() + "/api/v1/users/change_tel";
    public static String API_FCM_TOKEN_RERESH=GET_API_URL() + "/api/v1/users/update_token";
    public static String API_NOTICE_LIST=GET_API_URL() + "/api/v1/dlog/devicealarm/get";
    public static String API_NOTICE_DELETE=GET_API_URL() + "/api/v1/dlog/devicealarm/delete";
    public static String API_NOTICE_CLEAR=GET_API_URL() + "/api/v1/dlog/devicealarm/clear";
    public static String API_DEVICE_RESET=GET_API_URL() + "/api/v1/users/device_reset";
    public static String API_DEVICE_SHARE=GET_API_URL() + "/api/v1/ppsuser/device_share";
    public static String API_SHARE_LIST=GET_API_URL() + "/api/v1/ppsuser/share_list";
    public static String API_DEVICE_UNSHARE=GET_API_URL() + "/api/v1/ppsuser/device_unshare";
    public static String API_DAHUA_TOKEN_INSERT=GET_API_URL() + "/api/v1/ppsuser/dahua_token_insert";

    // 등록된 device list
    public static ArrayList<DeviceInfo> regDeviceList=null;
    public static UserMyInfo userInfo= new UserMyInfo();
    // device token
    public static String DEVICE_TOKEN="";
    // country code
    public static String COUNTRY_CODE="";


    public static Calendar createCalendar(String dt) {
        String[] spl = dt.split(" ");
        int y = Integer.parseInt(spl[0].split("-")[0]);
        int m = Integer.parseInt(spl[0].split("-")[1]);
        int d = Integer.parseInt(spl[0].split("-")[2]);

        int h = Integer.parseInt(spl[1].split(":")[0]);
        int mi = Integer.parseInt(spl[1].split(":")[1]);
        int s = Integer.parseInt(spl[1].split(":")[2]);

        Calendar c = Calendar.getInstance();
        c.set(y, (m-1), d, h, mi, s);

        return c;
    }

    public static String calculateSecondTime(String dt, int n) {
        Calendar c = createCalendar(dt);
        c.add(Calendar.SECOND, n);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(c.getTime());
    }

    public static String calculateHourTime(String dt, int n) {
        Calendar c = createCalendar(dt);
        c.add(Calendar.HOUR, n);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(c.getTime());
    }

    /*
     * 설정정보 확인
     *  1. QR 카메라 추가 -> "PPSTV" 로 검색
     */
    public static String GET_API_URL() {
        if(DEPLOY_MODE == DEPLOY_OP){
            return OP_API_URL;
        }
        return QA_API_URL;
    }

    public static DeviceInfo getDeviceInfo(String deviceId) {
        if(regDeviceList == null || regDeviceList.size() == 0) return null;
        for(DeviceInfo deviceInfo : regDeviceList) {
            if(deviceInfo.deviceId.equalsIgnoreCase(deviceId)) return deviceInfo;
        }

        return null;
    }

    public static void Logout(Context ctx) {
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_EMAIL,"");
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_PASSWD,"");
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_SUBACCESSTOKEN, "");
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_AUTOLOGIN, 0);

        LOGIN_EMAIL="";
        LOGIN_UID="";
    }

    public static void Withdrawal(Context ctx) {
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_EMAIL,"");
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_PASSWD,"");
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_SUBACCESSTOKEN, "");
        PreferencesHelper.getInstance(ctx).set(ACONST.PREF_AUTOLOGIN, 0);

        LOGIN_EMAIL="";
        LOGIN_UID="";
    }

    public static boolean isDevMode() {
        if(DEPLOY_MODE == DEPLOY_QA){
            return true;
        }
        return false;
    }
}