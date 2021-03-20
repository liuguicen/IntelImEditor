package com.mandi.intelimeditor.ptu.tietu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BitmapUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.ptu.MicroButtonData;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.imageProcessing.BaoZouFaceFuse;
import com.mandi.intelimeditor.ptu.rendpic.CacuCrackFail;
import com.mandi.intelimeditor.ptu.rendpic.RendDrawDate;
import com.mandi.intelimeditor.ptu.rendpic.RendListener;
import com.mandi.intelimeditor.ptu.rendpic.RendState;
import com.mandi.intelimeditor.ptu.rendpic.SawtoothPathData;
import com.mandi.intelimeditor.ptu.rendpic.SawtoothPathGenerator;
import com.mandi.intelimeditor.ptu.rendpic.SawtoothShapeController;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraser;
import com.mandi.intelimeditor.ptu.view.IconBitmapCreator;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;



import static java.lang.Math.min;
import static java.lang.StrictMath.max;

/**
 * Created by liuguicen on 2016/10/2.
 * 配合TietuFrameLayout,可以缩放，移动，选择，撕图
 */
public class FloatImageView extends AppCompatImageView {
    private static final boolean isInTest = false;
    public static final String TAG = "FloatImageView";

    public static final int PAD = Util.dp2Px(24);
    private static Bitmap cancelBm = IconBitmapCreator.createTietuCancelBm(PAD, Util.getColor(R.color.base_color1), Color.WHITE);
    protected static Bitmap sureBm = IconBitmapCreator.createTietuSureBm(PAD, Util.getColor(R.color.base_color1), Color.WHITE);
    private static Bitmap toolBm = IconBitmapCreator.createEllipsisMore(PAD, Util.getColor(R.color.base_color1), Color.WHITE);
    private static RectF itemDstToDraw = new RectF();
    public static final int minWidth = 9;
    public static final int minHeight = 16;

    /** 缩放拉伸相关
     ****/
    /**
     * 图片展示出来的bm的宽（注意不一定等于View宽) 除以 bm原来的宽度
     **/
    public float bmScaleRatio = 1;
    public float[] faceBoxex;
    public float[] faceLandmark;
    private boolean isInStretch = false;
    public static final int STRETCH_NO = 0;
    public static final int STRETCH_LEFT = 1;
    public static final int STRETCH_TOP = 2;
    public static final int STRETCH_RIGHT = 3;
    public static final int STRETCH_BOTTOM = 4;
    /**
     * 拉伸方向
     */
    private int stretchDir = STRETCH_NO;
    private Path commonPath; // 公用的path，可以多个地方使用
    private static Paint rimPaint; // 设置成静态即可，多个Tietu共用


    private final int STRETCH_ICON_R = PAD; // 单独设置变量，方便处理
    private MicroButtonData[] items;
    private static Paint itemPaint;
    private OperateState mOperateState;

    @Nullable // 未知原因空
    private Bitmap srcBitmap;
    /**
     * 用于某些操作撤销重做时需要
     * 目前只支持一次撤销
     * 比如融合，融合完成之后，使用新的bm，原来的bm保存为oldBm，撤销则用oldBm替换回去，同时oldBm置空
     */
    private Bitmap oldBm;
    public static final int OPERATION_NONE = 0;
    public static final int OPERATION_FUSE = 1;
    public static final int OPERATION_REND = 2;
    public static final int OPERATION_STRETCH = 3;
    public static final int OPERATION_ERASE = 4;
    public static final int OPERATION_MAKE_BAOZOU = 5;
    public static final int OPERATION_FLIP = 6;

    /**
     * 用于记录上次做的额操作，主要由于连续多次重复同一操作的情况下需要使用相同的基图进行处理，
     * 但是基图会因为不同的类型操作而改变，则是就需要改变基图
     * 当操作成功了就设置这个变量，没成功就不设置，如果是none的话就会保持none
     */
    private int lastOperation = OPERATION_NONE;

    /**** 撕图相关 */
    private RendDrawDate rdd;
    /**
     * 只用于访问状态，不要改变，改变由layout控制
     */
    private RendState rendState = new RendState();
    private Bitmap rendInitBm = null;
    private RendListener mRendListener;

    /**
     * 橡皮相关
     */
    private ViewEraser viewEraser;

    /**
     * 对于gif，贴图属于那些帧
     */
    private boolean[] mOfFrames = null;
    /**
     * gif自动添加贴图添加的
     * 这种贴图有些地方要特殊处理
     */
    private boolean isAutoAdd = false;


    static {
        // 画笔都是一样的
        itemPaint = BitmapUtil.getBitmapPaint();
        rimPaint = new Paint();
        rimPaint.setColor(ContextCompat.getColor(IntelImEditApplication.appContext, R.color.float_rim_color));
        rimPaint.setStrokeWidth(Util.dp2Px(1));
        rimPaint.setStyle(Paint.Style.STROKE);
    }

    @Nullable
    private RepealRedoListener repealRedoListener;
    private String tietuTags;
    // 特别的，融合之后图的颜色变了，同一个bm进行多次融合会颜色加深从而失真
    // 所以每次融合只能使用最初的图
    // 由此，每次做其它的bm变化时，如果这个bm不为空，就需要带着这个bm做同样的变化
    // 第一次之后的融合就使用它
    // 这样才能不让同样的bm像素进行多次融合
    private Bitmap originalFuseBm = null;


    public FloatImageView(Context context, @Nullable RepealRedoListener repealRedoListener) {
        super(context);
        init();
        this.repealRedoListener = repealRedoListener;
    }

    public FloatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //item方面
        mOperateState = new OperateState();
        setPadding(PAD, PAD, PAD, PAD);
        initItem();
        initRim();
        initRend();
        //        test_init();
    }

    public void copyDataFrom(@NotNull FloatImageView lockedView) {
        setRotation(lockedView.getRotation());
        setScaleType(ScaleType.FIT_XY);
        setImageBitmap(lockedView.getSrcBitmap());
        bmScaleRatio = lockedView.bmScaleRatio;
        faceBoxex = lockedView.faceBoxex;
        faceLandmark = lockedView.faceLandmark;
    }

    private void initRend() {
        rendState = new RendState();
    }

    private void initItem() {
        // 8个位置，4个角，四条边的中间
        items = new MicroButtonData[8];
        items[0] = new MicroButtonData(-1, -1, " ");
        items[0].bitmap = FloatImageView.cancelBm;

        items[2] = new MicroButtonData(-1, -1, " ");
        items[2].bitmap = FloatImageView.sureBm;

        items[4] = new MicroButtonData(-1, -1, "");
        items[4].bitmap = FloatImageView.toolBm;
    }

    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.repealRedoListener = repealRedoListener;
    }

    private void initRim() {
    }

    public boolean isChosen() {
        return mOperateState.isIn_Chosen();
    }

    /**
     * 撕图的过程中，不显示边框
     */
    public boolean judgeShowRim() {
        return mOperateState.isIn_Chosen() && !rendState.isIn_Rend();
    }

    /**
     * 点击是否发生在FloatImageView的取消按钮上
     * 关于旋转后点的计算，旋转之后View的getLeft好getTop是不会变的，以这个点为原点
     * 触摸的x，y不变，View内部的点以原点为中心，绕着旋转点getWidth() / 2f, getHeight() / 2f旋转之后，即可得到旋转后的坐标
     *
     * @param x 传入相当于view左上角的位置，不是父布局的
     * @param y 传入相当于view左上角的位置，不是父布局的
     * @return 点击是否发生取消按钮上
     */
    public boolean isOnSure(float x, float y) {
        return isOnItem(x, y, 2, 0);
    }

    /**
     * 点击是否发生在FloatImageView的取消按钮上
     *
     * @param x 在view的位置，不是父布局的
     * @param y 在view的位置，不是父布局的
     * @return 点击是否发生取消按钮上
     */
    public boolean isOnCancel(float x, float y) {
        return isOnItem(x, y, 0, 0);
    }

    public boolean isOnTools(float x, float y) {
        return isOnItem(x, y, 2, 2);
    }

    /**
     * @param xPos 表示Item坐标，3个值，0=左边或上面，1= 中间，2=右边或者下边
     */
    public boolean isOnItem(float x, float y, int xPos, int yPos) {
        if (!judgeShowRim()) return false;//边框没显示出来，返回false
        MRect itemBound = getItemBound(xPos, yPos);
        if (itemBound.contains(x, y))
            return true;
        return false;
    }

    @NotNull
    private MRect getItemBound(int xPos, int yPos) {
        MRect itemBound = new MRect();

        float rotation = getRotation();
        float scale = rotation == 0 ? 1.5f : 1.35f; // 让按钮宽度变大些，方便点击，另外旋转之后本身会放大，放大比例设置小一些
        int r = Math.round(PAD * scale);
        // 具体位置关系需要画图看下
        // -(r - PAD) / 2f 为起始位置，然后xPos 相当于x坐标，再乘以一个坐标代表的长度，等价于下面的代码
        // 点击的范围是在按钮的周围，四边都会超出View，不要当成点击范围也在View里面，
        // 开始自己就这样搞错了

        itemBound.left = -(r - PAD) / 2f + xPos * ((getWidth() - PAD) / 2f);
        itemBound.right = itemBound.left + r;

        itemBound.top = -(r - PAD) / 2f + yPos * ((getHeight() - PAD) / 2f);
        itemBound.bottom = itemBound.top + r;

        if (rotation != 0) { // 如果旋转了，因为这里用的系统的旋转，位置就变了,要加上旋转处理
            Matrix matrix = new Matrix();
            matrix.setRotate(rotation, getWidth() / 2f, getHeight() / 2f);
            matrix.mapRect(itemBound);
        }
        return itemBound;
    }

    /**
     * 返回false,父布局的onTouchEvent一定会被调用
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private void test_init() {
        // 初始化画笔
        Paint testPaint = new Paint();
        testPaint.setColor(Color.RED);
        testPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }


    @Override
    public void onDraw(Canvas canvas) {
        if (rendState.isIn_Rend()) {
            if (rendState.isIn_Ready()) {
                super.onDraw(canvas);
            }
            if (rendState.isIn_increaseCrack()) {
                super.onDraw(canvas);
                drawCrack(canvas);
            } else if (rendState.isIn_moveFrag()) {
                // 现在图片内的bitmap的宽高
                float curw = getBmWidthInView(), curh = getBmHeightInView();
                drawPicFragment(canvas, curw, curh, rdd);
            }
        } else {
            super.onDraw(canvas);
        }
        if (viewEraser != null && viewEraser.isInErasing()) { // 橡皮只针对图片，所以不能放到其它东西绘制之后
            viewEraser.eraseInView(canvas);
        }
        //        test(canvas); // 测试划线的
        if (judgeShowRim()) {
            //画item的icon
            if (items[0].bitmap != null) {
                canvas.drawBitmap(items[0].bitmap, 0, 0, itemPaint);
            }

            int itop = 0;
            int ileft = getWidth() - PAD;
            if (items[0].bitmap != null) {
                canvas.drawBitmap(items[2].bitmap, ileft, itop, itemPaint);
            }

            if (items[4].bitmap != null) {
                ileft = getWidth() - PAD;
                itop = getHeight() - PAD;
                itemDstToDraw.set(ileft, itop, getWidth(), getHeight());
                canvas.drawBitmap(items[4].bitmap, null, itemDstToDraw, itemPaint);
            }
            if (isInStretch) {
                drawStretchIcon(canvas);
            }
        }
    }

    private void drawStretchIcon(Canvas canvas) {
        // 首先画上四条边框
        drawRim(canvas);
        // 再画按钮，宽角120度的矩形
        int r = STRETCH_ICON_R;
        float length = r / 2f, width = (float) (length / Math.sqrt(3));
        if (commonPath == null) commonPath = new Path();
        // 上
        commonPath.reset();
        int viewWidth = getWidth();
        float centerX = viewWidth / 2f;
        commonPath.moveTo(centerX - length, r / 2f);   //左
        commonPath.lineTo(centerX, r / 2f - width);    // 上
        commonPath.lineTo(centerX + length, r / 2f);   // 右
        commonPath.lineTo(centerX, r / 2f + width);    //下
        commonPath.close();
        canvas.drawPath(commonPath, rimPaint);

        // 下
        commonPath.reset();
        int viewHeight = getHeight();
        commonPath.moveTo(centerX - length, viewHeight - r / 2f); //左
        commonPath.lineTo(centerX, viewHeight - r / 2f - width); // 上
        commonPath.lineTo(centerX + length, viewHeight - r / 2f); // 右
        commonPath.lineTo(centerX, viewHeight - r / 2f + width); //下
        commonPath.close();
        canvas.drawPath(commonPath, rimPaint);

        // 左
        commonPath.reset();
        float centerY = viewHeight / 2f;
        commonPath.moveTo(r / 2f - width, centerY); //左
        commonPath.lineTo(r / 2f, centerY - length); // 上
        commonPath.lineTo(r / 2f + width, centerY); // 右
        commonPath.lineTo(r / 2f, centerY + length); //下
        commonPath.close();
        canvas.drawPath(commonPath, rimPaint);

        // 右
        commonPath.reset();
        commonPath.moveTo(viewWidth - r / 2f - width, centerY); //左
        commonPath.lineTo(viewWidth - r / 2f, centerY - length); // 上
        commonPath.lineTo(viewWidth - r / 2f + width, centerY); // 右
        commonPath.lineTo(viewWidth - r / 2f, centerY + length); //下
        commonPath.close();
        canvas.drawPath(commonPath, rimPaint);

    }

    private void drawRim(Canvas canvas) {
        if (commonPath == null || rimPaint == null) {
            commonPath = new Path();
            initRim();
        }
        float rimLeft = PAD / 2f, rimTop = PAD / 2f, rimRight = getWidth() - PAD / 2f, rimBottom = getHeight() - PAD / 2f;
        //上边的线
        canvas.drawLine(rimLeft, rimTop, rimRight, rimTop, rimPaint);
        //左边的线
        canvas.drawLine(rimLeft, rimTop, rimLeft, rimBottom, rimPaint);
        //下边的线
        canvas.drawLine(rimLeft, rimBottom, rimRight, rimBottom, rimPaint);
        //右边的线
        canvas.drawLine(rimRight, rimTop, rimRight, rimBottom, rimPaint);
    }

    public Bitmap getSrcBitmap() {
        return srcBitmap;
    }

    public void releaseResource() {
        //回收图片时，需要设置setImageBitmap
        setImageBitmap(null);
        if (srcBitmap != null) {
            srcBitmap.recycle();
        }
        if (viewEraser != null) {
            viewEraser.releaseResource();
        }
        srcBitmap = null;
    }

    /**
     * @return 获取高除以宽的比
     */
    public float getHWRatio() {
        return (srcBitmap.getHeight() + PAD * 2) * 1f / (srcBitmap.getWidth() + PAD * 2);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        srcBitmap = bm;
        super.setImageBitmap(bm);
    }

    public float getCurPicWidth() {
        return srcBitmap.getWidth() * bmScaleRatio;
    }

    public float getCurPicHeight() {
        return srcBitmap.getHeight() * bmScaleRatio;
    }

    public int calculateTotalWidth() {
        return Math.round(srcBitmap.getWidth() * bmScaleRatio + PAD * 2);
    }

    public int calculateTotalHeight() {
        return Math.round(srcBitmap.getHeight() * bmScaleRatio + PAD * 2);
    }

    /**
     * @return 注意是布局中的x，不是View内的
     */
    public float getLayoutCenterX() {
        return (getLeft() + getRight()) / 2f;
    }

    /**
     * @return 注意是布局中的y，不是View内的
     */
    public float getLayoutCenterY() {
        return (getTop() + getBottom()) / 2f;
    }

    /**
     * 会加上一个起始宽度
     */
    public void changeCrackWidth(float totalMoveDis) {
        totalMoveDis = totalMoveDis > 0 ? totalMoveDis : 0;
        rdd.crackWidth = RendDrawDate.START_CRACK_WIDTH + totalMoveDis / 3;
        invalidate();
    }

    private int getBmHeightInView() {
        return getHeight() - PAD - PAD;
    }

    private int getBmWidthInView() {
        return getWidth() - PAD - PAD;
    }

    public MRect getBmBoundInView() {
        return new MRect(PAD, PAD, getWidth() - PAD - PAD, getHeight() - PAD - PAD);
    }

    public MRect getBmBoundInLayout() {
        return new MRect(getLeft() + PAD, getTop() + PAD, getRight() - PAD, getBottom() - PAD);
    }

    public static boolean isIsInTest() {
        return isInTest;
    }

    public boolean smallRepeal() {
        if (viewEraser != null && viewEraser.isInErasing()) {
            viewEraser.smallRepeal();
            return true;
        }
        if (rendState != null && rendState.isIn_moveFrag()) {
            repealRend();
            return true;
        }
        if (oldBm != null) {
            setImageBitmap(oldBm);
            if (lastOperation == OPERATION_STRETCH) {
                changeSize(Math.round(calculateTotalWidth()), Math.round(calculateTotalHeight()));
            }
            oldBm = null;
            if (repealRedoListener != null) {
                repealRedoListener.canRepeal(false);
            }
            return true;
        }
        return false;
    }

    public void smallRedo() {
        if (viewEraser != null && viewEraser.isInErasing()) {
            viewEraser.smallRedo();
        }
    }

    /**************************************** 缩放拉伸相关 *******************************************/

    /**
     * 是否处于拉伸的手势中，注意和是否显示拉伸按钮区分
     */
    public boolean isInStretchGesture() {
        return isInStretch && stretchDir != STRETCH_NO;
    }

    /**
     * 是否和是否显示拉伸按钮区分，注意处于拉伸的手势中
     */
    public boolean isInStretch() {
        return isInStretch;
    }

    public void switchStretchStatus(boolean showStretch) {
        isInStretch = showStretch;
        if (!isInStretch) {
            stretchDir = STRETCH_NO;
        }
        invalidate();
    }

    /**
     * 相对于本View的X,Y
     */
    public boolean onFirstFingerDown(float x, float y) {
        if (!judgeShowRim()) return false;//边框没显示出来，返回false
        MRect itemBound = new MRect();
        //        item方面的
        float rotation = getRotation();
        Matrix matrix = null;
        if (rotation != 0) { // 如果旋转了，因为这里用的系统的旋转，位置就变了,要加上旋转处理
            matrix = new Matrix();
            matrix.setRotate(rotation, getWidth() / 2f, getHeight() / 2f);
        }
        // 左边
        itemBound.left = 0;
        int stretch_click_r = (int) (STRETCH_ICON_R * 1.5); // 增大点击半径
        itemBound.top = getHeight() / 2f - stretch_click_r / 2f;
        itemBound.right = stretch_click_r;
        itemBound.bottom = getHeight() / 2f + stretch_click_r / 2f;
        if (matrix != null) {
            matrix.mapRect(itemBound);
        }
        if (itemBound.contains(x, y)) {
            if (LogUtil.debugTietuGesture) Log.d(TAG, "点击到向左拉伸");
            stretchDir = STRETCH_LEFT;
            return true;
        }

        // 右边
        itemBound.left = getWidth() - stretch_click_r;
        itemBound.top = getHeight() / 2f - stretch_click_r / 2f;
        itemBound.right = getWidth();
        itemBound.bottom = getHeight() / 2f + stretch_click_r / 2f;
        if (matrix != null) {
            matrix.mapRect(itemBound);
        }
        if (itemBound.contains(x, y)) {
            if (LogUtil.debugTietuGesture) Log.d(TAG, "点击到向右拉伸");
            stretchDir = STRETCH_RIGHT;
            return true;
        }

        // 上边
        itemBound.left = getWidth() / 2f - stretch_click_r / 2f;
        itemBound.top = 0;
        itemBound.right = getWidth() / 2f + stretch_click_r / 2f;
        itemBound.bottom = stretch_click_r;
        if (matrix != null) {
            matrix.mapRect(itemBound);
        }
        if (itemBound.contains(x, y)) {
            if (LogUtil.debugTietuGesture) Log.d(TAG, "点击到向上拉伸");
            stretchDir = STRETCH_TOP;
            return true;
        }

        // 下边
        itemBound.left = getWidth() / 2f - stretch_click_r / 2f;
        itemBound.top = getHeight() - stretch_click_r;
        itemBound.right = getWidth() / 2f + stretch_click_r / 2f;
        itemBound.bottom = getHeight();
        if (matrix != null) {
            matrix.mapRect(itemBound);
        }
        if (itemBound.contains(x, y)) {
            if (LogUtil.debugTietuGesture) Log.d(TAG, "点击到向下拉伸");
            stretchDir = STRETCH_BOTTOM;
            return true;
        }
        stretchDir = STRETCH_NO;
        return false;
    }


    public void onLastFingerUp(float v, float v1) {
        if (stretchDir != STRETCH_NO) {
            stretchDir = STRETCH_NO;
            stretchBm();
        }
    }


    /**
     * 获取bm在X方向缩放比例
     */
    public float getBmScaleX() {
        return bmScaleRatio;
    }

    /**
     * 获取bm在y方向缩放比例
     */
    public float getBmScaleY() {
        return bmScaleRatio;
    }


    /**
     * 缩放是针对的View，拉伸先针对View，当用户手指抬起来的时候在真的拉伸bm
     * 暂时不和缩放统一，那样要改的比较多
     * 这样最后画的时候取拉伸过得srmBm，再缩放绘制，只要fiv视图的位置对上了，是没问题的
     * 对于擦除，撕图，融合，翻转等的效果应该都能成立
     */
    public void stretchView(float dx, float dy) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getLayoutParams());
        //根据中心缩放：=当前位置=原中心-1/2长宽
        params.width = getWidth();
        params.height = getHeight();
        params.topMargin = getTop();
        params.leftMargin = getLeft();

        if (stretchDir == STRETCH_LEFT) {
            params.width = getWidth() - (int) dx;
            params.leftMargin = getLeft() + (int) dx;
        } else if (stretchDir == STRETCH_RIGHT) {
            params.width = getWidth() + (int) dx;
        } else if (stretchDir == STRETCH_TOP) {
            params.height = getHeight() - (int) dy;
            params.topMargin = getTop() + (int) dy;
        } else if (stretchDir == STRETCH_BOTTOM) {
            params.height = getHeight() + (int) dy;
        }
        if (params.width > AllData.screenWidth * 1.5 || params.height > AllData.screenHeight * 1.5)
            return;
        ViewGroup parent = (ViewGroup) getParent(); // null强制转换类型不会抛错
        if (parent != null) {
            parent.updateViewLayout(this, params);
        }
        if (LogUtil.debugTietuGesture) {
            Log.d(TAG, "拉伸贴图: " + dx + " , " + dy + " 方向 = " + stretchDir);
        }
    }

    /**
     * 拉伸本质上是改变bm的宽高比，额外多的一点是保证宽高在合适范围内
     */
    public void stretchBm() {
        float needWHRatio = getBmHeightInView() * 1f / getBmWidthInView();
        if (LogUtil.debugTietuGesture) {
            Log.d(TAG, "拉伸贴图bm， 需要的宽高 = " + getBmWidthInView() + " : " + getBmHeightInView());
        }
        //20201025 解决友盟bug java.lang.NullPointerException: Attempt to invoke virtual method 'int android.graphics.Bitmap.getWidth()' on a null object reference
        if (srcBitmap == null) {
            return;
        }
        int curW = srcBitmap.getWidth(), curH = srcBitmap.getHeight();
        float curWHRatio = curW * 1f / curH;
        // 1、使用底图进行缩放，避免多次缩小失真等
        // 2、对宽缩放还是对高缩放，在1的基础上，缩放宽还是高效果应该是一样的
        // 3、最后只需要避免缩放的图超过最大宽高即可
        if (needWHRatio != curWHRatio && curW > 0 && curH > 0) {
            float xRatio = 1, yRatio = 1;
            // 以宽为基准缩放src，如果太大或者太小，就以高为基准
            float[] wh = new float[]{curW, curW * needWHRatio};
            TietuSizeController.adjustWH(wh);
            if (wh[0] < 2 || wh[1] < 2) {
                // bitmap的scale的方法内部会算出这两个值的整数值，为0崩溃
                // 前面涉及到的计算过程比较多，难以判断哪里出了错，这里直接判断，防御式变成从
                return;
            }
            xRatio = wh[0] / curW;
            yRatio = wh[1] / curH;
            Matrix matrix = new Matrix();
            matrix.postScale(xRatio, yRatio); // 使用后乘
            oldBm = srcBitmap;
            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, curW, curH, matrix, true);
            if (originalFuseBm != null) {
                originalFuseBm = Bitmap.createBitmap(originalFuseBm, 0, 0, curW, curH, matrix, true);
            }
            // 注意好需要考虑缩放，缩放并没有实际的缩放bm，使用缩放比控制view实现的，所以这里改变了尺寸，要重新设置缩放比
            bmScaleRatio = getBmWidthInView() * 1f / srcBitmap.getWidth();
            if (LogUtil.debugTietuGesture) {
                Log.d(TAG, "拉伸贴图bm， 最后的出的宽高 = " + curW + " : " + curH);
            }
            setImageBitmap(srcBitmap);
            lastOperation = OPERATION_STRETCH;
            if (repealRedoListener != null) {
                repealRedoListener.canRepeal(true);
            }
        }
    }

    public float getBmScaleRatio() {
        return bmScaleRatio;
    }

    /****************************************** 撕图相关 *******************************************/

    public boolean startRend() {
        Log.e("-------- ", "开始撕图");
        if (srcBitmap == null) {
            return false;
        }
        oldBm = null;
        if (repealRedoListener != null) {
            repealRedoListener.canRepeal(false);
        }
        setRotation(0);
        rendInitBm = srcBitmap;
        rendState.startRendAndEnterReady();
        rdd = new RendDrawDate(srcBitmap);
        rdd.scaleRatioBeforeRend = bmScaleRatio;
        invalidate();
        return true;
    }

    /**
     * 计算锯齿路径
     * 经验：一些较复杂情况下，能用参数传到方法里面的，尽量传，尽量不要访问到类里面去，降低耦合性，代码看上去也简洁些
     */
    public boolean cacuCrackPath(MPoint rendTouch1, MPoint rendTouch2, RendDrawDate rdd) throws CacuCrackFail {

        //        Logcat.e("开始计算锯齿路径");

        // 首先将两个触摸点的坐标转换到原图的坐标上
        //        Logcat.e("原触摸点 = " + rendTouch1 + "  and  " + rendTouch2);

        //        // 还原旋转和位移
        //        float rotation = (float) (getRotation() / 180f * Math.PI);
        //        Logcat.e("View 旋转角度 = " + getRotation());
        //
        //        // 以bmCenter为圆心操作，用相对于View中心的坐标旋转,得到的是相对于View中心的坐标
        //        // 然后加上View中心相对于左上角的坐标，即是最终的坐标
        //        MPoint bmCenter = new MPoint(getLeft() + getWidth() / 2f,
        //                getTop() + getHeight() / 2f); // bmCenter = viewCenter = 旋转不旋转Center都一样
        //        Logcat.e("BmCenter = " + bmCenter);
        //        MPoint newP1 = new MPoint(rendTouch1.x - bmCenter.x, rendTouch1.y - bmCenter.y);
        //        MPoint newP2 = new MPoint(rendTouch2.x - bmCenter.x, rendTouch2.y - bmCenter.y);
        //        newP1 = GeoUtil.getCooderAfterRotate(bmCenter, newP1, rotation);
        //        newP2 = GeoUtil.getCooderAfterRotate(bmCenter, newP2, rotation); // 得到了旋转后相对于bmCenter的坐标
        //        Logcat.e();
        //
        //        float curBmWidth = getWidth() - pad * 2, curBmHeight = getHeight() - pad * 2;
        //        newP1.offset(curBmWidth / 2f, curBmHeight / 2f);
        //        newP2.offset(curBmWidth / 2f, curBmHeight / 2f);

        // 还原缩放
        MPoint touchP1_InBm = new MPoint(rendTouch1), touchP2_InBm = new MPoint(rendTouch2);
        touchP1_InBm.offset(-getBmLeftInLayout(), -getBmTopInLayout());
        touchP2_InBm.offset(-getBmLeftInLayout(), -getBmTopInLayout());

        touchP1_InBm.scale(1f / bmScaleRatio);
        touchP2_InBm.scale(1f / bmScaleRatio);


        MRect bmRect = new MRect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());
        //        Logcat.e("原图范围 = " + bmRect);
        SawtoothPathData data = SawtoothPathGenerator.generateRendPath(bmRect, touchP1_InBm, touchP2_InBm,
                bmScaleRatio, SawtoothShapeController.DEFAULT_SUB_PIC_MIN_WIDTH);

        if (data == null) {
            throw new CacuCrackFail("计算裂痕失败");
        }

        rdd.setRendTouchP(rendTouch1, rendTouch2);
        rdd.setSrcBmTouchPoint(touchP1_InBm, touchP2_InBm);

        Path stPath = new Path();
        setPathData(data.stPoints, stPath);
        rdd.setStPath(stPath);
        setPathData(data.points1, rdd.picPath[0]);
        rdd.picPath[0].close();
        setPathData(data.points2, rdd.picPath[1]);
        rdd.picPath[1].close();

        if (isInTest) {
            rdd.testPoints.clear();
            MPoint touchInView1 = new MPoint(rendTouch1);
            touchInView1.offset(-getLeft(), -getTop());
            rdd.testPoints.add(touchInView1);

            MPoint touchInView2 = new MPoint(rendTouch2);
            touchInView2.offset(-getLeft(), -getTop());
            rdd.testPoints.add(touchInView2);

            for (MPoint testPoint : data.testPoints) {
                MPoint np = new MPoint(testPoint);
                np.scale(rdd.scaleRatioBeforeRend);
                np.offset(PAD, PAD);
                rdd.testPoints.add(np);
            }
        }

        invalidate();
        return true;
    }

    private int getBmTopInLayout() {
        return getTop() + PAD;
    }

    public void setPathData(List<MPoint> sawtoothPath, Path path) {
        path.reset();
        if (sawtoothPath != null) { // 锯齿路径是针对原图的， 这里需要根据比例进行缩放，旋转和移动是View的，不用管
            for (int i = 0; i < sawtoothPath.size(); i++) {
                MPoint p = sawtoothPath.get(i);
                if (i == 0) {
                    path.moveTo(p.x, p.y);
                } else {
                    path.lineTo(p.x, p.y);
                }
            }
        }
    }

    /**
     * 绘制裂痕, 以图片放大到尺寸为依据移动碎片，保存的碎片的大小是原图的大小，然后
     * 这里根据用户撕开的时候的大小，有个比列，绘制到View上面，剩余的空间就是分开拉动的空间，over
     */
    private void drawCrack(Canvas canvas) {
        if (!rdd.stPathInView.isEmpty()) {
            RendDrawDate.crackPaint.setStrokeWidth(rdd.crackWidth);
            canvas.save();
            canvas.translate(PAD, PAD);
            canvas.drawPath(rdd.stPathInView, RendDrawDate.crackPaint);
            canvas.restore();
        }
    }

    private int getBmLeftInLayout() {
        return getLeft() + PAD;
    }

    //    @Override
    //    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    //        super.onLayout(changed, left, top, right, bottom);
    //        Log.d(TAG, "onLayout: " + changed);
    //    }

    /**
     * 这里的移动碎片，实际上是先变化View的尺寸，然后在根据缩放的大小，在onDraw里面绘图
     */
    public void cacuLayout() {
        // moveDis是沿着触摸点连线的距离，先把旋转变回去，即使用srcAngle，
        // 然后变成沿着x，y方向移动的距离，根据moveDis的符号决定扩大还是缩小
        //        Logcat.d("原View位置 = [" + getLeft() + " " + (getLeft() + getWidth()) + "  " +
        //                getTop() + " " + (getTop() + getHeight()) + " ]");
        MRect viewBoundInLayout = new MRect();
        viewBoundInLayout.left = min(rdd.fragBoundInLayout[0].left, rdd.fragBoundInLayout[1].left) - PAD;
        viewBoundInLayout.top = min(rdd.fragBoundInLayout[0].top, rdd.fragBoundInLayout[1].top) - PAD;
        viewBoundInLayout.right = max(rdd.fragBoundInLayout[0].right, rdd.fragBoundInLayout[1].right) + PAD;
        viewBoundInLayout.bottom = max(rdd.fragBoundInLayout[0].bottom, rdd.fragBoundInLayout[1].bottom) + PAD;

        //        Logcat.e(" \n移动后View 位置 " + viewBoundInLayout);
        relayout(viewBoundInLayout);
        invalidate();
    }

    private void relayout(MRect viewBoundInLayout) {
        if (getParent() instanceof TietuFrameLayout) {
            TietuFrameLayout.LayoutParams layoutParams = (TietuFrameLayout.LayoutParams) getLayoutParams();
            layoutParams.leftMargin = viewBoundInLayout.leftInt();
            layoutParams.topMargin = viewBoundInLayout.topInt();
            layoutParams.width = viewBoundInLayout.widthInt();
            layoutParams.height = viewBoundInLayout.heightInt();
            ((TietuFrameLayout) getParent()).updateViewLayout(this, layoutParams);
        }
    }

    /**
     * 保证的就是返回原对象，不要创建新的对象
     */
    public MRect getFragBoundInLayout(int i) {
        return rdd.fragBoundInLayout[i];
    }


    /**
     * 因为坐标系统一在Layout下面，所以进行坐标换算之后即可绘制
     * 根据前面计算出来的碎片在layout坐标下的bound，得到在View下的bound，
     * 再用canvas画到目标rect bound上去即可
     */
    public void drawPicFragment(Canvas canvas, float curw, float curh, RendDrawDate rdd) {
        for (int i = 0; i < 2; i++) {
            MRect r = cacuBoundIntView(rdd.fragBoundInLayout[i]);
            canvas.drawBitmap(rdd.fragBm[i], null, r, null);
        }
    }

    public Bitmap drawFragmentToBm() {
        if (!isIn_moveFrag()) {
            return srcBitmap;
        }
        // 宽高的控制，首先取碎片不缩放，即原图大小情况下的宽高，
        // 如果这个宽高超过了最大宽高，则使用最大宽高

        // 获取碎片在图片内的Bitmap坐标下的范围
        MRect[] fragBoundInBm = new MRect[2];
        for (int i = 0; i < 2; i++) {
            fragBoundInBm[i] = rdd.fragBoundInLayout[i].sub(getBmLeftInLayout(), getBmTopInLayout());
        }
        // 计算不准确，会出现left和top略小于0的情况，碎片都平移，另一边大于0没关系，可以画图的
        float dx = 0, dy = 0;
        for (int i = 0; i < 2; i++) {
            if (fragBoundInBm[i].left < 0) {
                dx = -fragBoundInBm[i].left;
            }
            if (fragBoundInBm[i].top < 0) {
                dy = -fragBoundInBm[i].top;
            }
        }
        fragBoundInBm[0].offset(dx, dy);
        fragBoundInBm[1].offset(dx, dy);

        MRect bmRect = new MRect(fragBoundInBm[0]);
        bmRect.union(fragBoundInBm[1]);

        // 变到原图src的大小
        fragBoundInBm[0].scale(1f / rdd.scaleRatioBeforeRend);
        fragBoundInBm[1].scale(1f / rdd.scaleRatioBeforeRend);
        bmRect.scale(1f / rdd.scaleRatioBeforeRend);

        // 不能超过最大的大小
        float ratio = bmRect.getScaleRatioOf_ANotBiggerThanB(new MRect(0, 0, TietuSizeController.getMaxWidth(), TietuSizeController.getMaxHeight()));

        // 最终大小
        fragBoundInBm[0].scale(ratio);
        fragBoundInBm[1].scale(ratio);
        bmRect.scale(ratio);
        LogUtil.d("撕图后所画图片大小为 ： " + bmRect);

        Bitmap bm = Bitmap.createBitmap(bmRect.widthInt(), bmRect.heightInt(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        for (int i = 0; i < 2; i++) {
            canvas.drawBitmap(rdd.fragBm[i], null, fragBoundInBm[i], BitmapUtil.getBitmapPaint());
        }
        return bm;
    }

    private void test(Canvas canvas) {
        if (!isInTest) return;
        Paint p = new Paint();
        p.setColor(Color.GREEN);
        p.setStrokeWidth(10);
        if (rdd != null && rdd.testPoints.size() >= 2) {
            canvas.drawLine(rdd.testPoints.get(0).x, rdd.testPoints.get(0).y,
                    rdd.testPoints.get(1).x, rdd.testPoints.get(1).y, p);

        }
        if (rdd != null && rdd.testPoints.size() >= 4) {
            p.setColor(Color.BLACK);
            canvas.drawLine(rdd.testPoints.get(2).x, rdd.testPoints.get(2).y,
                    rdd.testPoints.get(3).x, rdd.testPoints.get(3).y, p);
        }
    }

    public boolean isInRend() {
        return rendState.isIn_Rend();
    }

    public boolean isReady() {
        return rendState.isIn_Ready();
    }

    /**
     * 生成裂纹，进入增大裂纹状态
     */
    public boolean enter_increaseCrack(MPoint rendTouch1, MPoint rendTouch2) throws CacuCrackFail {
        boolean result;
        try {
            result = cacuCrackPath(rendTouch1, rendTouch2, rdd);
        } catch (CacuCrackFail e) {
            throw e;
        }

        if (!result) {
            return false;
        }
        rendState.enter_increaseCrack();
        changeCrackWidth(0);
        invalidate();
        return true;
    }

    public boolean isIn_increaseCrack() {
        return rendState.isIn_increaseCrack();
    }

    public void returnReady() {
        rendState.return_Ready();
        rdd.reset2_ready();
        invalidate();
    }

    /**
     * 注意！！！分离图片完成之后才能设置，那样才有数据，不然依赖它的View没有数据，出现空指针
     */
    public void enter_moveFrag() {
        dividePic();
        rendState.enter_moveFrag();
        if (mRendListener != null) {
            mRendListener.enter_increaseCrack();
        }
        if (repealRedoListener != null) {
            repealRedoListener.canRepeal(true);
        }
        invalidate();
    }

    private void dividePic() {
        rdd.dividePic(srcBitmap);
        rdd.cacuFragBoundInLayout(getBmLeftInLayout(), getBmTopInLayout());
        invalidate();
    }

    public boolean isIn_moveFrag() {
        if (rendState == null) return false;
        return rendState.isIn_moveFrag();
    }

    private void mergePic() {
        float nw = srcBitmap.getWidth() * rdd.scaleRatioBeforeRend + PAD + PAD;
        float nh = srcBitmap.getHeight() * rdd.scaleRatioBeforeRend + PAD + PAD;
        float dw = (getWidth() - nw) / 2, dh = (getHeight() - nh) / 2;
        float left = getLeft() + dw, top = getTop() + dh;
        relayout(new MRect(left, top, left + nw, top + nh));
    }

    public void setFragBoundInLayout(MRect newBound, int i) {
        rdd.fragBoundInLayout[i].set(newBound);
    }

    public MRect cacuBoundIntView(MRect r) {
        return r.sub(getLeft(), getTop());
    }

    public void return_increaseCrack() {
        mergePic();
        rdd.reset2_increaseCrack();
        rendState.return_increaseCrack();
    }

    /**
     * 连续多次撕图使用，目前不用这种方案，
     */
    public void finishOneRend() {
        if (isIn_moveFrag()) {
            // 将碎片画到Bitmap上, 然后替换src，重置一些数据
            srcBitmap = drawFragmentToBm();
            bmScaleRatio = (getWidth() * 1f - PAD - PAD) / srcBitmap.getWidth();
            setImageBitmap(srcBitmap);
            setAdjustViewBounds(true);
            setScaleType(ScaleType.FIT_CENTER);
            requestLayout();
            LogUtil.d("完成一次撕图");
        }
        // 清空撕图相关数据，就像一个一般的贴图View一样
        rendState.return_Ready();
        rdd.reset2_ready();
        invalidate();
    }

    public void finishAllRend() {
        if (isIn_moveFrag()) {
            // 将碎片画到Bitmap上, 然后替换src，重置一些数据
            //            mLastSrcBm = srcBitmap; 展示不做
            oldBm = srcBitmap;
            srcBitmap = drawFragmentToBm();
            if (oldBm == srcBitmap) { // 没有变
                oldBm = null;
            }
            bmScaleRatio = (getWidth() * 1f - PAD - PAD) / srcBitmap.getWidth();
            setImageBitmap(srcBitmap);
            setAdjustViewBounds(true);
            setScaleType(ScaleType.FIT_CENTER);
            requestLayout();
            if (repealRedoListener != null) {
                repealRedoListener.canRepeal(true);
            }
            lastOperation = OPERATION_REND;
        }
        // 清空撕图相关数据，就像一个一般的贴图View一样
        rendState.return_notRend();
        rdd = null;
        refreshRepealRedo();
        invalidate();
        LogUtil.d("完成撕图");
    }

    public void cancelAllRend() {
        if (rendInitBm != null) {
            repealRend();
        }
        rendState.return_notRend();
        rdd = null;
        invalidate();
        LogUtil.d("取消撕图");
    }

    private void repealRend() {
        bmScaleRatio = rdd.scaleRatioBeforeRend;
        float centerX = (getLeft() + getRight()) / 2f, centerY = (getTop() + getBottom()) / 2f;
        float initW = rendInitBm.getWidth() * bmScaleRatio + PAD + PAD;
        float initH = rendInitBm.getHeight() * bmScaleRatio + PAD + PAD;
        relayout(new MRect(centerX - initW / 2, centerY - initH / 2,
                centerX + initW / 2, centerY + initH / 2));
        setImageBitmap(rendInitBm);
        rendState.return_Ready();
    }

    public void swapFragData() {
        rdd.swapFragData();
    }

    public void setRendListener(RendListener rendListener) {
        mRendListener = rendListener;
    }

    /**************************************** 橡皮相关 ********************************************/

    public float getRendInitScaleRatio() {
        return rdd.scaleRatioBeforeRend;
    }

    /**
     * 相对于本View左上角的位置，View发生旋转等，仍然是相对于旋转钱的左上角
     */
    public void onEraseDown(float x, float y) {
        viewEraser.eraserDown(x, y);
    }

    /**
     * 相对于本View左上角的位置，View发生旋转等，仍然是相对于旋转钱的左上角
     */
    public void eraseMove(float dx, float dy) {
        viewEraser.eraserMove(dx, dy);
    }

    /**
     * 相对于本View左上角的位置，View发生旋转等，仍然是相对于旋转钱的左上角
     */
    public void eraseUp(float x, float y) {
        viewEraser.eraserUp(x, y);
    }

    public boolean startErase(PTuActivityInterface pTuActivityInterface, boolean isMakeBaozouFace) {
        if (srcBitmap == null) return false;
        oldBm = srcBitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (viewEraser == null) {
            ViewEraser.ViewEraserInterface eraserInterface = new ViewEraser.ViewEraserInterface() {
                @Override
                public View getView() {
                    return FloatImageView.this;
                }

                @Override
                public float getBmScaleRatio() {
                    return bmScaleRatio;
                }

                @Override
                public Bitmap getSrcBitmap() {
                    return srcBitmap;
                }

                @Override
                public void setBitmap(Bitmap bm) {
                    FloatImageView.this.setImageBitmap(bm);
                }

                @Override
                public float getBmLeft() {
                    return getPaddingLeft();
                }

                @Override
                public float getBmTop() {
                    return getPaddingTop();
                }
            };
            viewEraser = new ViewEraser(getContext(), eraserInterface, repealRedoListener, pTuActivityInterface);
        } else {
            viewEraser.reInitData();
        }
        if (isMakeBaozouFace) {
            viewEraser.enlargePaintWidth(1.2);
        }
        // draw里面使用橡皮，会使用到 xfermode, color filter, or alpha,需要设置这个属性才能生效
        // 使用完之后取消此属性，尽量不影响性能
        setLayerType(LAYER_TYPE_HARDWARE, null);
        if (repealRedoListener != null) {
            repealRedoListener.canRepeal(false);
        }
        return true;
    }

    public void finishErase() {
        toChosen();
        Bitmap erasedBm = BitmapUtil.eraseBmByPath(srcBitmap, viewEraser.getOperateList(), true);
        if (erasedBm != srcBitmap) {
            oldBm = srcBitmap;
            if (originalFuseBm != null) {
                originalFuseBm = BitmapUtil.eraseBmByPath(originalFuseBm, viewEraser.getOperateList(), true);
            }
        }
        lastOperation = OPERATION_ERASE;
        setImageBitmap(erasedBm);
        setLayerType(LAYER_TYPE_NONE, null);
        viewEraser.setInErasing(false);
        refreshRepealRedo();
    }

    public void cancelErase() {
        if (viewEraser != null) {
            viewEraser.cancelErase();
        }
        setImageBitmap(oldBm);
        setLayerType(LAYER_TYPE_NONE, null);
        refreshRepealRedo();

    }

    public ViewEraser getEraser() {
        return viewEraser;
    }


    void setOfGifFrames(boolean[] ofFrames) {
        mOfFrames = ofFrames;
    }

    @Nullable
    public boolean[] getOfFrames() {
        return mOfFrames;
    }

    public boolean isLocked() {
        return mOperateState.isIn_locked();
    }

    public void toPreview() {
        if (!mOperateState.isIn_preview()) {
            mOperateState.toPreview();
            invalidate();
        }
    }

    public void toChosen() {
        if (!mOperateState.isIn_Chosen()) {
            mOperateState.toChosen();
            invalidate();
        }
    }

    public void toLocked() {
        if (!mOperateState.isIn_locked()) {
            mOperateState.toLocked();
            invalidate();
        }
    }

    /**
     * @param currentRatio 在原来倍数的基础上乘以这么多倍数
     */
    public void scale(float currentRatio) {
        scaleTo(bmScaleRatio * currentRatio);
    }

    /**
     * @param totalRatio 总的比例变成这么多
     */
    public void scaleTo(float totalRatio) {
        bmScaleRatio = totalRatio;
        changeSize(Math.round(calculateTotalWidth()), Math.round(calculateTotalHeight()));
    }

    public void changeSize(int w, int h) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        //根据中心缩放：=当前位置=原中心-1/2长宽
        params.width = w;
        params.height = h;
        params.leftMargin = Math.round(getLayoutCenterX() - params.width / 2f);
        params.topMargin = Math.round(getLayoutCenterY() - params.height / 2f);
        ViewGroup parent = (ViewGroup) getParent(); // null强制转换类型不会抛错
        if (parent != null) {
            parent.updateViewLayout(this, params);
        }
    }

    public void flip(int reverseDic) {
        oldBm = srcBitmap;
        srcBitmap = BitmapUtil.flip(srcBitmap, reverseDic);
        if (originalFuseBm != null) {
            originalFuseBm = BitmapUtil.flip(originalFuseBm, reverseDic);
        }
        if (repealRedoListener != null) {
            repealRedoListener.canRepeal(false);
        }
        setImageBitmap(srcBitmap);
        lastOperation = OPERATION_FLIP;
        refreshRepealRedo();
    }

    public boolean onBackPressed() {
        return viewEraser != null && viewEraser.onBackPressed();
    }

    public void fuseBaoZouFace(PtuSeeView ptuSeeView) {
        try {
            Bitmap bm2Fuse;
            if (originalFuseBm != null) {
                bm2Fuse = originalFuseBm;
            } else {
                if (!srcBitmap.isMutable()) {
                    srcBitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
                }
                bm2Fuse = srcBitmap;
                originalFuseBm = srcBitmap;
            }
            Bitmap newBm = BaoZouFaceFuse.fuseTietu(bm2Fuse, this, ptuSeeView, null);
            if (newBm != null) {
                oldBm = srcBitmap;
                setImageBitmap(newBm);
                lastOperation = OPERATION_FUSE;
            } else {
                ToastUtils.show("抱歉，融合失败了！");
            }
            refreshRepealRedo();
        } catch (Exception e) {
            Log.e(TAG, "fuseBaoZouFace: " + e.getMessage());
            ToastUtils.show("抱歉，融合出错了！");
        }
    }


    public Bitmap fuseBaoZouFaceNonUI(Bitmap baseBm, PtuSeeView ptuSeeView) {
        try {
            Bitmap bm2Fuse;
            if (originalFuseBm != null) {
                bm2Fuse = originalFuseBm;
            } else {
                if (!srcBitmap.isMutable()) {
                    srcBitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
                }
                bm2Fuse = srcBitmap;
                originalFuseBm = srcBitmap;
            }
            Bitmap newBm = BaoZouFaceFuse.fuseTietu(bm2Fuse, this, ptuSeeView, baseBm);
            return newBm;
        } catch (Exception e) {
        }
        return null;
    }

    public void setTietuTags(String tietuTags) {
        this.tietuTags = tietuTags;
    }

    @Nullable
    public String getTietuTags() {
        return tietuTags;
    }


    public void setLastOperation(int operation) {
        this.lastOperation = operation;
    }

    public int getLastOperation() {
        return lastOperation;
    }

    public ViewEraser getTietuEraser() {
        return viewEraser;
    }

    private void refreshRepealRedo() {
        if (oldBm != null && repealRedoListener != null) {
            repealRedoListener.canRepeal(true);
            repealRedoListener.canRedo(false);
        }
    }

    public RendDrawDate getRendDrawData() {
        return rdd;
    }

    public boolean isAutoAdd() {
        return isAutoAdd;
    }

    public void setIsAutoAdd(boolean isAutoAdd) {
        this.isAutoAdd = isAutoAdd;
    }

    public MRect getSureBound() {
        return getItemBound(2, 0);
    }
}
