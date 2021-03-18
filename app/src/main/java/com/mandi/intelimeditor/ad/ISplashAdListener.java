package com.mandi.intelimeditor.ad;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/01
 *      version : 1.0
 * <pre>
 */
public interface ISplashAdListener {
    void onAdError(String res);
    void onAdFinish();

    void setUserPause(boolean isUserPause);

    void onAdExpose(String adResName);
}
