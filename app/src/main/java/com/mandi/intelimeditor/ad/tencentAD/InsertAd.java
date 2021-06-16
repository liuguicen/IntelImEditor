package com.mandi.intelimeditor.ad.tencentAD;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.user.US;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.util.AdError;

import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/18
 *      version : 1.0
 * <pre>
 */
public class InsertAd {
    public static final String TAG = "insert AD";
    private static UnifiedInterstitialAD iad;
    private static final String adId = AdData.GDT_ID_INSERT_SCREEN_AD;

    public static void onClickTarget(Activity activity) {
        if (activity == null) return;
        PtuActivity.myClickCount++;
        if (!AdData.judgeAdClose(AdData.TENCENT_AD_ID)
                && AllData.sRandom.nextDouble() < 1d / 45
                && PtuActivity.myClickCount > 3) { // 进入PTuActivity之后，点击超过3次才行
            prepareShowAd(activity);
        }
    }

    protected static void prepareShowAd(@NotNull Activity activity) {
        Util.hideInputMethod(activity);
        destroyAd();
        iad = getIAD(activity);
        iad.loadAD();
    }


    private static UnifiedInterstitialAD getIAD(Activity activity) {
        if (iad != null) {
            iad.close();
            iad.destroy();
        }
        if (iad == null) {
            iad = new UnifiedInterstitialAD(activity, adId, new UnifiedInterstitialADListener() {
                @Override
                public void onADReceive() {
                    // Log.d(TAG, "onADReceive: 视频插屏" + (iad.getAdPatternType() == AdPatternType.NATIVE_VIDEO));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed()) {
                        return;
                    }
                    if (iad != null) {
                        try {
                            iad.show();
                        } catch (WindowManager.BadTokenException badTokenException) {
                            US.putPTuInsertAdEvent(activity, US.ACTIVITY_DESTROYED, 0);
                        }
                    }
                }

                @Override
                public void onVideoCached() {
                }

                @Override
                public void onNoAD(AdError adError) {
                    Log.e(TAG, "onNoAD: ");
                    US.putPTuInsertAdEvent(activity, US.FAILED, adError.getErrorCode());
                }

                @Override
                public void onADOpened() {
                    LogUtil.d();
                }

                @Override
                public void onADExposure() {
                    US.putPTuInsertAdEvent(activity, US.EXPOSURE, 0);
                }

                @Override
                public void onADClicked() {
                    AdData.setClicked(adId);
                    US.putPTuInsertAdEvent(activity, US.CLICK, 0);
                    iad.close();
                }

                @Override
                public void onADLeftApplication() {

                }

                @Override
                public void onADClosed() {
                    US.putPTuInsertAdEvent(activity, US.CLOSE, 0);
                    destroyAd();
                }
            });
            iad.setVideoOption(new VideoOption.Builder().setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS).build());
            iad.setMediaListener(new UnifiedInterstitialMediaListener() {
                @Override
                public void onVideoInit() {
                    Log.d(TAG, "onVideoInit: ");
                }

                @Override
                public void onVideoLoading() {

                }

                @Override
                public void onVideoReady(long l) {

                }

                @Override
                public void onVideoStart() {

                }

                @Override
                public void onVideoPause() {

                }

                @Override
                public void onVideoComplete() {

                }

                @Override
                public void onVideoError(AdError adError) {

                }

                @Override
                public void onVideoPageOpen() {

                }

                @Override
                public void onVideoPageClose() {

                }
            });
        }
        return iad;
    }

    /**
     * 一定要销毁
     */
    public static void destroyAd() {
        if (iad != null) {
            iad.destroy();
            iad = null;
        }
    }
}
