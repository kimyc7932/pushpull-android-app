package com.pushpull.camapp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.openapi.ClassInstanceManager;
import com.example.usermodule.api.IAccountUserDataCallBack;
import com.example.usermodule.net.IDeviceOperationCallback;
import com.example.usermodule.net.UserNetManager;
import com.mm.android.deviceaddmodule.LCDeviceEngine;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.dialog.PPSConfirmDialog;
import com.mm.android.mobilecommon.pps.model.MyEvent;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.opensdk.devicedetail.net.DeviceDetailService;
import com.pushpull.camapp.R;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPageActivity extends Activity implements View.OnClickListener, PPSConfirmDialog.OnOkClickListener {
    public static final String TAG = MyPageActivity.class.getSimpleName();

    private LinearLayout llBack;
    private LinearLayout llLogout;
    private LinearLayout llWithdrawal;
    private TextView tvVersion;
    private TextView tvEmail;
    private RelativeLayout rlMain;
    private RelativeLayout rlPwChange;
    private LinearLayout llPwChangeBtn;
    private LinearLayout llPwChangeConfirm;
    private EditText m_uiEtPwOld;
    private EditText m_uiEtPwNew;
    private EditText m_uiEtPwNewRetry;
    private TextView m_uiTvPhoneNum;
    private TextView tvReset;
    private LinearLayout llSupport;
    private LinearLayout llKakao;
    private Button btnVersionUpdate;
    private ImageView ivPushCheck;
    private EditText m_uiEtChangeTel;
    private TextView m_uiTvAuthBtn;
    private EditText m_uiEtAuthInput;
    Context m_context;
    String m_strChangePhoneNumber;
    String m_strTelSid;
    RelativeLayout m_uiRlChangeTel;
    LinearLayout m_uiLLTelChangeSet;
    LinearLayout m_uiLLTelChangeBtn;
    private PPSConfirmDialog ppsConfirmDialog;
    List<DeviceDetailListData.ResponseData.DeviceListBean> mCameraDatas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        Bundle bundle = getIntent().getExtras();
        mCameraDatas = (List<DeviceDetailListData.ResponseData.DeviceListBean>) bundle.getSerializable("mCameraDatas");

        initVIew();
        initData();
    }

    private void initVIew() {
        llBack = findViewById(R.id.ll_back);
        llLogout = findViewById(R.id.ll_logout);
        llWithdrawal = findViewById(R.id.ll_withdrawal);
        tvVersion = findViewById(R.id.tv_version);
        btnVersionUpdate = findViewById(R.id.btn_version_update);
        tvEmail = findViewById(R.id.tv_email);
        rlMain = findViewById(R.id.rl_main);
        rlPwChange = findViewById(R.id.rl_pw_change);
        llPwChangeBtn = findViewById(R.id.ll_pw_change_btn);
        llPwChangeConfirm = findViewById(R.id.ll_password_change_confirm);

        m_uiEtPwOld = findViewById(R.id.et_old_pw);
        m_uiEtPwNew = findViewById(R.id.et_new_pw);
        m_uiEtPwNewRetry = findViewById(R.id.et_new_pw_retry);
        m_uiTvPhoneNum = findViewById(R.id.tv_phone_number);

        m_uiLLTelChangeSet = findViewById(R.id.ll_telchange_set);

        m_uiTvAuthBtn = findViewById(R.id.tv_auth_btn);
        m_uiRlChangeTel = findViewById(R.id.rl_tel_change);
        m_uiLLTelChangeBtn =findViewById(R.id.ll_tel_change_confirm);
        m_uiEtChangeTel = findViewById(R.id.et_tel_change);
        m_uiEtAuthInput = findViewById(R.id.et_tel_change_authcode);
        ivPushCheck = findViewById(R.id.iv_push_check);

        tvReset = findViewById(R.id.tv_reset);

        llBack.setOnClickListener(this);
        llLogout.setOnClickListener(this);
        llWithdrawal.setOnClickListener(this);
        llPwChangeBtn.setOnClickListener(this);
        llPwChangeConfirm.setOnClickListener(this);
        m_uiTvAuthBtn.setOnClickListener(this);
        m_uiLLTelChangeSet.setOnClickListener(this);
        m_uiLLTelChangeBtn.setOnClickListener(this);

        llSupport = findViewById(R.id.ll_support);
        llSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(getCustomerSupportURL());
                intent.setData(uri);
                startActivity(intent);
            }
        });

        // 카카오톡채널
        if(ACONST.COUNTRY_CODE != null && ACONST.COUNTRY_CODE.equals("+82")) {
            llKakao = findViewById(R.id.ll_kakao);
            llKakao.setVisibility(View.VISIBLE);
            llKakao.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("https://pf.kakao.com/_xhZqsxj");
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
        }

        drawImageView(ACONST.PUSH_NOTICE_KEY, ivPushCheck);
        ivPushCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pushOnOff();
                    }
                }, 200);
            }
        });
        tvReset.setOnClickListener(this);
    }

    public String getCustomerSupportURL() {
        String url = "https://bangtancctv.notion.site/CCTV-101f9b40fe6342ad80a5ed9a8764afb8";
        if(ACONST.COUNTRY_CODE.equals("+86")) {
            url = "https://bangtancctv.notion.site/27bf1d7024fa40fdb6fe99c0c9a3f300";
        }

        return url;
    }

    private void drawImageView(String key, ImageView imageView) {
        boolean b = PreferencesHelper.getInstance(this).getBoolean(key, false);

        if (b) {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.lc_demo_switch_on));
        } else {
            imageView.setImageDrawable(getResources().getDrawable(R.mipmap.lc_demo_switch_off));
        }
    }

    private void pushOnOff() {
        DialogUtils.show(this);
        String uid = ACONST.LOGIN_UID;
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("uid", uid);

        boolean pushValue = PreferencesHelper.getInstance(this).getBoolean(ACONST.PUSH_NOTICE_KEY, false);
        pushValue = !pushValue;
        paramMap.put("notice", (pushValue ? "1" : "0"));
        final boolean finalPushValue = pushValue;
        UserNetManager.getInstance().updateUserInfo(paramMap, new IAccountUserDataCallBack() {
            @Override
            public void onSuccess(String response) {
                DialogUtils.dismiss();
                if(response == null || response.equals("")) return;

                if(response.equals("SUCCESS")) {
                    PreferencesHelper.getInstance(MyPageActivity.this).set(ACONST.PUSH_NOTICE_KEY, finalPushValue);
                    drawImageView(ACONST.PUSH_NOTICE_KEY, ivPushCheck);
                }

                DialogUtils.dismiss();
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();
            }
        });
    }

    private void initData() {
        //version & version code
        ACONST.APP_VERCODE=getAppVersion();
        ACONST.APP_VERNAME=getAppVerName();

        String verName = "v"+ACONST.APP_VERNAME;
        tvVersion.setText(verName);

        String tel = ACONST.userInfo.userTel;
        if(tel != null && !tel.equals("") && tel.startsWith("null-")) {
            if(ACONST.COUNTRY_CODE == null) {
                tel = tel.replace("null-", "");
                ACONST.userInfo.userTel = tel;
            } else {
                tel = tel.replace("null", ACONST.COUNTRY_CODE);
                ACONST.userInfo.userTel = tel;
            }
        }

        tvEmail.setText(ACONST.LOGIN_EMAIL);
        m_uiTvPhoneNum.setText(ACONST.userInfo.userTel);

        m_context = getApplicationContext();

        btnVersionUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id="+getPackageName()));
                startActivity(intent);
            }
        });

        checkVersion();
    }

    private void checkVersion() {
        DialogUtils.show(this);
        String uid = ACONST.LOGIN_UID;
        UserNetManager.getInstance().getNewestVersion(uid, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                DialogUtils.dismiss();
                if(response == null || response.equals("")) return;

                String newVersion = response;
                if(response.contains(ACONST.VERSION_CHECK_SEPERATE)) {
                    String[] arr =  response.split("\\"+ACONST.VERSION_CHECK_SEPERATE);
                    newVersion = arr[0];
                }

                String newText = getResources().getString(R.string.lc_demo_device_version_new);
                tvVersion.setText(ACONST.APP_VERNAME + "("+ newText +")");
                if(!ACONST.APP_VERNAME.equals(newVersion)) {
                    tvVersion.setText(ACONST.APP_VERNAME + "/" + newVersion);
                    btnVersionUpdate.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();
            }
        });
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(rlPwChange.getVisibility() == View.VISIBLE){
                rlPwChange.setVisibility(View.GONE);
                rlMain.setVisibility(View.VISIBLE);
                return false;
            }

        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.tv_reset:
                ppsConfirmDialog = new PPSConfirmDialog(this, getString(R.string.lc_device_reset), getString(R.string.lc_device_reset_confirm));
                ppsConfirmDialog.setOnOkClickListener(this);

                ppsConfirmDialog.show();
                break;
            case R.id.ll_back: {

                if(rlPwChange.getVisibility() == View.VISIBLE){
                    rlPwChange.setVisibility(View.GONE);
                    rlMain.setVisibility(View.VISIBLE);
                    break;
                }

                if(m_uiRlChangeTel.getVisibility() == View.VISIBLE){
                    m_uiRlChangeTel.setVisibility(View.GONE);
                    rlMain.setVisibility(View.VISIBLE);
                    break;
                }

                finish();
                break;
            }
            case R.id.ll_logout:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.popup_title);
                builder.setMessage(R.string.popup_logout_text);
                builder.setCancelable(false);
                builder.setIcon(R.mipmap.sdk_launcher);
                builder.setPositiveButton(R.string.popup_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // logout
                        ACONST.Logout(getApplicationContext());

                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);

                        finish();

                        MyEvent event = new MyEvent(MyEvent.ACTION_DEVICELIST_ACTIVITY_DESTROY);
                        EventBus.getDefault().post(event);

                    }
                });
                builder.setNegativeButton(R.string.popup_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                final AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                    }
                });

                dialog.show();

                break;
            }
            case R.id.ll_pw_change_btn: // 패스워드 변경
            {
                rlMain.setVisibility(View.GONE);
                rlPwChange.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.ll_password_change_confirm: // 패스워드 변경 확인
            {
                final String strOldPw = m_uiEtPwOld.getText().toString().trim();
                final String strNewPw = m_uiEtPwNew.getText().toString().trim();
                final String strNewPwRetry = m_uiEtPwNewRetry.getText().toString().trim();

                if(!strNewPw.equals(strNewPwRetry)){
                    Toast.makeText(MyPageActivity.this, R.string.user_input_passwd_mismatch,Toast.LENGTH_SHORT).show();
                    return;
                }

                if( strNewPw.length() < 8 || !GlobalFunc.validatePass(strNewPwRetry) ){
                    Toast.makeText(MyPageActivity.this, R.string.user_input_passwd_invalid,Toast.LENGTH_SHORT).show();
                    return;
                }

                DialogUtils.show(this);

                final String reqUrl = ACONST.API_CHANGE_PW;
                String reqBody="";
                try {
                    JSONObject reqBodyJson = new JSONObject();
                    reqBodyJson.put("uid", ACONST.LOGIN_UID);
                    reqBodyJson.put("current_pw", strOldPw);
                    reqBodyJson.put("next_pw", strNewPw);
                    reqBody = reqBodyJson.toString();

                    Log.d(TAG, "<------- REQ: " + reqUrl + ", " + reqBody);
                    AsyncJSON.requestJSON(reqUrl, reqBody, new Processable<JSONObject>() {
                        @Override
                        public void process(JSONObject param) {
                            DialogUtils.dismiss();
                            if (param == null) {
                                Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                                DialogUtils.dismiss();
                                return;
                            }
                            Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());
                            try
                            {
                                int rescode = param.getInt("rescode");
                                if(rescode == 200){
                                    PreferencesHelper.getInstance(getApplicationContext()).set(ACONST.PREF_PASSWD, strNewPw);
                                    GlobalFunc.showToast(MyPageActivity.this, getString(R.string.password_change_success));
                                    rlMain.setVisibility(View.VISIBLE);
                                    rlPwChange.setVisibility(View.GONE);
                                } else {
                                    GlobalFunc.showToast(MyPageActivity.this, getString(R.string.password_change_fail));
                                }
                            } catch (JSONException ex){
                                ex.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            }
            case R.id.ll_withdrawal: // 회원탈퇴
            {
                int myDeviceCnt = 0;
                if(mCameraDatas != null) {
                    // 본인소유의 장비가 1대라도 등록되어 있으면 탈퇴 불가
                    for(DeviceDetailListData.ResponseData.DeviceListBean listBean : mCameraDatas) {
                        if(listBean.deviceStatus.equals("EMPTY")) continue;
                        if(!listBean.isShared) myDeviceCnt++;
                    }
                }

                if(myDeviceCnt > 0) {
                    GlobalFunc.showToast(m_context, getString(R.string.user_withdrawal_device_error));
                    return;
                }
                doWithdrawal();
                break;
            }
            case R.id.ll_telchange_set: // tel change
            {
                rlMain.setVisibility(View.GONE);
                m_uiRlChangeTel.setVisibility(View.VISIBLE);
                break;
            }
            case R.id.tv_auth_btn: // tel change auth button
            {
                doAuthTelNumer();
                break;
            }
            case R.id.ll_tel_change_confirm:
            {
                doTelChange();
                break;
            }
        }
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

    void doWithdrawal() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.popup_title);
        builder.setMessage(R.string.popup_withdrawal_text);
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.sdk_launcher);
        builder.setPositiveButton(R.string.popup_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                final String reqUrl = ACONST.API_WITHDRAWAL;
                String regBody = "";
                JSONObject reqBodyJson = new JSONObject();

                try {
                    reqBodyJson.put("uid", ACONST.LOGIN_UID);
                    reqBodyJson.put("email", ACONST.LOGIN_EMAIL);
                    reqBodyJson.put("wmode", "2");
                    regBody = reqBodyJson.toString();

                    Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
                    AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                        @Override
                        public void process(JSONObject param) {
                            try {
                                if(param == null){
                                    Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                                    return;
                                }

                                Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());
                                int rescode = param.getInt("rescode");
                                if(rescode == 200) {

                                    // subaccount 계정 삭제 - swkim
                                    String openid = LCDeviceEngine.newInstance().openid;
                                    UserNetManager.getInstance().deleteAccountToken(openid, new IAccountUserDataCallBack() {
                                        @Override
                                        public void onSuccess(String response) {
                                            Log.d(TAG, "deleteAccountToken.onSuccess -----------------------------------");
                                            Log.d(TAG, response);
                                            Log.d(TAG, "deleteAccountToken.onSuccess -----------------------------------");
                                        }

                                        @Override
                                        public void onError(Throwable throwable) {
                                            Log.d(TAG, "deleteAccountToken.onError -----------------------------------");
                                            Log.d(TAG, throwable.getMessage());
                                            Log.d(TAG, "deleteAccountToken.onError -----------------------------------");
                                        }
                                    });

                                    Toast.makeText(MyPageActivity.this, R.string.user_withdrawal_success, Toast.LENGTH_SHORT).show();
                                    // withdrawal
                                    ACONST.Withdrawal(getApplicationContext());
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();

                                    MyEvent event = new MyEvent(MyEvent.ACTION_DEVICELIST_ACTIVITY_DESTROY);
                                    EventBus.getDefault().post(event);

                                } else if( rescode == 416){
                                    Toast.makeText(MyPageActivity.this, R.string.user_withdrawal_device_error, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MyPageActivity.this, R.string.user_withdrawal_error, Toast.LENGTH_SHORT).show();
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
        });
        builder.setNegativeButton(R.string.popup_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
            }
        });

        dialog.show();
    }

    private void doAuthTelNumer() {
        DialogUtils.show(this);

        String phoneNum=m_uiEtChangeTel.getText().toString().trim();
        if( phoneNum.isEmpty() ){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "휴대폰 번호를 입력해주세요");
            return;
        }

        m_strChangePhoneNumber = ACONST.COUNTRY_CODE+"-"+phoneNum;

        final String reqUrl = ACONST.API_TEL_AUTH;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("sendto", m_strChangePhoneNumber);
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
                            m_uiEtChangeTel.setEnabled(false);
                        } else {
                            GlobalFunc.showToast(m_context, "인증코드를 전송할수 없습니다");
                            m_strTelSid = "";
                            m_uiEtChangeTel.setEnabled(true);
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

    private void doTelChange() {

        DialogUtils.show(this);

        String phoneNum=m_uiEtChangeTel.getText().toString().trim();
        if( phoneNum.isEmpty() ){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "휴대폰 번호를 입력해주세요");
            return;
        }

        m_strChangePhoneNumber = ACONST.COUNTRY_CODE+"-"+phoneNum;

        String authCode = m_uiEtAuthInput.getText().toString().trim();
        if( authCode.isEmpty() ){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "인증코드를 입력해주세요");
            return;
        }

        final String reqUrl = ACONST.API_TEL_AUTHCHECK;
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
                            sendChangeTel(m_strChangePhoneNumber);
                        } else {
                            GlobalFunc.showToast(m_context, "잘못된 인증코드 입니다");
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

    private void sendChangeTel(final String phoneNumber) {
        DialogUtils.show(this);

        final String reqUrl = ACONST.API_CHANGE_TEL;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("uid", ACONST.userInfo.userId);
            reqBodyJson.put("current_tel", ACONST.userInfo.userTel);
            reqBodyJson.put("next_tel", phoneNumber);
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
                        if(rescode == 200)
                        {
                            GlobalFunc.showToast(m_context, "핸드폰번호가 변경되었습니다.");

                            rlMain.setVisibility(View.VISIBLE);
                            m_uiRlChangeTel.setVisibility(View.GONE);

                            ACONST.userInfo.userTel = phoneNumber;

                        } else {
                            GlobalFunc.showToast(m_context, "핸드폰번호를 변경할 수 없습니다");
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

    @Override
    public void OnOK(String title, Map<String, Object> param) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                deviceReset();
            }
        },200);
    }

    void deviceReset() {
        DialogUtils.show(this);

        final String reqUrl = ACONST.API_DEVICE_RESET;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        String deviceIds = "";
        if(mCameraDatas != null) {
            for(DeviceDetailListData.ResponseData.DeviceListBean listBean : mCameraDatas) {
                deviceIds += listBean.deviceId+",";
            }

            if(deviceIds != null && !deviceIds.equals("")) {
                deviceIds = deviceIds.substring(0, deviceIds.length()-1);
            }
        }

        try {
            reqBodyJson.put("uid", ACONST.LOGIN_UID);
            reqBodyJson.put("deviceIds", deviceIds);
            regBody = reqBodyJson.toString();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @SuppressLint("NotifyDataSetChanged")
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
                        if (rescode == 200) {
                            String data = param.getString("data");
                            if(data != null && !data.equals("")) {
                                deleteFaultyCameras(data);
                            } else {
                                GlobalFunc.showToast(getApplicationContext(), getString(R.string.lc_all_device_ok));
                            }
                        }
                    } catch (JSONException ex) {
                    }
                }
            });
        } catch (JSONException ex){
        }
    }

    void deleteFaultyCameras(final String deviceIds){
        DialogUtils.show(this);
        String[] spl = deviceIds.split(",");

        final DeviceDetailService deviceDetailService = new DeviceDetailService();

        for(final String deviceId : spl) {
            final String reqUrl = ACONST.API_DEVICE_DEL;
            String regBody = "";
            JSONObject reqBodyJson = new JSONObject();
            try {
                reqBodyJson.put("uid", ACONST.LOGIN_UID);
                reqBodyJson.put("dmode", "2"); // 1:device_id, 2: delete by serialnum
                reqBodyJson.put("serialnum", deviceId);
                regBody = reqBodyJson.toString();

                Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
                AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                    @Override
                    public void process(JSONObject param) {
                        if (param == null) {
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            return;
                        }

                        Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());
                        try {
                            int rescode = param.getInt("rescode");
                            if(rescode == 200 || rescode == 404){
                                deviceDetailService.deletePermission(deviceId,null, null);
                            } else{
                                GlobalFunc.showToast(getApplicationContext(), getString(R.string.device_delete_fail));
                            }
                        } catch (JSONException ex){
                        }
                    }
                });
            } catch (JSONException e) {
                DialogUtils.dismiss();
                e.printStackTrace();
            }
        } // end for
        DialogUtils.dismiss();
        GlobalFunc.showToast(getApplicationContext(), getString(R.string.lc_device_reset_fin));
    }

}