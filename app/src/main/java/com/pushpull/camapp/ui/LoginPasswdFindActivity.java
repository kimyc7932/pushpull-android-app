package com.pushpull.camapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.pushpull.camapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPasswdFindActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String TAG = LoginPasswdFindActivity.class.getSimpleName();

    EditText m_uiEtEmail;
    EditText m_uiEtPhone;
    EditText m_uiEtPhoneAuthNum;
    LinearLayout m_uiLayoutFindPw;
    LinearLayout m_uiLayoutBack;
    TextView m_uiTvAuthBtn;
    Context m_context;
    String m_strTelSid;
    String m_strPhoneNumber;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.user_password_find_activity);
        initView();
        initData();
    }

    private void initView() {
        m_uiEtEmail = findViewById(R.id.et_email);
        m_uiEtPhone = findViewById(R.id.et_phone);
        m_uiEtPhoneAuthNum = findViewById(R.id.et_phone_auth);
        m_uiLayoutFindPw = findViewById(R.id.ll_find_pw);
        m_uiLayoutBack = findViewById(R.id.ll_back);
        m_uiTvAuthBtn = findViewById(R.id.tv_auth_btn);

        m_uiLayoutFindPw.setOnClickListener(this);
        m_uiLayoutBack.setOnClickListener(this);
        m_uiTvAuthBtn.setOnClickListener(this);
    }

    private void initData() {
        m_context = getApplicationContext();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id) {
            case R.id.ll_back: {
                Log.d(TAG, "back button !!!");
                finish();
                break;
            }
            case R.id.tv_auth_btn: // 인증
            {
                doAuthTelNumer();
                break;
            }
            case R.id.ll_find_pw: // 비번찾기
            {
                doFindPassword();
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void doAuthTelNumer() {
        DialogUtils.show(this);

        String phoneNum=m_uiEtPhone.getText().toString().trim();
        if( phoneNum.isEmpty() ){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "휴대폰 번호를 입력해주세요");
            return;
        }

        m_strPhoneNumber = ACONST.COUNTRY_CODE+"-"+phoneNum;

        final String reqUrl = ACONST.API_TEL_AUTH;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("sendto", m_strPhoneNumber);
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

    private void doFindPassword() {

        DialogUtils.show(this);

        String authCode = m_uiEtPhoneAuthNum.getText().toString().trim();
        if( authCode.isEmpty() ){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "인증코드를 입력해주세요");
            return;
        }

        final String authEmail = m_uiEtEmail.getText().toString().trim();
        if( authEmail.isEmpty() || !GlobalFunc.validateEmail(authEmail)){
            DialogUtils.dismiss();
            GlobalFunc.showToast(m_context, "잘못된 이메일 주소입니다.");
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
                            sendPasswordReset(authEmail);
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

    private void sendPasswordReset(String emailText) {
        DialogUtils.show(this);

        final String reqUrl = ACONST.API_RESET_PW;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("stype", "1"); // 1: email, 2:tel, 3: email check->send tel
            reqBodyJson.put("email", emailText);
            reqBodyJson.put("tel", m_strPhoneNumber);
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
                            GlobalFunc.showToast(m_context, "메일로 임시비밀번호가 발송되었습니다.\n확인후 로그인해 주세요");
                            finish();
                        } else {
                            GlobalFunc.showToast(m_context, "비밀번호를 재설정 할수 없습니다\n관리자에게 문의해주세요");
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

}
