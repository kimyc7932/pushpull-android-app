package com.pushpull.camapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mm.android.mobilecommon.pps.utils.Log;
import com.mm.android.mobilecommon.utils.DateHelper;
import com.pushpull.camapp.R;

import java.util.ArrayList;
import java.util.TimeZone;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.NoticeHolder> {
    private Context mContext;
    private ArrayList<NoticeItem> datas;

    public NoticeListAdapter(Context mContext, ArrayList<NoticeItem> datas) {
        this.mContext = mContext;
        this.datas = datas;
    }

    @NonNull
    @Override
    public NoticeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_notice_list, parent, false);
        return new NoticeHolder(view);
    }

    private int getInt(String s) {
        return Integer.parseInt(s);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoticeHolder holder, @SuppressLint("RecyclerView") final int position) {
        NoticeItem item = this.datas.get(position);
        System.out.println(TimeZone.getDefault().getID());
        String insDt = DateHelper.convertTime(item.ins_dt, TimeZone.getDefault());

        item.conv_ins_dt = insDt;
        JsonObject logJson = new Gson().fromJson(item.logmsg, JsonObject.class);

        holder.tvDate.setText(insDt);
        holder.tvDeviceName.setText(item.device_name);
        holder.tvLogText.setText(logJson.get("labelType").getAsString());

        String thumbUrl = logJson.get("thumbUrl").getAsString();
        String deviceId = logJson.get("did").getAsString();
        int cid = logJson.get("cid").getAsInt();

        item.deviceId = deviceId;
        item.channelId = cid+"";


//        holder.ivThumb.setImageDrawable(mContext.getDrawable(R.mipmap.lc_demo_default_bg));
        Log.d("NoticeListAdapter", "onBindViewHolder !!!!!! "+position);

//        ImageHelper.loadCacheImage(thumbUrl, deviceId, deviceId, cid, new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                try {
//                    Glide.with(mContext).load((Drawable) msg.obj).placeholder(R.mipmap.lc_demo_loading_orange).override(100, 100).into(holder.ivThumb);
////                    Glide.with(mContext).load((Drawable) msg.obj).placeholder(R.drawable.mobile_common_progress_loading_img).into(holder.ivThumb);
////                    holder.ivThumb.setImageDrawable((Drawable) msg.obj);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onNoticeClickListener != null) {
                    onNoticeClickListener.onNoticeClick(position);
                }
            }
        });
    }

    public NoticeItem getItem(int position) {
        return this.datas.get(position);
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    static class NoticeHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        TextView tvDeviceName;
        TextView tvLogText;
        ImageView ivThumb;

        public NoticeHolder(View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            tvLogText = itemView.findViewById(R.id.tv_log_text);
        }
    }

    private OnNoticeClickListener onNoticeClickListener;

    public interface OnNoticeClickListener {
        void onNoticeClick(int position);
    }

    public void setOnItemClickListener(OnNoticeClickListener onNoticeClickListener) {
        this.onNoticeClickListener = onNoticeClickListener;
    }
}
