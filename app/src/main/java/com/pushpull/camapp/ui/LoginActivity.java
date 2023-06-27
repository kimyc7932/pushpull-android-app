package com.pushpull.camapp.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.usermodule.net.IUserDataCallBack;
import com.example.usermodule.net.UserNetManager;
import com.mm.android.mobilecommon.common.CommonParam;
import com.mm.android.mobilecommon.eventbus.event.BaseEvent;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.base.BaseActivity;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.model.MyEvent;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.route.RoutePathManager;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.LogUtil;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.mm.android.mobilecommon.widget.ClearEditText;
import com.pushpull.camapp.R;
import com.pushpull.camapp.firebase.FCMInstanceIDService;

import static com.mm.android.mobilecommon.route.RoutePathManager.ActivityPath.LoginActivityPath;
import static com.mm.android.mobilecommon.utils.WordInputFilter.REGEX_EMAIL_GUO;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

@Route(path =LoginActivityPath )
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    public static final String TAG = BaseActivity.class.getSimpleName();
    ClearEditText mName;
    LinearLayout mLoginBtn;
    TextView mRegesiter;
    ImageView mReturn;
    private String mUserName;
    private int type;

    private TextView m_uiTvPasswdFind;
    private LinearLayout m_uiLayoutLoginView;
    private EditText m_uiEtEmail;
    private EditText m_uiEtPassword;

    private String mUserEmail;
    private String mUserPw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.usermodule.R.layout.login_activity);
        initView();
        initData();
    }


    private boolean initAccessToken(String appId, String appSecret) {
        String appIdKey=appId;
        String appSecretKey=appSecret;
        String url= ACONST.AUTH_API_URL;

        PreferencesHelper.getInstance(LoginActivity.this).set(ACONST.OVERSEAS_APP_ID_KEY, appIdKey);
        PreferencesHelper.getInstance(LoginActivity.this).set(ACONST.OVERSEAS_APP_SECRET_KEY, appSecretKey);
        PreferencesHelper.getInstance(LoginActivity.this).set(ACONST.OVERSEAS_URL, url);

        try {
            CommonParam commonParam = new CommonParam();
            commonParam.setEnvirment(url);
            commonParam.setContext(LoginActivity.this.getApplication());
            commonParam.setAppId(appIdKey);
            commonParam.setAppSecret(appSecretKey);

            TokenHelper.getInstance().init(commonParam);
            return true;
            //startActivity(new Intent(MainActivity.this, LoginActivity.class));

        } catch (Throwable e) {
            //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void initData() {
        requestPermission();
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


    private void initView(){
        mLoginBtn = findViewById(com.example.usermodule.R.id.login_login);
        mRegesiter = findViewById(com.example.usermodule.R.id.user_register);
        mReturn = findViewById(com.example.usermodule.R.id.iv_back);
        m_uiEtEmail = findViewById(com.example.usermodule.R.id.et_email);
        m_uiEtPassword = findViewById(com.example.usermodule.R.id.et_password);
        m_uiTvPasswdFind = findViewById(com.example.usermodule.R.id.user_passwd_find);
        m_uiLayoutLoginView = findViewById(com.example.usermodule.R.id.user_layout);

        mReturn.setOnClickListener(this);
        mLoginBtn.setOnClickListener(this);
        mRegesiter.setOnClickListener(this);
        m_uiTvPasswdFind.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return false;
//        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == com.example.usermodule.R.id.login_login) {
            doLogin();
        } else if (id == com.example.usermodule.R.id.user_register) {
            toRegesiterPage();
        } else if (id == com.example.usermodule.R.id.iv_back) {
            finish();
        } else if (id == com.example.usermodule.R.id.user_passwd_find) {
            Intent intent = new Intent(this, LoginPasswdFindActivity.class);
            startActivity(intent);
        }
    }


    private void doLogin() {
        mUserEmail = m_uiEtEmail.getText().toString().trim();
        if(mUserEmail.isEmpty() || !mUserEmail.matches(REGEX_EMAIL_GUO) ){
            Toast.makeText(LoginActivity.this, com.example.usermodule.R.string.phone_not_match_tip,Toast.LENGTH_SHORT).show();
            return;
        }

        mUserPw = m_uiEtPassword.getText().toString().trim();
        if(mUserPw.isEmpty()){
            Toast.makeText(LoginActivity.this, com.example.usermodule.R.string.password_not_match_tip,Toast.LENGTH_SHORT).show();
            return;
        }

        DialogUtils.show(this);

        final String reqUrl = ACONST.API_LOGIN;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        String bcHash = PreferencesHelper.getInstance(getApplicationContext()).getString("bcHash");
        if(bcHash == null || bcHash.equals("")) {
            bcHash = mUserEmail;
        }
        try {
            reqBodyJson.put("id", mUserEmail);
            reqBodyJson.put("type", 1); // email
            reqBodyJson.put("rkey", "1");
            reqBodyJson.put("bc_hash", bcHash);

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + reqBodyJson.toString());
            reqBodyJson.put("pw", mUserPw);
            regBody = reqBodyJson.toString();

            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if(param == null){
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            GlobalFunc.showToast(getApplicationContext(), getString(com.example.usermodule.R.string.network_error));
                            DialogUtils.dismiss();
                            return;
                        }
                        Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());
                        int rescode = param.getInt("rescode");
                        if(rescode == 200) {
                            JSONObject jdata = param.getJSONObject("data");
                            ACONST.LOGIN_UID = jdata.getString("uid");

                            final String appId = jdata.getString("appid").trim();
                            final String appSecret = jdata.getString("appsecret").trim();

                            // for test
//                            if(ACONST.isDevMode()){
//                                appId = ACONST.DEV_APP_ID;
//                                appSecret = ACONST.DEV_APP_SECRET;
//                            }

                            ACONST.userInfo.userId=ACONST.LOGIN_UID;
                            TokenHelper.getInstance().accessToken = jdata.getString("dahuatoken");
                            ACONST.userInfo.userName = jdata.getString("name");
                            ACONST.userInfo.userEmail = jdata.getString("email");
                            ACONST.userInfo.userTel = jdata.getString("tel");

//                            try { getBcHash(); } catch(Exception ex) {} // 블록체인 해쉬 조회

                            if(!initAccessToken(appId, appSecret)){
                                DialogUtils.dismiss();
                                Toast.makeText(LoginActivity.this, com.example.usermodule.R.string.login_server_error,Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ACONST.AUTH_SERVER_LOGIN=true;

                            //send token
                            FCMInstanceIDService fcmInstance=new FCMInstanceIDService();
                            fcmInstance.sendRegistrationToServer(ACONST.DEVICE_TOKEN);

                            UserNetManager.getInstance().getOpenIdByAccount(mUserEmail, new IUserDataCallBack() {
                                @Override
                                public void onCallBackOpenId(String str) {
                                    DialogUtils.dismiss();
                                    LogUtil.debugLog(TAG, "openid::" + str);
                                    if (str != null) {
                                        TokenHelper.getInstance().openid = str;
                                        getSubAccountToken(str);
                                    } else {
                                        Log.e(TAG, "getOpenIdByAccount error - openid is not null");
                                        Toast.makeText(LoginActivity.this, com.example.usermodule.R.string.login_error, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                    LogUtil.debugLog(TAG, "msg:::" + throwable.getMessage());
                                    DialogUtils.dismiss();
                                    String msg = throwable.getMessage(); // SUB1008, The parameter openid has no binding relationship with the application

                                    // tk1002 : 토큰 expired
                                    if(msg.contains("TK1002")) {
                                        doRecallAcessToken();
                                        return;
                                    }

                                    if (msg.contains("OP1009") || msg.contains("SUB1008")) {
                                        //Toast.makeText(LoginActivity.this, R.string.account_not_has, Toast.LENGTH_SHORT).show();
                                        UserNetManager.getInstance().createAccountToken(mUserEmail, new IUserDataCallBack() {
                                            @Override
                                            public void onCallBackOpenId(String openId) {
                                                LogUtil.debugLog(TAG,"openId"+openId);
                                                if (openId!=null){
                                                    TokenHelper.getInstance().openid =openId;
                                                    getSubAccountToken(openId);
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                String msg = throwable.getMessage();
                                                Log.e(TAG, msg);
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, "getOpenIdByAccount error - " + throwable.getMessage());
                                        Toast.makeText(LoginActivity.this, com.example.usermodule.R.string.login_error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            DialogUtils.dismiss();
                            Toast.makeText(LoginActivity.this, com.example.usermodule.R.string.login_error, Toast.LENGTH_SHORT).show();
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
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            GlobalFunc.showToast(getApplicationContext(), getString(R.string.network_error));
                            DialogUtils.dismiss();
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
                        doLogin();
                    } catch(Exception ex) {

                    }
                }
            });
        } catch(Exception ex) {

        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event instanceof MyEvent) {
            String code = event.getCode();
            if(code.equals(MyEvent.ACTION_LOGIN_ACTIVITY_DESTROY)){
                finish();
            }
        }
    }

    private void toRegesiterPage(){
        Intent intent = new Intent(this,UserRegisterActivity.class);
        startActivity(intent);
    }

    private void getSubAccountToken(String openId){
        UserNetManager.getInstance().subAccountToken(openId, new IUserDataCallBack() {
            @Override
            public void onCallBackOpenId(String str) {
                if (str!=null){
                    LogUtil.debugLog(TAG,"str:::"+str);
                    TokenHelper.getInstance().setSubAccessToken(str, getApplication());
                    toDeviceList();

                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_EMAIL,mUserEmail);
                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_PASSWD,mUserPw);
                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_SUBACCESSTOKEN, str);
                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_AUTOLOGIN, 1);
                    ACONST.LOGIN_EMAIL=mUserEmail;
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }
        });
    }

    private void toDeviceList(){
        finish();
        ARouter.getInstance().build(RoutePathManager.ActivityPath.DeviceListActivityPath).navigation();
    }

}
