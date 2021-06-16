package com.mandi.intelimeditor.ad.ttAD.videoAd;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.mandi.intelimeditor.ad.TTAdConfig;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.R;


/**
 * 主要负责激励视频广告
 */
public class TTRewardVadManager {

    private String TAG = "RewardVideoAdManager";
    private TTAdNative mTTAdNative;
    private TTRewardVideoAd mttRewardVideoAd;
    private boolean mHasShowDownloadActive = false;
    private static TTRewardVadManager rewardVideoAdManager;
    private VadListener rewardVadListener;
    private Context mContext;

    public static TTRewardVadManager getInstance() {
        if (rewardVideoAdManager == null) {
            synchronized (TTRewardVadManager.class) {
                if (rewardVideoAdManager == null) {
                    rewardVideoAdManager = new TTRewardVadManager();
                }
            }
        }
        return rewardVideoAdManager;
    }

    public void initAd(Context context) {
        mContext = context;
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        // 进入APP时会保证这些权限已经获取，头条这里会开一个AC来获取权限，对性能消耗太大
//        TTAdManagerHolder.get().requestPermissionIfNecessary(context);
        //step3:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(context);
    }

    public void setRewardVadListener(VadListener listener) {
        this.rewardVadListener = listener;
    }


    public void loadAd(Activity activity) {
        loadAd(TTAdConfig.REWARD_VIDEO_AD_ID, TTAdConstant.VERTICAL, activity);
    }


    private void loadAd(String codeId, int orientation, Activity activity) {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(AllData.getScreenWidth(),
                        AllData.getScreenHeight())
                .setExpressViewAcceptedSize(AllData.getScreenWidth(),
                        AllData.getScreenHeight())
                .setRewardName("图片资源") //奖励的名称
                .setRewardAmount(1)  //奖励的数量
                .setUserID(AllData.localUserId)//用户id,必传参数
                .setMediaExtra("media_extra") //附加参数，可选
                .setOrientation(orientation) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        //step5:请求广告
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                if (rewardVadListener != null) {
                    rewardVadListener.onError(code, message, AdData.TT_AD_NAME);
                    if (code == -2) {
                        ToastUtils.show(R.string.network_error_try_latter);
                    }
                }
                LogUtil.d(TAG, code + " msg = " + message);
            }

            //视频广告的素材加载完毕，视频还没有加载完成，
            // 比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                LogUtil.d(TAG, "视频广告素材加载完成");
                mttRewardVideoAd = ad;
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        LogUtil.d(TAG, "rewardVideoAd show");
                        if (rewardVadListener != null) {
                            rewardVadListener.onAdShow();
                        }
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        if (rewardVadListener != null) {
                            rewardVadListener.onVideoAdBarClick();
                        }
                        LogUtil.d(TAG, "rewardVideoAd bar click");
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        if (rewardVadListener != null) {
                            rewardVadListener.onVideoPlayComplete();
                        }
                        LogUtil.d(TAG, "rewardVideoAd complete");
                    }


                    @Override
                    public void onAdClose() {
                        LogUtil.d(TAG, "rewardVideoAd close");
                        if (rewardVadListener != null) {
                            rewardVadListener.onAdClose();
                        }
                    }

                    @Override
                    public void onVideoError() {
                        if (rewardVadListener != null) {
                            rewardVadListener.onVideoError();
                        }
                        LogUtil.d(TAG, "rewardVideoAd error");
                    }

                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg)  {
                        LogUtil.d(TAG, "verify:" + rewardVerify + " amount:" + rewardAmount +
                                " name:" + rewardName);
                        if (rewardVadListener != null) {
                            rewardVadListener.onRewardVerify(rewardVerify, rewardAmount, rewardName);
                        }
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称

                    @Override
                    public void onSkippedVideo() {
                        LogUtil.d(TAG, "rewardVideoAd has onSkippedVideo");
                    }
                });
                mttRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        // 这个回调，头条的SDK下载过程中会一直调用，所以这里加了判断
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            if (rewardVadListener != null) {
                                rewardVadListener.startDownload();
                            }
                            Log.d(TAG, "onDownloadActive: 开始下载");
                            ToastUtils.show("下载中，可在通知栏中取消");
                        }

                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        LogUtil.d(TAG, "下载暂停，点击下载区域继续");
                        ToastUtils.show("下载暂停，点击下载区域继续");
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        LogUtil.d(TAG, "下载失败，点击下载区域重新下载");
                        ToastUtils.show("下载失败，点击下载区域重新下载");
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        LogUtil.d(TAG, "下载完成，点击下载区域重新下载");
                        ToastUtils.show("下载完成");
                        if (rewardVadListener != null) {
                            rewardVadListener.downLoadFinish();
                        }
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        // 目前头条的SDK会调用两次安装完成 BUG？
                        if (rewardVadListener != null) {
                            rewardVadListener.onInstallSuccess();
                        }
                        LogUtil.d(TAG, "安装完成");
                        ToastUtils.show("安装完成，点击下载区域打开");
                    }
                });
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                LogUtil.d(TAG, "视频广告缓存完成");
                showAd(activity);
            }
        });
    }

    private void showAd(Activity activity) {
        if (mttRewardVideoAd != null) {
            //step6:在获取到广告后展示
            //该方法直接展示广告
            mttRewardVideoAd.showRewardVideoAd(activity);
            //展示广告，并传入广告展示的场景
            //            mttRewardVideoAd.showRewardVideoAd(activity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            mttRewardVideoAd = null;
        } else {
            LogUtil.d(TAG, "请先加载广告");
        }
    }
}
