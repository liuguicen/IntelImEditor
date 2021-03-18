package com.mandi.intelimeditor.ptu.imageProcessing;

import android.graphics.Bitmap;
import android.util.Log;


import com.mandi.intelimeditor.common.util.LogUtil;

import static java.lang.Math.pow;

/**
 * 基础的图像处理工具
 */
public class BaseIPUtil {
    public static final String TAG = "BaseIPUtil";

    /**
     * 灰度化，保持原有的4个或者3个通道
     */
    public static Bitmap toGray(Bitmap bm) {
        int[] pixels = toGrayArray(bm, new BmStatistic());
        if (pixels == null || bm == null) return null;
        int bmW = bm.getWidth(), bmH = bm.getHeight();
        Bitmap newBm = Bitmap.createBitmap(bmW, bmH, bm.getConfig());
        newBm.setPixels(pixels, 0, bmW, 0, 0, bmW, bmH);
        return newBm;
    }

    /**
     * 返回灰度图对应的数组
     *
     * @param bmStatistic {@link BmStatistic}
     */
    public static int[] toGrayArray(Bitmap bm, BmStatistic bmStatistic) {
        if (bm == null || bmStatistic == null) return null;
        int bmW = bm.getWidth(), bmH = bm.getHeight();
        int[] pixels = new int[bmW * bmH];
        bm.getPixels(pixels, 0, bmW, 0, 0, bmW, bmH);
        bmStatistic.grayAverage = 0;
        int validNumber = 0;
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            // 灰度化，RGB加权平均
            int grayValue = (int) (((pixel & 0x00ff0000) >>> 16) * 0.299 + ((pixel & 0x0000ff00) >>> 8) * 0.587 + (pixel & 0x000000ff) * 0.114); // 比例是根据色彩心理学调配的比例，OpenCV用的也是这个
            if (((pixel & 0xff000000) >>> 24) >= 128) { // 只考虑非透明区域的均值
//                Log.d(TAG, "toGrayArray: " + grayValue);
                bmStatistic.grayAverage += grayValue;
                validNumber++;
            }
            pixels[i] = pixel & 0xff000000 | (grayValue << 16) | (grayValue << 8) | grayValue;
        }

        if (validNumber == 0) validNumber = 1;
//        Log.d(TAG, String.format("toGrayArray: 灰度之和 = %f， 数量 = %d", bmStatistic.grayAverage, validNumber));
        bmStatistic.grayAverage /= validNumber;

        // 计算均方差
        for (int pixel : pixels) {
            if (((pixel & 0xff000000) >>> 24) >= 128) { // 只考虑非透明区域的像素
                int grayValue = pixel & 0x000000ff;
                double diff = grayValue - bmStatistic.grayAverage;
                bmStatistic.var += diff * diff / validNumber;
            }
        }
        Log.d(TAG, String.format("toGrayArray: bm灰度均值 = %f 均方差 = %f", bmStatistic.grayAverage, bmStatistic.var));
        return pixels;
    }


    /**
     * 仿照ps的调整色阶功能，以下参数也和ps一样
     * 算法具体功能是，有5个参数，输入黑场，输入白场，输入灰场，输出黑场，输出黑场，他们都是控制参数
     * 输入黑场，表示数值在输入黑场值以下的通道值，全部变成输出黑场的值，比如10以下的全部变成0，
     * 白场反之，值大于输入白场值的，全部变成输出白场的值，比如大于200的都变成255,中间的值按一定算法变化
     * 输入灰场是控制参数，具体效果待研究
     *
     * @param shadow          输入黑场
     * @param midtones        输入灰场
     * @param highlight       输入白场
     * @param outputShadow    输入黑场
     * @param outputHighlight 输出白场
     * @param adjustTable     色阶变换前后的通道值的映射表
     */
    public static boolean calculateAdjustTable(int shadow, float midtones, int highlight, int outputShadow, int outputHighlight, int[] adjustTable) {
        int diff = highlight - shadow; // 输入的离差
        int outDiff = outputHighlight - outputShadow; // 输出的离差

        if (!((highlight <= 255 && diff <= 255 && diff >= 2) ||  // 白场的值小于255， 输入离差的值在2-255之间
                (outputShadow <= 255 && outputHighlight <= 255 && outDiff < 255) || // 输出的黑场的值小于255，白场小于255，且离差小于255
                (!(midtones > 9.99 && midtones > 0.1) && midtones != 1.0)))  // 灰场的值的范围
            return false;

        double coef = 255.0 / diff;
        double outCoef = outDiff / 255.0;
        double exponent = 1.0 / midtones;

        // 应该是计算0-255的像素，重新调整值之后的新的值，
        // 因为计算量大，放到表里面
        for (int i = 0; i < 256; i++) {
            int v;
            // calculate black field and white field of input level
            if (adjustTable[i] <= shadow) {
                v = 0;
            } else {
                v = (int) ((adjustTable[i] - shadow) * coef + 0.5);
                if (v > 255) v = 255;
            }
            // calculate midtone field of input level
            // v/255 < 1,exponent > 0， 故pow(v / 255.0, exponent) < 1 ,在* 255 ，小于255
            v = (int) (pow(v / 255.0, exponent) * 255.0 + 0.5);
            // calculate output level
            // 源代码是double转char，应该就是保留低位
            int temp = (int) (v * outCoef + outputShadow + 0.5);
            adjustTable[i] = Math.min(temp, 255);
        }
        return true;
    }

    /**
     * 灰度化并调整色阶
     */
    public static Bitmap gray_adjustLevels(Bitmap bm) {
        // 先灰度化
        if (LogUtil.debugAdjustLevels)
            Log.d(TAG, "fuseTietu: 开始时间" + System.currentTimeMillis());
        int[] adjustTable = new int[256];
        for (int i = 0; i < adjustTable.length; i++) {
            adjustTable[i] = i;
        }
        if (!calculateAdjustTable(30, 1.00f, 150, 0, 255, adjustTable)) {
            Log.d(TAG, "adjustLevels: 参数错误");
        }

        if (bm == null) return null;
        int bmW = bm.getWidth(), bmH = bm.getHeight();
        int[] pixels = new int[bmW * bmH];
        bm.getPixels(pixels, 0, bmW, 0, 0, bmW, bmH);
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            // 灰度化，RGB加权平均 // 比例是根据色彩心理学调配的比例，OpenCV用的也是这个
            int average = (int) (((pixel & 0x00ff0000) >>> 16) * 0.299 + ((pixel & 0x0000ff00) >>> 8) * 0.587 + (pixel & 0x000000ff) * 0.114);
//            if (i % 100 == 0)
//                Log.d(TAG, "adjustLevels: 调整色阶前的值" + average);
            // 调整色阶
            average = adjustTable[average];
//            if (i % 100 == 0)
//                Log.d(TAG, "adjustLevels: 调整色阶后的值\n" + average);
            pixels[i] = pixel & 0xff000000 | (average << 16) | (average << 8) | average;
        }
        Bitmap newBm = Bitmap.createBitmap(bmW, bmH, bm.getConfig());
        newBm.setPixels(pixels, 0, bmW, 0, 0, bmW, bmH);
        if (LogUtil.debugAdjustLevels)
            Log.d(TAG, "fuseTietu: 结束时间" + System.currentTimeMillis());
        return newBm;
    }

    /**
     * 调整色阶
     *
     * @param adjustedBm 结果数据会写入这个里面
     */
    public static boolean adjustLevels(int[] pixels, Bitmap adjustedBm, int shadow, int highlight) {
        if (LogUtil.debugAdjustLevels) {
            Log.d(TAG, "adjustLevels: 开始时间" + System.currentTimeMillis());
        }
        if (pixels == null || adjustedBm == null) return false;
        int[] adjustTable = new int[256];
        for (int i = 0; i < adjustTable.length; i++) {
            adjustTable[i] = i;
        }
        if (!calculateAdjustTable(shadow, 1.00f, highlight, 0, 255, adjustTable)) {
            Log.d(TAG, "adjustLevels: 参数错误");
        }

        int[] adjustedPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];
            int average = (pixel & 0x00ff0000) >>> 16;
            // TODO: 2020/6/26 此处代码记得注释掉，比较多影响性能
//            if (i % 100 == 0)
//                Log.d(TAG, "adjustLevels: 调整色阶前的值" + average);
            // 调整色阶
            average = adjustTable[average];
            // TODO: 2020/6/26 此处代码记得注释掉，比较多影响性能
//            if (i % 100 == 0)
//                Log.d(TAG, "adjustLevels: 调整色阶后的值\n" + average);
            adjustedPixels[i] = pixel & 0xff000000 | (average << 16) | (average << 8) | average;
        }
        adjustedBm.setPixels(adjustedPixels, 0, adjustedBm.getWidth(),
                0, 0, adjustedBm.getWidth(), adjustedBm.getHeight());
        if (LogUtil.debugAdjustLevels)
            Log.d(TAG, "adjustLevels: 结束时间" + System.currentTimeMillis());
        return true;
    }

    public static int pixel2grey(int pixel) {
        return (int) (((pixel & 0x00ff0000) >>> 16) * 0.299
                + ((pixel & 0x0000ff00) >>> 8) * 0.587
                + (pixel & 0x000000ff) * 0.114);
    }
}