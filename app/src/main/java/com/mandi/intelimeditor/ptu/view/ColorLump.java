package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/5/24.
 */
public class ColorLump extends View {
    private int mColor;
    Paint mPaint = new Paint();

    public ColorLump(Context context) {
        super(context);
    }

    public ColorLump(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * 设置选中的颜色，同时重回绘图像
     *
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        invalidate();
    }

    public int getColor() {
        return mColor;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (mColor == 0) { // 透明色，特殊处理
            canvas.drawRect(0,0,getWidth() - getPaddingRight(), getHeight(), ColorPicker.getTransparentPaint());
        }
        mPaint.setColor(mColor);
        canvas.drawRect(0, 0, getWidth() - getPaddingRight(), getHeight(), mPaint);
        super.onDraw(canvas);
    }
}
