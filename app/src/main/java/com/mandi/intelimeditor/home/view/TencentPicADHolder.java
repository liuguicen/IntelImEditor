package com.mandi.intelimeditor.home.view;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.ad.ADHolder;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/04
 *      version : 1.0
 * <pre>
 */
public class TencentPicADHolder extends ADHolder {
    @Nullable
    public TextView adMarkTv;

    public TencentPicADHolder(View layout, FrameLayout container, @Nullable TextView adMarkTv) {
        super(layout, container);
        this.adMarkTv = adMarkTv;
    }
}
