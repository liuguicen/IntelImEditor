package com.mandi.intelimeditor.ptu.rendpic;

import android.graphics.RectF;

import androidx.annotation.NonNull;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.Line;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/24
 *      version : 1.0
 * <pre>
 */
public class SawtoothPathGenerator {
    private static final boolean isInTest = false;

    /**
     * 生成撕图的路径，两个点向两边拉扯图片，中间形成一条锯齿状的裂缝，这条裂缝结合图片的四角即生成的路径。
     * <P>生成的路径公有两条
     * @param rect   图片四周的Rect
     * @param touch1 拉扯图片的两个点，第一个点
     * @param touch2 拉扯图片的第二个点
     * @param scaleRatio 原图到View被缩放的比例,锯齿尺寸的计算需要用到
     * @param fragMinWidth 碎片最小宽度
     */
    public static SawtoothPathData generateRendPath(MRect rect, MPoint touch1, MPoint touch2,
                                                    float scaleRatio, double fragMinWidth) throws CacuCrackFail {

        float x1 = touch1.x, y1 = touch1.y, x2 = touch2.x, y2 = touch2.y;

        Line touchLine = new Line(x1, y1, x2, y2);

        // 中垂线方程
        Line vl = new Line();
        vl.a = touchLine.b;
        vl.b = -touchLine.a;
        vl.c = 0 - (vl.a * (x1 + x2) / 2 + vl.b * (y1 + y2) / 2);

        if (isInTest) {
            LogUtil.e("矩形是：" + rect);
            LogUtil.e("触摸点 P1 = " + touch1 + "  触摸点 P2 = " + touch2);
            LogUtil.e("连线方程式是:  " + touchLine.a + " * x + " + touchLine.b + " * y + " + touchLine.c + " = 0 ");
            LogUtil.e("中垂线方程式是:  " + vl.a + " * x + " + vl.b + " * y + " + vl.c + " = 0 ");
        }

        List<MPoint> rectP_of1 = new ArrayList<>(), rectP_of2 = new ArrayList<>();
        MPoint interaction_a = new MPoint(), interaction_b = new MPoint();
        int result = interact_lineWithRect(vl, rect, interaction_a, interaction_b, rectP_of1, rectP_of2, fragMinWidth);  // 生成了交点
        if (isInTest) {
            LogUtil.d("return value = " + result + (result == -1 ? " : no intersection" : "has intersection"));
        }
        if (result == -1) {
            throw new CacuCrackFail("没有交点");
        }
        SawtoothPathData data = new SawtoothPathData();
        if (isInTest) {
            LogUtil.d("交点1( " + interaction_a.x + " , " + interaction_a.y + " ) ");
            LogUtil.d("交点2( " + interaction_b.x + " , " + interaction_b.y + " ) ");
            data.testPoints.add(interaction_a);
            data.testPoints.add(interaction_b);
        }
        SawtoothShapeController config = SawtoothShapeController.nextStyleSawtooth(interaction_a, interaction_b, fragMinWidth);

        // 从交点开始生成锯齿
        generateSawtoothPoint(rect, interaction_a, interaction_b, data.stPoints, config);

        // 按顺时针加入所有点
        data.points2.addAll(rectP_of2);
        data.points2.addAll(data.stPoints);
        data.points2.add(rectP_of2.get(0)); // 闭合

        data.points1.addAll(rectP_of1);
        Collections.reverse(data.stPoints);
        data.points1.addAll(data.stPoints);
        data.points1.add(rectP_of1.get(0));

        // 让分块序号和touch点的序号对应上，
        // 分解线为中垂线，所以矩形分块上任取一点，改点距离哪个touch点近，这块就和该touch点绑定
        if (GeoUtil.getDis(rectP_of1.get(0), touch1) > GeoUtil.getDis(rectP_of2.get(0), touch1)) {
            List<MPoint> temp = data.points1; // 最初学C用的交换代码，今天实际项目中终于用了一次了，哈哈
            data.points1 = data.points2;
            data.points2 = temp;
        }
        if (isInTest) {
            visualizeResult(rect, data.points1, data.points2);
        }
        return data;
    }


    /**
     * 生成锯齿路径,
     * 包括边界点
     */
    private static void generateSawtoothPoint(RectF rect, MPoint pa, MPoint pb, List<MPoint> path1, SawtoothShapeController sawConfig) {
        // 基础数值
        double dx = pb.x - pa.x, dy = pb.y - pa.y;
        double alongLine_dis = sqrt(dx * dx + dy * dy); // 绝对值的形式

        double angle = atan2(dy, dx);
        double alongLine_sina = sin(angle);  // 三角函数符号可以控制增加x，y的方向
        double alongLine_cosa = cos(angle);
        double vertical_sina = sin(angle + PI / 2);
        double vertical_cosa = cos(angle + PI / 2);

        Random random = new Random(System.currentTimeMillis());

        // 开始生成
        double alongLine_add = 0;
        float resultx = pa.x, resulty = pa.y;
        while (alongLine_add <= alongLine_dis) {
            path1.add(new MPoint(resultx, resulty));

            double sawWidth = sawConfig.next_aloneLine_Width();
            alongLine_add += sawWidth;  // 移动总长 + 一步移动距离

            // 沿着连线的位置计算出来
            double alongLine_x = pa.x + alongLine_add * alongLine_cosa;
            double alongLine_y = pa.y + alongLine_add * alongLine_sina;

            // 计算垂直于连线的位移
            double vdis = sawConfig.next_Vertical_height(sawWidth);
            double vdx = vdis * vertical_cosa, vdy = vdis * vertical_sina;

            // 判断边界
            if (alongLine_x + vdx < rect.left) {
                vdx = (rect.left - alongLine_x) * random.nextDouble();
            }
            if (alongLine_x + vdx > rect.right) {
                vdx = (rect.right - alongLine_x) * random.nextDouble();
            }
            if (alongLine_y + vdy < rect.top) {
                vdy = (rect.top - alongLine_y) * random.nextDouble();
            }
            if (alongLine_y + vdy > rect.bottom) {
                vdy = (rect.bottom - alongLine_y) * random.nextDouble();
            }

            // 最终结果，加入path
            resultx = (float) (alongLine_x + vdx);
            resulty = (float) (alongLine_y + vdy);
        }
        path1.add(new MPoint(pb.x, pb.y));
    }

    /**
     * 直线 ax + by + c = 0;
     * 计算的顺序是从左上角和顶边开始顺时针计算，结果也是顺时针方向存储
     *
     * @param l    直线
     * @param rect 矩形
     * @param pa   结果第一个点
     * @param pb   结果的第二个点
     * @param pOf1 顺时针计算交点过程中，矩形上属于第一分块的点
     * @param pOf2 属于第二分块的点
     * @param fragMainWidth
     * @return -1 不相交
     * 1 相交，返回交点
     */
    public static int interact_lineWithRect(Line l, MRect rect, @NonNull MPoint pa, @NonNull MPoint pb, List<MPoint> pOf1, List<MPoint> pOf2,
                                            double fragMainWidth) {
        MPoint[] rp = rect.getPoints();
        float top = rect.top, left = rect.left, bottom = rect.bottom, right = rect.right;
        int count = 0, start = 0, end = 0;

        // 四条边顺时针依次计算，是否相交,相交则得出交点
        // 竖直或者水平的直线，在两个交点外的交点取址为无穷大，超过边界，不会计入交点，也能成立

        // 比较需要注意的一点是，如果直线与矩形相交在矩形的角上，会出现两个交点，这里用 <= 排除，
        // 但是浮点数的=比较准确性不可靠，是否会出现问题？

        // 计算交点位置和序号
        // 顶边
        MPoint[] ips = {pa, pb};
        double x = l.getX(top);
        if (x >= left && x < right) {
            ips[count].set((float) x, top);
            count++;
            start = 0;
        }

        //右边
        double y = l.getY(right);
        if (top <= y && y < bottom) {
            ips[count].set(right, (float) y);
            count++;
            if (count == 1) {
                start = 1;
            } else if (count == 2) {
                end = 1;
            }
        }

        // 底边
        x = l.getX(bottom);
        if (x > left && x <= right && count < 2) {
            ips[count].set((float) x, bottom);
            count++;
            if (count == 1) {
                start = 2;
            } else if (count == 2) {
                end = 2;
            }
        }

        //左边
        y = l.getY(left);
        if (top < y && y <= bottom && count < 2) {
            ips[count].set(left, (float) y);
            count++;
            if (count == 1) {
                start = 3;
            } else if (count == 2) {
                end = 3;
            }
        }

        if (count >= 2) {
            // 加入第一条路径的
            double maxDis = 0;
            if (isInTest) LogUtil.d("矩形上属于第一条路径的点是：");
            for (int i = start + 1; i <= end; i++) {
                pOf1.add(rp[i]);
                maxDis = Math.max(maxDis, l.cacuDis(rp[i].x, rp[i].y));
                if (isInTest) LogUtil.d("( " + rp[i].x + " , " + rp[i].y + " )");
            }

            if (maxDis < fragMainWidth) {
                return -1; // 距离边界过短，不能进行撕图
            }

            // 加入第二条路径的
            if (isInTest) LogUtil.d("矩形上属于第二条路径的点是：");
            maxDis = 0;
            for (int i = end + 1; i <= 3; i++) {
                pOf2.add(rp[i]);
                maxDis = Math.max(maxDis, l.cacuDis(rp[i].x, rp[i].y));
                if (isInTest) LogUtil.d(rp[i].toString());
            }
            for (int i = 0; i <= start; i++) {
                pOf2.add(rp[i]);
                maxDis = Math.max(maxDis, l.cacuDis(rp[i].x, rp[i].y));
                if (isInTest) LogUtil.d(rp[i].toString());
            }
            if (maxDis < fragMainWidth) {
                return -1;
            }
            return 1;
        }
        return -1;
    }

    /**
     * 结果可视化
     */
    private static void visualizeResult(RectF rect, List<MPoint> path1, List<MPoint> path2) {
        int m = (int) rect.bottom + 1, n = (int) rect.right + 1;
        LogUtil.d("\n最终路径第一条：");
        StringBuilder msg = new StringBuilder();
        for (com.mandi.intelimeditor.common.util.geoutil.MPoint MPoint : path1) {
            msg.append(MPoint).append(" , ");
        }

        LogUtil.d(msg.toString() + "\n\n\n最终路径第二条：");
        msg = new StringBuilder();

        for (com.mandi.intelimeditor.common.util.geoutil.MPoint MPoint : path2) {
            msg.append(MPoint).append(" , ");
        }
        LogUtil.d(msg.toString());

        int[][] ca = new int[m][n];
        for (int i = 0; i < ca.length; i++) {
            for (int i1 = 0; i1 < ca[i].length; i1++) {
                ca[i][i1] = 0;
            }
        }
        for (int i = 0; i < n; i++) {
            ca[(int) rect.top][i] = 1;
            ca[(int) rect.bottom][i] = 1;
        }
        for (int j = 0; j < m; j++) {
            ca[j][(int) rect.left] = ca[j][(int) rect.right] = 1;
        }
        for (MPoint p : path1) {
            ca[(int) p.y][(int) p.x] = 1;
        }
        for (MPoint p : path2) {
            ca[(int) p.y][(int) p.x] = 1;
        }
        /**
         if(isInTest)Logcat.d("矩形可视化：");
         for (int i = 0; i < ca.length; i++) {
         for (int i1 = 0; i1 < ca[i].length; i1++) {
         if(isInTest)Logcat.d(ca[i][i1] == 1 ? "+" : " ");
         }
         if(isInTest)Logcat.d();
         }
         */
    }

}
