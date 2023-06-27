package com.mm.android.mobilecommon.pps.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mm.android.mobilecommon.R;

import java.util.HashMap;
import java.util.Map;

public class PPSConfirmDialog extends Dialog {
    private TextView tv_title;
    private TextView tv_msg;
    private TextView btn_ok;
    private TextView btn_cancel;

    private Map<String, Object> param = new HashMap<>();

    private String dialogTitle;
    private String dialogMsg;

    public PPSConfirmDialog(Context context, String dialogTitle, String dialogMsg) {
        super(context, R.style.sign_dialog);
        this.dialogTitle = dialogTitle;
        this.dialogMsg = dialogMsg;
    }

    public void addParam(String key, Object value) {
        param.put(key, value);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sdcard_format);

        tv_title = findViewById(R.id.tv_title);
        tv_msg = findViewById(R.id.tv_msg);

        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnOkClickLisenter != null) {
                    mOnOkClickLisenter.OnOK(dialogTitle, param);
                }
                dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnCancelClickListener != null) {
                    mOnCancelClickListener.OnCancel(dialogTitle, param);
                }
                dismiss();
            }
        });

        tv_title.setText(dialogTitle);
        tv_msg.setText(dialogMsg);
    }

    public interface OnOkClickListener {
        void OnOK(String title, Map<String, Object> param);
    }
    public interface OnCancelClickListener {
        void OnCancel(String title, Map<String, Object> param);
    }

    private OnOkClickListener mOnOkClickLisenter;
    private OnCancelClickListener mOnCancelClickListener;

    public void setOnOkClickListener(OnOkClickListener lisenter) {
        this.mOnOkClickLisenter = lisenter;
    }
    public void setOnCancelClickListener(OnCancelClickListener lisenter) {
        this.mOnCancelClickListener = lisenter;
    }

}
