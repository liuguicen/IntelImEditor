package com.mandi.intelimeditor.ptu.gif;

import android.graphics.Bitmap;

import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.ptu.imageProcessing.FaceFeature;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/27
 *      version : 1.0
 * <pre>
 */
public class GifFrame {
    public Bitmap bm;
    public int delay;//图像延迟时间, 毫秒
    public boolean isChosen;

    /**
     * 保存帧在原图中的ID，目前送到gif播放器中，ID信息不存在时需要
     */
    public int originID = 0;
    public float[] faceLandmark;
    public FaceFeature faceFeature;

    public GifFrame(Bitmap image, int delay) {
        this.bm = image;
        this.delay = delay;
    }

    /**
     * 让帧的大小和时延一致
     * TODO 2020/10/25 添加后的图，为保证效果尺寸比例，不能压缩和拉伸，可以等比例缩放
     */
    @Nullable
    public static GifFrame getSimilarFrame(@NotNull GifFrame old, @NotNull String path) {
        int dstW = old.bm.getWidth(), dstH = old.bm.getHeight();
        Bitmap firstBm = BitmapUtil.decodeLossslessInSize(path, dstW * dstH);
        if (firstBm == null) return null;
        Bitmap scaledBm = Bitmap.createScaledBitmap(firstBm, dstW, dstH, true);
        if (scaledBm != firstBm) {
            firstBm.recycle();
        }
        return new GifFrame(scaledBm, old.delay);
    }
}
