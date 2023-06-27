package com.pushpull.camapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.example.usermodule.net.IDeviceOperationCallback;
import com.example.usermodule.net.IUserDataCallBack;
import com.example.usermodule.net.UserNetManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mm.android.deviceaddmodule.LCDeviceEngine;
import com.mm.android.mobilecommon.base.BaseActivity;
import com.mm.android.mobilecommon.common.CommonParam;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.dialog.PPSConfirmDialog;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.pps.utils.PhoneUtil;
import com.mm.android.mobilecommon.route.RoutePathManager;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.LogUtil;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.mm.android.deviceaddmodule.contract.ModuleConst;
import com.pushpull.camapp.R;
import com.pushpull.camapp.firebase.FCMInstanceIDService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {
    public static final String TAG = BaseActivity.class.getSimpleName();
    private PPSConfirmDialog ppsConfirmDialog;
    private static final String OVERSEAS_APP_ID_KEY = "OVERSEAS_APP_ID";
    private static final String OVERSEAS_APP_SECRET_KEY = "OVERSEAS_APP_SECRET";
    private static final String OVERSEAS_URL = "OVERSEAS_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeLcaleConfigulation(false); // true-> english, others ~~~
        setContentView(R.layout.activity_main);

        //version & version code
        ACONST.APP_VERCODE=getAppVersion();
        ACONST.APP_VERNAME=getAppVerName();

        versionCheck();
    }


    private void versionCheck() {
        DialogUtils.show(this);
        String uid = ACONST.LOGIN_UID;
        ppsConfirmDialog = new PPSConfirmDialog(MainActivity.this, getString(R.string.lc_app_update), getString(R.string.lc_app_update_confirm));
        UserNetManager.getInstance().getNewestVersion(uid, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                DialogUtils.dismiss();
                if(response == null || response.equals("")) return;

                String updateYn = "N";
                String newVersion = response;
                if(response.contains(ACONST.VERSION_CHECK_SEPERATE)) {
                    String[] arr =  response.split("\\"+ACONST.VERSION_CHECK_SEPERATE);
                    newVersion = arr[0];
                    updateYn = arr[1];
                }

                if(updateYn != null && updateYn.equals("Y")) {
                    if(!ACONST.APP_VERNAME.equals(newVersion)) {
                        ppsConfirmDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            @Override
                            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                                if(keyCode == KeyEvent.KEYCODE_BACK) {
                                    return true;
                                }
                                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                                    return false;
                                }
                                return false;
                            }
                        });
                        ppsConfirmDialog.setOnOkClickListener(new PPSConfirmDialog.OnOkClickListener() {
                            @Override
                            public void OnOK(String title, Map<String, Object> param) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id="+getPackageName()));
                                startActivity(intent);

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                moveTaskToBack(true);
                                finishAndRemoveTask();
                                System.exit(0);
                            }
                        });
                        ppsConfirmDialog.setOnCancelClickListener(new PPSConfirmDialog.OnCancelClickListener() {
                            @Override
                            public void OnCancel(String title, Map<String, Object> param) {
                                gotoNext();
                            }
                        });
                        ppsConfirmDialog.show();
                    } else {
                        gotoNext();
                    }
                } else {
                    gotoNext();

                }
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();
            }
        });
    }

    private void gotoNext() {
        initView();
        initData();
    }

    private void initView() {
    }

    private void initData() {
        initFCM();
        requestPermission();

        ACONST.COUNTRY_CODE= PhoneUtil.getCountryCode(this);
        Log.d(TAG, "phoneCode - "+ACONST.COUNTRY_CODE);

        final int autologin = PreferencesHelper.getInstance(getApplicationContext()).getInt(ACONST.PREF_AUTOLOGIN);
        if (autologin == 1) {
//            GlobalFunc.showToast(getApplicationContext(), getString(R.string.lc_ksw_connecting_blockchain_server));
            Toast t = Toast.makeText(this, "", Toast.LENGTH_LONG);

            ImageView iv = new ImageView(this);
            Glide.with(this).load(R.drawable.ic_bc_load4)
//                    .override(Resources.getSystem().getDisplayMetrics().widthPixels)
                    .into(iv);
//            iv.setImageResource(R.drawable.ic_bc_load);

//            ViewGroup.LayoutParams params = iv.getLayoutParams();
//            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//            iv.setLayoutParams(params);


            TextView tv = new TextView(this);
            tv.setText(R.string.lc_ksw_connecting_blockchain_server);
            tv.setTextColor(Color.GRAY);
            tv.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.addView(iv);
            layout.addView(tv);

            t.setView(layout);
            t.show();

            loginAuto();
        } else {
            gotoLoginView();
        }

        // 로그레벨 설정
        if(!ACONST.isDevMode()){
            Log.LOGLEVEL = android.util.Log.ERROR;
        }

        // added by lesc0. 2022.06.21
        LCDeviceEngine.newInstance().check_pps_camera=ACONST.ENABLE_PPS_CCTV_REG;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ppsConfirmDialog.isShowing()) {
            ppsConfirmDialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {

    }

    public void requestPermission() {
        boolean isMinSDKM = Build.VERSION.SDK_INT < 23;
        boolean isGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (isMinSDKM || isGranted) {
            return;
        }

        requestRecordAudioPermission();
    }

    private void requestRecordAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    long getAppVersion() {
        long versionCode = 0;
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionCode = pi.getLongVersionCode();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionCode = pi.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return versionCode;
    }

    String getAppVerName() {
        String versionName="";

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    private boolean initAccessToken(String appId, String appSecret) {
        String url=ACONST.AUTH_API_URL;
        PreferencesHelper.getInstance(MainActivity.this).set(ACONST.OVERSEAS_APP_ID_KEY, appId);
        PreferencesHelper.getInstance(MainActivity.this).set(ACONST.OVERSEAS_APP_SECRET_KEY, appSecret);
        PreferencesHelper.getInstance(MainActivity.this).set(ACONST.OVERSEAS_URL, url);

        try {
            CommonParam commonParam = new CommonParam();
            commonParam.setEnvirment(url);
            commonParam.setContext(MainActivity.this.getApplication());
            commonParam.setAppId(appId);
            commonParam.setAppSecret(appSecret);
            TokenHelper.getInstance().init(commonParam);
            return true;
        } catch (Throwable e) {
            //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void loginAuto() {
        final String strLoginEmail=PreferencesHelper.getInstance(getApplicationContext()).getString(ACONST.PREF_EMAIL);
        final String strLoginPw=PreferencesHelper.getInstance(getApplicationContext()).getString(ACONST.PREF_PASSWD);

        DialogUtils.show(this);
        final String reqUrl = ACONST.API_LOGIN;
        String reqBody = "";
        String bcHash = PreferencesHelper.getInstance(getApplicationContext()).getString("bcHash");
        if(bcHash == null || bcHash.equals("")) {
            bcHash = strLoginEmail;
        }
        try {
            JSONObject reqBodyJson = new JSONObject();
            reqBodyJson.put("id", strLoginEmail);
            reqBodyJson.put("pw", strLoginPw);
            reqBodyJson.put("type", 1);
            reqBodyJson.put("rkey", "1");
            reqBodyJson.put("bc_hash", bcHash);
            reqBody = reqBodyJson.toString();

            AsyncJSON.requestJSON(reqUrl, reqBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if(param == null){
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            GlobalFunc.showToast(getApplicationContext(), getString(R.string.network_error));
                            DialogUtils.dismiss();
                            gotoLoginView();
                            return;
                        }
                        Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());

                        int rescode = param.getInt("rescode");
                        if(rescode == 200) {
                            ACONST.LOGIN_EMAIL=strLoginEmail;
                            ACONST.LOGIN_PW=strLoginPw;
                            JSONObject jdata = param.getJSONObject("data");
                            ACONST.LOGIN_UID = jdata.getString("uid");
                            String appId = jdata.getString("appid");
                            String appSecret = jdata.getString("appsecret");

                            // for test
                            if(ACONST.isDevMode()){
                                appId = ACONST.DEV_APP_ID;
                                appSecret = ACONST.DEV_APP_SECRET;
                            }

                            ACONST.userInfo.userId=ACONST.LOGIN_UID;
                            TokenHelper.getInstance().accessToken = jdata.getString("dahuatoken");
                            ACONST.userInfo.userName = jdata.getString("name");
                            ACONST.userInfo.userEmail = jdata.getString("email");
                            ACONST.userInfo.userNotice = jdata.getString("notice");
                            PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PUSH_NOTICE_KEY, (ACONST.userInfo.userNotice.equals("1") ? true : false));

//                            try { getBcHash(); } catch(Exception ex) {} // 블록체인 해쉬 조회

                            String tel = jdata.getString("tel");
                            if(tel != null && !tel.equals("") && tel.startsWith("null-")) {
                                if(ACONST.COUNTRY_CODE == null) {
                                    tel = tel.replace("null-", "");
                                    ACONST.userInfo.userTel = tel;
                                } else {
                                    tel = tel.replace("null", ACONST.COUNTRY_CODE);
                                    ACONST.userInfo.userTel = tel;
                                }
                            }
                            ACONST.userInfo.userTel = tel;

                            if(!initAccessToken(appId, appSecret)){
                                DialogUtils.dismiss();
                                Toast.makeText(MainActivity.this, R.string.login_server_error,Toast.LENGTH_SHORT).show();
                                gotoLoginView();
                                return;
                            }

                            ACONST.AUTH_SERVER_LOGIN=true;

                            //send token
                            FCMInstanceIDService fcmInstance=new FCMInstanceIDService();
                            fcmInstance.sendRegistrationToServer(ACONST.DEVICE_TOKEN);

                            UserNetManager.getInstance().getOpenIdByAccount(ACONST.LOGIN_EMAIL, new IUserDataCallBack() {
                                @Override
                                public void onCallBackOpenId(String str) {
                                    LogUtil.debugLog(TAG,"openid::"+str);
                                    if (str!=null){
                                        TokenHelper.getInstance().openid = str;
                                        UserNetManager.getInstance().subAccountToken(str, new IUserDataCallBack() {
                                            @Override
                                            public void onCallBackOpenId(String str) {
                                                if (str!=null){
                                                    DialogUtils.dismiss();
                                                    TokenHelper.getInstance().setSubAccessToken(str, MainActivity.this.getApplication());
                                                    ARouter.getInstance().build(RoutePathManager.ActivityPath.DeviceListActivityPath).navigation();
                                                    finish();
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                DialogUtils.dismiss();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    DialogUtils.dismiss();
                                    LogUtil.debugLog(TAG,"msg:::"+throwable.getMessage());
                                    String msg = throwable.getMessage();

                                    // tk1002 : 토큰 expired
                                    if(msg.contains("TK1002")) {
                                        doRecallAcessToken();
                                        return;
                                    }
                                    gotoLoginView();
                                }
                            });
                        } else {
                            DialogUtils.dismiss();
                            GlobalFunc.showToast(getApplicationContext(), getString(R.string.login_error));
                            gotoLoginView();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void getBcHash() {
        final String strLoginEmail=PreferencesHelper.getInstance(getApplicationContext()).getString(ACONST.PREF_EMAIL);
        final String reqUrl = ACONST.API_BCHASH;
        String reqBody = "";
        try {
            JSONObject reqBodyJson = new JSONObject();
            reqBodyJson.put("email", strLoginEmail);
            reqBody = reqBodyJson.toString();

            AsyncJSON.requestJSON(reqUrl, reqBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if(param == null){
                            return;
                        }
                        Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());

                        int rescode = param.getInt("rescode");
                        if(rescode == 200) {
                            JSONObject jdata = param.getJSONObject("data");
                            if(jdata.has("bchash")) {
                                PreferencesHelper.getInstance(getApplicationContext()).set("bcHash", jdata.getString("bchash"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doRecallAcessToken() {
        final String reqUrl = ACONST.API_DAHUA_TOKEN_INSERT;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();
        try {
            regBody = reqBodyJson.toString();
            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if (param == null) {
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            DialogUtils.dismiss();
                            return;
                        }
                        loginAuto();
                    } catch(Exception ex) {

                    }
                }
            });
        } catch(Exception ex) {

        }
    }

    void gotoLoginView() {
        finish();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    void changeLcaleConfigulation(boolean istest) {
        ModuleConst.LANG_TYPE = "EN";

        if(istest){
            Locale en = Locale.US;
            Configuration config = new Configuration();
            config.locale = en;
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        } else {
            Locale current = getResources().getConfiguration().locale;
            Log.d(TAG, "Locale = " + current.getCountry());
            String country = current.getCountry();
            if(!country.equals("KR")){
                Locale locale = getResources().getConfiguration().locale;
                Configuration config = new Configuration();
                config.locale = locale;
                getResources().updateConfiguration(config, getResources().getDisplayMetrics());
            } else {
                ModuleConst.LANG_TYPE = "KR";
            }
        }
    }

    void initFCM() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                ACONST.DEVICE_TOKEN = token;

                //저장
                SharedPreferences pref = getSharedPreferences("prefName", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("DEVICE_TOKEN", token);
                editor.commit();
            }
        });


//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                Log.d(TAG, "getInstanceId onComplete ...");
//                if (!task.isSuccessful()) {
//                    Log.w(TAG, "getInstanceId failed", task.getException());
//                    return;
//                }
//
//                // Get new Instance ID token
//                String token = task.getResult().getToken();
//                ACONST.DEVICE_TOKEN = token;
//
//                //저장
//                SharedPreferences pref = getSharedPreferences("prefName", MODE_PRIVATE);
//                SharedPreferences.Editor editor = pref.edit();
//                editor.putString("DEVICE_TOKEN", token);
//                editor.commit();
//            }
//        });
    }
}