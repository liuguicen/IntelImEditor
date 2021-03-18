package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mathandintell.intelimeditor.R;


/**
 * Created by Administrator on 2019/5/2.
 * 圆形颜色选择块
 */
public class ColorLumpCircle extends View {
    private int mColor;
    private Paint mPaint;
    private int radius = 30;
    private int strokeWidth;
    private boolean isChecked;

    public ColorLumpCircle(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ColorLumpCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ColorLumpCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.obtainStyledAttributes(
                attrs,
                R.styleable.ColorLumpCircle,
                defStyle,
                0);
        mColor = ta.getColor(R.styleable.ColorLumpCircle_colorLumpColor, Color.BLACK);
        radius = ta.getDimensionPixelSize(R.styleable.ColorLumpCircle_colorLumpRadius, radius);
        strokeWidth = ta.getDimensionPixelSize(R.styleable.ColorLumpCircle_strokeWidth, strokeWidth);
        ta.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(mColor);
    }

    public int getColor() {
        return mColor;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制底部颜色
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.FILL);
        if (mColor == Color.WHITE) {
            mPaint.setColor(Color.GRAY);
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius, mPaint);
            mPaint.setColor(mColor);
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius - 2, mPaint);
        } else if (mColor == 0) {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius, ColorPicker.getTransparentPaint());
        } else {
            mPaint.setColor(mColor);
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius, mPaint);
        }

        if (isChecked) {
            //绘制白色圈圈
            mPaint.setStrokeWidth(strokeWidth);
            //如果背景是白色的，绘制圆圈
            if (mColor == Color.WHITE) {
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(Color.GRAY);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius / 2f, mPaint);
            } else {
                mPaint.setColor(Color.WHITE);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, radius - strokeWidth, mPaint);
            }
        }

    }

    /**
     * 设置选中的颜色，同时重回绘图像
     *
     * @param color
     */
    public void setColor(int color) {
        mColor = color;
        postInvalidate();
    }

    /**
     * 设置选中状态
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        isChecked = checked;
        postInvalidate();
    }
}
