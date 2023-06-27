package com.opensdk.devicedetail.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mm.android.mobilecommon.pps.GlobalFunc;
import com.mm.android.mobilecommon.pps.dialog.PPSConfirmDialog;
import com.mm.android.mobilecommon.pps.model.ShareInfo;
import com.mm.android.mobilecommon.utils.DateHelper;
import com.opensdk.devicedetail.R;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DeviceShareAdapter extends RecyclerView.Adapter<DeviceShareAdapter.ShareHolder> implements PPSConfirmDialog.OnOkClickListener {
    private Context context;
    private List<ShareInfo> datas;
    private PPSConfirmDialog ppsConfirmDialog;
    public DeviceShareAdapter(Context context, List<ShareInfo> list) {
        this.context = context;
        this.datas = list;
    }

    public ShareInfo getItem(int position) {
        return this.datas.get(position);
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    @NonNull
    @Override
    public ShareHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_device_share_item, parent, false);
        return new ShareHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ShareHolder holder, @SuppressLint("RecyclerView") final int position) {
        final ShareInfo item = this.datas.get(position);

        holder.tvEmail.setText(item.getEmail());
        String insDt = DateHelper.convertTime(item.getInsDt(), TimeZone.getDefault());

        String dt = context.getString(R.string.lc_shared_dt_info);
        dt = dt.replaceAll("::shared_dt::", insDt);
        holder.tvDt.setText(dt);

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onShareItemClickListener != null) {
                    onShareItemClickListener.onShareItemClick(position);
                }
            }
        });
    }

    @Override
    public void OnOK(String title, Map<String, Object> param) {
        ShareInfo shareInfo = (ShareInfo) param.get("shareInfo");
        GlobalFunc.showToast(context, shareInfo.getEmail());
    }

    static class ShareHolder extends RecyclerView.ViewHolder {
        TextView tvEmail;
        TextView tvDt;
        Button btnDelete;

        public ShareHolder(View itemView) {
            super(itemView);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvDt = itemView.findViewById(R.id.tv_dt);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private OnShareItemClickListener onShareItemClickListener;

    public interface OnShareItemClickListener {
        void onShareItemClick(int position);
    }

    public void setOnShareItemClickListener(OnShareItemClickListener onShareItemClickListener) {
        this.onShareItemClickListener = onShareItemClickListener;
    }

}
