package com.mandi.intelimeditor.ad.kjAD;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import com.kaijia.adsdk.Interface.KjSplashAdListener;
import com.kaijia.adsdk.Tools.KjSplashAd;
import com.mandi.intelimeditor.ad.IBaseSplashAd;
import com.mandi.intelimeditor.ad.ISplashAdListener;
import com.mandi.intelimeditor.user.US;

public class MyKjSplashAd implements IBaseSplashAd, KjSplashAdListener {
    public static final String TAG = "MyKjSplashAd";
    private final Activity activity;
    private final ViewGroup container;
    private final ISplashAdListener splashListener;
    /**
     * 是否已经跳转到广告页面
     */
    private boolean hadGoAdAc = false;
    private boolean hasClicked = false;

    public MyKjSplashAd(Activity ac, ViewGroup container, ISplashAdListener splashListener, long timeoutLong) {
        this.activity = ac;
        this.container = container;
        this.splashListener = splashListener;

    }

    @Override
    public void fetchSplashAD() {
        new KjSplashAd(activity, "ecc3a926", container, this); //请求广告
    }

    @Override
    public void onStop() {
        hadGoAdAc = true;
    }

    @Override
    public void onAdShow() {
        Log.i(TAG, "show");
        splashListener.onAdExpose(US.EXPOSURE + US.KJ_AD);
    }

    @Override
    public void onAdDismiss() {
        Log.i(TAG, "dismiss");
        // 这个广告点击了之后，还是会调用dismiss
        if (!hadGoAdAc && !hasClicked) {
            splashListener.onAdFinish();
        }
    }

    @Override
    public void onAdClick() {
        Log.i(TAG, "click");
        hasClicked = true;
        US.putSplashADEvent(US.CLICK + US.KJ_AD);
    }

    @Override
    public void onFailed(String error) {
        Log.e(TAG, "铠甲开屏广告错误" +error);
        US.putSplashADEvent(US.FAILED + US.KJ_AD + " " + error);
        splashListener.onAdError(US.KJ_AD);
    }

    @Override
    public void onAdReWard(int i) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void onResume() {
        //判断是否该跳转到主页面
        if (hadGoAdAc) {
            splashListener.onAdFinish();
        }
    }
}
