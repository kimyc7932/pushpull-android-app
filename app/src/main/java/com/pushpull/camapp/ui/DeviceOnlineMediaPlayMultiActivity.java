package com.pushpull.camapp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.lc.media.api.DeviceLocalCacheService;
import com.lc.media.entity.DeviceLocalCacheData;
import com.lc.media.handler.ActivityHandler;
import com.lc.media.ui.EncryptKeyInputDialog;
import com.lc.media.ui.LcPopupWindow;
import com.mm.android.mobilecommon.pps.utils.DeviceAbilityHelper;
import com.lc.media.utils.MediaPlayHelper;
import com.lechange.opensdk.listener.LCOpenSDK_EventListener;
import com.lechange.opensdk.media.LCOpenSDK_ParamReal;
import com.lechange.opensdk.media.LCOpenSDK_PlayWindow;
import com.mm.android.deviceaddmodule.device_wifi.DeviceConstant;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.openapi.TokenHelper;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.entity.Direction;
import com.mm.android.mobilecommon.route.ProviderManager;
import com.mm.android.mobilecommon.utils.LogUtil;
import com.pushpull.camapp.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceOnlineMediaPlayMultiActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = DeviceOnlineMediaPlayMultiActivity.class.getSimpleName();

    List<LCOpenSDK_PlayWindow> playwindowList = new ArrayList<>();
    private String[] deviceNameArr;
    public LCOpenSDK_PlayWindow currPlayWin = new LCOpenSDK_PlayWindow();

    private DeviceDetailListData.ResponseData.DeviceListBean deviceListBean;
    private Bundle bundle;

    private TextView tvDeviceName;
    private RecyclerView rcvVideoList;
    private LinearLayout llBack;
    private LinearLayout llRoot;

    private PlayStatus playStatus = PlayStatus.ERROR;
    private Direction mPTZPreDirection = null;
    private int mCurrentOrientation;
    private LinearLayout llTitle;
    private RelativeLayout rlTitle;
    private EncryptKeyInputDialog encryptKeyInputDialog;
    private String encryptKey;
    private boolean supportPTZ;
    private String videoPath = null;
    private OrientationEventListener mOrientationEventListener;
    private int mVideoViewCurrentOrientationRequest = NO_ORIENTATION_REQUEST;
    private LcPopupWindow lcPopupWindow;
    private int imageSize = -1;//视频播放分辨率
    private int bateMode;

    private int currIndex;
    private int linearHeight;

    private List<DeviceDetailListData.ResponseData.DeviceListBean> bundleMCameraDatas = new ArrayList<>();
    private List<DeviceDetailListData.ResponseData.DeviceListBean> mCameraDatas = new ArrayList<>();

    private Map<Integer, RelativeLayout> rlMap = new HashMap<>();
    private Map<Integer, ProgressBar> pbMap = new HashMap<>();
    private Map<Integer, ImageView> ivFrMap = new HashMap<>();
    private Map<Integer, TextView> tvFrNameMap = new HashMap<>();
    private Map<Integer, TextView> tvLoadingMsgMap = new HashMap<>();
    private Map<Integer, FrameLayout> frWindowContentMap = new HashMap<>();
    private Map<Integer, LinearLayout> linearLayoutMap = new HashMap<>();

    private String DEVICE_OFFLINE = "offline";
    private boolean isTwoDevices = false;
    private int portraitLineNumber = 5;
    private int landscapeLineNumber = 4;
    private int CHECK_TWO_DEVICES = 2;

    private int lcdWidth;
    private int lcdHeight;
    private int MAX_CNT = 20;

    public enum PlayStatus {
        PLAY, PAUSE, ERROR
    }

    public enum LoadStatus {
        LOADING, LOAD_SUCCESS, LOAD_ERROR
    }

    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mHandler = new ActivityHandler(this) {
            @Override
            public void handleMsg(Message msg) {
                switch (msg.what) {
                    case MSG_REQUEST_ORIENTATION:
                        requestedOrientation(msg.arg1, false);
                        break;
                    default:
                        break;
                }
                // handlePlayMessage(msg);
            }

        };
        super.onCreate(savedInstanceState);

        linearHeight = 0;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mCurrentOrientation = Configuration.ORIENTATION_PORTRAIT;
        initOrientationEventListener();
        setContentView(R.layout.activity_device_online_media_play_multi);
        initView();
        initData();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private LCOpenSDK_PlayWindow makePlayWindow() {
        LCOpenSDK_PlayWindow playWindow = new LCOpenSDK_PlayWindow();
        playwindowList.add(playWindow);

        return playWindow;
    }

    protected void requestedOrientation(int requestedOrientation, boolean isForce) {

        if (!isForce) {

            if (mVideoViewCurrentOrientationRequest == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mVideoViewCurrentOrientationRequest = NO_ORIENTATION_REQUEST;
            } else {
                if (mVideoViewCurrentOrientationRequest == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        mVideoViewCurrentOrientationRequest = NO_ORIENTATION_REQUEST;
                        return;
                    }

                    if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                        mVideoViewCurrentOrientationRequest = NO_ORIENTATION_REQUEST;
                        return;
                    }
                    mVideoViewCurrentOrientationRequest = NO_ORIENTATION_REQUEST;
                }
            }

        } else {
            mVideoViewCurrentOrientationRequest = requestedOrientation;
        }

        try {
            setRequestedOrientation(requestedOrientation);
        } catch (Exception e) {
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mCurrentOrientation = newConfig.orientation;
        super.onConfigurationChanged(newConfig);
        switchScreenDirection();
    }

    private void initView() {
        lcdWidth = getLcdSizeWidth();
        lcdHeight = getLcdSizeHeight();
        llBack = findViewById(R.id.ll_back);
        tvDeviceName = findViewById(R.id.tv_device_name);

        llRoot = findViewById(R.id.ll_root);

        llTitle = findViewById(R.id.ll_title);
        rlTitle = findViewById(R.id.rl_title);
        initCommonClickListener();
        switchScreenDirection();
    }

    private long doubleClickLastTime = 0L;
    private void ivFrClickLandSpace() {
        makeBorderLine(currIndex);

        Gson gson = new Gson();
        String json = gson.toJson(mCameraDatas.get(currIndex));
        ProviderManager.getPreviewProvider().gotoPrevew(json);
    }

    private void ivFrClickPortrait() {
        if(System.currentTimeMillis() - doubleClickLastTime < 300){
            doubleClickLastTime = 0L;
            ivFrClickLandSpace();
        }else{
            doubleClickLastTime = System.currentTimeMillis();
        }

        checkDeviceAvility(currIndex);
        tvDeviceName.setText(deviceNameArr[currIndex]);
        initAbility(currIndex, true);
        makeBorderLine(currIndex);
    }

    @SuppressLint("ResourceAsColor")
    private void switchScreenDirection() {
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) { // 세로전환
            if(linearLayoutMap != null && linearLayoutMap.size() > 0) {
                linearHeight = generateLinearHeight(portraitLineNumber);
                setLinearHeight();
            }

            // 장치가 2개면 타이틀 제거
            if(!isTwoDevices) {
                llTitle.setVisibility(View.VISIBLE);
                rlTitle.setVisibility(View.VISIBLE);
            }
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) { // 가로전환
            if(linearLayoutMap != null && linearLayoutMap.size() > 0) {
                if(!isTwoDevices) {
                    linearHeight = generateLinearHeight(landscapeLineNumber);
                } else {
                    linearHeight = generateLinearHeight(landscapeLineNumber);
                }
            }

            setLinearHeight();
            llTitle.setVisibility(View.GONE);
            rlTitle.setVisibility(View.GONE);
            setupWindowContent();
        }
    }

    public boolean isTablet(Context context) {
        boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
        return (xlarge || large);
    }

    private void setLinearHeight() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, linearHeight, 1.0f);
        for(int i = 0, j=0; i< linearLayoutMap.size(); i++) {
            linearLayoutMap.get(j).setLayoutParams(layoutParams);
            j += 2;
        }
    }

    private void setLinearHeight2() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, linearHeight, 1.0f);
        for(int i = 0, j=0; i< linearLayoutMap.size(); i++) {
            linearLayoutMap.get(j).setLayoutParams(layoutParams);
            j += 2;
        }
    }

    private LinearLayout currHoriLiLayout;
    private void initData() {
        bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }

        mCameraDatas = (List<DeviceDetailListData.ResponseData.DeviceListBean>) bundle.getSerializable(MethodConst.ParamConst.mCameraDatas);
        if(mCameraDatas == null) return;

        int cameraCnt = mCameraDatas.size();
        if(mCameraDatas.get(cameraCnt -1).deviceStatus.equals("EMPTY")) {
            mCameraDatas.remove(cameraCnt -1);
        }
//        // 디바이스 오프라인은 표시 안함.
//        for(DeviceDetailListData.ResponseData.DeviceListBean bean : bundleMCameraDatas) {
//            if(bean.status.equals(DEVICE_OFFLINE)) continue;
//            mCameraDatas.add(bean);
//        }

        if(mCameraDatas.size() < MAX_CNT) {
            MAX_CNT = mCameraDatas.size();
        }

        deviceNameArr = new String[MAX_CNT];
        if(mCameraDatas.size() == CHECK_TWO_DEVICES) {
            isTwoDevices = true;
            portraitLineNumber = 1;
            landscapeLineNumber = 1;
        }

        for(int i=0; i<MAX_CNT; i++) {
            DeviceDetailListData.ResponseData.DeviceListBean bean = mCameraDatas.get(i);
//            if(bean.status.equals(DEVICE_OFFLINE)) continue;
            if(i %2 == 0) {
                currHoriLiLayout = getHorizontalLinear(i);
                currHoriLiLayout.setVisibility(View.VISIBLE);
            }
            getItemLinear(i);
        }

        setupWindowContent();
        checkDeviceAvility(0);

        int i=0;
        for(DeviceDetailListData.ResponseData.DeviceListBean bean : mCameraDatas) {
            deviceNameArr[i] = bean.deviceName;
            getDeviceLocalCache(bean);

            i++;
        }

        tvDeviceName.setText(deviceListBean.channelList.get(deviceListBean.checkedChannel).channelName);

        lcPopupWindow = new LcPopupWindow(DeviceOnlineMediaPlayMultiActivity.this,deviceListBean.channelList.get(deviceListBean.checkedChannel).resolutions);
        lcPopupWindow.getContentView().measure(lcPopupWindow.makeDropDownMeasureSpec(lcPopupWindow.getWidth())
                ,lcPopupWindow.makeDropDownMeasureSpec(lcPopupWindow.getHeight()));

    }

    private RelativeLayout.LayoutParams getRelativeMatchParentParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return layoutParams;
    }

    private LinearLayout.LayoutParams getLinearMatchParentParamsWeight() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        return layoutParams;
    }

    private LinearLayout.LayoutParams getLinearMatchParentParams() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return layoutParams;
    }

    private void getItemLinear(int i) {
        int windowContentId = 2000000 + i;

        // fr_live_window_content0
        FrameLayout fl = getFrameLayout(i);
        // tv_fr_name0...n
        TextView tv = getTextView("tv_fr_name", i);
        // iv_fr0
        ImageView iv = getImageView(i);

        // rl_loading0
        RelativeLayout rl = getRelativeLayout(i);

        // pb_loading0
        ProgressBar pb = getProgressBar(i);

        // tv_loading_msg0
        TextView tvMsg = getTextView("tv_loading_msg", i);

        rlMap.put(i, rl);
        pbMap.put(i, pb);
        ivFrMap.put(i, iv);
        tvFrNameMap.put(i, tv);
        tvLoadingMsgMap.put(i, tvMsg);
        frWindowContentMap.put(i, fl);

        addEvent(i);
    }


    private void addEvent(final int i) {
        currPlayWin = makePlayWindow();

        currPlayWin.initPlayWindow(getApplicationContext(), frWindowContentMap.get(i), 0, false);
        currPlayWin.openTouchListener();

        setWindowListener(currPlayWin);

        ivFrMap.get(i).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currIndex = i;
                ivFrClickPortrait();
            }
        });
    }


    private void setWindowListener(LCOpenSDK_PlayWindow playWin) {
        playWin.setWindowListener(new LCOpenSDK_EventListener() {
            //手势缩放开始事件
            @Override
            public void onZoomBegin(int index) {
                super.onZoomBegin(index);
                LogUtil.debugLog(TAG, "onZoomBegin: index= " + index);
            }

            //手势缩放中事件
            @Override
            public void onZooming(int index, float dScale) {
                super.onZooming(index, dScale);
                LogUtil.debugLog(TAG, "onZooming: index= " + index + " , dScale= " + dScale);
                getCurrPlayWin(index).doScale(dScale);
            }

            //缩放结束事件
            @Override
            public void onZoomEnd(int index, ZoomType zoomType) {
                super.onZoomEnd(index, zoomType);
                LogUtil.debugLog(TAG, "onZoomEnd: index= " + index + " , zoomType= " + zoomType);
            }

            //窗口单击事件
            @Override
            public void onControlClick(int index, float dx, float dy) {
                super.onControlClick(index, dx, dy);
                LogUtil.debugLog(TAG, "onControlClick: index= " + index + " , dx= " + dx + " , dy= " + dy);
            }

            //窗口双击事件
            @Override
            public void onWindowDBClick(int index, float dx, float dy) {
                super.onWindowDBClick(index, dx, dy);
                LogUtil.debugLog(TAG, "onWindowDBClick: index= " + index + " , dx= " + dx + " , dy= " + dy);
            }

            //滑动开始事件
            @Override
            public boolean onSlipBegin(int index, Direction direction, float dx, float dy) {
                LogUtil.debugLog(TAG, "onSlipBegin: index= " + index + " , direction= " + direction + " , dx= " + dx + " , dy= " + dy);
                return super.onSlipBegin(index, direction, dx, dy);
            }

            //滑动中事件
            @Override
            public void onSlipping(int index, Direction direction, float prex, float prey, float dx, float dy) {
                super.onSlipping(index, direction, prex, prey, dx, dy);
                getCurrPlayWin(index).doTranslate(dx,dy);
                LogUtil.debugLog(TAG, "onSlipping: index= " + index + " , direction= " + direction + " , prex= " + prex + " , prey= " + prey + " , dx= " + dx + " , dy= " + dy);
            }

            //滑动结束事件
            @Override
            public void onSlipEnd(int index, Direction direction, float dx, float dy) {
                super.onSlipEnd(index, direction, dx, dy);
                getCurrPlayWin(index).doTranslateEnd();
                LogUtil.debugLog(TAG, "onSlipEnd: index= " + index + " , direction= " + direction + " , dx= " + dx + " , dy= " + dy);
            }

            //长按开始回调
            @Override
            public void onWindowLongPressBegin(int index, Direction direction, float dx, float dy) {
                super.onWindowLongPressBegin(index, direction, dx, dy);
                LogUtil.debugLog(TAG, "onWindowLongPressBegin: index= " + index + " , direction= " + direction + " , dx= " + dx + " , dy= " + dy);
            }

            //长按事件结束
            @Override
            public void onWindowLongPressEnd(int index) {
                super.onWindowLongPressEnd(index);
                LogUtil.debugLog(TAG, "onWindowLongPressEnd: index= " + index);
            }

            /**
             * 播放事件回调
             * resultSource:  0--RTSP  1--HLS  5--DHHTTP  99--OPENAPI
             */
            @Override
            public void onPlayerResult(int index, String code, int resultSource) {
                //mPlayWin.setSEnhanceMode(4);//设置降噪等级最大
                super.onPlayerResult(index, code, resultSource);
                LogUtil.debugLog(TAG, "onPlayerResult: index= " + index + " , code= " + code + " , resultSource= " + resultSource);
                boolean failed = false;
                if (resultSource == 99) {
                    //code  -1000 HTTP交互出错或超时
                    failed = true;
                } else {
                    if (resultSource == 5 && (!(code.equals("1000") || code.equals("0") || code.equals("4000")))) {
                        // code 1000-开启播放成功  0-开始拉流
                        failed = true;
                        if (code.equals("1000005")) {
//                            inputEncryptKey();
                        }
                    }

                    else if (resultSource == 0 && (code.equals("0") || code.equals("1") || code.equals("3") || code.equals("7"))) {
                        // code
                        // 0-组帧失败，错误状态
                        // 1-内部要求关闭,如连接断开等，错误状态
                        // 3-RTSP鉴权失败，错误状态
                        // 7-秘钥错误
                        failed = true;
                        if (code.equals("7")) {
//                            inputEncryptKey();
                        }
                    }
                }
                if (failed) {
                    getTvFrName(index).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    String msg = getResources().getString(R.string.lc_demo_device_video_play_error) + ":" + code + "." + resultSource;
                    if(mCameraDatas.get(index).deviceStatus.equals(DEVICE_OFFLINE)) {
                        msg = getResources().getString(R.string.lc_demo_main_offline);
                    }

                    loadingStatus(index, LoadStatus.LOAD_ERROR, msg, "");
                    playStatus = PlayStatus.ERROR;
                }
            }

            //分辨率改变事件
            @Override
            public void onResolutionChanged(int index, int width, int height) {
                super.onResolutionChanged(index, width, height);
                LogUtil.debugLog(TAG, "onResolutionChanged: index= " + index + " , width= " + width + " , height= " + height);
            }

            //播放开始回调
            @Override
            public void onPlayBegan(int index) {
                super.onPlayBegan(index);
                LogUtil.debugLog(TAG, "onPlayBegan: index= " + index);
                int i=-1;
                for(DeviceDetailListData.ResponseData.DeviceListBean bean : mCameraDatas) {
                    i++;
                    if(bean == null || bean.deviceId == null || bean.deviceId.equals("")) continue;
                    if(bean.deviceStatus.equals(DEVICE_OFFLINE)) {
                        getTvFrName(i).setText(deviceNameArr[i] + " - " + DEVICE_OFFLINE);
                        continue;
                    }
                    deviceListBean = mCameraDatas.get(index);
                    currIndex = i;
                    getTvFrName(i).setText(deviceNameArr[i]);
                    playwindowList.get(i).stopAudio();
                    loadingStatus(i, LoadStatus.LOAD_SUCCESS, "", "");
                }
                playStatus = PlayStatus.PLAY;
            }

            //接收数据回调
            @Override
            public void onReceiveData(int index, int len) {
                super.onReceiveData(index, len);
                LogUtil.debugLog(TAG, "onReceiveData: index= " + index + " , len= " + len);
            }

            //接收帧流回调
            @Override
            public void onStreamCallback(int index, byte[] bytes, int len) {
                super.onStreamCallback(index, bytes, len);
                LogUtil.debugLog(TAG, "onStreamCallback: index= " + index + " , len= " + len);
            }

            //播放结束事件
            @Override
            public void onPlayFinished(int index) {
                super.onPlayFinished(index);
                LogUtil.debugLog(TAG, "onPlayFinished: index= " + index);
            }

            //播放时间信息回调
            @Override
            public void onPlayerTime(int index, long time) {
                super.onPlayerTime(index, time);
                LogUtil.debugLog(TAG, "onPlayerTime: index= " + index + " , time= " + time);
            }


            @Override
            public void onIVSInfo(int index, final String ivsInfo, long type, long len, long realLen) {
                super.onIVSInfo(index, ivsInfo, type, len, realLen);
                LogUtil.debugLog(TAG, "onIVSInfo: index= " + index + " , ivsInfo= " + ivsInfo);

                if (playStatus != PlayStatus.PLAY) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (ivsInfo != null && ivsInfo.contains("PtzLimitStatus")) {
                                final String source = ivsInfo.substring(ivsInfo.lastIndexOf("[") + 1, ivsInfo.lastIndexOf("]")).replace(" ", "");
                                String[] target = source.split(",");
                                if (target != null && target.length == 2) {
                                    final String hor = target[0];
                                    final String ver = target[1];
                                    if (hor.equals("1") || hor.equals("-1") || ver.equals("1") || ver.equals("-1")) {
                                        if(hor.equals("1")){
                                        }else if(hor.equals("-1")){
                                        }else if(ver.equals("1")){
                                        }else if(ver.equals("-1")){
                                        }
                                    }
                                }
                            }else{
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private RelativeLayout getRelativeLayout(int i) {
        String idStr = "rl_loading"+i;
        int resID = getResources().getIdentifier(idStr, "id", getPackageName());
        RelativeLayout rl = (RelativeLayout) findViewById(resID);

        return rl;
    }

    private ProgressBar getProgressBar(int i) {
        String idStr = "pb_loading"+i;
        int resID = getResources().getIdentifier(idStr, "id", getPackageName());
        ProgressBar pb = (ProgressBar) findViewById(resID);
        pb.setVisibility(View.GONE);

        return pb;
    }

    private ImageView getImageView(int i) {
        String idStr = "iv_fr"+i;
        int resID = getResources().getIdentifier(idStr, "id", getPackageName());
        ImageView iv = (ImageView) findViewById(resID);

        return iv;
    }

    private TextView getTextView(String prefix, int i) {
        String idStr = prefix+i;
        int resID = getResources().getIdentifier(idStr, "id", getPackageName());
        TextView tv = (TextView) findViewById(resID);

        return tv;
    }

    private FrameLayout getFrameLayout(int i) {
        String idStr = "fr_live_window_content"+i;
        int resID = getResources().getIdentifier(idStr, "id", getPackageName());

        FrameLayout fl = (FrameLayout) findViewById(resID);

        return fl;
    }

    private LinearLayout getHorizontalLinear(int i) {
        String idStr = "ll_"+i;
        int resID = getResources().getIdentifier(idStr, "id", getPackageName());
        LinearLayout ll = (LinearLayout) findViewById(resID);

        if(isTwoDevices) {
            llTitle.setVisibility(View.GONE);
            ll.setOrientation(LinearLayout.VERTICAL); // 1단으로 할경우 주석 해제
        }

        linearHeight = generateLinearHeight(portraitLineNumber);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, linearHeight, 1.0f);
        ll.setLayoutParams(layoutParams);

        linearLayoutMap.put(i, ll);

        return ll;
    }

    private int getLcdSizeWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private int getLcdSizeHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    private int generateLinearHeight(int n) {
        int h = lcdHeight / n;
        if(isTablet(getApplicationContext()) && mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE ) h = (lcdHeight + 380) / n;
        return h;
    }

    private int generateLinearWidth(int n) {
        return lcdWidth / n;
    }

    private void checkDeviceAvility(int index) {
        deviceListBean = mCameraDatas.get(index);
    }

    private void setupWindowContent() {
    }

    /**
     * 获取设备缓存信息
     * @param bean
     */
    private void getDeviceLocalCache(DeviceDetailListData.ResponseData.DeviceListBean bean) {
        DeviceLocalCacheData deviceLocalCacheData = new DeviceLocalCacheData();
        deviceLocalCacheData.setDeviceId(bean.deviceId);
        if (bean.channelList != null && bean.channelList.size() > 0) {
            deviceLocalCacheData.setChannelId(bean.channelList.get(bean.checkedChannel).channelId);
        }
        DeviceLocalCacheService deviceLocalCacheService = DeviceLocalCacheService.getInstance();
        deviceLocalCacheService.findLocalCache(deviceLocalCacheData, new com.lc.media.api.IGetDeviceInfoCallBack.IDeviceCacheCallBack() {
            @Override
            public void deviceCache(DeviceLocalCacheData deviceLocalCacheData) {
                BitmapDrawable bitmapDrawable = MediaPlayHelper.picDrawable(deviceLocalCacheData.getPicPath());
                if (bitmapDrawable != null) {
//            getRlLoading(currIndex).setBackground(bitmapDrawable);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        for(int i=0; i<MAX_CNT; i++) {
            DeviceDetailListData.ResponseData.DeviceListBean bean = mCameraDatas.get(i);
            if(bean == null || bean.deviceId == null || bean.deviceId.equals("")) continue;
            deviceListBean = bean;
            if(bean.deviceStatus.equals(DEVICE_OFFLINE)) {
                getTvLoadingMsg(i).setText(bean.deviceName+"\r\n"+getResources().getString(R.string.lc_demo_main_offline));
                continue;
            }
            loadingStatus(i, LoadStatus.LOADING, getResources().getString(R.string.lc_demo_device_video_play_loading), deviceListBean.deviceId);
        }
    }

    private void makeBorderLine(int index) {
        for(int i=0; i<MAX_CNT; i++) {

            int borderLine = R.drawable.multi_border_color_clear;
            if(i == index) borderLine = R.drawable.multi_border_color;

            if(mCameraDatas.get(i).deviceStatus.equals(DEVICE_OFFLINE)) continue;
            ImageView iv = getIvFr(i);
            if(iv != null) {
                iv.setBackgroundResource(borderLine);
            }
        }
    }

    private void initAbility(int index, boolean loadSuccess) {
        deviceListBean = mCameraDatas.get(index);
        String deviceAbility = deviceListBean.deviceAbility;
        String channelAbility = deviceListBean.channelList.get(deviceListBean.checkedChannel).channelAbility;
        //云台
        supportPTZ = DeviceAbilityHelper.isHasAbility(deviceAbility,channelAbility,"PT","PTZ") && loadSuccess;
    }

    private void initCommonClickListener() {
        llBack.setOnClickListener(this);
    }

    private LCOpenSDK_PlayWindow getCurrPlayWin(int index) {
        return playwindowList.get(index);
//        if(index == 0) return mPlayWin1;
//        else if(index == 1) return mPlayWin2;
//        else if(index == 2) return mPlayWin3;
//        else if(index == 3) return mPlayWin4;
//
//        return mPlayWin1;
    }

    /**
     * 播放状态
     *
     * @param loadStatus 播放状态
     * @param msg
     */
    private void loadingStatus(final int index, final LoadStatus loadStatus, final String msg, final String psk) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currIndex = index;
                if (loadStatus == LoadStatus.LOADING) {
                    stop(index);
                    play(index, psk);
                    getRlLoading(index).setVisibility(View.VISIBLE);
                    getPbLoading(index).setVisibility(View.VISIBLE);
                    getTvLoadingMsg(index).setText(msg);
                } else if (loadStatus == LoadStatus.LOAD_SUCCESS) {
                    getFrLiveWindowContent(index).removeView(getTvFrName(index));
                    getFrLiveWindowContent(index).removeView(getIvFr(index));
                    getFrLiveWindowContent(index).addView(getIvFr(index));
                    getFrLiveWindowContent(index).addView(getTvFrName(index));
                    getRlLoading(index).setVisibility(View.GONE);
                    initAbility(index,true);
                } else {
                    stop(index);
                    getRlLoading(index).setVisibility(View.VISIBLE);
                    getPbLoading(index).setVisibility(View.GONE);
                    getTvLoadingMsg(index).setText(msg);
                }
            }
        });
    }

    private FrameLayout getFrLiveWindowContent(int index) {
        return frWindowContentMap.get(index);
//        if(index == 0) return frLiveWindowContent;
//        else if(index == 1) return frLiveWindowContent2;
//        else if(index == 2) return frLiveWindowContent3;
//        else if(index == 3) return frLiveWindowContent4;
//        return frLiveWindowContent;
    }

    private TextView getTvFrName(int index) {
        return tvFrNameMap.get(index);
//        if(index == 0) return tvFrName1;
//        else if(index == 1) return tvFrName2;
//        else if(index == 2) return tvFrName3;
//        else if(index == 3) return tvFrName4;
//        return tvFrName1;
    }

    private ImageView getIvFr(int index) {
        return ivFrMap.get(index);
//        if(index == 0) return ivFr1;
//        else if(index == 1) return ivFr2;
//        else if(index == 2) return ivFr3;
//        else if(index == 3) return ivFr4;
//        return ivFr1;
    }

    private TextView getTvLoadingMsg(int index) {
        return tvLoadingMsgMap.get(index);
//        if(index == 0) return tvLoadingMsg;
//        else if(index == 1) return tvLoadingMsg2;
//        else if(index == 2) return tvLoadingMsg3;
//        else if(index == 3) return tvLoadingMsg4;
//        return tvLoadingMsg;
    }

    private ProgressBar getPbLoading(int index) {
        return pbMap.get(index);
//        if(index == 0) return pbLoading;
//        else if(index == 1) return pbLoading2;
//        else if(index == 2) return pbLoading3;
//        else if(index == 3) return pbLoading4;
//        return pbLoading;
    }

    private RelativeLayout getRlLoading(int index) {
        return rlMap.get(index);
//        if(index == 0) return rlLoading;
//        else if(index == 1) return rlLoading2;
//        else if(index == 2) return rlLoading3;
//        else if(index == 3) return rlLoading4;
//        return rlLoading;
    }

    public void play(int index, String psk) {
        Log.d(TAG, "play = "+psk);
        LCOpenSDK_ParamReal paramReal = new LCOpenSDK_ParamReal(
                TokenHelper.getInstance().subAccessToken,
                deviceListBean.deviceId,
                Integer.parseInt(deviceListBean.channelList.get(deviceListBean.checkedChannel).channelId),
                psk,
                deviceListBean.playToken,
                bateMode,
                true,true,imageSize
        );
        getCurrPlayWin(index).playRtspReal(paramReal);
        Log.d(TAG, "play url "+paramReal);

    }

    public void stop(int index) {
        captureLastPic(index);
//        rudderView.enable(false);
        getCurrPlayWin(index).stopRtspReal(true);
    }

    public String capture(boolean notify) {
        String captureFilePath = null;
        String channelName = null;
        if (deviceListBean.channelList != null && deviceListBean.channelList.size() > 0) {
            channelName = deviceListBean.channelList.get(deviceListBean.checkedChannel).channelName;
        } else {
            channelName = deviceListBean.deviceName;
        }
        // 去除通道中在目录中的非法字符
        channelName = channelName.replace("-", "");
        captureFilePath = MediaPlayHelper.getCaptureAndVideoPath(notify ? MediaPlayHelper.LCFilesType.LCImage : MediaPlayHelper.LCFilesType.LCImageCache, channelName);
        int ret = getCurrPlayWin(currIndex).snapShot(captureFilePath);
        if (ret == 0) {
            if (notify) {
                // 扫描到相册中
                MediaPlayHelper.updatePhotoAlbum(captureFilePath);
                // MediaScannerConnection.scanFile(this, new String[]{captureFilePath}, null, null);
            }
        } else {
            captureFilePath = null;
        }
        return captureFilePath;
    }

    private void captureLastPic(int index) {
        if (playStatus == PlayStatus.ERROR) {
            return;
        }
        String capturePath;
        try {
            capturePath = capture(false);
        } catch (Throwable e) {
            capturePath = null;
        }
        if (capturePath == null) {
            return;
        }

        deviceListBean = mCameraDatas.get(index);


        DeviceLocalCacheService deviceLocalCacheService = DeviceLocalCacheService.getInstance();
        DeviceLocalCacheData deviceLocalCacheData = new DeviceLocalCacheData();
        deviceLocalCacheData.setPicPath(capturePath);
        deviceLocalCacheData.setDeviceName(deviceListBean.deviceName);
        deviceLocalCacheData.setDeviceId(deviceListBean.deviceId);
        if (deviceListBean.channelList != null && deviceListBean.channelList.size() > 0) {
            deviceLocalCacheData.setChannelId(deviceListBean.channelList.get(deviceListBean.checkedChannel).channelId);
            deviceLocalCacheData.setChannelName(deviceListBean.channelList.get(deviceListBean.checkedChannel).channelName);
        }
        deviceLocalCacheService.addLocalCache(deviceLocalCacheData);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_back) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCurrPlayWin(currIndex).uninitPlayWindow();// 销毁底层资源
        uninitOrientationEventListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            boolean unBind = data.getBooleanExtra(DeviceConstant.IntentKey.LCDEVICE_UNBIND, false);
            if (unBind) {
                finish();
            }
        }
        if (resultCode == 100 && data != null) {
            String name = data.getStringExtra(DeviceConstant.IntentKey.LCDEVICE_NEW_NAME);
            tvDeviceName.setText(name);
            deviceListBean.channelList.get(deviceListBean.checkedChannel).channelName = name;
        }
    }


    public static final int NO_ORIENTATION_REQUEST = -1;

    public static final int MSG_REQUEST_ORIENTATION = 1;


    private void initOrientationEventListener() {

        mOrientationEventListener = new MediaPlayOrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL, mHandler);

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        } else {
            mOrientationEventListener.disable();
        }
    }


    private void uninitOrientationEventListener() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
            mOrientationEventListener = null;
        }
    }



    static class MediaPlayOrientationEventListener extends OrientationEventListener {

        private WeakReference<Handler> mWRHandler;

        private int mOrientationEventListenerLastOrientationRequest = NO_ORIENTATION_REQUEST;

        public MediaPlayOrientationEventListener(Context context, int rate, Handler handler) {
            super(context, rate);
            mWRHandler = new WeakReference<Handler>(handler);
        }

        @Override
        public void onOrientationChanged(int orientation) {

            int requestedOrientation = createOrientationRequest(orientation);
            if (requestedOrientation != NO_ORIENTATION_REQUEST
                    && mOrientationEventListenerLastOrientationRequest != requestedOrientation) {

                Handler handler = mWRHandler.get();

                if (handler != null) {
                    handler.removeMessages(MSG_REQUEST_ORIENTATION);
                    Message msg = handler.obtainMessage(MSG_REQUEST_ORIENTATION);
                    msg.arg1 = requestedOrientation;
                    handler.sendMessageDelayed(msg, 200);
                    mOrientationEventListenerLastOrientationRequest = requestedOrientation;
                }
            }
        }

        private int createOrientationRequest(int rotation) {
            int requestedOrientation = NO_ORIENTATION_REQUEST;

            if (rotation == -1) {

            } else if (rotation < 10 || rotation > 350) {// 手机顶部向上

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

            } else if (rotation < 100 && rotation > 80) {// 手机左边向上

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;

            } else if (rotation < 190 && rotation > 170) {// 手机底边向上

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;

            } else if (rotation < 280 && rotation > 260) {// 手机右边向上

                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }

            return requestedOrientation;
        }

    }
}
