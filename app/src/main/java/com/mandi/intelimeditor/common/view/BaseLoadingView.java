package com.mandi.intelimeditor.common.view;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mandi.intelimeditor.R;


public class BaseLoadingView {

    private View loadingLayout;
    private View loadingProgressBar;
    private TextView loadingTv;

    public BaseLoadingView(View rootView) {
        initLoadView(rootView);
    }

    private void initLoadView(View rootView) {
        loadingLayout = rootView.findViewById(R.id.loadingLayout);
        loadingProgressBar = rootView.findViewById(R.id.progressBar);
        loadingTv = rootView.findViewById(R.id.loadingTv);
    }

    public void showLoading(boolean show) {
        if (loadingLayout != null) {
            if (show) {
                loadingLayout.setVisibility(View.VISIBLE);
            } else {
                loadingLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 带文字的LoadingView
     *
     * @param onlyShowText 是否显示
     * @param message      提示信息
     * @param textColor    文字颜色
     */
    public void showLoading(boolean onlyShowText, String message, int textColor) {
        if (loadingLayout != null) {
            if (!onlyShowText) {
                loadingLayout.setVisibility(View.VISIBLE);
                loadingProgressBar.setVisibility(View.VISIBLE);
                showLoadingText(message, textColor);
            } else {
                if (TextUtils.isEmpty(message)) {
                    loadingLayout.setVisibility(View.GONE);
                } else { // 信息不为空
                    loadingLayout.setVisibility(View.VISIBLE);
                    loadingProgressBar.setVisibility(View.GONE);
                    showLoadingText(message, textColor);
                }
            }
        }
    }

    protected void showLoadingText(String message, int textColor) {
        loadingTv.setText(message);
        loadingTv.setTextColor(textColor);
    }
}
