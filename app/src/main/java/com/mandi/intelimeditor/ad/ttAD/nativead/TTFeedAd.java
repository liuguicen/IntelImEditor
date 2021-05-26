package com.mandi.intelimeditor.ad.ttAD.nativead;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTAdManagerHolder;
import com.mandi.intelimeditor.common.util.ToastUtils;

import java.util.List;

/**
 * 信息流广告加载，使用步骤：
 * 1、新建对象 NativeExpressAd
 * 2、加载广告 loadExpressAd
 */
public class TTFeedAd {
    private String TAG = "NativeExpressAd";

    private TTAdNative mTTAdNative;
    private FrameLayout mExpressContainer;
    private Context mContext;
    private TTNativeExpressAd mTTAd;

    private long startTime = 0;
    private boolean mHasShowDownloadActive = false;

    public TTFeedAd(Context mContext) {
        this.mContext = mContext;
        initAd();
    }

    private void initAd() {
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        // 进入APP时会保证这些权限已经获取，头条这里会开一个AC来获取权限，对性能消耗太大
        // TTAdManagerHolder.get().requestPermissionIfNecessary(mContext);
        //step3:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(mContext);
    }


    /**
     * 加载原生广告
     *
     * @param container 广告显示容器
     * @param codeId    广告ID
     */
    public void loadExpressAd(FrameLayout container, String codeId, int width) {
        mExpressContainer = container;
        if (mExpressContainer != null) {
            mExpressContainer.removeAllViews();
        }
        float expressViewWidth = 250;
        if (width > 0) {
            expressViewWidth = width;
        }
        float expressViewHeight = 0; //0 高度自适应
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(640, 320)//这个参数设置即可，不影响模板广告的size
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                // ToastUtils.show("load error : " + code + ", " + message);
                if (mExpressContainer != null) {
                    mExpressContainer.removeAllViews();
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    return;
                }
                mTTAd = ads.get(0);
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                mTTAd.render();
            }
        });
    }

    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
                // ToastUtils.show(mContext, "广告被点击");
                AdData.hasClikedAdJust = true;
            }

            @Override
            public void onAdShow(View view, int type) {
                // ToastUtils.show(mContext, "广告展示");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e("ExpressView", "render fail:" + (System.currentTimeMillis() - startTime));
                // ToastUtils.show(mContext, msg + " code:" + code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.e("ExpressView", "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
//                ToastUtils.show(mContext, "渲染成功");
                if (mExpressContainer != null) {
                    mExpressContainer.removeAllViews();
                    mExpressContainer.addView(view);
                }
            }
        });
        //dislike设置
       bindDislike(ad, false);
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                // ToastUtils.show(mContext, "点击开始下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    ToastUtils.show("应用下载中，点击通知栏可暂停", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                // ToastUtils.show(mContext, "下载暂停，点击继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                // ToastUtils.show(mContext, "下载失败，点击重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                ToastUtils.show("安装完成，点击图片可打开");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                // ToastUtils.show(mContext, "点击安装", Toast.LENGTH_LONG);
            }
        });
    }


    public void destroy() {
        if (mTTAd != null) {
            mTTAd.destroy();
        }
    }

    /**
     * 设置广告的不喜欢，注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        //使用默认模板中默认dislike弹出样式
        ad.setDislikeCallback((Activity) mContext, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int position, String value, boolean isForce) {
                //用户选择不喜欢原因后，移除广告展示
                mExpressContainer.removeAllViews();
            }

            @Override
            public void onCancel() {
            }
        });
    }
}
