package com.mandi.intelimeditor.ad.ttAD;

import android.app.Activity;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.mandi.intelimeditor.ad.IBaseSplashAd;
import com.mandi.intelimeditor.ad.ISplashAdListener;
import com.mandi.intelimeditor.ad.TTAdConfig;
import com.mandi.intelimeditor.ad.ttAD.util.WeakHandler;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTAdManagerHolder;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.user.US;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/09/28
 *      version : 1.0
 * <pre>
 */
public class MyTTSplashAd implements WeakHandler.IHandler, IBaseSplashAd {
    public static final String TAG = "MyTTSplashAd";
    private static final int MSG_GO_MAIN = 1;
    private final TTAdNative mTTAdNative;
    private final Activity mActivity;
    private final WeakHandler mHandler = new WeakHandler(this);
    private boolean mHasLoaded;
    private ViewGroup mSplashContainer;
    private ISplashAdListener splashStrategy;
    private boolean hasFinish = false;
    private boolean mForceGoMain = false;
    private final long mTimeOutLong;

    public MyTTSplashAd(Activity activity, ViewGroup adContainer, ISplashAdListener splashStrategy, long timeoutLong) {
        this.splashStrategy = splashStrategy;
        this.mActivity = activity;
        mSplashContainer = adContainer;
        mTimeOutLong = timeoutLong;
        //开屏广告加载发生超时但是SDK没有及时回调结果的时候，做的一层保护。
        //step2:创建TTAdNative对象
        mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        //在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        //在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        // TTAdManagerHolder.getInstance(this).requestPermissionIfNecessary(this);
        //定时，AD_TIME_OUT时间到时执行，如果开屏广告没有加载则跳转到主页面
        mHandler.sendEmptyMessageDelayed(MSG_GO_MAIN, mTimeOutLong);
    }

    @Override
    public void fetchSplashAD() {
        //加载开屏广告
        loadSplashAd();
    }

    /**
     * 加载开屏广告
     */
    private void loadSplashAd() {
        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(TTAdConfig.TT_SPLASH_AD_ID)
                // .setCodeId("test_error")
                .setSupportDeepLink(true)
                .setImageAcceptedSize(AllData.getScreenWidth(), AllData.getScreenHeight())
                .build();
        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        mTTAdNative.loadSplashAd(adSlot,
                new TTAdNative.SplashAdListener() {
                    @Override
                    @MainThread
                    public void onError(int code, String message) {
                        Log.e(TAG, "onError: " + message); // 注意这里message可能 == null，引起崩溃
                        US.putSplashADEvent(US.FAILED + US.TT_AD + " " + code);
                        mHasLoaded = true;
                        splashStrategy.onAdError("TT");
                        mSplashContainer.removeAllViews();
                    }

                    @Override
                    @MainThread
                    public void onTimeout() {
                        mHasLoaded = true;
                        Log.e(TAG, "onTimeout");
                        US.putSplashADEvent(US.FAILED + US.TT_AD + " " + US.TIME_OUT);
                        splashStrategy.onAdError("TT");
                        mSplashContainer.removeAllViews();
                    }

                    @Override
                    @MainThread
                    public void onSplashAdLoad(TTSplashAd ad) {
                        Log.d(TAG, "开屏广告请求成功");
                        mHasLoaded = true;
                        mHandler.removeCallbacksAndMessages(null);
                        if (ad == null) {
                            return;
                        }
                        US.putSplashADEvent(US.load_success + US.TT_AD);
                        //获取SplashView
                        View view = ad.getSplashView();
                        if (view != null) {
                            mSplashContainer.removeAllViews();
                            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
                            mSplashContainer.addView(view);
                            //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
                            //ad.setNotAllowSdkCountdown();
                        } else {
                            splashStrategy.onAdError("TT");
                        }

                        //设置SplashView的交互监听器
                        ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {

                            @Override
                            public void onAdShow(View view, int type) {
                                Log.d(TAG, "onAdShow");
                                splashStrategy.onAdExpose(US.TT_AD);
                            }

                            @Override
                            public void onAdClicked(View view, int type) {
                                LogUtil.d(TAG, "onAdClicked");
                                splashStrategy.setUserPause(true);
                                US.putSplashADEvent(US.CLICK + US.TT_AD);
                            }


                            @Override
                            public void onAdSkip() {
                                Log.d(TAG, "onAdSkip");
                                if (Util.DoubleClick.isDoubleClick(1500))
                                    return;
                                splashStrategy.onAdFinish();
                                mSplashContainer.removeAllViews();
                                hasFinish = true;
                            }

                            @Override
                            public void onAdTimeOver() {
                                Log.d(TAG, "onAdTimeOver");
                                if (!hasFinish) {// sdk这里有bug，有时skip之后还会调用这个
                                    splashStrategy.onAdFinish();
                                    mSplashContainer.removeAllViews();
                                }
                            }
                        });
                        if (ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                            ad.setDownloadListener(new TTAppDownloadListener() {
                                boolean hasShow = false;

                                @Override
                                public void onIdle() {

                                }

                                @Override
                                public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                                    if (!hasShow) {
                                        showToast("下载中...");
                                        hasShow = true;
                                        US.putSplashADEvent(US.START_DOWNLOAD + US.TT_AD);
                                    }
                                }

                                @Override
                                public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                                    showToast("下载暂停...");

                                }

                                @Override
                                public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                                    showToast("下载失败...");

                                }

                                @Override
                                public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                                    Log.d(TAG, "onDownloadFinished: ");
                                    US.putSplashADEvent(US.DOWNLOAD_COMPLETE + US.TT_AD);
                                }

                                @Override
                                public void onInstalled(String fileName, String appName) {
                                    Log.d(TAG, "onInstalled: ");
                                    US.putSplashADEvent(US.INSTALL_SUCCESS + US.TT_AD);
                                }
                            });
                        }
                    }
                },
                (int) mTimeOutLong);

    }

    public void onResume() {
        //判断是否该跳转到主页面
        if (mForceGoMain) {
            mHandler.removeCallbacksAndMessages(null);
            splashStrategy.onAdFinish();
        }
    }

    /**
     * 用于进入广告跳转回来时调到主界面
     */
    @Override
    public void onStop() {
        mForceGoMain = true;
    }

    private void showToast(String s) {
        ToastUtils.show(s);
    }

    @Override
    public void handleMsg(Message msg) {
        if (msg.what == MSG_GO_MAIN) {
            if (!mHasLoaded) {
                Log.e(TAG, US.FAILED + US.TT_AD + " " + US.TIME_OUT);
                US.putSplashADEvent(US.FAILED + US.TT_AD + " " + US.TIME_OUT);
                splashStrategy.onAdError("TT");
            }
        }
    }

    public void destroy() {

    }
}
