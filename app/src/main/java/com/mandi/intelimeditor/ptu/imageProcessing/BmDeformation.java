package com.mandi.intelimeditor.ptu.imageProcessing;

import android.util.Log;

import androidx.annotation.Nullable;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;

/**
 * 图片扭曲
 */
public class BmDeformation {
    public static final String TAG = "BmMesh";

    /**
     * @param verts 原来的像素数组
     * @param c     拖动起点，也是圆心
     * @param m     拖动终点
     * @param r     扭曲半径
     *              注意拖动的距离，也就是结束点m和启动c的距离，必须比半径大很多（2倍以上），变形看上去才会平滑, 所以这里按照拖动方向放大end点坐标
     *              不知为何，目前来看应该是是算法本身如此，（不太可能是下面的实现写错了）
     * @return
     */
    @Nullable
    public static void deformation(float[] verts, int bmW, int bmH,
                                   MPoint c, MPoint m, float r) {
        long startTime = System.currentTimeMillis();

        if (LogUtil.debugDeformation) {
            Log.d(TAG, "开始变形，时间=  " + startTime);
            Log.d(TAG, "扭曲起点 " + c + " 扭曲终点 " + m + " 扭曲半径 " + r);
            Log.d(TAG, "扭曲移动距离" + c.sub(m).module());
        }
        for (int i = 0; i < verts.length; i += 2) {
            // 这个点相当于变换后的点，使用逆变换公式，得到这个点变换前的的
            MPoint x = new MPoint(verts[i], verts[i + 1]);
            float x_c_moduleSquare = x.sub(c).moduleSquare();
            if (x_c_moduleSquare > r * r) continue;
            MPoint m_c = m.sub(c);
            float r2_xc2 = r * r - x_c_moduleSquare;
            float k = r2_xc2 / (r2_xc2 + m_c.moduleSquare());
            k *= k;

            MPoint u = x.add(m_c.numMulti(k));
//                    if (LogUtil.debugBmMesh) {
//                        Log.d(TAG, String.format("替换点 %d %d 的为点 %d %d , 值为 %d", i, j, u.xInt(), u.yInt(), verts[u.xInt() + u.yInt() * bmW]));
//                    }
            verts[i] = u.x;
            verts[i + 1] = u.y;
//                    if (LogUtil.debugBmMesh) {
//                        Log.d(TAG, "扭曲前的点 " + u.toPoint() + " 扭曲后的点 " + x.toPoint());
//                    }
            // nothing
        }
        if (LogUtil.debugDeformation) {
            Log.d(TAG, "结束变形，时间 =  " + (System.currentTimeMillis() - startTime));
        }
    }

    /**
     * 双线性插值
     */
    private static int biLinearInsert(int[] pixels, int width, MPoint u, int insertPosition) {
        int x1 = u.xInt(), y1 = u.yInt();
        int x2 = x1 - 1, y2 = y1 - 1;
        // 公式需要推导，可见 https://blog.csdn.net/qq_37577735/article/details/80041586

        int[] res = new int[3];
        int posion = y1 * width + x1;
        float coe = (x2 - u.x) * (y2 - u.y); // 系数
        getRgb(pixels, posion, coe, insertPosition, res);

        posion = y1 * width + x2;
        coe = (u.x - x1) * (y2 - u.y);
        getRgb(pixels, posion, coe, insertPosition, res);

        posion = y2 * width + x1;
        coe = (x2 - u.x) * (u.y - y1);
        getRgb(pixels, posion, coe, insertPosition, res);

        posion = y2 * width + x2;
        coe = (u.x - x1) * (u.y - y1);
        getRgb(pixels, posion, coe, insertPosition, res);
        return 0xff000000 | (res[0] << 16) | (res[1] << 8) | res[2];
    }

    private static void getRgb(int[] pixels, int position, float coe, int insertPosition, int[] res) {
        if (position < 0 || position >= pixels.length)
            position = insertPosition;
        int color = pixels[position];
        res[0] += (int) (((color >> 16) & 0xFF) * coe);
        res[1] += (int) (((color >> 8) & 0xFF) * coe);
        res[2] += (int) (((color & 0xFF)) * coe);
    }
//    #双线性插值法
//    def BilinearInsert(src,ux,uy):
//    w,h,c = src.shape
//    if c == 3:
//    x1=int(ux)
//    x2=x1+1
//    y1=int(uy)
//    y2=y1+1
//
//    part1=src[y1,x1].astype(np.float)*(float(x2)-ux)*(float(y2)-uy)
//    part2=src[y1,x2].astype(np.float)*(ux-float(x1))*(float(y2)-uy)
//    part3=src[y2,x1].astype(np.float) * (float(x2) - ux)*(uy-float(y1))
//    part4 = src[y2,x2].astype(np.float) * (ux-float(x1)) * (uy - float(y1))
//
//    insertValue=part1+part2+part3+part4
//
//        return insertValue.astype(np.int8)

}
