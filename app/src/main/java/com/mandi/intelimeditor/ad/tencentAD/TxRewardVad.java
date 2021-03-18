package com.mandi.intelimeditor.ad.tencentAD;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.ttAD.videoAd.VadListener;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.util.AdError;

import java.util.Map;

/**
 * 腾讯激励视频广告
 */
public class TxRewardVad implements RewardVideoADListener {
    public static final String TAG = "TxRewardVad";
    private Context mContext;
    private RewardVideoAD rewardVideoAD;
    private boolean adLoaded = false;
    private boolean videoCached = false;
    private VadListener rewardVadListener;

    public TxRewardVad(Activity activity) {
        this.mContext = activity;
        init();
    }

    private void init() {
        rewardVideoAD = new RewardVideoAD(mContext, AdData.GDT_ID_REWARD_VAD, this, true);
    }

    public void loadAd() {
        rewardVideoAD.loadAD();
    }

    private void showAd() {
        // 3. 展示激励视频广告
        if (adLoaded && rewardVideoAD != null) {//广告展示检查1：广告成功加载，此处也可以使用videoCached来实现视频预加载完成后再展示激励视频广告的逻辑
            if (!rewardVideoAD.hasShown()) {//广告展示检查2：当前广告数据还没有展示过
                long delta = 1000;//建议给广告过期时间加个buffer，单位ms，这里demo采用1000ms的buffer
                //广告展示检查3：展示广告前判断广告数据未过期
                if (SystemClock.elapsedRealtime() < (rewardVideoAD.getExpireTimestamp() - delta)) {
//                    if (view.getId() == R.id.show_ad_button) {
                    rewardVideoAD.showAD();
//                    } else {
//                        rewardVideoAD.showAD(RewardVideoActivity.this);
//                    }
                } else {
                    Log.d(TAG, "showAd: 激励视频广告已过期，请再次请求广告后进行广告展示！");
                }
            } else {
                Log.d(TAG, "此条广告已经展示过，请再次请求广告后进行广告展示！");
            }
        } else {
            Log.d(TAG, "成功加载广告后再进行广告展示！");
        }
    }

    public void setRewardVadListener(VadListener rewardVadListener) {
        this.rewardVadListener = rewardVadListener;
    }

    @Override
    public void onADLoad() {
        adLoaded = true;
        showAd();
        Log.d(TAG, "eCPMLevel = " + rewardVideoAD.getECPMLevel());
    }

    @Override
    public void onVideoCached() {
        videoCached = true;

    }

    /**
     * 激励视频广告页面展示
     */
    @Override
    public void onADShow() {
        Log.i(TAG, "onADShow");
    }

    /**
     * 激励视频广告曝光
     */
    @Override
    public void onADExpose() {
        rewardVadListener.onAdShow();
        Log.i(TAG, "onADExpose");
    }

    /**
     * 激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
     */
    @Override
    public void onReward(Map<String, Object> map) {
        Log.i(TAG, "onReward");
        rewardVadListener.onRewardVerify(true, 1, "图片解锁成功");
    }

    @Override
    public void onADClick() {
        rewardVadListener.onVideoAdBarClick();
    }

    @Override
    public void onVideoComplete() {
        rewardVadListener.onVideoPlayComplete();
    }

    @Override
    public void onADClose() {
        rewardVadListener.onAdClose();
    }

    @Override
    public void onError(AdError adError) {
        rewardVadListener.onError(adError.getErrorCode(), adError.getErrorMsg(), "TX");
    }
}
