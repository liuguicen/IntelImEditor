package com.mandi.intelimeditor.common.util.geoutil;

import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/03
 *      version : 1.0
 * <pre>
 */
public class MRect extends RectF {
    public MRect(int left, int top, int right, int bottom) {
        super(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
    }

    public MRect() {
        super();
    }

    public MRect(float left, float top, float right, float bottom) {
        super(left, top, right, bottom);
    }

    public MRect(RectF r) {
        this(r.left, r.top, r.right, r.bottom);
    }

    public MRect(Rect r) {
        super(r);
    }

    public MRect(View v) {
        super(0, 0, v.getWidth(), v.getHeight());
    }

    public void offset(MPoint p) {
        offset(p.x, p.y);
    }


    public int widthInt() {
        return Math.round(width());
    }

    public int heightInt() {
        return Math.round(height());
    }

    public int leftInt() {
        return Math.round(left);
    }

    public int topInt() {
        return Math.round(top);
    }

    public int rightInt() {
        return Math.round(right);
    }

    public int bottomInt() {
        return Math.round(bottom);
    }

    public MPoint leftTop() {
        return new MPoint(left, top);
    }

    public MPoint rightTop() {
        return new MPoint(right, top);
    }

    public MPoint rightBottom() {
        return new MPoint(right, bottom);
    }

    public MPoint leftBottom() {
        return new MPoint(left, bottom);
    }

    public MPoint center() {
        return new MPoint((left + right) / 2, (top + bottom) / 2);
    }


    /**
     * @return 从左上角开始，顺时针方向, 四个点
     */
    public MPoint[] getPoints() {
        MPoint[] pa = new MPoint[]{new MPoint(left, top), new MPoint(right, top),
                new MPoint(right, bottom), new MPoint(left, bottom)};
        return pa;
    }

    /**
     * 将矩形约束到给定的矩形范围内，如果超过范围则收缩它
     * 这里多了，其实系统提供了API，intersect交集，先不改
     */
    public void shrinkInB(float left, float top, float right, float bottom) {
        if (left < right && top < bottom) {
            if (this.left < left) this.left = left;
            if (this.top < top) this.top = top;
            if (this.right > right) this.right = right;
            if (this.bottom > bottom) this.bottom = bottom;
        }
    }

    public void shrinkInB(MRect b) {
        shrinkInB(b.left, b.top, b.right, b.bottom);
    }

    /**
     * 坐标系下缩放，即每个坐标缩放，整体大小也缩放
     *
     * @return
     */
    public MRect scale_(float scaleRatio) {
        return scale_((double) scaleRatio);
    }

    public MRect scale_(double scaleRatio) {
        left *= scaleRatio;
        top *= scaleRatio;
        right *= scaleRatio;
        bottom *= scaleRatio;
        return this;
    }

    /**
     * A不能完全在B之外，即AB有相交的部分，否则移动A
     *
     * @return 返回A移动的距离
     */
    public MPoint moveANotOutOfB(MRect b) {
        float dx = 0, dy = 0;
        if (right < b.left) { // 超出左边界
            dx = b.left - right;
        } else if (left > b.right) { // 超出右边界，左右不会同时超出
            dx = b.right - left;
        }
        if (bottom < b.top) { // 超出上边界
            dy = b.top - bottom;
        } else if (top > b.bottom) {
            dy = b.bottom - top;
        }
        offset(dx, dy);
        return new MPoint(dx, dy);
    }

    public MRect sub(int x, int y) {
        MRect newRect = new MRect(this);
        newRect.offset(-x, -y);
        return newRect;
    }

    public MRect sub(float x, float y) {
        MRect newRect = new MRect(this);
        newRect.offset(-x, -y);
        return newRect;
    }

    public MRect add_(float x, float y) {
        left += x;
        right += x;
        top += y;
        bottom += y;
        return this;
    }

    /**
     * 按照尺寸，让a的保持长宽比的缩放到B的大小以内, 返回需要缩放的比例
     */
    public float getScaleRatioOf_ANotBiggerThanB(MRect b) {
        float aw = width(), ah = height();
        float bw = b.width(), bh = b.height();
        float ratio = 1; // ratio肯定不会大于1，a小是1，a小则小于于1
        if (aw > bw) {
            ratio = min(ratio, bw / aw);
        }
        if (ah > bh) {  // 返回长宽缩放的比例中
            ratio = min(ratio, bh / ah);
        }
        return ratio;
    }

    public boolean contains(MPoint p) {
        return contains(p.x, p.y);
    }

    public float dis2center(MPoint p) {
        return GeoUtil.getDis(p.x, p.y, centerX(), centerY());
    }

    /**
     * 与边界的距离, 如果在内部返回负的距离，外部非边角返回正的距离，外部边角返回两个值
     * 画个图，用if else 划分区域，然后一个个返回距离，处理过的划掉，直到9个区域处理完
     * 返回列表，用以区分是否处于角上
     */
    public List<Float> disEdge(float x, float y) { // 距离边界的距离
        ArrayList<Float> dis = new ArrayList<>();
        if (x >= left) { // 左边界的右边
            if (x <= right) { // 右边界的左边
                if (y < top) { // 上边界上边
                    dis.add(top - y);
                } else if (y > bottom) { // 下边界下边
                    dis.add(y - bottom);
                } else { // 上下边界的中间, 矩形中间，比较四条边距离
                    dis.add(-Math.min(
                            Math.min(Math.abs(left - x), Math.abs(right - x)),
                            Math.min(Math.abs(top - y), Math.abs(bottom - y))));
                }
            } else { // 右边界的右边
                if (y < top) {
                    dis.add(x - right);
                    dis.add(top - y);
                } else if (y > bottom) {
                    dis.add(x - right);
                    dis.add(y - bottom);
                } else {
                    dis.add(x - right);
                }
            }
        } else { // 左边界的左边
            if (y < top) {
                dis.add(left - x);
                dis.add(top - y);
            } else if (y > bottom) {
                dis.add(left - x);
                dis.add(y - bottom);
            } else {
                dis.add(left - x);
            }
        }
        return dis;
    }


    /**
     * @param d 四条边都向外扩张d的距离
     */
    public void expand(double d) {
        left -= d;
        top -= d;
        right += d;
        bottom += d;
    }

    public Rect toRect() {
        return new Rect(Math.round(left), Math.round(top), Math.round(right), Math.round(bottom));
    }

    public MRect offset_(int x, int y) {
        return add_(x, y);
    }

    public MRect sub_(float x, float y) {
        return add_(-x, -y);
    }
}
