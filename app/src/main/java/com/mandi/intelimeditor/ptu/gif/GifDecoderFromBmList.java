package com.mandi.intelimeditor.ptu.gif;

import android.graphics.Bitmap;

import java.util.List;

public class GifDecoderFromBmList extends GifDecoder {
    public static final String TAG = "GifDecoderFromBmList";

    public void readFromBmList(List<Bitmap> bmList) {
        // 首先获取每张图片的原始尺寸
        progressCallback.setMax(bmList.size());
        // 将每张图缩放到统一的帧大小，
        // 如果长宽比不同，还需要再进行变形绘制到统一的大小上面
        for (int i = 0; i < bmList.size(); i++) {
            Bitmap bm = bmList.get(i);
            frameList.add(new GifFrame(bm, DEFAULT_FRAME_DELAY));
            if (progressCallback != null) {
                progressCallback.onProgress(i);
            }
        }
        if (progressCallback != null) {
            progressCallback.onProgress(1);
        }
    }

    @Override
    boolean err() {
        return false;
    }

    @Override
    void stopDecode() {

    }

    @Override
    void releaseUnnecessaryData() {

    }
}
