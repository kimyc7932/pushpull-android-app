package com.opensdk.devicedetail.ui;

import static com.mm.android.mobilecommon.utils.WordInputFilter.REGEX_EMAIL_GUO;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mm.android.mobilecommon.base.LCBusinessHandler;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.dialog.PPSConfirmDialog;
import com.mm.android.mobilecommon.pps.model.DeviceInfo;
import com.mm.android.mobilecommon.pps.model.ShareInfo;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.opensdk.devicedetail.R;
import com.opensdk.devicedetail.adapter.DeviceShareAdapter;
import com.opensdk.devicedetail.manager.DeviceShareManager;
import com.opensdk.devicedetail.manager.IShareUserDataCallBack;
import com.opensdk.devicedetail.net.DeviceShareService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeviceDetailShareFragment extends Fragment implements DeviceShareAdapter.OnShareItemClickListener, View.OnClickListener, PPSConfirmDialog.OnOkClickListener {
    private static final String TAG = DeviceDetailShareFragment.class.getSimpleName();
    private Bundle arguments;
    private EditText etEmail;
    private Button btnOk;
    private DeviceDetailActivity deviceDetailActivity;
    private DeviceShareService deviceShareService;
    private DeviceDetailListData.ResponseData.DeviceListBean deviceListBean;
    public boolean isFinish = false;
    public Handler taskHandler;
    private PPSConfirmDialog ppsConfirmDialog;
    private String deviceId;
    private RecyclerView m_uiRecyclerView;
    private List<ShareInfo> shareInfoList;
    private DeviceShareAdapter adapter;
    private List<DeviceInfo> deviceInfoList;

    public DeviceDetailShareFragment() {
    }

    public static DeviceDetailShareFragment newInstance() {
        DeviceDetailShareFragment fragment = new DeviceDetailShareFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.llOperate.setVisibility(View.GONE);
        arguments = getArguments();
        taskHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(!isFinish){
                    taskHandler.sendEmptyMessageDelayed(0,5000);
                }
            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DeviceDetailActivity deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.tvTitle.setText(getResources().getString(R.string.lc_device_share));
        initView(view);
        initData();
    }

    private void initView(View view) {
        m_uiRecyclerView = view.findViewById(R.id.rcv_share);
        m_uiRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        shareInfoList = new ArrayList<>();
        adapter = new DeviceShareAdapter(getContext(), shareInfoList);
        adapter.setOnShareItemClickListener(this);
        m_uiRecyclerView.setAdapter(adapter);

        etEmail = view.findViewById(R.id.et_email);
        btnOk = view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
    }

    private void initData() {
        //获取设备版本信息
        if (arguments == null) {
            return;
        }
        deviceListBean = (DeviceDetailListData.ResponseData.DeviceListBean) arguments.getSerializable(MethodConst.ParamConst.deviceDetail);
        if (deviceListBean == null) {
            return;
        }
        deviceId = deviceListBean.deviceId;

//        deviceDetailActivity.rlLoading.setVisibility(View.VISIBLE);
        deviceShareService =  new DeviceShareService();
        getSharedUserList();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            String email = getEmail();

            if(email == null || email.equals("")) return;

            if(email.isEmpty() || !email.matches(REGEX_EMAIL_GUO) ){
                Toast.makeText(getContext(), R.string.phone_not_match_tip, Toast.LENGTH_SHORT).show();
                return;
            }

            // 본인 email일경우 공유 불가
            if(email.equals(ACONST.LOGIN_EMAIL)) {
                Toast.makeText(getContext(), R.string.lc_device_share_fail_me, Toast.LENGTH_SHORT).show();
                return;
            }

            ppsConfirmDialog = new PPSConfirmDialog(getContext(), getString(R.string.lc_device_share), getString(R.string.lc_device_share_confirm));
            ppsConfirmDialog.setOnOkClickListener(this);
            ppsConfirmDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(taskHandler == null) return;
        taskHandler.removeMessages(0);
    }

    Toast toast;
    @Override
    public void OnOK(String title, Map<String, Object> param) {
        if(title.equals(getString(R.string.lc_device_share))) {
            DialogUtils.show(getActivity());
            if (deviceShareService == null) {
                deviceShareService = new DeviceShareService();
            }
            // openId 획득
            getOpenIdByAccount();
        }
        else if(title.equals(getString(R.string.lc_not_exists_account))) {
            // 계정생성..
            createSubAccount();
        }
        else if(title.equals(getString(R.string.lc_delete_share))) {
            ShareInfo shareInfo = (ShareInfo) param.get("shareInfo");
//            listSubAccountDevice(shareInfo.getOpenId(), shareInfo.getGroupId());
//            clearUserPolicy(shareInfo.getOpenId(), shareInfo.getGroupId());
            deleteUserPermission(shareInfo.getOpenId(), deviceId, shareInfo.getGroupId());
        }
    }

//    private void makeDeviceInfoList(JsonObject jsonObject) {
//        if(jsonObject == null) return;
//
//        this.deviceInfoList = new ArrayList<>();
//        JsonArray jsonArray = jsonObject.getAsJsonArray("policy");
//
//
//        for(int i=0; i<jsonArray.size(); i++) {
//            JsonObject obj = jsonArray.get(i).getAsJsonObject();
//            DeviceInfo deviceInfo = new DeviceInfo();
//            deviceInfo.deviceId = obj.get("deviceId").getAsString();
//            deviceInfo.permission = obj.get("permission").getAsString();
//
//            this.deviceInfoList.add(deviceInfo);
//        }
//    }

//    private void listSubAccountDevice(final String openId, final int groupId){
//        LCBusinessHandler policyHandler = new LCBusinessHandler(Looper.getMainLooper()) {
//
//            @Override
//            public void handleBusiness(Message msg) {
//                JsonObject response = (JsonObject) msg.obj;
//                makeDeviceInfoList(response);
//                clearUserPolicy(openId, groupId);
//            }
//        };
//        DeviceShareManager.newInstance().listSubAccountDevice(1, 30, openId, policyHandler);
//    }

    private void deleteUserPermission(final String openId, String sn, final int groupId){
        LCBusinessHandler policyHandler = new LCBusinessHandler(Looper.getMainLooper()) {

            @Override
            public void handleBusiness(Message msg) {
                removeShareUserDevice(groupId);

                GlobalFunc.showToast(getContext(), getString(R.string.lc_common_process_ok));
                DialogUtils.dismiss();

            }
        };
        DeviceShareManager.newInstance().deleteUserPermission(openId, sn, policyHandler);
    }

    private void getSharedUserList(){
        DialogUtils.show(getActivity());
        final String reqUrl = ACONST.API_SHARE_LIST;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();
        try {
            reqBodyJson.put("serialnum", deviceId);
            regBody = reqBodyJson.toString();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    if (param == null) {
                        Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());

                    makeAdapter(param);

                    DialogUtils.dismiss();
                }
            });
        } catch (JSONException e) {
            DialogUtils.dismiss();
            e.printStackTrace();

            GlobalFunc.showToast(getContext(), "error occured.");
        }

        DialogUtils.dismiss();
    }

    private void makeAdapter(JSONObject param) {
        try {
            shareInfoList.clear();
            JSONArray array = param.getJSONArray("data");
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                ShareInfo shareInfo = new ShareInfo(obj);
                if(shareInfo.getRights() != 4) continue;
                shareInfoList.add(shareInfo);
            }

        } catch(Exception ex) {}
        adapter.notifyDataSetChanged();
    }

    String sharedPermission = "Alarm,Config,Ptz,Capture,Real,RecordReplay,Talk,DevControl";
    private void addDeviceToPolicy(final String openId, String sn, String permission){
        LCBusinessHandler policyHandler = new LCBusinessHandler(Looper.getMainLooper()) {

            @Override
            public void handleBusiness(Message msg) {
                addShareUserDevice(openId, deviceId);
//                Toast.makeText(getContext(), getString(R.string.lc_common_process_ok), Toast.LENGTH_SHORT).show();
            }
        };
        DeviceShareManager.newInstance().addUserPolicy(openId, sn, permission, policyHandler);
    }

    private void clearUserPolicy(final String openId, final int groupId){
        LCBusinessHandler policyHandler = new LCBusinessHandler(Looper.getMainLooper()) {

            @Override
            public void handleBusiness(Message msg) {

                removeShareUserDevice(groupId);

                // 공유 해제된 사용자의 기존 등록된 카메라 추가 ( clearPolicy 를 할경우 모든 카메라가 사라지기 때문에... )
                restoreExistsCameraPolicy(openId);

                GlobalFunc.showToast(getContext(), getString(R.string.lc_common_process_ok));
                DialogUtils.dismiss();
            }
        };
        DeviceShareManager.newInstance().clearUserPolicy(openId, policyHandler);
    }

    private void restoreExistsCameraPolicy(String openId) {
        // 공유 해제된 사용자의 기존 등록된 카메라 추가 ( clearPolicy 를 할경우 모든 카메라가 사라지기 때문에... )
        for(DeviceInfo deviceInfo : deviceInfoList) {
            if(deviceInfo.deviceId.equalsIgnoreCase(deviceId)) continue;
            addDeviceToPolicy(openId, deviceInfo.deviceId, deviceInfo.permission);
        }
    }

    private String getEmail() {
        return etEmail.getText().toString().trim();
    }

    private void getOpenIdByAccount(){
        final String email = getEmail();
        DeviceShareManager.newInstance().getOpenIdByAccount(email, new IShareUserDataCallBack() {
            @Override
            public void onCallBackOpenId(String str) {
                DialogUtils.dismiss();

                // addpolicy 처리
                addDeviceToPolicy(str, deviceId, sharedPermission);
                // server처리
//                addShareUserDevice();
            }

            @Override
            public void onError(Throwable throwable) {
                String msg = throwable.getMessage();
                if(msg.contains("SUB1008")) {
                    GlobalFunc.showToast(getContext(), getString(R.string.lc_not_exists_account_text2));
//                    /** 가입되지 않은 회원의 경우 미리 회원가입 후 나중에 가입시 자동 공유 처리 **/
//                    String email = getEmail();
//                    String text = getString(R.string.lc_not_exists_account_text);
//                    text = text.replaceAll("::invite_email::", email);
//
//                    ppsConfirmDialog = new PPSConfirmDialog(getContext(), getString(R.string.lc_not_exists_account), text);
//                    ppsConfirmDialog.setOnOkClickLisenter(DeviceDetailShareFragment.this);
//                    ppsConfirmDialog.show();
                } else {
                    Toast.makeText(getContext(), "wrong account", Toast.LENGTH_SHORT).show();
                }
                DialogUtils.dismiss();
            }
        });
    }

    private void createSubAccount() {
        LCBusinessHandler policyHandler = new LCBusinessHandler(Looper.getMainLooper()) {

            @Override
            public void handleBusiness(Message msg) {
                String openId = (String) msg.obj;
                addDeviceToPolicy(openId, deviceId, sharedPermission);
            }
        };

        String email = getEmail();
        DeviceShareManager.newInstance().createSubAccount(email, policyHandler);
    }

    void removeShareUserDevice(final int groupId){
        DialogUtils.show(getActivity());
        final String reqUrl = ACONST.API_DEVICE_UNSHARE;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();
        try {
            reqBodyJson.put("groupId", groupId+"");
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

//                    DialogUtils.dismiss();
                    GlobalFunc.showToast(getContext(), getString(R.string.lc_common_process_ok));
                    getSharedUserList();
                }
            });
        } catch (JSONException e) {
            DialogUtils.dismiss();
            e.printStackTrace();

            GlobalFunc.showToast(getContext(), "error occured.");
        }

        DialogUtils.dismiss();
    }

    void addShareUserDevice(final String openId, final String deviceId){
        DialogUtils.show(getActivity());
        final String reqUrl = ACONST.API_DEVICE_SHARE;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();
        try {
            reqBodyJson.put("email", getEmail());
            reqBodyJson.put("openId", openId);
            reqBodyJson.put("serialnum", deviceId);
            reqBodyJson.put("permission", sharedPermission);
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

                    DialogUtils.dismiss();
//                    GlobalFunc.showToast(getContext(), getString(R.string.lc_common_process_ok));
                    etEmail.setText("");
                    getSharedUserList();
                }
            });
        } catch (JSONException e) {
            DialogUtils.dismiss();
            e.printStackTrace();

            GlobalFunc.showToast(getContext(), "error occured.");
        }

        DialogUtils.dismiss();
    }

    private void turnOffAlarm(String openId){
        LCBusinessHandler policyHandler = new LCBusinessHandler(Looper.getMainLooper()) {

            @Override
            public void handleBusiness(Message msg) {
//                System.out.println(msg);
            }
        };
        DeviceShareManager.newInstance().clearUserPolicy(openId, policyHandler);
    }



    @Override
    public void onShareItemClick(int position) {
        DialogUtils.show(getActivity());

        ShareInfo shareInfo = adapter.getItem(position);
        ppsConfirmDialog = new PPSConfirmDialog(getActivity(), getString(R.string.lc_delete_share), getString(R.string.lc_delete_share_confirm));
        ppsConfirmDialog.setOnOkClickListener(this);
        ppsConfirmDialog.addParam("shareInfo", shareInfo);
        ppsConfirmDialog.show();
        DialogUtils.dismiss();
    }
}
