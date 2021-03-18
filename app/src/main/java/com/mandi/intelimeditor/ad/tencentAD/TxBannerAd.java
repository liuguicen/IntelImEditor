package com.mandi.intelimeditor.ad.tencentAD;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

public class TxBannerAd implements UnifiedBannerADListener {
    public static final String TAG = "TxBannerAd";
    private final Activity activity;
    private final FrameLayout adContainer;
    private final int bannerWidth;
    public static final float BANNER_WH_RATIO = 6.4f;
    UnifiedBannerView bv;

    public TxBannerAd(Activity activity, FrameLayout adContainer, int width) {
        this.activity = activity;
        this.adContainer = adContainer;
        this.bannerWidth = width;
    }

    public void show() {
        adContainer.setVisibility(View.VISIBLE);
        getBanner().loadAD();
    }


    private UnifiedBannerView getBanner() {
        if (this.bv != null) {
            adContainer.removeView(bv);
            bv.destroy();
        }
        String posId = "3061517941928420";
        this.bv = new UnifiedBannerView(activity, posId, this);
        this.bv.setRefresh(30);
        // this.bv = new UnifiedBannerView(this, Constants.APPID, posId, this);
        adContainer.addView(bv, getUnifiedBannerLayoutParams());
        return this.bv;
    }

    /**
     * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
     *
     * @return
     */
    private FrameLayout.LayoutParams getUnifiedBannerLayoutParams() {
        return new FrameLayout.LayoutParams(bannerWidth, Math.round(bannerWidth / BANNER_WH_RATIO));
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (bv != null) {
            bv.setLayoutParams(getUnifiedBannerLayoutParams());
        }
    }

    public void destroy() {
        adContainer.removeAllViews();
        adContainer.setVisibility(View.GONE);
        if (bv != null) {
            bv.destroy();
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        Log.d(TAG, "onNoAD: ");
    }

    @Override
    public void onADReceive() {
        Log.d(TAG, "onADReceive: ");
    }

    @Override
    public void onADExposure() {
        Log.d(TAG, "onADExposure: ");
    }

    @Override
    public void onADClosed() {
        adContainer.removeAllViews();
        if (bv != null) {
            bv.destroy();
            bv = null;
        }
    }

    public boolean hasClosed() {
        return bv == null;
    }

    @Override
    public void onADClicked() {
        Log.d(TAG, "onADClicked: ");
    }

    @Override
    public void onADLeftApplication() {
        Log.d(TAG, "onADLeftApplication: ");
    }

    @Override
    public void onADOpenOverlay() {
        Log.d(TAG, "onADOpenOverlay: ");
    }

    @Override
    public void onADCloseOverlay() {
        Log.d(TAG, "onADCloseOverlay: ");
    }

    public void hide() {
        adContainer.setVisibility(View.GONE);
    }

    public void setVisibility(int visibility) {
        if (adContainer.getVisibility() != visibility) {
            adContainer.setVisibility(visibility);
        }
    }
}
