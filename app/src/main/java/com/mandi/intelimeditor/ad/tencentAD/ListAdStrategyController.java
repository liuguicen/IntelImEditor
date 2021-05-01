package com.mandi.intelimeditor.ad.tencentAD;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.mandi.intelimeditor.EventName;
import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.IFeedAd;
import com.mandi.intelimeditor.ad.IFeedAdPool;
import com.mandi.intelimeditor.common.util.LogUtil;


import java.util.Random;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/04
 *      图片列表下的广告策略控制的类
 *      主要有两个功能，一个是
 * <pre>
 */
public class ListAdStrategyController {
    private final String mAdId;
    private final IFeedAdPool mAdPool;
    private final long seed;
    private Context mContext;
    private int startCount; // 第一个的间隔，保证首屏就能看到
    private int addPosition;
    private int minInterval; //
    private int maxInterval;
    private int mod = -1;

    private boolean mIsCanBeforeStart = true;
    private boolean isEndAdd; // 是否在末尾添加
    private final Random mRandom;
    private String umEventName = EventName.pic_resource_ad;
    private SparseArray<IFeedAd> mAdMap;

    public ListAdStrategyController(Context context, String adId, IFeedAdPool adPool,
                                    int startCount, int minInterval, int maxInterval, boolean isEndAdd) {
        this(context, adId, adPool,
                startCount, minInterval, maxInterval, isEndAdd,
                true);
    }

    /**
     * 设置友盟统计的事件名称
     */
    public void setUmEventName(String eventName) {
        umEventName = eventName;
    }

    /**
     * @param context
     * @param adId
     * @param adPool
     * @param startCount
     * @param minInterval
     * @param maxInterval
     * @param isEndAdd    是否在末尾添加
     */
    public ListAdStrategyController(Context context, String adId, IFeedAdPool adPool,
                                    int startCount, int minInterval, int maxInterval,
                                    boolean isEndAdd, boolean isCanBeforeStart) {

        mContext = context;
        mAdPool = adPool;
        seed = System.currentTimeMillis();
        mRandom = new Random(seed);
        if (startCount <= 0) {
            throw new IllegalArgumentException("startCount must be > 0");
        }
        this.startCount = startCount;
        this.minInterval = minInterval;
        this.maxInterval = maxInterval;
        this.isEndAdd = isEndAdd;
        mAdId = adId;
        mAdMap = new SparseArray<>();
        mIsCanBeforeStart = isCanBeforeStart;
    }

    /**
     * 重设广告指针位置，清空上一次填入的广告等
     */
    public void reSet() {
        setStartPosition();
        mRandom.setSeed(seed); // 为了让刷新列表之后加入位置差不多
        mAdMap.clear();
    }

    private void setStartPosition() {
        if (mIsCanBeforeStart) {
            this.addPosition = mRandom.nextInt(startCount);
        } else {
            this.addPosition = startCount + mRandom.nextInt(minInterval / 3);
        }
        modifyPositionByMod();
    }

    /**
     * @param mod 在整数倍位置添加，比如3
     */
    public void setMod(int mod) {
        this.mod = mod;
    }

    public void setAddPosition(int addPosition) {
        this.addPosition = addPosition;
        modifyPositionByMod();
    }

    //    /**
    //     * @return 是否处于关闭期
    //     */
    //    public boolean inInCloseTerm() {
    //        return System.currentTimeMillis() - AdData.getAdCloseTimeByID(mAdId)
    //                > AdData.getCloseIntervalByID(mAdId);
    //    }

    public boolean isAddAd(int position) {
        if (position == addPosition) {
            // 计算下一次添加的位置
            addPosition += minInterval;
            int interval = maxInterval - minInterval;
            if (interval < 1) interval = 1;
            addPosition += mRandom.nextInt(interval);
            modifyPositionByMod();
            return true;
        } else {
            return false;
        }
    }

    public boolean isEndAdd() {
        return isEndAdd;
    }

    private void modifyPositionByMod() {
        if (mod > 1) {
            addPosition -= addPosition % mod;
            if (LogUtil.debugPicListFeedAd) {
                Log.d("modifyPositionByMod: ", "addPosition = " + addPosition);
            }
        }
    }

    public String getAD_ID() {
        return mAdId;
    }

    /**
     * 和加载图片的原理类似，这里的位置相当于图片的url
     * 流程是现根据位置去map中获取加载好的广告，相当于获取到图片的缓存，
     * map相当于具有位置对应关系的一级缓存，如果获取到了，那么调用bind方法显示广告
     * <p>如果map中没有获取到，那么就从广告池获取，广告池里面可能有已经加载好的广告，这相当于二级缓存，
     * 获取到了，同样的，直接显示，并且将其放到一级缓存map中
     * <p>广告池里面也没能获取到，那么就去创建一个新的广告，创建过来之后，调用加载广告资源的方法，
     * 后面异步得到广告数据，得到广告数据之后再显示广告，对于腾讯的，显示直接显示就行，头条的广告，
     * 显示过程还会有一次异步过程，先render，然后再异步回调中show
     * <p>
     * 这里面接口话支持两种广告，头条的和腾讯的
     *
     * @param adPositionName 友盟事件下二级名称
     */
    public void showAd(int position, ADHolder holder, String adPositionName) {
        IFeedAd ad = mAdMap.get(position);
        // 无效广告，重新加载
        if (ad != null && !mAdPool.checkValid(ad)) {
            mAdMap.remove(position);
            ad = null;
        }
        if (ad == null) {
            ad = mAdPool.getNextAD(mAdId, holder, umEventName, adPositionName);
            if (ad == null) // 一般不会，防止崩溃
                return;
            mAdMap.put(position, ad);
        }
        // 如果没加载成功，load广告资源，load之后内部可能调用bindData，所以这里判断，load了就不bind
        if (!ad.isLoadSuccess()) {
            ad.loadAdResources(holder);
        } else {
            ad.bindData(holder);
        }
    }

    public void onAdHolderRecycled(ADHolder adHolder) {
        adHolder.container.removeAllViews();
        LogUtil.d("回收了" + adHolder.getAdapterPosition());
    }

    public boolean isAddInEnd(int size) { // 最后一个位置，如果最后一位距离下次添加位置minInterval / 2,也就是说距离上次添加位置>=minInterval / 2,此时添加
        if (isEndAdd && Math.abs(addPosition - size) < minInterval / 2)
            return true;
        return false;
    }
}
