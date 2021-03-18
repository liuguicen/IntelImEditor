package com.mandi.intelimeditor.ptu.tietu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.widget.FrameLayout;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;

import org.jetbrains.annotations.Nullable;

/**
 * Created by liuguicen on 2016/9/29.
 * <p>
 * 贴图的FloatImageView的Bitmap操作类，
 * <p>（1）
 * <p>从sd卡中取出图片，能控制图片的最大尺寸，缩放过程中不超过这个尺寸；
 * <p>用参数表示出他们
 * <p>将图片画到原图上时，可以使用原始大小。
 * <p/>
 * <p>（2）
 * <p>图片所在的位置，因为要旋转，所以以它的中心点为准。初始化时放到底图的中心
 * <p>图片所在的范围，非水平的Rect，用于判断点击发生的位置
 * <p/>
 */
public class TietuSizeController {
    private static final String TAG = "TietuSizeController";
    private float maxWidth;
    private float minWidth;
    private float maxHeight;
    private float minHeight;

    TietuSizeController() {

    }

    public static int getMaxWidth() {
        return AllData.getScreenWidth();
    }

    public static int getMaxHeight() {
        return AllData.getScreenHeight();
    }

    @Nullable
    public static BitmapFactory.Options getFitWh(String path, boolean smallMode) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return adjustWH(options, smallMode);
    }

    @Nullable
    private static BitmapFactory.Options adjustWH(BitmapFactory.Options options, boolean smallMode) {
        Log.d(TAG, String.format("getFitWh: 原始宽 = %d, 原始高 = %d", options.outWidth, options.outHeight));
        if (options.outWidth <= 0 || options.outHeight <= 0) return null;

        // 宽高要求在一定的范围，不能太大，也不能太小
        float maxWidth = AllData.getScreenWidth(), maxHeight = AllData.getScreenHeight();
        if (smallMode) {
            maxWidth *= 0.5f;
            maxHeight *= 0.5f;
        }
        // 缩放比例，首先如果长宽都超过或者其中一条 最大宽高，那么取较小的比例，也就是取缩小程度最大的那个，保证长宽都不超过
        // 上面的到比例值小于1，与1比较取小
        // 如果长宽都没有超过，那么比例大于1，此时和1比取小的一个，
        float ratio = Math.min(1, Math.min(maxWidth / options.outWidth, maxHeight / options.outHeight));

        options.outWidth *= ratio;
        options.outHeight *= ratio;

        float minW = 200, minH = 200;
        // 不低于最小值只能满足一边，因为对于长宽比很大的图，都大于最小值会导致上面的缩小失效
        if (options.outWidth < minW && options.outHeight < minH) {
            // 如果两边都小于最小值，那么取放大程度较小的那个比例，也就是说，让一条边达到最小值，另一边不足最小值
            ratio = Math.min(minW / options.outWidth, minH / options.outHeight);
        }
        options.outWidth *= ratio;
        options.outHeight *= ratio;
        Log.d(TAG, String.format("getFitWh: 调整后的宽 = %d, 调整后的高 = %d", options.outWidth, options.outHeight));
        return options;
    }

    /**
     * @return 如果Bitmap用的内存超过剩余的值，会返回空
     * @deprecated 废弃方法
     * use glide {@link #getFitWh(String, boolean)}
     * 获取合适大小的Bitmap，Bitmap占用的内存不能超过剩余内存，如果超过，则返回空
     */
    @Nullable
    public static Bitmap getBitmapInSize(String path) {
        Bitmap bitmap = AllData.getPTuBmPool().get(path);
        if (bitmap != null) { // 直接从缓存获取
            return bitmap;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options = getFitOption(options);
        if (options == null) return null;
        Log.d(TAG, String.format("width = %d, height = %d", options.outWidth, options.outHeight));
        options.outWidth *= 2;
        options.outHeight *= 2;
        Log.d(TAG, String.format("width = %d, height = %d", options.outWidth, options.outHeight));
        return BitmapUtil.decodeBitmap(path, options);
    }

    /**
     * 让获取到Bitmap的宽高不超过屏幕且不超过剩余内存
     * 超过剩余内存返回NUll
     *
     * @param options
     * @return
     */
    static private BitmapFactory.Options getFitOption(BitmapFactory.Options options) {
        // 参数检查
        if (options.outWidth <= 0 || options.outHeight <= 0) return null;
        if (options.inSampleSize <= 1) {
            options.inSampleSize = 1;
        }

        int srcWidth = (int) Math.min(options.outWidth, getMaxWidth());
        int srcHeight = (int) Math.min(options.outHeight, getMaxHeight());

        options.inJustDecodeBounds = false;
        options.inDither = true;
        options.inMutable = false;
        options.inPreferQualityOverSpeed = true;
        options.inSampleSize = Math.min(options.outWidth / srcWidth, options.outHeight / srcHeight);
        long totalSize = options.outWidth / options.inSampleSize * (options.outHeight / options.inSampleSize) * 4;
        long realFreeMemory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();//使用freeMemaory不对
        if (totalSize > realFreeMemory) {
            return null;//内存超出了剩余内存
        }
        return options;
    }

    /**
     * 废弃方法
     * use glide
     */
    public static Bitmap getBitmapInSize(int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(IntelImEditApplication.appContext.getResources(), id, options);
        options = getFitOption(options);
        if (options == null) return null;
        return BitmapFactory.decodeResource(IntelImEditApplication.appContext.getResources(), id, options);
    }

    public static final int TIETU_POSITION_RANDOM = -Integer.MAX_VALUE;

    /**
     * 获取tietu的FloatImageView的布局参数，大小为min(合适的值,图片的大小）；
     * 合适的大小：
     * <p>（1）图片适中，不能太大，也不能太小。
     * <p> （2）对于底图很小的情况，其实不必考虑，照样保持正常大小即可
     * <p> 解析图片时，解析的就是一个适中的图，如果图片本身是特别小的，
     * 设置好view的大小，自动缩放即可
     * <p>
     * 位置为图片范围内的一个随机值
     *
     * @param bmHeight,bmWidth 原图的bm大小
     * @param picBound           显示的范围
     * @param left,top           贴图位置，{@link TietuSizeController#TIETU_POSITION_RANDOM}等等
     * @return 布局参数
     */
    public static FrameLayout.LayoutParams getFeatParams(int bmWidth, int bmHeight,
                                                         Rect picBound, int left, int top) {
        MPoint resWH = new MPoint();
        float ratio = getFeatWH(bmWidth, bmHeight, picBound, resWH);
        int exceptW = resWH.xInt();
        int exceptH = resWH.yInt();
        FrameLayout.LayoutParams parmas = new FrameLayout.LayoutParams(exceptW, exceptH);
        //位置，随机数，需要图片范围内
        if (left == TIETU_POSITION_RANDOM) {
            left = (int) Math.round(Math.random() * (picBound.width() - exceptW));
        }
        if (top == TIETU_POSITION_RANDOM) {
            top = (int) Math.round(Math.random() * (picBound.height() - exceptH));
        }
        parmas.leftMargin = picBound.left + left;
        parmas.topMargin = picBound.top + top;
        return parmas;
    }

    /**
     */
    public static float getFeatWH(int bmWidth, int bmHeight, Rect picBound, MPoint resWH) {
        if (bmWidth <= 0) {
            bmWidth = 1;
        }

        if (bmHeight <= 0) {
            bmHeight = 1;
        }

        //宽和高
        int exceptWidth = picBound.width() / 2;//1/2图片宽
        int screenWidth = AllData.screenWidth;
        if (screenWidth < 480)
            screenWidth = 480;

        if (exceptWidth > screenWidth * 2 / 5)
            exceptWidth = screenWidth * 2 / 5; //不能大于屏幕的某个比例
        else if (exceptWidth < screenWidth / 6) //太小
            exceptWidth = screenWidth / 6;

        int exceptHeight = picBound.height() / 2;  // 1/2图片高
        if (exceptHeight > AllData.getScreenHeight() * 2 / 7)
            exceptHeight = AllData.getScreenHeight() * 2 / 7; // 不能大于屏幕的高的某个比例
        else if (exceptHeight < AllData.getScreenHeight() / 7) // 太小
            exceptHeight = AllData.getScreenHeight() / 7;

        float ratio = Math.min(exceptHeight * 1f / bmHeight, exceptWidth * 1f / bmWidth);//保持长宽比，取小的一个
        exceptWidth = Math.round(bmWidth * ratio) + FloatImageView.PAD * 2;
        exceptHeight = Math.round(bmHeight * ratio) + FloatImageView.PAD * 2;
        resWH.x = exceptWidth;
        resWH.y = exceptHeight;
        return ratio;
    }

    /**
     * 在保证图片源宽高比不变的情况下，把图片宽高调整到合适的大小内
     * 不超过最大宽高，尽量不低于最小宽高
     */
    public static void adjustWH(float[] wh) {
        if (wh[0] == 0 || wh[1] == 0) return;
        float whRatio = wh[1] / wh[0];
        float maxWidth = getMaxWidth();
        if (wh[0] > maxWidth) { // 保证宽不超过
            wh[0] = maxWidth;
            wh[1] = maxWidth * whRatio;
        }
        float maxHeight = getMaxHeight();
        // 经过前面的检查，宽肯定不超过了，如果这里高不超过，跳过，如果高超过，会缩小，高缩小时，宽也会缩小，宽缩小更不会超过， 则两者都不超过
        if (wh[1] > maxHeight) {
            wh[1] = maxHeight;
            wh[0] = maxHeight / whRatio;
        }

        float minWidth = getStretchMinWidth();
        if (wh[0] < minWidth && minWidth * whRatio < maxHeight) { // 如果宽太小，在高不超过最大的情况下增大高
            wh[0] = minWidth;
            wh[1] = minWidth * whRatio;
        }

        float minHeight = getStretchMinHeight();
        if (wh[1] < minHeight && minHeight / whRatio < maxWidth) { // 对高同样的
            wh[1] = minHeight;
            wh[0] = minHeight / whRatio;
        }
    }

    private static float getStretchMinWidth() {
        return 50;
    }

    private static float getStretchMinHeight() {
        return 50;
    }

}
