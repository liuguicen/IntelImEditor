package com.mandi.intelimeditor.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mandi.intelimeditor.R;
import com.scwang.smart.refresh.layout.api.RefreshHeader;
import com.scwang.smart.refresh.layout.api.RefreshKernel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.constant.RefreshState;
import com.scwang.smart.refresh.layout.constant.SpinnerStyle;



/**
 * 列表下拉刷新头部，自定义Header
 */
public class PicRefreshHeader extends ConstraintLayout implements RefreshHeader {

    public static String REFRESH_HEADER_PULLING = "下拉显示";//"下拉可以刷新";
    public static String REFRESH_HEADER_LOADING = "正在切换";//"正在加载...";
    public static String REFRESH_HEADER_RELEASE = "释放立即显示";
    public static String REFRESH_HEADER_FINISH = "切换成功";//"刷新完成";
    public static String REFRESH_HEADER_FAILED = "切换失败";//"刷新失败";

    private TextView mTitleText;
    private ImageView mArrowIv;
    private ProgressBar mLoadingView;
    private String refreshTint = "最新列表"; //最新列表，分组排列

    public PicRefreshHeader(Context context) {
        this(context, null);
    }

    public PicRefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PicRefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.pic_refresh_head, this);
        mTitleText = findViewById(R.id.tv_title);
        mLoadingView = findViewById(R.id.pb_progress);
        mArrowIv = findViewById(R.id.iv_arrow);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @NonNull
    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(@NonNull RefreshKernel kernel, int height, int maxDragHeight) {

    }

    @Override
    public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) {

    }

    @Override
    public void onReleased(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public void onStartAnimator(@NonNull RefreshLayout refreshLayout, int height, int maxDragHeight) {

    }

    @Override
    public int onFinish(@NonNull RefreshLayout layout, boolean success) {
        if (success) {
            mTitleText.setText(REFRESH_HEADER_FINISH);
        } else {
            mTitleText.setText(REFRESH_HEADER_FAILED);
        }
        return 500; //延迟500毫秒之后再弹回
    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState newState) {
        switch (newState) {
            case PullDownToRefresh: //下拉过程
                mArrowIv.setVisibility(VISIBLE);
                mLoadingView.setVisibility(GONE);
                mArrowIv.setImageResource(R.drawable.round_arrow_downward);
                mTitleText.setText(REFRESH_HEADER_PULLING + refreshTint);
                break;
            case ReleaseToRefresh: //松开刷新
                mArrowIv.setVisibility(VISIBLE);
                mLoadingView.setVisibility(GONE);
                mArrowIv.setImageResource(R.drawable.round_arrow_upward);
                mTitleText.setText(REFRESH_HEADER_RELEASE + refreshTint);
                break;
            case Refreshing: //loading中
                mArrowIv.setVisibility(GONE);
                mLoadingView.setVisibility(VISIBLE);
                mTitleText.setText(REFRESH_HEADER_LOADING);
                break;
        }
    }

    public void setNextUpdateText(String message) {
        refreshTint = message;
    }
}