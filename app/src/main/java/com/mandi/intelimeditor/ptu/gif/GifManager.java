package com.mandi.intelimeditor.ptu.gif;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bilibili.burstlinker.BurstLinker;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ProgressCallback;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.WrapContentLinearLayoutManager;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.imageProcessing.FaceFeatureDetector;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.R;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/26
 *      version : 1.0
 * <pre>
 */
public class GifManager implements GifFramesLvAdapter.OnRecyclerViewItemClickListener {
    public static final int MAX_FRAME_DELAY = 100 * 1000;
    public static final int MIN_FRAME_DELAY = 10;
    public static final String TAG = "GifManager";
    private boolean mIsDestroy;
    private RecyclerView framesLv;
    private GifFrame[] frames;
    private Context mContext;
    private GifFramesLvAdapter framesLvAdapter;
    private GifPlayer mainPlayer; // 用于在PTuSeeView上播放gif的
    private int mLastPosition;
    private int MIN_REFRESH_INTERVAl = 16;
    private GifDecoder mGifDecoder;
    private ImageView showHideBtn;
    private TextView mPlayAllBtn;
    private TextView operationNoticeTv;
    private boolean mIsPreview;
    // 判断播放的gif帧的选中状态是否变化的，比如连续几张都是选中，就没变化，通过这个变量判断
    private boolean mIsLastChosen;

    public GifManager(Context context) {
        this.mContext = context;
        US.putGifEvent(US.GIF_USE);
        mIsDestroy = false;
        mIsPreview = false;
        mIsLastChosen = true; //
    }

    /**
     * 耗时较长，有IO操作，非主线程调用
     *
     * @param isVideo          从从短视频中制作
     * @param progressCallback
     */
    public void loadGifData(String picPath, boolean isVideo, ProgressCallback progressCallback) throws IOException {
        if (isVideo) {
            mGifDecoder = new GifDecoderFromVideo();
            mGifDecoder.setProgressCallback(progressCallback);
        } else {
            mGifDecoder = new StandardGifDecoder();
            mGifDecoder.setProgressCallback(progressCallback);
        }
        mGifDecoder.read(picPath);
        processData(); // 解析gif的实现不一样，其它都一样
    }

    /**
     * @param picList          从图片路径列表获取Bitmap列表，然后生成gif
     * @param progressCallback
     */
    public void loadFromPicList(List<String> picList, ProgressCallback progressCallback) throws IOException {
        if (picList == null) {
            throw new NullPointerException();
        }
        mGifDecoder = new GifDecoderFromPicList();
        mGifDecoder.setProgressCallback(progressCallback);
        mGifDecoder.read(picList);
        processData();
    }

    /**
     * @param frmList          从图片路径列表获取Bitmap列表，然后生成gif
     * @param progressCallback
     */
    public void loadFromGifFrames(List<GifFrame> frmList, ProgressCallback progressCallback) throws IOException {
        if (frmList == null) {
            throw new NullPointerException();
        }
        mGifDecoder = new GifDecoderFromGifFrames(frmList);
        mGifDecoder.setProgressCallback(progressCallback);
        processData();
    }


    private void processData() throws IOException {
        if (mIsDestroy) { // 已经销毁，直接退出，不要抛异常，依赖于AC，会出错
            return;
        }
        if (mGifDecoder.err()) {
            throw new IOException(mGifDecoder.getErrorMessage());
        }
        List<GifFrame> allFrameList = filterFrame(mGifDecoder.getAllFrames());
        int size = allFrameList.size();
        frames = new GifFrame[size];
        for (int i = 0; i < size; i++) {
            GifFrame frame = allFrameList.get(i);
            GifFrame gifFrame = new GifFrame(frame.bm, frame.delay);
            gifFrame.isChosen = true;
            gifFrame.originID = i;
            frames[i] = gifFrame;
        }
        LogUtil.d("gif帧数 = " + frames.length);
        mainPlayer = new GifPlayer(frames.clone());
        mainPlayer.setGifPlayCallBack(this::onPlay);
        mLastPosition = -1;
        mGifDecoder = null; // 不需要了，释放，destroy需要它，不能设置成局部变量
    }

    /**
     * 过滤掉不正确的帧
     * 目前时长太短的
     *
     * @param srcFrames
     * @return
     */
    private @NotNull
    List<GifFrame> filterFrame(List<GifFrame> srcFrames) throws IOException {
        if (srcFrames.size() == 0) {
            throw new IOException("GIF图片没有内容");
        }

        List<GifFrame> finalFrameList = new ArrayList<>();
        for (GifFrame frame : srcFrames) {
            if (frame.delay < MIN_REFRESH_INTERVAl) //  时延太短，去除
                continue;
            finalFrameList.add(frame);
        }
        if (srcFrames.size() > 0 && finalFrameList.size() == 0) { // 一帧都没加，加入第一帧
            GifFrame e = srcFrames.get(0);
            e.delay = 1000;
            finalFrameList.add(e);
        }
        return finalFrameList;
    }


    public void initView(View operationLayout) {
        operationLayout.setVisibility(View.VISIBLE);
        framesLv = operationLayout.findViewById(R.id.gif_rv);
        mPlayAllBtn = operationLayout.findViewById(R.id.gif_play_all);
        operationNoticeTv = operationLayout.findViewById(R.id.gif_notice_tv);
        operationNoticeTv.setOnClickListener(v -> {
            ToastUtils.show(R.string.gif_operation_notice);
        });
        mPlayAllBtn.setOnClickListener(v -> {
            if (mIsPreview) {
                setPlayChosen();
            } else {
                preview();
            }
        });
        showHideBtn = operationLayout.findViewById(R.id.gif_frame_show_hide);
        showHideBtn.setOnClickListener(v -> {
            if (framesLv.getVisibility() == View.VISIBLE) {
                framesLv.setVisibility(View.GONE);
                mPlayAllBtn.setVisibility(View.GONE);
                operationNoticeTv.setVisibility(View.GONE);
                showHideBtn.setImageResource(R.drawable.ic_arrow_up);
                operationLayout.setBackgroundColor(0);
            } else {
                framesLv.setVisibility(View.VISIBLE);
                mPlayAllBtn.setVisibility(View.VISIBLE);
                operationNoticeTv.setVisibility(View.VISIBLE);
                showHideBtn.setImageResource(R.drawable.ic_arrow_down);
                operationLayout.setBackgroundColor(Color.WHITE);
            }
        });

    }

    public void preview() {
        mIsPreview = true;
        mPlayAllBtn.setTextColor(Color.parseColor("#ff5722"));
        resetPlayer();
    }

    /**
     * 播放选中的帧
     */
    private void setPlayChosen() {
        mIsPreview = false;
        resetPlayer();
        // 通知其它组件开始播放选中，有可能前面播放预览时组件隐藏了相关视图，这里通知显示出来
        EventBus.getDefault().post(EventBusConstants.GIF_PLAY_CHOSEN);
        //        mPlayAllBtn.setImageResource(R.drawable.preview_normal);
        mPlayAllBtn.setTextColor(Color.parseColor("#000000"));
    }

    /**
     * 播放指定的帧
     *
     * @param gifFrames
     */
    private void setPlayDesignated(GifFrame[] gifFrames) {
        mIsPreview = false;
        mainPlayer.resetFramesAndStart(gifFrames);
        mPlayAllBtn.setTextColor(Color.parseColor("#000000"));
    }

    public void initPtuSeeView(PtuSeeView pTuSeeView, Rect totalBound) {
        pTuSeeView.switchStatus2Gif();
        mainPlayer.setGifView(pTuSeeView);
        showGifFrames();
        pTuSeeView.setBitmapAndInit(getFirstFrameBm(), totalBound);
        pTuSeeView.post(mainPlayer::start); // 必须放到当前UI线程任务的最后，减少时间误差
        LogUtil.d("初始化加载Gig的视图完成");
    }

    private void showGifFrames() {
        WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(mContext, LinearLayout.HORIZONTAL, false);
        framesLv.setLayoutManager(layoutManager);
        framesLvAdapter = new GifFramesLvAdapter(mContext, frames);
        framesLvAdapter.setOnItemClickListener(this);
        framesLv.setAdapter(framesLvAdapter);
        int size = 0;
        if (frames != null) {
            size = frames.length;
        }
        operationNoticeTv.setText(mContext.getString(R.string.make_gif_notice));
        //        FirstUseUtil.gifGuide(mContext);
    }


    private void setChooseStateAll(boolean isChoose) {
        for (int i = 0; i < frames.length; i++) {
            frames[i].isChosen = isChoose;
        }
    }

    /**
     * 目前Gif P图区间的选择策略，
     * 提示用时着色图片表示要P的帧，点击第一张gif进行预览
     * 长按选择区间（或者点击选择？）
     *
     * 首先，对于首图，一开始进入，默认选中全部
     * 后面，长按选中区间，或者已经选中区间重新确定区间
     * 双击取消选择区间
     * 只选中一个时点击选中另一个
     *
     * 用isHeadChosen 表示的是是否处于预览状态，
     */
    /**
     * 点击的控制
     */
    @Override
    public void onItemClick(GifFramesLvAdapter.MyViewHolder itemHolder, int position) {
        int chooseCount = getFrameChooseCount();
        if (frames != null && frames.length > position && position >= 0) {
            // 如果是0个或者1个，或者双击，那么重新选中一个
            if (chooseCount <= 1 || Util.DoubleClick.isDoubleClick(1000) && position == mLastPosition) {
                setChooseStateAll(false);
                frames[position].isChosen = true;
                framesLvAdapter.notifyDataSetChanged();
                setPlayChosen();
            } else {
                setPlayDesignated(new GifFrame[]{frames[position]});
            }
            mLastPosition = position;
        }
    }

    @Override
    public boolean onItemLongClick(GifFramesLvAdapter.MyViewHolder viewHolder, int position) {
        int chosenCount = getFrameChooseCount();
        int firstChoose = getFirstChoosePosition();
        boolean hasChanged = false;

        if (chosenCount == 0) { //  没选中
            hasChanged = true;
            setChooseStateAll(false);
            frames[position].isChosen = true;
        } else if (chosenCount >= 1) {   //  选中一个或多个
            if (firstChoose != position) { // 并且位置不同
                hasChanged = true;
                setChooseStateAll(false);
                int start = firstChoose, end = position;
                if (start > end) {
                    start = position;
                    end = firstChoose;
                }
                if (start == 0 && end == frames.length - 1) {
                    setChooseStateAll(true);
                } else {
                    for (int i = start; i <= end; i++) {
                        frames[i].isChosen = true;
                    }
                }
            }
        }

        if (hasChanged) {
            framesLvAdapter.notifyDataSetChanged();
            setPlayChosen();
        } else if (mIsPreview) { // 预览状态也改为播放选中
            setPlayChosen();
        }
        return false;
    }

    private void resetPlayer() { // 注意处理部分播放和全部播放
        int frameChooseCount = getFrameChooseCount();
        //预览，帧数为空
        if (mIsPreview && frames == null) return;
        try {
            GifFrame[] playFrames = new GifFrame[mIsPreview ? frames.length : frameChooseCount];
            int i = 0;
            for (GifFrame gifFrame : frames) {
                if (mIsPreview || gifFrame.isChosen) {
                    playFrames[i++] = gifFrame;
                }
            }
            mainPlayer.resetFramesAndStart(playFrames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onPlay(int id) {
        if (mIsPreview) { // 预览的情况下，需要通知相关组件做好操作View的隐藏和显示
            if (0 <= id && id < frames.length) {
                boolean isCurChosen = frames[id].isChosen;
                if (mIsLastChosen != isCurChosen) { // 发送选中状态变化的通知
                    mIsLastChosen = isCurChosen;
                    if (isCurChosen) {
                        EventBus.getDefault().post(EventBusConstants.GIF_PLAY_CHOSEN);
                    } else {
                        EventBus.getDefault().post(EventBusConstants.GIF_PLAY_UN_CHOSEN);
                    }
                }
            }
        }
    }

    public GifPlayer getMainPlayer() {
        return mainPlayer;
    }

    public GifFrame[] getFrames() {
        return frames;
    }

    public int getFrameChooseCount() {
        int number = 0;
        if (frames != null) {
            for (int i = 0; i < frames.length; i++) {
                if (frames[i].isChosen) {
                    number++;
                }
            }
        }
        return number;
    }

    private int getFirstChoosePosition() {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].isChosen) {
                return i;
            }
        }
        return -1;
    }

    private int getLastChoosePosition() {
        for (int i = frames.length - 1; i >= 0; i--) {
            if (frames[i].isChosen) {
                return i;
            }
        }
        return -1;
    }

    public void destroy() {
        mIsDestroy = true;
        stopAnimation();
        if (framesLvAdapter != null)
            framesLvAdapter.clear();
        // Logcat.d();
        // framesLvAdapter.notifyDataSetChanged();
        if (mGifDecoder != null) {
            mGifDecoder.stopDecode();
        }
        if (frames != null) {
            for (GifFrame gifFrame : frames) {
                gifFrame.bm.recycle();
            }
        }
    }

    public void resumeAnimation() {
        if (mainPlayer != null) {
            mainPlayer.resume();
        }
    }

    public void pauseAnimation() {
        if (mainPlayer != null) {
            mainPlayer.pause();
        }

    }

    public void startAnimation() {
        if (mainPlayer != null) {
            mainPlayer.start();
        }
    }


    public void stopAnimation() {
        if (mainPlayer != null) {
            mainPlayer.stop();
        }
    }

    /**
     * 每个帧的Bitmap的数据已经写好，这里只需要合成即可
     */
    @WorkerThread
    public String saveResult(String savePath) throws Exception {
        boolean isOnlySaveChosen = false;
        // for (GifFrame gifFrame : frames) { // 提示用户
        //     if (!gifFrame.isChosen) {
        //         if (mContext instanceof Activity) {
        //             ((Activity) mContext).runOnUiThread(() -> FirstUseUtil.firstGifSave(mContext));
        //         } else {
        //             String msg = "gif保存提示用户失败";
        //             MobclickAgent.reportError(mContext, new Exception(msg));
        //             Logcat.e(msg);
        //         }
        //         break;
        //     }
        // }
        final BurstLinker burstLinker = new BurstLinker();
        try {
            burstLinker.init(frames[0].bm.getWidth(), frames[0].bm.getHeight(), savePath, 0, BurstLinker.CPU_COUNT);
            for (GifFrame gifFrame : frames) {
                // 后期添加只保存选中的帧这样的功能，现在保存全部帧
                if (!isOnlySaveChosen || (isOnlySaveChosen && gifFrame.isChosen)) {
                    burstLinker.connect(gifFrame.bm, BurstLinker.OCTREE_QUANTIZER,
                            BurstLinker.NO_DITHER, 0, 0, gifFrame.delay);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            burstLinker.release();
        }
        //发送添加图片的广播
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(savePath));
        intent.setData(uri);
        mContext.sendBroadcast(intent);
        return savePath;
    }

    /**
     * 将上层图片加入到当前选中的帧
     *
     * @param addBm
     * @param boundInPic  在gif帧图片中的范围
     * @param rotateAngle
     */
    public void addBm2Frames(@Nullable Bitmap addBm, RectF boundInPic, float rotateAngle) {
        for (GifFrame gifFrame : frames) {
            if (gifFrame.isChosen) {
                PtuUtil.addBm2Canvas(new Canvas(gifFrame.bm), addBm, boundInPic, rotateAngle);
            }
        }
    }

    /**
     * 将上层图片加入到它自己所属的帧，可能每个上层图片会加入到不同的帧中
     *
     * @param addBm
     * @param ofFrames   要画到gif上面的图片属于哪些帧，1表示属于
     * @param boundInPic 在gif帧图片中的范围
     */
    public void addBm2Frames(@Nullable Bitmap addBm, boolean[] ofFrames, RectF boundInPic, float rotateAngle) {
        for (int i = 0; i < ofFrames.length; i++) {
            if (ofFrames[i]) {
                PtuUtil.addBm2Canvas(new Canvas(frames[i].bm), addBm, boundInPic, rotateAngle);
            }
        }
    }

    public Bitmap getFirstFrameBm() {
        return frames[0].bm;
    }

    public GifFrame getCurPlayFrame() {
        if (mainPlayer.getCurPlayFrame() != null) {
            return mainPlayer.getCurPlayFrame().first;
        }
        return null;
    }

    public void notifyFrameChanged() {
        if (framesLvAdapter != null) {
            framesLvAdapter.notifyDataSetChanged();
        }
    }

    private void chooseAll() {
        for (GifFrame frame : frames) {
            frame.isChosen = true;
        }
        framesLvAdapter.notifyDataSetChanged();
    }

    /**
     * @return 获取当前gif帧列表选中的状态，返回整个帧长度的boolean数组，选中为true，否则false
     */
    public boolean[] getChosenState() {
        boolean[] chosenState = new boolean[frames.length];
        for (int i = 0; i < chosenState.length; i++) {
            chosenState[i] = frames[i].isChosen;
        }
        return chosenState;
    }

    public boolean[] getPlayState() {
        GifFrame[] playFrameList = mainPlayer.getPlayFrameList();
        boolean[] chosenState = new boolean[frames.length];
        for (int i = 0; i < playFrameList.length; i++) {
            int playId = playFrameList[i].originID;
            if (playId < 0 || playId > chosenState.length) continue;
            chosenState[playId] = true;
        }
        return chosenState;
    }


    /**
     * @return 帧时延均值
     */
    public float getMeanDaley() {
        float time = 0;
        for (GifFrame frame : frames) {
            if (!frame.isChosen) continue;
            time += frame.delay;
        }
        return time / frames.length;
    }

    /**
     * 考虑每一帧的时延并不是全部一样的情况，用每个时延加上 新旧时延均值的差 得到每一帧的新时延
     * 然后考虑不要超过最值范围
     */
    public float adjustDaley(float newValue) {
        if (LogUtil.debugGif) {
            Log.d(TAG, "adjustDaley: " + newValue);
        }
        float meanDelay = getMeanDaley();
        for (GifFrame frame : frames) {
            if (!frame.isChosen) continue;
            frame.delay += newValue - meanDelay;
            frame.delay = MathUtils.clamp(frame.delay, GifManager.MIN_FRAME_DELAY, GifManager.MAX_FRAME_DELAY);
        }
        preview();
        return getMeanDaley();
    }

    public void addFrameByUrl(String url) {
        if (FileTool.urlType(url).equals(FileTool.UrlType.OTHERS)) { // 判断是否是本地图片路径
            addFameByPath(url);
            return;
        }
        BitmapUtil.getBmPathInGlide(url, (path, msg) -> {
            if (!TextUtils.isEmpty(path)) {
                addFameByPath(path);
            } else {
                ToastUtils.show(R.string.load_pic_failed);
            }
        });
    }

    /**
     * gif图添加图片
     *
     * @param path 图片地址
     */
    private void addFameByPath(String path) {
        // TODO: 2020/10/12  目前加在选中帧的后面一帧，这样就不能加在第一帧了
        if (frames == null) return;
        GifFrame[] newFrames = new GifFrame[frames.length + 1];
        if (frames.length == 0) {// 当删除所有GIF图片帧后，再次添加时，图片列表为空，需要加载第一帧
            GifFrame addFrame = new GifFrame(BitmapUtil.decodeLossslessInSize(path, AllData.globalSettings.maxSupportGifBmSize), 500);
            newFrames[0] = addFrame;
        } else { // 在已存在的图片列表添加新图，走下面逻辑
            GifFrame addFrame = GifFrame.getSimilarFrame(frames[0], path);
            int addPos = getLastChoosePosition();
            if (addPos == -1) addPos = frames.length - 1;
            int id = 0;
            for (int i = 0; i < frames.length; i++) {
                newFrames[id] = frames[i];
                if (i == addPos) {
                    id++;
                    newFrames[id] = addFrame;
                }
                newFrames[id].originID = id; // 重新设置原始ID
                id++;
            }
        }
        frames = newFrames;
        framesLvAdapter.setData(frames);
        preview();
    }

    public void delChosenFrame() {
        int chosenCount = getFrameChooseCount();
        if (chosenCount == frames.length) {
            ToastUtils.show("请选择部分图片进行删除");
            return;
        }
        GifFrame[] newFrames = new GifFrame[frames.length - chosenCount];
        int id = 0;
        for (GifFrame frame : frames) {
            if (!frame.isChosen) {
                newFrames[id] = frame;
                newFrames[id].originID = id;
                id++;
            }
        }
        frames = newFrames;
        framesLvAdapter.setData(frames);
        preview();
    }

    public void detectFaceLandmark(FaceFeatureDetector faceFeatureDetector) {
        long start = System.currentTimeMillis();
        Log.d(TAG, "detectFaceLandmark: gif 帧数 = " + frames.length);
        for (GifFrame frame : frames) {
            float[] faceBoxes = faceFeatureDetector.detectFace(frame.bm);
            float[] baseLandmark = faceFeatureDetector.faceLandmark(frame.bm, faceBoxes);
            if (baseLandmark == null) {
                Log.d(TAG, "没有检测到人脸");
                continue;
            }
            if (LogUtil.debugFace) {
                FaceFeatureDetector.drawLandmark(frame.bm, baseLandmark);
                faceFeatureDetector.drawFaceBox(frame.bm, faceBoxes);
            }
            frame.faceLandmark = baseLandmark;
            RectF faceBox = new RectF(faceBoxes[1], faceBoxes[2], faceBoxes[3], faceBoxes[4]);
            frame.faceFeature = faceFeatureDetector.analysisFaceFeature(baseLandmark, faceBox);
        }
        Log.d(TAG, "detectFaceLandmark:gif的人脸关键点检测耗时 time = " + (System.currentTimeMillis() - start));
    }
}