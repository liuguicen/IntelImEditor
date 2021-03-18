package com.mandi.intelimeditor.ptu.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.MU;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.ptu.FloatView;
import com.mandi.intelimeditor.ptu.MicroButtonData;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.repealRedo.TextStepData;
import com.mandi.intelimeditor.ptu.view.IconBitmapCreator;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mathandintell.intelimeditor.R;

import java.util.ArrayList;
import java.util.Random;



/**
 * 添加textView的顶部视图
 * Created by Administrator on 2016/5/29.
 */
public class FloatTextView extends androidx.appcompat.widget.AppCompatEditText implements FloatView {
    private static String TAG = "FloatTextView";
    private static final int PARTIAL_RESET_ITEM_ID = 6;
    public static final int JUSTIFY_ITEM_ID = 1; // 对齐ID
    private static final int BG_ITEM_ID = 2;

    /**
     * 顶点的x，y坐标,相对于父组件的值，保证不会超出当前显示的图片的边界
     */
    private float fLeft, fTop;
    /**
     * 移动的顶点最后的位置
     */
    public float relativeX, relativeY;
    private static final String ITEM_EDTI = "edit";
    private static final String ITEM_PARTIAL_RESET = "bcenter";
    private static final String ITEM_JUSTIFY = "对齐";
    private Context mContext;
    public static int PAD = Util.dp2Px(24);

    public float currentRatio;
    private float mTextSize = 30;
    private String mHint = "点击输入文字";
    private Paint mPaint = new Paint();
    /**
     * floatView的宽和高，包括padding，保证加上mleft，mtop之后不会超出原图片的边界
     */
    public float mWidth, mHeight;
    private static int CUR_SHOW_STATUS;
    private int rimColor;
    private int itemColor;
    private int downShowState;
    private float minMoveDis = Util.dp2Px(3);
    private long downTime = 0;
    private float downY;
    private float downX;
    private boolean hasUp = true;
    private int lastSelectionId;
    private Rect picBound;
    private String mText = "";
    private int mBackGroundColor = 0x00000000;
    private Bitmap[] justifyBm;
    private Bitmap mResultBm;
    private TextStepData mTextStepData;
    private Bitmap mDialogBm;
    /**
     * 额外的底部margin，目前方式如果默认的margin容易引起混乱
     * 一些情况下PTu的图片底部有其它View，会挡住默认位置的文本框，将文本框抬上去一些
     * 比如gif帧
     */
    private int mExtraBottomMargin = 0;


    private Matrix rotateMatrix;
    /**
     * 文字方向，涉及翻转
     * 0 不翻转
     * 1 y翻转，x不翻转
     * 2 y翻转，x翻转
     * 3 y不翻转，x翻转
     */
    private int flipDirection = 0;

    /**
     * 边框的上下左右位置,相对于文本框的左上角,注意这个位置相对与文本的边界发生了偏移
     */
    private float rimLeft, rimTop, rimRight, rimBottom;
    private Rect rimRect;

    ArrayList<MicroButtonData> items = new ArrayList<>(8);
    private int transparency = 0;

    /**
     * 传入布局容器的宽高，确定view的位置
     */
    public FloatTextView(Context mContext, Rect picBound) {
        super(mContext);
        this.mContext = mContext;
        init(picBound);
    }

    public void setDownState() {
        downShowState = CUR_SHOW_STATUS;
    }

    public int getDownState() {
        return downShowState;
    }

    public int getPadding() {
        return PAD;
    }

    public int getShowState() {
        return CUR_SHOW_STATUS;
    }

    public float getRelativeY() {
        return relativeY;
    }

    @Override
    public void setRelativeX(float relativeX) {
        this.relativeX = relativeX;
    }

    @Override
    public void setRelativeY(float relativeY) {
        this.relativeY = relativeY;
    }


    public float getRelativeX() {
        return relativeX;
    }

    @Override
    public float getmWidth() {
        return mWidth;
    }

    @Override
    public float getmHeight() {
        return mHeight;
    }

    public float getfTop() {
        return fTop;
    }

    public float getfLeft() {
        return fLeft;
    }

    public FloatTextView(Context context) {
        super(context);
        mContext = context;
    }

    private void init(final Rect picBound) {
        Log.d(TAG, "initBeforeCreateView");
        //获取ptuView的范围
        this.picBound = picBound;
//        unlevelBound = new UnLevelRect();
        rotateMatrix = new Matrix();
/**
 * 设置文字布局
 */
        setGravity(Gravity.CENTER);
        final FrameLayout.LayoutParams layoutParms = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParms);

        setHint(mHint);
        setTextSize(mTextSize);
        setTextColorWithOpacity(255, Color.BLACK);
        setPadding(PAD, PAD, PAD, PAD);
        setBackground(null);
        setSelected(true);
        //获取view的宽和高
        getSizePreliminarily();

        //获取了实际的宽高之后才能获取初始位置
        fLeft = (picBound.left + picBound.right) / 2f - mWidth / 2;
        fTop = picBound.bottom - mHeight;
        CUR_SHOW_STATUS = STATUS_ITEM;
        refreshRimSize();
        initItems();
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastSelectionId = start + after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //原来的中心坐标
                float cx = fLeft + mWidth / 2, cy = fTop + mHeight / 2;
                mText = s.toString();
                setHint("");
                getSizePreliminarily();
                float tWidth = mWidth;
                //判断是否超出总长，超出时缩小
                float totalWidth = (picBound.right - picBound.left) * 2;
                if (mWidth > totalWidth) {
                    mTextSize = getTextSizeByWidth(mTextSize, totalWidth);
                    // mHeight*=(mWidth/tWidth);
                }

                float tHeight = mHeight;
                float totalHeight = (picBound.bottom - picBound.top) * 2;
                if (mHeight > totalHeight) {
                    mTextSize = getTextSizeByHeight(mTextSize, totalHeight);
                    // mWidth*=(mHeight/tHeight);
                }

                //按原来的的中心坐标缩放
                fLeft = cx - mWidth / 2;
                fTop = cy - mHeight / 2;
                changeLocation();
                if (lastSelectionId > 0) {
                    setCursorVisible(true);
                    requestFocus();
                    setSelection(lastSelectionId);
                }
            }
        });
        setLongClickable(false);
        setOnLongClickListener(v -> true);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
//        教训：View旋转之后判断触摸位置，反向旋转点而不是View，推而广之类似，两个相对变化、对称变化的东西，变化复杂的等价于变化简单的，选择简单的
//        float rotation = getRotation();
//        unlevelBound.set(l, t, r, t, r, b, l, b);
//        if (rotation >= 5) { // 旋转角度超过阈值时，处理旋转
//            float centerX = (l + r) / 2f;
//            float centerY = (t + b) / 2f;
//            roateMatrix.reset();
//            roateMatrix.setRotate(rotation, centerX, centerY);
//            if (isTest) {
//                ld("绕着中心点：" + centerX + ", " + centerY);
//                ld("旋转：" + rotation + " 度");
//                ld("转换前的矩形 = " + new Rect(l, t, r, b));
//            }
//            float[] coordinates = unlevelBound.getCoordinates(); // layout方法中的这里新建对象了，实在不好优化掉
//            roateMatrix.mapPoints(coordinates);
//            unlevelBound.set(coordinates);
//            if (isTest) {
//                ld("旋转后的矩形 = " + unlevelBound.toString());
//            }
//        }
    }

    private float getCenterX() {
        return (getLeft() + getRight()) / 2f;
    }

    private float getCenterY() {
        return (getTop() + getBottom()) / 2f;
    }

    public void initItems() {
        //getColor(int)已过时 替换方案
        rimColor = ContextCompat.getColor(mContext, R.color.float_rim_color);
        itemColor = ContextCompat.getColor(mContext, R.color.float_item_color);
        items.add(null);//第0个

        justifyBm = new Bitmap[3];
        justifyBm[0] = IconBitmapCreator.createJustifyIcon(PAD, itemColor, 0x00000000, 0);
        justifyBm[1] = IconBitmapCreator.createJustifyIcon(PAD, itemColor, 0x00000000, 1);
        justifyBm[2] = IconBitmapCreator.createJustifyIcon(PAD, itemColor, 0x00000000, 2);
        MicroButtonData item = new MicroButtonData(-1, -1, ITEM_JUSTIFY);
        item.mode = 0;
        item.bitmap = justifyBm[0];
        items.add(item);//第1个

        item = new MicroButtonData(-1, -1, ITEM_EDTI);
        item.bitmap = IconBitmapCreator.getEditBitmap(PAD, itemColor);
        items.add(item);//第2个


        item = new MicroButtonData(-1, -1, ITEM_EDTI);
        item.bitmap = IconBitmapCreator.getEditBitmap(PAD, itemColor);

        items.add(item);//第3个
        items.add(null);//第4个
        items.add(null);//第5个
        // 文字效果部分重置，比如位置，旋转
        item = new MicroButtonData(-1, -1, ITEM_PARTIAL_RESET);
        item.bitmap = IconBitmapCreator.CreateToBottomCenterBitmap(PAD, itemColor);
        items.add(item);//第6个
        items.add(null);//第7个
    }

    /**
     * 重要当view的某个影响宽高的参数改变之后，在view还没绘制之前提前获取宽高
     * <p>并且将其值赋给mwidth，mHeight，此方法只会改变mwidth，mHeight
     */
    private void getSizePreliminarily() {
        Log.d(TAG, "getSizePreliminarily");
        int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        this.measure(width, height);
        mWidth = this.getMeasuredWidth();
        mHeight = this.getMeasuredHeight();
    }

    public MRect getBoundWithoutPad() {
        return new MRect(getLeft() + PAD, getTop() + PAD, getRight() - PAD, getBottom() - PAD);
    }


    /**
     * 缩放视图，重置视图的宽和高，然后重绘
     */
    public void scale(float ratio) {
        LogUtil.d(TAG, "scale =" + ratio);
        float centerX = fLeft + mWidth / 2, centerY = fTop + mHeight / 2;
        adjustSize(ratio);
        fLeft = centerX - mWidth / 2;
        fTop = centerY - mHeight / 2;
        //发生缩放之后可能超出边界，然后适配边界
        adjustEdgeBound();
    }

    public float adjustSize(float ratio) {
        LogUtil.d(TAG, "adjust Size=" + ratio);
        currentRatio = ratio;
        //尝试直接设置
        float tsize = mTextSize;
        tsize *= ratio;
        setTextSize(tsize);
        getSizePreliminarily();
        //根据范围调整比例
        if (mTextSize <= 2) {
            mTextSize = 3;
            if (currentRatio < 1)
                currentRatio = 1f;
        }
        //让长宽大约多出两个汉字
        float totalWidth = (picBound.right - picBound.left) * 2;
        if (mWidth > totalWidth) {
            float tr = totalWidth / mWidth;
            if (tr < 0.999) tr = 0.999f;//会产生小抖动，来回缩放，提醒用户不能说放了
            currentRatio = tr;
        }
        float totalHeight = (picBound.bottom - picBound.top) * 2;
        if (mHeight > totalHeight) {
            float tr = currentRatio = totalHeight / mHeight;
            if (tr < 0.996) tr = 0.996f;
            currentRatio = tr;
        }
        //比例设置为调整后的大小
        mTextSize *= currentRatio;
        if (currentRatio == ratio)//没有变化，直接返回
            return -1;

        setTextSize(mTextSize);
        getSizePreliminarily();

        return currentRatio;
    }

    @Override
    public boolean adjustEdgeBound(float nx, float ny) {
        return false;
    }

    /**
     * 拖动floatview，利用相对点，相对点变化，
     * view根据原来他与相对点的坐标差移动，避免了拖动
     * 处理的误差
     *
     * @param nx 新的对应点的x坐标
     * @param ny 新的对应点的y坐标
     */
    public void drag(float nx, float ny) {
        fLeft = nx - relativeX;
        fTop = ny - relativeY;
        adjustEdgeBound();
    }

    /**
     * 适配floatview的位置,不能超出图片的边界,不算padding的内部就不能超出边界
     * 超出之后移动startx，starty,不影响其它数据
     */
    public boolean adjustEdgeBound() {
//        PTuLog.d(TAG, "adjustEdgeBound");
        refreshRimSize();
        if (fLeft + rimRight < picBound.left)//右边小于左边界
            fLeft = picBound.left - rimRight;
        if (fTop + rimBottom < picBound.top)//下边小于上边界
            fTop = picBound.top - rimBottom;
        if (fLeft + rimLeft > picBound.right)//左边大于右边界
            fLeft = picBound.right - rimLeft;
        if (fTop + rimTop > picBound.bottom)//上边大于下边界
            fTop = picBound.bottom - rimTop;
        return true;
    }

    private void refreshRimSize() {
        mPaint.setTextSize(mTextSize);

        Paint.FontMetrics fm = mPaint.getFontMetrics();
        /**
         * 文本默认不局中，计算中间位置
         */
        float md = sp2px(fm.ascent - fm.top), nd = sp2px(fm.bottom - fm.descent), dd = md - nd;
        rimLeft = PAD;
        rimTop = PAD;
        rimRight = mWidth - PAD;
        rimBottom = mHeight - PAD;
        rimRect = new Rect((int) rimLeft, (int) rimTop, (int) rimRight, (int) rimBottom);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     */
    private int sp2px(float spValue) {
        final float fontScale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return Math.round(spValue * fontScale + 0.5f);
    }

    /**
     * <p>原始图片被缩放了，现在把floatview反着缩放回去，使底图回到原始大小时floatview的相对大小不变
     * <p> 获取子功能生成点的bitmap，以及bitmap的大小，位置的相关参数，
     * <p> 传入initRatio：ptuView上的图片一开始缩放的比例，
     * <p>方法内部再用RealRatio=1/initRation,算出实际的缩放比例,
     * <p>然后得出子功能获得参数：
     * <p>相对left：rleft=（FloatView的letf-ptuView的图片的left）*realRatio,rtop一样
     * <p>FloatView的宽mwidth*=realRatio获取的实际的宽，高一样
     * <p>此方法会改变floatTextView的大小，textSize，显示状态，
     * <p/>
     * <p>另外，获取的图片view可能会过大，造成内存溢出，通过innerRect表示真实尺寸
     * <p>outRect表示需要的尺寸，最后绘制时缩放，减小内存溢出的可能
     * <p>
     * 注意，必须在主线程调用
     *
     * @param ptuSeeView 底图图片视图，用于获取地图的各种信息。
     */
    public void generateResultDataInMain(PtuSeeView ptuSeeView) {
        if (mText.trim().equals("")) return;//表示没有添加以
        mTextStepData = new TextStepData(PtuUtil.EDIT_TEXT);
        setCursorVisible(false);
        //文本在view中的位置
        /*代表view有效区域在底图上的位置的rect，相对于原始图片的左上角上下左右边的距离*/
        RectF boundRectInPic = new RectF();
        getBoundInPic(ptuSeeView, boundRectInPic);

        String realRatio = MU.di(Double.toString(1), Float.toString(ptuSeeView.getTotalRatio()));
        Bitmap textViewBm = generateProximateScaleBm(realRatio);
        Matrix matrix = new Matrix();
        matrix.postScale(getRotationX() == 180 ? -1 : 1, getRotationY() == 180 ? -1 : 1);
        mResultBm = Bitmap.createBitmap(textViewBm, Math.round(rimLeft), Math.round(rimTop),
                Math.round(rimRight - rimLeft), Math.round(rimBottom - rimTop), matrix, true);
        textViewBm.recycle();
        mTextStepData.boundRectInPic = boundRectInPic;
        mTextStepData.rotateAngle = 0;
//        PTuLog.d(TAG, "获取添加文字的Bitmap和相关数据成功");
    }


    /**
     * 获取当前的文字在底图原图片中的位置
     *
     * @param boundRectInPic 代表view有效区域在底图上的位置的rect，相对于原始图片的左上角上下左右边的距离
     *                       <p>outRect大小和innerRect大小相同的</p>
     */
    private void getBoundInPic(PtuSeeView ptuSeeView, RectF boundRectInPic) {
        String textLeft = MU.add(fLeft, rimLeft);
        String textTop = MU.add(fTop, rimTop);
        String textRight = MU.add(fLeft, rimRight);
        String textBottom = MU.add(fTop, rimBottom);
        //先计算出文字部分在当前整个PtuView中的位置

        String[] temp = PtuUtil.getLocationAtBaseBm(textLeft, textTop, ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        boundRectInPic.left = Float.valueOf(temp[0]);
        boundRectInPic.top = Float.valueOf(temp[1]);
        temp = PtuUtil.getLocationAtBaseBm(textRight, textBottom, ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        boundRectInPic.right = Float.valueOf(temp[0]);
        boundRectInPic.bottom = Float.valueOf(temp[1]);
    }

    /**
     * 根据需要缩放的比例，缩放floatTextView，使其大小尽量接近需要的比例，
     * <p>因为文字size的关系，不能保证完全的满足要求缩放，故得到一个近似的大小</p>
     *
     * @param realRatio 需要缩放的的比例
     * @return 整个FloatTextView的bitmap
     */
    //获取realratio比例下，内部text的宽高text
    private Bitmap generateProximateScaleBm(String realRatio) {
//        PTuLog.d(TAG, "开始转化FloatTextView成Bitmap");
//        PTuLog.d(TAG, "当前剩余内存" + Runtime.getRuntime().freeMemory());
        float centerX = fLeft + mWidth / 2;
        float centerY = fTop + mHeight / 2;
        // 原来的缩放结果文字大小代码，放大后有bug，不知为何，缩小后失真，故不缩放了
        // String textWidth = MU.su(mWidth, MU.mu(mPadding, 2f));//目前文本的宽度
        // String textHeight = MU.su(mHeight, MU.mu(mPadding, 2f));//目前文本的高度

        // String fTextWidth = MU.mu(textWidth, realRatio);
        // String fTextHeight = MU.mu(textHeight, realRatio);
        // String fWidth = MU.add(fTextWidth, MU.mu(mPadding, 2f));
        // String fHeight = MU.add(fTextHeight, MU.mu(mPadding, 2f));
        // String limit = MU.di(Runtime.getRuntime().maxMemory() * 1d, 5f);
        // String size = MU.mu(MU.mu(fWidth, fHeight), Float.toString(4));
        // if (MU.co(size, limit) > 0) {
        //     float newRatio = (float) Math.sqrt(Double.valueOf(
        //             MU.di(limit, (MU.mu(
        //                     MU.mu(mWidth, mHeight),
        //                     4f))
        //             )));
        // fTextWidth = MU.mu(textWidth, newRatio);
        // fTextHeight = MU.mu(textHeight, newRatio);
        // fWidth = MU.add(fTextWidth, MU.mu(mPadding, 2f));
        // fHeight = MU.add(fTextHeight, MU.mu(mPadding, 2f));
        // }
        // mTextSize = getTextSizeByWidth(mTextSize, Float.valueOf(fWidth));
        fLeft = centerX - mWidth / 2;
        fTop = centerY - mHeight / 2;
        changeLocation();
        Bitmap bitmap = Bitmap.createBitmap(Math.round(mWidth), Math.round(mHeight), Bitmap.Config.ARGB_8888);
        CUR_SHOW_STATUS = STATUS_TOUMING;
        requestLayout();
        setHeight(Math.round(mHeight));
        setWidth(Math.round(mHeight));
        ((PtuFrameLayout) getParent()).measure(((PtuFrameLayout) getParent()).getWidth(),
                ((PtuFrameLayout) getParent()).getHeight());
        ((PtuFrameLayout) getParent()).layout(0, 0, ((PtuFrameLayout) getParent()).getWidth(),
                ((PtuFrameLayout) getParent()).getHeight());
        Canvas canvas = new Canvas(bitmap);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
                Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG));
        draw(canvas);
        LogUtil.d(TAG, "转化FloatTextView成Bitmap成功");
        return bitmap;
    }

    /**
     * 调整文字的大小使文本框的宽度与给定值误差在一定范围之内
     * 文本缩放时缩放程度小于实际宽高的缩放,另外，
     * 文本框正常受到字数的限制，每个字都增加一些，所
     * 所以循环增加的方式，让宽高达到要求
     * <p/>
     * <p>注意操作的同时textsize和mwidth，mheight已经设置好了</p>
     *
     * @param curSize 当前的文字大小
     * @param fWidth  最终要求的宽度
     * @return 达到要求的文字size
     */
    private float getTextSizeByWidth(float curSize, float fWidth) {
        //缩放只针对内部的文本框，所以以为本匡长宽做比例运算
        float textWidth = mWidth - PAD * 2, fTextWidth = fWidth - PAD * 2;
        while (textWidth < fTextWidth) {
            float add = (fTextWidth - textWidth) / (textWidth + 2) * curSize;//避免除以0，同时每次变化值少一点
            if (add < 0.1) add = 0.1f;
            curSize += add;
            setTextSize(curSize);
            getSizePreliminarily();
            textWidth = mWidth - PAD * 2;
        }
        while (textWidth > fTextWidth) {
            float red = (textWidth - fTextWidth) / (textWidth + 2) * curSize;//避免除以0，同时每次变化值少一点
            if (red < 0.1) red = 0.1f;
            curSize -= red;
            if (curSize <= 1) {
                curSize = 1;
                break;
            }
            setTextSize(curSize);
            getSizePreliminarily();
            textWidth = mWidth - PAD * 2;
        }
        return curSize;
    }

    /**
     * 调整文字的大小是文本框的高度与给定值误差在一定范围之内
     * 文本缩放时缩放程度小于实际高高的缩放,另外，
     * 文本框正常受到字数的限制，每个字都增加一些，所以总厂不能线性增加，而是阶梯增加的
     * 所以循环增加的方式，让高高达到要求
     * <p/>
     * <p>注意操作的同时textsize和mwidth，mheight已经设置好了</p>
     *
     * @param curSize 当前的文字大小
     * @param fHeight 最终要求的高度
     * @return 达到要求的文字size
     */
    private float getTextSizeByHeight(float curSize, float fHeight) {
        //缩放只针对内部的文本框，所以以为本匡长高做比例运算
        float textHeight = mHeight - PAD * 2, fTextHeight = fHeight - PAD * 2;
        while (textHeight < fTextHeight) {
            float add = (fTextHeight - textHeight) / (textHeight + 2) * curSize;//避免除以0，同时每次变化值少一点
            if (add < 0.1) add = 0.1f;
            curSize += add;
            setTextSize(curSize);
            getSizePreliminarily();
            textHeight = mHeight - PAD * 2;
        }
        while (textHeight > fTextHeight) {
            float red = (textHeight - fTextHeight) / (textHeight + 2) * curSize;//避免除以0，同时每次变化值少一点
            if (red < 0.1) red = 0.1f;
            curSize -= red;
            if (curSize <= 1) {
                curSize = 1;
                break;
            }
            setTextSize(curSize);
            getSizePreliminarily();
            textHeight = mHeight - PAD * 2;
        }
        return curSize;
    }

    @Override
    public void onDraw(Canvas canvas) {
        refreshRimSize();
        mPaint.setColor(mBackGroundColor);
        drawDialog(canvas);
        canvas.drawRect(rimRect, mPaint);
        if (CUR_SHOW_STATUS >= STATUS_RIM) {//要显示的东西不止边框
            mPaint.setColor(rimColor);
            mPaint.setStrokeWidth(3);
            //上边的线
            canvas.drawLine(rimLeft, rimTop, rimRight, rimTop, mPaint);
            //左边的线
            canvas.drawLine(rimLeft, rimTop, rimLeft, rimBottom, mPaint);
            //下边的线
            canvas.drawLine(rimLeft, rimBottom, rimRight, rimBottom, mPaint);
            //右边的线
            canvas.drawLine(rimRight, rimTop, rimRight, rimBottom, mPaint);
        }
        if (CUR_SHOW_STATUS >= STATUS_ITEM) {
            mPaint.setColor(itemColor);
            // 这个位置应该在布局变化是设置更好
            items.get(JUSTIFY_ITEM_ID).x = mWidth / 2 - PAD / 2f;
            items.get(JUSTIFY_ITEM_ID).y = rimTop - PAD + PAD / 8f;
            drawItem(canvas, items.get(JUSTIFY_ITEM_ID));

            items.get(BG_ITEM_ID).x = rimRight - PAD / 2f;
            items.get(BG_ITEM_ID).y = rimTop - PAD / 2f;
            drawItem(canvas, items.get(BG_ITEM_ID));

            items.get(PARTIAL_RESET_ITEM_ID).x = mWidth / 2 - PAD / 2f;
            items.get(PARTIAL_RESET_ITEM_ID).y = rimBottom;
            drawItem(canvas, items.get(PARTIAL_RESET_ITEM_ID));
        }

        flipCanvas(canvas, flipDirection);
        super.onDraw(canvas);
//        canvas.restore();
    }

    public void drawItem(Canvas canvas, MicroButtonData item) {
        canvas.drawBitmap(item.bitmap, new Rect(0, 0, PAD, PAD),
                new RectF(item.x, item.y,
                        item.x + PAD,
                        item.y + PAD),
                mPaint);
    }

    /**
     * 改变当前的显示状态，显示变换操作逻辑处理中心，有些复杂，要注意
     */
    public void changeShowState(int state) {
        if (state != CUR_SHOW_STATUS) {//非输入状态
            if (CUR_SHOW_STATUS == STATUS_INPUT) {//如果当前是输入状态，要改变到非输入状态，则需要取消输入法
                Util.hideInputMethod(mContext, this);
                setCursorVisible(false);
            }
            if (state == STATUS_INPUT) {
                setCursorVisible(true);
            }
            //不需要重绘的情况
            if (CUR_SHOW_STATUS == STATUS_ITEM && state == STATUS_INPUT
                    || CUR_SHOW_STATUS == STATUS_INPUT && state == STATUS_ITEM)
                CUR_SHOW_STATUS = state;
            else {//需要重绘的情况
                CUR_SHOW_STATUS = state;
                invalidate();
            }
        }
//        PTuLog.d(TAG, "changeShowState=" + CUR_SHOW_STATUS);
    }

    /**
     * 点击发生在view上，根据具体位置和相应条件判断是否重回自己，告诉父图是否需要重新layout；
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isFocusable()) return false;
        float x = event.getX();
        float y = event.getY();
        // 处理旋转
        float[] xy = new float[]{x, y};
        rotateMatrix.reset();
        rotateMatrix.setRotate(-getRotation(), getWidth() / 2f, getHeight() / 2f); // 注意此处中心点坐标，是相对于本View的
        rotateMatrix.mapPoints(xy);
        x = xy[0];
        y = xy[1];
        if (LogUtil.debugText) {
            ld("旋转前的点 " + event.getX() + " , " + event.getY());
            ld("旋转后的点 " + x + " , " + y);
            ld("相对位置 " + (x - getLeft()) + " , " + (y - getTop()));
            ld("View范围 " + getBound());
            ld("------------");
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (hasUp) hasUp = false;
                downX = x;
                downY = y;
                //两次点击时间必须大于双击时间+200，阻止双击事件
                if (System.currentTimeMillis() - downTime <= ViewConfiguration.getDoubleTapTimeout() + 200)
                    break;
                downTime = System.currentTimeMillis();
                //大于等于显示item时才分发事件，引起输入法
                if (CUR_SHOW_STATUS >= STATUS_ITEM)
                    super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                //发生了点击事件,点击事件都应该给floatView消费掉，非点击事件交由ptuFrameLayout处理
                if (!hasUp && GeoUtil.getDis(downX, downY, x, y) < minMoveDis
                        && System.currentTimeMillis() - downTime < 500) {
                    LogUtil.d(TAG, "drawItem");

                    //内部子item的处理
//对其按钮
                    RectF itemBound = new RectF(items.get(JUSTIFY_ITEM_ID).x - 10, items.get(JUSTIFY_ITEM_ID).y - 10,
                            items.get(JUSTIFY_ITEM_ID).x + PAD + 10, items.get(JUSTIFY_ITEM_ID).y + PAD);
                    if (LogUtil.debugText) {
                        ld("Item范围" + itemBound);
                        ld("是否包含 = " + itemBound.contains(x, y));
                    }
                    if (CUR_SHOW_STATUS >= STATUS_ITEM && itemBound.contains(x, y)) {
                        onClickJustify();
                        return true;
                    }
//背景按钮
                    itemBound.set(items.get(BG_ITEM_ID).x, items.get(BG_ITEM_ID).y - 10,
                            items.get(BG_ITEM_ID).x + PAD + 10, items.get(BG_ITEM_ID).y + PAD);
                    if (CUR_SHOW_STATUS >= STATUS_ITEM && itemBound.contains(x, y)) {
                        if (mBackGroundColor == 0x00000000)
                            mBackGroundColor = 0xffffffff;
                        else
                            mBackGroundColor = 0x00000000;
                        invalidate();
                        return true;
                    }
                    //点击到了重置位置按钮
                    itemBound.set(items.get(PARTIAL_RESET_ITEM_ID).x - 10, items.get(PARTIAL_RESET_ITEM_ID).y,
                            items.get(PARTIAL_RESET_ITEM_ID).x + PAD + 10, items.get(PARTIAL_RESET_ITEM_ID).y + PAD + 10);
                    if (CUR_SHOW_STATUS >= STATUS_ITEM && itemBound.contains(x, y)) {
                        if (getRotation() != 0) {
                            setRotation(0);
                        } else {
                            partialReset();
                        }
                        return true;
                    }

                    //点击到view的其他部分
                    if (CUR_SHOW_STATUS < STATUS_ITEM) {//没显示item时就不发送事件，不弹出输入法
                        changeShowState(STATUS_ITEM);
                        return true;
                    } else {//显示item时，就要弹出输入法
//                        PTuLog.d(TAG, "显示输入法了");
                        super.dispatchTouchEvent(event);
                        //如果没有弹出输入法，强制弹出
                        showKeyboard(this);
                        changeShowState(STATUS_INPUT);
                        return true;
                    }
                }
                hasUp = true;
        }
        return false;
    }

    //显示虚拟键盘
    public static void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);

    }

    /**
     * 对齐方式
     */
    private void onClickJustify() {
//        PTuLog.d(TAG, "onClickBottomCenter");
        items.get(JUSTIFY_ITEM_ID).mode = (items.get(JUSTIFY_ITEM_ID).mode + 1) % 3;
        items.get(JUSTIFY_ITEM_ID).bitmap = justifyBm[items.get(JUSTIFY_ITEM_ID).mode];
        if (items.get(JUSTIFY_ITEM_ID).mode == 0) {
            setGravity(Gravity.CENTER);
        } else if (items.get(JUSTIFY_ITEM_ID).mode == 1) {
            setGravity(Gravity.LEFT);
        } else if (items.get(JUSTIFY_ITEM_ID).mode == 2) {
            setGravity(Gravity.RIGHT);
        }
    }

    /**
     * 重置部分文字效果
     */
    public void partialReset() {
//        PTuLog.d(TAG, "partialReset");
        fLeft = (picBound.left + picBound.right) / 2f - mWidth / 2;
        fTop = picBound.bottom - mHeight - mExtraBottomMargin;
        setRotation(0); // 旋转归零
        flipDirection = 0; // 翻转归零
        Editable text = getText();
        if (text != null) {
            text.clearSpans();
            setText(text);
        }
        changeShowState(STATUS_ITEM);
        // 改变位置
        changeLocation();
    }

    /**
     * 当view的某个影响宽高的参数改变之后，需要改变边框，提前获取它的宽高，并更新再父视图上
     */
    public void updateSize() {
        float centerX = fLeft + mWidth / 2, centerY = fTop + mHeight / 2;
        getSizePreliminarily();
        fLeft = centerX - mWidth / 2;
        fTop = centerY - mHeight / 2;
        changeLocation();
    }

    /**
     * 改变位置，不能在float为空时调用
     */
    public void changeLocation() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
        layoutParams.width = Math.round(mWidth);
        layoutParams.height = Math.round(mHeight);

        layoutParams.setMargins(Math.round(fLeft), Math.round(fTop), 0, 0);
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.updateViewLayout(this, layoutParams);
        }
    }


    public void releaseResource() {
    }

    public Bitmap getResultBm() {
        return mResultBm;
    }

    @Nullable
    public TextStepData getResultStepData() {
        return mTextStepData;
    }

    /**
     * 不能直接设置View的alpha，边上的图标会消失，通过设置文字颜色控制透明度
     *
     * @param opacity 0~255
     */
    public void setTextColorWithOpacity(int opacity, int color) {
        color = (opacity << 24) | (color & 0x00FFFFFF); // 把透明度设置进去
        super.setTextColor(color);
    }

    /**
     * 不能直接设置View的alpha，边上的图标会消失，通过设置文字颜色控制透明度
     *
     * @param transparency 0~255
     */
    public void setTextTransparency(int transparency) {
        this.transparency = transparency;
        // 传到颜色里面的这个值意思是不透明度
        setTextColorWithOpacity(255 - transparency, getTextColors().getDefaultColor());
    }

    /**
     * @return 透明程度 0-255
     */
    public int getTextTransparency() {
        return transparency;
    }


    public void move(float dx, float dy) {
        fLeft = dx;
        fTop = dy;
        adjustEdgeBound();
    }

    //    public int getTextTransparencyInt() {
//        return 255 - (getTextColors().getDefaultColor() >>> 24);
//    }
    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return super.getLayoutParams();
    }


    public RectF getBound() {
        return new RectF(getLeft(), getTop(), getRight(), getBottom());
    }

    /**
     * @param sx 父布局中的位置
     * @param sy 父布局中的位置
     */
    public boolean contains(float sx, float sy) {
        rotateMatrix.reset();
        rotateMatrix.setRotate(-getRotation(), getCenterX(), getCenterY());
        float[] p = {sx, sy};
        rotateMatrix.mapPoints(p);
        boolean contains = getBound().contains((int) p[0], (int) p[1]);
//        if (isTest) {
//            ld("旋转前的点 " + sx + " , " + sy);
//            ld("旋转后的点 " + p[0] + " , " + p[1]);
//            ld("相对位置 " + (p[0] - getLeft()) + " , " + (p[1] - getTop()));
//            ld("View范围 " + getBound());
//            ld("是否包含 = " + contains);
//            ld("------------");
//        }
        return contains;
    }

    @NonNull
    public String getString() {
        Editable editable = getText();
        return editable == null ? "" : editable.toString();
    }

    /**
     *
     */
    public void setBigAndSmallText() {
        String string = getString();
        SpannableString spannableString = new SpannableString(string);
        Random random = new Random();
        int spanBound = Math.max(2, Math.round(string.length() / 3f));
        int spanLen = 0;
        for (int i = 0; i < string.length(); i += spanLen) {
            spanLen = random.nextInt(spanBound);
            int end = Math.min(i + spanLen, string.length());
            float ratio = 0.1f + random.nextFloat() * 3;
            spannableString.setSpan(new RelativeSizeSpan(ratio), i, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        setText(spannableString);
    }

    /**
     * {@link #flipDirection}
     */
    public void flip() {
        flipDirection = (flipDirection + 1) % 4;
        invalidate();
    }


    /**
     * {@link #flipDirection}
     */
    private void flipCanvas(Canvas canvas, int flipDirection) {
        float centerY = getHeight() / 2f;
        float centerX = getWidth() / 2f;
        if (flipDirection == 1) {
            canvas.scale(1, -1, centerX, centerY);
        } else if (flipDirection == 2) {
            canvas.scale(-1, -1, centerX, centerY);
        } else if (flipDirection == 3) {
            canvas.scale(-1, 1, centerX, centerY);
        }
    }

    /**
     * 设置一个对话框作为背景
     *
     * @param dialogBm 为空表示不加背景
     */
    void setDialogBg(@Nullable Bitmap dialogBm) {
        this.mDialogBm = dialogBm;
        if (mDialogBm != null) {
            invalidate();
        }
    }

    private void drawDialog(Canvas canvas) {
        if (mDialogBm == null) return;
        canvas.drawBitmap(mDialogBm, null, rimRect, null);
    }

    private void ld(Object obj) {
        if (LogUtil.debugText && obj != null) {
            Log.d(TAG, obj.toString());
        }
    }

    /**
     * {@link #mExtraBottomMargin}
     */
    public void setExtraBottomMargin(int extraBottomMargin) {
        this.mExtraBottomMargin = extraBottomMargin;
        fTop -= extraBottomMargin;
        changeLocation();
    }
}
