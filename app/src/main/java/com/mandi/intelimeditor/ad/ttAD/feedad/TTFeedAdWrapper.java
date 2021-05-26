package com.mandi.intelimeditor.ad.ttAD.feedad;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.RequestManager;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.mandi.intelimeditor.ad.ADHolder;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.IFeedAd;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.R;

import java.util.List;
import java.util.Map;



/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/03
 *      version : 1.0
 * <pre>
 *     包装TTFeedAd, 用于获取UI控件，绑定UI数据，点击等事件处理，等能
 */
public class TTFeedAdWrapper implements IFeedAd {
    public static final String TAG = "TTFeedAdWrapper";
    private final String mAdPositionName;
    private FrameLayout mContainer;
    private RequestManager mRequestManager;
    private Activity mActivity;
    private TTNativeExpressAd mFeedAd;
    private TTAdNative mTTAdNative;
    private int mAdWidth;
    private Map<ADHolder, TTAppDownloadListener> mTTAppDownloadListenerMap;

    private boolean mIsClicked = false;
    private long mFirstShowTime;
    private boolean mHasError;
    private String mAdID;
    /**
     * 用于删除后的广告，由于广告采用了position到广告池里面取广告，删除之后位置会不对应，
     * 所以不删数据，把类型改了，然后让视图变成不可见
     */
    private boolean isDeleted = false;


    public TTFeedAdWrapper(Activity activity,
                           FrameLayout container,
                           TTAdNative ttAdNative,
                           String adId,
                           String adPositionName,
                           int adWidth) {
        mActivity = activity;
        mContainer = container;
        mTTAdNative = ttAdNative;
        mAdID = adId;
        mFirstShowTime = System.currentTimeMillis(); // 初始化也要先设置一下，不然=0，引起后面判断不对
        mAdPositionName = adPositionName;
        mAdWidth = adWidth;
    }

    /**
     * @return 是否已经加载好资源
     */
    @Override
    public boolean isLoadSuccess() {
        return mFeedAd != null;
    }

    @Override
    public void loadAdResources(ADHolder adHolder) {
        if (LogUtil.debugPicListFeedAd) {
            Log.d(TAG, "loadAdResources: 开始加载头条信息流广告");
        }
        if (adHolder != null) {
            mContainer = adHolder.container;
        }
        float expressWidth = Util.px2Dp(mAdWidth);
        float expressViewHeight = 0;  //高度设置为0,则高度会自适应
        //step4:创建feed广告请求类型参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mAdID)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setExpressViewAcceptedSize(expressWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setAdCount(1) //请求广告数量为1到3条
                .build();

        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(TAG, "onError: 头条信息流广告加载错误，code = " + code + " message = " + message);
                US.putPicListFeedADEvent_tt(mAdPositionName + ": " + US.FAILED + ": " + code);
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() < 1) return;
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(TAG, "onNativeExpressAdLoad: 获取头条信息流广告成功");
                }
                mFeedAd = ads.get(0);
                // load完成之后设置监听器，监听rend完成。 rend完成之后就会利用container显示显示
                mFeedAd.setExpressInteractionListener(getEventListener());
                // 调用rend
                Log.d(TAG, "onNativeExpressAdLoad: 开始渲染头条信息流广告");
                initOtherSetting(mFeedAd);
                mFeedAd.render();
            }
        });
    }

    /**
     * 两个功能：
     * 渲染完成之后要调用显示方法
     * 统计显示和点击等事件
     */
    public TTNativeExpressAd.ExpressAdInteractionListener getEventListener() {
        return new TTNativeExpressAd.ExpressAdInteractionListener() {

            @Override
            public void onAdClicked(View view, int type) {
                mIsClicked = true;
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(TAG, "onAdClicked: 广告收到点击");
                }
                US.putPicListFeedADEvent_tt(mAdPositionName + ": " + US.CLICK);
            }

            @Override
            public void onAdShow(View view, int type) {
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(TAG, "onAdClicked: 广告展示");
                }
                mFirstShowTime = System.currentTimeMillis();
                US.putPicListFeedADEvent_tt(mAdPositionName + ": " + US.EXPOSURE);
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                // TToast.show(NativeExpressListActivity.this, msg + " code:" + code);
                mHasError = true;
                US.putPicListFeedADEvent_tt(mAdPositionName + ": " + US.REND_ERROR + ": " + code);
                if (LogUtil.debugPicListFeedAd) {
                    Log.e(TAG, "onRenderFail: 头条信息流广告渲染失败" + msg + ":  " + code);
                }
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                //返回view的宽高 单位 dp
                // TToast.show(NativeExpressListActivity.this, "渲染成功");
                if (LogUtil.debugPicListFeedAd) {
                    Log.d(TAG, "onRenderSuccess: 头条信息流广告渲染成功，准备显示");
                }
                bindData(mContainer);
            }
        };
    }

    @Override
    public void bindData(ADHolder holder) {
        bindData(holder.container);
    }

    private void bindData(FrameLayout container) {
        if (LogUtil.debugPicListFeedAd) {
            Log.d(TAG, "bindData: 开始显示头条信息流广告，绑定广告数据");
        }
        if (isDeleted) return;
        mContainer = container;
        if (mContainer != null) {
            mContainer.removeAllViews();
            View expressAdView = mFeedAd.getExpressAdView();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            expressAdView.setLayoutParams(params);

            ViewParent parent = expressAdView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(expressAdView);
            }
            mContainer.addView(expressAdView);
        }
    }

    private void initOtherSetting(TTNativeExpressAd feedAd) {
        //设置dislike弹窗，这里展示自定义的dialog
        bindDislike(feedAd, true);
        switch (feedAd.getInteractionType()) {
            case TTAdConstant.INTERACTION_TYPE_DOWNLOAD:
                // bindDownloadListener(holder, feedAd);
                break;
        }
    }

    /**
     * 设置广告的不喜欢，注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     *
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private void bindDislike(TTNativeExpressAd feedAd, boolean customStyle) {
        // if (customStyle) {
        //     //使用自定义样式
        //     List<FilterWord> words = getFilterWords();
        //     if (words == null || words.isEmpty()) {
        //         return;
        //     }
        //
        //     final DislikeDialog dislikeDialog = new DislikeDialog(mContext, words);
        //     dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
        //         @Override
        //         public void onItemClick(FilterWord filterWord) {
        //             //屏蔽广告
        //             TToast.show(mContext, "点击 " + filterWord.getName());
        //             //用户选择不喜欢原因后，移除广告展示
        //             mData.remove(ad);
        //             notifyDataSetChanged();
        //         }
        //     });
        //     ad.setDislikeDialog(dislikeDialog);
        //     return;
        // }
        //使用默认模板中默认dislike弹出样式
        feedAd.setDislikeCallback(mActivity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int position, String value, boolean isForce) {
                // TToast.show(mContext, "点击 " + value);
                // 用户选择不喜欢原因后，移除广告展示
                // mData.remove(ad);
                // notifyDataSetChanged();
                // TODO: 2020/1/7 需要清除所有同类广告
                ToastUtils.show(R.string.we_will_reduce_likely_ad);
                isDeleted = true;
                mContainer.removeAllViews();
                if (mActivity instanceof FragmentActivity) {
                    VipUtil.judgeShowToOpenVip((FragmentActivity) mActivity, 1.0 / 6);
                }
            }

            @Override
            public void onCancel() {
                // TToast.show(mContext, "点击取消 ");
                if (mActivity instanceof FragmentActivity) {
                    VipUtil.judgeShowToOpenVip((FragmentActivity) mActivity, 1.0 / 6);
                }
            }
        });
    }


    private void bindDownloadListener(TTNativeExpressAd feedAd, final ADHolder adViewHolder, TTNativeExpressAd ad) {
        TTAppDownloadListener downloadListener = new TTAppDownloadListener() {
            private boolean mHasShowDownloadActive = false;

            @Override
            public void onIdle() {
                if (!isValid()) {
                    return;
                }
                // TToast.show(mContext, "点击广告开始下载");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    // TToast.show(mContext, appName + " 下载中，点击暂停", Toast.LENGTH_LONG);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                // TToast.show(mContext, appName + " 下载暂停", Toast.LENGTH_LONG);

            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                // TToast.show(mContext, appName + " 下载失败，重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                // TToast.show(mContext, appName + " 安装完成，点击打开", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                if (!isValid()) {
                    return;
                }
                // TToast.show(mContext, appName + " 下载成功，点击安装", Toast.LENGTH_LONG);

            }

            @SuppressWarnings("BooleanMethodIsAlwaysInverted")
            private boolean isValid() {
                return mTTAppDownloadListenerMap.get(adViewHolder) == this;
            }
        };
        //一个ViewHolder对应一个downloadListener, isValid判断当前ViewHolder绑定的listener是不是自己
        ad.setDownloadListener(downloadListener); // 注册下载监听器
        mTTAppDownloadListenerMap.put(adViewHolder, downloadListener);
    }

    @Override
    public long getLoadTime() {
        return mFirstShowTime;
    }

    @Override
    public boolean isInvalid() {
        return isExpire() || isClicked() || isHadError();
    }

    public boolean isClicked() {
        return mIsClicked;
    }


    public boolean isExpire() {
        return System.currentTimeMillis() - mFirstShowTime > AdData.NATIVE_AD_LOAD_TIME_INTERVAL;
    }

    public boolean isHadError() {
        return mHasError;
    }

    @Override
    public String getAdInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getAdId() {
        return mAdID;
    }

    public void setContainer(FrameLayout container) {
        this.mContainer = container;
    }
}
