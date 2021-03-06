package com.mandi.intelimeditor.ptu.changeFace;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaygoo.widget.OnRangeChangedListener;
import com.jaygoo.widget.RangeSeekBar;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PtuData;
import com.mandi.intelimeditor.ptu.common.SimpleEraser;
import com.mandi.intelimeditor.ptu.common.TietuController;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.tietu.FloatImageView;
import com.mandi.intelimeditor.ptu.view.ColorPicker;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.ptu.FunctionAdapter;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FaceChanger {
    public static final String TAG = "BaozouFaceMaker";
    private final RepealRedoListener repealRedoListener;
    private final PTuActivityInterface pTuActivityInterface;
    private final TietuController functionController;
    private Activity activity;
    private FaceChangerListener listener;

    private LevelsAdjuster levelsAdjuster;
    private boolean isMaking = false;
    private PtuSeeView ptuSeeView;

    /**
     * ??????????????????
     */
    private RecyclerView functionRcv;
    private List<FunctionInfoBean> mFunctionList;
    private FunctionAdapter mFunctionAdapter;
    private SimpleEraser bgEraser;
    private int bgEraserWidth = -1;
    private int bgEraserColor = Color.WHITE;
    private boolean isInClearBg = false;

    public FaceChanger(Activity context, TietuController functionController, PTuActivityInterface pTuActivityInterface) {
        this.activity = context;
        this.functionController = functionController;
        this.pTuActivityInterface = pTuActivityInterface;
        this.repealRedoListener = pTuActivityInterface.getRepealRedoListener();
        this.ptuSeeView = pTuActivityInterface.getPtuSeeView();
        levelsAdjuster = new LevelsAdjuster();
    }

    public void releaseResource() {

    }

    public void setListener(FaceChangerListener listener) {
        this.listener = listener;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     * ????????????????????????????????????????????????
     *
     * @param isLevelsChangeMuch ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *                           ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *                           ?????????????????????????????????
     */
    public void generateLevelsData(@Nullable Bitmap originalBm, boolean isLevelsChangeMuch) {
        if (originalBm == null) return;
        levelsAdjuster.generateData(originalBm, isLevelsChangeMuch);
    }

    public void setLevelsAdjuster(@NotNull LevelsAdjuster levelsAdjuster) {
        this.levelsAdjuster = levelsAdjuster;
    }

    public void tiaoSe(@Nullable Bitmap curBm, boolean isBmChanged, View anchor) {
        if (isBmChanged) { // ????????????bm?????????
            generateLevelsData(curBm, true);
        }
        showAdjustLevelPop(anchor);
    }

    private void showAdjustLevelPop(View anchor) {
        View adjustLayout = LayoutInflater.from(activity).inflate(R.layout.layout_adjust_levels, null);
        ThreeLevelToolUtil.showThreeLevelFunctionPop(activity, anchor, adjustLayout);
        RangeSeekBar rangeSeekBar = adjustLayout.findViewById(R.id.levels_range_seek_bar);
        rangeSeekBar.setIndicatorTextDecimalFormat("0");
        rangeSeekBar.setProgress(levelsAdjuster.shadow, levelsAdjuster.highlight);
        rangeSeekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                if (!isMaking) { // ???????????????????????????
                    adjustLevels(Math.round(leftValue), Math.round(rightValue));
                }
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
    }

    /**
     * ????????????
     * ????????????1 ????????????????????????????????????bm=A???????????????bm?????????????????????=B????????????????????????????????????????????????????????????bm?????????bm????????????
     * 2 ??????????????????????????????A???B????????????????????????????????????????????????
     * 3??????????????????????????????????????????????????????B?????????????????????????????????A,????????????????????????????????????
     * 4 ??????????????????????????????????????????bm?????????bm???????????????????????????A???B???????????????
     * 5 ???????????????????????????????????????????????????B???????????????????????????????????????A,????????????????????????????????????
     * ?????? ???????????????????????????????????????????????????B??????????????????????????????????????????????????????bm????????????????????????????????????
     *
     * @param shadow    ???????????????
     * @param highlight ???????????????
     */
    public void adjustLevels(int shadow, int highlight) {
        if (LogUtil.debugAdjustLevels) {
            Log.d(TAG, "adjustLevels: ????????? = " + shadow + "  ????????? = " + highlight);
        }
        if (highlight - shadow < 1) return;
        if (shadow < 0) return;
        if (highlight > 255) return;

        isMaking = true;
        Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    boolean result = levelsAdjuster.adjustLevel(shadow, highlight);
                    if (result) {
                        emitter.onNext(true);
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Throwable());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SimpleObserver<Object>() {

                    @Override
                    public void onNext(Object srcData) {
                        if (listener != null)
                            listener.result(levelsAdjuster.adjustedBm);
                        isMaking = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        ToastUtils.show("????????????????????????");
                        isMaking = false;
                    }
                });
    }

    public void adjustLevelsAuto() {
        if (levelsAdjuster == null) return;
        adjustLevels(levelsAdjuster.getAutoShadow(), levelsAdjuster.getAutoHighlight());
    }


    public void initFunctionList(RecyclerView mFunctionRcv) {
        int[] nameIdList = new int[]{R.string.choose_base_pic, R.string.tiao_se, R.string.rubber, R.string.clear_bg, R.string.tools};
        int[] iconIdList = new int[]{R.mipmap.choose_base, R.drawable.adjust_levels, R.mipmap.eraser, R.drawable.clear_icon, R.drawable.tools};
        mFunctionList = new ArrayList<>();
        for (int i = 0; i < nameIdList.length; i++) {
            mFunctionList.add(new FunctionInfoBean(nameIdList[i], iconIdList[i], PtuUtil.EDIT_TIETU));
        }
        this.functionRcv = mFunctionRcv;
        mFunctionAdapter = new FunctionAdapter(activity, mFunctionList);
        mFunctionAdapter.setOnClickListener((view, position, isLocked) -> {
            int title = mFunctionList.get(position).getTitleResId();
            switch (title) {
                case R.string.choose_base_pic:
                    if (listener != null) {
                        listener.switchPtuBaseChooseView();
                    }
                    US.putChangeFaceEvent(US.CHANGE_FACE_CHOOSE_BG);
                    InsertAd.onClickTarget(activity);
                    break;
                case R.string.tiao_se:
                    FloatImageView curChosenView = listener.getCurChosenView();
                    Bitmap curBm = null;
                    boolean isBmChanged = false;
                    if (curChosenView != null) {
                        curBm = curChosenView.getSrcBitmap();
                        isBmChanged = curChosenView.getLastOperation() != FloatImageView.OPERATION_MAKE_BAOZOU
                                && curChosenView.getLastOperation() != FloatImageView.OPERATION_ERASE
                                && curChosenView.getLastOperation() != FloatImageView.OPERATION_NONE;
                    }
                    if (!AllData.hasReadConfig.hasRead_changeFace_tiaose()) {
                        pTuActivityInterface.showGuideDialog(Arrays.asList("????????????", "??????"));
                        AllData.hasReadConfig.put_changeFace_tiaose(true);
                    }
                    US.putChangeFaceEvent(US.CHANGE_FACE_ADJUST_LEVELS);
                    tiaoSe(curBm, isBmChanged, view);
                    InsertAd.onClickTarget(activity);
                    break;
                case R.string.rubber:
                    if (listener != null)
                        listener.startEraseTietu();
                    if (!AllData.hasReadConfig.hasRead_changeFace_eraser()) {
                        pTuActivityInterface.showGuideDialog(Arrays.asList("????????????", "??????"));
                        AllData.hasReadConfig.put_changeFace_eraser(true);
                    }
                    US.putChangeFaceEvent(US.CHANGE_FACE_ERASE);
                    InsertAd.onClickTarget(activity);
                    break;
                case R.string.clear_bg:
                    if (!AllData.hasReadConfig.hasRead_changeFace_eraseBg()) {
                        pTuActivityInterface.showGuideDialog(Arrays.asList("???????????????", "??????"));
                        AllData.hasReadConfig.put_changeFace_eraseBg(true);
                    }
                    clearBg();
                    US.putChangeFaceEvent(US.CHANGE_FACE_ERASE_BG);
                    InsertAd.onClickTarget(activity);
                    break;
                case R.string.tools:
                    listener.showTools(view);
                    US.putChangeFaceEvent(US.CHANGE_FACE_TOOLS);
                    InsertAd.onClickTarget(activity);
                    break;
            }
        });
        mFunctionRcv.setAdapter(mFunctionAdapter);
        mFunctionRcv.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
    }

    private void clearBg() {
        if (!PtuData.ptuConfig.hasReadTextRubber()) {
            final FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
            firstUseDialog.createDialog("??????????????????,????????????????????????????????????", "", () -> {
                PtuData.ptuConfig.writeConfig_TextRubber(true);
            });
        }
        switchClearBgFunction();
    }

    private void switchClearBgFunction() {
        int[] nameIdList = new int[]{R.string.size, R.string.text_color, R.string.finish};
        int[] iconIdList = new int[]{R.mipmap.fixed_size, R.drawable.ic_color_lens, R.drawable.ic_tick};
        mFunctionList = new ArrayList<>();
        for (int i = 0; i < nameIdList.length; i++) {
            mFunctionList.add(new FunctionInfoBean(nameIdList[i], iconIdList[i], PtuUtil.EDIT_TIETU));
        }
        mFunctionAdapter = new FunctionAdapter(activity, mFunctionList);

        bgEraser = new SimpleEraser(activity, pTuActivityInterface);
        if (bgEraserWidth > 0) {
            bgEraser.setRubberWidth(bgEraserWidth);
            bgEraser.setColor(bgEraserColor);
        }
        bgEraser.setRepealRedoListener(repealRedoListener);
        listener.addClearBgView(bgEraser);
        mFunctionAdapter.setOnClickListener((view, position, isLocked) -> {
            int title = mFunctionList.get(position).getTitleResId();
            switch (title) {
                case R.string.size:
                    showClearBgSizePop(view, bgEraser);
                    break;
                case R.string.text_color:
                    setColorPopWindow(view, bgEraser);
                    break;
                case R.string.finish:
                    finishClearBg();
                    break;
            }
        });
        functionRcv.setAdapter(mFunctionAdapter);
        isInClearBg = true;
    }

    private void finishClearBg() {
        drawRubber(bgEraser.getResultData());
        ((ViewGroup) ptuSeeView.getParent()).removeView(bgEraser);
        bgEraser.finishErase();
        initFunctionList(functionRcv); // ?????????????????????????????????
        FloatImageView curChosenView = listener.getCurChosenView();
        if (curChosenView != null) {
            curChosenView.setVisibility(View.VISIBLE);
        }
        isInClearBg = false;
    }

    private void cancelClearBg() {
        initFunctionList(functionRcv); // ?????????????????????????????????
        ((ViewGroup) ptuSeeView.getParent()).removeView(bgEraser);
        bgEraser.cancelErase();
        FloatImageView curChosenView = listener.getCurChosenView();
        if (curChosenView != null) {
            curChosenView.setVisibility(View.INVISIBLE);
        }
        isInClearBg = false;
    }

    private void drawRubber(List<Pair<Path, Paint>> pathPaintList) {
        GifManager gifManager = pTuActivityInterface.getGifManager();
        List<Canvas> canvasList = new ArrayList<>();
        if (gifManager == null) {
            canvasList.add(ptuSeeView.getSourceCanvas());
        } else {
            GifFrame[] gifFrames = gifManager.getFrames();
            for (GifFrame gifFrame : gifFrames) {
                if (gifFrame.isChosen) {
                    canvasList.add(new Canvas(gifFrame.bm));
                }
            }
        }

        if (pathPaintList != null) {//??????????????????
            for (Canvas canvas : canvasList) {
                for (Pair<Path, Paint> pair : pathPaintList) {
                    canvas.drawPath(pair.first, pair.second);
                }
            }
        }
        ptuSeeView.invalidate();
    }


    //??????
    void setColorPopWindow(View v, SimpleEraser eraser) {
        ColorPicker colorPicker = new ColorPicker(activity);
        colorPicker.setColorTarget(new ColorPicker.ColorTarget() {
            @Override
            public void setColor(int color) {
                eraser.setColor(color);
                bgEraserColor = color;
            }

            @Override
            public int getCurColor() {
                return eraser.getColor();
            }
        });
        colorPicker.addViewToGetColor(ptuSeeView, ptuSeeView.getSourceBm(),
                ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        colorPicker.setAbsorbListener(new ColorPicker.AbsorbListener() {
            @Override
            public void startAbsorb(ColorPicker colorPicker) {
                if (!AllData.hasReadConfig.hasRead_absorb()) {
                    FirstUseDialog firstUseDialog = new FirstUseDialog(activity);
                    firstUseDialog.createDialog("????????????", "?????????????????????????????????????????????????????????????????????????????????", new FirstUseDialog.ActionListener() {
                        @Override
                        public void onSure() {
                            AllData.hasReadConfig.put_absorb(true);
                        }
                    });
                }
            }

            @Override
            public boolean stopAbsorbColor() {
                return false;
            }
        });
        ColorPicker.showInDefaultLocation(activity, colorPicker, v.getHeight() + Util.dp2Px(5), v.getRootView());
    }

    void showClearBgSizePop(View v, SimpleEraser eraser) {
        View contentView = LayoutInflater.from(activity).inflate(R.layout.seek_bar_layout, null);
        RangeSeekBar seekBar = contentView.findViewById(R.id.seek_bar_popw);
        TextView valueTv = contentView.findViewById(R.id.seek_bar_value_tv);
        int width = ptuSeeView.getDstRect().width() / 2;
        seekBar.setRange(0, width != 0 ? width : 100);
        seekBar.setProgress(eraser.getRubberWidth());
        valueTv.setText(String.valueOf(eraser.getRubberWidth()));
        seekBar.setOnRangeChangedListener(new OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
                eraser.setRubberWidth((int) leftValue);
                bgEraserWidth = (int) leftValue;
                valueTv.setText(String.valueOf(bgEraserWidth));
            }

            @Override
            public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }

            @Override
            public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

            }
        });
//        seekBar.setOnRangeChangedListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                eraser.setRubberWidth(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//            }
//        });
        ThreeLevelToolUtil.showThreeLevelFunctionPop(activity, v, contentView);
    }

    public Bitmap getOriginalBm() {
        return levelsAdjuster.originalBm;
    }

    public boolean smallRepeal() {
        if (isInClearBg) {
            bgEraser.smallRepeal();
            return true;
        }
        return false;
    }

    public boolean smallRedo() {
        if (isInClearBg) {
            bgEraser.smallRedo();
            return true;
        }
        return false;
    }

    public boolean onBackPressed() {
        if (isInClearBg) {
            cancelClearBg();
            return true;
        }
        if (Util.DoubleClick.isDoubleClick(1000)) {
            return false;
        } else {
            ToastUtils.show("????????????????????????????????????");
            return true;
        }
    }

    public boolean onSure() {
        if (isInClearBg) {
            finishClearBg();
            return true;
        }
        judgeSaveBaozouFace();
        return false;
    }

    private void judgeSaveBaozouFace() {
        // ????????????????????????????????????????????????
        if (functionController != null && functionController.needSaveTietu) {
            String picPath = FileTool.createPicPathWithTag(".png", MyDatabase.CHANGE_FACE_FACE_TAG, BitmapUtil.PIC_SUFFIX_PNG);
            Bitmap finalTietuBm = listener.getFinalTietuBm();
            if (picPath != null && finalTietuBm != null) {
                BitmapUtil.SaveResult saveResult = BitmapUtil.saveBitmap(activity, finalTietuBm, picPath);
                if (!BitmapUtil.SaveResult.SAVE_RESULT_FAILED.equals(saveResult.result)) {
                    try {
                        AllData.getThreadPool_single().execute(() ->
                                MyDatabase.getInstance().insertMyTietu(picPath, System.currentTimeMillis()));
                    } catch (Exception e) {
                        // nothing
                    }
                    ToastUtils.show("??????????????????????????????????????????", Toast.LENGTH_LONG);
                }
            }
        }
    }

    public interface FaceChangerListener {
        void result(Bitmap bm);

        void startEraseTietu();

        void switchPtuBaseChooseView();

        @Nullable
        FloatImageView getCurChosenView();

        void addClearBgView(SimpleEraser eraser);

        void showTools(View anchor);

        @Nullable
        Bitmap getFinalTietuBm();
    }
}
