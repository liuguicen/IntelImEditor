package com.mandi.intelimeditor.ptu.gif;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;

import com.mandi.intelimeditor.common.dataAndLogic.MemoryManager;
import com.mandi.intelimeditor.common.util.BitmapUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/08/10
 *      version : 1.0
 * <pre>
 */
class GifDecoderFromPicList extends GifDecoder {
    public static final String TAG = "GifDecoderFromPicList";
    public static final boolean isInTest = false;
    private boolean mIsStop;
    int decode_status;

    public GifDecoderFromPicList() {
        mIsStop = false;
    }

    @Override
    public void read(List<String> picPathList) throws IOException {
        // 首先获取每张图片的原始尺寸
        progressCallback.setMax(picPathList.size());
        List<Point> whList = readPicWH(picPathList);
        if (mIsStop) {
            return;
        }
        Point chosenWh = getFitWH(whList);
        if (mIsStop) {
            return;
        }
        Point finalWH = getFinalWH(picPathList, chosenWh);
        if (mIsStop) {
            return;
        }
        // 将每张图缩放到统一的帧大小，
        // 如果长宽比不同，还需要再进行变形绘制到统一的大小上面
        BitmapFactory.Options optsa = new BitmapFactory.Options();
        optsa.inMutable = true;
        optsa.inPreferredConfig = Bitmap.Config.ARGB_8888;
        for (int i = 0; i < picPathList.size(); i++) {
            if (mIsStop) {
                releaseUnnecessaryData();
                return;
            }
            Bitmap bm = decodeFitSize(picPathList.get(i), whList.get(i), finalWH, optsa);
            if (bm == null) {
                decode_status = STATUS_OPEN_ERROR;
                releaseUnnecessaryData();
                debugLog("读取或缩放图片出错");
                return;
            }
            debugLog("最终尺寸 " + i + bm.getWidth() + bm.getHeight());
            frameList.add(new GifFrame(bm, DEFAULT_FRAME_DELAY));
            if (progressCallback != null) {
                progressCallback.onProgress(i);
            }
        }
        if (progressCallback != null) {
            progressCallback.onProgress(1);
        }
    }

    private Bitmap decodeFitSize(String path, Point WHI, Point finalWH, BitmapFactory.Options optsa) {
        // 同样尺寸的，直接缩放得出图片
        Bitmap firstBm;
        optsa.inSampleSize = 1;
        if (WHI.equals(finalWH)) {
            debugLog("没有缩放，直接读取并加入");
            return BitmapUtil.decodeBitmap(path, optsa);
        } else {
            // 不是同样尺寸的，先缩放读取
            float ratio = Math.max(WHI.x * 1f / finalWH.x, WHI.y * 1f / finalWH.y); // 将当前图片完全缩放到帧的矩形里面，需要比较长宽那个缩放倍数高，按照高的来
            optsa.inSampleSize = (int) ratio;
            firstBm = BitmapUtil.decodeBitmap(path, optsa);
            if (firstBm == null) return null;

            if (firstBm.getWidth() == finalWH.x && firstBm.getHeight() == finalWH.y) {
                debugLog("读取时缩放后尺寸一致，加入");
                return firstBm;
            } else {
                debugLog("读取缩放后尺寸不一致，变形缩放的帧的框中后加入");
                Bitmap scaledBm = Bitmap.createScaledBitmap(firstBm, finalWH.x, finalWH.y, true);
                if (scaledBm != firstBm) {
                    firstBm.recycle();
                }
                return scaledBm;
            }
        }

    }

    private void debugLog(String s) {
        if (isInTest) {
            Log.d(TAG, s);
        }
    }

    private Point getFinalWH(List<String> picPathList, Point chosenWh) {
        // 算出gif的fragment帧列表统一的长宽
        // 原始大小
        float originalSize = chosenWh.x * chosenWh.y * 0.5f * picPathList.size();
        // 允许大小
        float allowSize = Math.min(10 * 1024 * 1024, MemoryManager.getUsableMemoryByte() / 5);
        // 如果原图的大小超过允许的大小，就需要缩放
        Point finalWh = new Point(chosenWh.x, chosenWh.y);
        if (originalSize > allowSize) {
            // 要缩放的倍数, Bitmap解析总体积是倍数的平方，这里先开方
            double ratio = Math.sqrt(originalSize / allowSize);
            finalWh.set((int) Math.round(chosenWh.x / ratio), (int) Math.round(chosenWh.y / ratio));
            debugLog("缩小 " + ratio + "倍");
        }
        debugLog("最终的选定宽高为 " + finalWh);
        return finalWh;
    }

    /**
     * 选取某个尺寸，尺寸等于这个尺寸的图片的数量最多，然后用这个尺寸缩放，得到最后的Gif帧的尺寸
     * 就是让最多的图能直接用原图来做，不用变形
     */
    private Point getFitWH(List<Point> whList) {
//        int maxWidth = 0;
//        int maxHeight = 0;
        int maxEqualNumber = 1;
        int chosenId = 0;
        // 数量少，直接双层循环比较
        for (int i = 0; i < whList.size(); i++) {
            int number = 0;
            Point pi = whList.get(i);
            for (int j = 0; j < whList.size(); j++) {
                Point pj = whList.get(j);
                if (pi.x == pj.x && pi.y == pj.y) {
                    number++;
                }
//                if (pj.x >= pi.x) {
//                    maxWidth = pi.x;
//                }
//                if (pj.y >= pi.y) {
//                    maxHeight = pi.y;
//                }
            }
            if (number > maxEqualNumber) {
                maxEqualNumber = number;
                chosenId = i;
            }
        }
        Point chosenWh = new Point(whList.get(chosenId).x, whList.get(chosenId).y);
        debugLog(String.format(Locale.CHINA, "选中了第%d张图片，长宽为%s", chosenId, chosenWh));
        return chosenWh;
    }

    private List<Point> readPicWH(List<String> picPathList) {
        List<Point> whList = new ArrayList<>();
        for (String path : picPathList) {
            Point bmWH = BitmapUtil.getBmWH(path);
            whList.add(bmWH);
            debugLog("" + bmWH);
        }
        return whList;
    }

    @Override
    public void stopDecode() {
        mIsStop = true;
    }


    @Override
    public boolean err() {
        return decode_status != STATUS_OK;
    }

    @Override
    public String getErrorMessage() {
        return "";
    }

    @Override
    public void releaseUnnecessaryData() {
        for (GifFrame gifFrame : frameList) {
            if (gifFrame != null && gifFrame.bm != null) {
                gifFrame.bm.recycle();
            }
        }
    }
}
