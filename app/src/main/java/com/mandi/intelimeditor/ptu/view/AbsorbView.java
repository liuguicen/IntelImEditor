package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.ptu.PtuUtil;

/**
 * Created by LiuGuicen on 2017/3/6 0006.
 */

public class AbsorbView extends View {

    interface AbsorbViewListener {
        /**
         * 吸取到的颜色，每次都触摸都有
         */
        void onAbsorbColor(int color);

        /**
         * 停止吸取颜色时的回调
         */
        void onStopAbsorb();
    }

    Paint framePaint;
    Rect frameRect;
    int frameWidth = Util.dp2Px(3.5f);
    private final int absorbRadius = 0;
    private Bitmap sourceBm;
    private Rect srcBound;
    private Rect dstBound;
    private float lastX, lastY;
    private float downX, downY;

    AbsorbViewListener absorbViewListener;

    public AbsorbView(Context context, @NonNull View view, Bitmap source, Rect srcBound, Rect dstBound) {
        super(context);
        this.sourceBm = source;
        if (this.sourceBm == null) {
            sourceBm = loadBitmapFromViewBySystem(view);
        }

        this.srcBound = srcBound;
        if (this.srcBound == null)
            this.srcBound = new Rect(0, 0, sourceBm.getWidth(), sourceBm.getHeight());

        this.dstBound = dstBound;
        if (this.dstBound == null) {
            this.dstBound = new Rect(0, 0, view.getHeight(), view.getWidth());
        }

        framePaint = new Paint();
        framePaint.setStrokeWidth(frameWidth);
        framePaint.setColor(0xbf5af158);
        framePaint.setStyle(Paint.Style.STROKE);
        frameRect = new Rect(dstBound.left + frameWidth / 2, dstBound.top + frameWidth / 2,
                dstBound.right - frameWidth / 2, dstBound.bottom - frameWidth / 2);
    }

    public void setAbsorbViewListener(AbsorbViewListener absorbViewListener) {
        this.absorbViewListener = absorbViewListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (GeoUtil.getDis(x, y, lastX, lastY) < 6)//距离很小不处理
            return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (!frameRect.contains((int) downX, (int) downY)
                        && GeoUtil.getDis(downX, downY, x, y) < Util.dp2Px(3)) {
                    // 点击了图片外的区域，取消吸取颜色
                    if (absorbViewListener != null) {
                        absorbViewListener.onStopAbsorb();
                    }
                    return true;
                }
                break;

        }
        if (frameRect.contains((int) downX, (int) downY)) {
            absorbColor(event.getX(), event.getY());
        }
        // invalidate();
        return true;
    }

    /**
     * 从View 的某个位置吸取颜色
     *
     * @param x view中的x坐标
     * @param y view中的y坐标
     */
    private void absorbColor(float x, float y) {
        if (absorbViewListener != null && sourceBm != null) {
            float[] fpxy = PtuUtil.getLocationAtBaseBm(x, y, srcBound, dstBound);
            int rx = (int) (fpxy[0] + 0.5f), ry = (int) (fpxy[1] + 0.5f);
            long lColor = 0;
            //取点周围的几个点的平均值
            for (int i = -absorbRadius; i <= absorbRadius; i++) {
                for (int j = -absorbRadius; j <= absorbRadius; j++) {
                    int nx = rx + i, ny = ry + j;
                    if (0 <= nx && nx < sourceBm.getWidth() && 0 <= ny && ny < sourceBm.getHeight()) {
                        lColor += sourceBm.getPixel(nx, ny);
                    }
                }
            }
            int reColor = (int) (lColor / ((absorbRadius * 2 + 1) * (absorbRadius * 2 + 1)));
            absorbViewListener.onAbsorbColor(reColor);
        }
    }

    /**
     * 从窗口中移除时，此时进行Bitmap等的回收
     */
    @Override
    public void onDetachedFromWindow() {
        if (absorbViewListener != null) {
            absorbViewListener.onStopAbsorb();
        }
        sourceBm = null;
        srcBound = null;
        super.onDetachedFromWindow();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(frameRect, framePaint);
        super.onDraw(canvas);
    }

    public static Bitmap loadBitmapFromViewBySystem(View v) {
        if (v == null) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();
        Bitmap bitmap = v.getDrawingCache();
        return bitmap;
    }
}
