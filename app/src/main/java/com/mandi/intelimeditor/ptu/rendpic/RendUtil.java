package com.mandi.intelimeditor.ptu.rendpic;

import android.graphics.PointF;

import com.mandi.intelimeditor.common.util.geoutil.Line;

import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/25
 *      version : 1.0
 *
 * <pre>
 */
public class RendUtil {
    public static final double EPS = 0.0001;

    /**
     * 根据用户输入的点进行线性拟合，得出用户施力方向
     */
    public static Line lineFitting(List<PointF> dateList) {
        if(dateList ==  null) return null;
        double XSum = 0, YSum = 0, X2Sum = 0, XYSum = 0;
        Line l;
        int n = dateList.size();
        for (PointF p: dateList) {
            XSum += p.x;
            YSum += p.y;
            X2Sum += p.x * p.x;
            XYSum += p.x * p.y;
        }

        double fm = (XSum*XSum/n-X2Sum);
        double k;
        if (Math.abs(fm) < EPS) { // 竖直的线,直接x - c = 0，c取x的均值
            l = new Line(1, 0, -XSum / n);
        } else {
            k = (XSum*YSum/n - XYSum) / fm;
            double b = (YSum-k*XSum)/n;
            // 斜截式化为一般式
            l=new Line(k, -1, b);
        }

        return l;
    }
}
