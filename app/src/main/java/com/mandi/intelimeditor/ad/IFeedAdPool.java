package com.mandi.intelimeditor.ad;

import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/04
 *      version : 1.0
 * <pre>
 */
public interface IFeedAdPool {
    /**
     * @return boolen 腾讯的要考虑是否是新建的
     */
    IFeedAd getNextAD(@NotNull String ad_id,
                      ADHolder adHolder,
                      String adEventName,
                      String adPositionName);

    boolean checkValid(IFeedAd ad);
}
