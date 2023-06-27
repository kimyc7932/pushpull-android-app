package com.pushpull.camapp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.common.openapi.entity.DeviceAlarmService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lc.media.api.DeviceRecordService;
import com.lc.media.api.IGetDeviceInfoCallBack;
import com.lc.media.entity.LocalRecordsData;
import com.lc.media.entity.RecordsData;
import com.lc.playback.ui.DeviceRecordPlayActivity;
import com.mm.android.mobilecommon.base.BaseActivity;
import com.mm.android.mobilecommon.entity.device.DeviceDetailListData;
import com.mm.android.mobilecommon.pps.ACONST;
import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.cont.MethodConst;
import com.mm.android.mobilecommon.pps.dialog.PPSConfirmDialog;
import com.mm.android.mobilecommon.pps.model.AlarmVO;
import com.mm.android.mobilecommon.pps.provider.AsyncJSON;
import com.mm.android.mobilecommon.pps.provider.Processable;
import com.mm.android.mobilecommon.pps.service.IDeviceOperationCallback;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.DialogUtils;
import com.mm.android.mobilecommon.utils.ImageHelper;
import com.pushpull.camapp.R;
import com.pushpull.camapp.adapter.NoticeItem;
import com.pushpull.camapp.adapter.NoticeListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoticeActivity extends BaseActivity implements View.OnClickListener, PPSConfirmDialog.OnOkClickListener, NoticeListAdapter.OnNoticeClickListener {
    public static final String TAG = NoticeActivity.class.getSimpleName();
    RecyclerView m_uiRecyclerView;
    ArrayList<NoticeItem> m_NoticeDataList;
    NoticeListAdapter m_NoticeListAdapter;
    LinearLayout llDelete;
    ImageView ivImage;
    private List<String> idList;
    Context m_context;
    private PPSConfirmDialog ppsConfirmDialog;
    private LinearLayout llBack;
    private Handler handler;
    private DeviceRecordService deviceRecordService = new DeviceRecordService();
    List<DeviceDetailListData.ResponseData.DeviceListBean> mCameraDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);

        Bundle bundle = getIntent().getExtras();
        mCameraDatas = (List<DeviceDetailListData.ResponseData.DeviceListBean>) bundle.getSerializable("mCameraDatas");

        initView();
        initData();
    }

    void initView() {
        m_uiRecyclerView = findViewById(R.id.rcv_notice);
        llBack = findViewById(R.id.ll_back);

        llDelete = findViewById(R.id.ll_delete);

        m_uiRecyclerView.setLayoutManager(new LinearLayoutManager(NoticeActivity.this));
        llBack.setOnClickListener(this);
        llDelete.setOnClickListener(this);

//        deviceListBean = (DeviceDetailListData.ResponseData.DeviceListBean) arguments.getSerializable(MethodConst.ParamConst.deviceDetail);

        handler = new Handler();

//        getAlarmMessage();
//
//
//        ImageHelper.loadCacheImage(thumbUrl, deviceId, deviceId, 0, new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                try {
//                    ivImage.setImageDrawable((Drawable) msg.obj);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    private void getLocalData(final NoticeItem noticeItem){
        final LocalRecordsData localRecordsData = new LocalRecordsData();
        localRecordsData.data.deviceId = noticeItem.deviceId;
        localRecordsData.data.channelId = noticeItem.channelId;
        localRecordsData.data.beginTime = ACONST.calculateSecondTime(noticeItem.conv_ins_dt +":00", -30);
        localRecordsData.data.endTime = ACONST.calculateSecondTime(noticeItem.conv_ins_dt +":00", 30);
        localRecordsData.data.type = "All";
        localRecordsData.data.queryRange =  1 + "-" + (1 + 30 - 1);
        deviceRecordService.queryLocalRecords(localRecordsData, new IGetDeviceInfoCallBack.IDeviceLocalRecordCallBack() {
            @Override
            public void deviceLocalRecord(LocalRecordsData.Response result) {
                if(result == null || result.data == null || result.data.records == null) {
                    Toast.makeText(getApplicationContext(), "No data", Toast.LENGTH_SHORT).show();
                    DialogUtils.dismiss();
                    return;
                }

                LocalRecordsData.ResponseData.RecordsBean recordsBean = result.data.records.get(0);

                RecordsData recordData = new RecordsData();
                recordData.beginTime = recordsBean.beginTime;
                recordData.channelId = recordsBean.channelID;
                recordData.channelID = recordsBean.channelID;
                recordData.deviceId = noticeItem.deviceId;
                recordData.endTime = recordsBean.endTime;
                recordData.fileLength = recordsBean.fileLength;
                recordData.recordId = recordsBean.recordId;
                recordData.type = recordsBean.type;

                Bundle bundle = new Bundle();
                bundle.putSerializable(MethodConst.ParamConst.deviceDetail, getDeviceListBean(noticeItem.deviceId));
                bundle.putSerializable(MethodConst.ParamConst.recordData, recordData);
                bundle.putInt(MethodConst.ParamConst.recordType, MethodConst.ParamConst.recordTypeLocal);
                Intent intent = new Intent(getApplicationContext(), DeviceRecordPlayActivity.class);
                intent.putExtras(bundle);
                DialogUtils.dismiss();
                startActivity(intent);
            }

            @Override
            public void onError(Throwable throwable) {
                DialogUtils.dismiss();;
                Toast.makeText(getApplicationContext(), "No data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getAlarmMessage() {
        final String deviceId = "PPSTV2000L101S00418";
        AlarmVO alarmVO = new AlarmVO();
        alarmVO.setChannelId("0");
        alarmVO.setDeviceId(deviceId);
        alarmVO.setBeginTime("2022-07-14 17:40:00");
        alarmVO.setEndTime("2022-07-14 17:50:00");
        DeviceAlarmService.getInstance().getAlarmMessage(alarmVO, new IDeviceOperationCallback() {
            @Override
            public void onSuccess(String response) {
                JsonObject resultJson = new Gson().fromJson(response, JsonObject.class);
//                System.out.println("getAlarmMessage.onSuccess ===> " + resultJson);

                JsonArray alarms = resultJson.getAsJsonArray("alarms");

                JsonObject alarm = alarms.get(0).getAsJsonObject();
                ImageHelper.loadCacheImage(alarm.get("thumbUrl").getAsString(), alarm.get("deviceId").getAsString(), alarm.get("deviceId").getAsString(), 0, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        try {
                            ivImage.setImageDrawable((Drawable) msg.obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {

            }
        });
    }

    private DeviceDetailListData.ResponseData.DeviceListBean getDeviceListBean(String deviceId) {
        if(mCameraDatas == null) return null;
        for(DeviceDetailListData.ResponseData.DeviceListBean listBean : mCameraDatas) {
            if(listBean.deviceId.equals(deviceId)) return listBean;
        }

        return null;
    }

    void initData() {
        m_context = getApplicationContext();

        m_NoticeDataList = new ArrayList<>();
        m_NoticeListAdapter = new NoticeListAdapter(m_context, m_NoticeDataList);
        m_uiRecyclerView.setAdapter(m_NoticeListAdapter);
        m_NoticeListAdapter.setOnItemClickListener(this);

        getNoticeData();
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.ll_back:
            {
                finish();
                break;
            }
            case R.id.ll_delete:
            {
                if(idList == null || idList.size() == 0) return;

                ppsConfirmDialog = new PPSConfirmDialog(this, getString(R.string.lc_notice_delete_dialog_title), getString(R.string.lc_notice_delete_dialog_msg));
                ppsConfirmDialog.setOnOkClickListener(this);

                ppsConfirmDialog.show();
            }
        }
    }

    void getNoticeData() {
        this.idList = new ArrayList<>();
        DialogUtils.show(this);

        final String reqUrl = ACONST.API_NOTICE_LIST;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("type", "2");
            reqBodyJson.put("user_id", ACONST.LOGIN_UID);
            reqBodyJson.put("event_type", "3");
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
                            JSONArray jArraydata = param.getJSONArray("data");
                            for( int i=0; i < jArraydata.length(); i++){
                                JSONObject jobj = jArraydata.getJSONObject(i);
                                final NoticeItem item = new NoticeItem();

                                item.event_id = jobj.getString("id");
                                item.event_type = jobj.getInt("event_type");
                                item.event_stype = jobj.getInt("event_stype");
                                item.device_name = jobj.getString("device_name");
                                item.logmsg=jobj.getString("logmsg");
                                item.ins_dt = jobj.getString("ins_dt");
                                item.ins_dt = item.ins_dt.substring(0,16).replace('T',' ');
                                m_NoticeDataList.add(item);

                                JsonObject logJson = new Gson().fromJson(item.logmsg, JsonObject.class);
                                String thumbUrl = logJson.get("thumbUrl").getAsString();
                                String deviceId = logJson.get("did").getAsString();
                                int cid = logJson.get("cid").getAsInt();

//                                ImageHelper.loadCacheImage(thumbUrl, deviceId, deviceId, cid, new Handler() {
//                                    @Override
//                                    public void handleMessage(Message msg) {
//                                        super.handleMessage(msg);
//                                        try {
//                                            item.thumb = (Drawable) msg.obj;
////                                            Glide.with(getApplicationContext()).load((Drawable) msg.obj).placeholder(R.mipmap.lc_demo_loading_orange).override(100, 100).into(ivThumb);
////                    Glide.with(mContext).load((Drawable) msg.obj).placeholder(R.drawable.mobile_common_progress_loading_img).into(holder.ivThumb);
////                    holder.ivThumb.setImageDrawable((Drawable) msg.obj);
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                    }
//                                });

                                idList.add(item.event_id);
                            }
                        }

                        Log.d(TAG, "data list size = "+m_NoticeDataList.size());
                        m_NoticeListAdapter.notifyDataSetChanged();

                    } catch (JSONException ex) {
                    }
                }
            });
        } catch (JSONException ex){
        }
    }

    @Override
    public void OnOK(String title, Map<String, Object> param) {
        DialogUtils.show(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                noticeDelete();
            }
        },200);
    }

    private String makeDeleteIds() {
        String idStr = "";
        for(String id : idList) {
            idStr += id+"|";
        }

        idStr = idStr.trim();
        if(idStr.length() > 0) idStr = idStr.substring(0, idStr.length()-1);

        return idStr;
    }

    Toast noticeToast;
    void noticeDelete() {
        String idStr = makeDeleteIds();
        if(idStr == null || idStr.equals("")) return;

        final String reqUrl = ACONST.API_NOTICE_DELETE;
        String regBody = "";
        JSONObject reqBodyJson = new JSONObject();

        try {
            reqBodyJson.put("type", "2");
            reqBodyJson.put("user_id", ACONST.LOGIN_UID);
            reqBodyJson.put("delete_ids", idStr);
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
                            if(noticeToast != null) noticeToast.cancel();
                            noticeToast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.lc_notice_delete_ok), Toast.LENGTH_SHORT);
                            noticeToast.show();
                            m_NoticeDataList.clear();
                            m_NoticeListAdapter.notifyDataSetChanged();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getNoticeData();
                                }
                            },100);
                        }
                    } catch (JSONException ex) {
                    }
                }
            });
        } catch (JSONException ex){
        }
    }

    @Override
    public void onNoticeClick(int position) {
        DialogUtils.show(this);
        NoticeItem noticeItem = m_NoticeListAdapter.getItem(position);
        getLocalData(noticeItem);
    }
}
