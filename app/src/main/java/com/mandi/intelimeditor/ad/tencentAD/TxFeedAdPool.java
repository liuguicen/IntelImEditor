package com.mandi.intelimeditor.ad.tencentAD;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.IFeedAd;
import com.mandi.intelimeditor.ad.IFeedAdPool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.home.view.TencentPicADHolder;
import com.qq.e.ads.nativ.ADSize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/04
 *      version : 1.0
 * <pre>
 *      腾讯的信息流广告池，里面包含纯图片广告
 *
 *      图片列表里面的广告由于刷新次数很多，而目前腾讯广告价格计算是价格与点击率是二次函数类似的增长关系，而不是线性关系
 *      应该在一定阈值类降低刷新数，提高点击数。通过这个广告池，重复利用刷新到的广告，降低刷新量
 *      具体策略：先设置一个预定的最大数量阈值，然后根据广告重用率和点击率等增加或减少阈值
 */
public class TxFeedAdPool implements IFeedAdPool {
    public static final String TAG = "ListAdPool";
    public static int START_AD_CAPACITY = 6;
    public static int MAX_AD_CAPCITY = 10;
    private final Context mContext;
    private final boolean isPurePic;
    private int mMaxNumber = START_AD_CAPACITY;
    @Nullable
    private List<TxFeedAd> adList;
    private int lastId = 0;

    public TxFeedAdPool(Context context, boolean isPurePic) {
        mContext = context;
        this.isPurePic = isPurePic;
    }

    public void destroy() {
        if (adList != null) {
            for (TxFeedAd txFeedAd : adList) {
                txFeedAd.destroy();
            }
            adList.clear();
        }
    }

    public void setMaxAdNumber(int number) {
        mMaxNumber = number;
    }

    /**
     * @return boolen 是否是新建的
     */
    @Override
    public IFeedAd getNextAD(@NotNull String ad_id,
                             ADHolder adHolder,
                             String adEventName,
                             String adPositionName) {
        TextView adMarkTv = null;
        int height = adHolder.container.getLayoutParams().height;
        ADSize adSize = new ADSize(ADSize.FULL_WIDTH, height);
        @NotNull FrameLayout container = adHolder.container;

        if (isPurePic && adHolder instanceof TencentPicADHolder) {
            TencentPicADHolder tencentPicAdHolder = ((TencentPicADHolder) adHolder);
            adMarkTv = tencentPicAdHolder.adMarkTv;
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "加载广告，位置 = " + tencentPicAdHolder.getAdapterPosition() +
                    "\n高度 = " + height);
        }

        Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告池，开始获取广告");
        if (adList == null) {
            adList = new ArrayList<>();
        }

        // 检查无效或效果差广告
        // 过期或加载失败的或者已经点击过的
        for (int i = adList.size() - 1; i >= 0; i--) {
            TxFeedAd ad = adList.get(i);
            if (!checkValid(ad)) { // 注意check会删除
                if (LogUtil.debugTxFeedAd) {
                    Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告池删除过期和加载失败广告, ID = " + i + "\n 广告信息为： " + ad.getAdInfo());
                }
            }
        }

        // 检查广告重复
        // 加载的广告会出现重复的，影响效果，目前去除策略是:
        // 根据间隔数逐渐增大来去除，间隔小，重复感高，先删除，比如1,2间隔1个就重复，删掉一个，依次
        // 目前一次只删除一个，删多了，刷新多，影响点击率
        for (int len = 1; len <= adList.size() - 1; len++) {
            boolean hasRemove = false;
            for (int j = 0; j < adList.size(); j++) {
                int k = (j + len) % adList.size();
                TxFeedAd ad_j = adList.get(j);
                TxFeedAd ad_k = adList.get(k);
                if (ad_j.isLoadSuccess() && ad_k.isLoadSuccess() && ad_j.contentEquals(ad_k)) {
                    // 删除后加载的一个
                    TxFeedAd r = ad_k;
                    if (ad_j.getLoadTime() > ad_k.getLoadTime()) {
                        r = ad_j;
                    }
                    adList.remove(r);
                    r.destroy();
                    hasRemove = true;
                    if (LogUtil.debugTxFeedAd) {
                        Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告池删除重复广告, ID = " + k + "\n 广告信息为： " + r.getAdInfo());
                    }
                    break;
                }
            }
            if (hasRemove)
                break;
        }

        // 轮询广告，让用户看到的广告尽量不同，先找序号大的，
        for (int i = lastId + 1; i < adList.size(); i++) {
            TxFeedAd ad = adList.get(i);
            if (ad_id.equals(ad.getAdId())) {
                lastId = i;
                if (LogUtil.debugTxFeedAd) {
                    Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告池直接获取广告, ID = " + i + "\n 广告信息为： " + ad.getAdInfo());
                }
                return ad;
            }
        }
        if (adList.size() >= mMaxNumber) { //装满了，再找序号小的
            for (int i = 0; i < lastId && i < adList.size(); i++) {
                TxFeedAd ad = adList.get(i);
                if (ad_id.equals(ad.getAdId())) {
                    lastId = i;
                    if (LogUtil.debugTxFeedAd) {
                        Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告池直接获取广告, ID = " + i + "\n 广告信息为： " + ad.getAdInfo());
                    }
                    return ad;
                }
            }
        }

        // 没装满，并且大的一端没找到，直接新建，
        // 或者，装满了，但都没找到，删除并新建
        TxFeedAd newAd ;
        if (isPurePic) newAd = new TxFeedAd(container, ad_id, adEventName, adPositionName);
        else newAd = new TxFeedAd(mContext, container, ad_id, adEventName, adPositionName);
        newAd.setIsPurePic(isPurePic);
        if (isPurePic) {
            newAd.setAdMarkTv(adMarkTv);
        }
        newAd.setAdSize(adSize);
        if (LogUtil.debugTxFeedAd) {
            Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告池新建了广告腾讯");
        }
        if (adList.size() + 1 > mMaxNumber && adList.size() >= 1) { // 如果加入之后数量超过， 删除一个
            TxFeedAd oldestAd = adList.get(0);
            for (TxFeedAd adClass : adList) {
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

    public boolean checkValid(@NotNull IFeedAd ad) {
        if (adList != null && !adList.contains(ad)) {
            if (LogUtil.debugTxFeedAd) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告已从广告池删除，无效，将被删除" + "\n 广告信息为： " + ad.getAdInfo());
            }
            return false;
        }

        if (ad.isInvalid()) {
            if (LogUtil.debugTxFeedAd) {
                Log.d(TAG + (isPurePic ? "纯图" : "信息流"), "广告无效，将被删除" + "\n 广告信息为： " + ad.getAdInfo());
            }
            adList.remove(ad);
            ad.destroy();
            return false;
        }
        return true;
    }
}
