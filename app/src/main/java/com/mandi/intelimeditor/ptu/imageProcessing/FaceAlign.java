package com.mandi.intelimeditor.ptu.imageProcessing;

// TODO: 2020/10/16     

import android.util.Log;

import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.ptu.tietu.FloatImageView;

import java.util.Locale;

/**
 * 目前使用简单的方法对齐，有专门的方法，叫做 人脸姿势 face pose，是3维空间
 * 获取人脸姿势之后根据人脸姿势对齐，主要是进行3d旋转
 */
public class FaceAlign {
    public static final String TAG = "FaceAlign";

    /**
     * @return 返回旋转角a[0] degree, 缩放比例a[1]
     */
    public static float[] align(float[] src, float[] dst) {
        MPoint[] kp_dst = getNeedKp(dst), kep_src = getNeedKp(src);

        // 嘴部和下巴的点比较容易懂，应该不能用来算，用眼睛和鼻尖的点
        // 大概情形就是两个三角形A，B，缩放和旋转B, 让B和A的大小形状最接近
        // a坐标a1x，a1y，a2x,a2y,a3x，a3y，b的类似 b1x,b1y,bx2,b2y...
        // c1=b1x * a1x + b1y * a1y + bx2 * a2x + b2y * a2y + ...
        // c2=b1x * a1y - b1y * a1x + bx2 * a2y - b2y * a2x + ...
        // l=B中三个点距离的平方的和
        // 结果 角度beta= arctan(c2/c1), 缩放比你 k = (c1 * cos(beta) + c1  * sin(beta)) / l
        // 列公式就是旋转缩放b后的坐标，然后计算与A的对应坐标的距离平方和，最小化这个平方和

        // 上面的方式是处理多个点以原点为坐标旋转的，但是这里有所不同，公式白推了！！
        // 鼻尖大概位于人脸的中心，这里先让鼻尖对齐，然后以鼻尖为圆心，进行旋转缩放，可以直接算旋转角和缩放比例从

        // 实测发现，鼻子和眼睛的相对位置不一定固定，所以检测缩放和旋转只用眼睛
        float dis_dst = GeoUtil.getDis(kp_dst[1], kp_dst[2]);
        float dis_src = GeoUtil.getDis(kep_src[1], kep_src[2]);
        float k = dis_dst / dis_src;
        Log.d(TAG, String.format(Locale.CANADA, "双眼距离 A = %f, B = %f， 缩放比例 = %f", dis_dst, dis_src, k));
        MPoint line_dst = kp_dst[2].sub(kp_dst[1]), line_src = kep_src[2].sub(kep_src[1]);
        float angleA = (float) Math.toDegrees(Math.atan2(line_dst.y, line_dst.x));
        float angleB = (float) Math.toDegrees(Math.atan2(line_src.y, line_src.x));
        float beta = angleA - angleB;
        Log.d(TAG, String.format(Locale.CANADA, "双眼角度 A = %f, B = %f， 旋转角度 = %f", angleA, angleB, beta));
        // 鼻尖对齐
//        MPoint mov = kp_dst[0].sub(kep_src[0]);
//        for (int i = 0; i < kp_dst.length; i++) {
//            kep_src[i].add_(mov);
//        }
//        // 更改坐标，以鼻尖为圆心
//        for (int i = kep_src.length - 1; i >= 0; i--) {
//            kep_src[i].sub_(kep_src[0]);
//            kp_dst[i].sub_(kp_dst[0]);
//        }
//
//        float c1 = 0, c2 = 0;
//        for (int i = 0; i < kp_dst.length; i++) {
//            c1 += kep_src[i].x * kp_dst[i].x + kep_src[i].y * kp_dst[i].y;
//            c2 += kep_src[i].x * kp_dst[i].y - kep_src[i].y * kp_dst[i].x;
//        }
//        float beta = (float) Math.toDegrees(Math.atan2(c2, c1));
//        float k = (float) ((c1 * Math.cos(beta) + c2 * Math.sin(beta))
//                / (kep_src[0].moduleSquare() + kep_src[1].moduleSquare() + kep_src[2].moduleSquare()));
//        Log.d(TAG, String.format(Locale.CANADA, "第一种方法：  k = %f beta = %f", k, beta));
//
//        k = GeoUtil.getDis(kp_dst[1], kp_dst[2]) / GeoUtil.getDis(kep_src[1], kep_src[2]);
//        double betaA = Math.toDegrees(Math.atan2(kp_dst[1].y - kp_dst[0].y, kp_dst[1].x - kp_dst[0].x));
//        double betaB = Math.toDegrees(Math.atan2(kep_src[1].y - kep_src[0].y, kep_src[1].x - kep_src[0].x));
//        Log.d(TAG, "kp_dst = " + Arrays.toString(kp_dst));
//        Log.d(TAG, "kep_src = " + Arrays.toString(kep_src));
//        beta = (float) (betaA - betaB);
//        Log.d(TAG, String.format(Locale.CANADA, "第二种方法：第一张角度 %f, 第二张角度： %f 旋转角 %f", betaA, betaB, beta));
//        Log.d(TAG, "dis A = " + GeoUtil.getDis(kp_dst[1], kp_dst[2]) + "dis B = " + GeoUtil.getDis(kep_src[1], kep_src[2]));
//        MPoint[] b1 = testChange(kep_src, 0, k);
//        Log.d(TAG, "缩放后的b = " + Arrays.toString(b1));
//        Log.d(TAG, "dis A = " + GeoUtil.getDis(kp_dst[1], kp_dst[2]) + "dis B = " + GeoUtil.getDis(b1[1], b1[2]));
//        Log.d(TAG, "缩放 k = " + k + " 旋转 beta = " + beta);
//        b1 = testChange(kep_src, -beta, k);
//        Log.d(TAG, "缩放 + 旋转后的b = " + Arrays.toString(b1));
//        Log.d(TAG, "dis A = " + GeoUtil.getDis(kp_dst[1], kp_dst[2]) + " dis B = " + GeoUtil.getDis(b1[1], b1[2]));
//        // 计算旋转
        return new float[]{beta, k};
    }

    /**
     * @return 0 角度 1 缩放比
     */
    public static float[] align(FaceFeature src, FaceFeature dst) {
        return new float[] {dst.angleY - src.angleY, dst.faceWidth / src.faceWidth};
    }

    private static MPoint[] testChange(MPoint[] b, float beta, float k) {
        MPoint[] b1 = new MPoint[b.length];
        for (int i = 0; i < b.length; i++) {
            b1[i] = b[i].rotate_radius(null, beta).numMulti(k);
        }
        return b1;
    }


    public static MPoint[] getNeedKp(float[] landmark) {
        if (landmark == null || landmark.length < FaceFeatureDetector.KP_ID[FaceFeatureDetector.KP_ID.length - 1]) {
            return null;
        }
        return new MPoint[]{FaceFeatureDetector.kp2Point(landmark, FaceFeatureDetector.KP_NOSE),
                FaceFeatureDetector.kp2Point(landmark, FaceFeatureDetector.KP_L_EYE),
                FaceFeatureDetector.kp2Point(landmark, FaceFeatureDetector.KP_R_EYE)};
    }

    public static int[] getNearPoint(float[] kp, FloatImageView fiv) {
        MPoint[] viewCornerPoint = GeoUtil.getViewCornerPoint(fiv);
        int[] nearId = new int[2];
        float minDis = Float.MAX_VALUE;
        for (int vi = 0; vi < 1; vi++) {
            for (int fi = 0; fi < kp.length / 2; fi++) {
                if (viewCornerPoint[vi].sub(kp[fi * 2], kp[fi * 2 + 1]).moduleSquare() < minDis) {
                    nearId[0] = vi;
                    nearId[1] = fi;
                }
            }
        }
        return nearId;
    }
}
