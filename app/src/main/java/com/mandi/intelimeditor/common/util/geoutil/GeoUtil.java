package com.mandi.intelimeditor.common.util.geoutil;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.common.util.MU;

import java.util.List;

import static java.lang.Math.cos;
import static java.lang.StrictMath.sin;

/**
 * 几何操作的工具
 * Created by Administrator on 2016/6/1.
 */
public class GeoUtil {
    public static final double EPS = 0.00001;

    public static float getDis(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1, dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float getDis(MPoint a, MPoint b) {
        return getDis(a.x, a.y, b.x, b.y);
    }

    public static double getAnglePI(MPoint a, MPoint b) {
        float mdx = a.x - b.x,
                mdy = a.y - b.y;
        return Math.atan2(mdy, mdx);
    }

    /**
     * 获取线段向量的角度,非弧度PI制，
     */
    public static float getAngleDegree(float x, float y, float x1, float y1) {
        float dx = x1 - x, dy = y1 - y;
        double angle = Math.atan2(dy, dx);
        return (float) Math.toDegrees(angle);
    }

    public static double getAnglePI(float x, float y, float x1, float y1) {
        float dx = x1 - x, dy = y1 - y;
        return Math.atan2(dy, dx);
    }


    /**
     * 利用matrix进行缩放
     *
     * @param scalingX     被缩放的x
     * @param scalingY     被缩放的y
     * @param scaleCenterX 缩放中心x
     * @param scaleCenterY 缩放中心y
     * @param scale        缩放倍数
     * @return 缩放后的x坐标
     */
    public static float getScaledX(float scalingX, float scalingY, float scaleCenterX, float scaleCenterY, float scale) {
        Matrix matrix = new Matrix();
        // 将Matrix移到到当前圆所在的位置，
        // 然后再以某个点为中心进行缩放
        matrix.preTranslate(scalingX, scalingY);
        matrix.postScale(scale, scale, scaleCenterX, scaleCenterY);
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MTRANS_X];
    }

    /**
     * 利用matrix进行缩放
     *
     * @param scalingX     被缩放的x
     * @param scalingY     被缩放的y
     * @param scaleCenterX 缩放中心x
     * @param scaleCenterY 缩放中心y
     * @param scale        缩放倍数
     * @return 缩放后的Y坐标
     */
    public static float getScaledY(float scalingX, float scalingY, float scaleCenterX, float scaleCenterY, float scale) {
        Matrix matrix = new Matrix();
        // 将Matrix移到到当前圆所在的位置，
        // 然后再以某个点为中心进行缩放
        matrix.preTranslate(scalingX, scalingY);
        matrix.postScale(scale, scale, scaleCenterX, scaleCenterY);
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MTRANS_Y];
    }

    public static Rect rectF2Rect(RectF rf) {
        return new Rect(Math.round(rf.left), Math.round(rf.top), Math.round(rf.right), Math.round(rf.bottom));
    }

    /**
     * 高精度计算缩放后的坐标
     *
     * @param xy    存放缩放后的坐标
     * @param cx    缩放中心的x坐标
     * @param cy    缩放中心的y坐标
     * @param bx    被缩放点的x坐标
     * @param by    被缩放点的x坐标
     * @param ratio 缩放比例
     */
    public static void getScaledCoord(float[] xy, float cx, float cy, float bx, float by, float ratio) {
        //中心的坐标
        xy[0] = Float.valueOf(MU.su(cx, MU.mu(MU.su(cx, String.valueOf(bx)), ratio)));
        xy[1] = Float.valueOf(MU.su(cy, MU.mu(MU.su(cy, String.valueOf(by)), ratio)));
    }

    /**
     * 顺时针，从左上角到左下角
     */
    public static MPoint[] getViewCornerPoint(View v) {
        return new MPoint[]{new MPoint(v.getLeft(), v.getTop()), new MPoint(v.getRight(), v.getTop()),
                new MPoint(v.getRight(), v.getBottom()), new MPoint(v.getLeft(), v.getBottom())};
    }

    public final float dots(MPoint a, MPoint b) {
        return 0;
    }

    /**
     * 坐标旋转公式，目前使用Android提供的Matrix可以完成，更为方便
     * x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
     * y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;
     *
     * @param center 旋转中心点
     * @param a      弧度，顺时针为正
     */
    public static MPoint rotatePoint(MPoint p, MPoint center, double a) {
        MPoint p2 = new MPoint();
        float dx = p.x - center.x;
        float dy = p.y - center.y;
        p2.x = (float) (dx * cos(a) - dy * sin(a) + center.x);
        p2.y = (float) (dx * sin(a) + dy * cos(a) + center.y);
        return p2;
    }

    /**
     * 直线 y = ax + b;
     * 竖直的情况, a = Float.MAX_VALUE代表x的值，即直线方程x = b;
     *
     * @return -1 不相交
     * 1 相交，返回交点
     */
    public static int interact_lineWithRect(float a, float b, @NonNull MPoint ip1, @NonNull MPoint ip2, RectF rect) {
        float top = rect.top, left = rect.left, bottom = rect.bottom, right = rect.right;
        if (Math.abs(a - Float.MAX_VALUE) < 0.001) {  // 竖直的线
            if (b <= left || b >= right) return -1;
            else {
                ip1.x = ip2.x = b;
                ip1.y = top;
                ip2.y = bottom;
            }
        } else if (Math.abs(a - 0) < 0.001) {  // 水平的线
            if (b <= top || b >= bottom) return -1;
            else {
                ip1.x = left;
                ip2.x = right;
                ip1.y = ip2.y = b;
            }
        }
        // 其它情况，四条边依次计算，是否相交,相交则得出交点
        MPoint[] ips = {ip1, ip2};
        int count = 0;
        float x = (top - b) / a;
        if (x > left && x < right) { // 顶上的边
            ips[count].x = x;
            ips[count].y = top;
            count++;
        }
        float y = right * a + b;  //右边的边
        if (top < y && y < bottom) {
            ips[count].x = right;
            ips[count].y = y;
            count++;
        }
        x = (bottom - b) / a; // 下面的边
        if (x > left && x < right && count < 2) {
            ips[count].x = x;
            ips[count].y = bottom;
            count++;
        }
        y = left * a + b;  //左边的边
        if (top < y && y < bottom && count < 2) {
            ips[count].x = left;
            ips[count].y = y;
            count++;
        }
        if (count >= 2) {
            return 1;
        }
        return -1;
    }

    /**
     * 根据用户输入的点进行线性拟合，得出用户施力方向
     */
    public static Line lineFitting(List<MPoint> dateList) {
        if (dateList == null) return null;
        double XSum = 0, YSum = 0, X2Sum = 0, XYSum = 0;
        Line l;
        int n = dateList.size();
        for (MPoint p : dateList) {
            XSum += p.x;
            YSum += p.y;
            X2Sum += p.x * p.x;
            XYSum += p.x * p.y;
        }

        double fm = (XSum * XSum / n - X2Sum);
        double k;
        if (Math.abs(fm) < EPS) { // 竖直的线,直接x - c = 0，c取x的均值
            l = new Line(1, 0, -XSum / n);
        } else {
            k = (XSum * YSum / n - XYSum) / fm;
            double b = (YSum - k * XSum) / n;
            // 斜截式化为一般式
            l = new Line(k, -1, b);
        }
        return l;
    }

    /**
     * 角度转弧度
     */
    public static double degree2Pi(double degree) {
        return degree / 180 * Math.PI;
    }

    /**
     * 弧度转角度
     */
    public static double pi2Degree(double r) {
        return r / Math.PI * 180;
    }

    /**
     * 调整长宽，使它们都不超过最大的长宽
     * @return 是否进行了调整，也即长宽是否超过最大长宽
     */
    public static boolean adjustWhIn(int[] wh, int maxW, int maxH) {
        if (wh[0] <= maxW && wh[1] <= maxH) return false;
        // 取比例较小的，也即缩小程度最大的
        float ratio = Math.min(maxW * 1f / wh[0], maxH * 1f / wh[1]);
        wh[0] *= ratio;
        wh[1] *= ratio;
        return true;
    }

}
