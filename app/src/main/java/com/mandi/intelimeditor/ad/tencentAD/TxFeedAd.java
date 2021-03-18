package com.mandi.intelimeditor.ad.tencentAD;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.IFeedAd;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.user.US;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/29
 *      version : 1.0
 * <pre>
 */
public class TxFeedAd implements NativeExpressAD.NativeExpressADListener, IFeedAd {

    private final boolean isDebug = true;
    private String adEventName;
    private String mAdPositionName;
    private Context mContext;
    private FrameLayout container;
    private NativeExpressAD nativeExpressAD;
    private NativeExpressADView nativeExpressADView;

    private String TAG = "TxFeedAd";
    private String mADId;
    private AdCloseListener mCloseListener;
    private long mLoadTime;

    private ADSize mAdSize;
    private boolean mIsLoadFailed;
    private boolean mHasClicked;
    private int mShowNumber;
    private int mMaxShowNumber = 20;

    // 广告标志Tv
    @Nullable
    private TextView adMarkTv;
    private boolean isPurePic = false;

    /**
     * @param adEventName 用于友盟统计的事件名称，也即广告位置

     */
    public TxFeedAd(Context context, @Nullable FrameLayout container, String adId,
                    String adEventName, String adPositionName) {
        init(context, container, adId, adEventName, adPositionName);
    }

    /**
     * 测试版的，貌似View不使用Activity的Context也能用，这样的话可以很大的减少曝光量，增加点击率
     */
    public TxFeedAd( @Nullable FrameLayout container, String adId,
                    String adEventName, String adPositionName) {
        init(IntelImEditApplication.appContext, container, adId, adEventName, adPositionName);
    }

    private void init(Context context, @Nullable FrameLayout container, String adId,
                      String adEventName, String adPositionName) {
        this.mContext = context;
        this.container = container;
        this.adEventName = adEventName;
        mADId = adId;
        mLoadTime = 0;
        mAdPositionName = adPositionName;
        mIsLoadFailed = false;
        mHasClicked = false;
        mShowNumber = 0;
    }

    public void setMaxExposureNumber(int number) {
        if (number < 1) return;
        mMaxShowNumber = number;
    }

    public void setCloseListener(AdCloseListener closeListener) {
        mCloseListener = closeListener;
    }

    public long getLoadTime() {
        return mLoadTime;
    }

    public String getAdId() {
        return mADId;
    }

    public void destroy() {
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
    }

    public void setContainer(FrameLayout container) {
        this.container = container;
    }

    /**
     * 获取广告资源
     */
    @Override
    public void loadAdResources(@Nullable ADHolder adHolder) {
        if (adHolder != null) {
            container = adHolder.container;
        }
        try {
            //            hideSoftInput();
            /**
             *  如果选择支持视频的模版样式，请使用{@link Constants#NativeExpressSupportVideoPosID}
             */
            if (mAdSize == null) {
                mAdSize = getMyADSize();
            }
            nativeExpressAD = new NativeExpressAD(mContext, mAdSize, mADId, this); // 这里的Context必须为Activity
            if (!isPurePic) {
                nativeExpressAD.setVideoOption(TencentAdUtil.getVideoOption()); // setVideoOption是可选的，开发者可根据需要选择是否配置
                nativeExpressAD.setMinVideoDuration(5);
                nativeExpressAD.setMaxVideoDuration(60);
                /**
                 * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前调用setVideoPlayPolicy，有助于提高视频广告的eCPM值 <br/>
                 * 如果广告位仅支持图文广告，则无需调用
                 */

                /**
                 * 设置本次拉取的视频广告，从用户角度看到的视频播放策略<p/>
                 *
                 * "用户角度"特指用户看到的情况，并非SDK是否自动播放，与自动播放策略AutoPlayPolicy的取值并非一一对应 <br/>
                 *
                 * 如自动播放策略为AutoPlayPolicy.WIFI，但此时用户网络为4G环境，在用户看来就是手工播放的
                 */
                nativeExpressAD.setVideoPlayPolicy(TencentAdUtil.getVideoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS, mContext));  // 本次拉回的视频广告，在用户看来是否为自动播放的

            }
            nativeExpressAD.loadAD(1);
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "refresh ad success");
            }
        } catch (NumberFormatException e) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "ad 宽高数值 不合法.");
        }
    }

    @Override
    public void onADLoaded(List<NativeExpressADView> adList) {
        // 释放前一个展示的NativeExpressADView的资源
        if (nativeExpressADView != null) {
            nativeExpressADView.destroy();
        }
        nativeExpressADView = adList.get(0);
        if (isDebug) {
            LogUtil.d(TAG + (isPurePic ? "纯图" : "信息流"), hashCode() + " 加载广告, video info: " + getAdInfo(nativeExpressADView)
                    + "\n view 的 hashcode = " + nativeExpressADView.hashCode());
        }
        if (nativeExpressADView.getBoundData().getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
            nativeExpressADView.setMediaListener(mediaListener);
        }
        if (isDebug) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "获取图片资源下的广告视图成功， 广告ID为： " + mADId);
        }
        mLoadTime = System.currentTimeMillis();
        bindData(container);
        nativeExpressADView.render();
    }

    @Override
    public void bindData(ADHolder holder) {
        bindData(holder.container);
    }

    /**
     * 腾讯的加入sdk内部的广告视图，相当于绑定数据
     */
    public void bindData(FrameLayout container) {
        this.container = container;
        if (nativeExpressADView == null) return;
        // 在RecyclerView中，可能会多次调用binViewHolder,所以必须先清除这个子View，不然报异常
        if (nativeExpressADView.getParent() != null) {
            ((ViewGroup) nativeExpressADView.getParent()).removeView(nativeExpressADView);
        }

        if (container == null) return;

        if (container.getVisibility() != View.VISIBLE) {
            container.setVisibility(View.VISIBLE);
        }
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }
        // 广告可见才会产生曝光，否则将无法产生收益。
        if (isDebug) {
            LogUtil.d(TAG + (isPurePic ? "纯图" : "信息流"), hashCode() + " 展示广告，View的hashCode = " + nativeExpressADView.hashCode());
        }
        container.addView(nativeExpressADView);
        // nativeExpressADView.render(); 不应该写在这里，写在加载时才能避免同一广告多次曝光
        if (adMarkTv != null) {
            if (AllData.sRandom.nextDouble() > 0.125) {
                adMarkTv.setVisibility(View.VISIBLE);
            }
        }
        mShowNumber++; // 渲染成功不会回调，只能这里添加
        // Log.d(mADId, " 广告加入布局成功，高度=  " + container.getHeight());
    }

    public boolean isLoadSuccess() {
        return nativeExpressADView != null;
    }

    public boolean isLoadFailed() {
        return mIsLoadFailed;
    }


    public boolean contentEquals(TxFeedAd b) {
        if (b != null) {
            return TextUtils.equals(getAdInfo(), b.getAdInfo());
        }
        return false;
    }


    public static ADSize getMyADSize() {
        int w = ADSize.FULL_WIDTH;
        //        int h; // 目前的广告都是占用整个屏幕宽，根据腾讯广告联盟上推荐的宽高比设置高度
        //        switch (adType) {
        //            case AdData.TYPE_THREE_SMALL_PIC:
        //                h = AdData.HEIGHT_RATIO_THREE_SMALL_PIC;
        //                break;
        //            case AdData.TYPE_PIC_ABOVE_TEXT_UNDER:
        //                h = AdData.HEIGHT_RATIO_PIC_ABOVE_PIC_UNDER;
        //                break;
        //            default:
        //                h = ADSize.AUTO_HEIGHT;
        //        }
        return new ADSize(w, ADSize.AUTO_HEIGHT);
    }

    public void setAdSize(ADSize adSize) {
        mAdSize = adSize;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); // 不要复写此方法,其它地方有要求
        // 判有效的地方，已经被删除, 不能用contain判断，大错，自己判断重复的时候就是用equal，contain也用equal，重复了，
        // 删掉一个另一个contain也是true阿，是无法通过它判断广告有效的
    }

    @Nullable
    public String getAdInfo() {
        return getAdInfo(nativeExpressADView);
    }

    /**
     * 获取广告数据
     *
     * @param nativeExpressADView
     * @return
     */
    private String getAdInfo(NativeExpressADView nativeExpressADView) {
        if (nativeExpressADView == null) return null;
        com.qq.e.comm.pi.AdData adData = nativeExpressADView.getBoundData();
        if (adData != null) {
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("title:").append(adData.getTitle()).append(",")
                    .append("desc:").append(adData.getDesc()).append(",")
                    .append("patternType:").append(adData.getAdPatternType());
            if (adData.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                infoBuilder.append(", video info: ").append(getVideoInfo(adData.getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
            }
            return infoBuilder.toString();
        }
        return null;
    }

    /**
     * 获取播放器实例
     * <p>
     * 仅当视频回调{@link NativeExpressMediaListener#onVideoInit(NativeExpressADView)}调用后才会有返回值
     *
     * @param videoPlayer
     * @return
     */
    private String getVideoInfo(com.qq.e.comm.pi.AdData.VideoPlayer videoPlayer) {
        if (videoPlayer != null) {
            StringBuilder videoBuilder = new StringBuilder();
            videoBuilder.append("{state:").append(videoPlayer.getVideoState()).append(",")
                    .append("duration:").append(videoPlayer.getDuration()).append(",")
                    .append("position:").append(videoPlayer.getCurrentPosition()).append("}");
            return videoBuilder.toString();
        }
        return null;
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.e(TAG + (isPurePic ? "纯图" : "信息流"),
                String.format("onNoAD, error code: %d, error msg: %s, 广告ID为：%s", adError.getErrorCode(),
                        adError.getErrorMsg(), mADId));
        mIsLoadFailed = true;
        US.putADEvent(mContext, adEventName, mAdPositionName + ": " + US.FAILED + " " + adError.getErrorCode());
        if (adMarkTv != null) {
            adMarkTv.setVisibility(View.GONE);
        }
        // mLoadTime = 0;  // 加载出错，每次都重新加载，是否会导致过度刷新，更多的错误
    }

    @Override
    public void onRenderFail(NativeExpressADView adView) {
        Log.e(TAG + (isPurePic ? "纯图" : "信息流"), "onRenderFail");
        if (adMarkTv != null) {
            adMarkTv.setVisibility(View.GONE);
        }
        US.putADEvent(mContext, adEventName, mAdPositionName + ": " + US.REND_ERROR);
    }

    /**
     * 只会第一次渲染成功回调，后面不会
     */
    @Override
    public void onRenderSuccess(NativeExpressADView adView) {

    }

    @Override
    public void onADExposure(NativeExpressADView adView) {
        if (adMarkTv != null) {
            if (AllData.sRandom.nextInt(8) == 0) {
                return; // 概率性的不显示广告标志，以期增加点击
            }
            adMarkTv.setVisibility(View.VISIBLE);
        }
        if (isDebug) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告展示成功， onADExposure， ID = " + mADId);
        }
        US.putADEvent(mContext, adEventName, mAdPositionName + ": " + US.EXPOSURE);
    }

    @Override
    public void onADClicked(NativeExpressADView adView) {
        if (isDebug) {
            Log.d(TAG, "onADClicked");
        }
        mHasClicked = true;
        AdData.setClicked(mADId);
        AdData.hasClikedAdJust = true;
        US.putADEvent(mContext, adEventName, mAdPositionName + ": " + US.CLICK);
    }

    @Override
    public void onADClosed(NativeExpressADView adView) {
        if (isDebug) {
            Log.d(TAG, "onADClosed");
        }
        clearContainer(container);
        US.putADEvent(mContext, adEventName, mAdPositionName + ": " + US.CLOSE);
        if (mCloseListener != null) {
            mCloseListener.close();
        }
    }

    public void clearContainer(FrameLayout container) {
        // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，释放资源，不可以再用来展示。
        if (container != null && container.getChildCount() > 0) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
    }

    @Override
    public void onADLeftApplication(NativeExpressADView adView) {
        if (isDebug) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onADLeftApplication");
        }
    }

    @Override
    public void onADOpenOverlay(NativeExpressADView adView) {
        if (isDebug) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onADOpenOverlay");
        }
    }

    @Override
    public void onADCloseOverlay(NativeExpressADView adView) {
        if (isDebug) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onADCloseOverlay");
        }
    }


    // 隐藏软键盘，这只是个简单的隐藏软键盘示例实现，与广告sdk功能无关
    //    private void hideSoftInput() {
    //        if (getCurrentFocus() == null || getCurrentFocus().getWindowToken() == null) {
    //            return;
    //        }
    //
    //        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
    //                NativeExpressDemoActivity.this.getCurrentFocus().getWindowToken(),
    //                InputMethodManager.HIDE_NOT_ALWAYS);
    //    }

    /**
     * 注意：带有视频的广告被点击后会进入全屏播放视频，此时视频可以跟随屏幕方向的旋转而旋转，
     * 请开发者注意处理好自己的Activity的运行时变更，不要让Activity销毁。
     * 例如，在AndroidManifest文件中给Activity添加属性android:configChanges="keyboard|keyboardHidden|orientation|screenSize"，
     */
    private NativeExpressMediaListener mediaListener = new NativeExpressMediaListener() {
        @Override
        public void onVideoInit(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoInit: "
                        + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
            }
        }

        @Override
        public void onVideoLoading(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoLoading");
            }
        }

        @Override
        public void onVideoCached(NativeExpressADView nativeExpressADView) {

        }

        @Override
        public void onVideoReady(NativeExpressADView nativeExpressADView, long l) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoReady");
            }
        }

        @Override
        public void onVideoStart(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoStart: "
                        + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
            }
        }

        @Override
        public void onVideoPause(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoPause: "
                        + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
            }
        }

        @Override
        public void onVideoComplete(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoComplete: "
                        + getVideoInfo(nativeExpressADView.getBoundData().getProperty(com.qq.e.comm.pi.AdData.VideoPlayer.class)));
            }
        }

        @Override
        public void onVideoError(NativeExpressADView nativeExpressADView, AdError adError) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoError");
            }
        }

        @Override
        public void onVideoPageOpen(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoPageOpen");
            }
        }

        @Override
        public void onVideoPageClose(NativeExpressADView nativeExpressADView) {
            if (isDebug) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "onVideoPageClose");
            }
        }
    };

    public boolean isExpire() {
        return System.currentTimeMillis() - mLoadTime > AdData.NATIVE_AD_LOAD_TIME_INTERVAL;
    }

    public boolean hasClicked() {
        return mHasClicked;
    }

    public int getShowNumber() {
        return mShowNumber;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean isInvalid() {
        return isExpire() || isLoadFailed() || hasClicked() || mShowNumber >= mMaxShowNumber;
    }

    public void setIsPurePic(boolean isPurePic) {
        this.isPurePic = isPurePic;
    }

    public void setAdMarkTv(@Nullable TextView adMarkTv) {
        this.adMarkTv = adMarkTv;
    }

    public interface AdCloseListener {
        void close();
    }
}
