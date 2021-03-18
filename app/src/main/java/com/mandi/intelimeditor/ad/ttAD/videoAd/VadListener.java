package com.mandi.intelimeditor.ad.ttAD.videoAd;

public interface VadListener {
    /**
     * 激励视频和视频广告 交互回调接口
     */
    //视频广告展示回调
    void onAdShow();

    void onVideoAdBarClick();

    /**
     * 视频广告关闭回调，目前的逻辑是close之后解锁，根据前面是否获取奖励成功
     */
    void onAdClose();

    //视频广告播放完毕回调
    void onVideoPlayComplete();

    //视频广告播完验证奖励有效性回调，参数分别为是否有效，奖励数量，奖励名称
    void onRewardVerify(boolean var1, int var2, String var3);

    void onError(int code, String msg, String res);

    void onVideoError();

    void startDownload();

    void downLoadFinish();

    void onInstallSuccess();

    void onSkipVideo();
}