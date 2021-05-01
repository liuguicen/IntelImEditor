package com.mandi.intelimeditor.ptu.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.ptu.BasePtuFragment;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.common.DrawController;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.repealRedo.DrawStepData;
import com.mandi.intelimeditor.ptu.repealRedo.StepData;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraser;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraserUI;
import com.mandi.intelimeditor.ptu.view.ColorPicker;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.home.view.BottomFunctionView;
import com.mandi.intelimeditor.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import draw.PaintStrokeDialog;

/**
 * Created by Administrator on 2016/7/25.
 */
public class DrawFragment extends BasePtuFragment {
    private static final int STROKE_TYPE_DRAW = 1;
    private String TAG = "DrawFragment";
    private Context mContext;

    private View eraserFunctionLayout;
    /**
     * 底部功能列表
     */
    private View mFunctionLayout;

    private DrawView drawView;
    private FunctionPopWindowBuilder drawPopupBuilder;
    private PTuActivityInterface pTuActivityInterface;
    private PtuSeeView ptuSeeView;
    private RepealRedoListener repealRedoListener;
    private Util.DoubleClick leaveJudge;
    private boolean isInClearDraw;
    /**
     * 上一次样式
     */
    private int lastStyle = 0;
    private int size;
    /**
     * 底部控制列表
     */
    private BottomFunctionView drawStyleTv, drawEraserTv, drawColorTv;
    private ViewEraser viewEraser;
    private DrawController functionController;
    private View mSecondarySureBtn;

    @Override
    public void setPTuActivityInterface(PTuActivityInterface ptuActivity) {
        this.pTuActivityInterface = ptuActivity;
        this.ptuSeeView = ptuActivity.getPtuSeeView();
        repealRedoListener = ptuActivity.getRepealRedoListener();
        if (drawView != null)
            drawView.setRepealRedoListener(repealRedoListener);
        repealRedoListener.canRepeal(false);
        repealRedoListener.canRedo(false);
    }


    public void initBeforeCreateView(DrawController secondFuncControl, PtuFrameLayout ptuFrame) {
        this.functionController = secondFuncControl;
//         gif的二级按钮，已经不使用了
        // TODO: 2020/10/21 这个地方太难理解了，自己用起来，不看代码都理解不了，待处理
        if (pTuActivityInterface.getGifManager() != null) {
            mSecondarySureBtn = ptuFrame.addSecondarySureBtn();
            FirstUseUtil.drawGifSecondarySureGuide((FragmentActivity) pTuActivityInterface);
            mSecondarySureBtn.setOnClickListener(v -> {
                directlyDrawOnGifFrames();
            });
        }
    }

    /**
     * 为了方便用户选中gif的某些帧进行draw，不用一个sure，将整个draw模块返回，那样太麻烦
     * 这里直接draw在gif上，后面sure时就不用再次draw了
     */
    private void directlyDrawOnGifFrames() {
        GifManager gifManager = pTuActivityInterface.getGifManager();
        if (gifManager == null) return;
        GifFrame[] gifFrames = gifManager.getFrames();
        List<Canvas> canvasList = new ArrayList<>();
        for (GifFrame gifFrame : gifFrames) {
            if (gifFrame.isChosen) {
                canvasList.add(new Canvas(gifFrame.bm));
            }
        }

        for (Canvas canvas : canvasList) {
            PtuUtil.combineBitmap(canvas, drawView.getResultBm());
        }
        drawView.clearDrawData();
        ptuSeeView.postInvalidate();
    }


    @Override
    public int getLayoutResId() {
        return R.layout.fragment_draw;
    }

    @Override
    public void initView() {
        super.initView();
        mContext = getActivity();
        leaveJudge = new Util.DoubleClick(3000);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        leaveJudge = new Util.DoubleClick(3000);
        drawPopupBuilder = new FunctionPopWindowBuilder(mContext);
        isInClearDraw = false;

        drawStyleTv = rootView.findViewById(R.id.draw_style);
        drawEraserTv = rootView.findViewById(R.id.draw_eraser);
        drawColorTv = rootView.findViewById(R.id.draw_color);
//        drawEraserView = rootView.findViewById(R.id.eraserView);

        mFunctionLayout = rootView.findViewById(R.id.draw_function_layout);
        eraserFunctionLayout = rootView.findViewById(R.id.view_eraser_function_layout);
        initIconList();
        if (functionController != null) {
            drawView.setPaintColor(functionController.startColor);
            drawView.setPaintSize((int) (ptuSeeView.getWidth() * functionController.startWidthRatio));
        }
    }

    /**
     * 初始化控制列表，点击事件
     */
    private void initIconList() {
        drawStyleTv.setOnClickListener(v -> {
            isInClearDraw = false;
            US.putPTuDrawEvent(US.PTU_DRAW_STYLE);
//            drawStyleTv.setIconBackgroundResource(R.drawable.function_background_draw_pink);
            drawView.selectPaintStyle(lastStyle);
            drawPopupBuilder.setStrokePopWindow(v);
            // InsertAd.onClickTarget(getActivity()); 本来就是弹窗，不弹广告这里
        });
        drawEraserTv.setOnClickListener(v -> {
            US.putPTuDrawEvent(US.PTU_DRAW_ERASE);
            startErase();
            InsertAd.onClickTarget(getActivity());
        });
        drawColorTv.setOnClickListener(v -> {
            US.putPTuDrawEvent(US.PTU_DRAW_COLOR);
            drawPopupBuilder.setColorPopWindow(rootView);
            InsertAd.onClickTarget(getActivity());
        });
    }


    @Override
    public void onActivityCreated(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (ptuSeeView != null) {
            ptuSeeView.setCanDoubleClick(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (ptuSeeView != null) {
            ptuSeeView.setCanDoubleClick(true);
        }
        // Fragment动画导致子View还没清除就添加出错的问题，
        // 这是一种处理方式，但是退出动画没了，如果前面一种处理方式不行，再在用这种
        /*if (rootView != null) {
            ViewGroup parentView = (ViewGroup) rootView.getParent();
            if (parentView != null) {
                parentView.removeView(rootView);
            }
        }*/
        EventBus.getDefault().unregister(this);
    }

    public View createDrawView(Context context, Rect totalBound, PtuSeeView ptuSeeView) {
        drawView = new DrawView(context, totalBound, ptuSeeView);
        if (repealRedoListener != null) {
            drawView.setRepealRedoListener(repealRedoListener);
            drawView.setPtuActivityInterface(pTuActivityInterface);
        }
        return drawView;
    }

    @Override
    public void smallRepeal() {
        if (eraserFunctionLayout.getVisibility() == View.VISIBLE && viewEraser != null) {
            viewEraser.smallRepeal();
        } else {
            drawView.undo();
        }
        Log.e(TAG, "repealPrepare");
    }

    @Override
    public void smallRedo() {
        if (eraserFunctionLayout.getVisibility() == View.VISIBLE && viewEraser != null) {
            viewEraser.smallRedo();
        } else {
            drawView.recover();
        }
        Log.e(TAG, "recover");
    }

    public Bitmap getResultBm(float ratio) {
        return drawView.getResultBm();
    }

    @Override
    public StepData getResultDataAndDraw(float ratio) {
        //获取和设置数据
        DrawStepData dsd = new DrawStepData(PtuUtil.EDIT_DRAW);
        dsd.rotateAngle = 0;
        if (drawView != null) {
            dsd.setSavePath(drawView.getResultData());
            if (viewEraser != null) {
                dsd.setEraseData(viewEraser.getOperateList());
                dsd.hasTransparency = true;
            }
        }
        //获取并保存数据
        String tempPath = FileTool.createTempPicPath();
        AllData.getPTuBmPool().putBitmap(tempPath, getResultBm(1));
        dsd.picPath = tempPath;
        addBigStep(dsd, pTuActivityInterface);
        return dsd;
    }

    @Override
    public void generateResultDataInMain(float ratio) {
        if (eraserFunctionLayout.getVisibility() == View.VISIBLE) {
            finishErase();
        }
    }

    public static void addBigStep(StepData sd, PTuActivityInterface pTuActivityInterface) {
        PtuSeeView ptuSeeView = pTuActivityInterface.getPtuSeeView();
        DrawStepData dsd = (DrawStepData) sd;
        GifManager gifManager = pTuActivityInterface.getGifManager();
        // 画橡皮擦擦除的东西
        if (dsd.eraseData != null && ptuSeeView.getSourceBm() != null) {
            Bitmap bitmap = BitmapUtil.eraseBmByPath(ptuSeeView.getSourceBm(), dsd.eraseData, false);
            ptuSeeView.replaceSourceBm(bitmap);
        }

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
        boolean isCreateBmWithAlpha = false;
        for (Canvas canvas : canvasList) {
            List<DrawView.DrawPath> pathPaintList = dsd.getSavePath();
            for (DrawView.DrawPath pair : pathPaintList) {
//                float width = pair.paint.getStrokeWidth();
//                pair.paint.setStrokeWidth(pair.paint.getStrokeWidth()
//                        * ptuSeeView.getSrcRect().height() * 1f / ptuSeeView.getDstRect().height());
//                if (pair.paint.isTransparent && !pair.paint.isErase && ptuSeeView.getSourceBm() != null) { // 透明颜色，特殊处理
//                    Bitmap src = ptuSeeView.getSourceBm();
//                    // 如果原来的图片不包含透明像素，需要重新创建一个包含透明像素的才行
//                    // 但是Android读取不包含透明像素的图片，比如jpeg读出来格式仍然是ARGB_8888格式，
//                    // 结果你保存的时候透明像素又丢失了，这应该算是一个bug吧
//                    // 这导致无法判断什么时候是真的支持透明度的ARGB_8888,什么时候是假的
//                    // 所以这里只能不管哪种情况，都新建一个bitmap
//                    if (!isCreateBmWithAlpha) {
//                        isCreateBmWithAlpha = true;
//                        Bitmap tempBm = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
//                        new Canvas(tempBm).drawBitmap(src, 0, 0, BitmapUtil.getBitmapPaint());
//                        src = tempBm;
//                    }
//                    Bitmap bm = BitmapUtil.drawTransparencyInBm(src, pair.path, pair.paint);
//                    ptuSeeView.onlyReplaceSrcBm(bm);
//                    dsd.hasTransparency = true;
//                }
//                if (Color.alpha(pair.paint.getColor()) < 255) {
//                    dsd.hasTransparency = true;
//                }
//                canvas.drawPath(pair.path, pair.paint);
//                pair.paint.setStrokeWidth(width);
            }
            String tempPath = dsd.picPath;
            Bitmap bitmap = AllData.getPTuBmPool().get(tempPath);
            if (bitmap != null) {
                PtuUtil.combineBitmap(canvas, AllData.getPTuBmPool().get(tempPath));
            }
        }
        ptuSeeView.postInvalidate();
    }

    @Override
    public void releaseResource() {
        EventBus.getDefault().unregister(this);
        if (viewEraser != null) {
            viewEraser.releaseResource();
            viewEraser = null;
            ptuSeeView.injectViewEraser(null);
            drawView = null;
        }
    }

    private void startErase() {
        mFunctionLayout.setVisibility(View.GONE);
        eraserFunctionLayout.setVisibility(View.VISIBLE);
        if (viewEraser == null) {
            new ViewEraserUI(mContext, drawView,
                    new ViewEraserUI.EraseUIInterface() {
                        @Override
                        public void finishErase() {
                            DrawFragment.this.finishErase();
                        }

                        @Override
                        public int getPaintWidth() {
                            return (int) getEraserPaintWidth();
                        }

                        @Override
                        public int getBlurWidth() {
                            return (int) getEraserBlurWidth();
                        }

                        @Override
                        public void setEraserBlurWidth(float width) {
                            DrawFragment.this.setEraserBlurWidth(width);
                        }

                        @Override
                        public void setEraserWidth(float width) {
                            setEraserPaintWidth(width);
                        }
                    }, eraserFunctionLayout);
            ViewEraser.ViewEraserInterface eraserInterface = new ViewEraser.ViewEraserInterface() {
                @Override
                public View getView() {
                    return ptuSeeView;
                }

                @Override
                public float getBmScaleRatio() {
                    if (ptuSeeView.getSourceBm() != null) {
                        // 因为没有局部缩放，所以直接用getWidth()
                        return (float) ptuSeeView.getWidth() / ptuSeeView.getSourceBm().getWidth();
                    }
                    return 1;
                }

                @Override
                public Bitmap getSrcBitmap() {
                    return ptuSeeView.getSourceBm();
                }

                @Override
                public void setBitmap(Bitmap bm) {
                    ptuSeeView.replaceSourceBm(bm);
                }

                @Override
                public float getBmLeft() {
                    return ptuSeeView.getPicBound().left;
                }

                @Override
                public float getBmTop() {
                    return ptuSeeView.getPicBound().top;
                }
            };
            viewEraser = new ViewEraser(getContext(), eraserInterface, repealRedoListener, pTuActivityInterface);
            viewEraser.enlargePaintWidth(0.5);
        }
        ViewGroup ViewGroup = (android.view.ViewGroup) ptuSeeView.getParent();
        ViewGroup.addView(viewEraser, new FrameLayout.LayoutParams(ptuSeeView.getLayoutParams()));
        ptuSeeView.injectViewEraser(viewEraser);
        if (repealRedoListener != null) {
            repealRedoListener.canRepeal(false);
            repealRedoListener.canRedo(false);
        }
    }

    private void finishErase() {
        mFunctionLayout.setVisibility(View.VISIBLE);
        ViewGroup parent = (ViewGroup) ptuSeeView.getParent();
        parent.removeView(viewEraser);
        eraserFunctionLayout.setVisibility(View.GONE);
        // draw里面使用橡皮，会使用到 xfermode, color filter, or alpha,需要设置这个属性才能生效
        // 使用完之后取消此属性，尽量不影响性能
//        ptuSeeView.setLayerType(View.LAYER_TYPE_NONE, null);
//        ptuSeeView.injectViewEraser(null);
//        viewEraser.setInErasing(true);
        drawView.refreshRepealRedo();
    }

    private void cancelErase() {
        mFunctionLayout.setVisibility(View.VISIBLE);
        ViewGroup parent = (ViewGroup) ptuSeeView.getParent();
        parent.removeView(viewEraser);
        eraserFunctionLayout.setVisibility(View.GONE);
        // draw里面使用橡皮，会使用到 xfermode, color filter, or alpha,需要设置这个属性才能生效
        // 使用完之后取消此属性，尽量不影响性能
//        ptuSeeView.setLayerType(View.LAYER_TYPE_NONE, null);
//        ptuSeeView.injectViewEraser(null);
//        viewEraser.setInErasing(true);
        viewEraser.cancelErase();
        drawView.refreshRepealRedo();
    }

    private void cancelClearDraw() {
        isInClearDraw = false;
        if (getContext() != null) {
            if (drawView.currentStyle != DrawView.PAINT_STYLE_CLEAR_DRAW) {
                lastStyle = drawView.currentStyle;
            }
        }
    }

    public float getEraserPaintWidth() {
        if (viewEraser != null)
            return viewEraser.getPaintWidth();
        return 0;
    }

    public void setEraserPaintWidth(float width) {
        if (viewEraser != null)
            viewEraser.setPaintWidth(width);

    }

    public float getEraserBlurWidth() {
        if (viewEraser != null) {
            return viewEraser.getBlurWidth();
        }
        return 0;
    }

    public void setEraserBlurWidth(float width) {
        if (viewEraser != null) {
            viewEraser.setBlurWidth(width);
        }
    }

    public List<Pair<Path, MPaint>> getEraseData() {
        if (viewEraser != null) {
            return viewEraser.getOperateList();
        }
        return null;
    }

    /**
     * 清除画笔画的东西
     */
    private void clearDraw() {
        isInClearDraw = !isInClearDraw;
        if (isInClearDraw) {
            drawStyleTv.setIconBackgroundResource(R.drawable.function_background_text_yellow);
            drawView.selectPaintStyle(DrawView.PAINT_STYLE_CLEAR_DRAW);
        } else {
            drawStyleTv.setIconBackgroundResource(R.drawable.function_background_draw_pink);
            drawView.selectPaintStyle(lastStyle);
        }
    }

    //弹出选择画笔或橡皮擦的对话框
    private int select_paint_style_index = 0;

    @Override
    public int getEditMode() {
        return PtuUtil.EDIT_DRAW;
    }


    @Override
    public boolean onSure() {
        if (eraserFunctionLayout.getVisibility() == View.VISIBLE) {
            finishErase();
            return true;
        }
        return false;
    }

    @Override
    public boolean onBackPressed(boolean isFromKey) {
        // 修改较多，防止误点离开，全面屏手势容易和绘图操作混淆
        // 次数多，且不是第二次点击，就退出
        if (eraserFunctionLayout!=null&&eraserFunctionLayout.getVisibility() == View.VISIBLE) {
            if (viewEraser != null && viewEraser.onBackPressed()) { // 橡皮消耗返回事件
                return true;
            } else { // 橡皮不消耗返回事件，但是处于橡皮状态，返回绘图状态，这个返回事件也被消耗
                cancelErase();
                viewEraser.reInitData();
                return true;
            }
        }
        if (isFromKey && drawView.getOperationNumber() > 10 && !leaveJudge.isDoubleClick_m()) {
            ToastUtils.show(R.string.leave_from_much_edit);
            return true;
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        int isVisible = EventBusConstants.GIF_PLAY_CHOSEN.equals(event)
                ? View.VISIBLE : View.INVISIBLE;
        drawView.setVisibility(isVisible);
    }

    /**
     * 创建模块功能区的功能操作视图
     * Created by Administrator on 2016/5/24.
     */
    public class FunctionPopWindowBuilder {
        Context mContext;

        public FunctionPopWindowBuilder(Context context) {
            mContext = context;
        }

        public void setColorPopWindow(View v) {
            ColorPicker colorPicker = new ColorPicker(getActivity());
            colorPicker.setColorTarget(new ColorPicker.ColorTarget() {
                @Override
                public void setColor(int color) {
                    drawView.setPaintColor(color);
                }

                @Override
                public int getCurColor() {
                    return drawView.getCurPaintColor();
                }
            });
            colorPicker.setAbsorbListener(new ColorPicker.AbsorbListener() {
                @Override
                public void startAbsorb(ColorPicker colorPicker) {
                    if (!AllData.hasReadConfig.hasRead_absorb()) {
                        FirstUseDialog firstUseDialog = new FirstUseDialog(getActivity());
                        firstUseDialog.createDialog("吸取颜色", "可在图片中吸取想要的颜色，吸取之后点击其它地方即可使用", new FirstUseDialog.ActionListener() {
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
            colorPicker.addViewToGetColor(ptuSeeView, ptuSeeView.getSourceBm(),
                    ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
            ColorPicker.showInDefaultLocation(getActivity(), colorPicker, v.getHeight() + Util.dp2Px(5), v.getRootView());
        }

        public void setStrokePopWindow(View view) {
            PaintStrokeDialog paintStrokeDialog = new PaintStrokeDialog();
            paintStrokeDialog.setPaintStrokeSize(drawView.currentSize);
            paintStrokeDialog.setPaintAlpha(drawView.currentAlpha);
            paintStrokeDialog.setPaintStrokeStyle(drawView.currentStyle);
            paintStrokeDialog.setDrawToolChangeListener((style, size, alpha) -> {
                if (drawView != null) {
                    lastStyle = style;
                    drawView.setPaintSize(size);
                    drawView.setStrokeAlpha(alpha);
                    drawView.selectPaintStyle(style);
                }
            });
            paintStrokeDialog.showIt(getActivity());
        }
    }

}
