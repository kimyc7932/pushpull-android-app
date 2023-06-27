package com.opensdk.devicedetail.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.dialog.PPSConfirmDialog;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.service.IDeviceOperationCallback;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.pps.utils.StringUtils;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.widget.LcProgressBar;
import com.opensdk.devicedetail.R;
import com.opensdk.devicedetail.entity.DeviceOperationService;
import com.opensdk.devicedetail.entity.DeviceVersionListData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class DeviceDetailStorageFragment extends Fragment implements View.OnClickListener, PPSConfirmDialog.OnOkClickListener {
    private static final String TAG = DeviceDetailStorageFragment.class.getSimpleName();
    private Bundle arguments;
    private LcProgressBar pgFormat;
    private TextView tvSdcardInfo;
    private Button btnDeviceStorage;
    private DeviceDetailActivity deviceDetailActivity;
    private DeviceOperationService deviceOperationService;
    private DeviceDetailListData.ResponseData.DeviceListBean deviceListBean;
    public boolean isFinish = false;
    private DeviceVersionListData deviceVersionListData;
    public Handler taskHandler;
    private PPSConfirmDialog ppsConfirmDialog;
    private String deviceId;

    public DeviceDetailStorageFragment() {
    }

    public static DeviceDetailStorageFragment newInstance() {
        DeviceDetailStorageFragment fragment = new DeviceDetailStorageFragment();
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
        return inflater.inflate(R.layout.fragment_device_storage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DeviceDetailActivity deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.tvTitle.setText(getResources().getString(R.string.lc_sdcard_text));
        initView(view);
        initData();
    }

    private void initView(View view) {
        tvSdcardInfo = view.findViewById(R.id.tv_sdcard_info);
        pgFormat = view.findViewById(R.id.pg_format);
        pgFormat.setText(getResources().getString(R.string.lc_sdcard_format_text));
        pgFormat.setOnClickListener(this);
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
        deviceOperationService =  new DeviceOperationService();

        try {
            deviceStorage();
        } catch(Exception ex) {

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void deviceStorage() {
        tvSdcardInfo.setText(getResources().getString(R.string.lc_sdcard_storage));
        deviceOperationService.deviceStorage(deviceId, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);
                try {
                    String usedBytes = resultJson.get("usedBytes").getAsString();
                    String totalBytes = resultJson.get("totalBytes").getAsString();
                    String used = StringUtils.byteCalculation(usedBytes);
                    String total = StringUtils.byteCalculation(totalBytes);
                    tvSdcardInfo.setText(used +" / " + total);
                } catch(Exception ex) {
                    deviceStorage();
                }

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    private void storageFormatCheck() {
        deviceOperationService.storageFormatCheck(deviceId, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);
                String sdCardStatus = resultJson.get("status").getAsString();
                if(sdCardStatus == null || sdCardStatus.equals("")) return;
                if(sdCardStatus.equals("normal")) {
                    pgFormat.setText(getResources().getString(R.string.lc_sdcard_format_text));
                    Toast.makeText(getContext(), getResources().getString(R.string.lc_sdcard_format_finish_text), Toast.LENGTH_SHORT).show();

                    // 해당 기기 알람내역 삭제 ( db에서 삭제 )
                    alarmListClear(deviceId);
                } else {
                    storageFormatCheck();
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    Toast noticeToast;
    private void alarmListClear(String serialnum) {
        final String reqUrl = ACONST.API_NOTICE_CLEAR;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("serialnum", serialnum);
            reqBodyJson.put("user_id", ACONST.LOGIN_UID);
            regBody = reqBodyJson.toString();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void process(JSONObject param) {
                    try {
                        if (param == null) {
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            GlobalFunc.showToast(getContext(), getString(R.string.network_error));
                            DialogUtils.dismiss();
                            return;
                        }

                        Log.d(TAG, "-------> RES: " + reqUrl + " " + param.toString());

                        DialogUtils.dismiss();

                        int rescode = param.getInt("rescode");
                        if (rescode == 200) {
//                            if(noticeToast != null) noticeToast.cancel();
//                            noticeToast = Toast.makeText(getContext(), getResources().getString(R.string.lc_notice_delete_ok), Toast.LENGTH_SHORT);
//                            noticeToast.show();
                        }
                    } catch (JSONException ex) {
                    } finally {
                        deviceStorage();
                    }
                }
            });
        } catch (JSONException ex){
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.pg_format) {
            if(pgFormat.getText().equals(getResources().getString(R.string.lc_sdcard_formating_text))){
                Toast.makeText(getContext(),getResources().getString(R.string.lc_sdcard_formating_text), Toast.LENGTH_SHORT).show();
                return;
            }

            ppsConfirmDialog = new PPSConfirmDialog(getContext(), getString(R.string.lc_sdcard_format_text), getString(R.string.lc_sdcard_format_dialog));
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

    Toast formatToast;
    @Override
    public void OnOK(String title, Map<String, Object> param) {
        pgFormat.setText(getResources().getString(R.string.lc_sdcard_formating_text));
        tvSdcardInfo.setText(getResources().getString(R.string.lc_sdcard_formating_text));
        if(deviceOperationService==null){
            deviceOperationService =  new DeviceOperationService();
        }
        deviceOperationService.formatStorage(deviceListBean.deviceId, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                if (!isAdded()){
                    return;
                }

                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);
                String sdCardStatus = resultJson.get("result").getAsString();

                if(sdCardStatus.equals("normal")){
                    taskHandler.sendEmptyMessageDelayed(0,5000);
                }else{
//            deviceDetailActivity.rlLoading.setVisibility(View.GONE);
                    storageFormatCheck();
                    if(formatToast != null) formatToast.cancel();
//                    formatToast = Toast.makeText(getContext(), getResources().getString(R.string.lc_sdcard_formating_text), Toast.LENGTH_SHORT);
//                    formatToast.show();
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }
}
