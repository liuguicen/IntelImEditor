package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.ptu.PicGestureListener;
import com.mandi.intelimeditor.ptu.text.FloatTextView;
import com.mandi.intelimeditor.ptu.tietu.TietuFrameLayout;
import com.mathandintell.intelimeditor.R;

import view.TSRView;

import org.jetbrains.annotations.NotNull;



/**
 * 重绘子视图是一个重要的功能，应该写得简洁有力
 * Created by Administrator on 2016/5/30.
 */
public class PtuFrameLayout extends FrameLayout implements TSRView {
    public static final int DEFAULT_ENLARGE_W = Util.dp2Px(30);
    public static final String TOUCH_ENLARGE_VIEW_TAG = "TouchPEnlargeView";
    private static final String TAG = "PtuFrameLayout";

    private PicGestureListener mPicGestureListener;

    private FloatTextView floatView;
    Context mContext;

    public PtuFrameLayout(Context context) {
        super(context);
        init();
    }

    public PtuFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PtuFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        //以下是在IED中显示是不能依赖的代码，使用isInEditMode返回
        if (isInEditMode()) {
            return;
        }
        mPicGestureListener = new PicGestureListener();
    }


    public FloatTextView initAddTextFloat(Rect picBound) {
        //设置floatText的基本属性
        floatView = new FloatTextView(mContext, picBound);

        //设置布局
        LayoutParams floatParams =
                new LayoutParams(Math.round(floatView.getmWidth()), Math.round(floatView.getmHeight()));

        floatParams.setMargins(Math.round(floatView.getfLeft()), Math.round(floatView.getfTop()),
                0, 0);
        //先remove掉，然后在add。防止多次添加
        addView(floatView, floatParams);
        return floatView;
    }

    /**
     * 策略是：
     * 如果顶层是FloatTextView,做特殊处理，
     * 否则就不做特殊处理
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getChildCount() > 1 && getChildAt(getChildCount() - 1) instanceof FloatTextView) {//对于文字框特殊处理，其它照旧
            boolean isConsume = false;
            FloatTextView floatText = (FloatTextView) getChildAt(getChildCount() - 1);
            float sx = ev.getX(), sy = ev.getY();
            if (!floatText.isClickable()) { // 使用RubberView时
                View childView = getChildAt(getChildCount() - 2);
                ev.setLocation(sx - childView.getLeft(), sy - childView.getTop());
                isConsume = childView.dispatchTouchEvent(ev);
            } else if (floatText.contains(sx, sy)) { // 是文字框，并且在内部
                ev.setLocation(sx - floatText.getLeft(), sy - floatText.getTop());
                isConsume = floatText.dispatchTouchEvent(ev);
            }
            //没有消费才分发事件，不然就不分发
            if (!isConsume) {
                ev.setLocation(sx, sy);
                onTouchEvent(ev);
            }
            return true;
        } else //只有PtuView时只将事件分发给它，坐标不用变换，他们大小一样，PtuView占满了整个布局
            return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mPicGestureListener.onTouchEvent(this, event);
    }

    public TietuFrameLayout initAddImageFloat(Rect bound) {
        TietuFrameLayout tietuFrameLayout = new TietuFrameLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(bound.width(), bound.height(), Gravity.CENTER);
        tietuFrameLayout.setBackgroundColor(0x0000);
        addView(tietuFrameLayout, layoutParams);
        return tietuFrameLayout;
    }

    /**
     * 暂时不用的方法
     *
     * @return 如果二级页面需要一个二级的确定按钮的话，在这里添加
     */
    public View addSecondarySureBtn() {
        ImageView imageView = new ImageView(mContext);
        int width = Util.dp2Px(36);
        imageView.setImageBitmap(IconBitmapCreator.createSureBitmap(
                width,
                Util.getColor(R.color.text_checked_color)));
        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = Util.dp2Px(8);
        layoutParams.leftMargin = (int) (getWidth() - width - margin * 2.6);
        layoutParams.topMargin = margin;
        imageView.setBackgroundResource(R.drawable.background_circle_half_alpha);
        addView(imageView, layoutParams);
        return imageView;
    }

    /**
     * @param srcBm            P图操作的底图，放大的来源图,为空表示不显示
     * @param effectWidth      画笔影响区域宽度，用于判断放大区域是否放大视图重合
     * @param xInEnlargeParent 触摸点相对于父View的位置
     */
    public void showTouchPEnlargeView(Bitmap srcBm, float effectWidth, float xInEnlargeParent, float yInEnlargeParent) {
        if (srcBm == null) { // 不再显示触摸点放大窗口时
            removeView(findViewWithTag(TOUCH_ENLARGE_VIEW_TAG));
            return;
        }
        // 加入视图并更新布局位置
        ImageView enlargeView = findViewWithTag(TOUCH_ENLARGE_VIEW_TAG);

        float enlargeViewW = getWidth() / 3.5f; // 放大视图的宽度
        // 计算放大视图的位置， 左上角还是右上角
        float ratio = 1.1f;
        int margin = Util.dp2Px(2);
        LayoutParams layoutParams = enlargeView != null ? (LayoutParams) enlargeView.getLayoutParams()
                : new LayoutParams((int) enlargeViewW, (int) enlargeViewW);
        if (enlargeView == null) {
            layoutParams.leftMargin = margin;
        }
        layoutParams.topMargin = margin;
        int marginForRight = (int) (getWidth() - enlargeViewW - margin);
        boolean isCoincide = false;
        if (layoutParams.leftMargin == margin) { // 在左边
            // 画笔影响区域的左上角进入放大视图的内部，重合
            if (xInEnlargeParent - effectWidth / 2 < enlargeViewW * ratio && yInEnlargeParent - effectWidth / 2 < enlargeViewW * ratio) {
                layoutParams.leftMargin = marginForRight;
                isCoincide = true;
            }
        } else { // 在右边
            if (xInEnlargeParent + effectWidth / 2 > marginForRight && yInEnlargeParent - effectWidth / 2 < enlargeViewW * ratio) { // 重合
                layoutParams.leftMargin = margin;
                isCoincide = true;
            }
        }

//        // 如果放大宽度特别大，触摸区域会一直和放大View冲突，直接显示在左边
//        if (enlargeViewW * ratio + effectWidth > getWidth() || enlargeViewW * ratio + effectWidth > getHeight()) {
//            layoutParams.leftMargin = margin;
//            isCoincide = false;
//        }

        if (enlargeView == null) { // 没有加入
            enlargeView = new ImageView(mContext);
            enlargeView.setTag(TOUCH_ENLARGE_VIEW_TAG);
            enlargeView.setScaleType(ImageView.ScaleType.FIT_XY);
            addView(enlargeView, layoutParams);
        } else if (isCoincide) { // 已经加入, 但是需要改变左右位置的
            removeView(enlargeView);
            addView(enlargeView, layoutParams);
        }

        // 最后，显示bm
        enlargeView.setImageBitmap(srcBm);
    }

    @Override
    public void scale(float centerX, float centerY, float ratio) {
        if (floatView == null) return;

        floatView.scale(ratio);
        //设置文字位置
        floatView.setRelativeX(centerX);
        floatView.setRelativeY(centerY);
        floatView.changeLocation();
    }

    @Override
    public void move(float dx, float dy, boolean isMultiFingers) {
        if (floatView == null)
            return;

//        // 计算移动后的位置
        MRect bmBoundInLayout = floatView.getBoundWithoutPad();
        bmBoundInLayout.offset(dx, dy);

        // 更新布局
        // 注意，LayoutParams类文档注释上说margin不能为负，但是FrameLayout的似乎是可以的，为负View就到Layout的外面去
        LayoutParams parmas = (LayoutParams) floatView.getLayoutParams();
        parmas.leftMargin = bmBoundInLayout.leftInt() - FloatTextView.PAD;
        parmas.topMargin = bmBoundInLayout.topInt() - FloatTextView.PAD;
        // 这里报了个错，floatView不是this的子View，move事件延迟发过来，view已经移除了
        // 判断下，不要addView，因为可能是正常已经移除了，加了就不对了
        if (floatView.getParent() == this) {
            updateViewLayout(floatView, parmas);
        }

        //移动
        floatView.move(parmas.leftMargin, parmas.topMargin);
        floatView.changeLocation();
    }

    @Override
    public void rotate(float touchCenterX, float toucheCenterY, float angle) {
        if (floatView != null) {
            LogUtil.d(TAG, "rotation =" + (floatView.getRotation() + angle));
            floatView.setRotation(floatView.getRotation() + angle);
        }
    }

    @Override
    public void twoFingerDisChange(@NotNull PointF P1, @NotNull PointF P2, boolean isFingerChange) {

    }

    @Override
    public void adjustEdge(float dx, float dy) {

    }

    @Override
    public float adjustSize(float ratio) {
        return 0;
    }

    @Override
    public boolean onClick(float x, float y) {
        LogUtil.d("经过了up");
        //点击事件

        //点击发生在floatView之外
        if (!floatView.contains(x, y))
            floatView.changeShowState(floatView.STATUS_TOUMING);
        else {  //不是点击事件，将之前状态显示出来
            floatView.changeShowState(floatView.getDownState());
        }
        return false;
    }

    @Override
    public void onFirstFingerDown(float x0, float y0) {

    }

    @Override
    public boolean onLastFingerUp(float x, float y) {
        return false;
    }

    private void ld(Object obj) {
        if (LogUtil.debugText && obj != null) {
            Log.d(TAG, obj.toString());
        }
    }

    @Override
    public void onMultiFingerDown() {

    }
}