package com.mandi.intelimeditor.ad;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/01
 *      version : 1.0
 * <pre>
 *     可能使用多种开屏广告
 *     典型的接口或者工厂模式
 */
public interface IBaseSplashAd {
    void fetchSplashAD();

    void onStop();

    void destroy();
    void onResume();
}
