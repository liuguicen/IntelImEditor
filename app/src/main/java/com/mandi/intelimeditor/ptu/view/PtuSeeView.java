package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.BitmapUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.imageProcessing.FaceFeatureDetector;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraser;

import org.jetbrains.annotations.NotNull;

/**
 * 内部原理：添加什么东西上去就在sourceBitmap上面添加（用了一个全局的sourceCanvas，除此之外，此canvas不起其它作用）
 * <P>绘图时，根据手势会改变相应的参数，然后根据相应的参数，到sourceBitmap上面剪切一个子图下来，用BitmapDrawable显示出来</P>
 * <p>sourceBitmap可能会被替换，此时尺寸大小也可能会被改变</p>
 * 注意： 每次缩放要寻改的地方有三个，totalRatio，currentRatio
 * <P>要学到的一个东西是，事件处理和绘图动作处理分开来，即我是视图，那么事件交给其它模块处理，只需通过缩放接口，我这边缩放即可
 * <p>职责分割，模块分割，单一职责等原理，能分割的职责尽量分割开来，尤其会变复杂的代码
 * <P>自己目前这个部分里很多代码都做得不好的，糅在一起了
 */
public class PtuSeeView extends PicGestureView {
    String TAG = "PtuView";
    private boolean canDoubleClick = true;
    private float minRatio;
    public boolean canDiminish = true;
    /**
     * 防止空，用来替代的图片的宽
     */
    public static final int REPLACE_LEN = 2;
    private Rect totalBound = new Rect(0, 0, REPLACE_LEN, REPLACE_LEN); // 不知哪里出的，会空指针，避免之
    private boolean canScale;
    /**
     * 默认透明背景色不显示，设置之后用特殊形式显示出来
     */
    private boolean isShowTransparentBg;
    private Paint transparentPaint;
    private ViewEraser viewEraser;
    private float[] faceLandmark;
    private float[] faceBoxes;

    public void setCanRotate(boolean canRotate) {
        this.canRotate = canRotate;
    }

    public boolean isCanRotate() {
        return canRotate;
    }

    boolean canRotate = false;

    /***
     * 只用于显示背景颜色，不用显示图片
     */
    private boolean isOnlyShowColor = false;
    private int mOnlyShowColor_color = 0x00000000;

    /**
     * 每次刷新0.0002倍
     */
    public static final float SCALE_FREQUENCE = 0.0002f;
    public static float MAX_RATIO = 8;

    /**
     * 最近的x的位置,上一次的x的位置，y的
     */
    public float lastX = -1, lastY = -1;
    /**
     * 最近一次用于缩放两手指间的距离
     */
    public float lastDis;

    /**
     * 中的缩放比例，其它的是辅助，放大时直接需要就是一个totalRatio
     */
    public float totalRatio = 1f;
    /**
     * mContext
     */
    public Context context;
    /**
     * 原图片
     */
    public Bitmap sourceBitmap;
    public Bitmap tempBitmap;

    /**
     * 用于处理图片的矩阵
     */
    private Matrix matrix = new Matrix();


    /**
     * 原图片的宽度,高度
     */
    public int srcPicWidth = REPLACE_LEN, srcPicHeight = REPLACE_LEN;
    /**
     * 当前缩放比例下，以PtuView坐标系下（=纵坐标系）下右上角x坐标，y坐标，
     * 以view的右上角为原点，（0,0）
     */
    public int picLeft = 0, picTop = 0;

    public Rect getSrcRect() {
        return srcRect;
    }

    public Rect getDstRect() {
        return dstRect;
    }

    /**
     * 图片的局部，要显示出来的部分
     */
    public Rect srcRect = new Rect(0, 0, REPLACE_LEN, REPLACE_LEN);
    /**
     * 要绘制的总图在view的canvas上面的位置,
     */
    public Rect dstRect = new Rect(0, 0, REPLACE_LEN, REPLACE_LEN);

    private Paint picPaint;

    public float initRatio = 1f;
    /**
     * 当前图片的宽和高
     */
    public int curPicWidth = REPLACE_LEN, curPicHeight = REPLACE_LEN;

    /**
     * 目前只用于P图后添加数据的
     * 和draw无关
     */
    public Canvas sourceCanvas;

    private PTuActivityInterface pTuActivityInterface;

    public PtuSeeView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PtuSeeView(Context context, AttributeSet set) {
        super(context, set);
        this.context = context;
        init();
    }

    /**
     * 一开始的初始化不能获取totalBound，需要显示出来以后才有totalBound的
     *
     * @param totalBound 总的FrameLayout图片的范围
     */
    public PtuSeeView(Context context, Bitmap bitmap, Rect totalBound) {
        super(context);
        this.context = context;
        this.totalBound = totalBound;
        init();
        setBitmapAndInit(bitmap, totalBound);
    }

    private void init() {
        picPaint = new Paint();
        picPaint.setDither(true);
        canDiminish = true;
        isOnlyShowColor = false;
        canScale = true;
        isShowTransparentBg = false;
    }

    public void setPTuActivityInterface(PTuActivityInterface pTuActivityInterface) {
        this.pTuActivityInterface = pTuActivityInterface;
    }

    /**
     * 根据提供的缩放比例，将p图的图片缩放到原图*缩放比例大小，并返回
     *
     * @param finalRatio 缩放的比例
     */
    public Bitmap getFinalPicture(float finalRatio) {
//        if (finalRatio != 1.0) {
//            Bitmap bitmap = Bitmap.createScaledBitmap(sourceBitmap, Math.round(srcPicWidth * finalRatio),
//                    Math.round(srcPicHeight * finalRatio), true);
//            /*if (sourceBitmap != null && bitmap.equals(sourceBitmap)) {
//                sourceBitmap.recycle();
//                sourceBitmap = null;
//            }*/
//        }
        return sourceBitmap;
    }

    public float getInitRatio() {
        return initRatio;
    }

    public float getTotalRatio() {
        return totalRatio;
    }

    /**
     * 根据路径解析出图片
     * 获取原始bitmap的宽和高
     * <p>创建并设置好用于保存的Bitmap
     * <p>获取当前何种的Ratio
     */
    public void setBitmapAndInit(String path, Rect totalBound) {
        setBitmapAndInit(BitmapUtil.getLosslessBitmap(path), totalBound);
    }

    /**
     * 图片可为空，为空时显示透明的图一张
     */
    public void setBitmapAndInit(@Nullable Bitmap bitmap, Rect totalBound) {
        this.totalBound = totalBound;
        sourceBitmap = enlargeTooSmallBm(bitmap);

        sourceCanvas = new Canvas(sourceBitmap);
        srcPicWidth = sourceBitmap.getWidth();
        srcPicHeight = sourceBitmap.getHeight();
        initRatio = Math.min(totalBound.width() * 1f / srcPicWidth,
                totalBound.height() * 1f / srcPicHeight); // 让长或者宽占满视图，以此得到缩放比例
        totalRatio = initRatio;
        minRatio = Math.min(totalBound.width() * 1f / 2 / srcPicWidth, totalBound.height() * 1f / 3 / srcPicHeight);
        curPicWidth = (int) (srcPicWidth * totalRatio + 0.5f);//简略的四舍五入
        curPicHeight = (int) (srcPicHeight * totalRatio + 0.5f);
        picLeft = (totalBound.width() - curPicWidth) / 2;
        picTop = (totalBound.height() - curPicHeight) / 2;
        getConvertParameter(curPicWidth, curPicHeight, picLeft, picTop);

        invalidate();
    }

    /**
     * 对于很小的图，使用目前的绘制方法将另一张bm绘制上去时，会出现明显的锯齿、模糊失真
     * 原因应该是假如现在要将贴图绘制到这个小底图的局部区域，只有3*3大小，这个3*3区域对应到屏幕上的显示区域是30 *30，
     * 现在要把300*300的图绘制上去，又这么3层关系，
     * Android目前的方法， 应该平均采样，取3行*3列，后又显示到屏幕的30*30区域，就失真了
     * 没绘制上去的时候为什么没失真，没绘制的时候贴图是在屏幕上，300->30不太会失真，
     * 底图3*3区域为什么没失真，应该是底图3*3区域，显示的就是原始的图，只是放大了，没有经过缩小采样，所以不怎么失真
     * 进一步的，似乎原图片如果比View小，显示的时候放大了，这时候把贴图绘制上去，都会经历缩小采样，再放大显示的过程，都会有些失真
     * 目前似乎没看到好的方法，美图秀秀，天天P图都会这样
     * 自己想到直接放大底图的方式，目前看到picsArt也是这样做的
     * <p>
     * 解决方法是当srcBm，放大srcBm
     *
     * @return 原图或者放大后的图
     */
    @NotNull
    public Bitmap enlargeTooSmallBm(Bitmap bitmap) {
        if (bitmap == null)
            sourceBitmap = Bitmap.createBitmap(REPLACE_LEN, REPLACE_LEN, Bitmap.Config.ARGB_4444);
        else {
            sourceBitmap = bitmap;
            // gif的图不自动放大
            if (pTuActivityInterface != null && pTuActivityInterface.getGifManager() != null)
                return sourceBitmap;


            int minSupportWidth = AllData.getScreenWidth() / 3;
            int minSupportHeight = AllData.getScreenHeight() / 3;
            if (sourceBitmap.getWidth() < minSupportWidth && sourceBitmap.getHeight() < minSupportHeight) {
                int usedWidth = minSupportWidth + 10, usedHeight = minSupportHeight + 10; // 大一点，避免计算误差导致增大后的宽度又小于最小支持宽度了
                float widthRatio = usedWidth * 1f / sourceBitmap.getWidth(), heightRatio = usedHeight * 1f / sourceBitmap.getHeight();
                float ratio = Math.min(widthRatio, heightRatio); // 取小的那个，避免细长形状的图，导致占用过多内存

                sourceBitmap = Bitmap.createScaledBitmap(sourceBitmap,
                        Math.round(sourceBitmap.getWidth() * ratio),
                        Math.round(sourceBitmap.getHeight() * ratio),
                        true);

            }
        }
        return sourceBitmap;
    }

    public void setOnlyShowColor(@ColorInt int color, Rect totalBound) {
        sourceBitmap = Bitmap.createBitmap(totalBound.width(), totalBound.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(sourceBitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
        canvas.drawColor(color);
        setCanRotate(false);
        setCanScale(false);
        setBitmapAndInit(sourceBitmap, totalBound);
        mOnlyShowColor_color = color;
    }

    /**
     * 配合子功能，将此视图设置成不同的状态，然后具有不同的特性
     */
    public void switchStatus2Main() {
        isOnlyShowColor = false;
        setCanRotate(true);
        setCanScale(true);
        isShowTransparentBg = false;
    }

    public void switch2Draw() {
        isOnlyShowColor = false;
        isShowTransparentBg = true;
        setCanRotate(true);
        setCanScale(true);
    }

    public void switchStatus2Rend() {
        isOnlyShowColor = false;
        setCanRotate(false);
        setCanScale(false);
    }

    public void switchStatus2Dig() {
        isShowTransparentBg = true;
        setCanRotate(false);
        setCanScale(false);
    }

    public void setShowTransparentBg(boolean isShowTransparentBg) {
        this.isShowTransparentBg = isShowTransparentBg;
        invalidate();
    }

    /**
     * 显示Gif
     */
    public void switchStatus2Gif() {
        isShowTransparentBg = false;
        isOnlyShowColor = false;
        setCanScale(false);
        setCanRotate(false);
    }

    public void onlyShowColor_setColor(@ColorInt int color) {
        if (isOnlyShowColor) {
            sourceBitmap = Bitmap.createBitmap(totalBound.width(), totalBound.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sourceBitmap);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                    Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
            canvas.drawColor(color); // 将颜色绘制上去即可
            mOnlyShowColor_color = color;
            invalidate();
        }
    }

    public int onlyShowColor_getColor() {
        return mOnlyShowColor_color;
    }

    public void setCanScale(boolean canScale) {
        this.canScale = canScale;
    }

    public boolean getCanScale() {
        return canScale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                LogUtil.d(TAG, "经过了down");
                if (Util.DoubleClick.isDoubleClick()) {
                    if (!canDoubleClick) return true;//不支持双击
                    if (lastX < dstRect.left || lastX > dstRect.right || lastY < dstRect.top
                            || lastY > dstRect.bottom)//点击不在图片范围内
                        return true;
                    if (initRatio * 0.97 <= totalRatio && totalRatio < MAX_RATIO * 0.95) {//进行放大
                        float curRatio = MAX_RATIO / totalRatio;
                        scale(event.getX(), event.getY(), curRatio);
                    } else {
                        resetShow();
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Util.DoubleClick.cancel();//多个手指，或者移动了，双击取消
                lastDis = GeoUtil.getDis(event.getX(0), event.getY(0), event.getX(1), event.getY(1));

                //移动
                lastX = (event.getX(0) + event.getX(1)) / 2;
                lastY = (event.getY(0) + event.getY(1)) / 2;
            case MotionEvent.ACTION_MOVE:
                if (GeoUtil.getDis(lastX, lastY, event.getX(), event.getY()) > 5)//防抖动
                    Util.DoubleClick.cancel();
                if (event.getPointerCount() == 1) {
                    move(event.getX(), event.getY());
                } else {
                    //缩放
                    float endD = GeoUtil.getDis(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    float currentRatio = endD / lastDis;
                    lastDis = endD;
                    scale((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2, currentRatio);

                    //移动
                    move((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //剩下一个手指时，获取该手指的位置，后面跟着能进行移动
                if (event.getPointerCount() == 2) {
                    int index = event.getActionIndex() == 0 ? 1 : 0;
                    lastX = event.getX(index);
                    lastY = event.getY(index);
                    //当缩小范围超过最小值时
                    if (canDiminish && totalRatio < minRatio) {
                        scale(totalBound.width() / 2f, totalBound.height() / 2f, minRatio / totalRatio);
                    }
                }
                if (event.getPointerCount() == 3) {
                    int index = event.getActionIndex();

                    int i0, i1;
                    if (index == 0) {
                        i0 = 1;
                        i1 = 2;
                    } else if (index == 1) {
                        i0 = 0;
                        i1 = 2;
                    } else {
                        i0 = 0;
                        i1 = 1;
                    }
                    //缩放
                    lastDis = GeoUtil.getDis(event.getX(i0), event.getY(i0), event.getX(i1), event.getY(i1));

                    //移动
                    lastX = (event.getX(i0) + event.getX(i1)) / 2;
                    lastY = (event.getY(i0) + event.getY(i1)) / 2;
                }
            default:
                break;
        }
        return true;
    }

    public void move(float curX, float curY) {
        int tx = picLeft;
        picLeft += curX - lastX;
        lastX = curX;
        if (picLeft > 0 || Math.abs(picLeft) + totalBound.width() > curPicWidth)//如果x超出界限，x方向就不移动了
            picLeft = tx;

        int ty = picTop;
        picTop += curY - lastY;
        lastY = curY;
        if (picTop >= 0 || Math.abs(picTop) + totalBound.height() > curPicHeight)//如果y超出界限，y方向就不移动了
            picTop = ty;

        if (picLeft == tx && picTop == ty)//x，y方向都移动不了，就不移动的标志
            return;
        getConvertParameter(curPicWidth, curPicHeight, picLeft, picTop);
        invalidate();
    }

    /**
     * 这里判断条件，探后只管缩放，不控制size大小
     * 缩放后调整到中间位置
     */
    public void scale(float scaleCenterX, float scaleCenterY, float currentRatio) {
        //对于缩放的几种限制情况，必须放到缩放函数内部，这样其它地方调用的时候才不会超出条件
        if (!canScale) return;
        if (currentRatio > 1 - SCALE_FREQUENCE && currentRatio < 1 + SCALE_FREQUENCE)
            return;//本次缩放比例不够大
        if (totalRatio * currentRatio > MAX_RATIO)
            return;//总的缩放比例超出了最大范围
        if (!canDiminish && currentRatio * totalRatio < initRatio)//不支持比屏幕小时
            currentRatio = initRatio / totalRatio;

        // 获取当前图片的宽、高
        totalRatio *= currentRatio;
        curPicWidth = Math.round(srcPicWidth * totalRatio);
        curPicHeight = Math.round(srcPicHeight * totalRatio);

        //如果某一边超出边界，则使用手指的中心，否则使用图片的中心
        //缩放中心随手指移动，因为缩放时双指同时移动也会导致图片移动，故不采用固定的缩放中心

        //高精度的计算缩放后坐标
        float[] xy = new float[2];
        GeoUtil.getScaledCoord(xy, scaleCenterX, scaleCenterY, picLeft, picTop, currentRatio);
        picLeft = Math.round(xy[0]);
        picTop = Math.round(xy[1]);
        adjustEdge();
        getConvertParameter(curPicWidth, curPicHeight, picLeft, picTop);
        invalidate();
    }

    public void adjustEdge() {
        //当缩放到view内部时，调整图片的边界
        if (curPicWidth < totalBound.width()) picLeft = (totalBound.width() - curPicWidth) / 2;
        else {
            if (picLeft + curPicWidth < getRight()) picLeft = getRight() - curPicWidth;
            else if (picLeft > 0) picLeft = 0;
        }

        if (curPicHeight < totalBound.height()) picTop = (totalBound.height() - curPicHeight) / 2;
        else {
            if (picTop + curPicHeight < getBottom()) picTop = getBottom() - curPicHeight;
            else if (picTop > 0) picTop = 0;
        }
    }

    /**
     * @return 图片在PtuFrameLayout上的相对位置
     */
    public Rect getPicBound() {
        return dstRect;
    }

    /**
     * 绘制，这里根据不同的当前状态来绘制图片CURRENT_STATUS
     */
    @Override
    public void onDraw(Canvas canvas) {
        if (isOnlyShowColor && mOnlyShowColor_color == 0) { // 透明色特殊显示
            if (transparentPaint == null) {
                transparentPaint = ColorPicker.getTransparentPaint();
            }
            canvas.drawRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    transparentPaint);
            return;
        }
        if (isShowTransparentBg()) { // 5.0 以下的机器这里会出问题，不知道原因
            if (transparentPaint == null) {
                transparentPaint = ColorPicker.getTransparentPaint();
            }
            canvas.drawRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    transparentPaint);
        }
        canvas.save();
        int saveLayer = -Integer.MAX_VALUE;
        if (viewEraser != null && viewEraser.isInErasing()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 低于5.0的就不管了，问题不严重
            saveLayer = canvas.saveLayer(null, null);
        }
//        drawBmOldWay(canvas);
        drawBmNewWay(canvas);
        if (LogUtil.debugFace) {
            test_drawFaceFeature(canvas);
        }
        if (viewEraser != null && viewEraser.isInErasing()) {
            viewEraser.eraseInView(canvas);
        }
        if (saveLayer != -Integer.MAX_VALUE) {
            canvas.restoreToCount(saveLayer);
        }
        canvas.restore();
    }

    private void drawBmNewWay(Canvas canvas) {
        if (sourceBitmap == null || sourceBitmap.isRecycled()) return;
        canvas.drawBitmap(sourceBitmap, srcRect, dstRect, BitmapUtil.getBitmapPaint());
    }

    /**
     * 测试代码，画出人脸特征
     */
    private void test_drawFaceFeature(Canvas canvas) {
        // 人脸
        if (faceBoxes != null) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(Util.dp2Px(2));
            for (int i = 1; i < faceBoxes.length; i += 4) {
                if (i + 3 < faceBoxes.length) {
                    float left = faceBoxes[i + 0] * totalRatio + dstRect.left,
                            top = faceBoxes[i + 1] * totalRatio + dstRect.top,
                            right = faceBoxes[i + 2] * totalRatio + dstRect.left,
                            bottom = faceBoxes[i + 3] * totalRatio + dstRect.top;
                    canvas.drawLine(left, top, left, bottom, paint);
                    canvas.drawLine(left, top, right, top, paint);
                    canvas.drawLine(right, top, right, bottom, paint);
                    canvas.drawLine(left, bottom, right, bottom, paint);
                }
            }
        }
        if (faceLandmark != null) {
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            for (int i = 0; i < faceLandmark.length - 1; i += 2) {
                if (Util.idInArray(FaceFeatureDetector.KP_ID, i / 2) >= 0) {
                    paint.setStrokeWidth(Util.dp2Px(6));
                } else {
                    paint.setStrokeWidth(Util.dp2Px(2));
                }
                canvas.drawPoint(faceLandmark[i] * totalRatio + dstRect.left,
                        faceLandmark[i + 1] * totalRatio + dstRect.top, paint);
            }
        }
    }

    /**
     * 原来draw bm局部的方式，创建了bm，当时是因为缩放的时候这样清晰度高些，但是效率低
     * 但是不知道为什么现在看似乎清晰度没区别，是手机不同的问题？ 代码暂时保留
     */
    private void drawBmOldWay(Canvas canvas) {
        if (sourceBitmap == null || sourceBitmap.isRecycled()) return;
        //        如果是缩小，获取局部图片时就直接缩小，
        int srcWidth = srcRect.width();
        int srcHeight = srcRect.height();
//        LogUtil.d(TAG, "tempBitmap = " + srcWidth + " dstRect.width() :" + dstRect.width() +
//                " bitmap.width() =" + sourceBitmap.getWidth() + " x + width =" + srcRect.left + srcWidth);
//        LogUtil.d(TAG, "dstRect " + getDstRect() + " srcRect" +getSrcRect());
        if (srcWidth > dstRect.width() && srcHeight > 0) {
            float ratio = (dstRect.width()) * 1.0f / srcWidth;
            matrix.reset();
            matrix.setScale(ratio, ratio);
            tempBitmap = Bitmap.createBitmap(sourceBitmap, srcRect.left, srcRect.top,
                    srcWidth, srcHeight, matrix, true);
        } else {
            if (sourceBitmap.getWidth() >= srcRect.left + srcWidth
                    && srcWidth > 0 && srcHeight > 0) {
                tempBitmap = Bitmap.createBitmap(sourceBitmap, srcRect.left, srcRect.top,
                        srcWidth, srcHeight);
            }
        }
        if (tempBitmap != null && !tempBitmap.isRecycled()) {
            BitmapDrawable tempDrawable = new BitmapDrawable(context.getResources(), tempBitmap);
            tempDrawable.setDither(true);
            tempDrawable.setAntiAlias(true);
            tempDrawable.setFilterBitmap(true);
            tempDrawable.setBounds(dstRect);
            tempDrawable.draw(canvas);//将底图绘制到View上面到
            if (tempBitmap != null) {
                tempBitmap.recycle();
            }
        }
    }

    public boolean isShowTransparentBg() {
        return isShowTransparentBg && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * 比较常用的方法，将图片还原到开始的位置,情况，即长边与父布局长边对齐
     * <p>基本参数还原到初始化状态,可用于撤销重做等
     */
    public void resetShow() {
        totalRatio = initRatio;
        curPicWidth = Math.round(srcPicWidth * totalRatio);
        curPicHeight = Math.round(srcPicHeight * totalRatio);
        picLeft = (totalBound.width() - curPicWidth) / 2;
        picTop = (totalBound.height() - curPicHeight) / 2;
        getConvertParameter(curPicWidth, curPicHeight, picLeft, picTop);
        invalidate();
    }

    /**
     * 将原始的图片换掉,并且回收原始图片的资源，
     * <p> 会处理图片大小不同的情况
     * 会显示出来
     */
    public void replaceSourceBm(@Nullable Bitmap newBm) {
        if (newBm == sourceBitmap) {
            invalidate(); // 对象相同，但是里面的数据可能变了
            return;
        }
       /* if (sourceBitmap != null) {
            sourceBitmap.recycle();
            sourceBitmap = null;
        }*/
        setBitmapAndInit(newBm, totalBound);
    }

    public void onlyReplaceSrcBm(Bitmap bm) {
        sourceBitmap = bm;
    }

    /**
     * 将原始的图片换掉,不回收原始图片的资源，
     * <p> 会处理图片大小不同的情况
     * 会显示出来
     */
    public void replaceSourceBmNoRecycle(Bitmap newBm, boolean isGif) {
        setBitmapAndInit(newBm, totalBound);
    }

    /**
     * 获取变换后的参数
     * 获取当前的宽高，
     * <p>要绘制出的宽高，
     * <p>缩放后图片右上角顶点的位置
     * <p>绘画时用到的矩形，原图裁剪矩形srcRect，在画布上的位置矩形dstRect
     * <p/>
     */
    public void getConvertParameter(int curPicWidth, int curPicHeight, int picLeft, int picTop) {
        // 显示在屏幕上绘制的宽度、高度,目标矩形
        int leftInView = picLeft < 0 ? 0 : picLeft, topInView = picTop < 0 ? 0 : picTop;
        //图片宽与view宽构成两条线段平行相交问题，求交集，用右边小者减左边大者
        int drawWidth = Math.min(totalBound.right, picLeft + curPicWidth) - Math.max(0, picLeft);
        int drawHeight = Math.min(totalBound.bottom, picTop + curPicHeight) - Math.max(0, picTop);
        dstRect.set(leftInView, topInView, leftInView + drawWidth, topInView + drawHeight);

        //图片上的位置，源矩形
        int leftInPic = picLeft > 0 ? 0 : -picLeft, topInPic = picTop > 0 ? 0 : -picTop;
        int x = Math.round(leftInPic / totalRatio), y = Math.round(topInPic / totalRatio);
        int x1 = x + Math.round(drawWidth / totalRatio), y1 = y + Math.round(drawHeight / totalRatio);
        srcRect.set(x, y, x1, y1);
        //srcRect的边界不能超过bitmap的边界
        if (srcRect.left < 0) srcRect.left = 0;
        if (srcRect.top < 0) srcRect.top = 0;
        if (srcRect.right > srcPicWidth) srcRect.right = srcPicWidth;
        if (srcRect.bottom > srcPicHeight) srcRect.bottom = srcPicHeight;
    }

    /**
     * 释放资源，目前只有SourceBitmap一个
     */
    public void releaseResource() {
      /*  if (sourceBitmap != null) {
            sourceBitmap.recycle();
            sourceBitmap = null;
        }*/
    }

    // srcBm可能为空，目前不知哪儿来的
    @Nullable
    public Bitmap getSourceBm() {
        return sourceBitmap;
    }

    public Canvas getSourceCanvas() {
        return sourceCanvas;
    }

    public void setCanDoubleClick(boolean b) {
        canDoubleClick = b;
    }

    /**
     * 必须在setBitmapAndInit后面调用
     *
     * @param canLessThanScreen 是否能小于屏幕
     */
    public void setCanLessThanScreen(boolean canLessThanScreen) {
        canDiminish = canLessThanScreen;
    }

    public void setTotalBound(Rect totalBound) {
        this.totalBound = totalBound;
    }

    @Override
    public void rotate(float touchCenterX, float toucheCenterY, float angle) {

    }

    @Override
    public void adjustEdge(float dx, float dy) {

    }

    @Override
    public float adjustSize(float ratio) {
        return ratio;
    }

    @Override
    public boolean onClick(float x, float y) {
        return false;
    }

    /**
     * @param viewEraser null 表示不设置橡皮
     */
    public void injectViewEraser(ViewEraser viewEraser) {
        this.viewEraser = viewEraser;
        if (viewEraser != null) {
            // draw里面使用橡皮，会使用到 xfermode, color filter, or alpha,需要设置这个属性才能生效
            // 使用完之后取消此属性，尽量不影响性能
            setLayerType(LAYER_TYPE_HARDWARE, null);
        } else {
            // draw里面使用橡皮，会使用到 xfermode, color filter, or alpha,需要设置这个属性才能生效
            // 使用完之后取消此属性，尽量不影响性能
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    public void meshDeformation() {

    }

    public void setFaceBoxes(float[] faceBoxes) {
        this.faceBoxes = faceBoxes;
    }

    public void setFaceLandMark(float[] faceLandmark) {
        this.faceLandmark = faceLandmark;
    }
}
