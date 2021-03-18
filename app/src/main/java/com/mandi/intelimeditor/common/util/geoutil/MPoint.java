package com.mandi.intelimeditor.common.util.geoutil;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.lang.Math.cos;
import static java.lang.StrictMath.round;
import static java.lang.StrictMath.sin;

/**
 * 可以当向量使用
 * 方法 xx返回新的point，方法xx_修改自身， 返回自身，便于连续运算，类似python
 */
public class MPoint extends PointF {

    public MPoint() {
        x = y = 0;
    }

    public MPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public MPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public MPoint(float[] xy) {
        x = xy[0];
        y = xy[1];
    }

    public MPoint(MPoint p) {
        x = p.x;
        y = p.y;
    }

    public MPoint(Point p) {
        x = p.x;
        y = p.y;
    }

    public MPoint(PointF p) {
        x = p.x;
        y = p.y;
    }

    public void set(float[] f) {
        if (f.length < 2) {
            return;
        }
        set(f[0], f[1]);
    }

    public void offset(Point p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void offset(PointF p) {
        this.x += p.x;
        this.y += p.y;
    }

    public void offset(MPoint p) {
        this.x += p.x;
        this.y += p.y;
    }

    public int xInt() {
        return round(x);
    }

    public int yInt() {
        return round(y);
    }

    /**
     * 按当前坐标缩放
     *
     * @param ratio
     */
    public void scale(float ratio) {
        x *= ratio;
        y *= ratio;
    }

    /**
     * 数乘
     *
     * @param k
     * @return
     */
    public MPoint numMulti(float k) {
        return new MPoint(x * k, y * k);
    }

    public MPoint numMulti_(float k) {
        x *= k;
        y *= k;
        return this;
    }

    public float module() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * @return 模平方
     */
    public float moduleSquare() {
        return x * x + y * y;
    }

    public Point toPoint() {
        return new Point(Math.round(x), Math.round(y));
    }

    public PointF toPointF() {
        return new PointF(x, y);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("( %f, %f )", x, y);
    }

    public MPoint sub(MPoint b) {
        return new MPoint(x - b.x, y - b.y);
    }

    public MPoint sub(float bx, float by) {
        return new MPoint(x - bx, y - by);
    }

    public MPoint sub_(MPoint b) {
        x -= b.x;
        y -= b.y;
        return this;
    }

    public MPoint add(MPoint b) {
        return new MPoint(x + b.x, y + b.y);
    }

    public MPoint add_(MPoint b) {
        x += b.x;
        y += b.y;
        return this;
    }

    public MPoint add_(float bx, float by) {
        x += bx;
        y += by;
        return this;
    }

    public double angle() {
        return Math.atan2(y, x);
    }

    public MPoint copy() {
        return new MPoint(x, y);
    }

    public void reset() {
        x = y = 0;
    }

    public float[] toArray() {
        return new float[]{x, y};
    }



    public MPoint rotate_radius_(double a) {
        return rotate_radius(null, a);
    }

    public MPoint rotate_radius_(@Nullable MPoint center, double a) {
        if (center == null) center = new MPoint();
        return rotate_radius_(center.x, center.y, a);
    }

    public MPoint rotate_degree_(double a) {
        return rotate_radius(null, Math.toRadians(a));
    }

    public MPoint rotate_degree_(@Nullable MPoint center, double a) {
        if (center == null) center = new MPoint();
        a= Math.toRadians(a);
        return rotate_radius_(center.x, center.y, a);
    }

    /**
     * @param a 注意是弧度
     */
    public MPoint rotate_radius_(float ox, float oy, double a) {
        if (Math.abs(a) > Math.PI) {
            Log.e("---", "rotate_: " + "注意传入弧度!!");
        }
        float dx = x - ox;
        float dy = y - oy;
        x = (float) (dx * cos(a) - dy * sin(a) + ox);
        y = (float) (dx * sin(a) + dy * cos(a) + oy);
        return this;
    }

    public MPoint rotate_radius(double a) {
        return rotate_radius(null, a);
    }

    /**
     * 坐标旋转公式，目前使用Android提供的Matrix可以完成，更为方便
     * x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
     * y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;
     *
     * @param center 旋转中心点,空则原点
     * @param a      弧度，顺时针为正
     */
    public MPoint rotate_radius(@Nullable MPoint center, double a) {
        if (Math.abs(a) > Math.PI) {
            Log.e("---", "rotate_: " + "注意传入弧度!!");
        }
        if (center == null) center = new MPoint();

        MPoint p2 = new MPoint();
        float dx = x - center.x;
        float dy = y - center.y;
        p2.x = (float) (dx * cos(a) - dy * sin(a) + center.x);
        p2.y = (float) (dx * sin(a) + dy * cos(a) + center.y);
        return p2;
    }

}
