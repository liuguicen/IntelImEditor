package com.mandi.intelimeditor.ptu.dig;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.R;


/**
 * Created by liuguicen on 2016/8/5.
 */
class MatPathManager {
    private Paint mPenPaint, mlinePaint;
    /**
     * 保存的是在原图中的位置
     */
    private Path mLinePath;
    private float lastLineTop;
    private float lastLineLeft;

    public MatPathManager() {
        mlinePaint = new Paint();
        mlinePaint.setAntiAlias(true);
        mlinePaint.setDither(true);
        mlinePaint.setStrokeWidth(3);
        mlinePaint.setStyle(Paint.Style.STROKE);
        mlinePaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));
        mlinePaint.setColor(Util.getColor(R.color.base_color1));

        mLinePath = new Path();
        lastLineLeft = lastLineTop = -1;
    }

    public void addLinePoint(float[] xy) {
        float lineLeft = xy[0], lineTop = xy[1];
        mLinePath.quadTo(lastLineLeft, lastLineTop,
                (lastLineLeft + lineLeft) / 2,
                (lastLineTop + lineTop) / 2);
        lastLineLeft = lineLeft;
        lastLineTop = lineTop;
    }

    public void drawLine(Canvas sourceCanvas) {
        sourceCanvas.drawPath(mLinePath, mlinePaint);
    }


    /**
     * @param xy 起点的坐标
     */
    public void startDrawLine(float[] xy) {
        mLinePath.moveTo(xy[0], xy[1]);
        lastLineLeft = xy[0];
        lastLineTop = xy[1];
    }

    public void finishDrawLine() {
        mLinePath.rewind();
    }
}
