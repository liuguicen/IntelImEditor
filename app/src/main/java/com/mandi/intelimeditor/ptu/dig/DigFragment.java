package com.mandi.intelimeditor.ptu.dig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.DigController;
import com.mandi.intelimeditor.ptu.repealRedo.DigStepData;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.R;


import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by caiyonglong on 2019/4/4.
 */
public class DigFragment extends BasePtuFragment {
    private static final String TAG = "DigFragment";
    public static final int EDIT_MODE = PtuUtil.EDIT_DIG;

    public static final float DEFAULT_BLUR_RADIUS_DIVIDE = 35f;
    public static final float MAX_BLUR_RADIUS_MULTIPLE = 4.5f;
    private int mBlurRadius;
    private int mMaxBlurRadius;
    private SeekBar mBlurRadiusSb;
    private PTuActivityInterface pTuActivityInterface;
    private View mPreviewLayout;
    private TextView mPreviewTv;
    private ImageView mPreviewIv;
    private View mBlurRadiusBtn;
    private ViewGroup blurRadiusLayout;
    private FrameLayout pTuParentLayout;
    private DigActionListener digActionListener;
    private DigController functionController;

    @Override
    public int getEditMode() {
        return EDIT_MODE;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_dig;
    }

    private Context mContext;
    private DigView digView;
    private PtuSeeView ptuSeeView;
    private RepealRedoListener repealRedoListener;

    public void initBeforeCreateView(DigController functionController) {
        this.functionController = functionController;
    }

    @Override
    public void initView() {
        super.initView();
        mContext = getActivity();
        mPreviewLayout = rootView.findViewById(R.id.dig_preview);
        mPreviewLayout.setOnClickListener(v -> {
            US.putPTuDigEvent(US.PTU_DIG_PREVIEW);
            switchPreview();
        });
        mPreviewTv = mPreviewLayout.findViewById(R.id.dig_preview_tv);
        mPreviewIv = mPreviewLayout.findViewById(R.id.dig_preview_iv);
        mBlurRadiusBtn = rootView.findViewById(R.id.dig_blur_radius);
        mBlurRadiusBtn.setOnClickListener(v -> {
            if (pTuParentLayout.indexOfChild(blurRadiusLayout) >= 0) {
                pTuParentLayout.removeView(blurRadiusLayout);
            } else {
                US.putPTuDigEvent(US.PTU_DIG_BLUR_RADIUS);
                FirstUseUtil.digBLurRadiusGuide(getActivity());
                showBlurRadiusPop();
            }
        });
        showBlurRadiusPop();
        if (functionController != null && !functionController.isShowChangeFace)
            rootView.findViewById(R.id.go_change_face).setVisibility(View.GONE);
        rootView.findViewById(R.id.go_change_face).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (digActionListener != null) {
                    pTuActivityInterface.showLoading("?????????...");
                    Observable
                            .create(((ObservableOnSubscribe<StepData>) emitter -> {
                                Pair<StepData, Bitmap> resultData = getResultData(1);
                                if (resultData == null) {  // ??????????????????????????????????????????????????????
                                    DigStepData dsd = new DigStepData(PtuUtil.EDIT_DIG);
                                    dsd.picPath = pTuActivityInterface.getBasePicPath();
                                    dsd.hasTransparency = true;
                                    emitter.onNext(dsd);
                                } else {
                                    emitter.onNext(resultData.first);
                                }
                                emitter.onComplete();
                            }))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SimpleObserver<StepData>() {
                                @Override
                                public void onNext(StepData sd) {
                                    pTuActivityInterface.dismissLoading();
                                    // ??????????????????????????????
                                    US.putMainFunctionEvent(US.MAIN_FUNCTION_CHANGE_FACE);
                                    US.putChangeFaceEvent(US.CHANGE_FACE_DIG_ENTER);
                                    US.putPTuDigEvent(US.MAIN_FUNCTION_CHANGE_FACE); // ???dig????????????????????????
                                    digActionListener.toMakeBaozouFace(sd);
                                }

                                @Override
                                public void onError(Throwable e) {  // emitter?????????NUll???????????????
                                    super.onError(e);
                                    ToastUtils.show("????????????????????????????????????");
                                    pTuActivityInterface.dismissLoading();
                                }
                            });
                }
            }
        });
    }

    private void switchPreview() {
        if (!digView.isInPreview()) {
            preview();
        } else {
            reset();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FirstUseUtil.digGuide(mContext);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Fragment???????????????View???????????????????????????????????????
        // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
        /*if (rootView != null) {
            ViewGroup parentView = (ViewGroup) rootView.getParent();
            if (parentView != null) {
                parentView.removeView(rootView);
            }
        }*/
    }

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        if (ptuActivity != null) {
            this.ptuSeeView = ptuActivity.getPtuSeeView();
            pTuParentLayout = (FrameLayout) ptuSeeView.getParent();
            this.repealRedoListener = ptuActivity.getRepealRedoListener();
            this.repealRedoListener.canRepeal(false);
            this.repealRedoListener.canRedo(false);
        }
    }

    public void setDigActionListener(DigActionListener digActionListener) {
        this.digActionListener = digActionListener;
    }

    @Override
    public boolean onSure() {
        return false;
    }


    private void preview() {
        digView.setIsPreView(true);
        mPreviewTv.setText(mContext.getString(R.string.reset));
        mPreviewIv.setImageResource(R.mipmap.reset);
    }

    private void reset() {
        digView.setIsPreView(false);
        mPreviewTv.setText(mContext.getString(R.string.preview));
        mPreviewIv.setImageResource(R.drawable.preview_normal);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void smallRepeal() {
        if (digView.isInPreview())
            digView.setIsPreView(false);
        else
            digView.smallRepeal();
    }

    @Override
    public void smallRedo() {
        digView.smallRedo();
    }

    public Pair<StepData, Bitmap> getResultData(float ratio) {
        if (!digView.hasOperation()) {
            return null;
        }
        if (pTuActivityInterface.getGifManager() != null) {
            digView.digGif(pTuActivityInterface.getGifManager());
            return null;
        }
        //????????????
        DigStepData dsd = new DigStepData(PtuUtil.EDIT_DIG);
        dsd.isGoChooseBase = functionController != null ? functionController.isShowChangeBase : true;
        Bitmap resultBm = digView.getResultBm();
        //?????????????????????
        String tempPath = FileTool.createTempPicPath();
        AllData.getPTuBmPool().putBitmap(tempPath, resultBm);
        // ?????????????????????????????????PNG
        BitmapUtil.asySaveTempBm(tempPath, resultBm, BitmapUtil.PIC_SUFFIX_PNG, new SimpleObserver<String>() {
            @Override
            public void onNext(String realPath) {
                AllData.getPTuBmPool().replaceUrl(tempPath, realPath);
                dsd.picPath = realPath;
            }
        });
        dsd.picPath = tempPath;
        dsd.hasTransparency = true;
        return new Pair<>(dsd, resultBm);
    }

    @Override
    public void generateResultDataInMain(float ratio) {
        // gif??????????????????????????????????????????????????????gif???????????????
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ?????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????????????????????????????
        if (pTuActivityInterface.getGifManager() != null) {
            pTuActivityInterface.getGifManager().stopAnimation();
        }
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        Pair<StepData, Bitmap> pair = getResultData(ratio);
        if (pair == null) return null;
        //????????????
        ptuSeeView.post(() -> ptuSeeView.replaceSourceBm(pair.second));
        return pair.first;
    }

    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        Bitmap bitmap = AllData.getPTuBmPool().get(sd.picPath);
        if (bitmap == null) {
            bitmap = BitmapUtil.decodeLosslessBitmap(sd.picPath);
        }
        Bitmap finalBitmap = bitmap;
        ptuSeeView.post(() -> ptuSeeView.replaceSourceBm(finalBitmap));
    }

    @Override
    public void releaseResource() {
        pTuParentLayout.removeView(blurRadiusLayout);
        digView.releaseResource();
    }

    public DigView createDigView(Context context, Rect bound, Bitmap sourceBitmap) {
        digView = new DigView(context, ptuSeeView);
        if (sourceBitmap == null) { // TODO: 2019/5/30 ?????????????????????????????????????????????????????????????????????
            mBlurRadius = 0;
        } else {
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            // ?????????????????????????????????bound???????????????????????????bound?????????????????????????????????
            mBlurRadius = (int) (bound.width() / DEFAULT_BLUR_RADIUS_DIVIDE);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "createDigView: ???????????? = " + mBlurRadius);
            }
        }
        digView.setRepealRedoListener(pTuActivityInterface.getRepealRedoListener());
        mMaxBlurRadius = (int) (mBlurRadius * MAX_BLUR_RADIUS_MULTIPLE);
        digView.setBlurRadius(mBlurRadius);
        digView.setPTuActivityInterface(pTuActivityInterface);
        return digView;
    }

    private void showBlurRadiusPop() {
        // final BlurRadiusPopupWindow popupWindow = new BlurRadiusPopupWindow(getContext());
        blurRadiusLayout = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.popwindow_dig_blur_radius, null);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        pTuParentLayout.addView(blurRadiusLayout, layoutParams);
        SeekBar mBlurRadiusSb = blurRadiusLayout.findViewById(R.id.dig_sb_blur_radius);
        mBlurRadiusSb.setMax(mMaxBlurRadius); // ????????????????????????????????????????????????????????????????????????????????????????????????
        TextView mBlurTv = blurRadiusLayout.findViewById(R.id.tv_blur);
        mBlurTv.setText(getString(R.string.blur_radius_value, (int) digView.getBlurRadius()));
        mBlurRadiusSb.setProgress((int) digView.getBlurRadius());
        mBlurRadiusSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                digView.setBlurRadius(progress);
                mBlurTv.setText(getString(R.string.blur_radius_value, progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekBar.getProgress() < 1) {
                    seekBar.setProgress(1);
                }
            }
        });
        // popupWindow.setPopWindow_for3LevelFunction(parent, viewGroup);
    }

    public interface DigActionListener {
        void toMakeBaozouFace(StepData sd);
    }
}
