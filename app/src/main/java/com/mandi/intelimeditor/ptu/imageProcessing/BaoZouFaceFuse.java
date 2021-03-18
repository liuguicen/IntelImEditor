package com.mandi.intelimeditor.ptu.imageProcessing;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.Nullable;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.tietu.FloatImageView;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 暴走脸融合，暴走脸融合背景图片的脸上面
 */
public class BaoZouFaceFuse {
    public static final String TAG = "BaoZouFaceFuse";
    /**
     * 最大采样点数，要保证这个数乘以255 <int最值，因为会对像素值求和，一般不会超
     */
    private static int MAX_SIMPLING_NUMBER = 2500;

    /**
     * 融合贴图
     * 需要边缘羽化的半径足够大，如果小的话效果就比较差
     * 编写几何变化相关代码时，一般都要统一坐标系，如原点位置等，不然很可能出错
     * 坐标系相对于外层的frameLayout
     *
     * @param tietuBm 必须保证tietuBm是可更改的，否则什么也不做
     */
    @Nullable
    public static Bitmap fuseTietu(Bitmap tietuBm, FloatImageView fiv, PtuSeeView ptuSeeView, Bitmap baseBm) {
        if (tietuBm == null || tietuBm.isRecycled() || !tietuBm.isMutable()) return null;
        if (LogUtil.debugBaoZouFaceFuse)
            Log.d(TAG, "fuseTietu: 开始时间" + System.currentTimeMillis());
        Rect srcRect = ptuSeeView.getSrcRect(), dstRect = ptuSeeView.getDstRect();
        if (baseBm == null)
            baseBm = ptuSeeView.getSourceBm();
        if (baseBm == null) return null;
        int baseWidth = baseBm.getWidth(), baseHeight = baseBm.getHeight();

        int tietuBmWidth = tietuBm.getWidth(), tietuBmHeight = tietuBm.getHeight();
        float[] positionInTietu = generateSimplingPosition(fiv); // 在视图矩形中生成采样位置
        float[] positionInBase = getPositionInBase(fiv, positionInTietu, srcRect, dstRect); // 旋转采样位置，并获取其在底图中的位置
        // 不能和上面交换顺序
        getPositionInTietu(fiv, positionInTietu);

        int[] tietuPixels = new int[tietuBmWidth * tietuBmHeight];
        tietuBm.getPixels(tietuPixels, 0, tietuBmWidth, 0, 0, tietuBmWidth, tietuBmHeight);

//        int totalRed = 0, totalGreen = 0, totalBlue = 0, pixelNumber = 0;
        List<Integer> simplingPixelInBase = new ArrayList<>();
        List<Integer> simplingGrayInBase = new ArrayList<>();
        for (int i = 0; i < positionInBase.length; i += 2) {
            int xInBase = (int) (positionInBase[i]), yInBase = (int) (positionInBase[i + 1]); // 底图中像素位置
            if (xInBase < 0 || yInBase < 0 || xInBase >= baseWidth || yInBase >= baseHeight) {
//                if (LogUtil.debugBaoZouFaceFuse) {
//                    Log.d(TAG, "fuseTietu: 底图像素超出界限" + xInBase + " , " + yInBase);
//                }
                continue;
            }
            int tx = (int) positionInTietu[i], ty = (int) positionInTietu[i + 1]; // 贴图中像素位置
            if (tx < 0 || tx >= tietuBmWidth || ty < 0 || ty >= tietuBmHeight) {
//                if (LogUtil.debugBaoZouFaceFuse) {
//                    Log.d(TAG, "fuseTietu: 贴图像素超出界限" + tx + " , " + ty);
//                }
                continue;
            }
            int tietuPix = tietuPixels[ty * tietuBmWidth + tx];
            if (tietuPix >>> 24 <= 240) { // 贴图中比较透明的部分的像素不考虑
//                if (LogUtil.debugBaoZouFaceFuse) {
//                    Log.d(TAG, "fuseTietu: 贴图像素透明度高，跳过" + tx + " , " + ty);
//                }
                continue;
            }

            // 获取到底图中的像素值
            int basePixel = baseBm.getPixel(xInBase, yInBase);
            simplingPixelInBase.add(basePixel);

            // 统计像素的情况
            int grayPixel = (int) (((basePixel & 0x00ff0000) >>> 16) * 0.299 + ((basePixel & 0x0000ff00) >>> 8) * 0.587 + (basePixel & 0x000000ff) * 0.114);
            simplingGrayInBase.add(grayPixel); // 比例是根据色彩心理学调配的比例，OpenCV用的也是这个
        }
        Collections.sort(simplingGrayInBase); // 对采样的灰度像素进行排序
        int quartile = simplingGrayInBase.get((int) (simplingGrayInBase.size() / 3.5)); // 2分位点
        int totalRed = 0, totalGreen = 0, totalBlue = 0, pixelNumber = 0;
        for (Integer pixel : simplingPixelInBase) {
            int red = (pixel & 0x00ff0000) >>> 16;
            int green = (pixel & 0x0000ff00) >>> 8;
            int blue = pixel & 0x000000ff;
            int grayPixel = (int) (red * 0.299 + green * 0.587 + blue * 0.114);
            if (grayPixel < quartile) { // 过滤掉颜色比较低的点，针对人脸头发，眼睛灯，很暗的点导致融合效果不好
//                if (LogUtil.debugBaoZouFaceFuse) {
//                    Log.d(TAG, "fuseTietu: 过滤和皮肤颜色相差大的点");
//                }
                continue;
            }
            totalRed += red; // 应该不能合成一个int加，再求均值，因为涉及到进位,正负号表示等
            totalGreen += green;
            totalBlue += blue;
            pixelNumber++;
        }

        if (pixelNumber == 0) return null; // 可能贴图在底图外面
        // 第二步
        // 得出RGB均值
        totalRed /= pixelNumber;
        totalGreen /= pixelNumber;
        totalBlue /= pixelNumber;
        // 均值化之后的背景区域与贴图进行正片叠底
        // 因为采用均值化，所以背景图只需要采用同样的像素操作就行
        // TODO: 2020/5/31 此处为了用户多次融合效果相同，以及撤销重做，但是内存消耗大
        // 使用像素数组的方案
        for (int i = 0; i < tietuPixels.length; i++) {
            int tietuPixel = tietuPixels[i];
            tietuPixels[i] = Multiply(tietuPixel, totalRed, totalGreen, totalBlue);
        }
        Bitmap newTietuBm = Bitmap.createBitmap(tietuPixels, tietuBmWidth, tietuBmHeight, tietuBm.getConfig());

        // 直接使用bm的setPixel的方案，测试，根据图片的不同，得到的效果，使用set，get比数组慢几倍到几十倍的
        if (LogUtil.debugBaoZouFaceFuse)
            Log.d(TAG, "fuseTietu: 结束时间" + System.currentTimeMillis());
        return newTietuBm;
    }

    /* 测试代码，测试直接使用bm的get，set方式的，根据图片的不同，得到的效果，使用set，get比数组慢几倍到几十倍的
     * 融合贴图
     * 需要边缘羽化的半径足够大，如果小的话效果就比较差
     * 编写几何变化相关代码时，一般都要统一坐标系，如原点位置等，不然很可能出错
     * 坐标系相对于外层的frameLayout
     *
     * @param tietuBm 必须保证tietuBm是可更改的，否则什么也不做
     *//*
    @Nullable
    public static Bitmap fuseTietu_2(Bitmap tietuBm, FloatImageView fiv, PtuSeeView ptuSeeView) {
        if (tietuBm == null || tietuBm.isRecycled() || !tietuBm.isMutable()) return null;
        if (LogUtil.debugBaoZouFaceFuse)
            Log.d(TAG, "fuseTietu: 开始时间" + System.currentTimeMillis());
        Rect srcRect = ptuSeeView.getSrcRect(), dstRect = ptuSeeView.getDstRect();
        Bitmap baseBm = ptuSeeView.getSourceBm();
        if (baseBm == null) return null;
        int baseWidth = baseBm.getWidth(), baseHeight = baseBm.getHeight();
        int tietuBmWidth = tietuBm.getWidth(), tietuBmHeight = tietuBm.getHeight();
//        int[] tietuPixels = new int[tietuBmWidth * tietuBmHeight];

//        tietuBm.getPixels(tietuPixels, 0, tietuBmWidth, 0, 0, tietuBmWidth, tietuBmHeight);
//        Matrix matrix = new Matrix();
//        matrix.mapPoints(tietuPixels);
        float[] pointsInTietu = getSimplingPoint(fiv);
        float[] pointsInBase = rotatePoints(fiv, pointsInTietu);
        getLocationInTietuBm(fiv, pointsInTietu);

        int totalRed = 0, totalGreen = 0, totalBlue = 0, pixelNumber = 0;
        for (int i = 0; i < pointsInBase.length; i += 2) {
            float[] locationAtBaseBm = getLocationAtBaseBm(pointsInBase[i], pointsInBase[i + 1], srcRect, dstRect);
            int xInBase = (int) (locationAtBaseBm[0]), yInBase = (int) (locationAtBaseBm[1]); // 底图中像素位置
            if (xInBase < 0 || yInBase < 0 || xInBase >= baseWidth || yInBase >= baseHeight) {
                if (LogUtil.debugBaoZouFaceFuse) {
                    Log.d(TAG, "fuseTietu: 底图像素超出界限" + xInBase + " , " + yInBase);
                }
                continue;
            }
            int tx = (int) pointsInTietu[i], ty = (int) pointsInTietu[i + 1]; // 贴图中像素位置
            if (tx < 0 || tx >= tietuBmWidth || ty < 0 || ty >= tietuBmHeight) {
                if (LogUtil.debugBaoZouFaceFuse) {
                    Log.d(TAG, "fuseTietu: 贴图像素超出界限" + tx + " , " + ty);
                }
                continue;
            }
            int tietuPix = tietuBm.getPixel(tx, ty);
            if (tietuPix >>> 24 <= 240) { // 贴图中比较透明的部分的像素不考虑
                if (LogUtil.debugBaoZouFaceFuse) {
                    Log.d(TAG, "fuseTietu: 贴图像素透明度高，跳过" + tx + " , " + ty);
                }
                continue;
            }
            // 如果该像素在贴图中不是完全透明的话，对其均值化
            int basePixel = baseBm.getPixel(xInBase, yInBase);
            totalRed += (basePixel & 0x00ff0000) >>> 16; // 应该不能合成一个int加，再求均值，因为涉及到进位正负号表示等
            totalGreen += (basePixel & 0x0000ff00) >>> 8;
            totalBlue += basePixel & 0x000000ff;
            pixelNumber++;
        }
        if (pixelNumber == 0) return null; // 可能贴图在底图外面
        // 第二步
        // 得出RGB均值
        totalRed /= pixelNumber;
        totalGreen /= pixelNumber;
        totalBlue /= pixelNumber;
        // 均值化之后的背景区域与贴图进行正片叠底
        // 因为采用均值化，所以背景图只需要采用同样的像素操作就行
        // TODO: 2020/5/31 此处为了用户多次融合效果相同，以及撤销重做，但是内存消耗大
        Bitmap newTietuBm = Bitmap.createBitmap(tietuBmWidth, tietuBmHeight, tietuBm.getConfig());
        for (int i = 0; i < tietuBmWidth; i++) {
            for (int j = 0; j < tietuBmHeight; j++) {
                // TODO: 2020/5/31 效率低，待优化
                newTietuBm.setPixel(i, j, Multiply(tietuBm.getPixel(i, j), totalRed, totalGreen, totalBlue));
                if (LogUtil.debugBaoZouFaceFuse) {
                    if (i % 30 == 0 && j % 30 == 0)
                        Log.d(TAG, String.format("第%d, %d个贴图上的点，值为%d", i, j, tietuBm.getPixel(i, j)));
                }
//                tietuBm.setPixel(i, j, tietuBm.getPixel(i, j));
            }
        }
        if (LogUtil.debugBaoZouFaceFuse)
            Log.d(TAG, "fuseTietu: 结束时间" + System.currentTimeMillis());
        return newTietuBm;
    }*/

    /**
     * 生成采样最值，即从贴图视图的像素矩形中取若干个点
     */
    private static float[] generateSimplingPosition(FloatImageView fiv) {
        int left = fiv.getLeft() + FloatImageView.PAD, right = fiv.getRight() - FloatImageView.PAD,
                top = fiv.getTop() + FloatImageView.PAD, bottom = fiv.getBottom() - FloatImageView.PAD;

        if (left < 0) left = 0;
        if (top < 0) top = 0;
        int step = (int) Math.sqrt((bottom - top) * (right - left) * 1d / MAX_SIMPLING_NUMBER); // 最多取2500个点,不用取所有的点
        if (step < 1) step = 1;
        float[] tietuPoints = new float[((bottom - top) / step + 1) * ((right - left) / step + 1) * 2];
        int id = 0;
        for (int y = top; y <= bottom; y += step) {
            for (int x = left; x <= right; x += step) {
                tietuPoints[id++] = x;
                tietuPoints[id++] = y;
            }
        }
        return tietuPoints;
    }

    private static float[] getPositionInBase(FloatImageView fiv, float[] tietuPoints, Rect srcRect, Rect dstRect) {
        // 设置旋转矩阵,旋转点

        Matrix matrix = new Matrix();
        float rotation = fiv.getRotation();

        if (LogUtil.debugBaoZouFaceFuse) {
            Log.d(TAG, "fuseTietu: 变换前的点");
            LogUtil.d(tietuPoints);
        }

        float[] pointInBase = new float[tietuPoints.length];
        matrix.postRotate(rotation, fiv.getLayoutCenterX(), fiv.getLayoutCenterY());
        matrix.postTranslate(-dstRect.left, -dstRect.top);
        float ratio = srcRect.width() * 1f / dstRect.width();
        matrix.postScale(ratio, ratio);
        matrix.postTranslate(srcRect.left, srcRect.top);

        matrix.mapPoints(pointInBase, tietuPoints);
        if (LogUtil.debugBaoZouFaceFuse) {
            Log.d(TAG, "fuseTietu: 变换后的点");
            LogUtil.d(pointInBase);
        }
        return pointInBase;
    }

    /**
     * 获取采样点对应在贴图的bm上的位置
     *
     * @param pointsInTietu 采样点，位置相对于父布局TietuFrameLayout,结果设置到这个参数里面
     */
    private static void getPositionInTietu(FloatImageView fiv, float[] pointsInTietu) {
        if (LogUtil.debugBaoZouFaceFuse) {
            Log.d(TAG, "原始采样点");
            LogUtil.d(pointsInTietu);
            Log.d(TAG, "原点" + (fiv.getLeft() + FloatImageView.PAD) + ", " + (fiv.getTop() + FloatImageView.PAD));
            Log.d(TAG, "缩放比" + 1 / fiv.getBmScaleX() + " , " + 1 / fiv.getBmScaleY());
        }
        Matrix matrix = new Matrix();
        matrix.postTranslate(-fiv.getLeft() - FloatImageView.PAD, -fiv.getTop() - FloatImageView.PAD); // 得到相对于bm起点的坐标

        matrix.postScale(1 / fiv.getBmScaleX(), 1 / fiv.getBmScaleY()); // 注意不是从中心缩小，从左上角缩小，两种对图片效果相同但结果的坐标值不同
        matrix.mapPoints(pointsInTietu);
        if (LogUtil.debugBaoZouFaceFuse) {
            Log.d(TAG, "缩放后的点");
            LogUtil.d(pointsInTietu);
        }
    }


    /**
     * 正片叠底，就是两个像素所有通道归一化之后相乘
     */
    static int Multiply(int basePixel, int r, int g, int b) {
//        return 0xFF000000 + (r << 16) + (g << 8) + b;
        int a = basePixel >>> 24;
        if (a == 0) return basePixel;
        r = ((basePixel & 0x00ff0000) >>> 16) * r >>> 8;
        g = ((basePixel & 0x0000ff00) >>> 8) * g >>> 8;
        b = (basePixel & 0x000000ff) * b >>> 8;

        //   叠加会降低亮度， 尝试提升整体亮度,但是似乎效果不佳
//        r = Math.min(255, (int) (r * 1.02));
//        g = Math.min(255, (int) (g * 1.02));
//        b = Math.min(255, (int) (b * 1.02));
        return (a << 24) + (r << 16) + (g << 8) + b;
    }

    /**
     * {@link PtuUtil#getLocationAtBaseBm(float, float, Rect, Rect)}
     */
    private static float[] getLocationAtBaseBm(float px, float py, Rect srcRect, Rect dstRect) {
        px -= dstRect.left;
        py -= dstRect.top;
        float ratio = srcRect.width() * 1f / dstRect.width();
        px *= ratio;
        py *= ratio;
        return new float[]{srcRect.left + px, srcRect.top + py};
    }

    /**
     * 让方法内联，不要改变private 属性
     */
    private int getGray(int pixel) {
        return (int) (((pixel & 0x00ff0000) >>> 16) * 0.299 + ((pixel & 0x0000ff00) >>> 8) * 0.587 + (pixel & 0x000000ff) * 0.114);
    }
}
