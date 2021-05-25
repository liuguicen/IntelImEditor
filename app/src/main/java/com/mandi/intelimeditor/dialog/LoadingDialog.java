package com.mandi.intelimeditor.dialog;


import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.mandi.intelimeditor.dialog.IBaseDialog;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.Nullable;


/**
 * 作者：yonglong
 * 包名：com.mandi.intelimeditor.widget
 * 时间：2019/4/3 13:21
 * 描述：
 */
public class LoadingDialog extends IBaseDialog {
    private TextView tv, progressTv;
    private String loadingInfo = "";
    private String progress = "";
    private static final String MSG_KEY = "loading";

    public static LoadingDialog newInstance(String loadingInfo) {
        Bundle args = new Bundle();
        LoadingDialog fragment = new LoadingDialog();
        args.putString(MSG_KEY, loadingInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.loading_ptu_layout;
    }

    public void setLoadingInfo(String loadingInfo) {
        this.loadingInfo = loadingInfo;
        if (tv != null) {
            tv.setText(loadingInfo);
        }
    }

    public void setProgress(String progress) {
        this.progress = progress;
        if (progressTv != null) {
            progressTv.setText(progress);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loadingInfo = getArguments().getString(MSG_KEY);
        }
    }

    @Override
    public void onActivityCreated(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getRootView() != null && !TextUtils.isEmpty(loadingInfo)) {
            tv = getRootView().findViewById(R.id.progressTips);
            progressTv = getRootView().findViewById(R.id.progressTv);
            tv.setText(loadingInfo);
        }
    }
}
