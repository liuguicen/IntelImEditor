package com.mandi.intelimeditor.dialog;


import android.os.Bundle;
import android.widget.TextView;

import com.mathandintell.intelimedit.dialog.IBaseDialog;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.Nullable;



/**
 * 作者：yonglong
 * 包名：a.baozouptu.widget
 * 时间：2019/4/3 13:21
 * 描述：
 */
public class LoadingDialog extends IBaseDialog {
    private TextView tv;
    private String loadingInfo = "";
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
        return R.layout.loading_layout;
    }


    public void setLoadingInfo(String loadingInfo) {
        this.loadingInfo = loadingInfo;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            loadingInfo = getArguments().getString(MSG_KEY);
        }
        if (loadingInfo == null || loadingInfo.length() == 0) {

        }
    }

    @Override
    public void onActivityCreated(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getRootView() != null && !loadingInfo.isEmpty()) {
            tv = getRootView().findViewById(R.id.progressTips);
            tv.setText(loadingInfo);
        }
    }
}
