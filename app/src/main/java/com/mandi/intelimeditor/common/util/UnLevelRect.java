package com.mandi.intelimeditor.common.util;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;

/**
 * 非水平的矩形,可以为斜的的那种矩形，注意构造时必须按顺时针或逆时针放入四个点
 * 这应该是个失败的方案
 * 一般是处理矩形和点的关系，这时候不要旋转矩形，矩形旋转之后很难处理，
 * 只需要反向的旋转点就行了，因为矩形r绕o旋转x度，他与点相对位置变化等价于点A绕O反向旋转x度
 * Created by Administrator on 2016/5/31.
 */
public class UnLevelRect {
    float x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0, x4 = 0, y4 = 0;

    /**
     * 注意构造时必须按顺时针或逆时针放入四个点
     */
    public UnLevelRect(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        this.x1 = x1;
        this.y1 = y1;

        this.y2 = y2;
        this.x2 = x2;

        this.x3 = x3;
        this.y3 = y3;

        this.x4 = x4;
        this.y4 = y4;
    }

    /**
     * 水平的矩形，使用左上角和右下角设置,注意构造时必须按顺时针或逆时针放入四个点
     */
    public UnLevelRect(float x1, float y1, float x3, float y3) {
        this.x1 = x1;
        this.y1 = y1;
        this.x3 = x3;
        this.y3 = y3;
        x2 = x3;
        y2 = y1;
        x4 = x1;
        y4 = y3;
    }

    public UnLevelRect() {

    }

    /**
     * 水平的矩形，使用左上角和右下角设置,注意构造时必须按顺时针或逆时针放入四个点
     */
    public UnLevelRect(MPoint p1, MPoint p2, MPoint p3, MPoint p4) {
        this(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }

    public UnLevelRect(UnLevelRect t) {
        x1 = t.x1;
        y1 = t.y1;
        x2 = t.x2;
        y2 = t.y2;
        x3 = t.x3;
        y3 = t.y3;
        x4 = t.x4;
        y4 = t.y4;
    }

    /**
     * 矩形绕中心旋转一定的角度形成的非水平矩形
     *
     * @param angle 弧度制旋转的角度
     */
    public static UnLevelRect getUnlevelRectByRotate(MRect r, double angle) {
        MPoint co = r.center();
        MPoint p = r.leftTop();
        GeoUtil.rotatePoint(p, co, angle);
        MPoint p1 = r.rightTop();
        GeoUtil.rotatePoint(p1, co, angle);
        MPoint p2 = r.rightBottom();
        GeoUtil.rotatePoint(p2, co, angle);
        MPoint p3 = r.leftBottom();
        GeoUtil.rotatePoint(p3, co, angle);
        return new UnLevelRect(p, p1, p2, p3);
    }

    public void set(MPoint p1, MPoint p2, MPoint p3, MPoint p4) {
        set(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
    }

    public float getLeft() {
        return Math.min(Math.min(x1, x2), Math.min(x3, x4));
    }

    public float getRight() {
        return Math.max(Math.max(x1, x2), Math.max(x3, x4));
    }

    public float getTop() {
        return Math.min(Math.min(y1, y2), Math.min(y3, y4));
    }

    public float getButtom() {
        return Math.max(Math.max(y1, y2), Math.max(y3, y4));
    }

    public float getCenterX() {
        return (x1 + x3) / 2; // 矩形对角线中点即中心点
    }

    public float getCenterY() {
        return (y1 + y3) / 2;  // 矩形对角线中点即中心点
    }

    public MPoint getCenterPoint() {
        return new MPoint(getCenterX(), getCenterY());
    }

    public void set(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        this.x1 = x1;
        this.y2 = y2;
        this.x2 = x2;
        this.y1 = y1;
        this.x3 = x3;
        this.y4 = y4;
        this.x4 = x4;
        this.y3 = y3;
    }

    public void set(float[] p) {
        if (p.length != 8) {
            throw new IllegalArgumentException("the number of coordinate must be 8");
        }
        this.x1 = p[0];
        this.y2 = p[1];
        this.x2 = p[2];
        this.y1 = p[3];
        this.x3 = p[4];
        this.y4 = p[5];
        this.x4 = p[6];
        this.y3 = p[7];
    }

    public boolean contain(float x, float y) {
        if (isLeft(x1, y1, x2, y2, x, y) * isLeft(x1, y1, x2, y2, x3, y3) >= 0
                && isLeft(x2, y2, x3, y3, x, y) * isLeft(x2, y2, x3, y3, x4, y4) >= 0
                && isLeft(x3, y3, x4, y4, x, y) * isLeft(x3, y3, x4, y4, x1, y1) >= 0
                && isLeft(x4, y4, x1, y1, x, y) * isLeft(x4, y4, x1, y1, x2, y2) >= 0)
            return true;
        return false;
    }

    private float isLeft(float x1, float y1, float x2, float y2, float x3, float y3) {
        float x_1 = x2 - x1, y_1 = y2 - y1, x_2 = x3 - x1, y_2 = y3 - y1;
        return x_1 * y_2 - x_2 * y_1;
    }

    public void translate(float dx, float dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
        x3 += dx;
        y3 += dy;
        x4 += dx;
        y4 += dy;
    }

    /**
     * @param angle 弧度制角度
     */
    public void rotate(double angle) {
        rotate(getCenterPoint(), angle);
    }

    /**
     * @param angle 弧度制角度
     */
    private void rotate(MPoint centerPoint, double angle) {
        Matrix matrix = new Matrix();
        matrix.setRotate((float) (angle * 180), centerPoint.x, centerPoint.y);
        float[] pts = {x1, y1, x2, y2, x3, y3, x4, y4};
        matrix.mapPoints(pts);
        set(pts);
    }

    public MPoint[] getPoints() {
        return new MPoint[]{new MPoint(x1, y1), new MPoint(x2, y2), new MPoint(x3, y3), new MPoint(x4, y4)};
    }

    public float[] getCoordinates() {
        return new float[]{x1, y1, x2, y2, x3, y3, x4, y4};
    }


    /**
     * 平移系统的平行的矩形
     *
     * @param rect
     * @param dx
     * @param dy
     */
    public void translateFormRect(RectF rect, float dx, float dy) {
        rect.left += dx;
        rect.top += dy;
        rect.right += dx;
        rect.bottom += dy;
    }

    @androidx.annotation.NonNull
    @Override
    public String toString() {
        return String.format("(%f, %f), (%f, %f), (%f, %f), (%f, %f)", x1, y1, x2, y2, x3, y3, x4, y4);
    }
}
