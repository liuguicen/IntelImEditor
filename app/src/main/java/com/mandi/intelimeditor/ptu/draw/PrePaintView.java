package com.mandi.intelimeditor.ptu.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class PrePaintView extends BaseDrawView {
    private Path path = new Path();
    private int w = 20;

    public PrePaintView(Context context) {
        super(context);
        init(context);
    }

    public PrePaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PrePaintView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void dealTransparentColor() {
        invalidate();
    }

    private void init(Context context) {
        setPaintStyle();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.reset();
        int mItemWidth = getWidth();
        int halfItem = mItemWidth / 2;
        path.moveTo(w, getHeight() / 2f);
        for (int i = 0; i < mItemWidth + getWidth(); i += mItemWidth) {
            path.rQuadTo(halfItem / 2f, -100, halfItem, 0);
            path.rQuadTo(halfItem / 2f, 100, halfItem, 0);
        }
//        path.moveTo(w, getHeight() - w);
//        path.rQuadTo((getWidth() - w) / 2f, 40, (getWidth() - w) * 2 / 3f, getHeight());
//        path.rQuadTo(getWidth() - w, getHeight() / 2f, getWidth() - w, 0);
        canvas.drawPath(path, mPaint);
    }

    public void setPaintSize(int size) {
        mPaint.setStrokeWidth(size);
        w = size;
        invalidate();
    }

    public void setPaintAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidate();
    }

}
