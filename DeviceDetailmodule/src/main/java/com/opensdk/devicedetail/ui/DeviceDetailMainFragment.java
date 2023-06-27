package com.opensdk.devicedetail.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mm.android.deviceaddmodule.LCDeviceEngine;
import com.mm.android.deviceaddmodule.device_wifi.CurWifiInfo;
import com.mm.android.deviceaddmodule.device_wifi.DeviceConstant;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.entity.device.LCDevice;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.manager.TimeZoneManager;
import com.mm.android.mobilecommon.pps.model.TimeZoneInfo;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.service.DeviceTimezoneService;
import com.mm.android.mobilecommon.pps.service.IDeviceOperationCallback;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.pps.utils.StringUtils;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.LogUtil;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.opensdk.devicedetail.dialog.CommonDialog;
import com.opensdk.devicedetail.entity.DevSdStoreData;
import com.opensdk.devicedetail.R;
import com.opensdk.devicedetail.callback.IGetDeviceInfoCallBack;
import com.opensdk.devicedetail.entity.DeviceOperationService;
import com.opensdk.devicedetail.entity.DeviceUnBindData;
import com.opensdk.devicedetail.manager.DetailInstanceManager;
import com.opensdk.devicedetail.net.DeviceDetailService;
import com.opensdk.devicedetail.tools.GsonUtils;

import java.text.DecimalFormat;

import static android.app.Activity.RESULT_OK;
import static com.mm.android.deviceaddmodule.device_wifi.DeviceConstant.IntentCode.DEVICE_SETTING_WIFI_LIST;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceDetailMainFragment extends Fragment implements View.OnClickListener, IGetDeviceInfoCallBack.IUnbindDeviceCallBack, IGetDeviceInfoCallBack.IModifyDeviceName, IGetDeviceInfoCallBack.ITimeZoneInfo  {
    private static final String TAG = DeviceDetailMainFragment.class.getSimpleName();
    private static final float OFFLINE_ALPHA = 0.5f;//离线状态透明度

    private RelativeLayout rlDeviceDetail;
    private RelativeLayout rlDetailVersion;
    private RelativeLayout rlDeployment;
    private RelativeLayout rlDetele;
    private RelativeLayout rlSdcard;
    private RelativeLayout rlTimezone;
    private RelativeLayout rlShare;

    private TextView tvDeviceName;
    private ImageView ivDevicePic;
    private TextView tvDeviceVersion;
    private Bundle arguments;
    private DeviceDetailListData.ResponseData.DeviceListBean deviceListBean;
    private DeviceDetailActivity deviceDetailActivity;
    private DeviceDetailService deviceDetailService;
    private CurWifiInfo wifiInfo;
    private TextView tvCurrentWifi;
    private RelativeLayout rlCurWifi;
    private IGetDeviceInfoCallBack.IModifyDeviceName modifyNameListener;
    private String fromWhere;
    private TextView tvUseSpace;
    private TextView tvDeviceSettingTip;
    private ProgressBar pbStore;
    private TextView tvTimezoneInfo;
    private String deviceId;
    private String channelId;
    private TextView tvSdcardInfo;
    String sdCardStatus = "";
    
    public static DeviceDetailMainFragment newInstance() {
        DeviceDetailMainFragment fragment = new DeviceDetailMainFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.llOperate.setVisibility(View.GONE);
        arguments = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_detail_main_new, container, false);
    }

    public void setModifyNameListener(IGetDeviceInfoCallBack.IModifyDeviceName modifyNameListener) {
        this.modifyNameListener = modifyNameListener;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rlSdcard = view.findViewById(R.id.rl_sdcard);
        rlDeviceDetail = view.findViewById(R.id.rl_device_detail);
        rlDetailVersion = view.findViewById(R.id.rl_detail_version);
        rlDeployment = view.findViewById(R.id.rl_deployment);
        rlDetele = view.findViewById(R.id.rl_detele);
        tvDeviceName = view.findViewById(R.id.tv_device_name);
        ivDevicePic = view.findViewById(R.id.iv_device_pic);
        tvDeviceVersion = view.findViewById(R.id.tv_device_version);
        tvCurrentWifi = view.findViewById(R.id.tv_current_wifi);
        rlCurWifi = view.findViewById(R.id.rl_cur_wifi);
        tvTimezoneInfo = view.findViewById(R.id.tv_timezone_info);
        rlTimezone = view.findViewById(R.id.rl_timezone);
        rlShare = view.findViewById(R.id.rl_share);
        tvSdcardInfo = view.findViewById(R.id.tv_sdcard_info);

        tvUseSpace = view.findViewById(R.id.tv_use_space);
        pbStore = view.findViewById(R.id.pb_store);

        rlDeployment.setOnClickListener(this);
        rlDetele.setOnClickListener(this);
        rlDeviceDetail.setOnClickListener(this);
        rlCurWifi.setOnClickListener(this);
        rlDetailVersion.setOnClickListener(this);
        rlTimezone.setOnClickListener(this);
        rlShare.setOnClickListener(this);

        DeviceDetailActivity deviceDetailActivity = (DeviceDetailActivity) getActivity();
        deviceDetailActivity.tvTitle.setText(getResources().getString(R.string.lc_demo_device_detail_title));
        if (arguments == null) {
            return;
        }
        String deviceListStr = arguments.getString(MethodConst.ParamConst.deviceDetail);
        if (TextUtils.isEmpty(deviceListStr)) {
            return;
        }

        deviceListBean = GsonUtils.fromJson(deviceListStr, DeviceDetailListData.ResponseData.DeviceListBean.class);
        this.deviceId = deviceListBean.deviceId;
        DeviceDetailListData.ResponseData.DeviceListBean.ChannelsBean channelsBean = deviceListBean.channelList.get(deviceListBean.checkedChannel);
        this.channelId = channelsBean.channelId;

        tvSdcardInfo.setText(R.string.lc_sdcard_not_exists);
        getTimezone();

        try {
            checkStorage();
        } catch(Exception ex) {}
        
        //不为空 列表页跳转
        fromWhere = arguments.getString(MethodConst.ParamConst.fromList);
        if (deviceListBean == null) {
            return;
        }
        deviceDetailService = DetailInstanceManager.newInstance().getDeviceDetailService();
        if (deviceListBean.channelList != null && deviceListBean.channelList.size() > 1) {
            //多通道有挂载通道  （布防，云存储，网络配置都不展示）
            rlDeployment.setVisibility(View.GONE);
            rlCurWifi.setVisibility(View.GONE);
            tvDeviceSettingTip.setVisibility(View.GONE);
            if (MethodConst.ParamConst.fromList.equals(fromWhere)) {
                //设备详情
                tvDeviceName.setText(deviceListBean.deviceName);
                tvDeviceVersion.setText(deviceListBean.deviceVersion);
            } else {
                //通道详情   暂时显示的是设备详情
                tvDeviceName.setText(deviceListBean.deviceName);
                tvDeviceVersion.setText(deviceListBean.deviceVersion);
            }
        } else if (deviceListBean.channelList != null && deviceListBean.channelList.size() == 1) {
            //单通道
            tvDeviceName.setText(deviceListBean.channelList.get(deviceListBean.checkedChannel).channelName);
            getDeviceLocalCache();
            tvDeviceVersion.setText(deviceListBean.deviceVersion);
            if (deviceListBean.deviceSource == 2) {
                rlDetele.setVisibility(View.GONE);
            }
        } else {
            //多通道但是没有挂载通道 （布防，云存储，网络配置都不展示）
            tvDeviceName.setText(deviceListBean.deviceName);
            tvDeviceVersion.setText(deviceListBean.deviceVersion);
            rlDeployment.setVisibility(View.GONE);
            rlCurWifi.setVisibility(View.GONE);
            tvDeviceSettingTip.setVisibility(View.GONE);
        }

    }

    private void checkStorage() {
        String openid = LCDeviceEngine.newInstance().openid;
        final String deviceId = deviceListBean.deviceId;
        String time = null;
        DeviceOperationService.getInstance().deviceSdcardStatus(openid, deviceId, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "checkStorage.onSuccess -----------------------------------");
                Log.d(TAG, "checkStorage : " + response);
                Log.d(TAG, "checkStorage.onSuccess -----------------------------------");

                if(response == null || response.equals("")) return;
                Log.d(TAG, "sdcard.checkStorage : " + response);
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);
                Log.d(TAG, "sdcard.sdCardStatus1 :: "+ sdCardStatus);
                sdCardStatus = resultJson.get("status").getAsString();
                Log.d(TAG, "sdcard.sdCardStatus2 :: "+ sdCardStatus);

                Log.d(TAG, "sdcard.sdCardStatus3 :: >>"+ sdCardStatus + ":::normal<<");

                if(sdCardStatus.equals("normal")) {
                    rlSdcard.setOnClickListener(DeviceDetailMainFragment.this);
                    tvSdcardInfo.setText(getResources().getString(R.string.lc_sdcard_storage));
                    deviceStorage();
                } else if(sdCardStatus.equals("abnormal")) {
                    rlSdcard.setOnClickListener(DeviceDetailMainFragment.this);
                    Log.d(TAG, "abnomal. 글자 셋팅");
                    tvSdcardInfo.setText(R.string.lc_sdcard_abnormal);
                } else {
                    Log.d(TAG, "저장장치가 없습니다. 글자 셋팅");
                    tvSdcardInfo.setText(R.string.lc_sdcard_not_exists);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "checkStorage.onError -----------------------------------");
                Log.d(TAG, "checkStorage : "+ throwable.getMessage());
                Log.d(TAG, "checkStorage.onError -----------------------------------");
                tvSdcardInfo.setText(R.string.lc_sdcard_not_exists);
                sdCardStatus = null;
            }
        });
    }


    private void deviceStorage() {
        final String deviceId = deviceListBean.deviceId;
        DeviceOperationService.getInstance().deviceStorage(deviceId, new IDeviceOperationCallback() {
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

    private void getDeviceLocalCache() {
//        DeviceLocalCacheData deviceLocalCacheData = new DeviceLocalCacheData();
//        deviceLocalCacheData.setDeviceId(deviceListBean.deviceId);
//        if (deviceListBean.channelList != null && deviceListBean.channelList.size() > 0) {
//            deviceLocalCacheData.setChannelId(deviceListBean.channelList.get(deviceListBean.checkedChannel).channelId);
//        }
//        DeviceLocalCacheService deviceLocalCacheService = ClassInstanceManager.newInstance().getDeviceLocalCacheService();
//        deviceLocalCacheService.findLocalCache(deviceLocalCacheData, this);
    }

    private void getCurrentWifiInfo() {
        deviceDetailService.currentDeviceWifi(deviceListBean.deviceId, new IGetDeviceInfoCallBack.IDeviceCurrentWifiInfoCallBack() {
            @Override
            public void deviceCurrentWifiInfo(CurWifiInfo curWifiInfo) {
                if (!isAdded() || curWifiInfo == null) {
                    return;
                }
                rlCurWifi.setVisibility(View.VISIBLE);
                if (curWifiInfo.isLinkEnable()) {
                    wifiInfo = curWifiInfo;
                    tvCurrentWifi.setText(wifiInfo.getSsid());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                DeviceDetailMainFragment.this.onError(throwable);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void gotoModifyNamePage(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null || fragmentActivity.getSupportFragmentManager() == null) {
            return;
        }
        DeviceDetailNameFragment fragment = DeviceDetailNameFragment.newInstance();
        fragment.setArguments(arguments);
        fragment.setModifyNameListener(this);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    public void gotoUpdatePage(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null || fragmentActivity.getSupportFragmentManager() == null) {
            return;
        }
        com.opensdk.devicedetail.ui.DeviceDetailVersionFragment fragment = com.opensdk.devicedetail.ui.DeviceDetailVersionFragment.newInstance();
        fragment.setArguments(arguments);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    public void gotoDeploymentPage(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null || fragmentActivity.getSupportFragmentManager() == null) {
            return;
        }
        DeviceDetailDeploymentFragment fragment = DeviceDetailDeploymentFragment.newInstance();
        fragment.setArguments(arguments);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    public void gotoSdCardPage(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null || fragmentActivity.getSupportFragmentManager() == null) {
            return;
        }
//        SdCardFragment fragment = SdCardFragment.Companion.newInstance();
//        fragment.setArguments(arguments);
//        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
//        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
//        transaction.commitAllowingStateLoss();
        arguments.putSerializable(MethodConst.ParamConst.deviceDetail, deviceListBean);

        DeviceDetailStorageFragment fragment = DeviceDetailStorageFragment.newInstance();
        fragment.setArguments(arguments);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_device_detail) {
            gotoModifyNamePage(getActivity());
        } else if (id == R.id.rl_cur_wifi) {
            LCDevice device = new LCDevice();
            device.setDeviceId(deviceListBean.deviceId);
            device.setName(deviceListBean.deviceName);
            device.setStatus(deviceListBean.getDeviceStatus());
            LCDeviceEngine.newInstance().deviceOnlineChangeNet(getActivity(), device, wifiInfo);
        } else if (id == R.id.rl_deployment) {
            gotoDeploymentPage(getActivity());
        } else if (id == R.id.rl_detail_version) {
            gotoUpdatePage(getActivity());
        } else if (id == R.id.rl_detele) {
            //解绑设备
            deleteDevice();
        } else if (id == R.id.rl_sdcard) {
            gotoSdCardPage(getActivity());
        } else if (id == R.id.rl_timezone) {
            gotoTimeZonePage(getActivity());
        } else if (id == R.id.rl_share) {
            gotoSharePage(getActivity());
        }
    }


    void deleteCameraFromDB(){
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
                            deviceDetailActivity.rlLoading.setVisibility(View.VISIBLE);
                            deviceDetailService.deletePermission(deviceListBean.deviceId,null,DeviceDetailMainFragment.this);
                        } else{
                            GlobalFunc.showToast(getContext(), getString(R.string.device_delete_fail));
                        }
                    } catch (JSONException ex){
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void gotoSharePage(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null || fragmentActivity.getSupportFragmentManager() == null) {
            return;
        }

        arguments.putSerializable(MethodConst.ParamConst.deviceDetail, deviceListBean);
        DeviceDetailShareFragment fragment = DeviceDetailShareFragment.newInstance();
        fragment.setArguments(arguments);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    public void gotoTimeZonePage(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null || fragmentActivity.getSupportFragmentManager() == null) {
            return;
        }

        arguments.putSerializable(MethodConst.ParamConst.deviceDetail, deviceListBean);
        DeviceDetailTimeZoneFragment fragment = DeviceDetailTimeZoneFragment.newInstance();
        fragment.setArguments(arguments);
        fragment.setTimeZoneListener(this);
        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        transaction.hide(this).add(R.id.fr_content, fragment).addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    //    @Override
//    public void deviceCache(DeviceLocalCacheData deviceLocalCacheData) {
//        if (!isAdded()) {
//            return;
//        }
//        BitmapDrawable bitmapDrawable = MediaPlayHelper.picDrawable(deviceLocalCacheData.getPicPath());
//        if (bitmapDrawable != null) {
//            ivDevicePic.setImageDrawable(bitmapDrawable);
//        }
//    }
    /**
     * Delete Device
     *
     * 删除设备
     */
    public void deleteDevice(){
        CommonDialog dialog = new CommonDialog.Builder(getActivity())
                .setTitle(R.string.device_delete_tip)
                .setMessage(R.string.device_delete_confirm_msg)
                .setGravity2(Gravity.CENTER)
                .setCancelButton(R.string.common_cancel, null)
                .setConfirmButton(R.string.common_confirm, (new com.opensdk.devicedetail.dialog.CommonDialog.OnClickListener() {
                    public final void onClick(CommonDialog dialog, int which, boolean isChecked) {
                        DeviceDetailService deviceDetailService = DetailInstanceManager.newInstance().getDeviceDetailService();
                        DialogUtils.show(getActivity());
                            /*
                              DeviceUnBindData deviceUnBindData = new DeviceUnBindData();
                              deviceUnBindData.data.deviceId = deviceListBean.deviceId;
                              deviceDetailService.unBindDevice(deviceUnBindData, this);
                              */

                        deleteCameraFromDB();
                    }
                }))
                .create();
        dialog.show(getChildFragmentManager(), null);
    }
    @Override
    public void unBindDevice(boolean result) {
        if (!isAdded()) {
            return;
        }
        DeviceUnBindData deviceUnBindData = new DeviceUnBindData();
        deviceUnBindData.data.deviceId = deviceListBean.deviceId;
        deviceDetailService.unBindDevice(deviceUnBindData, this);
        DialogUtils.dismiss();
        Toast.makeText(getContext(), getResources().getString(R.string.lc_demo_device_unbind_success), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra(DeviceConstant.IntentKey.LCDEVICE_UNBIND, true);
        deviceDetailActivity.setResult(RESULT_OK, intent);
        deviceDetailActivity.finish();
    }

    @Override
    public void onError(Throwable throwable) {
        if (!isAdded()) {
            return;
        }
        deviceDetailActivity.rlLoading.setVisibility(View.GONE);
        LogUtil.errorLog(TAG, "error", throwable);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVICE_SETTING_WIFI_LIST && resultCode == RESULT_OK && data != null) {
            CurWifiInfo curWifiInfo = (CurWifiInfo) data.getSerializableExtra(DeviceConstant.IntentKey.DEVICE_CURRENT_WIFI_INFO);
            if (curWifiInfo != null) {
                wifiInfo = curWifiInfo;
                tvCurrentWifi.setText(wifiInfo.getSsid());
            }
        }
    }

    @Override
    public void deviceName(String newName) {
        tvDeviceName.setText(newName);
        //多通道设备详情
//        if (deviceListBean.channelList.size() == 0 || (deviceListBean.channelList.size() > 1 && MethodConst.ParamConst.fromList.equals(fromWhere))) {
        if (deviceListBean.channelList.size() == 0 || deviceListBean.channelList.size() > 1) {
            deviceListBean.deviceName = newName;
        } else {
            deviceListBean.channelList.get(deviceListBean.checkedChannel).channelName = newName;
        }
        arguments.putString(MethodConst.ParamConst.deviceDetail,GsonUtils.toJson(deviceListBean));
        if (modifyNameListener != null) {
            modifyNameListener.deviceName(newName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
    }

    /**
     * Update the progress bar and storage information
     * @param total  Total Storage
     * @param used  Used Storage
     * @param progress  Progress
     *
     * 更新进度条和存储信息
     * @param total  总内存
     * @param used  已使用内存
     * @param progress  进度
     */
    public void updatePbStore(long total, long used, int progress) {
        //更新进度条
        pbStore.setProgress(progress);
        tvUseSpace.setText(String.format(getString(R.string.sd_use_space_gb), getSizeGB(used), getSizeGB(total)));//已使用：used  总：total
        tvUseSpace.setVisibility(View.VISIBLE);
    }

    /**
     * Convert "byte" to "GB" and retain two decimal places
     * @param num  Long Numbers
     *
     * 将“byte”转化为“GB”,并保留两位小数
     * @param num  Long类型数字
     */
    public static String getSizeGB(long num) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format((float) num / (1024 * 1024 * 1024));
    }

    /**
     * View the offline state
     *
     * 显示离线状态View
     */
    private void showOfflineView() {
        //组件置灰
        changeViewAlpha(rlDeviceDetail, OFFLINE_ALPHA);
        changeViewAlpha(rlDetailVersion, OFFLINE_ALPHA);
        changeViewAlpha(rlDeployment, OFFLINE_ALPHA);
        changeViewAlpha(rlSdcard, OFFLINE_ALPHA);
        changeViewAlpha(rlCurWifi, OFFLINE_ALPHA);

        //置于不可点击状态
        rlDeviceDetail.setClickable(false);
        rlDetailVersion.setClickable(false);
        rlDeployment.setClickable(false);
        rlSdcard.setClickable(false);
        rlCurWifi.setClickable(false);

        //SD卡离线状态设置
        tvUseSpace.setVisibility(View.INVISIBLE);
    }

    /**
     * Change the child View transparency
     * @param viewGroup  ViewGroup subclass
     * @param alpha  transparency
     *
     * 改变子View透明度
     * @param viewGroup  ViewGroup子类
     * @param alpha  透明度
     */
    private void changeViewAlpha(ViewGroup viewGroup, Float alpha) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                changeViewAlpha((ViewGroup) child, alpha);
            } else {
                child.setAlpha(alpha);
            }
        }
    }

    @Override
    public void timeZoneInfo(String response) {
        getTimezone();
    }


    public void getTimezone() {
        String openid = LCDeviceEngine.newInstance().openid;
        final String deviceId = deviceListBean.deviceId;

        String timeZoneStr = PreferencesHelper.getInstance(getContext()).getString(deviceId+"_"+ ACONST.TIME_ZONE, "");
        if(timeZoneStr != null && !timeZoneStr.equals("")) {
            tvTimezoneInfo.setText(timeZoneStr);
            return;
        }

        DeviceTimezoneService.getInstance().getTimezone(openid, deviceId, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "getTimezone.onSuccess -----------------------------------");
                Log.d(TAG, "getTimezone : " + response);
                Log.d(TAG, "getTimezone.onSuccess -----------------------------------");

                tvTimezoneInfo.setText("None");
                if(response == null || response.equals("")) return;
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);
                if(!resultJson.has("areaIndex")) return ;
                String areaIndex = resultJson.get("areaIndex").getAsString();
                TimeZoneInfo timeZoneInfo = TimeZoneManager.getInstance().getTimeZoneInfo(getContext(), areaIndex);
                if(timeZoneInfo == null) return;
                tvTimezoneInfo.setText(timeZoneInfo.getUtc() + " " + timeZoneInfo.getCity());
            }

            @Override
            public void onError(Throwable throwable) {
                Log.d(TAG, "checkStorage.onError -----------------------------------");
                Log.d(TAG, "checkStorage : "+ throwable.getMessage());
                Log.d(TAG, "checkStorage.onError -----------------------------------");
                tvSdcardInfo.setText(R.string.lc_sdcard_not_exists);
                sdCardStatus = null;
            }
        });
    }
}
