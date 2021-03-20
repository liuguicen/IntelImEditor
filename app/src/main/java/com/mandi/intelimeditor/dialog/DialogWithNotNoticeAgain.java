package com.mandi.intelimeditor.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.dialog.IBaseDialog;
import com.mandi.intelimeditor.R;


/**
 * 对话框，带有不再提示的按钮
 */
public class DialogWithNotNoticeAgain extends IBaseDialog {

    private View rootView;
    private String title;
    private String content;
    private String verifyName = "确定";
    private String cancelName = "取消";
    private CheckBox notNoticeCb;
    private TextView contentTv;
    private TextView titleTv;
    private TextView cancelBtn;
    private TextView verifyBtn;
    private WithNotNoticeListener listener;

    @Override
    public int getLayoutResId() {
        return R.layout.dialog_with_do_not_notice;
    }

    public DialogWithNotNoticeAgain(String title, String content, String verifyName, String cancelName) {
        this.title = title;
        this.content = content;
        if (verifyName != null)
            this.verifyName = verifyName;
        if (cancelName != null)
            this.cancelName = cancelName;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rootView = getRootView();
        if (rootView == null) return;
        bindView(rootView);
        initView();
    }

    private void bindView(View rootView) {
        notNoticeCb = rootView.findViewById(R.id.not_notice_cb);
        titleTv = rootView.findViewById(R.id.not_notice_title);
        contentTv = rootView.findViewById(R.id.not_notice_content);
        cancelBtn = rootView.findViewById(R.id.not_notice_cancel);
        verifyBtn = rootView.findViewById(R.id.not_notice_verify);
    }

    private void initView() {
        titleTv.setText(title);
        contentTv.setText(content);
        cancelBtn.setText(cancelName);
        cancelBtn.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.cancel(notNoticeCb.isChecked());
            }
        });
        verifyBtn.setText(verifyName);
        verifyBtn.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.verify(notNoticeCb.isChecked());
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if (listener != null) {
            listener.cancel(notNoticeCb.isChecked());
        }
        return false;
    }

    public void setNotNoticeListener(WithNotNoticeListener listener) {
        this.listener = listener;
    }


    public interface WithNotNoticeListener {
        void cancel(boolean isNotNotice);

        void verify(boolean isNotNotice);
    }

}
