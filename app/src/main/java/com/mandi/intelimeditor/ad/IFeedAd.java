package com.mandi.intelimeditor.ad;

import androidx.annotation.Nullable;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/04
 *      version : 1.0
 * <pre>
 */
public interface IFeedAd {

    /**
     * @param holder 显示广告的Item，同RecyclerView
     */
    void bindData(ADHolder holder);
    void loadAdResources(@Nullable ADHolder holder);

    boolean isInvalid();

    String getAdInfo();

    void destroy();

    String getAdId();

    long getLoadTime();

    boolean isLoadSuccess();
}
