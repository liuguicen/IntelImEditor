package com.mandi.intelimeditor.ad.ttAD.feedad;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import com.bumptech.glide.RequestManager;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.IFeedAd;
import com.mandi.intelimeditor.ad.IFeedAdPool;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTAdManagerHolder;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.home.view.TencentPicADHolder;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/03
 *      version : 1.0
 * <pre>
 *     穿山甲的信息流广告的广告池
 *     负责管理TTFeedAdWrapper，具体的ad逻辑交给TTFeedAdWrapper
 */
public class TTFeedAdPool implements IFeedAdPool {
    public static final String TAG = "TTFeedAdPool";
    public static final int DEFAULT_MAX_AD_NUMBER = 8;
    private static final int LOAD_UNSTART = 1;
    private static final int LOAD_LOADING = 2;
    private static final int LOAD_FINISH = 3;

    private TTAdNative mTTAdNative;
    private List<TTFeedAdWrapper> adList;
    private Activity mActivity;
    private RequestManager mRequestManager;
    private Map<TencentPicADHolder, TTAppDownloadListener> mTTAppDownloadListenerMap;
    private final int maxFeedAdNumber;
    private int lastId = 0;

    /**
     * 头条的信息流，异步加载的加载状态
     */
    private int loadState = LOAD_UNSTART;

    public TTFeedAdPool(Activity activity, int maxFeedAdNumber) {
        initTTAd(activity);
        mActivity = activity;
        adList = new ArrayList<>();
        this.maxFeedAdNumber = maxFeedAdNumber;
    }

    void initTTAd(Activity activity) {
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        mTTAdNative = ttAdManager.createAdNative(IntelImEditApplication.appContext);
        //申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        // 进入APP时会保证这些权限已经获取，头条这里会开一个AC来获取权限，对性能消耗太大
//        TTAdManagerHolder.get().requestPermissionIfNecessary(activity);
    }

    public TTAdNative getTTAdNative() {
        return mTTAdNative;
    }

    /**
     * 加载feed广告
     */
    // public void loadFeedAdList(int number, String adPositionName) {
    //     float expressViewWidth = Util.px2Dp(AllData.screenWidth / 2);
    //     if (expressViewWidth <= 0) {
    //         expressViewWidth = 150;
    //     }
    //     float expressViewHeight = 0;  //高度设置为0,则高度会自适应
    //
    //     //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
    //     AdSlot adSlot = new AdSlot.Builder()
    //             .setCodeId(AdConfig.TEMPLETE_FEED_AD_ID)
    //             .setSupportDeepLink(true)
    //             .setImageAcceptedSize(640, 320)
    //             .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
    //             .setAdCount(number) //请求广告数量为1到3条
    //             .build();
    //     //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
    //     mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
    //         @Override
    //         public void onError(int code, String message) {
    //             Log.d(TAG, "onError: 头条信息流广告加载错误，code = " + code + " message = " + message);
    //             US.putPicListFeedADEvent_tt(US.FAILED + ": " + code);
    //         }
    //
    //         @Override
    //         public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
    //             if (ads == null) return;
    //             if (isTest) {
    //                 Log.d(TAG, "onNativeExpressAdLoad: 获取头条信息流广告列表成功 size = " + ads.size());
    //             }
    //             for (TTNativeExpressAd ad : ads) {
    //                 feedAdList.add(new TTFeedAdWrapper(mContext, ad, mRequestManager, mTTAppDownloadListenerMap, adPositionName));
    //             }
    //         }
    //     });
    // }

    /**
     * @return boolean 头条的不需要考虑是否是新建的广告
     */
    @Override
    @Nullable
    public IFeedAd getNextAD(@NotNull String ad_id, ADHolder adHolder, String adEventName, String adPositionName) {
        // 检查无效或效果差广告
        // 过期或加载失败的或者已经点击过的
        for (int i = adList.size() - 1; i >= 0; i--) {
            TTFeedAdWrapper ad = adList.get(i);
            if (!checkValid(ad)) { // 注意check会删除
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(TAG,"广告池删除过期和加载失败广告, ID = " + i + "\n 广告信息为： " + ad.getAdInfo());
                }
            }
        }

        // 检查广告重复
        // 加载的广告会出现重复的，影响效果，目前去除策略是:
        // 根据间隔数逐渐增大来去除，间隔小，重复感高，先删除，比如1,2间隔1个就重复，删掉一个，依次
        // 目前一次只删除一个，删多了，刷新多，影响点击率
        // for (int len = 1; len <= adList.size() - 1; len++) {
        //     boolean hasRemove = false;
        //     for (int j = 0; j < adList.size(); j++) {
        //         int k = (j + len) % adList.size();
        //         TTFeedAdWrapper ad_j = adList.get(j);
        //         TTFeedAdWrapper ad_k = adList.get(k);
        //         if (ad_j.isLoadSuccess() && ad_k.isLoadSuccess() && ad_j.contentEquals(ad_k)) {
        //             // 删除后加载的一个
        //             NativeAdClass r = ad_k;
        //             if (ad_j.getLoadTime() > ad_k.getLoadTime()) {
        //                 r = ad_j;
        //             }
        //             adList.remove(r);
        //             r.destroy();
        //             hasRemove = true;
        //             if (isTest) {
        //                 Logcat.d("广告池删除重复广告, ID = " + k + "\n 广告信息为： " + r.getAdInfo());
        //             }
        //             break;
        //         }
        //     }
        //     if (hasRemove)
        //         break;
        // }

        // 轮询广告，让用户看到的广告尽量不同，先找序号大的，
        for (int i = lastId + 1; i < adList.size(); i++) {
            TTFeedAdWrapper ad = adList.get(i);
            if (ad_id.equals(ad.getAdId())) {
                lastId = i;
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(TAG,"广告池直接获取广告, ID = " + i + "\n 广告信息为： " + ad.getAdInfo());
                }
                return  ad;
            }
        }
        if (adList.size() >= maxFeedAdNumber) { // 装满了，那么找序号小的，否则新建
            for (int i = 0; i < lastId && i < adList.size(); i++) {
                TTFeedAdWrapper ad = adList.get(i);
                if (ad_id.equals(ad.getAdId())) {
                    lastId = i;
                    if (LogUtil.debugPicListFeedAd) {
                        Log.d(TAG,"广告池直接获取广告, ID = " + i + "\n 广告信息为： " + ad.getAdInfo());
                    }
                    return  ad;
                }
            }
        }

        // 没装满，并且大的一端没找到，直接新建，
        // 或者，装满了，但都没找到，删除并新建
        int widht = adHolder.container.getLayoutParams().width;
        TTFeedAdWrapper newAd = new TTFeedAdWrapper(mActivity, adHolder.container, mTTAdNative,
                ad_id, adPositionName, widht);
        if (LogUtil.debugPicListFeedAd) {
             Log.d(TAG,"广告池新建了广告");
        }// 如果加入之后数量超过， 删除一个
        if (adList.size() + 1 > maxFeedAdNumber && adList.size() >= 1) {
            TTFeedAdWrapper oldestAd = adList.get(0);
            for (TTFeedAdWrapper adClass : adList) {
                if (adClass.getLoadTime() < oldestAd.getLoadTime()) {
                    oldestAd = adClass;
                }
            }
            adList.remove(oldestAd);
            oldestAd.destroy();
        }
        adList.add(newAd);
        lastId = adList.size() - 1;
        return newAd;
    }

    public void preloadOneFeedAd(String ad_id, String adPositionName, int width) {
        // 没装满，并且大的一端没找到，直接新建，
        // 或者，装满了，但都没找到，删除并新建
        TTFeedAdWrapper newAd = new TTFeedAdWrapper(mActivity, null, mTTAdNative,
                ad_id, adPositionName, width);
        if (LogUtil.debugPicListFeedAd) {
            Log.d(TAG, "广告池预加载头条信息流了广告");
        }
        newAd.loadAdResources(null);
        adList.add(newAd);
        lastId = adList.size() - 2;
    }

    @Override
    public boolean checkValid(IFeedAd ad) {
        if (adList != null && !adList.contains(ad)) {
            if (LogUtil.debugPicListFeedAd) {
                LogUtil.d("广告已从广告池删除，无效，将被删除" + "\n 广告信息为： " + ad.getAdInfo());
            }
            return false;
        }

        if (ad.isInvalid()) {
            if (LogUtil.debugPicListFeedAd) {
                LogUtil.d("广告无效，将被删除" + "\n 广告信息为： " + ad.getAdInfo());
            }
            adList.remove(ad);
            ad.destroy();
            return false;
        }
        return true;
    }

    public int getAdSize() {
        return adList.size();
    }
}
