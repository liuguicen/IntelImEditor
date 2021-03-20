package com.mandi.intelimeditor.ptu.rendpic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.PtuBaseChooser;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.tietu.FloatImageView;
import com.mandi.intelimeditor.ptu.tietu.TietuFrameLayout;
import com.mandi.intelimeditor.ptu.tietu.TietuSizeController;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.bean.FunctionInfoBean;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.TestOnly;

import java.util.Collections;
import java.util.List;


/**
 * Created by Administrator on 2016/7/1.
 */
public class RendFragment extends BasePtuFragment {
    public static final String TIETU_PATH_KEY = "tietu_path";

    private static String TAG = "TietuFragment";
    private PTuActivityInterface ptuActivityInterface;
    private TietuFrameLayout tietuLayout;
    Context mContext;

    private PtuSeeView ptuSeeView;
    private int clickRendCount = 0;
    private String mPicPath;
    @Nullable
    private Bitmap bmFromMain;
    private RepealRedoListener mRepealRedoListener;
    private FloatImageView mFloatImageView;
    private PtuBaseChooser mPTuBaseChooser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    public void initBeforeCreateView(final PtuFrameLayout ptuFrame, final PtuSeeView ptuSeeView, Rect totalBound, String picPath) {
        this.ptuSeeView = ptuSeeView;
        bmFromMain = ptuSeeView.getSourceBm();
        ptuSeeView.replaceSourceBm(null);
        tietuLayout = ptuFrame.initAddImageFloat(new Rect(
                ptuFrame.getLeft(), ptuFrame.getTop(), ptuFrame.getRight(), ptuFrame.getBottom()));
        mPicPath = picPath;
    }

    @Override
    public void initView() {
        super.initView();
        initFloatImageView(bmFromMain, mPicPath, null);
        mPTuBaseChooser = new PtuBaseChooser(mContext, this,
                ptuActivityInterface, Collections.singletonList("撕图"));
        mPTuBaseChooser.setIsUpdateHeat(false);
        mPTuBaseChooser.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        FirstUseUtil.rendGuide(mContext);
    }

    @TestOnly
    void test() {
        new Handler().postDelayed(() -> {
            Log.e(TAG, "执行测试切换");
            addTietuByPath("/storage/emulated/0/rend_test.png");
        }, 500);
    }

    /**
     * 添加一个tietu，
     */
    @TestOnly
    private void addTietuByPath(String path) {
        BitmapFactory.Options options = TietuSizeController.getFitWh(path, ptuActivityInterface.getGifManager() != null);
        Glide.with(IntelImEditApplication.appContext).asBitmap().load(path).into(new CustomTarget<Bitmap>(options.outWidth, options.outHeight) {
            @Override
            public void onResourceReady(@NonNull Bitmap srcBitmap, @Nullable Transition<? super Bitmap> transition) {
                if (srcBitmap == null || srcBitmap.getWidth() == 0 || srcBitmap.getHeight() == 0) {
                    ToastUtils.show("获取贴图失败");
                    return;
                }
                Log.d(TAG, "onResourceReady: " + srcBitmap.getWidth() + " " + srcBitmap.getHeight());
                initFloatImageView(srcBitmap, path, null);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    private void initFloatImageView(@Nullable Bitmap srcBitmap, String path, Integer id) {
        mFloatImageView = new FloatImageView(mContext, (RepealRedoListener) null);
        if (srcBitmap == null) {
            return;
        }
        mFloatImageView.setAdjustViewBounds(true);
        mFloatImageView.setImageBitmap(srcBitmap);
        MPoint resWH = new MPoint();
        TietuSizeController.getFeatWH(srcBitmap.getWidth(), srcBitmap.getHeight(), ptuSeeView.getPicBound(), resWH);
        resWH.x *= 1.17;
        resWH.y *= 1.17;
        mFloatImageView.bmScaleRatio = (resWH.x - (FloatImageView.PAD << 1)) / srcBitmap.getWidth();
        // 贴图默认尺寸，太小了，这里增加
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(resWH.xInt(), resWH.yInt());
        params.leftMargin = ptuSeeView.getPicBound().centerX() - params.width / 2;
        params.topMargin = ptuSeeView.getPicBound().centerY() - params.height / 2;
        Log.e(TAG, "添加位置" + params.leftMargin + " " + params.topMargin);
        tietuLayout.addView(mFloatImageView, params);
        mFloatImageView.startRend();
        mFloatImageView.setRendListener(() -> mRepealRedoListener.canRepeal(true));
    }

    //  保存的结果自动取最大，暂时不用
    private void switchScale() {
        if (mFloatImageView.isIn_moveFrag()) {
            mFloatImageView.finishAllRend();
        } else if (!mFloatImageView.isInRend()) {
            mFloatImageView.startRend();
        }
    }

    private void finishRendPic() {
        tietuLayout.finishRend();
    }

    private void rendOnceAgain() {
        if (mFloatImageView.isIn_moveFrag()) {
            mFloatImageView.finishOneRend();
            mFloatImageView.startRend();
        }
    }

    @Override
    public void smallRepeal() {
        if (mFloatImageView.isIn_moveFrag()) {
            mFloatImageView.return_increaseCrack();
            mFloatImageView.returnReady();
            tietuLayout.initRendGestureStatus();
            mRepealRedoListener.canRepeal(false); // 只能重做一次
        }
    }

    @Override
    public void smallRedo() {

    }

    @Override
    public void generateResultDataInMain(float ratio) {
        if (mFloatImageView.isInRend()) {
            mFloatImageView.finishAllRend();
        }
    }

    /**
     * {@link BasePtuFragment#getResultDataAndDraw(float)}
     */
    @Override
    @Nullable
    public StepData getResultDataAndDraw(float ratio) {
        if (mPTuBaseChooser != null) {
            mPTuBaseChooser.updateDefault();
        }
        // 相当于贴图的图片
        Bitmap rendBm = mFloatImageView.getSrcBitmap();

        // 获取在底图上的位置
        RectF boundInPic = new RectF();
        float[] realLocation = PtuUtil.getLocationAtBaseBm(
                mFloatImageView.getLeft() + FloatImageView.PAD, mFloatImageView.getTop() + FloatImageView.PAD,
                ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        boundInPic.left = realLocation[0];
        boundInPic.top = realLocation[1];

        realLocation = PtuUtil.getLocationAtBaseBm(
                mFloatImageView.getRight() - FloatImageView.PAD, mFloatImageView.getBottom() - FloatImageView.PAD,
                ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        boundInPic.right = realLocation[0];
        boundInPic.bottom = realLocation[1];

        // 旋转角度
        float rotateAngle = mFloatImageView.getRotation();

        // 绘制结果到ptuView内的底图
        GifManager gifManager = ptuActivityInterface.getGifManager();
        if (gifManager == null) {
            PtuUtil.addBm2Canvas(ptuSeeView.getSourceCanvas(), rendBm, boundInPic, rotateAngle);
        } else {
            // 绘制结果
            gifManager.addBm2Frames(rendBm, boundInPic, rotateAngle);
        }
        return null; // 暂时不支持重做，重做需要底图，撕图两张图一起弄，复杂度比较高
    }

    public static void addBigStep(StepData sd, PTuActivityInterface ptuActivityInterface) {
        PtuSeeView ptuSeeView = ptuActivityInterface.getPtuSeeView();
        Bitmap bm = BitmapUtil.getLosslessBitmap(sd.picPath);
        ptuSeeView.post(() -> ptuSeeView.replaceSourceBm(bm));
    }

    @Override
    public void releaseResource() {
        if (mPTuBaseChooser != null) {
            mPTuBaseChooser.releaseResources();
            mPTuBaseChooser = null;
        }
        tietuLayout = null;
        bmFromMain = null;
        mFloatImageView = null;
    }

    @Override
    public boolean onBackPressed(boolean isFromKey) {
        ptuActivityInterface.replaceBase(mPicPath);
        return false; // 不消耗返回事件
    }

    @Override
    public int getEditMode() {
        return PtuUtil.EDIT_REND;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_bottom_function;
    }

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.ptuActivityInterface = ptuActivity;
        this.mRepealRedoListener = ptuActivity.getRepealRedoListener();
        mRepealRedoListener.canRepeal(false);
        mRepealRedoListener.canRedo(false);
    }

    @Override
    public boolean onSure() {
        return false;
    }

    RendActionListener mRendActionListener;

    public void setRendActionListener(RendActionListener rendActionListener) {
        this.mRendActionListener = rendActionListener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PtuActivity.REQUEST_CODE_CHOOSE_BASE && data != null) {
            PicResource picRes = (PicResource) data.getSerializableExtra(PtuActivity.INTENT_EXTRA_CHOSE_BASE_PIC_RES);
            ptuActivityInterface.replaceBase(picRes.getUrlString());
            ptuActivityInterface.addUsedTags(true, picRes.getTag());
        }
    }

    @Override
    public List<FunctionInfoBean> getFunctionList() {
        pFunctionList.clear();
        pFunctionList.add(new FunctionInfoBean(R.string.choose_base_pic, R.mipmap.choose_base, R.drawable.function_background_text_yellow, PtuUtil.EDIT_REND));
        pFunctionList.add(new FunctionInfoBean(R.string.add_text, R.mipmap.text, R.drawable.function_background_text_yellow, PtuUtil.EDIT_TEXT));
        pFunctionList.add(new FunctionInfoBean(R.string.go_edit, R.mipmap.edit, R.drawable.function_background_text_yellow, PtuUtil.EDIT_CUT));
        pFunctionList.add(new FunctionInfoBean(R.string.rend_again, R.mipmap.bold, R.drawable.function_background_text_yellow, PtuUtil.EDIT_REND));
        return pFunctionList;
    }

    @Override
    public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
        super.onItemClick(adapter, view, position);
        InsertAd.onClickTarget(getActivity());
        switch (pFunctionList.get(position).getTitleResId()) {
            case R.string.choose_base_pic:
                mPTuBaseChooser.switchPtuBaseChooseView();
                break;
            case R.string.add_text:
                US.putPTuRendEvent(US.PTU_REND_ADD_TEXT);
                if (mRendActionListener != null) {
                    mRendActionListener.goToAddText();
                }
                break;
            case R.string.go_edit:
                US.putPTuRendEvent(US.PTU_REND_GO_EDIT);
                if (mRendActionListener != null) {
                    mRendActionListener.goToEdit();
                }
                break;
            case R.string.rend_again:
                US.putPTuRendEvent(US.PTU_REND_REND_AGAIN);
                rendOnceAgain();
                break;
        }
    }

    public interface RendActionListener {
        void goToAddText();

        void goToEdit();

        void changePicFormat(String suffix);
    }
}