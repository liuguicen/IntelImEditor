package com.mandi.intelimeditor.ad.ttAD.videoAd;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.TTAdConfig;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.dialog.LoadingDialog;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.dialog.ToOpenVipDialog;

import org.jetbrains.annotations.Nullable;

/**
 * 主要负责展示视频广告
 */
public class FullScreenVadManager {

    private String TAG = "FullScreenVideoAdManager";
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullScreenVideoAd;
    private boolean mHasShowDownloadActive = false;
    private static FullScreenVadManager sRewardFullScreenVideoAdManager;
    private VadListener vadListener;
    private Context mContext;
    private LoadingDialog loadingDialog;

    public static FullScreenVadManager getInstance() {
        if (sRewardFullScreenVideoAdManager == null) {
            synchronized (FullScreenVadManager.class) {
                if (sRewardFullScreenVideoAdManager == null) {
                    sRewardFullScreenVideoAdManager = new FullScreenVadManager();
                }
            }
        }
        return sRewardFullScreenVideoAdManager;
    }

    public static void showFullScreenVad(FragmentActivity activity, @Nullable Runnable task, String notice) {
        FullScreenVadManager fullScreenVideoAdManager = getInstance();
        if (notice != null) {
            Toast.makeText(activity, notice, Toast.LENGTH_LONG).show();
        }
        fullScreenVideoAdManager.loadAd(activity);
        fullScreenVideoAdManager.setVadListener(new VadListener() {
            @Override
            public void onAdShow() {
                US.putFullScreenVideoAdEvent(US.EXPOSURE);
            }

            @Override
            public void onVideoAdBarClick() {
                US.putFullScreenVideoAdEvent(US.CLICK_VIDEO_BAR);
            }

            @Override
            public void onAdClose() {
                US.putFullScreenVideoAdEvent(US.CLOSE);
                if (Math.random() < 1.0 / 17) { // 显示开通VIP的对话框
                    ToOpenVipDialog toOpenVipDialog = ToOpenVipDialog.Companion.newInstance(activity);
                    toOpenVipDialog.showIt();
                }
                if (task != null) {
                    task.run();
                }
            }

            @Override
            public void onVideoPlayComplete() {
                US.putFullScreenVideoAdEvent(US.VIDEO_PLAY_COMPLETE);
            }

            @Override
            public void onRewardVerify(boolean var1, int var2, String var3) {
            }

            @Override
            public void onError(int code, String msg, String res) {
                US.putFullScreenVideoAdEvent(US.FAILED + code);
                if (task != null) {
                    task.run();
                }
            }

            @Override
            public void onVideoError() {
                US.putFullScreenVideoAdEvent("VideoError");
                if (task != null) {
                    task.run();
                }
            }

            @Override
            public void startDownload() {
                US.putFullScreenVideoAdEvent(US.START_DOWNLOAD);
            }

            @Override
            public void downLoadFinish() {
                US.putFullScreenVideoAdEvent(US.DOWNLOAD_COMPLETE);
            }

            @Override
            public void onInstallSuccess() {
                US.putFullScreenVideoAdEvent(US.INSTALL_SUCCESS);
            }

            @Override
            public void onSkipVideo() {
                US.putFullScreenVideoAdEvent(US.AD_SKIP);
            }
        });
    }

    public void initAd(Context context) {
        mContext = context;
        loadingDialog = LoadingDialog.newInstance("");
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
// 进入APP时会保证这些权限已经获取，头条这里会开一个AC来获取权限，对性能消耗太大
//        TTAdManagerHolder.get().requestPermissionIfNecessary(context);
        //step3:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(context);
    }

    public void setVadListener(VadListener listener) {
        this.vadListener = listener;
    }


    public void loadAd(Activity activity) {
        loadAd(TTAdConfig.FULL_SCREEN_VIDEO_AD_ID, TTAdConstant.VERTICAL, activity);
    }

    private void loadAd(String codeId, int orientation, Activity activity) {
        if (mTTAdNative == null) {
            US.putFullScreenVideoAdEvent(US.FAILED + " TTAdNative init error");
            return;
        }
        showLoading(activity);
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setOrientation(orientation)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        //step5:请求广告
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                if (vadListener != null) {
                    vadListener.onError(code, message, "TT");
                }
                hideLoading();
                LogUtil.d(TAG, message);
            }

            //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onFullScreenVideoCached() {
                LogUtil.d(TAG, "rewardVideoAd video cached");
                showAd(activity);
            }


            //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd videoAd) {

                //                Logcat.d(TAG, "rewardVideoAd loaded");
                mttFullScreenVideoAd = videoAd;
                mttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        LogUtil.d(TAG, "rewardVideoAd show");
                        if (vadListener != null) {
                            vadListener.onAdShow();
                        }
                        AdData.lastVideoAdShowTime = System.currentTimeMillis();
                        hideLoading();
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        if (vadListener != null) {
                            vadListener.onVideoAdBarClick();
                        }
                        LogUtil.d(TAG, "rewardVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        LogUtil.d(TAG, "rewardVideoAd close");
                        if (vadListener != null) {
                            vadListener.onAdClose();
                        }
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        if (vadListener != null) {
                            vadListener.onVideoPlayComplete();
                        }
                        LogUtil.d(TAG, "rewardVideoAd complete");
                    }


                    public void onVideoError() {
                        if (vadListener != null) {
                            vadListener.onVideoError();
                        }
                        LogUtil.d(TAG, "rewardVideoAd error");
                    }


                    @Override
                    public void onSkippedVideo() {
                        if (vadListener != null) {
                            vadListener.onSkipVideo();
                        }
                        LogUtil.d(TAG, "rewardVideoAd has onSkippedVideo");
                    }
                });
                mttFullScreenVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            if (vadListener != null) {
                                vadListener.startDownload();
                            }
                            ToastUtils.show("下载中，点击通知栏暂停");
                        }
                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        LogUtil.d(TAG, "下载暂停，点击下载区域继续");
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        LogUtil.d(TAG, "下载失败，点击下载区域重新下载");
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        LogUtil.d(TAG, "下载完成，点击下载区域重新下载");
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        if (vadListener != null) {
                            vadListener.onInstallSuccess();
                        }
                        // ToastUtils.show("安装完成，点击下载区域打开");
                    }
                });
            }
        });
    }

    private void showAd(Activity activity) {
        hideLoading();
        if (mttFullScreenVideoAd != null) {
            //step6:在获取到广告后展示
            //该方法直接展示广告
            mttFullScreenVideoAd.showFullScreenVideoAd(activity);
            //展示广告，并传入广告展示的场景
            //            mttRewardVideoAd.showRewardVideoAd(activity, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
            mttFullScreenVideoAd = null;
        } else {
            LogUtil.d(TAG, "请先加载广告");
        }
    }

    private void showLoading(Activity activity) {
        if (loadingDialog != null) {
            loadingDialog.showIt((FragmentActivity) activity);
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            Log.d(TAG, "hideLoading: ");
            loadingDialog.dismissAllowingStateLoss();
        }
    }
}
