package com.mandi.intelimeditor.ptu.draw;

import android.graphics.Paint;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/11/05
 *      version : 1.0
 * <pre>
 */
public class MPaint extends Paint {

    /**
     * 用于透明色特殊处理，
     * 一些时候画笔需要设置成背景色或者特殊颜色来表示透明色，这时无法直接判断画笔是否画的透明色，
     * 所以加上这个
     */
    public boolean isTransparent = false;
    public boolean isErase = false;
    /**
     * api里面设置模糊半径之后不能获取，只能手动记录一般
     */
    public float blurWidth = 0; //

    public MPaint() {
        super();
    }

    public MPaint(Paint paint) {
        super(paint);
    }

    public MPaint(MPaint mPaint) {
        super(mPaint);
        isTransparent = mPaint.isTransparent;
        isErase = mPaint.isErase;
        blurWidth = mPaint.blurWidth;
    }
}
