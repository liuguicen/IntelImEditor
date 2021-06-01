package com.mandi.intelimeditor.ptu.common;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class TransferController extends SecondFuncController {
    @Nullable
    public final String contentUrl;
    @Nullable
    public final String styleUrl;
    /**
     * 宽度值占图像宽度的比例
     */
    @Nullable
    public Bitmap styleBm;
    @Nullable
    public Bitmap contentBm;

    public TransferController(Bitmap contentBm, String contentUrl, Bitmap styleBm, String styleUrl) {
        this.contentBm = contentBm;
        this.styleBm = styleBm;
        this.contentUrl = contentUrl;
        this.styleUrl = styleUrl;
    }
}
