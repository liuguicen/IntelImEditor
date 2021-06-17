package com.mandi.intelimeditor.ad.tencentAD;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.IBaseSplashAd;
import com.mandi.intelimeditor.ad.ISplashAdListener;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.user.US;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.constants.LoadAdParams;
import com.qq.e.comm.util.AdError;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/01
 *      version : 1.0
 * <pre>
 */
public class TencentSplashAd implements SplashADListener, IBaseSplashAd {
    public static final String TAG = "TencentSplashAd";

    private final Activity activity;
    private SplashAD splashAD;
    private final ISplashAdListener splashAdStrategy;
    private boolean isPauseToSee;
    /**
     * 计时结束标志位，用于处理暂停后且计时结束点击广告时，onPause不要拦截跳转的问题
     */
    private boolean isTimingFinish;

    public boolean canJump = false;

    private ViewGroup container;
    private long mTimeoutLong;
    private boolean loadAdOnly = false;

    public TencentSplashAd(Activity ac, ViewGroup container, ISplashAdListener splashAdStrategy, long timeoutLong) {
        isTimingFinish = false;
        this.activity = ac;
        this.container = container;

        this.splashAdStrategy = splashAdStrategy;
        mTimeoutLong = timeoutLong;
    }

    @Override
    public void fetchSplashAD() {
        container.setVisibility(View.VISIBLE);
        container.removeAllViews();
        // 总是报告很多容器不可见，这里测试看看是否是这个原因
        // 原因：其它广告方对它进行了异常的删除等，
        // 某个View在它的前面 挡在了大部分
        // 系统拦截，
        if (container.getParent() == null) {
            US.putSplashADEvent(US.FAILED + AdData.TX_AD_NAME + " container parent null");
        }
        //        boolean isCover = isCover(container);
        //        if (isCover) {
        //            US.putSplashADEvent(US.FAILED + US.TENCENT_AD + " container is covered");
        //        }
        fetchSplashAD(activity, container,
                TxAdConfig.SPLASH_ID,
                this, mTimeoutLong);
    }


    @Override
    public void onStop() {
        // nothing
    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     *
     * @param activity    展示广告的activity
     * @param adContainer 展示广告的大容器
     * @param posId       广告位ID
     * @param adListener  广告状态监听器
     * @param timeoutLong 拉取广告的超时时长：取值范围[3000, 5000]，设为0表示使用广点通SDK默认的超时时长。
     */
    private void fetchSplashAD(Activity activity, ViewGroup adContainer, String posId, SplashADListener adListener, long timeoutLong) {
        if (LogUtil.debugSplashAd) {
            Log.d(TAG, "fetch" + timeoutLong);
        }
        splashAD = new SplashAD(activity, posId, adListener, (int) timeoutLong);
        splashAD.fetchAndShowIn(adContainer);
    }

    @Override
    public void onADLoaded(long expireTimestamp) {
        Log.i(TAG, "SplashADFetch expireTimestamp:" + expireTimestamp);

        //        if(loadAdOnly) {
        //            loadAdOnlyDisplayButton.setEnabled(true);
        //            long timeIntervalSec = (expireTimestamp- SystemClock.elapsedRealtime())/1000;
        //            long min = timeIntervalSec/60;
        //            long second = timeIntervalSec-(min*60);
        //            loadAdOnlyStatusTextView.setText("加载成功,广告将在:"+min+"分"+second+"秒后过期，请在此之前展示(showAd)");
        //        }
    }

    /**
     * 广告曝光
     */
    @Override
    public void onADExposure() {
        if (LogUtil.debugSplashAd) {
            Log.i(TAG, "SplashADExposure");
        }
        splashAdStrategy.onAdExpose(AdData.TX_AD_NAME);
    }

    /**
     * 广告成功展示
     */
    @Override
    public void onADPresent() {
        if (LogUtil.debugSplashAd) {
            Log.i(TAG, "SplashADPresent");
        }
    }

    @Override
    public void onNoAD(AdError error) {
        int errorCode = error.getErrorCode();
        Log.e(TAG,
                String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", errorCode,
                        error.getErrorMsg()));
        US.putSplashADEvent(US.FAILED + AdData.TX_AD_NAME, "" + errorCode);
        if (splashAdStrategy != null) {
            splashAdStrategy.onAdError(AdData.TX_AD_NAME);
        }
    }

    /**
     * 倒计时回调，返回广告还将被展示的剩余时间。
     * 通过这个接口，开发者可以自行决定是否显示倒计时提示，或者还剩几秒的时候显示倒计时
     *
     * @param millisUntilFinished 剩余毫秒数
     */
    @Override
    public void onADTick(long millisUntilFinished) {
        if (LogUtil.debugSplashAd) {
            Log.i(TAG, "SplashADTick " + millisUntilFinished + "ms");
        }
    }

    @Override
    public void onADClicked() {
        if (LogUtil.debugSplashAd) {
            Log.i(TAG, "SplashADClicked");
        }
        splashAdStrategy.setUserPause(true);
        US.putSplashADEvent(US.CLICK + AdData.TX_AD_NAME);
    }

    @Override
    public void onADDismissed() {
        if (LogUtil.debugSplashAd) {
            Log.i(TAG, "SplashADDismissed");
        }
        isTimingFinish = true;
        next();
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，
     * 此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     * <p>
     * 这里canJump用得比较绕，有点6，第一次进入canJump = false, onResume中 不跳转，然后设置canJump为true，
     * 然后（1）如果没有点击广告页，计时完成或者点击跳过next里面能够跳转。
     * （2）然后如果点击广告页，并且广告页跳转，
     * 在onPause中设置了canJump为false，然后如果广告页在计时完成前或者后返回，onResume和计时完成总共调用两次
     * onNext，跳转到App能够成功
     */
    private void next() {
        if (LogUtil.debugSplashAd) {
            LogUtil.d(TAG, "next");
        }
        if (canJump) {
            if (splashAdStrategy != null) {
                splashAdStrategy.onAdFinish();
            }
        } else {
            canJump = true;
        }
    }

    public void onResume() {
        if (canJump) {
            next();
        }
        canJump = true;
    }

    public void onPause() {
        if (isTimingFinish) {
            canJump = true;
        } else {
            canJump = false;
        }
    }

    public void destroy() {
    }


    /**
     * 预加载广告，客服say暂时没有此功能，待完善，2020-6-3
     */
    public static void preloadSplashAd(Activity activity) {
        SplashAD splashAD = new SplashAD(activity, AdData.GDT_ID_LAUNCH_QY, null);
        LoadAdParams params = new LoadAdParams();
        params.setLoginAppId("testAppId");
        params.setLoginOpenid("testOpenId");
        params.setUin("testUin");
        splashAD.setLoadAdParams(params);
        splashAD.preLoad();
    }
}
