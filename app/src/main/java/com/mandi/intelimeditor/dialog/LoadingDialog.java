package com.mandi.intelimeditor.dialog;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mandi.intelimeditor.dialog.IBaseDialog;
import com.mandi.intelimeditor.R;
import com.wang.avi.AVLoadingIndicatorView;

import org.jetbrains.annotations.NotNull;
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
            progressTv.setVisibility(View.VISIBLE);
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

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setCancelable(false);
        if (getRootView() != null) {
            tv = getRootView().findViewById(R.id.progressTips);
            AVLoadingIndicatorView avi = getRootView().findViewById(R.id.progressBar1);
            avi.show();
            progressTv = getRootView().findViewById(R.id.progressTv);
            if (!TextUtils.isEmpty(loadingInfo)) {
                tv.setText(loadingInfo);
            }
        }
        return view;
    }
}
