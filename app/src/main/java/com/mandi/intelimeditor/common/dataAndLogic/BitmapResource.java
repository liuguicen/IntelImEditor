package com.mandi.intelimeditor.common.dataAndLogic;

import android.graphics.Bitmap;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/06/09
 *      version : 1.0
 *      位图资源，用于重复利用，添加了一些标志位
 * <pre>
 */
public class BitmapResource {

    Bitmap bitmap;
    /**
     * PTu操作的临时图片，P图结束之后，通常PTUActivity Destroy之后就释放
     */
    public boolean isPTutemp = false;
    public BitmapResource(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public BitmapResource(Bitmap bitmap, boolean isPTuTemp) {
        this.bitmap = bitmap;
        this.isPTutemp = isPTuTemp;
    }
}
