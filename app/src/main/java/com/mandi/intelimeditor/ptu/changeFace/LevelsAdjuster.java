package com.mandi.intelimeditor.ptu.changeFace;

import android.graphics.Bitmap;

import com.mandi.intelimeditor.ptu.imageProcessing.BaseIPUtil;
import com.mandi.intelimeditor.ptu.imageProcessing.BmStatistic;

import org.jetbrains.annotations.NotNull;

/**
 * 用于控制色阶调整的
 */
public class LevelsAdjuster {
    public Bitmap originalBm;
    public Bitmap adjustedBm = null; // 调整后的bm
    public int[] originalGrayPixels; // 原始对应的灰度图
    public BmStatistic bmStatistic;  // 原始灰度图的统计数据
    public static final int DEFAULT_SHADOW = 20;
    public static final int DEFAULT_HIGHLIGHT = 180;
    public int shadow = 20;
    public int highlight = 180;

    /**
     * @param originalBm 不能为空，为空这个类没办法工作，所以将判空任务交给上层
     */
    public void generateData(@NotNull Bitmap originalBm, boolean isLevelsChangeMuch) {
        this.originalBm = originalBm;
        if (adjustedBm == null || adjustedBm.getWidth() != originalBm.getWidth()
                || adjustedBm.getHeight() != originalBm.getHeight())
            adjustedBm = Bitmap.createBitmap(originalBm.getWidth(), originalBm.getHeight(), Bitmap.Config.ARGB_8888);
        bmStatistic = new BmStatistic();
        originalGrayPixels = BaseIPUtil.toGrayArray(originalBm, bmStatistic);
        if (!isLevelsChangeMuch) {
            shadow = 0;
            highlight = 255;
        }
    }

    public int getAutoShadow() {
        return 15;
    }

    public int getAutoHighlight() {
        return (int) (bmStatistic.grayAverage * (1 + 1 / 15f));
    }

    public boolean adjustLevel(int shadow, int highlight) {
        this.shadow = shadow;
        this.highlight = highlight;
        return BaseIPUtil.adjustLevels(originalGrayPixels, adjustedBm, shadow, highlight);
    }

    public boolean autoAdjust() {
        return adjustLevel(getAutoShadow(), getAutoHighlight());
    }
}
