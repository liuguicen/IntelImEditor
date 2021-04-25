package com.mandi.intelimeditor.ptu.common;

import android.graphics.Bitmap;

public class TransferController extends SecondFuncController {
    /**
     * 宽度值占图像宽度的比例
     */
    public Bitmap styleBm;
    public Bitmap contentBm;

    public TransferController(Bitmap contentBm, Bitmap styleBm) {
        this.contentBm = contentBm;
        this.styleBm = styleBm;
    }
}
