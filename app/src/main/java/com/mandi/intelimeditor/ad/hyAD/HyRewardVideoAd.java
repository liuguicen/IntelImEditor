package com.mandi.intelimeditor.ad.hyAD;//package com.mandi.intelimeditor.ad.hyAD;
//
//import android.app.Activity;
//import android.util.Log;
//import android.view.View;
//
//import com.hytt.hyadxopensdk.hyadxopenad.HyAdXOpenMotivateVideoAd;
//import com.hytt.hyadxopensdk.interfoot.HyAdXOpenListener;
//
//import com.mandi.intelimeditor.ad.ttAD.videoAd.RewardVadListener;
//
//public class HyRewardVideoAd {
//    public static final String TAG = "HyRewardVideoAd";
//    private HyAdXOpenMotivateVideoAd hyAdXOpenMotivateVideoAd;
//    private RewardVadListener rewardVadListener;
//
//    public HyRewardVideoAd(Activity ac) {
//        hyAdXOpenMotivateVideoAd =
//                new HyAdXOpenMotivateVideoAd(ac,
//                        "7268884",
//                        new HyAdXOpenListener() {
//                            //广告填充时候回调，
//                            //code默认为200，
//                            //searchid为每次请求时候的唯一id
//                            //view为当广告为图文素材时返回广告的view，开发者可直接添加到指定的viewgroup中
//                            @Override
//                            public void onAdFill(int code, final String searchId, View view) {
//                                hyAdXOpenMotivateVideoAd.show();
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.downLoadFinish();
//                                }
//                                Log.d(TAG, "onAdFill: ");
//                            }
//
//                            //广告被展示时候回调
//                            @Override
//                            public void onAdShow(int code, String searchId) {
//                                Log.d(TAG, "onAdShow: ");
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.onAdShow();
//                                }
//                            }
//
//                            //广告被点击时候的回调
//                            @Override
//                            public void onAdClick(int code, String searchId) {
//                                Log.d(TAG, "onAdClick: ");
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.onVideoAdBarClick();
//                                }
//                            }
//
//                            //广告被关闭时候的回调
//                            @Override
//                            public void onAdClose(int code, String searchId) {
//                                Log.d(TAG, "onAdClose: ");
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.onAdClose();
//                                }
//                            }
//
//                            //广告请求出错时候的回调，如没有填充
//                            @Override
//                            public void onAdFailed(int code, String message) {
//                                Log.d(TAG, "onAdFailed: " + code + " " + message);
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.onError(code, message, "HY");
//                                }
//                            }
//
//                            //视频广告下载成功回调
//                            @Override
//                            public void onVideoDownloadSuccess(int code, String searchId) {
//                                Log.d(TAG, "onVideoDownloadSuccess: ");
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.downLoadFinish();
//                                }
//                            }
//
//                            //视频广告下载失败回调
//                            @Override
//                            public void onVideoDownloadFailed(int code, String searchId) {
//                                Log.d(TAG, "onVideoDownloadFailed: ");
//                            }
//
//                            //视频广告开始播放回调
//                            @Override
//                            public void onVideoPlayStart(int code, String searchId) {
//                                Log.d(TAG, "onVideoPlayStart: ");
//                            }
//
//                            //视频广告播放完成回调
//                            @Override
//                            public void onVideoPlayEnd(int code, String searchId) {
//                                Log.d(TAG, "onVideoPlayEnd: ");
//                                if (rewardVadListener != null) {
//                                    rewardVadListener.onRewardVerify(true, 1, "解锁图片"); // 通知获取奖励成功
//                                }
//                            }
//                        });
//    }
//
//    public void loadAd() {
//        hyAdXOpenMotivateVideoAd.load();
//    }
//
//    public void setRewardVadListener(RewardVadListener rewardVadListener) {
//        this.rewardVadListener = rewardVadListener;
//    }
//}
