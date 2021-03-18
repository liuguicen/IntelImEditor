package com.mandi.intelimeditor.ptu.dig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.ViewConfiguration;

import com.mandi.intelimeditor.common.util.UnLevelRect;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.ptu.view.IconBitmapCreator;
import com.mathandintell.intelimeditor.R;


/**
 * Created by liuguicen on 2016/8/3.
 *
 * @description 底图移动，钢笔不动
 */
public class Pen {

    private final Context mContext;
    private final Rect totalBound;
    private Bitmap penBmMove, penBmLine;
    private boolean isDrawLine;
    /**
     * 可旋转，旋转角度，默认30度
     */
    private double angle = -Math.PI / 6;
    /**
     * <p> penLeft和penTop是旋转中心，也是
     * <P>钢笔的笔记所在位置，不会因为旋转改变
     */
    float pointLeft, pointTop, penWidth, penHeight;

    private final UnLevelRect mBound;
    private Paint mPenPaint;
    private Bitmap penBm;
    public float lastX, lastY;
    private float startAngle;

    Pen(Context context, Rect bound) {
        mContext = context;
        mPenPaint = new Paint();
        mPenPaint.setAntiAlias(true);
        mPenPaint.setDither(true);

        isDrawLine = false;
        penWidth = 60;
        int c1 = Util.getColor(R.color.mat_pen_line1);
        int c_1 = Color.argb(Color.alpha(c1) / 3, Color.red(c1), Color.green(c1), Color.blue(c1));
        int c2 = Util.getColor(R.color.mat_pen_line2);
        int c_2 = Color.argb(Color.alpha(c2) / 3, Color.red(c2), Color.green(c2), Color.blue(c2));
        penBmLine = IconBitmapCreator.createPen(Math.round(penWidth), c1, c2);
        penBm = penBmMove = IconBitmapCreator.createPen(Math.round(penWidth), c_1, c_2);
        penHeight = penBm.getHeight();

        pointLeft = bound.width() / 2;
        pointTop = bound.height() / 2;
        totalBound = bound;

        angle = startAngle = -30;
        this.mBound = new UnLevelRect();
        resetBound();
    }


    //动作以及包含区域范围部分
    /*********************************************************************************************/

    /**
     * 移动，会先检测边界(图片边界和总的边界）
     *
     * @param picBound 图片的范围
     */
    public void move(float nx, float ny, Rect picBound) {
        if (lastX < 0) {//从外面突然移进了Pen时
            lastX = nx;
            lastY = ny;
        }
        float dx = nx - lastX, dy = ny - lastY;
        //图片边界
        if (pointLeft + dx < picBound.left) {
            dx = picBound.left - pointLeft;
        }
        if (pointLeft + dx > picBound.right) {
            dx = picBound.right - pointLeft;
        }
        if (pointTop + dy < picBound.top) {
            dy = picBound.top - pointTop;
        }
        if (pointTop + dy > picBound.bottom) {
            dy = picBound.bottom - pointTop;
        }

        //钢笔尖不能超出总的边界
        if (pointLeft + dx < totalBound.left) {
            dx = totalBound.left - pointLeft;
        }
        if (pointLeft + dx > totalBound.right) {
            dx = totalBound.right - pointLeft;
        }
        if (pointTop + dy < totalBound.top) {
            dy = totalBound.top - pointTop;
        }
        if (pointTop + dy > totalBound.bottom) {
            dy = totalBound.bottom - pointTop;
        }

        //总的边界
        UnLevelRect tempRect = new UnLevelRect(mBound);
        tempRect.translate(dx, dy);
        if (tempRect.getLeft() + penWidth / 2 < totalBound.left) {//左边超出
            if (pointTop < totalBound.height() - penHeight)//在上部
                rotateTo(-30);
            else//在下部
                rotateTo(-150);
        }
        if (tempRect.getRight() - penWidth / 2 > totalBound.right) {//右边超出
            if (pointTop < totalBound.height() - penHeight)//在上部
                rotateTo(30);
            else
                rotateTo(150);
        }
        if (tempRect.getTop() + penHeight / 3 < totalBound.top) {//上边超出
            if (pointLeft < totalBound.width() - penWidth)
                rotateTo(-30);
            else//在右部
                rotateTo(30);
        }
        if (tempRect.getButtom() - penHeight / 3 > totalBound.bottom) {//下边超出
            if (pointLeft < totalBound.width() - penWidth)//在左部
                rotateTo(-150);
            else
                rotateTo(150);
        }

        lastX = nx;
        lastY = ny;

        pointLeft += dx;
        pointTop += dy;
        resetBound();
    }


    public void rotateTo(float angle) {
        this.angle = angle;
    }


    private void resetBound() {
        MPoint p0 = new MPoint(pointLeft, pointTop);
        MPoint p1 = new MPoint(p0.x - penWidth / 2, p0.y), p2 = new MPoint(p0.x + penWidth / 2, p0.y),
                p3 = new MPoint(p0.x + penWidth / 2, p0.y + penHeight), p4 = new MPoint(p0.x - penWidth / 2, p0.y + penHeight);

        p1 = GeoUtil.rotatePoint(p0, p1, angle);
        p2 = GeoUtil.rotatePoint(p0, p2, angle);
        p3 = GeoUtil.rotatePoint(p0, p3, angle);
        p4 = GeoUtil.rotatePoint(p0, p4, angle);
        mBound.set(p1, p2, p3, p4);
    }

    public boolean contain(float x, float y) {
        return mBound.contain(x, y);
    }

    public void prepareMove(float x, float y) {
        lastX = x;
        lastY = y;
    }


    //绘制部分

    /*****************************************************************************/
    void drawPen(Canvas canvas) {
        canvas.save();
        canvas.rotate((float) (angle / 180f), pointLeft, pointTop);
        canvas.drawBitmap(penBm, pointLeft - penWidth / 2, pointTop, mPenPaint);
        canvas.restore();
    }

    /**
     * 离开钢笔
     */
    public void outTouch() {
        lastX = lastY = -1;
    }

    /**
     * 在抠图界面，没有返回，以前点击过划线，再次点击时
     *
     * @param totalBound 总的范围
     */
    public void reSet(Rect totalBound) {
        lastX = lastY = -1;
        pointLeft = totalBound.width() / 2;
        pointTop = totalBound.height() / 2;
        penBm = penBmMove;
        isDrawLine = false;
        angle = startAngle;
        resetBound();
    }


    /*******************************************************************************/
    public boolean isDrawLine() {
        return isDrawLine;
    }

    public long lastTime = -1;

    public boolean isDoubleClick() {
        long curTime = System.currentTimeMillis();
        //貌似系统定义的双击正是300毫秒 ViewConfiguration.getDoubleTapTimeout()
        if (curTime - lastTime < ViewConfiguration.getDoubleTapTimeout()) {
            lastTime = curTime;
            isDrawLine = !isDrawLine;
            changeApearence();
            return true;
        } else {
            lastTime = curTime;
            return false;
        }
    }

    public void cancel() {
        lastTime = -1;
    }

    private void changeApearence() {
        if (isDrawLine) penBm = penBmLine;
        else penBm = penBmMove;
    }

    public void releaseResource() {
        if (penBmLine != null) {
            penBmLine.recycle();
            penBmLine = null;
        }
        if (penBmMove != null) {
            penBmMove.recycle();
            penBmMove = null;
        }
    }
}
