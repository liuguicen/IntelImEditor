package com.mandi.intelimeditor.ptu.rendpic;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;

import java.util.ArrayList;
import java.util.List;

/**
 * 撕图操作和绘图相关数据的持有者
 * 相当于撕图的数据层，自己一开始没想到，这里也要一个小的MVC呀，只要视图及其对应的操作都复杂的情况下，都应该这样
 * 把Activity等的技巧拿过来
 */
public class RendDrawDate {
    /**
     * 画笔，都一样，静态的就行
     */
    public static Paint crackPaint;
    public static float START_CRACK_WIDTH = Util.dp2Px(3);

    static {
        crackPaint = new Paint();
        crackPaint.setAntiAlias(false);
        crackPaint.setDither(false);
        crackPaint.setColor(0xffaaaaaa);
        crackPaint.setStyle(Paint.Style.STROKE);
    }

    public float scaleRatioBeforeRend;

    /**
     * 裂纹相关数据
     **/
    public Path[] picPath;
    /**
     * 锯齿路径
     ***/
    private Path stPath;
    public Path stPathInView;
    public List<MPoint> testPoints;
    public float crackWidth = 0;

    /**
     * 撕图触摸点，拉扯的点
     */
    private MPoint[] rendTouch;
    public float touchAngle;

    public float srcAngle;

    /**
     * 图片分开后的数据
     */
    /**
     * 碎片在原图坐标系下的范围
     */
    public MRect[] fragBoundInSrcBm;
    /**
     * 原图坐标系下的碎片的bm
     */
    public Bitmap[] fragBm;
    /**
     * 碎片在View所在的布局内的范围，不是View本身的这样计算起来方便一些
     */
    public MRect[] fragBoundInLayout;

    public RendDrawDate(Bitmap srcBm) {
        rendTouch = new MPoint[2];
        testPoints = new ArrayList<>();
        stPath = new Path();

        picPath = new Path[2];

        fragBm = new Bitmap[2];
        fragBoundInSrcBm = new MRect[2];
        fragBoundInLayout = new MRect[2];

        fragBoundInSrcBm[0] = new MRect();
        fragBoundInSrcBm[1] = new MRect();
        fragBoundInLayout[0] = new MRect();
        fragBoundInLayout[1] = new MRect();
        for (int i = 0; i < 2; i++) {
            rendTouch[i] = new MPoint();
            fragBoundInSrcBm[i] = new MRect();
            picPath[i] = new Path();
        }
    }

    /**
     * 数据回到准备状态, 基本所有数据都清掉
     */
    public void reset2_ready() {
        stPath.reset();
        testPoints.clear();

        for (int i = 0; i < 2; i++) {
            picPath[i].reset();
            rendTouch[i].set(0, 0);
            if (fragBm[i] != null)
                fragBm[i].recycle();
            fragBm[i] = null;
            fragBoundInSrcBm[i].set(0, 0, 0, 0);
            fragBoundInLayout[i].set(0, 0, 0, 0);
        }
    }

    public void setRendTouchP(MPoint touch1, MPoint touch2) {
        rendTouch[0].set(touch1); // 不直接赋值，避免被外部修改
        rendTouch[1].set(touch2);
        float mdx = touch1.x - touch2.x,
                mdy = touch1.y - touch2.y;
        touchAngle = (float) Math.atan2(mdy, mdx);
    }

    public void setSrcBmTouchPoint(MPoint p1, MPoint p2) {
        srcAngle = (float) GeoUtil.getAnglePI(p1, p2);
    }

    public MPoint getTouchCenter() {
        return new MPoint((rendTouch[0].x + rendTouch[1].x) / 2f, (rendTouch[0].y + rendTouch[1].y) / 2f);
    }

    public float getFragStartW(int i) {
        return fragBm[i].getWidth() * scaleRatioBeforeRend;
    }

    public float getFragStartH(int i) {
        return fragBm[i].getHeight() * scaleRatioBeforeRend;
    }

    public MPoint getTouch(int i) {
        return rendTouch[i];
    }

    public void reset2_increaseCrack() {
        for (int i = 0; i < 2; i++) {
            fragBoundInSrcBm[i].set(0, 0, 0, 0);
            fragBoundInLayout[i].set(0, 0, 0, 0);
            if(fragBm[i] != null) {
                fragBm[i].recycle();
                fragBm[i] = null;
            }
        }
    }

    public void dividePic(Bitmap srcBm) {
        for (int i = 0; i < 2; i++) {
            fragBm[i] = BitmapUtil.digBitmap(srcBm, picPath[i], fragBoundInSrcBm[i]);
        }
    }

    public void cacuFragBoundInLayout(int left, int top) {
        for (int i = 0; i < 2; i++) {
            fragBoundInLayout[i].set(fragBoundInSrcBm[i]); // 在原图中的范围
            fragBoundInLayout[i].scale(scaleRatioBeforeRend); // 缩放
            fragBoundInLayout[i].offset(left, top); // 加上起始位置
        }
    }

    public void swapFragData() {
        Object temp;
        temp = fragBm[0];
        fragBm[0] = fragBm[1];
        fragBm[1] = (Bitmap) temp;

        temp = fragBoundInSrcBm[0];
        fragBoundInSrcBm[0] = fragBoundInSrcBm[1];
        fragBoundInSrcBm[1] = (MRect) temp;

        temp = fragBoundInLayout[0];
        fragBoundInLayout[0] = fragBoundInLayout[1];
        fragBoundInLayout[1] = (MRect) temp;

    }

    public void setStPath(Path stPath) {
        stPathInView = new Path();
        Matrix matrix = new Matrix();
        matrix.setScale(scaleRatioBeforeRend, scaleRatioBeforeRend);
        stPath.transform(matrix, stPathInView);

    }
}