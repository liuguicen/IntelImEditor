package com.mandi.intelimeditor.ptu.tietu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.bean.GifPlayFrameEvent;
import com.mandi.intelimeditor.common.CommonConstant;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.common.view.PtuConstraintLayout;
import com.mandi.intelimeditor.dialog.BottomTietuListDialog;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PicGestureListener;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.changeFace.ChangeFaceUtil;
import com.mandi.intelimeditor.ptu.changeFace.FaceChanger;
import com.mandi.intelimeditor.ptu.common.PtuBaseChooser;
import com.mandi.intelimeditor.ptu.common.SimpleEraser;
import com.mandi.intelimeditor.ptu.common.TietuController;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.imageProcessing.FaceAlign;
import com.mandi.intelimeditor.ptu.imageProcessing.FaceFeature;
import com.mandi.intelimeditor.ptu.imageProcessing.FaceFeatureDetector;
import com.mandi.intelimeditor.ptu.imageProcessing.StyleTransferMnn;
import com.mandi.intelimeditor.ptu.rendpic.RendDrawDate;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.repealRedo.TietuStepData;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultData;
import com.mandi.intelimeditor.ptu.threeLevelFunction.ThreeLevelToolUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraser;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraserUI;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.dialog.UnlockDialog;
import com.mandi.intelimeditor.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/1.
 * 贴图功能
 * 注意！！！
 * 贴图Fragment不止贴图功能，还包含换脸，抠图选底图功能，开发，测试，修改时都需要考虑到
 */
public class TietuFragment extends BasePtuFragment {
    //  注意！！！ 贴图Fragment不止贴图功能，还包含换脸，抠图选底图功能，开发，测试，修改时都需要考虑到
    public static final String INTENT_EXTRA_CHOSE_TIETU_RES = "com.mandi.intelimeditor.tietu_res";

    private static String TAG = "TietuFragment";
    private TietuFrameLayout tietuLayout;
    public static final boolean isAutoAddWhenSure = false;

    Context mContext;

    /**
     * 撕图的功能布局
     */
    private View rendFunctionLayout;
    private View eraserFunctionLayout;

    private PtuSeeView ptuSeeView;
    public String curCategory = "-----------";
    // 本地贴图第一次加载显示不了，尝试多种办法不行，目前只能用这个
    private boolean isFirstShowTietu;
    private RecyclerView tietuRcv;

    private PTuActivityInterface pTuActivityInterface;
    private PtuBaseChooser ptuBaseChooser;
    private TietuController funcControl;
    private RepealRedoManager<List<FloatImageView>> rrManager;

    @NotNull
    private RepealRedoListener repealRedoListener;
    private int flip = 0;
    private FaceChanger faceChanger;
    private FaceFeatureDetector faceFeatureDetector;
    private BottomTietuListDialog tietuListDialog;
    private TietuRecyclerAdapter tietuListAdapter;
    private long toolWindowDismissTime = 0;

    /*********************************************基础功能**********************************************/

    public void initBeforeCreateView(final PtuFrameLayout ptuFrame, final PtuSeeView ptuSeeView, @Nullable TietuController funcControl) {
        setTietuLayout(ptuFrame.initAddImageFloat(new Rect(
                ptuFrame.getLeft(), ptuFrame.getTop(), ptuFrame.getRight(), ptuFrame.getBottom()
        )));
        // gif的二级按钮，已经不使用了
        //        if (mPTuActivityInterface.getGifManager() != null) {
        //            mSecondarySureBtn = ptuFrame.addSecondarySureBtn();
        //            FirstUseUtil.tietuGifSecondarySureGuide((FragmentActivity) mPTuActivityInterface);
        //            mSecondarySureBtn.setOnClickListener(v -> {
        //                onLockTietu();
        //            });
        //        }
        this.funcControl = funcControl;
    }

    private void setTietuLayout(TietuFrameLayout tietuLayout) {
        this.tietuLayout = tietuLayout;
        tietuLayout.setOnTietuAddRemoveListener(
                new TietuFrameLayout.TietuChangeListener() {
                    @Override
                    public void onTietuRemove(FloatImageView view) {
                        // if (tietuLayout.getChildCount() == 0) {
                        //     rendBtn.setVisibility(View.GONE);
                        // }
                        refreshRepealRedoView();
                    }

                    @Override
                    public void onTietuAdd(FloatImageView view) {
                        // rendBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void lockFloatView(@NotNull FloatImageView floatImageView) {
                        onLockTietu(floatImageView);
                    }

                    @Override
                    public void onClickTools(FloatImageView curChosenView) {
                        // 出现一个问题是，只要点击贴图就调用这个方法，无法达到第二次点击popwindow消失的效果
                        // 因为手指down的时候就会消失，比较难判断到底前面window是那种状态，目前是用时间判断，
                        // 如果消失间隔时间小于点击事件判断时间，说明是因为点击的down消失的，就不显示
                        if (System.currentTimeMillis() - toolWindowDismissTime > PicGestureListener.clickInternalTime + 100) {
                            showTietuTools(pFunctionRcv);
                        }
                    }
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.fragment_tietu;
    }

    @Override
    public void initView() {
        super.initView();
        EventBus.getDefault().register(this);

        rendFunctionLayout = rootView.findViewById(R.id.rend_pic_function_layout);
        eraserFunctionLayout = rootView.findViewById(R.id.view_eraser_function_layout);


        rendFunctionLayout.setOnTouchListener((v, event) -> true); // 屏蔽触摸事件
        rootView.findViewById(R.id.rend_pic_finish).setOnClickListener(v -> finishRendPic());
        rootView.findViewById(R.id.rend_pic_cancel).setOnClickListener(v -> cancelRendPic());

        //底部的贴图列表
        tietuRcv = new RecyclerView(mContext);
        tietuRcv.setTag(PtuConstraintLayout.TAG_TIETU_RCV);

        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(mContext, TietuRecyclerAdapter.DEFAULT_ROW_NUMBER,
                GridLayoutManager.HORIZONTAL, false);
        tietuRcv.setLayoutManager(gridLayoutManager);
        isFirstShowTietu = true;
        tietuListAdapter = new TietuRecyclerAdapter(mContext, true);
        tietuListAdapter.setOnItemClickListener(tietuRecyclerListener);
        if (funcControl != null && funcControl.isChangeFace) {
            init_changeFace_multiType(funcControl);
        }
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (funcControl == null || (!funcControl.needChooseBase && !funcControl.isChangeFace))
            showBottomTietuListDialog(1);
    }

    /**
     * 采用RecyclerView的形式处理，因为贴图相当于是app的核心操作，会在多种情景下用到，下面的功能列表需要灵活变换
     */
    private void initFunctionIconList() {
        pFunctionList.clear();
        if (funcControl != null && funcControl.needChooseBase) {
            ptuBaseChooser = new PtuBaseChooser(mContext, this, pTuActivityInterface, null);
            ptuBaseChooser.setIsUpdateHeat(false);
            ptuBaseChooser.show();
            pFunctionList.add(new FunctionInfoBean(R.string.choose_base_pic, R.mipmap.choose_base, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
            addTietuByPath(funcControl.tietuUrl, null);
        }
        //        mFunctionList.add(new FunctionInfoBean(R.string.property, R.mipmap.baojian, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
        pFunctionList.add(new FunctionInfoBean(R.string.expression, R.mipmap.tietu, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
        pFunctionList.add(new FunctionInfoBean(R.string.my_tietu, R.mipmap.my_tietu, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
        pFunctionList.add(new FunctionInfoBean(R.string.fuse, R.mipmap.synthesis, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
        pFunctionList.add(new FunctionInfoBean(R.string.stretch, R.drawable.stretch_icon, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
        pFunctionList.add(new FunctionInfoBean(R.string.tools, R.drawable.tools, R.drawable.function_background_tietu_green, PtuUtil.EDIT_TIETU));
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        initFunctionIconList();
        return pFunctionList;
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        super.onItemClick(adapter, view, position);
        if (pFunctionList.get(position).isLocked()) {
            UnlockDialog unlockDialog = UnlockDialog.Companion.newInstance();
            unlockDialog.setUnlockListener(result -> {
                LogUtil.d(TAG, "video result $result");
                if (result) {
                    pFunctionList.get(position).setLocked(false);
                    pFunctionAdapter.notifyItemChanged(position);
                }
                return null;
            });
            unlockDialog.showIt(getActivity());
            return;
        }
        if (pFunctionList == null) return; // 未知原因，用户手机端报空
        int title = pFunctionList.get(position).getTitleResId();
        switch (title) {
            //            case R.string.property:
            //                onClickProperty(view);
            //                break;
            case R.string.expression:
                showBottomTietuListDialog(-1);
                break;
            case R.string.my_tietu:
                onClickMy(view);
                break;
            case R.string.title_choose_local:
                onClickMore();
                break;
            case R.string.tools:
                showTietuTools(view);
                break;
            case R.string.fuse:
                onClickFuse();
                break;
            case R.string.stretch:
                stretchTietu();
                break;
            case R.string.choose_base_pic:
                if (ptuBaseChooser != null) {
                    ptuBaseChooser.show();
                }
                break;
            default:
                break;
        }
    }

    private void init_changeFace_multiType(@NonNull TietuController funcControl) {
        pTuActivityInterface.addUsedTags(false, null);
        BitmapFactory.Options options = TietuSizeController.getFitWh(funcControl.tietuUrl, pTuActivityInterface.getGifManager() != null);
        if (options == null) {
            ToastUtils.show("获取贴图失败");
            return;
        }
        Glide.with(IntelImEditApplication.appContext).asBitmap().load(funcControl.tietuUrl).into(
                new CustomTarget<Bitmap>(options.outWidth, options.outHeight) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap srcBitmap, @Nullable Transition<? super Bitmap> transition) {
                        Log.d(TAG, "onResourceReady: " + srcBitmap.getWidth() + " " + srcBitmap.getHeight());
                        if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0) {
                            ToastUtils.show("获取贴图失败");
                            return;
                        }

                        initFor_ChangeFace(funcControl, srcBitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void initFor_ChangeFace(@NonNull TietuController funcControl, Bitmap srcBitmap) {
        faceChanger = new FaceChanger(getActivity(), funcControl, pTuActivityInterface);
        // 注意调用顺序，接口设置需要的贴图的ImageView初始化之后
        faceChanger.setListener(
                new FaceChanger.FaceChangerListener() {
                    @Override
                    public void result(Bitmap resultBm) {
                        FloatImageView curChosenView = tietuLayout.getCurChosenView();
                        if (curChosenView == null) {
                            curChosenView = initAndAddTietu(resultBm, funcControl.tietuUrl,
                                    ptuSeeView.getPicBound().width() * 2 / 5, (int) (ptuSeeView.getPicBound().height() * 0.5 / 5f),
                                    -1, -1,
                                    0,
                                    true, false);
                        } else {
                            curChosenView.setImageBitmap(resultBm);
                        }
                        curChosenView.setLastOperation(FloatImageView.OPERATION_MAKE_BAOZOU);
                    }

                    @Override
                    public void startEraseTietu() {
                        TietuFragment.this.startEraseTietu(true);
                    }

                    @Override
                    public void switchPtuBaseChooseView() {
                        if (ptuBaseChooser == null) {
                            initBgChooser_ForChangeFace(false);
                        } else {
                            ptuBaseChooser.switchPtuBaseChooseView();
                        }
                    }

                    @Override
                    public FloatImageView getCurChosenView() {
                        return tietuLayout.getCurChosenView();
                    }

                    @Override
                    public void addClearBgView(SimpleEraser eraser) {
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ptuSeeView.getDstRect().width(), ptuSeeView.getDstRect().height());
                        params.setMargins(ptuSeeView.getDstRect().left, ptuSeeView.getDstRect().top, 0, 0);
                        FrameLayout parent = (FrameLayout) pTuActivityInterface.getPtuSeeView().getParent();
                        parent.addView(eraser, params);
                        FloatImageView curChosenView = tietuLayout.getCurChosenView();
                        if (curChosenView != null) {
                            curChosenView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void showTools(View view) {
                        showTietuTools(view);
                    }

                    @Override
                    public Bitmap getFinalTietuBm() {
                        FloatImageView curChosenView = tietuLayout.getCurChosenView();
                        if (curChosenView == null) {
                            View childAt = tietuLayout.getChildAt(tietuLayout.getChildCount() - 1);
                            if (childAt instanceof FloatImageView) {
                                curChosenView = ((FloatImageView) childAt);
                                return curChosenView.getSrcBitmap();
                            } else {
                                return null;
                            }
                        } else {
                            return curChosenView.getSrcBitmap();
                        }
                    }
                });
        // 需要调整，传入bm，计算出并更新bm相关数据，之后调用自动调整
        if (funcControl.needAdjustLevel) {
            faceChanger.generateLevelsData(srcBitmap, true);
            faceChanger.adjustLevelsAuto();
        } else if (AllData.levelsAdjuster != null) { // 不需要调整，已经调整好了，直接设置bm相关数据
            faceChanger.setLevelsAdjuster(AllData.levelsAdjuster);
            AllData.levelsAdjuster = null;
            addTietuByMultiType(srcBitmap, null);
        } else { // 不需要调整，且没有调整好，准备数据，不自动调整
            faceChanger.generateLevelsData(srcBitmap, true);
            addTietuByMultiType(srcBitmap, null);
        }
        if (funcControl.needChooseBase) {
            initBgChooser_ForChangeFace(true);
        }
        faceChanger.initFunctionList(pFunctionRcv);
    }

    private void initBgChooser_ForChangeFace(boolean isChoseAuto) {
        List<String> priorTagList = new ArrayList<String>() {{
            add("熊猫头-无脸");
            addAll(ChangeFaceUtil.changeFaceTagList);
        }}; // 指定一张图， 再过滤出支持换年的图
        ptuBaseChooser = new PtuBaseChooser(mContext, this, pTuActivityInterface, priorTagList);
        ptuBaseChooser.setIsUpdateHeat(false);
        ptuBaseChooser.setChooseBgAuto(isChoseAuto);
        ptuBaseChooser.show();
    }


    @Override
    public void smallRepeal() {
        if (tietuLayout.smallRepeal()) {
            return;
        }
        if (faceChanger != null && faceChanger.smallRepeal()) {
            return;
        }
        // 为了避免混乱，只有当贴图没被选中时才支持Fragement的撤销重做
        if (rrManager != null && rrManager.canRepeal()) {
            List<FloatImageView> fivList = rrManager.getCurrentStepDate();
            for (FloatImageView fiv : fivList) {
                tietuLayout.removeFloatView(fiv);
            }
            rrManager.repealPrepare();
            refreshRepealRedoView();
        }
    }

    public void smallRedo() {
        if (tietuLayout.smallRedo()) {
            return;
        }
        if (faceChanger != null && faceChanger.smallRedo()) {
            return;
        }
        // 为了避免混乱，只有当贴图没被选中时才支持Fragement的撤销重做
        if (tietuLayout.getCurChosenView() == null
                && rrManager != null && rrManager.canRedo()) {
            List<FloatImageView> redoList = rrManager.redo();
            for (int i = 0; i < redoList.size(); i++) {
                FloatImageView redoFiv = redoList.get(i);
                if (redoFiv != null) {
                    if (redoFiv.getParent() != null) {
                        ((ViewGroup) redoFiv.getParent()).removeView(redoFiv);
                    }
                    tietuLayout.addView(redoFiv, tietuLayout.getChildCount()); // 相当于top
                }
            }
            refreshRepealRedoView();
        }
    }

    @Override
    public void generateResultDataInMain(float ratio) {
        int count = tietuLayout.getChildCount();
        if (count == 0) return;
        if (tietuLayout.isInRend()) {
            tietuLayout.finishRend();
        }
    }

    /**
     * 获取结果，因为会有多个贴图，所以返回的 {@link TietuStepData} 里面放的是{@link StepData}的链表
     *
     * @return {@link StepData}
     */
    @Override
    public StepData getResultDataAndDraw(float ratio) {
        int count = tietuLayout.getChildCount();
        if (ptuBaseChooser != null) {
            ptuBaseChooser.updateDefault();
        }
        if (count == 0) return null;

        GifManager gifManager = pTuActivityInterface.getGifManager();
        TietuStepData tsd = null;
        if (gifManager == null) {
            tsd = new TietuStepData(PtuUtil.EDIT_TIETU);
        }

        for (int i = 0; i < count; i++) {
            //获取数据
            FloatImageView fiv = (FloatImageView) tietuLayout.getChildAt(i);

            //每个tietu的范围
            RectF boundInPic = new RectF();
            float[] realLocation = PtuUtil.getLocationAtBaseBm(fiv.getLeft() + FloatImageView.PAD, fiv.getTop() + FloatImageView.PAD,
                    ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
            boundInPic.left = realLocation[0];
            boundInPic.top = realLocation[1];

            realLocation = PtuUtil.getLocationAtBaseBm(fiv.getRight() - FloatImageView.PAD, fiv.getBottom() - FloatImageView.PAD,
                    ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
            boundInPic.right = realLocation[0];
            boundInPic.bottom = realLocation[1];
            Bitmap tietuBm = fiv.getSrcBitmap();

            // 旋转角度
            float rotateAngle = fiv.getRotation();

            if (gifManager == null) {
                // 设置撤销重做数据
                String tempPath = FileTool.createTempPicPath();  // 暂存Bm到sd卡上面
                final TietuStepData.OneTietu oneTietu = new TietuStepData.OneTietu(tempPath, boundInPic, rotateAngle);
                BitmapUtil.asySaveTempBm(tempPath, tietuBm, new SimpleObserver<String>() {
                    @Override
                    public void onNext(@NotNull String realPath) {
                        Log.d(TAG, "onNext: 保存完成" + Thread.currentThread().getName());
                        oneTietu.setPicPath(realPath);
                    }
                });
                tsd.addOneTietu(oneTietu);

                // 绘制结果到ptuView内的Bitmap上面
                PtuUtil.addBm2Canvas(ptuSeeView.getSourceCanvas(), tietuBm, boundInPic, rotateAngle);
            } else {
                // 绘制结果
                boolean[] ofFrames = fiv.getOfFrames();
                // fiv没有设置选中那些帧，那么使用当前选中的帧，比如用户直接点击了大的sure，
                // 没有lockTietu，这时候不会fiv里面就没设置
                ofFrames = ofFrames != null ? ofFrames : gifManager.getPlayState();
                gifManager.addBm2Frames(tietuBm, ofFrames, boundInPic, rotateAngle);
            }
        }
        ptuSeeView.postInvalidate();
        return tsd;

    }

    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        TietuStepData ttsd = (TietuStepData) sd;
        Iterator<TietuStepData.OneTietu> iterator = ttsd.iterator();
        while (iterator.hasNext()) {
            TietuStepData.OneTietu oneTietu = iterator.next();
            Bitmap tietuBm = BitmapUtil.getLosslessBitmap(oneTietu.getPicPath());
            Log.e(TAG, "addBigStep: 重做贴图" + tietuBm);
            PtuUtil.addBm2Canvas(ptuSeeView.getSourceCanvas(), tietuBm, oneTietu.getBoundRectInPic(), oneTietu.getRotateAngle());
        }
        ptuSeeView.postInvalidate();
    }

    private void refreshRepealRedoView() {
        if (rrManager == null) {
            rrManager = new RepealRedoManager<>(100);
        }
        repealRedoListener.canRedo(rrManager.canRedo());
        repealRedoListener.canRepeal(rrManager.canRepeal());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this); // 再反注册一次，防止前面没有反注册到
    }

    @Override
    public void releaseResource() {
        int count = tietuLayout.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            ((FloatImageView) tietuLayout.getChildAt(i)).releaseResource();
            tietuLayout.removeViewAt(i);
        }
        if (ptuBaseChooser != null) {
            ptuBaseChooser.releaseResources();
            ptuBaseChooser = null;
        }
        tietuLayout = null;
        rendFunctionLayout = null;
        if (faceChanger != null) {
            faceChanger.releaseResource();
            faceChanger = null;
        }
    }

    @Override
    public boolean onBackPressed(boolean isFromKey) {
        if (tietuLayout.isInRend()) {
            cancelRendPic();
            return true;
        }

        if (tietuLayout.isInErase()) {
            FloatImageView curChosenView = tietuLayout.getCurChosenView();
            if (curChosenView != null && curChosenView.onBackPressed()) { // 不能取消的情况
                ToastUtils.show("请再滑一次取消");
                return true;
            }
            cancelErase();
            return true;
        }

        if (faceChanger != null) {
            return faceChanger.onBackPressed();
        }

        if (tietuLayout.getChildCount() > 3 && Util.DoubleClick.isDoubleClick(1000)) {
            return true;
        }
        return false;
    }


    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
        this.repealRedoListener = ptuActivity.getRepealRedoListener();
    }

    @Override
    public boolean onSure() {
        if (faceChanger != null && faceChanger.onSure()) {
            return true;
        }
        if (eraserFunctionLayout.getVisibility() == View.VISIBLE) {
            finishErase();
            return true;
        }
        if (rendFunctionLayout.getVisibility() == View.VISIBLE) {
            finishRendPic();
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_TIETU && data != null) {
            PicResource picResource = (PicResource) data.getSerializableExtra(INTENT_EXTRA_CHOSE_TIETU_RES);
            addTietuByMultiType(picResource.getUrlString(), picResource.getTag());
        }
        if (requestCode == PtuActivity.REQUEST_CODE_MAKE_TIETU && resultCode == PtuActivity.RESULT_CODE_INTERMEDIATE_PTU
                && data != null) {
            String picPath = data.getStringExtra(PTuResultData.NEW_PIC_PATH);
            MyDatabase.getInstance().insertMyTietu(picPath, System.currentTimeMillis());
            addTietuByMultiType(picPath, null);
        }

        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_BASE && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(PtuActivity.INTENT_EXTRA_CHOSE_BASE_PIC_RES);
            pTuActivityInterface.replaceBase(picRes.getUrlString());
            pTuActivityInterface.addUsedTags(true, picRes.getTag());
        }

        // 开通会员解锁, 开通成功后隐藏列表，用户重新点击，重新将列表加入adapter，一尺排除广告数据
        if (resultCode == CommonConstant.RESULT_CODE_OPEN_VIP_SUCCESS) {
            if (AllData.isVip) {
                // TODO: 2020/10/22 隐藏贴图列表对话框
                tietuListDialog.dismissAllowingStateLoss();
                curCategory = "-------------";
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /******************************************添加贴图相关*****************************************/
    public void onClickMore() {
        //        InsertAd.onClickTarget(getActivity());//
        US.putPTuTietuEvent(US.PTU_TIETU_MORE);
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setAction(HomeActivity.PTU_ACTION_CHOOSE_TIETU);
        intent.putExtra(HomeActivity.INTENT_EXTRA_FRAGMENT_ID, HomeActivity.LOCAL_FRAG_ID);
        startActivityForResult(intent, PtuActivity.REQUEST_CODE_CHOOSE_TIETU);
    }

    /**
     * 点击表情弹出底部对话框
     *
     * @param index 默认选中位置
     */
    private void showBottomTietuListDialog(int index) {
        if (tietuListDialog == null) {
            tietuListDialog = BottomTietuListDialog.newInstance(index);
        }
        //防止抛出 java.lang.IllegalStateException: Fragment already added:
        if (!tietuListDialog.isAdded()) {
            if (index != -1) { //点击表情，进入上次选中的tab，否则进入我的或者热门列表
                tietuListDialog.setSelectIndex(index);
            }
            tietuListDialog.show(getChildFragmentManager(), "BottomTietuListDialog");
        } else {
            LogUtil.d(TAG, "tietuListDialog 已经add，不要重复show");
        }
    }

    public void hideBottomTietuListDialog() {
        if (tietuListDialog != null) {
            tietuListDialog.dismissAllowingStateLoss();
        }
    }


    private void onClickMy(View view) {
        InsertAd.onClickTarget(getActivity());
        US.putPTuTietuEvent(US.PTU_TIETU_MY);
        FirstUseUtil.myTietuGuide(mContext);
        prepareSomeTietu(view, PicResource.SECOND_CLASS_MY);
        if (isFirstShowTietu) { // 本地贴图第一次加载显示不了，尝试多种办法不行，目前只能用这个
            view.post(() -> prepareSomeTietu(view, PicResource.SECOND_CLASS_MY));
            view.post(() -> prepareSomeTietu(view, PicResource.SECOND_CLASS_MY));
        }
        isFirstShowTietu = false;
    }

    private void prepareSomeTietu(View view, String category) {
        if (curCategory.equals(category) && tietuRcv.getParent() != null) { // 再次点击同一个类别，隐藏视图
            ((ViewGroup) tietuRcv.getParent()).removeView(tietuRcv);
            curCategory = "-------------";
            return;
        }
        ViewParent parent = view.getParent();
        while (parent != null && !(parent instanceof PtuConstraintLayout)) {
            parent = parent.getParent();
        }
        if (!(parent instanceof PtuConstraintLayout)) {
            return;
        }
        PtuConstraintLayout ptuFrameLayout = (PtuConstraintLayout) parent;

        if (ptuFrameLayout.indexOfChild(tietuRcv) == -1) { //
            ptuFrameLayout.addPicResourceLv(tietuRcv);
            tietuRcv.setAdapter(tietuListAdapter);
        }
        Observable
                .create(PicResourceDownloader::queryMyTietu)
                .subscribe(new SimpleObserver<List<PicResource>>() {

                    @Override
                    public void onError(Throwable throwable) {
                        if (isDetached()) {
                            return;
                        }
                        onNoTietu(category);
                        LogUtil.e(throwable.getMessage());
                        tietuListAdapter.setList(new ArrayList<>());
                    }

                    @Override
                    public void onNext(List<PicResource> tietumaterials) {
                        if (isDetached()) {
                            return;
                        }
                        int size = tietumaterials.size();
                        if (size == 0) {
                            onNoTietu(category);
                            return;
                        }
                        Log.d("TAG", "onNext: 获取到的贴图数量" + size);
                        tietuListAdapter.setList(tietumaterials);
                    }
                })
        ;
        curCategory = category;
    }

    private RcvItemClickListener1 tietuRecyclerListener = new RcvItemClickListener1() {
        @Override
        public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
            int position = itemHolder.getLayoutPosition();
            if (position == -1) return;
            if (tietuListAdapter != null) {
                PicResource oneTietu = tietuListAdapter.get(position).data;
                if (oneTietu != null && oneTietu.getUrl() != null) {
                    String url = oneTietu.getUrl().getUrl();
                    ViewGroup parent = (ViewGroup) tietuRcv.getParent();
                    FirstUseUtil.tietuGuide(mContext);
                    addTietuByMultiType(url, oneTietu.getTag());
                    MyDatabase.getInstance().updateMyTietu(url, System.currentTimeMillis());
                    // 移除选择列表视图
                    // 这里一开始出了bug，视频播放可能长达30秒，出现变化，直接getParent为空了
                    if (parent != null) {
                        parent.removeView(tietuRcv);
                    }
                    curCategory = "-------------";
                } else {
                    Log.e(this.getClass().getSimpleName(), "点击贴图后获取失败");
                }
            }
        }
    };

    public void onNoTietu(String category) {
        if (tietuListAdapter != null) { // 这个方法会异步回调，此时tietuListAdapter已经回收置空了，原来自己就没处理，GG
            tietuListAdapter.setList(new ArrayList<>());
            String msg;
            if (PicResource.SECOND_CLASS_MY.equals(category)) {
                msg = mContext.getString(R.string.no_my_tietu_notice);
            } else {
                msg = mContext.getString(R.string.no_network_tietu_notice);
            }
            PtuUtil.onNoPicResource(msg);
        }
    }


    public void addTietuByMultiType(final Object obj, @Nullable String tietuTags) {
        pTuActivityInterface.addUsedTags(false, tietuTags);
        if (obj instanceof Bitmap) {
            initAndAddTietu((Bitmap) obj, tietuTags,
                    TietuSizeController.TIETU_POSITION_RANDOM, TietuSizeController.TIETU_POSITION_RANDOM,
                    -1, -1,
                    0,
                    true, pTuActivityInterface.getGifManager() != null);
            return;
        }

        if (obj instanceof String) {
            if (FileTool.urlType((String) obj).equals(FileTool.UrlType.OTHERS)) { // 判断是否是本地图片路径
                addTietuByPath((String) obj, tietuTags);
                return;
            }
        }
        BitmapUtil.getBmPathInGlide(obj, (path, msg) -> {
            if (!TextUtils.isEmpty(path)) {
                addTietuByPath(path, tietuTags);
            } else {
                ToastUtils.show(R.string.load_tietu_failed);
            }
        });
    }

    private void addTietuByPath(String path, @Nullable String tietuTags) {
        BitmapFactory.Options options = TietuSizeController.getFitWh(path, pTuActivityInterface.getGifManager() != null);
        if (options == null) {
            ToastUtils.show("获取贴图失败");
            return;
        }
        Glide.with(IntelImEditApplication.appContext).asBitmap().load(path).into(new CustomTarget<Bitmap>(options.outWidth, options.outHeight) {
            @Override
            public void onResourceReady(@NonNull Bitmap srcBitmap, @Nullable Transition<? super Bitmap> transition) {
                Log.d(TAG, "onResourceReady: " + srcBitmap.getWidth() + " " + srcBitmap.getHeight());
                if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0) {
                    ToastUtils.show("获取贴图失败");
                    return;
                }

                initAndAddTietu(srcBitmap, tietuTags,
                        TietuSizeController.TIETU_POSITION_RANDOM, TietuSizeController.TIETU_POSITION_RANDOM,
                        -1, -1,
                        0,
                        true,
                        pTuActivityInterface.getGifManager() != null);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
    //  注意！！！ 贴图Fragment不止贴图功能，还包含换脸，抠图选底图功能，开发，测试，修改时都需要考虑到

    /**
     * @param width         内部图的宽高，不是总的View的宽高, 负数自动确定
     * @param left,top      贴图位置，{@link TietuSizeController#TIETU_POSITION_RANDOM}等等
     * @param angle         旋转角
     * @param add2rrManager 是否添加到撤销重做
     */
    private FloatImageView initAndAddTietu(@NotNull Bitmap bm, String tietuTags,
                                           int left, int top,
                                           int width, int height,
                                           float angle,
                                           boolean add2rrManager,
                                           boolean isAnalysisFace) {

        FloatImageView fiv = new FloatImageView(mContext, repealRedoListener);
        fiv.setScaleType(ImageView.ScaleType.FIT_XY);
        fiv.setImageBitmap(bm);
        fiv.setTietuTags(tietuTags);

        FrameLayout.LayoutParams params;
        if (width > 0 && height > 0) {
            params = new FrameLayout.LayoutParams(width + FloatImageView.PAD * 2, height + FloatImageView.PAD * 2);
            params.leftMargin = left;
            params.topMargin = top;
        } else {
            params = TietuSizeController.getFeatParams(bm.getWidth(), bm.getHeight(),
                    ptuSeeView.getPicBound(), left, top);
            width = params.width - FloatImageView.PAD * 2;
        }
        fiv.bmScaleRatio = (float) width / bm.getWidth();
        fiv.setRotation(angle);

        if (tietuLayout != null) { // TODO: 2019/5/30 这里用户端出现几个空指针，不知为何
            tietuLayout.addView(fiv, params);
            if (add2rrManager) {
                commit2rrManager(Collections.singletonList(fiv));
            }
        }
        return fiv;
    }

    /**
     * 当前这张锁定之后自动添加同样的一张，大小和旋转角度相同，位置在当前图片左上角，方便用户快速拖动然后立即就能贴图
     * 便于gif放置多张同样的但是位置，角度等有所变化的贴图
     */
    private void addSameTietu(FloatImageView lockedView) {
        FirstUseUtil.tietuAutoAddTietuNotice((FragmentActivity) mContext);

        FloatImageView newTietuView = new FloatImageView(mContext, repealRedoListener);
        newTietuView.copyDataFrom(lockedView);
        newTietuView.setAdjustViewBounds(true);


        int newLeft = Math.max(ptuSeeView.getPicBound().left, lockedView.getLeft() - lockedView.getWidth() + FloatImageView.PAD); // 左上角，减掉之后两个pad间距，只留一个pad间距
        int newTop = Math.max(ptuSeeView.getPicBound().top, lockedView.getTop() - lockedView.getHeight() + FloatImageView.PAD);
        FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(lockedView.getWidth(), lockedView.getHeight());
        paras.leftMargin = newLeft;
        paras.topMargin = newTop;

        // Log.e(TAG, "添加位置" + params.leftMargin + " " + params.topMargin);
        tietuLayout.addView(newTietuView, paras);
        commit2rrManager(Collections.singletonList(newTietuView));
    }

    /**
     * 将贴图锁定到底图上面，没有实际的画上去，便于后面做变动，给用户的感觉是像画上去了一样
     *
     * @param lockedView
     */
    private void onLockTietu(@NotNull FloatImageView lockedView) {
        if (lockedView == null) return;
        GifManager gifManager = pTuActivityInterface.getGifManager();
        if (!AllData.hasReadConfig.hasRead_fuseBaoZouFace() && gifManager == null) { // 对于暴走脸，让用户尝试融合效果
            if (lockedView.getTietuTags() != null && lockedView.getTietuTags().contains("暴走")) {
                pTuActivityInterface.showGuideDialog(Collections.singletonList("融合"));
                ToastUtils.show("试试融合功能，贴图效果更好哦");
                fuseBaoZouFace();
                AllData.hasReadConfig.put_fuseBaoZouFace(true);
            }
        }
        if (gifManager != null && !lockedView.isAutoAdd()) {
            // 自动加
            if (isAutoAddWhenSure && SPUtil.get_isGifAutoAddOn()) { // 第一种方案 点击sure直接添加的方案, 需要开关打开
                detectFaceAutoAdd2Gif(lockedView);
            } else if (!isAutoAddWhenSure &&
                    !AllData.hasReadConfig.hasRead_gifAutoAddEffect() && faceFeatureDetector == null) {
                // 第二种方案, 用户点击按钮才自动加，这里首次使用展示效果
                // 如果检测过人脸faceFeatureDetector != null，就不要再进入了，防止没检测出人脸，没有自动添加，然后标志位没有设置，一直走这个地方，用户看不到贴图效果
                detectFaceAutoAdd2Gif(lockedView);
            } else { // 不自动加
                // 所见即所得，当前播放的帧列表添加，而不是选中的帧，也不是全部的帧
                lockedView.setOfGifFrames(gifManager.getPlayState());
                lockedView.toLocked();
                addSameTietu(lockedView);
            }
        } else {
            lockedView.toLocked();
            tietuLayout.unChoseCurTietu();
        }
    }

    private void commit2rrManager(List<FloatImageView> fivList) {
        if (rrManager == null) {
            rrManager = new RepealRedoManager<>(100);
        }
        rrManager.commit(fivList);
        int currentIndex = rrManager.getCurrentIndex();
        for (int id = currentIndex + 1; id < rrManager.getSize(); id++) {
            List<FloatImageView> discardList = rrManager.getStepdata(id);
            for (FloatImageView fiv : discardList) {
                tietuLayout.removeFloatView(fiv);
            }
        }
        refreshRepealRedoView();
    }

    /****************************************具体的子功能*******************************************/

    void test() {
        new Handler().postDelayed(() -> {
            Log.e(TAG, "执行测试切换");
            addTietuByPath("/storage/emulated/0/test.png", null);
        }, 500);
    }

    // 调色,这个方式不行，已经不用了，此处代码留着看下C++调用方式
    // Bitmap bitmap = new PictureSynthesis().
    //         changeBm(underBm, aboveBm,
    //                 new Rect(dstLeft, dstTop,
    //                         dstLeft + chosenTietu.getWidth() - FloatImageView.PAD * 2,
    //                         dstTop + chosenTietu.getHeight() - FloatImageView.PAD * 2));

    private void showTietuTools(View anchor) {
        if (isAutoAddWhenSure) {

        }
        List<Integer> iconIdList = new ArrayList<>(Arrays.asList(R.mipmap.eraser,/* R.mipmap.synthesis,*/ R.mipmap.rend_pic,
                R.drawable.make_tietu, R.drawable.flip, R.drawable.stretch_icon));
        List<Integer> nameIdList = new ArrayList<>(Arrays.asList(R.string.rubber, /*R.string.fuse,*/ R.string.rend_pic, R.string.make, R.string.cut_flip, R.string.stretch));
        if (pTuActivityInterface.getGifManager() != null) {
            if (isAutoAddWhenSure) {
                if (SPUtil.get_isGifAutoAddOn()) {
                    iconIdList.add(R.drawable.on);
                } else {
                    iconIdList.add(R.drawable.off);
                }
            } else {
                iconIdList.add(R.drawable.ic_auto);
            }
            nameIdList.add(R.string.auto_add);
        }
        RcvItemClickListener1 rcvItemClickListener = new RcvItemClickListener1() {
            @Override
            public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
                int position = itemHolder.getLayoutPosition();
                if (position < 0) return;
                InsertAd.onClickTarget(getActivity());
                switch (nameIdList.get(position)) {
                    case R.string.rubber:
                        startEraseTietu(false);
                        break;
                    case R.string.fuse:
                        onClickFuse();
                        break;
                    case R.string.rend_pic:
                        toRendTietu();
                        break;
                    case R.string.cut_flip:
                        toFlipTietu();
                        break;
                    case R.string.make:
                        toMakeTietu();
                        break;
                    case R.string.stretch:
                        stretchTietu();
                        break;
                    case R.string.auto_add:
                        US.putPTuTietuEvent(US.PTU_TIETU_AUTO_ADD);
                        detectFaceAutoAdd2Gif(tietuLayout.getCurChosenView());// 另一种方案，用户点击再自动跟踪，而不是直接自动跟踪
                        //                        switchGifAutoAdd();
                }
            }
        };
        PopupWindow popupWindow = ThreeLevelToolUtil.showToolsRcvWindow(getContext(),
                anchor,
                2,
                rcvItemClickListener,
                iconIdList, R.drawable.function_background_text_yellow,
                nameIdList);
        popupWindow.setOnDismissListener(() -> toolWindowDismissTime = System.currentTimeMillis());
        //        PtuUtil.setPopWindow_for3LevelFunction(popupWindow, anchor, layout);5
    }

    private void switchGifAutoAdd() {
        if (SPUtil.get_isGifAutoAddOn()) {
            US.putPTuTietuEvent(US.PTU_TIETU_CLOSE_AUTO);
            SPUtil.putGifAutoAdd(false);
            ToastUtils.show("已关闭", Toast.LENGTH_SHORT);
            showTietuTools(pFunctionRcv);
        } else {
            US.putPTuTietuEvent(US.PTU_TIETU_OPEN_AUTO);
            SPUtil.putGifAutoAdd(true);
            ToastUtils.show("已开启", Toast.LENGTH_SHORT);
            showTietuTools(pFunctionRcv);
        }
    }

    private void onClickFuse() {
        US.putPTuTietuEvent(US.PTU_TIETU_FUSE);
        if (!AllData.hasReadConfig.hasRead_fuseBaoZouFace_1()) { // 对于暴走脸，让用户尝试融合效果
            pTuActivityInterface.showGuideDialog(Collections.singletonList("融合教程"));
            AllData.hasReadConfig.put_fuseBaoZouFace_1(true);
        }
        if (faceChanger != null) {
            US.putChangeFaceEvent(US.PTU_TIETU_FUSE);
        }
        fuseBaoZouFace();
    }

    private void fuseBaoZouFace() {
        FloatImageView choseFiv = tietuLayout.getCurChosenView();
        if (choseFiv != null) {
            choseFiv.fuseBaoZouFace(ptuSeeView);
        } else {
            GifManager gifManager = pTuActivityInterface.getGifManager();
            if (rrManager != null && gifManager != null) {
                fuseForGif();
            } else {
                ToastUtils.show("请先选择一张贴图");
            }
        }
    }

    private void fuseForGif() {
        pTuActivityInterface.showLoading("处理中...");
        Observable
                .create((ObservableOnSubscribe<List<Bitmap>>) emitter -> {
                    List<FloatImageView> fivList = rrManager.getCurrentStepDate();
                    if (fivList == null) {
                        emitter.onError(new Exception(""));
                        return;
                    }

                    GifFrame[] frames = pTuActivityInterface.getGifManager().getFrames();
                    List<Bitmap> newBmList = new ArrayList<>();
                    for (int i = 0; i < fivList.size(); i++) {
                        FloatImageView gifFiv = fivList.get(i);
                        int ofId = 0;
                        boolean[] ofFrames = gifFiv.getOfFrames();
                        if (ofFrames == null) continue;
                        for (int ofi = 0; ofi < ofFrames.length; ofi++) {
                            if (ofFrames[i]) {
                                ofId = ofi; //获取当前fiv属于的帧
                                break;
                            }
                        }
                        newBmList.add(gifFiv.fuseBaoZouFaceNonUI(frames[ofId].bm, ptuSeeView));
                    }
                    emitter.onNext(newBmList);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Bitmap>>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull List<Bitmap> bmList) {
                        pTuActivityInterface.dismissLoading();
                        List<FloatImageView> fivList = rrManager.getCurrentStepDate();
                        for (int i = 0; i < bmList.size(); i++) {
                            Bitmap bitmap = bmList.get(i);
                            if (bitmap != null) {
                                fivList.get(i).setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        pTuActivityInterface.dismissLoading();
                        Log.e(TAG, "onError: gif自动添加的融合出错");
                        ToastUtils.show("融合出错了", Toast.LENGTH_SHORT);
                        e.printStackTrace();
                        super.onError(e);
                    }
                });
    }

    private void toMakeTietu() {
        US.putPTuTietuEvent(US.PTU_TIETU_MAKE);
        startActivity2make();
    }

    /**
     * 翻转贴图
     */
    private void toFlipTietu() {
        US.putPTuTietuEvent(US.PTU_TIETU_FLIP);
        tietuLayout.flipTietu(flip);
        flip = flip == 0 ? 1 : 0;
    }

    private void stretchTietu() {
        US.putPTuTietuEvent(US.PTU_TIETU_STRETCH);
        FloatImageView chosenTietu = tietuLayout.getCurChosenView();
        if (chosenTietu != null) {
            if (!chosenTietu.isInStretch()) chosenTietu.switchStretchStatus(true);
            else chosenTietu.switchStretchStatus(false);
        } else {
            ToastUtils.show("请选择一张贴图");
        }
    }

    private void startActivity2make() {
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setAction(PtuActivity.PTU_ACTION_MAKE_TIETU);
        startActivityForResult(intent, PtuActivity.REQUEST_CODE_MAKE_TIETU);
    }

    private void toRendTietu() {
        InsertAd.onClickTarget(getActivity());
        FirstUseUtil.rendGuide(mContext);
        US.putPTuTietuEvent(US.PTU_TIETU_REND);
        startRendPic();
    }

    private void startRendPic() {
        if (tietuLayout != null && tietuLayout.startRendPic()) {
            pFunctionRcv.setVisibility(View.GONE);
            rendFunctionLayout.setVisibility(View.VISIBLE);
            if (!AllData.hasReadConfig.hasRead_rendGuide()) {
                AllData.hasReadConfig.put_rendGuide(true);
                pTuActivityInterface.showGuideDialog(Collections.singletonList("撕图"));
            }
        } else { // 开启撕图失败
            LogUtil.e("开启贴图模式失败！");
            ToastUtils.show("请选择一张贴图");
        }
    }

    private void finishRendPic() {
        // 将碎片放到一个贴图中的形式，目前不采用这种形式，代码要保留，重写很麻烦的
        //        tietuLayout.finishRend();
        //        rendFunctionLayout.setVisibility(View.GONE);
        //        mFunctionRcv.setVisibility(View.VISIBLE);
        // 将碎片放到一个贴图中的形式，目前不采用这种形式，代码要保留，重写很麻烦的

        rendFunctionLayout.setVisibility(View.GONE);
        pFunctionRcv.setVisibility(View.VISIBLE);
        FloatImageView curChosenView = tietuLayout.getCurChosenView();

        if (curChosenView == null) {
            return;
        }
        RendDrawDate rdd = curChosenView.getRendDrawData();

        if (!curChosenView.isIn_moveFrag() || rdd == null) {
            curChosenView.finishAllRend();
            return;
        }

        // RendDrawDate 这里面保存了bm碎片和位置，直接拿到放到贴图中即可
        for (int i = 0; i < rdd.fragBoundInLayout.length; i++) {
            if (rdd.fragBm[i] == null) continue;
            MRect bound = rdd.fragBoundInLayout[i];
            initAndAddTietu(rdd.fragBm[i], "",
                    (int) (bound.left - FloatImageView.PAD), (int) (bound.top - FloatImageView.PAD),
                    bound.widthInt(), bound.heightInt(),
                    0,
                    true, false);

        }
        tietuLayout.removeFloatView(curChosenView);
    }

    private void cancelRendPic() {
        rendFunctionLayout.setVisibility(View.GONE);
        pFunctionRcv.setVisibility(View.VISIBLE);
        tietuLayout.cancelRend();
    }

    private void startEraseTietu(boolean isMakeBaozouFace) {
        InsertAd.onClickTarget(getActivity());
        FirstUseUtil.rendGuide(mContext);
        US.putPTuTietuEvent(US.PTU_TIETU_ERASE);
        if (tietuLayout != null && tietuLayout.startEraseTietu(pTuActivityInterface, isMakeBaozouFace)) {
            FloatImageView curChosenView = tietuLayout.getCurChosenView();
            if (curChosenView == null) return;
            eraserFunctionLayout.setVisibility(View.VISIBLE);
            pFunctionRcv.setVisibility(View.GONE);
            new ViewEraserUI(mContext, curChosenView,
                    new ViewEraserUI.EraseUIInterface() {
                        @Override
                        public void finishErase() {
                            TietuFragment.this.finishErase();
                        }

                        @Override
                        public int getPaintWidth() {
                            ViewEraser eraser = tietuLayout.getCurTietuEraser();
                            if (eraser != null) {
                                return (int) eraser.getPaintWidth();
                            } else {
                                return 1;
                            }
                        }

                        @Override
                        public int getBlurWidth() {
                            ViewEraser eraser = tietuLayout.getCurTietuEraser();
                            if (eraser != null) {
                                return (int) eraser.getBlurWidth();
                            } else {
                                return 1;
                            }
                        }

                        @Override
                        public void setEraserBlurWidth(float width) {
                            ViewEraser eraser = tietuLayout.getCurTietuEraser();
                            if (eraser != null) {
                                eraser.setBlurWidth(width);
                            }
                        }

                        @Override
                        public void setEraserWidth(float progress) {
                            ViewEraser eraser = tietuLayout.getCurTietuEraser();
                            if (eraser != null) {
                                eraser.setPaintWidth(progress);
                            }
                        }
                    }, eraserFunctionLayout);
        } else { // 开启失败
            LogUtil.e("开启橡皮模式失败！");
            ToastUtils.show("请选择一张贴图");
        }
        if (!AllData.hasReadConfig.hasRead_tietuErase()) {
            pTuActivityInterface.showGuideDialog(Collections.singletonList("贴图橡皮"));
            AllData.hasReadConfig.put_tietuErase(true);
        }
    }

    private void finishErase() {
        // 特殊情况，制作暴走脸，擦掉的是调整之后的图，但是调整最好是基于调整之前的图，所以这里将调整之前的原始图做同样的
        // 擦除操作，然后用原始图来调整色阶，如果用新图来调整色阶，因为新图很多颜色已经丢失，是不行的
        if (faceChanger != null && tietuLayout.getCurChosenView() != null) {
            ViewEraser tietuEraser = tietuLayout.getCurChosenView().getTietuEraser();
            if (tietuEraser != null) {
                Bitmap erasedBm = BitmapUtil.eraseBmByPath(faceChanger.getOriginalBm(), tietuEraser.getOperateList(), true);
                faceChanger.generateLevelsData(erasedBm, false);
            }
        }
        tietuLayout.finishEraseTietu();
        eraserFunctionLayout.setVisibility(View.GONE);
        pFunctionRcv.setVisibility(View.VISIBLE);
    }

    private void cancelErase() {
        tietuLayout.cancelEraseTietu();
        eraserFunctionLayout.setVisibility(View.GONE);
        pFunctionRcv.setVisibility(View.VISIBLE);
    }

    /**********************************人脸对齐相关 *************************************/

    //
    private void analysisTietuFace(@NotNull FaceFeatureDetector faceFeatureDetector, FloatImageView fiv) {
        Bitmap tietuBm = fiv.getSrcBitmap();
        if (!tietuBm.isMutable()) { // 变成可更改的，防止多个fiv内部重复做这个操作
            tietuBm = tietuBm.copy(tietuBm.getConfig(), true);
        }
        float[] boxes = faceFeatureDetector.detectFace(tietuBm);
        float[] faceLandmark = faceFeatureDetector.faceLandmark(tietuBm, boxes);
        fiv.faceBoxex = boxes;
        if (fiv.faceBoxex == null) {
            fiv.faceBoxex = new float[0];//表示检测过了
        }
        fiv.faceLandmark = faceLandmark;
    }


    private void test_drawTietuLandmark(float[] tietuLandmark, FloatImageView fiv) {
        Bitmap newbm = fiv.getSrcBitmap();
        if (!newbm.isMutable()) {
            newbm = newbm.copy(newbm.getConfig(), true);
        }
        FaceFeatureDetector.drawLandmark(newbm, tietuLandmark);
        fiv.setImageBitmap(newbm);
    }

    private void alignByLandmark(FloatImageView floatImageView, TietuFrameLayout tietuLayout,
                                 float[] tietuLandmark, float[] baseLandmark) {
        MPoint tietuEye = FaceFeatureDetector.kp2Point(tietuLandmark, FaceFeatureDetector.KP_L_EYE);
        MPoint baseEye = FaceFeatureDetector.kp2Point(baseLandmark, FaceFeatureDetector.KP_L_EYE);
        float[] alignPara = FaceAlign.align(tietuLandmark, baseLandmark);
        floatImageView.post(() -> {
            // 旋转缩放= 大小和角度对齐，
            tietuLayout.rotate(0, 0, alignPara[0]);
            // 缩放比例注意，输出的是bm上的，要转换到View上，贴图和底图View都要考虑
            tietuLayout.scale(0, 0, alignPara[1] / floatImageView.bmScaleRatio
                    * ptuSeeView.getTotalRatio());

            // 移动 = 位置对齐
            MPoint tietuEyeView = PtuUtil.bmPosition2FloatImageView(tietuEye, floatImageView);
            MPoint baseEyeInView = PtuUtil.bmPosition2PtuSeeView(baseEye, ptuSeeView);
            tietuEyeView.add_(floatImageView.getLeft(), floatImageView.getTop());
            MPoint move = baseEyeInView.sub(tietuEyeView);
            tietuLayout.move(move.x, move.y, false);
        });
    }

    /**
     * 检测人脸，并根据人脸自动添加贴图到gif
     * 非同步调用
     */
    private void detectFaceAutoAdd2Gif(FloatImageView fiv) {
        if (fiv == null) {
            ToastUtils.show("请选择一张贴图");
            return;
        }
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                            // 检测gif底图里面的人脸
                            if (faceFeatureDetector == null) {
                                faceFeatureDetector = new FaceFeatureDetector(mContext);
                                GifManager gifManager = pTuActivityInterface.getGifManager();
//                                if (gifManager != null) {
//                                    gifManager.detectFaceLandmark(faceFeatureDetector);
//                                }
                                StyleTransferMnn styleTransferMnn = new StyleTransferMnn(mContext);
                                styleTransferMnn.mnnTransfer(gifManager.getFirstFrameBm(), gifManager.getFirstFrameBm());
                            }
                            // 检测贴图上面的人脸
                            if (fiv.faceBoxex == null) {
                                analysisTietuFace(faceFeatureDetector, fiv);
                            }
                            emitter.onNext("");
                            emitter.onComplete();
                        }
                )
                .subscribeOn(Schedulers.computation()) // 注意是计算线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String res) {
                        gifAutoAdd(fiv);
                    }
                });
    }


    /**
     * gif里面根据人脸对齐贴图
     * 两种情况，1、贴图包含人脸，自动对齐
     * 2、贴图不包含人脸，根据贴图相对底图中人脸的位置进行对齐
     *
     * @return 是否null表示，是否自动添加了贴图
     */
    private List<FloatImageView> gifAutoAdd(FloatImageView fiv) {
        if (fiv == null) {
            ToastUtils.show("请选择一张贴图");
            return null;
        }
        Bitmap tietuBm = fiv.getSrcBitmap();
        GifManager gifManager = pTuActivityInterface.getGifManager();
        GifFrame[] frames = gifManager.getFrames();

        FaceFeature tietuFaceFeature = null;
        MPoint tietuEye = null;

        // 物品和人脸对齐需要的
        GifFrame startFrame = null;
        int[] nearPointId = null;

        List<FloatImageView> fivList = null;

        for (int i = 0; i < frames.length; i++) {
            GifFrame frame = frames[i];
            //            if (!frame.isChosen) continue; 暂时不采用
            if (frame.faceLandmark == null) continue;

            // 这个方法有点复杂，这一段相当于初始化，待优化
            if (tietuEye == null && nearPointId == null) {
                fivList = new ArrayList<>();
                if (fiv.faceBoxex != null && fiv.faceBoxex.length >= 5) { // 贴图上有人脸
                    smallRepeal();
                    tietuEye = FaceFeatureDetector.kp2Point(fiv.faceLandmark, FaceFeatureDetector.KP_L_EYE);
                    RectF faceBox = new RectF(fiv.faceBoxex[1], fiv.faceBoxex[2], fiv.faceBoxex[3], fiv.faceBoxex[4]);
                    tietuFaceFeature = FaceFeatureDetector.analysisFaceFeature(fiv.faceLandmark, faceBox);
                    if (LogUtil.debugFace) {
                        test_drawTietuLandmark(fiv.faceLandmark, fiv);
                    }
                } else {
                    Log.d(TAG, " 贴图中没有检测到人脸");
                    //                        那么此时使用物品对人脸的方式对齐
                    //                        首先获取人脸初始的大小和角度
                    // 不包含人脸的贴图，需要根据用户操作的位置进行，不能完全自动化
                    // 所以是根据与当前显示的帧的人脸相当位置进行贴图

                    // 因为是根据人脸贴图，前面没有人脸的图都不用加，所以在这里初始化是可以的
                    GifFrame showingFrame = gifManager.getCurPlayFrame();
                    if (showingFrame != null && showingFrame.faceLandmark != null) { // 如果显示的帧里面没有人脸， 不自动添加
                        startFrame = showingFrame;
                    } else {
                        return fivList;
                    }
                    float[] kp = FaceFeatureDetector.get6Landmark(frame.faceLandmark);
                    nearPointId = FaceAlign.getNearPoint(kp, fiv);
                }
            }

            FloatImageView addedFiv;
            if (fiv.faceLandmark != null) {
                addedFiv = alignFaceByFace(tietuBm, tietuFaceFeature, tietuEye, frame);
            } else {
                if (frame == startFrame) {
                    addedFiv = fiv; // 起始帧，就是添加原来的fiv
                    rrManager.repealPrepare(); // 撤销重做需要，管理器里面去掉这个图，但是不从layout删除
                } else {
                    addedFiv = alignThingByFace(startFrame, frame, fiv, nearPointId);
                }
            }

            // 添加完之后处理方式相同
            addedFiv.setIsAutoAdd(true);
            boolean[] ofFrames = new boolean[frames.length];
            Arrays.fill(ofFrames, false);
            ofFrames[i] = true;
            addedFiv.setOfGifFrames(ofFrames);
            addedFiv.toLocked();
            tietuLayout.unChoseCurTietu();
            fivList.add(addedFiv);
        }

        if (fivList == null) {
            ToastUtils.show("没有检测到人脸, 无法添加");
        } else {
            if (!AllData.hasReadConfig.hasRead_gifAutoAddEffect()) {
                showTietuTools(pFunctionRcv);
                final FirstUseDialog firstUseDialog = new FirstUseDialog(getActivity());
                String msg = isAutoAddWhenSure ? mContext.getString(R.string.gif_auto_add_tietu_notice)
                        : mContext.getString(R.string.gif_auto_add_tietu_notice_1);
                firstUseDialog.createDialog(null, msg,
                        new FirstUseDialog.ActionListener() {
                            @Override
                            public void onSure() {
                                AllData.hasReadConfig.put_gifAutoAddEffect(true);
                            }
                        });
            } else {
                FirstUseUtil.gifAutoAddTips(getActivity());
            }
            gifManager.preview();
        }
        commit2rrManager(fivList);
        return fivList;
    }


    /**
     * 根据人脸添加其它物品，比如墨镜，香烟
     * 这个方法的代码记录，写一下吧
     * 这种A的Bm+View  进行旋转 缩放 移动 然后B的Bm+View，让B进行旋转缩放移动等，与A对齐
     * 写起来很麻烦，这个地方写代码了整整半天
     * 很容易转换关系 数学上没搞对，或者哪个地方代码出错了，没见结果了
     * <p>
     * 这里一开始转换关系完全搞错一遍，算好了，写代码，然后GG
     * 然后转换关系总体弄对了，但是没有正确结果，几个小的地方错了，但是不知道是哪里，很难查
     * 最后用了可视化的方法，用python 把几个关键点在电脑画板模拟画出来，这大大加快了debug的速度，
     * 所以以后类似问题，记得用这个方法来解决，会好很多的
     */
    private FloatImageView alignThingByFace(GifFrame startFrame, GifFrame curFrame, FloatImageView startFiv, int[] nearPointId) {

        float scale = curFrame.faceFeature.faceWidth / startFrame.faceFeature.faceWidth;
        int curW = (int) (startFiv.getCurPicWidth() * scale);
        int curH = (int) (startFiv.getCurPicHeight() * scale);

        float rotate = curFrame.faceFeature.angleY - startFrame.faceFeature.angleY;

        // 位移的计算比较复杂些, 要画图分析下，
        // 以鼻尖为中心，view的点用中心点，不受旋转影响，不然难算
        MPoint viewCenter = new MPoint(startFiv.getLayoutCenterX(), startFiv.getLayoutCenterY());
        if (LogUtil.debugFace) {
            Log.d(TAG, "alignThingByFace: \n");
            Log.d(TAG, String.format("alignThingByFace: start的fiv中心在View中位置 %f, %f", viewCenter.x, viewCenter.y));
        }
        viewCenter = PtuUtil.getLocationAtBaseBm(viewCenter.x, viewCenter.y, ptuSeeView); // 先变到bm坐标下
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: start的fiv中心在Bm中位置 %f, %f", viewCenter.x, viewCenter.y));
        }

        MPoint startFaceCenter = FaceFeatureDetector.kp2Point(startFrame.faceLandmark, FaceFeatureDetector.KP_NOSE);
        MPoint curFaceCenter = FaceFeatureDetector.kp2Point(curFrame.faceLandmark, FaceFeatureDetector.KP_NOSE);
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: start人脸中心 %f, %f", startFaceCenter.x, startFaceCenter.y));
            Log.d(TAG, String.format("alignThingByFace: cur人脸中心 %f, %f", curFaceCenter.x, curFaceCenter.y));
        }


        // 缩放不是整体缩放，只是人脸缩小了，所以这里缩放和一般的不一样，只是缩放两者之前的距离
        MPoint dis = viewCenter.sub(startFaceCenter);
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: 移动距离 %f, %f", dis.x, dis.y));
        }
        // 缩放
        dis.numMulti_(scale);
        viewCenter = curFaceCenter.add(dis);
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: 缩放k= %f 后移动距离 %f, %f", scale, dis.x, dis.y));
            Log.d(TAG, String.format("alignThingByFace: 移动+缩放后fiv中心在Bm %f, %f", viewCenter.x, viewCenter.y));
        }

        viewCenter.rotate_degree_(curFaceCenter, rotate);
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: 旋转后fiv中心在Bm %f, %f", viewCenter.x, viewCenter.y));
        }

        viewCenter = PtuUtil.bmPosition2PtuSeeView(viewCenter, ptuSeeView); // 转换回view坐标
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: 移动+缩放后fiv中心在View %f, %f", viewCenter.x, viewCenter.y));
            MPoint curFaceCenterInView = PtuUtil.bmPosition2PtuSeeView(curFaceCenter, ptuSeeView);
            Log.d(TAG, String.format("alignThingByFace: cur的人脸中心在View中 %f, %f", curFaceCenterInView.x, curFaceCenterInView.y));
            Log.d(TAG, String.format("alignThingByFace: rotate = %f", rotate));
        }

        FloatImageView fiv = initAndAddTietu(startFiv.getSrcBitmap(), null,
                (int) viewCenter.x - curW / 2 - FloatImageView.PAD, (int) viewCenter.y - curH / 2 - FloatImageView.PAD,
                curW, curH,
                startFiv.getRotation() + rotate,
                false, false);

        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignThingByFace: 添加之后的fiv中心在View %f, %f", fiv.getLayoutCenterX(), fiv.getLayoutCenterY()));
        }
        return fiv;
    }

    private FloatImageView alignFaceByFace(Bitmap tietuBm, FaceFeature tietuFaceFeature, MPoint tietuEye, GifFrame frame) {
        // 对其操作，
        MPoint baseEye = FaceFeatureDetector.kp2Point(frame.faceLandmark, FaceFeatureDetector.KP_L_EYE);

        float[] alignPara = FaceAlign.align(tietuFaceFeature, frame.faceFeature);
        if (LogUtil.debugFace) {
            Log.d(TAG, String.format("alignFaceByFace: bm上的缩放比例 = %f", alignPara[1]));
        }
        alignPara[1] *= ptuSeeView.getTotalRatio();
        if (tietuBm.getWidth() * alignPara[1] > AllData.getScreenWidth()) {
            alignPara[1] = AllData.getScreenWidth() * 1f / tietuBm.getWidth();
        }
        int tietuW = (int) (tietuBm.getWidth() * alignPara[1]);
        int tietuH = (int) (tietuBm.getHeight() * alignPara[1]);

        MPoint tietuEyeInView = tietuEye.numMulti(alignPara[1])
                .rotate_radius_(tietuW / 2f, tietuH / 2f, Math.toRadians(alignPara[0]))
                .add_(FloatImageView.PAD, FloatImageView.PAD);
        baseEye = PtuUtil.bmPosition2PtuSeeView(baseEye, ptuSeeView);
        if (LogUtil.debugFace) {
            Log.d(TAG, "tietu eye" + tietuEyeInView + "base eye " + baseEye);
        }
        MPoint start = baseEye.sub(tietuEyeInView);
        FloatImageView fiv = initAndAddTietu(tietuBm, null,
                (int) start.x, (int) start.y,
                tietuW, tietuH,
                alignPara[0],
                false, false);
        return fiv;
    }

    /******************************************其它********************************************/

    @Override
    public int getEditMode() {
        return PtuUtil.EDIT_TIETU;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        int isVisible = EventBusConstants.GIF_PLAY_CHOSEN.equals(event)
                ? View.VISIBLE : View.INVISIBLE;

        int count = tietuLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            //获取数据
            FloatImageView fiv = (FloatImageView) tietuLayout.getChildAt(i);
            if (!fiv.isLocked() && !fiv.isAutoAdd()) { // 锁住的图, 自动添加的图不受当前帧选中情况的影响
                fiv.setVisibility(isVisible);
            }
        }
    }

    /**
     * @param frameEvent 传入gif当前播放的帧
     */
    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onPlayGifFrameEvent(GifPlayFrameEvent frameEvent) {
        if (tietuLayout == null) return;
        int childCount = tietuLayout.getChildCount();
        for (int i = 0; i < childCount; i++) { // 遍历所有贴图
            FloatImageView fiv = ((FloatImageView) tietuLayout.getChildAt(i));
            if (!fiv.isLocked() && !fiv.isAutoAdd()) continue; // 锁定的固定显示，自动添加的也固定显示
            boolean[] ofFrames = fiv.getOfFrames();
            int playID = frameEvent.id;
            if (ofFrames != null && ofFrames.length > playID) {
                if (ofFrames[playID]) { // 判断贴图在这一帧是否显示
                    fiv.setVisibility(View.VISIBLE);
                } else {
                    fiv.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public List<String> getGuidKeyword() {
        List<String> keywordList = new ArrayList<>();
        if (faceChanger != null) {
            keywordList.add("换脸");
        }
        keywordList.add("贴图");
        return keywordList;
    }
    //  注意！！！ 贴图Fragment不止贴图功能，还包含换脸，抠图选底图功能，开发，测试，修改时都需要考虑到
}