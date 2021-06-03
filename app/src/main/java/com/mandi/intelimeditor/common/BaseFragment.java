package com.mandi.intelimeditor.common;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mandi.intelimeditor.R;
import com.mandi.intelimeditor.common.view.BaseLoadingView;


/**
 * Fragment基类
 */
public abstract class BaseFragment extends Fragment {
    public String TAG = getClass().getSimpleName();
    public View rootView;
    public Context mContext;
    protected boolean isDestroyView = false;
    private BaseLoadingView baseLoadingView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutResId(), container, false); // 暂时使用这个布局
        }
//        else {
//            ViewGroup parentView = (ViewGroup) rootView.getParent();
//            if (parentView != null) {
//                // Fragment动画导致子View还没清除就添加出错的问题
//                parentView.endViewTransition(rootView); // 主动调用清除动画
//                parentView.removeView(rootView);
//            }
//        }
        return rootView;
    }

    /**
     * 当前布局
     */
    public abstract int getLayoutResId();

    /**
     * 初始化View
     */
    public void initView() {
        baseLoadingView = new BaseLoadingView(rootView);
    }

    /**
     * 初始化数据
     */
    public void initData() {
    }

    public void showLoading() {
        baseLoadingView.showLoading(true);
    }

    public void dismissLoading() {
        baseLoadingView.showLoading(false);
    }


    /**
     * 带文字的LoadingView
     *
     * @param onlyShowText 是否显示
     * @param message      提示信息
     * @param textColor    文字颜色
     */
    public void showLoading(boolean onlyShowText, String message, int textColor) {
        baseLoadingView.showLoading(onlyShowText, message, textColor);
    }

    @Override
    public void onDestroyView() {
        isDestroyView = true;
        super.onDestroyView();
    }
}