package com.mandi.intelimeditor.ptu.gif;

import android.graphics.Bitmap;

import com.mandi.intelimeditor.common.util.ProgressCallback;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/08/10
 *      version : 1.0
 *      GIF解码器的虚基类，可能会用到不同的方式解码生成gif的帧序列，
 *      比如标准的gif图片文件，图片列表，视频，或者到相机等
 * <pre>
 */
abstract class GifDecoder {
    int STATUS_OK = 0;//解码成功
    int STATUS_FORMAT_ERROR = 1;//格式错误
    int STATUS_OPEN_ERROR = 2;//打开图片失败
    int DEFAULT_FRAME_DELAY = 500;
    protected ProgressCallback progressCallback;
    protected List<GifFrame> frameList;

    public GifDecoder() {
        frameList = new ArrayList<>();
    }

    void read(String picPath) throws IOException {
    }

    void read(List<String> picList) throws IOException {
    }

    void readFromBmList(List<Bitmap> bmList, List<Integer> delayList) {
    }

    ;

    abstract boolean err();

    String getErrorMessage() {
        return "";
    }

    @NotNull
    public List<GifFrame> getAllFrames() {
        return frameList;
    }

    abstract void stopDecode();

    abstract void releaseUnnecessaryData();

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
}
