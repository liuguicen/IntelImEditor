package com.mandi.intelimeditor.ptu.rendpic;

import com.mandi.intelimeditor.common.util.Util;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/26
 *      version : 1.0
 * <pre>
 */
public class RendPicGestureConfig {
    public static final float DEFALT_DIVIDE_PIC_LIMIT = Util.dp2Px(14);
    public static final float MERGE_RATIO_TO_DIVIDE = 1f/3;

    /**
     * 这个变量一般需要手动设置，将屏幕尺寸的dp转换为px
     */
    public float divideLimit = DEFALT_DIVIDE_PIC_LIMIT;

    /**
     * 默认的最小有效移动距离
     */
    public static float DEFAULT_MIN_VALID_MOVE_DIS = 3;
    public float minValidMoveDis = DEFAULT_MIN_VALID_MOVE_DIS;
    /** 合并的限值比分开的限值小，这样合并之后不会又立即分开图片 */
    public float mergeLimit = DEFALT_DIVIDE_PIC_LIMIT * MERGE_RATIO_TO_DIVIDE;

    public RendPicGestureConfig() {

    }

    public RendPicGestureConfig setMinValidMoveDis(float min) {
        minValidMoveDis = min;
        return this;
    }

    public RendPicGestureConfig setDivideLimit(float divideLimit) {
        this.divideLimit = divideLimit;
        mergeLimit = divideLimit * MERGE_RATIO_TO_DIVIDE;
        return this;
    }
}
