package com.mandi.intelimeditor.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.NotNull;



/**
 * 图片列表基类
 * 抽取公共方法，减少重复代码，方便代码管理
 */
public abstract class BasePicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final String TAG = this.getClass().getSimpleName();

    protected RcvItemClickListener1 clickListener;
//    protected RcvItemMultiFunctionClickListener multiFuncListener;
    protected LongClickListener longClickListener;

    protected int spanCount = 2;
    protected int mItemImagePadding;
    protected boolean mIsTietu;

    public Context mContext;


    public BasePicAdapter(Context mContext) {
        this.mContext = mContext;
        mItemImagePadding = Util.dp2Px(1f);
    }

    protected ImageView createItemView(ViewGroup parent, ImageView.ScaleType scaleType) {
        // 创建LinearLayout对象
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(scaleType);
        int parentWidth = parent.getWidth();
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, parentWidth / spanCount - mItemImagePadding);
        imageView.setLayoutParams(layoutParams);
        return imageView;
    }

    /**
     * 创建多选时的选择按钮
     */
    protected ImageView createChooserView() {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int width = Util.dp2Px(24);
        int margin = Util.dp2Px(3);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(width, width);
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topMargin = margin;
        layoutParams.rightMargin = margin;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.chosen);
        imageView.setVisibility(View.GONE);
        return imageView;
    }

    /**
     * 创建本地视频图标
     */
    protected ImageView createVideoSign() {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int height = Util.dp2Px(36);
        int margin = Util.dp2Px(3);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout
                .LayoutParams(height, height);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topMargin = margin;
        layoutParams.rightMargin = margin;
        imageView.setLayoutParams(layoutParams);
        imageView.setImageResource(R.drawable.video_sign);
        imageView.setVisibility(View.GONE);
        return imageView;
    }


    public RecyclerView.ViewHolder createFeedAdHolder(LayoutInflater layoutInflater, @NotNull ViewGroup parent) {
        // 返回正常大小feed的布局，后面pool会根据布局的宽获取对应大小的ad，然后显示
        if (LogUtil.debugPicResource) {
            Log.d(TAG, "创建信息流广告View");
        }
        FrameLayout adLayout = ((FrameLayout) layoutInflater.inflate(R.layout.item_feed_ad, parent, false));
        // 注意宽度必须设置成值，后面广告渲染需要用到
        int parentWidth = AllData.screenWidth > 100 ? AllData.screenWidth : parent.getWidth();
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                parentWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        adLayout.setLayoutParams(layoutParams);
        return new ADHolder(adLayout, adLayout);
    }

    /**
     * 创建广告容器
     */
    protected FrameLayout createADContainer(ViewGroup parent) {
        FrameLayout container = new FrameLayout(mContext);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, parent.getWidth() / spanCount - mItemImagePadding);
        container.setLayoutParams(layoutParams);
        return container;
    }


    public void setClickListener(RcvItemClickListener1 clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

//    public void setMultiFuncListener(RcvItemMultiFunctionClickListener multiFuncListener) {
//        this.multiFuncListener = multiFuncListener;
//    }

    public interface LongClickListener {
        boolean onItemLongClick(RecyclerView.ViewHolder itemHolder);
    }

}
