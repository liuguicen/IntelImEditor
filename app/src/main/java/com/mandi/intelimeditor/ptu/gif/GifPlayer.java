package com.mandi.intelimeditor.ptu.gif;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.mandi.intelimeditor.bean.GifPlayFrameEvent;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;

/**
 * <pre>
 *     Gif播放控制的类，配合MyGifDecoder使用的
 * <pre>
 */
public class GifPlayer {
    public static final String TAG = "GifPlayer";
    private long startTime = 0;
    private long totalPlayTime;
    private View mGifView;
    private GifFrame[] allFrames;
    private long[] frameStartTimes;
    private volatile boolean isPlaying;
    private final Handler mMainHandler;
    private Runnable mPlay;
    private long mPauseTime;
    private GifPlayCallBack mGifPlayCallBack;

    public GifPlayer(GifFrame[] gifFrames) {
        allFrames = gifFrames;
        frameStartTimes = new long[allFrames.length];
        totalPlayTime = 0;
        for (int i = 0; i < allFrames.length; i++) {
            frameStartTimes[i] = totalPlayTime; // 刚好，前一次的总时间，可以做这个Frame的起始时间
            totalPlayTime += allFrames[i].delay;
        }
        setPlaying(false);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void resetFramesAndStart(GifFrame[] gifFrames) {
        stop();
        allFrames = gifFrames;
        frameStartTimes = new long[allFrames.length];
        totalPlayTime = 0;
        for (int i = 0; i < allFrames.length; i++) {
            frameStartTimes[i] = totalPlayTime; // 刚好，前一次的总时间，可以做这个Frame的起始时间
            totalPlayTime += allFrames[i].delay;
        }
        start();
    }

    public void setGifView(View gifView) {
        mGifView = gifView;
    }

    public void setGifPlayCallBack(GifPlayCallBack gifPlayCallBack) {
        mGifPlayCallBack = gifPlayCallBack;
    }

    /**
     * 需在主线程里面调用
     */
    public void start() {
        if (!isPlaying()) {
            startTime = System.currentTimeMillis();
            //            Logcat.d("gif动画开始 time = " + startTime);
            setPlaying(true);
            playOneFrame();
        }
        //        Logcat.d("开始Play时间
    }

    public synchronized void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public synchronized boolean isPlaying() {
        return isPlaying;
    }

    public void stop() {
        setPlaying(false);
        mMainHandler.removeCallbacks(mPlay);
    }

    public void pause() {
        if (isPlaying()) {
            stop();
            mPauseTime = System.currentTimeMillis();
        }
    }

    public void resume() {
        if (!isPlaying()) {
            startTime += System.currentTimeMillis() - mPauseTime;
            setPlaying(true);
            playOneFrame();
        }
    }

    /**
     * 内部方法
     * 采用一旦开始就连续循环播放的方式，不会暂停，这样计时简单，
     * 即使用户不可见，也会计时，相当于播放了
     * <p>
     * 具体方法是 计算当前时间与开始时间的差dt，判段dt时间改播放哪一帧就播放那一帧，然后将距离下一帧的时间传给postDelay
     * 再循环调用此方法
     */
    private void playOneFrame() {
        // Log.d(TAG, "play: ");
        // Log.d(TAG, "play: " + this + " " + isPlaying());
        if (isPlaying()) {
            // Log.d(TAG, "play: enter");
            // Log.d(TAG, "play: "  + this + " " +  isPlaying());
            Pair<GifFrame, Long> pair = getCurPlayFrame();
            if (pair == null) return;

            if (mGifView instanceof PtuSeeView) {
                ((PtuSeeView) mGifView).onlyReplaceSrcBm(pair.first.bm);
                mGifView.postInvalidate();
            } else if (mGifView instanceof ImageView) {
                ((ImageView) mGifView).setImageBitmap(pair.first.bm);
            }


            if (allFrames.length == 1) return; // 只有一帧
            mPlay = this::playOneFrame;
            //            Logcat.d("postDelay = " + pair.second);
            mMainHandler.postDelayed(mPlay, pair.second);
        }
    }

    /**
     * @return 距离显示下一帧的时间，用于View设置下一次绘制的时延
     * @see GifPlayer#playOneFrame() 只获取当前应该播放哪一帧，不改变内部数据，不影响播放，可以外部调用，获取的
     * 结果就是正常情况下（播放View没被阻塞到等），播放View当前播放出来的帧
     */
    @Nullable
    public Pair<GifFrame, Long> getCurPlayFrame() {
        // Logcat.d();
        long curTime = System.currentTimeMillis();
        long modTime = 0;
        if (totalPlayTime > 0) {
            modTime = (curTime - startTime) % totalPlayTime;
        }
        GifFrame playFrame = null;
        long nextDelay = 1000;
        for (int i = 0; i < frameStartTimes.length; i++) {
            // right即下一帧开始播放的时间
            long right = (i == frameStartTimes.length - 1) ? totalPlayTime + 1 : frameStartTimes[i + 1];
            if (frameStartTimes[i] <= modTime && modTime < right) { // 夹在中间
                nextDelay = right - modTime;
                //                Logcat.d(String.format(Locale.CHINA,
                //                        "当前时间 = %d " +
                //                                "\n 总时延 = %d " +
                //                                "\n 取余后的时间= %d " +
                //                                "\n 显示的帧序号=  %d " +
                //                                "\n 到下一帧时延= %d ",
                //                        curTime, totalPlayTime, modTime, i, nextDelay));
                playFrame = allFrames[i];
            }
        }
        if (playFrame == null && allFrames.length > 0) {
            nextDelay = totalPlayTime - modTime;
            playFrame = allFrames[allFrames.length - 1];
        }
        if (playFrame == null) {
            return null;
        }
        if (mGifPlayCallBack != null) {
            mGifPlayCallBack.onPlay(playFrame.originID);
        }
        EventBus.getDefault().post(new GifPlayFrameEvent(playFrame.originID));
        return new Pair<>(playFrame, nextDelay);
    }

    public Bitmap getFirstFrameBm() {
        return allFrames[0].bm;
    }

    public boolean hasStarted() {
        return isPlaying();
    }

    public GifFrame[] getPlayFrameList() {
        return allFrames;
    }

    public interface GifPlayCallBack {
        /**
         * @param id 正在播放的帧
         */
        void onPlay(int id);
    }
}
