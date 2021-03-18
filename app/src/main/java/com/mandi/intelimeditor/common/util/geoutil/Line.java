package com.mandi.intelimeditor.common.util.geoutil;

import androidx.annotation.NonNull;

/**
 * 直线的一般式，可以表示竖直和水平的线
 * 以及平面上所有的点
 * <P>小经验，这个类也是逐步抽象出来的，其实自己面向对象的思想还是不够好，没有尽早抽象出这个类
 * 这个类的相关方法，比如两点构造直线，也没有立即根据封装和单一职责原则立即封装到这个类里面
 * 单一职责：我只管我的事，其它不能管2、还有一层境界，该我管的一定要管，没管到是不对的
 * </P>
 */
public class Line {
    public static final double EPS = 0.0001;
    public double a;
    public double b;
    public double c;

    public Line() {
        this.a = this.b = this.c = 0;
    }
    public Line(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Line(MPoint a, MPoint b) {
        this(a.x, a.y, b.x, b.y);
    }

    public Line(double x1, double y1, double x2, double y2) {
        this(y2 - y1, x1 - x2, x2 * y1 - x1 * y2);
    }

    /**
     * 知道y，求x
     * @return 水平的线，返回{@link Double#MAX_VALUE}
     */
    public double getX(double y) {
        if (Math.abs(a) < EPS) {
            return Double.MAX_VALUE;
        }
        return - (b * y + c) / a;
    }

    /**
     * 知道x，求y
     * @return 竖直的线，返回{@link Double#MAX_VALUE}
     */
    public double getY(double x) {
        if (Math.abs(b) < EPS)
            return Double.MAX_VALUE;
        return - (a * x + c) / b;
    }

    public double cacuDis(MPoint p) {
        return cacuDis(p.x, p.y);
    }
    /**
     * 直线一般式的，点到直线的距离
     */
    public double cacuDis(double x, double y) {
        return Math.abs((a * x + b * y + c) / Math.sqrt(a * a + b * b));
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("the line is %f * x + %f * y + %f = 0", a, b, c);
    }
}
