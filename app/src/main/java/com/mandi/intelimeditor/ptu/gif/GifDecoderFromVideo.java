package com.mandi.intelimeditor.ptu.gif;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
// 400 时间 = 1565945045166 - 1565944969275 = 76s

/**
 * 从视频解析出GIF帧的
 */
public class GifDecoderFromVideo extends GifDecoder {
    private boolean isDebug = false;
    public static final String TAG = "GifDecoderFromVideo";
    // 视频解析成gif需要一个时长和帧长的策略
    public static final int DEFAULT_INTERVAL = 200;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 400;

    public static final int STATUS_TOO_LONG = 594;
    public static final int STATUS_TOO_SHORT = 926;
    public static final int STATUS_OK = 310;
    private int decode_status = STATUS_OK;
    private boolean mIsStop;
    private MediaMetadataRetriever mRetriever;

    GifDecoderFromVideo() {
        mIsStop = false;
    }

    public void read(String path) {
        mRetriever = new MediaMetadataRetriever();
        long startTime = System.currentTimeMillis();
        if (isDebug) {
            Log.d(TAG, "进入 read，time = " + System.currentTimeMillis());
        }
        mRetriever.setDataSource(path);
        if (isDebug) {
            Log.d(TAG, "setDataSource完成，time = " + System.currentTimeMillis());
        }
        if (mIsStop) {
            releaseUnnecessaryData();
            return;
        }
        // 取得视频的长度(单位为毫秒)
        String time = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(毫秒)
        int duration = Integer.valueOf(time);
        if (isDebug) {
            Log.d(TAG, "视频长度 = " + duration);
        }
        if (duration <= 10) {
            decode_status = STATUS_TOO_SHORT;
            return;
        }
        if (duration > AllData.SHORT_VIDEO_DURATION_MAX) {
            Log.e(TAG, "read: 视频太长，不支持");
            decode_status = STATUS_TOO_LONG;
            return;
        }
        // 按照间隔取出帧
        int delay = DEFAULT_INTERVAL;
        int frameNumber = duration / delay;
        if (progressCallback != null) {
            progressCallback.setMax(frameNumber);
        }
        String swidth = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        int width = Integer.valueOf(swidth);
        String sheight = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        int height = Integer.valueOf(sheight);
        float ratio = 1;
        if (width > MAX_WIDTH) {
            ratio = width * 1f / MAX_WIDTH;
        }
        if (height > MAX_HEIGHT) {
            ratio = Math.max(ratio, height * 1f / MAX_HEIGHT);
        }
        int finalWidth = Math.round(width / ratio);
        int finalHeight = Math.round(height / ratio);
        if (isDebug) {
            Log.d(TAG, "获取长宽时长等数据完成，准备获取帧，time = " + System.currentTimeMillis());
        }
        for (int i = 0; i <= frameNumber; i++) {
            // 注意是以微妙为单位
            long timeUs = i * delay * 1000L;
            Bitmap oneFrameBm = getFrameBm(timeUs, finalWidth, finalHeight);
            if (isDebug) {
                Log.d(TAG, "获取第:  " + i + "  帧完成，time = " + System.currentTimeMillis());
            }
            if (oneFrameBm != null) {
                frameList.add(new GifFrame(oneFrameBm, delay));
                if (progressCallback != null) {
                    progressCallback.onProgress(i);
                }
                if (isDebug) {
                    Log.d(TAG, "第 " + i + " 帧: 宽 = " + oneFrameBm.getWidth() + " 高 = " + oneFrameBm.getHeight());
                }
            }
            if (mIsStop) {
                releaseUnnecessaryData();
                return;
            }
        }
        // 最后一帧,如果没加入，加入它
        int gifLen = frameNumber * delay;
        if (gifLen < duration) {
            Bitmap lastFrameBm = getFrameBm(duration, finalWidth, finalHeight);
            if (lastFrameBm != null) {
                frameList.add(new GifFrame(lastFrameBm, duration - gifLen));
                if (isDebug) {
                    Log.d(TAG, "最后一帧: 宽 = " + lastFrameBm.getWidth() + " 高 = " + lastFrameBm.getHeight());
                }
            }
            if (isDebug) {
                Log.d(TAG, "最后一帧: 宽 = " + lastFrameBm.getWidth() + " 高 = " + lastFrameBm.getHeight());
                Log.d(TAG, "获取最后一帧完成， 准备释放数据，time = " + System.currentTimeMillis());
            }
            if (progressCallback != null) {
                progressCallback.onProgress(frameNumber);
            }
        }

        mRetriever.release();
        if (isDebug) {
            Log.d(TAG, "释放数据完成，time = " + System.currentTimeMillis());
            Log.d(TAG, "总耗时 = " + (System.currentTimeMillis() - startTime));
        }
    }

    private Bitmap getFrameBm(long timeUs, int finalWidth, int finalHeight) {
        Bitmap frameBm;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            frameBm = mRetriever.getScaledFrameAtTime(timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST, finalWidth, finalHeight);
        } else {
            frameBm = mRetriever.getFrameAtTime(timeUs);
            if (isDebug) {
                Log.d(TAG, "获取帧完成，time = " + System.currentTimeMillis());
            }
            if (frameBm.getWidth() != finalWidth && frameBm.getHeight() != finalHeight) {
                Bitmap oldBm = frameBm;
                frameBm = Bitmap.createScaledBitmap(frameBm, finalWidth, finalHeight, false);
                if (isDebug) {
                    Log.d(TAG, "缩放帧完成，time = " + System.currentTimeMillis());
                }
                oldBm.recycle();
            }
        }
        // TODO: 2020/10/20 因为人脸检测那个部分只能处理ARGB 8888的 所以这里565更改为8888了
        Bitmap oldBm = frameBm;
        if (oldBm != null) {
            frameBm = oldBm.copy(Bitmap.Config.ARGB_8888, true);
            oldBm.recycle();
        }
        return frameBm;
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
    public void stopDecode() {
        mIsStop = true;
    }

    @Override
    public void releaseUnnecessaryData() {
        mRetriever.release();
        for (GifFrame gifFrame : frameList) {
            if (gifFrame.bm != null) {
                gifFrame.bm.recycle();
            }
        }
    }
}
