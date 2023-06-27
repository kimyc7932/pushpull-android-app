package com.pushpull.camapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.openapi.ClassInstanceManager;
import com.common.openapi.DeviceSubAccountListService;
import com.common.openapi.IGetDeviceInfoCallBack;
import com.mm.android.deviceaddmodule.event.DeviceAddEvent;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.eventbus.event.BaseEvent;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.lc.message.MessageDetailActivity;
import com.lechange.common.log.Logger;

import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.cont.CAPABILITY;
import com.mm.android.mobilecommon.pps.model.DeviceInfo;
import com.mm.android.mobilecommon.pps.model.MyEvent;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.opensdk.devicedetail.net.DeviceDetailService;
import com.pushpull.camapp.R;
import com.pushpull.camapp.adapter.DeviceListAdapter;
import com.lechange.opensdk.media.LCOpenSDK_LoginManager;
import com.lechange.opensdk.media.LCOpenSDK_P2PDeviceInfo;
import com.lechange.pulltorefreshlistview.Mode;
import com.lechange.pulltorefreshlistview.PullToRefreshBase;
import com.mm.android.deviceaddmodule.LCDeviceEngine;
import com.mm.android.mobilecommon.AppConsume.ThreadPool;
import com.mm.android.mobilecommon.openapi.HttpSend;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.route.ProviderManager;
import com.mm.android.mobilecommon.utils.LogUtil;
import com.mm.android.mobilecommon.widget.LcPullToRefreshRecyclerView;
import com.opensdk.devicedetail.tools.GsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.mm.android.mobilecommon.route.RoutePathManager.ActivityPath.DeviceListActivityPath;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Route(path = DeviceListActivityPath)
public class DeviceListActivity extends Activity implements IGetDeviceInfoCallBack.ISubAccountDevice<DeviceDetailListData.Response>, PullToRefreshBase.OnRefreshListener2 {
    private static final String TAG = DeviceListActivity.class.getSimpleName();
    private LcPullToRefreshRecyclerView deviceList;
    private RecyclerView mRecyclerView;
    private List<DeviceDetailListData.ResponseData.DeviceListBean> mCameraDatas = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;
    private LinearLayout llAdd;
    private LinearLayout llMultiView, llMypage;
    private LinearLayout llBack;
    private RelativeLayout rlNoDevice;
    private LinearLayout llLogout;
    private List<DeviceDetailListData.ResponseData.DeviceListBean> adapterList = new ArrayList<>();

    int pageSize = 8;
    //乐橙分页index
    public long baseBindId = -1;
    //开放平台分页index
    public long openBindId = -1;
    int pageNum = 1;
    int listOffset = 0;
    private BottomNavigationView navView;
    private Context mContext;
    private Toast toast;
    private boolean isFirst;

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        initView();
        initData();
    }

    void initData() {
        mContext = getApplicationContext();
        EventBus.getDefault().register(this);

//        getPpsRegisteredDeviceList();
        getDeviceList(false);
    }

    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, R.string.lc_common_quit2, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }

    private void initView() {
        isFirst = true;

        llMypage = findViewById(R.id.ll_mypage);
        llMypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("mCameraDatas", (Serializable) mCameraDatas);

                Intent intent = new Intent(DeviceListActivity.this, MyPageActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        llMultiView = findViewById(R.id.ll_multiview);
        llMultiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCameraDatas == null || mCameraDatas.size() -1 < 2) return;

                Bundle bundle = new Bundle();
                bundle.putSerializable(MethodConst.ParamConst.mCameraDatas, (Serializable) mCameraDatas);
                bundle.putSerializable(MethodConst.ParamConst.deviceDetail, mCameraDatas.get(0));

                Intent intent = new Intent(DeviceListActivity.this, DeviceOnlineMediaPlayMultiActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

//        CommonTitle title = (CommonTitle) findViewById(R.id.rl_title);
//        title.setIconRight(R.mipmap.lc_demo_nav_add);
//        title.setTitleCenter(R.string.lc_demo_main_title);
//        title.setOnTitleClickListener(new CommonTitle.OnTitleClickListener() {
//            @Override
//            public void onCommonTitleClick(int id) {
//                if (id == CommonTitle.ID_LEFT) {
//                    finish();
//                } else if (id == CommonTitle.ID_RIGHT) {
//                    try {
//                        LCDeviceEngine.newInstance().addDevice(DeviceListActivity.this);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });

        llAdd = findViewById(R.id.ll_add);
        llAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    LCDeviceEngine.newInstance().addDevice(DeviceListActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        deviceList = findViewById(R.id.device_list);
        deviceList.setOnRefreshListener(this);
        refreshState(false);
        mRecyclerView = deviceList.getRefreshableView();
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(DeviceListActivity.this));
        listOffset = getResources().getDimensionPixelOffset(R.dimen.px_30);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = listOffset;
                // Add top margin only for the first item to avoid double space between items
                if (parent.getChildLayoutPosition(view) == 0) {
                    outRect.top = listOffset;
                }
            }
        });

        toast = Toast.makeText(getApplicationContext(), getString(R.string.lc_ksw_auth_blockchain_server), Toast.LENGTH_SHORT);
        toast.show();
    }

    private boolean isClosedCamera(DeviceDetailListData.ResponseData.DeviceListBean listBean) {
        // 사생활 보호 기능 동작 여부 확인
        String deviceId = listBean.deviceId;
        String channelId = listBean.channelList.get(listBean.checkedChannel).channelId;
        boolean isClosedCamera = PreferencesHelper.getInstance(getApplicationContext()).getBoolean(channelId+"_"+deviceId+"_"+ CAPABILITY.CLOSE_CAMERA, false);
        if(isClosedCamera) {
            return true;
        }

        return false;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BaseEvent event) {
        if (event instanceof DeviceAddEvent)
        {
            String code = event.getCode();
            if (DeviceAddEvent.DEVICE_ADDED.equals(code)) { // 디바이스 추가
                Log.d(TAG, "device added !!!!");
                Bundle bundle=((DeviceAddEvent) event).getBundle();
                String sn = bundle.getString("sn");
                final String deviceId = sn;
                String model = bundle.getString("model");
                String devJson = bundle.getString("devjson");
                String devName = bundle.getString("devname");
                Log.d(TAG, "sn= "+sn+", model="+model+", devname="+devName);

                final String reqUrl = ACONST.API_DEVICE_ADD;
                String regBody = "";
                JSONObject reqBodyJson = new JSONObject();
                try {
                    reqBodyJson.put("device_type", "1002");
                    reqBodyJson.put("device_name", devName);
                    reqBodyJson.put("device_json", devJson);
                    reqBodyJson.put("serialnum", sn);
                    reqBodyJson.put("uid", ACONST.LOGIN_UID);
                    regBody = reqBodyJson.toString();

                    Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
                    AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                        @Override
                        public void process(JSONObject param) {
                            if (param == null) {
                                Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                                return;
                            }
                            Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());

                            try {
                                int rescode = param.getInt("rescode");
                                if(rescode != 200)
                                {
                                    // 디바이스 삭제함
                                    DeviceDetailService deviceDetailService = new DeviceDetailService();
                                    deviceDetailService.deletePermission(deviceId,null,null);
                                    GlobalFunc.showToast(mContext, getString(R.string.device_reg_fail));
                                }
                            }catch(JSONException ex){
                            }
                        }
                    });
                } catch (JSONException ex){
                }
            }
        } else if (event instanceof MyEvent) {
            String code = event.getCode();
            if(code.equals(MyEvent.ACTION_DEVICELIST_ACTIVITY_DESTROY)){
                finish();
            }
        }
    }

    private void setAdapter() {

        if (deviceListAdapter == null) {
            deviceListAdapter = new DeviceListAdapter(DeviceListActivity.this, mCameraDatas);
        }
        mRecyclerView.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemClickListener(new DeviceListAdapter.OnItemClickListener() {
            @Override
            public void onAddClick() {
                try {
                    LCDeviceEngine.newInstance().addDevice(DeviceListActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessageClick(int outPosition, int innerPosition) {
                if (mCameraDatas.size() == 0) {
                    return;
                }
                Bundle bundle = new Bundle();
//                bundle.putSerializable("mCameraDatas", (Serializable) mCameraDatas);
//                Intent intent = new Intent(DeviceListActivity.this, NoticeActivity.class);
//                intent.putExtras(bundle);
//                startActivity(intent);

                bundle.putString("deviceId", mCameraDatas.get(outPosition).deviceId);
                bundle.putString("channelId", "" + innerPosition);
                bundle.putSerializable("deviceListBean", mCameraDatas.get(outPosition));

                if (mCameraDatas.get(outPosition).channelList.size() > 1) {
                    bundle.putString("deviceName", mCameraDatas.get(outPosition).channelList.get(innerPosition).channelName);
                } else {
                    bundle.putString("deviceName", mCameraDatas.get(outPosition).deviceName);
                }
                Intent intent = new Intent(DeviceListActivity.this, MessageDetailActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onSettingClick(int position) {
                if (mCameraDatas.size() == 0) {
                    return;
                }
                ProviderManager.getDeviceDetailProvider().gotoDeviceDetails(DeviceListActivity.this, GsonUtils.toJson(mCameraDatas.get(position)), MethodConst.ParamConst.fromList);
            }

            @Override
            public void onDetailClick(int position) {
                if (mCameraDatas.size() == 0) {
                    return;
                }
                if (!mCameraDatas.get(position).getDeviceStatus().equals("online")) {
                    return;
                }

                // 사생활 보호 기능 동작 여부 확인
                DeviceDetailListData.ResponseData.DeviceListBean listBean = mCameraDatas.get(position);
                if(isClosedCamera(listBean)) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.lc_close_camera_not_play), Toast.LENGTH_SHORT).show();
                    return;
                }

                Logger.e("998877", "DeviceListActivity onChannelClick");
                Gson gson = new Gson();
                String json = gson.toJson(mCameraDatas.get(position));
                ProviderManager.getPreviewProvider().gotoPrevew(json);
            }

            @Override
            public void onChannelClick(int outPosition, int innerPosition) {
                if (mCameraDatas.size() == 0) {
                    return;
                }
                if (!mCameraDatas.get(outPosition).channelList.get(innerPosition).channelStatus.equals("online")) {
                    return;
                }
                Logger.e("998877", "DeviceListActivity onChannelClick");
                DeviceDetailListData.ResponseData.DeviceListBean deviceListBean = mCameraDatas.get(outPosition);
                deviceListBean.checkedChannel = innerPosition;
                Gson gson = new Gson();
                String json = gson.toJson(deviceListBean);
                ProviderManager.getPreviewProvider().gotoPrevew(json);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setAdapter();
                refreshMode(Mode.PULL_FROM_START);
                refreshState(true);
            }
        }, 200);
    }

    @Override
    public void DeviceList(DeviceDetailListData.Response responseData) {
        if (isDestroyed()) {
            return;
        }
        if(deviceListAdapter != null) {
            deviceListAdapter.notifyDataSetChanged();
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        refreshState(false);
        if (responseData.baseBindId != -1) {
            baseBindId = responseData.baseBindId;
        }
        if (responseData.openBindId != -1) {
            openBindId = responseData.openBindId;
        }
        if (responseData.data != null && responseData.data.deviceList != null && responseData.data.deviceList.size() != 0) {
            Iterator<DeviceDetailListData.ResponseData.DeviceListBean> iterator = responseData.data.deviceList.iterator();
            while (iterator.hasNext()) {
                DeviceDetailListData.ResponseData.DeviceListBean next = iterator.next();
                if (next.channelList != null && next.channelList.size() == 0 && !next.catalog.contains("NVR")) {
                    // 使用迭代器中的remove()方法,可以删除元素.
                    iterator.remove();
                }
            }
        }
        //没有数据
        if ((responseData.data == null || responseData.data.deviceList == null || responseData.data.deviceList.size() == 0) && mCameraDatas.size() == 0) {
            // no data
            //本次未拉到数据且上次也没有数据
//            deviceListAdapter.notifyDataSetChanged();
            DialogUtils.dismiss();
            this.getPpsRegisteredDeviceList();
        } else {
            if ((responseData.data == null || responseData.data.deviceList == null || responseData.data.deviceList.size() == 0)) {//本次未拉到数据但上次有数据
                if (pageNum == 1) {
                    mCameraDatas.clear();
                    addEmptyCameraView();
                    deviceListAdapter.notifyDataSetChanged();
                }
                return;
            }
            deviceList.setVisibility(View.VISIBLE);
            if (pageNum == 1) {
                mCameraDatas.clear();
            }
            mCameraDatas.addAll(responseData.data.deviceList);

            addEmptyCameraView();

            deviceListAdapter.notifyDataSetChanged();
            if (mCameraDatas.size() >= 8) {
                refreshMode(Mode.BOTH);
            } else {
                refreshMode(Mode.PULL_FROM_START);
            }
            ThreadPool.submit(() -> {
                try {
                    List<LCOpenSDK_P2PDeviceInfo> deviceInfos = new ArrayList<>();
                    for (DeviceDetailListData.ResponseData.DeviceListBean deviceListBean : responseData.data.deviceList) {
                        if (TextUtils.isEmpty(deviceListBean.playToken)) {
                            continue;
                        }
                        LCOpenSDK_P2PDeviceInfo p2PDeviceInfo = new LCOpenSDK_P2PDeviceInfo();
                        p2PDeviceInfo.playToken = deviceListBean.playToken;
                        p2PDeviceInfo.did = deviceListBean.deviceId;
                        deviceInfos.add(p2PDeviceInfo);
                    }
                    int rst=LCOpenSDK_LoginManager.addDevices(TokenHelper.getInstance().subAccessToken, deviceInfos);
                    Logger.e("addDevices 返回信息：", ""+rst);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            this.getPpsRegisteredDeviceList();

        }
    }

    private void addEmptyCameraView() {
        DeviceDetailListData.ResponseData.DeviceListBean emptyDeviceListBean = new DeviceDetailListData.ResponseData.DeviceListBean();
        emptyDeviceListBean.deviceName = getString(R.string.lc_ksw_add_camera);
        emptyDeviceListBean.deviceStatus = "EMPTY";
        emptyDeviceListBean.deviceAbility = "EMPTY";
        mCameraDatas.add(emptyDeviceListBean);
    }

    @Override
    public void onError(Throwable throwable) {
        if (isDestroyed()) {
            return;
        }
        refreshState(false);
        LogUtil.errorLog(TAG, "error", throwable);
        if(TextUtils.equals(throwable.getMessage(),String.valueOf(HttpSend.NET_ERROR_CODE))){
            Toast.makeText(DeviceListActivity.this, R.string.mobile_common_operate_fail, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(DeviceListActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase pullToRefreshBase) {
        pageNum = 1;
        getDeviceList(false);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase pullToRefreshBase) {
        pageNum = pageNum + 1;
        getDeviceList(true);
    }

    private void getDeviceList(boolean isLoadMore) {
        if (!isLoadMore) {
            baseBindId = -1;
            openBindId = -1;
//            datas.clear();
        }
        DeviceSubAccountListService deviceSubAccountListService = ClassInstanceManager.newInstance().getDeviceSubAccountListService();
        deviceSubAccountListService.getSubAccountDeviceList(pageNum, this);


      /*  DeviceListService deviceVideoService = ClassInstanceManager.newInstance().getDeviceListService();
        DeviceListData deviceListData = new DeviceListData();
        deviceListData.data.openBindId = this.openBindId;
        deviceListData.data.baseBindId = this.baseBindId;
        deviceVideoService.deviceBaseList(deviceListData, DeviceListActivity.this);*/
    }

    private void refreshState(boolean refresh) {
        if (refresh) {
            deviceList.setRefreshing(true);
        } else {
            if(mCameraDatas != null) mCameraDatas.clear();
            addEmptyCameraView();
            if(deviceListAdapter != null) {
                deviceListAdapter.notifyDataSetChanged();
            }
            deviceList.onRefreshComplete();
        }
    }

    private void refreshMode(Mode mode) {
        deviceList.setMode(mode);
    }



    private void getPpsRegisteredDeviceList() {
        final String reqUrl = ACONST.API_DEVICE_GET;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();
        try {
            reqBodyJson.put("uid", ACONST.LOGIN_UID);
            regBody = reqBodyJson.toString();

            mCameraDatas.addAll(adapterList);
            deviceListAdapter.notifyDataSetChanged();

            Log.d(TAG, "<------- REQ: " + reqUrl + ", " + regBody);
            AsyncJSON.requestJSON(reqUrl, regBody, new Processable<JSONObject>() {
                @Override
                public void process(JSONObject param) {
                    try {
                        if(param == null){
                            Log.e(TAG, "-------> RES: " + reqUrl + " param is null");
                            for(int i=0; i < mCameraDatas.size(); i++){
                                DeviceDetailListData.ResponseData.DeviceListBean device = mCameraDatas.get(i);
                                if(!device.deviceStatus.equals("offline")) device.deviceStatus = "blockchain-fail";
                            }
                            deviceListAdapter.notifyDataSetChanged();
                            DialogUtils.dismiss();
                            return;
                        }

                        Log.d(TAG, "-------> RES: " + reqUrl + " "+param.toString());
                        int rescode = param.getInt("rescode");
                        if(rescode == 200) {
                            ArrayList<DeviceInfo> regDeviceList = new ArrayList<DeviceInfo>();
                            JSONArray jarrayList = param.getJSONArray("data");
                            for(int i = 0; i < jarrayList.length(); i++){
                                JSONObject jobj = (JSONObject)jarrayList.get(i);
                                DeviceInfo info = new DeviceInfo();
                                info.groupId = jobj.getInt("group_id");
                                info.deviceId = jobj.getString("device_id");
                                info.deviceName = jobj.getString("device_name");
                                info.deviceType = jobj.getInt("device_type");
                                info.deviceSerial = jobj.getString("serialnum");
                                regDeviceList.add(info);

//                                System.out.println("*********************************");
//                                System.out.println("*********************************:: " + info.toString());
//                                System.out.println("*********************************");

                            }

                            ACONST.regDeviceList=regDeviceList;

                            // 서버에 정보가 없으면 offline 처리함
                            for(int i=0; i < mCameraDatas.size(); i++){
                                DeviceDetailListData.ResponseData.DeviceListBean device = mCameraDatas.get(i);
                                if(device.deviceStatus.equals("EMPTY")) continue;
                                if( !isRegistedDevice(device.deviceId)) {
                                    device.deviceStatus = "offline";
                                    device.deviceName += "!";
                                }
                            }
                            if(isFirst) {
                                toast.cancel();
                                toast = Toast.makeText(getApplicationContext(), getString(R.string.lc_ksw_authfin_blockchain_server), Toast.LENGTH_SHORT);
                                toast.show();
                                isFirst = false;
                            }
                        } else {
                            ACONST.regDeviceList=null;

                            // 공유된 장비는 offline 처리 안함.
                            for(int i=0; i < mCameraDatas.size(); i++){
                                DeviceDetailListData.ResponseData.DeviceListBean device = mCameraDatas.get(i);
                                device.deviceStatus = "offline";
                            }
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                        Log.d("DeviceListActivity", "getPpsRegisteredDeviceList.exception.e1 --- " + e1.getMessage());
                    }

                    deviceListAdapter.notifyDataSetChanged();
                    DialogUtils.dismiss();
//                    if(toast != null) {
//                        toast.cancel();
//                        toast = null;
//                    }

                } // end process
            });
        } catch (JSONException e2) {
            e2.printStackTrace();
            Log.d("DeviceListActivity", "getPpsRegisteredDeviceList.exception.e1 --- " + e2.getMessage());
        }
    }


    // 서버의 등록된 카메라정보와 비교한다.
    private boolean isRegistedDevice(String deviceId){
        if(ACONST.regDeviceList == null)
            return false;

        for(int i=0; i < ACONST.regDeviceList.size(); i++){
            DeviceInfo device = ACONST.regDeviceList.get(i);
            if(device.deviceSerial.contains(deviceId)){
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
