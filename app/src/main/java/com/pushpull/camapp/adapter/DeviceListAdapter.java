package com.pushpull.camapp.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lc.media.entity.DeviceLocalCacheData;
import com.mm.android.mobilecommon.pps.utils.DeviceAbilityHelper;
import com.lc.media.utils.MediaPlayHelper;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.pps.cont.CAPABILITY;
import com.mm.android.mobilecommon.pps.model.CameraStatusVO;
import com.mm.android.mobilecommon.pps.service.DeviceCameraStatusService;
import com.mm.android.mobilecommon.pps.service.IDeviceCameraStatus;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.PreferencesHelper;
import com.pushpull.camapp.R;
import com.pushpull.camapp.tools.GridItemDecoration;
import com.pushpull.camapp.tools.RoundedCornersTransform;
import com.mm.android.mobilecommon.AppConsume.ProviderManager;
import com.mm.android.mobilecommon.common.LCConfiguration;

import java.io.File;
import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListHolder> {

    public Context mContext;
    private List<DeviceDetailListData.ResponseData.DeviceListBean> datas;
    private RecyclerView.ItemDecoration decoration;
    private RoundedCornersTransform transform;
    private RequestOptions options;
    private int listOffset = 0;

    public DeviceListAdapter(Context mContext, List<DeviceDetailListData.ResponseData.DeviceListBean> datas) {
        this.mContext = mContext;
        this.datas = datas;
        transform = new RoundedCornersTransform(mContext, mContext.getResources().getDimensionPixelOffset(R.dimen.px_26));
        transform.setNeedCorner(false, false, true, true);
        options = new RequestOptions().placeholder(R.color.c10).transform(transform);
        listOffset = mContext.getResources().getDimensionPixelOffset(R.dimen.px_10);
        decoration = new GridItemDecoration(listOffset, true);
    }


    private void drawImageView(ImageView imageView, boolean b) {
        if(imageView == null) return;
        if (b) {
            imageView.setImageDrawable(this.mContext.getResources().getDrawable(R.mipmap.lc_demo_switch_on));
        } else {
            imageView.setImageDrawable(this.mContext.getResources().getDrawable(R.mipmap.lc_demo_switch_off));
        }
    }

    @NonNull
    @Override
    public DeviceListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == -1) {
            view = LayoutInflater.from(mContext).inflate(R.layout.empty_view, parent, false);
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_device_list, parent, false);
        }
        return new DeviceListHolder(mContext, view,viewType);
    }

    @Override
    public int getItemViewType(int position) {
        if (datas == null || datas.size() == 0) {
            return -1;
        }
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceListHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (datas == null ||datas.size()==0) {
            return;
        }

        final DeviceDetailListData.ResponseData.DeviceListBean listBean = datas.get(position);
        final String deviceId = listBean.deviceId;

        holder.tvName.setText("");
        holder.tvSharedInfo.setText("");
        holder.llWrap.setVisibility(View.GONE);

        // 기기에 사생활보호 기능이 있으면...
        if(listBean.deviceAbility.toLowerCase().contains(CAPABILITY.CLOSE_CAMERA.toLowerCase())) {
            holder.llCloseCamera.setVisibility(View.VISIBLE);
        }

        holder.llReverseImage.setVisibility(View.VISIBLE);
        holder.tvSharedInfo.setVisibility(View.GONE);

        // 목록이 3개이상일경우 맨 밑의 목록은 표시되다가 잘리기 때문에 여백 표시 처리.
//        if(getItemCount() > 1 && position == getItemCount() -1) {
//            holder.llWrap.setVisibility(View.VISIBLE);
//        }

        holder.ivAdd.setVisibility(View.GONE);


        // 마지막 ( 기기 추가 보여주기 )
        if(position == datas.size()-1 && listBean.deviceStatus.equals("EMPTY")) {
//            holder.llTitle.setVisibility(View.GONE);
            holder.tvName.setText(mContext.getString(R.string.lc_ksw_add_camera));
            holder.tvOffline.setText("\r\n\r\n\r\n\r\n\r\n"+mContext.getString(R.string.lc_ksw_add_camera));
            holder.ivDetail.setVisibility(View.GONE);
            holder.to_msg.setVisibility(View.GONE);
            holder.rlOffline.setBackground(mContext.getDrawable(R.drawable.lc_demo_soild_20r_c66000000_shape));
            holder.ivAdd.setBackground(mContext.getDrawable(R.color.transparent));
            holder.llCloseCamera.setVisibility(View.GONE);
            holder.llReverseImage.setVisibility(View.GONE);
            holder.ivAdd.setVisibility(View.VISIBLE);
            holder.ivPlay.setVisibility(View.GONE);

            holder.rlOffline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onAddClick();
                    }
                }
            });
            return;
        }

        /** 공유된 기기 여부 셋팅 **/
        boolean isShared = false;
        boolean isBlockchainFail = false;
        datas.get(position).setShared(false);
        String permission = datas.get(position).permission;

        String tvNameStr = datas.get(position).deviceName;
        if(permission != null && !permission.equalsIgnoreCase("devcontrol")) {
            isShared = true;
            datas.get(position).setShared(true);

            holder.llCloseCamera.setVisibility(View.GONE);
            holder.llReverseImage.setVisibility(View.GONE);

            holder.tvSharedInfo.setText("[" + holder.itemView.getResources().getString(R.string.lc_device_share_list) +"]  ");
            holder.tvSharedInfo.setVisibility(View.VISIBLE);
        }

        String channelId = "0";
        if(listBean.channelList != null && listBean.channelList.size() > 0) {
            channelId = listBean.channelList.get(listBean.checkedChannel).channelId;
        }
        final String closeCameraKey = channelId+"_"+deviceId+"_"+ CAPABILITY.CLOSE_CAMERA;
        final String reverseImageKey = channelId+"_"+deviceId+"_"+ CAPABILITY.MODIFY_FRAME_REVERSE_STATUS;

        // 사생활보호 기능이 있으면... swkim
        if(listBean.deviceAbility.toLowerCase().contains(CAPABILITY.CLOSE_CAMERA.toLowerCase())) {
            holder.llCloseCamera.setVisibility(View.VISIBLE);

            /** 장비의 사생활보호 상태 값 조회 **/
            CameraStatusVO cameraStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId);
            cameraStatusVO.setEnableType(CAPABILITY.CLOSE_CAMERA);
            cameraStatusVO.setTargetIV(holder.ivCloseCheck);
            getDeviceCameraStatus(cameraStatusVO);

            holder.llCloseCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.ivCloseCheck.callOnClick();
                }
            });

            /** 사생활보호 **/
            String finalChannelId = channelId;
            holder.ivCloseCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.show((Activity) mContext);
                    boolean closeCameraValue = PreferencesHelper.getInstance(mContext).getBoolean(closeCameraKey, true);
                    closeCameraValue = !closeCameraValue;

                    final CameraStatusVO cameraStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, finalChannelId);
                    cameraStatusVO.setEnableValue(closeCameraValue);
                    cameraStatusVO.setEnableType(CAPABILITY.CLOSE_CAMERA);
                    cameraStatusVO.setSaveKey(closeCameraKey);
                    cameraStatusVO.setTargetIV(holder.ivCloseCheck);

                    if(closeCameraValue) {
                        holder.ivBg.setImageResource(R.mipmap.lc_demo_default_bg);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    setDeviceCameraStatus(cameraStatusVO);
                }
            });
        }

        holder.llReverseImage.setVisibility(View.GONE);
        holder.llPrivacyReverse.setVisibility(View.GONE);
        if(!listBean.deviceStatus.equals("EMPTY")) {
            holder.llPrivacyReverse.setVisibility(View.VISIBLE);

            /** 장비의 스마트트랙 셋팅 값 조회 **/
            CameraStatusVO cameraStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId);
            cameraStatusVO.setEnableType(CAPABILITY.SMART_TRACK);
            getDeviceCameraStatus(cameraStatusVO);

            /** 장비의 이미지회전 셋팅 값 조회 **/
            CameraStatusVO reverseCameraStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, channelId);
            reverseCameraStatusVO.setTargetIV(holder.ivReverseCheck);
            frameReverseStatus(reverseCameraStatusVO);
            if(listBean.deviceStatus.equals("online")) {
                holder.llReverseImage.setVisibility(View.VISIBLE);
            } else {
                holder.llPrivacyReverse.setVisibility(View.GONE);
            }

            holder.tvName.setText(tvNameStr);
            holder.tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.frDetail.getVisibility() == View.VISIBLE) {
                        holder.frDetail.setVisibility(View.GONE);
                    } else {
                        holder.frDetail.setVisibility(View.VISIBLE);
                    }
                }
            });

            holder.ivDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onSettingClick(position);
                    }
                }
            });

            holder.llReverseImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.ivReverseCheck.callOnClick();
                }
            });

            /** 이미지 회전 **/
            String finalChannelId1 = channelId;
            holder.ivReverseCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.show((Activity) mContext);
                    boolean reverseImageValue = PreferencesHelper.getInstance(mContext).getBoolean(reverseImageKey, true);
                    reverseImageValue = !reverseImageValue;
                    String direction = (reverseImageValue == true) ? "reverse" : "normal";

                    CameraStatusVO cameraStatusVO = DeviceAbilityHelper.createCameraStatusVO(deviceId, finalChannelId1);
                    cameraStatusVO.setDirection(direction);
                    cameraStatusVO.setEnableType(CAPABILITY.MODIFY_FRAME_REVERSE_STATUS);

                    final boolean finalReverseValue = reverseImageValue;
                    DeviceCameraStatusService.getInstance().modifyFrameReverseStatus(cameraStatusVO, new IDeviceCameraStatus.CommonCallback() {
                        @Override
                        public void onSuccess(String response) {
                            PreferencesHelper.getInstance(mContext).set(reverseImageKey, finalReverseValue);
                            drawImageView(holder.ivReverseCheck, finalReverseValue);
                            DialogUtils.dismiss();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            DialogUtils.dismiss();
                        }
                    });
                }
            });
        }

        holder.rlOffline.setVisibility(View.VISIBLE);
        if (datas.get(position).channelList != null && datas.get(position).channelList.size() > 1) {
            //多通道

            ivDetailVisible(holder, datas.get(position).isShared());

            holder.rlDetail.setVisibility(View.GONE);
            holder.to_msg.setVisibility(View.GONE);
            holder.rcvChannel.setVisibility(View.VISIBLE);
            holder.rlOffline.setVisibility(View.GONE);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 2);
            gridLayoutManager.setOrientation(GridLayoutManager.HORIZONTAL);
            holder.rcvChannel.setLayoutManager(gridLayoutManager);
            holder.ivDetail.post(() -> {
                ChannelListAdapter channelListAdapter = new ChannelListAdapter(mContext, datas.get(position).channelList, datas.get(position).deviceId);
                channelListAdapter.setParentViewWight(holder.itemView.getMeasuredWidth());
                holder.rcvChannel.setAdapter(channelListAdapter);
                holder.rcvChannel.removeItemDecoration(decoration);
                holder.rcvChannel.addItemDecoration(decoration);
                channelListAdapter.setOnItemClickListener(new ChannelListAdapter.OnChannelClickListener() {
                    @Override
                    public void onChannelClick(int channelPosition) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onChannelClick(position, channelPosition);
                        }
                    }

                    @Override
                    public void onMessageClick(int channelPosition) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onMessageClick(position, channelPosition);
                        }
                    }
                });
            });
        } else if (datas.get(position).channelList != null && datas.get(position).channelList.size() == 0) {
            //多通道NVR,但是没有通道数
            ivDetailVisible(holder, datas.get(position).isShared());

            holder.ivCloseCheck.setVisibility(View.GONE);
//            holder.ivReverseCheck.setVisibility(View.GONE);

            holder.ivPlay.setVisibility(View.GONE);
            holder.to_msg.setVisibility(View.GONE);
            holder.rcvChannel.setVisibility(View.GONE);
            holder.rlDetail.setVisibility(View.VISIBLE);
            holder.ivBg.setImageResource(R.mipmap.lc_demo_default_bg);

            holder.rlOffline.setVisibility(View.VISIBLE);
            if(!listBean.deviceStatus.equals("EMPTY")) {
                holder.rlOffline.setBackground(mContext.getDrawable(R.color.transparent));
                holder.tvOffline.setText(R.string.lc_demo_device_nvr_no_channel);
            }

            holder.rlDetail.setOnClickListener(null);
        } else {
            //单通道
            ivDetailVisible(holder, datas.get(position).isShared());

            holder.to_msg.setVisibility(View.VISIBLE);
            holder.rlDetail.setVisibility(View.VISIBLE);
            holder.rcvChannel.setVisibility(View.GONE);
            holder.to_msg.setOnClickListener((view) -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onMessageClick(position, datas.get(position).checkedChannel);
                }
            });
            if ("online".equals(datas.get(position).getDeviceStatus()) && datas.get(position).channelList != null && datas.get(position).channelList.size() > 0) {
                holder.ivPlay.setVisibility(View.VISIBLE);

                holder.ivCloseCheck.setVisibility(View.VISIBLE);
                holder.ivReverseCheck.setVisibility(View.VISIBLE);

                holder.rlOffline.setVisibility(View.GONE);
                holder.rlOffline.setBackground(mContext.getDrawable(R.color.transparent));
                holder.rlDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onDetailClick(position);
                        }
                    }
                });
            } else {
                holder.ivPlay.setVisibility(View.GONE);

                holder.ivCloseCheck.setVisibility(View.GONE);
//                holder.ivReverseCheck.setVisibility(View.GONE);

                holder.rlOffline.setVisibility(View.VISIBLE);
                holder.rlOffline.setBackground(mContext.getDrawable(R.drawable.lc_demo_soild_20r_c66000000_shape));
                holder.tvOffline.setText(R.string.lc_demo_main_offline);
                if (ProviderManager.getAppProvider().getAppType() == LCConfiguration.APP_LECHANGE_OVERSEA) { // 海外   显示0时区时间
//                    holder.offlineTime.setText(TimeUtils.changeTimeFormat2StandardMinByDateFormat(datas.get(position).lastOffLineTime, "MM-dd HH:mm:ss"));
                } else {
//                    holder.offlineTime.setText(TimeUtils.changeTimeFormat2StandardNoYear(datas.get(position).lastOffLineTime)); //国内
                }
                holder.rlDetail.setOnClickListener(null);
            }
            //获取设备缓存信息
            DeviceLocalCacheData deviceLocalCacheData = new DeviceLocalCacheData();
            deviceLocalCacheData.setDeviceId(datas.get(position).deviceId);
            if (datas.get(position).channelList != null && datas.get(position).channelList.size() > 0) {
                deviceLocalCacheData.setChannelId(datas.get(position).channelList.get(datas.get(position).checkedChannel).channelId);
            }

            String captureFilePath = null;
            String channelName = null;
            if (datas.get(position).channelList != null && datas.get(position).channelList.size() > 0) {
                channelName = datas.get(position).deviceId + "&" + datas.get(position).channelList.get(datas.get(position).checkedChannel).channelId;
            } else {
                channelName = datas.get(position).deviceId;
            }
            // 去除通道中在目录中的非法字符
            channelName = channelName.replace("-", "");
            captureFilePath =
                    com.lc.media.utils.MediaPlayHelper.getDevicesListImageCachePath(MediaPlayHelper.LCFilesType.LCImageCache, channelName);
            if (TextUtils.isEmpty(captureFilePath)) {
                holder.ivBg.setImageResource(R.mipmap.lc_demo_default_bg);
            } else {
                holder.ivBg.setImageURI(Uri.fromFile(new File(captureFilePath)));
            }
        }
    }


    private void ivDetailVisible(DeviceListHolder holder, boolean shared) {
        if(shared) {
            holder.ivDetail.setVisibility(View.GONE);
        } else {
            holder.ivDetail.setVisibility(View.VISIBLE);
        }
    }


    private void frameReverseStatus(final CameraStatusVO cameraStatusVO) {
        DeviceCameraStatusService.getInstance().frameReverseStatus(cameraStatusVO, new IDeviceCameraStatus.CommonCallback() {
            @Override
            public void onSuccess(String response) {
                if(response == null || response.equals("")) {
                    DialogUtils.dismiss();
                    return;
                }
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);

                String direction = resultJson.get("direction").getAsString();

                boolean b = false;
                if(direction.equalsIgnoreCase("reverse")) b = true;

                String key = cameraStatusVO.getChannelId()+"_"+cameraStatusVO.getDeviceId()+"_"+ CAPABILITY.MODIFY_FRAME_REVERSE_STATUS;
                PreferencesHelper.getInstance(mContext).set(key, b); // 장비에서 읽어온 값으로 변수 값 셋팅.
                drawImageView(cameraStatusVO.getTargetIV(), b);

                DialogUtils.dismiss();
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();
            }
        });
    }

    private void setDeviceCameraStatus(final CameraStatusVO cameraStatusVO) {
        DeviceCameraStatusService.getInstance().setDeviceCameraStatus(cameraStatusVO, new IDeviceCameraStatus.CommonCallback() {
            @Override
            public void onSuccess(String response) {
                String key = cameraStatusVO.getSaveKey();
                boolean value = cameraStatusVO.isEnableValue();
                ImageView targetIV = cameraStatusVO.getTargetIV();

                if(key == null || targetIV == null) {
                    DialogUtils.dismiss();
                    return;
                }

                PreferencesHelper.getInstance(mContext).set(key, value);

                if(key.contains(CAPABILITY.CLOSE_CAMERA)) {
                    PreferencesHelper.getInstance(mContext).set(key+"_CLOSE", value);
                }

                drawImageView(targetIV, value);
                DialogUtils.dismiss();

//                if(key.equals(CAPACBILITY.CLOSE_CAMERA)) {
//                    disableSmartTrack(cameraStatusVO);
//                }
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();
            }
        });
    }

    private void getDeviceCameraStatus(final CameraStatusVO cameraStatusVO) {
        DialogUtils.show((Activity) mContext);
        DeviceCameraStatusService.getInstance().getDeviceCameraStatus(cameraStatusVO, new IDeviceCameraStatus.CommonCallback() {
            @Override
            public void onSuccess(String response) {
                if(response == null || response.equals("")) {
                    DialogUtils.dismiss();
                    return;
                }
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);

                String enableType = resultJson.get("enableType").getAsString();
                String status = resultJson.get("status").getAsString();

                if(enableType != null && !enableType.equals("")) {
                    boolean b = false;
                    if(status.equalsIgnoreCase("on")) b = true;

                    String key = cameraStatusVO.getChannelId()+"_"+cameraStatusVO.getDeviceId()+"_"+enableType;
                    PreferencesHelper.getInstance(mContext).set(key, b); // 장비에서 읽어온 값으로 변수 값 셋팅.
                    drawImageView(cameraStatusVO.getTargetIV(), b); // 읽어온 기능의 기본값 셋팅
                }

                DialogUtils.dismiss();
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (datas == null || datas.size() == 0) ? 1 : datas.size();
    }

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onAddClick();

        void onMessageClick(int outPosition, int innerPosition);

        void onSettingClick(int position);

        void onDetailClick(int position);

        void onChannelClick(int outPosition, int innerPosition);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class DeviceListHolder extends RecyclerView.ViewHolder {
        LinearLayout llCloseCamera;
        LinearLayout llReverseImage;
        LinearLayout llEmptySpace;
        LinearLayout llTitle;
        LinearLayout llPrivacyReverse;

        TextView tvName;
        TextView tvSharedInfo;
        ImageView ivDetail;
        ImageView ivBg;
        RelativeLayout rlOffline;
        TextView tvOffline;
        TextView offlineTime;
        ImageView ivPlay;
        ImageView ivAdd;
        ImageView ivCloseCheck;
        ImageView ivReverseCheck;
        RelativeLayout rlDetail;
        RecyclerView rcvChannel;
        FrameLayout frDetail;
        ImageView to_msg;
        Context context;
        int viewType;
        LinearLayout llWrap;


        public void init() {
            tvName.setText("");
            tvSharedInfo.setText("");
            llWrap.setVisibility(View.GONE);
            tvSharedInfo.setVisibility(View.GONE);
        }

        public DeviceListHolder(Context context, View itemView,int viewType) {
            super(itemView);
            this.viewType=viewType;
            if(viewType==-1)return;

            this.context = context;
            llTitle = itemView.findViewById(R.id.ll_title);
            llCloseCamera = itemView.findViewById(R.id.ll_close_camera);
            llReverseImage = itemView.findViewById(R.id.ll_reverse_image);
            llEmptySpace = itemView.findViewById(R.id.ll_empty_space);
            llReverseImage.setVisibility(View.VISIBLE);
            llWrap = itemView.findViewById(R.id.ll_wrap);
            llPrivacyReverse = itemView.findViewById(R.id.ll_privacy_reverse);

            tvSharedInfo = itemView.findViewById(R.id.tv_sharedInfo);

            tvName = itemView.findViewById(R.id.tv_name);
            ivDetail = itemView.findViewById(R.id.iv_detail);
            ivBg = itemView.findViewById(R.id.iv_bg);
            ivAdd = itemView.findViewById(R.id.iv_add);
            rlDetail = itemView.findViewById(R.id.rl_detail);
            frDetail = itemView.findViewById(R.id.fr_detail);
            rlOffline = itemView.findViewById(R.id.rl_offline);
            tvOffline = itemView.findViewById(R.id.tv_offline);
            ivPlay = itemView.findViewById(R.id.iv_play);

            ivCloseCheck = itemView.findViewById(R.id.iv_close_check);
            ivReverseCheck = itemView.findViewById(R.id.iv_reverse_check);

            rcvChannel = itemView.findViewById(R.id.rcv_channel);
            offlineTime = itemView.findViewById(R.id.tv_offline_time);
            to_msg = itemView.findViewById(R.id.to_msg);
            ivBg.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                RelativeLayout.LayoutParams mLayoutParams = (RelativeLayout.LayoutParams) ivBg.getLayoutParams();
                mLayoutParams.height = mLayoutParams.width * 9 / 16;
                mLayoutParams.width = frDetail.getWidth(); // 屏幕宽度（像素）
                mLayoutParams.setMargins(0, 0, 0, 0);
                ivBg.setLayoutParams(mLayoutParams);
            });
            rlOffline.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                RelativeLayout.LayoutParams mLayoutParams = (RelativeLayout.LayoutParams) rlOffline.getLayoutParams();
                mLayoutParams.height = ivBg.getHeight();
                mLayoutParams.width = ivBg.getWidth();
                mLayoutParams.setMargins(0, 0, 0, 0);
                rlOffline.setLayoutParams(mLayoutParams);
            });
        }
    }
}
