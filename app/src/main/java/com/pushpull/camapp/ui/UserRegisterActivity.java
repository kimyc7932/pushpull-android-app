package com.pushpull.camapp.ui;

import static com.mm.android.mobilecommon.route.RoutePathManager.ActivityPath.UserRegesiterPath;
import static com.mm.android.mobilecommon.utils.WordInputFilter.REGEX_EMAIL_GUO;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.usermodule.net.IUserDataCallBack;
import com.example.usermodule.net.UserNetManager;
import com.mm.android.mobilecommon.base.BaseActivity;
import com.mm.android.mobilecommon.common.CommonParam;
import com.mm.android.mobilecommon.openapi.TokenHelper;
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
import com.pushpull.camapp.R;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

@Route(path =UserRegesiterPath )
public class UserRegisterActivity extends BaseActivity implements View.OnClickListener{
    public final static String TAG = UserRegisterActivity.class.getSimpleName();

    EditText m_uiEtEmail;
    LinearLayout mRegesiter;
    LinearLayout mBack;
    EditText m_uiEtEmailConfirm;
    EditText m_uiEtPasswd;
    EditText m_uiEtPasswdConfirm;
    EditText m_uiEtName;
    EditText m_uiEtPhoneNum;

    private String mUserName;
    private String mUserPw;
    private String mUserNickName;
    private String mUserPhoneNum;
    Button m_uiBtnEmailConfirm;

    TextView m_uiTvAuthTimer;
    Button m_uiBtnAuth;
    int mTimerValue=120; // 2분
    MyTimer mTimerTask;
    Timer mTimer;

    Context m_context;
    String m_strTelSid;
    boolean m_bEmailAuthCodeOk=false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.user_regesiter_activity);
        initView();
        initData();
    }

    private void initView() {

        m_uiEtEmail = findViewById(R.id.login_username);
        mRegesiter = findViewById(R.id.login_regesiter);
        mBack = findViewById(R.id.ll_back);
        m_uiTvAuthTimer = findViewById(R.id.tv_auth_timer);
        m_uiBtnAuth = findViewById(R.id.btn_auth);
        m_uiEtEmailConfirm = findViewById(R.id.et_email_confirm);

        m_uiEtPasswd = findViewById(R.id.et_passwd);
        m_uiEtPasswdConfirm = findViewById(R.id.et_passwd_confirm);
        m_uiEtName = findViewById(R.id.et_name);
        m_uiEtPhoneNum = findViewById(R.id.et_phone);

        m_uiBtnEmailConfirm =findViewById(R.id.btn_auth_confirm);

        mRegesiter.setOnClickListener(this);
        mBack.setOnClickListener(this);
        m_uiBtnAuth.setOnClickListener(this);
        m_uiBtnEmailConfirm.setOnClickListener(this);
    }

    private void initData() {
        m_context = getApplicationContext();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.login_regesiter: // 회원가입
            {
                doRegister();
                break;
            }
            case R.id.ll_back: {
                Log.d(TAG, "back button !!!");
                finish();
            }
            case R.id.btn_auth: // 인증버튼
            {
                if(mTimer != null){
                    mTimer.cancel();
                    mTimerTask.cancel();
                    mTimer = null;
                    Log.d(TAG, "timer cancel !!!");
                }

                doAuthEmailNumer();

                break;
            }
            case R.id.btn_auth_confirm: //인증확인 버튼
            {
                doCehckEmailConfirm();
                break;
            }
        }
    }

    private void toDeviceList(){
        ARouter.getInstance().build(RoutePathManager.ActivityPath.DeviceListActivityPath).navigation();
    }

    public class MyTimer extends TimerTask{
        @Override
        public void run() {

            runOnUiThread(new Runnable() {
                @SuppressLint("DefaultLocale")
                @Override
                public void run() {
                    int minutes = mTimerValue / 60;
                    int seconds = mTimerValue - minutes * 60;
                    Log.d(TAG, minutes +":"+seconds);
                    m_uiTvAuthTimer.setText(String.format("%02d:%02d", minutes,seconds));
                    if(mTimerValue <= 0){
                        if(mTimer != null){
                            mTimer.cancel();
                            mTimerTask.cancel();
                            mTimer = null;
                        }
                    }
                    mTimerValue -= 1;
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mTimer != null){
            mTimer.cancel();
            mTimerTask.cancel();
            mTimer = null;
        }
    }

    void doRegister() {
        mUserName = m_uiEtEmail.getText().toString().trim();
        if(mUserName.isEmpty() || !mUserName.matches(REGEX_EMAIL_GUO) ){
            Toast.makeText(UserRegisterActivity.this, R.string.phone_not_match_tip,Toast.LENGTH_SHORT).show();
            return;
        }

        // 이메일 인증번호 체크
//        if( !m_bEmailAuthCodeOk ){
//            Toast.makeText(UserRegisterActivity.this, R.string.user_input_auth_email_error,Toast.LENGTH_SHORT).show();
//            return;
//        }

        // 패스워드 체크
        final String strPasswd = m_uiEtPasswd.getText().toString().trim();
        final String strPasswdConfirm = m_uiEtPasswdConfirm.getText().toString().trim();
        if(strPasswd.isEmpty() || strPasswdConfirm.isEmpty()){
            Toast.makeText(UserRegisterActivity.this, R.string.user_input_passwd_error,Toast.LENGTH_SHORT).show();
            return;
        }

        if(!strPasswd.equals(strPasswdConfirm)){
            Toast.makeText(UserRegisterActivity.this, R.string.user_input_passwd_mismatch,Toast.LENGTH_SHORT).show();
            return;
        }

        // 비밀번호 형식 체크
//        if( strPasswd.length() < 8 || !GlobalFunc.validatePass(strPasswd) ){
//            Toast.makeText(UserRegisterActivity.this, R.string.user_input_passwd_invalid,Toast.LENGTH_SHORT).show();
//            return;
//        }

        mUserPw = strPasswd;
        mUserNickName = m_uiEtName.getText().toString().trim();
        mUserPhoneNum = m_uiEtPhoneNum.getText().toString().trim();

        if(mUserNickName.isEmpty()){
            Toast.makeText(UserRegisterActivity.this, R.string.user_input_name_error,Toast.LENGTH_SHORT).show();
            return;
        }

        // 전화번호 필수 체크
        if(mUserPhoneNum.isEmpty()){
            Toast.makeText(UserRegisterActivity.this, R.string.user_input_phonenum_error,Toast.LENGTH_SHORT).show();
            return;
        }

        DialogUtils.show(this);

        final String reqUrl = ACONST.API_JOIN;
        String reqBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("email", mUserName);
            reqBodyJson.put("pw", mUserPw);
            reqBodyJson.put("name", mUserNickName);
            reqBodyJson.put("tel", ACONST.COUNTRY_CODE+"-"+mUserPhoneNum);
            reqBodyJson.put("age", "");
            reqBodyJson.put("sex", "");
            reqBodyJson.put("phone_type",1); // 1:android, 2:ios
            reqBodyJson.put("rkey", "1");
            reqBody = reqBodyJson.toString();
            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + reqBody);
            AsyncJSON.requestJSON(reqUrl, reqBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    if (param == null) {
                        Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                        GlobalFunc.showToast(getApplicationContext(), getString(R.string.network_error));
                        DialogUtils.dismiss();
                        return;
                    }
                    Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());

                    try {
                        int rescode = param.getInt("rescode");
                        if (rescode == 200) {
                            JSONObject jdata = param.getJSONObject("data");

                            TokenHelper.getInstance().accessToken = jdata.getString("dahuatoken");
                            ACONST.LOGIN_UID = jdata.getString("uid");
                            ACONST.LOGIN_EMAIL = mUserName;

                            String appId = jdata.getString("appid");
                            String appSecret = jdata.getString("appsecret");

                            if(!initAccessToken(appId, appSecret)){
                                DialogUtils.dismiss();
                                Toast.makeText(UserRegisterActivity.this, R.string.login_server_error,Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ACONST.AUTH_SERVER_LOGIN=true;

                            UserNetManager.getInstance().createAccountToken(mUserName, new IUserDataCallBack() {
                                @Override
                                public void onCallBackOpenId(String openId) {
                                    LogUtil.debugLog(TAG,"openId"+openId);
                                    if (openId!=null){
                                        TokenHelper.getInstance().openid =openId;

                                        UserNetManager.getInstance().subAccountToken(openId, new IUserDataCallBack() {
                                            @Override
                                            public void onCallBackOpenId(String token) {
                                                if (token != null) { // token

                                                    DialogUtils.dismiss();
                                                    Toast.makeText(UserRegisterActivity.this, R.string.user_reg_ok, Toast.LENGTH_SHORT).show();

                                                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_EMAIL, mUserName);
                                                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_PASSWD, mUserPw);
                                                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_SUBACCESSTOKEN, token);
                                                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_AUTOLOGIN, 1);

                                                    TokenHelper.getInstance().setSubAccessToken(token, getApplication());
                                                    toDeviceList();
                                                    finish();

                                                    MyEvent event = new MyEvent(MyEvent.ACTION_LOGIN_ACTIVITY_DESTROY);
                                                    EventBus.getDefault().post(event);
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable throwable) {
                                                Log.e(TAG, "---> "+throwable.getMessage());
                                                DialogUtils.dismiss();
                                            }
                                        });
                                    }
                                }


                                @Override
                                public void onError(Throwable throwable) {
                                    String msg = throwable.getMessage();
                                    Log.e(TAG, msg);

                                    if(!msg.contains("SUB1010")){ // exist
                                        DialogUtils.dismiss();
                                        Toast.makeText(UserRegisterActivity.this, R.string.user_reg_fail, Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // exist account
                                    UserNetManager.getInstance().getOpenIdByAccount(mUserName, new IUserDataCallBack() {
                                        @Override
                                        public void onCallBackOpenId(String openId) {
                                            if (openId != null) {
                                                TokenHelper.getInstance().openid = openId;

                                                UserNetManager.getInstance().subAccountToken(openId, new IUserDataCallBack() {
                                                    @Override
                                                    public void onCallBackOpenId(String token) {
                                                        if (token != null) { // token

                                                            DialogUtils.dismiss();
                                                            Toast.makeText(UserRegisterActivity.this, R.string.user_reg_ok, Toast.LENGTH_SHORT).show();

                                                            /** 회원가입 완료 후 자동 로그인시 오류가 발생하는 경우가 있어 자동로그인 기능 제외 - 2022.06.21 swkim **/
                                                            if(ACONST.REGISTER_FINISH_AUTO_LOGIN) {
                                                                gotoAutoLogin(token);
                                                            } else {
                                                                gotoLoginView();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Throwable throwable) {
                                                        Log.e(TAG, "---> "+throwable.getMessage());
                                                        DialogUtils.dismiss();
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            LogUtil.debugLog(TAG, "msg:::" + throwable.getMessage());
                                            DialogUtils.dismiss();
                                        }
                                    });
                                }
                            });

                        } else if( rescode == 432 ){
                            DialogUtils.dismiss();
                            Toast.makeText(UserRegisterActivity.this, R.string.user_reg_exist, Toast.LENGTH_SHORT).show();
                        } else {
                            DialogUtils.dismiss();
                            Toast.makeText(UserRegisterActivity.this, R.string.user_reg_fail, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException ex){
                        ex.printStackTrace();
                    }
                }
            });

        } catch (JSONException ex){
            ex.printStackTrace();
        }

    }


    /**
     * 회원가입 완료 후 로그인 페이지로 이동
     */
    private void gotoLoginView() {
        Intent intent = new Intent(UserRegisterActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 회원가입 완료 후 자동 로그인처리
     * @param token
     */
    private void gotoAutoLogin(String token) {
        doLogin(mUserName, mUserPw); //auto login
        Toast.makeText(UserRegisterActivity.this, R.string.user_reg_ok, Toast.LENGTH_SHORT).show();

        PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_EMAIL, mUserName);
        PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_PASSWD, mUserPw);
        PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_SUBACCESSTOKEN, token);
        PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_AUTOLOGIN, 1);
        TokenHelper.getInstance().setSubAccessToken(token, getApplication());
        toDeviceList();
        finish();
    }

    private boolean initAccessToken(String appId, String appSecret) {
        String appIdKey=appId;
        String appSecretKey=appSecret;
        String url=ACONST.AUTH_API_URL;

        PreferencesHelper.getInstance(UserRegisterActivity.this).set(ACONST.OVERSEAS_APP_ID_KEY, appIdKey);
        PreferencesHelper.getInstance(UserRegisterActivity.this).set(ACONST.OVERSEAS_APP_SECRET_KEY, appSecretKey);
        PreferencesHelper.getInstance(UserRegisterActivity.this).set(ACONST.OVERSEAS_URL, url);

        try {
            CommonParam commonParam = new CommonParam();
            commonParam.setEnvirment(url);
            commonParam.setContext(UserRegisterActivity.this.getApplication());
            commonParam.setAppId(appIdKey);
            commonParam.setAppSecret(appSecretKey);
            TokenHelper.getInstance().init(commonParam);

            return true;
        } catch (Throwable e) {
            //Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    // 이메일 인증 요청
    private void doAuthEmailNumer() {
        mUserName = m_uiEtEmail.getText().toString().trim();
        if(mUserName.isEmpty() || !mUserName.matches(REGEX_EMAIL_GUO) ){
            Toast.makeText(UserRegisterActivity.this, R.string.phone_not_match_tip,Toast.LENGTH_SHORT).show();
            return;
        }

        DialogUtils.show(this);

        m_bEmailAuthCodeOk=false;

        final String reqUrl = ACONST.API_EMAIL_AUTH;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("sendto", mUserName);
            reqBodyJson.put("is_register", "-1"); // 0: 가입시
            regBody = reqBodyJson.toString();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if (param == null) {
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            GlobalFunc.showToast(getApplicationContext(), getString(R.string.network_error));
                            DialogUtils.dismiss();
                            return;
                        }
                        Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());

                        DialogUtils.dismiss();

                        int rescode = param.getInt("rescode");
                        if(rescode == 200){
                            JSONObject jdata = param.getJSONObject("data");
                            GlobalFunc.showToast(m_context, "인증코드를 전송했습니다");
                            m_strTelSid = jdata.getString("sid");

                            try {
                                mTimerValue = 120; //120
                                mTimer = new Timer();
                                mTimerTask = new MyTimer();
                                mTimer.schedule(mTimerTask, 0, 1000);
                            } catch(Exception ex){
                                Log.e(TAG, ex.toString());
                            }

                        } else {
                            GlobalFunc.showToast(m_context, "인증코드를 전송할수 없습니다");
                            m_strTelSid = "";
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        } catch (JSONException ex){
            ex.printStackTrace();
        }
    }

    private void doCehckEmailConfirm() {

        DialogUtils.show(this);

        String authCode = m_uiEtEmailConfirm.getText().toString().trim();
        if( authCode.isEmpty() ){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "인증코드를 입력해주세요");
            return;
        }

        if( mUserName.isEmpty() || !GlobalFunc.validateEmail(mUserName)){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "잘못된 이메일 주소입니다.");
            return;
        }

        final String reqUrl = ACONST.API_EMAIL_AUTHCHECK;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("sid", m_strTelSid);
            reqBodyJson.put("akey", authCode);
            regBody = reqBodyJson.toString();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if (param == null) {
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            GlobalFunc.showToast(getApplicationContext(), getString(R.string.network_error));
                            DialogUtils.dismiss();
                            return;
                        }
                        Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());

                        DialogUtils.dismiss();

                        int rescode = param.getInt("rescode");
                        if(rescode == 200) {
                            m_bEmailAuthCodeOk=true;
                            GlobalFunc.showToast(m_context, "이메일 인증되었습니다.");

                            //인증버튼, 인증코드 입력창 disable
                            m_uiEtEmail.setEnabled(false);
                            m_uiBtnAuth.setEnabled(false);
                            m_uiEtEmailConfirm.setEnabled(false);
                            m_uiBtnEmailConfirm.setEnabled(false);

                        } else {
                            GlobalFunc.showToast(m_context, "잘못된 인증코드 입니다");
                            m_uiTvAuthTimer.setText("00:00");
                            m_uiEtEmailConfirm.setText("");
                            m_bEmailAuthCodeOk=false;
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }

                    if(mTimer != null){
                        mTimer.cancel();
                        mTimerTask.cancel();
                        mTimer = null;
                    }
                }
            });
        } catch (JSONException ex){
            ex.printStackTrace();
        }
    }

    private void doLogin(String email, String passwd) {
        final String reqUrl = ACONST.API_LOGIN;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("id", email);
            reqBodyJson.put("pw", passwd);
            reqBodyJson.put("type", 1); // email
            reqBodyJson.put("rkey", "1");
            regBody = reqBodyJson.toString();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    if(param == null){
                        Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                        return;
                    }
                    Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
