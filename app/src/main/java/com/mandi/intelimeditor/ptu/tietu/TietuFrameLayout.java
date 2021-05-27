package com.mandi.intelimeditor.ptu.tietu;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.common.util.geoutil.MRect;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PicGestureListener;
import com.mandi.intelimeditor.ptu.rendpic.CacuCrackFail;
import com.mandi.intelimeditor.ptu.rendpic.RendPicGestureConfig;
import com.mandi.intelimeditor.ptu.rendpic.RendState;
import com.mandi.intelimeditor.ptu.tietu.tietuEraser.ViewEraser;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;


import view.TSRView;

import static java.lang.StrictMath.abs;

/**
 * Created by liuguicen on 2016/10/2.
 * <p>
 * 放置tietu的图片的FrameLayout，用于支持多个贴图在同一界面下的
 * 移动，缩放，旋转操作
 * <p>
 * * 撕图的完整过程：
 * * 只看撕图存在3个状态：准备，增大裂纹状态，移动碎片状态，状态之间夹着中间结点，生成裂纹和撕开图片
 * *（另一状态可以合起来 即未进入=完成,转回去了）
 * *
 * * <p>  经验记住：编程处理较复杂的操作活动的代码时，使用状态法，Activity就是。这里撕图的控制，
 * * 自己一开始觉得很混，搞清状态关系之后就清晰得多了。
 * *
 * * <p>
 * *     手势有：单指移动，多指移动，缩放，旋转，点击
 * * 首先：单指移动，3态可用，旋转，缩放，多指移动，3态禁用
 * *
 * * 未进入状态下，点击撕图按钮，即进入撕图模式，此时处于准备状态
 * * <p>
 * * 准备状态下：若出现两个手指点击，可能是一个手指变两个，也可能是3个手指变两个，此时即进入裂纹状态，
 * <p>
 * * 裂纹状态下：显示裂纹,撕图距离用来控制状态
 * * 如果用户抬起手指，只剩一个或0个手指, 那么裂纹状态消失（可以加个缓慢消失？）
 * * 如果向内移动手指，裂纹同样变小, 小到比原来小时，回到准备状态，没有动作，不然有些问题
 * * 如果用户拉动图片，那么裂纹增大，大到某个值之后，进入分开状态，
 * <p>
 * * 分开状态下：用两点的相对位移控制状态
 * * 图片被分开，然后分开移动，每个碎片跟随对应的手指移动，跟随手指移动的逻辑就相当于单个贴图移动的逻辑
 * * 然后合成一个rect，再画图
 * * 当两个碎片接近合并时，用矩形位置判断，回到裂纹状态，让图片重新合并
 * <p>
 * 动作控制上，准备和裂纹状态主要用两个手指距离变化形成的rendDis 控制， 进入移动碎片状态之后，自由移动，
 * 只判断碎片是否合并，然后返回增大裂纹状态
 */
public class TietuFrameLayout extends FrameLayout implements TSRView {

    public static final String TAG = "TietuFrameLayout";

    private FloatImageView curChosenView;
    private TietuChangeListener tietuChangeListener;
    private Util.DoubleClick mUnlockedDoubleClick = new Util.DoubleClick(600); // 锁住的图双击判断的


    /************************************** 撕图相关  *********************************************/

    private PicGestureListener mPicGestureListener;
    private RendPicGestureConfig mRendGestureConfig;


    /******* 手势控制相关变量 ********/
    private float lastFingerDis = 0;
    private float lastMoveDis;
    /**
     * 撕图拉动的距离，从两指开始接触屏幕，进入撕图的准备状态 {@link RendState#READY}
     * 就开始计算，和手指移动的绝对距离一致
     */
    private float lastRendDis = 0;
    /*** 上次触摸点 */
    public MPoint[] lastTouchP = new MPoint[2];

    /**
     * 擦除贴图的
     */
    private boolean isInErase = false;
    private PopupWindow surePop;

    @Override
    public void onFirstFingerDown(float x, float y) {
        if (curChosenView == null) return;
        float x_toChild = x - curChosenView.getLeft();
        float y_toChild = y - curChosenView.getTop();
        if (isInErase) {
            curChosenView.onEraseDown(x_toChild, y_toChild);
        } else {
            curChosenView.onFirstFingerDown(x_toChild, y_toChild);
        }
    }

    public boolean smallRepeal() {
        if (isInErase && curChosenView != null) {
            curChosenView.smallRepeal();
            return true;
        }

        if (curChosenView != null && curChosenView.getVisibility() == VISIBLE
                && curChosenView.smallRepeal()) {
            return true;
        }
        return false;
    }

    public boolean smallRedo() {
        if (isInErase && curChosenView != null) {
            curChosenView.smallRedo();
            return true;
        }
        return false;
    }

    /**
     * @param reverseDic 翻转方向 0 水平 1 竖直
     */
    public void flipTietu(int reverseDic) {
        if (curChosenView != null) {
            curChosenView.flip(reverseDic);
        } else {
            ToastUtils.show("请先选择一张贴图");
        }
    }

    @Override
    public void onMultiFingerDown() {

    }

    public interface TietuChangeListener {
        void onTietuRemove(FloatImageView view);

        void onTietuAdd(FloatImageView view);

        void lockFloatView(FloatImageView curChosenView);

        void onClickTools(FloatImageView curChosenView);
    }

    public void setOnTietuAddRemoveListener(TietuChangeListener tietuChangeListener) {
        this.tietuChangeListener = tietuChangeListener;
    }

    /************************** 配置相关 ***************************/
    public void setRendGestureConfig(RendPicGestureConfig config) {
        this.mRendGestureConfig = config;
    }

    public TietuFrameLayout(Context context) {
        super(context);
        init();
    }


    public TietuFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPicGestureListener = new PicGestureListener();
        mRendGestureConfig = new RendPicGestureConfig();
    }


    /**
     * @param child 注意只能添加贴图的View，不然不好处理，要加其它的加到更底层的View上去
     */
    public void addView(FloatImageView child, LayoutParams params) {
        addView(child, -1, params);
    }

    /**
     * @param child 注意只能添加贴图的View，不然不好处理，要加其它的加到更底层的View上去
     */
    public void addView(FloatImageView child, int index) {
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(child instanceof FloatImageView)) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() +
                    " only add " + FloatImageView.class.getSimpleName());
        }
        if (curChosenView != null && !curChosenView.isLocked())
            curChosenView.toPreview();
        if (tietuChangeListener != null) {
            tietuChangeListener.onTietuAdd((FloatImageView) child);
        }
        curChosenView = (FloatImageView) child;
        super.addView(child, index, params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mPicGestureListener.onTouchEvent(this, event);
    }

    /**
     * 移动的矩形，边界不能超过底图所在的边界，左边界不能
     */
    private void adjustBound(MRect chosenBound) {
        View ptuView = ((FrameLayout) getParent()).getChildAt(0);
        if (ptuView instanceof PtuSeeView) {
            MRect limitBound = new MRect(((PtuSeeView) ptuView).getPicBound());
            chosenBound.moveANotOutOfB(limitBound);
        } else {
            // 不超出当前布局的范围
            chosenBound.moveANotOutOfB(new MRect(this));
        }
    }

    public void removeFloatView(FloatImageView chosenView) {
        if (tietuChangeListener != null)
            tietuChangeListener.onTietuRemove(chosenView);
        removeView(chosenView);
    }

    /**
     * View被选中的时候，会先判断是否与选中相同，相同则不变化
     * 否则显示边框
     */
    void onChosenView(FloatImageView childView) {
        if (childView == curChosenView)
            curChosenView.toChosen();
        else {
            if (curChosenView != null)
                curChosenView.toPreview();//处理原来选中的View
            curChosenView = childView;
            // 首先将View更新到最前面
            LayoutParams layoutParams = (LayoutParams) childView.getLayoutParams();
            super.removeView(childView);
            childView.toChosen();
            addView(childView, layoutParams);
        }
    }

    @Override
    public void onViewRemoved(View child) {
        if (child == curChosenView) curChosenView = null;
        super.onViewRemoved(child);
    }

    /**
     * 缩放还给其它地方使用
     * 缩放只是针对View在缩放，bitmap不用管，View会自动缩放的
     *
     * @param centerX      这里面中心没有用，按照View中心旋转
     * @param currentRatio 需要缩放的比例
     */
    @Override
    public void scale(float centerX, float centerY, float currentRatio) {
        if (curChosenView == null) return;
        if (!curChosenView.isInRend() && !curChosenView.isChosen()) //非撕图状态，边框没显示出来，不缩放
            return;
        if (curChosenView.isInRend()) // 撕图状态，禁用
            return;
        if (isInErase)
            return;

        if (LogUtil.debugTietuGesture) Log.d(TAG, "缩放贴图，比例" + currentRatio);
        currentRatio = adjustSize(currentRatio);
        curChosenView.scale(currentRatio);
    }

    /**
     * @param dx             移动距离
     * @param isMultiFingers 是否多指移动
     */
    @Override
    public void move(float dx, float dy, boolean isMultiFingers) {
        if (curChosenView == null)
            return;
        if (isInErase) { // 橡皮状态
            if (isMultiFingers) return;
            curChosenView.eraseMove(dx, dy);
            return;
        }
        if (!curChosenView.isInRend() && !curChosenView.isChosen()) return; // 边框没显示出来，不移动
        if (curChosenView.isInRend() && isMultiFingers) return; // 撕图状态下，多指不能移动
        if (curChosenView.isInStretchGesture()) {
            curChosenView.stretchView(dx, dy);
            return;
        }


        // 计算移动后的位置
        MRect bmBoundInLayout = curChosenView.getBmBoundInLayout();
        MRect temBound = new MRect(bmBoundInLayout);
        bmBoundInLayout.offset(dx, dy);
        adjustBound(bmBoundInLayout);
        float rdx = bmBoundInLayout.left - temBound.left,
                rdy = bmBoundInLayout.top - temBound.top;
        // 撕图时碎片在layout中的布局一样要移动
        if (curChosenView.isIn_moveFrag()) {
            for (int i = 0; i < 2; i++) {
                MRect fragBoundInLayout = curChosenView.getFragBoundInLayout(i);
                fragBoundInLayout.offset(rdx, rdy);
            }
        }

        // 更新布局
        LayoutParams parmas = (LayoutParams) curChosenView.getLayoutParams();
        parmas.leftMargin = bmBoundInLayout.leftInt() - curChosenView.PAD;
        parmas.topMargin = bmBoundInLayout.topInt() - curChosenView.PAD;
        // 注意，LayoutParams类文档注释上说margin不能为负，但是FrameLayout的似乎是可以的，为负View就到Layout的外面去
        updateViewLayout(curChosenView, parmas);
    }

    /**
     * @param touchCenterX 这里面没用旋转中心，按照View旋转
     */
    @Override
    public void rotate(float touchCenterX, float toucheCenterY, float angle) {
        if (curChosenView == null) return;
        if (curChosenView.isInRend()) return;
        if (isInErase) return;
        if (curChosenView.isChosen())
            curChosenView.setRotation(curChosenView.getRotation() + angle);
    }

    /************************************  撕图相关  ***********************************************/
    public void initRendGestureStatus() {
        lastRendDis = lastMoveDis = lastFingerDis = 0;
        lastTouchP[0].set(0, 0);
        lastTouchP[1].reset();
    }

    public boolean startRendPic() {
        if (curChosenView != null) {
            boolean r = curChosenView.startRend();
            if (r) {
                rotate(0, 0, -curChosenView.getRotation());
            }
            return r;
        }
        return false;
    }

    public void finishRend() {
        if (curChosenView != null) {
            curChosenView.finishAllRend();
        }
    }

    public void cancelRend() {
        if (curChosenView != null) {
            curChosenView.cancelAllRend();
        }
    }

    public boolean isInRend() {
        if (curChosenView != null)
            return curChosenView.isInRend();
        return false;
    }

    @Override
    public void twoFingerDisChange(@Nullable PointF ap1, @Nullable PointF ap2, boolean isFingerChange) {
        MPoint[] fingers = new MPoint[]{ap1 == null ? null : new MPoint(ap1), ap2 == null ? null : new MPoint(ap2)};
        //        Logcat.e("调用");
        if (curChosenView == null) {
            //            Logcat.e("curChosenView == null 返回");
            return;
        }
        if (!curChosenView.isInRend()) {
            //            Logcat.e("!curChosenView.isInRend() 返回");
            return;
        }

        // 0个或1个手指，初始化设置0
        if (fingers[0] == null || fingers[1] == null) {
            lastFingerDis = 0;
            if (curChosenView.isIn_increaseCrack()) {
                curChosenView.returnReady(); // 增大裂纹状态回到准备状态
            } else if (curChosenView.isIn_moveFrag()) {
                // 重新将触摸点与碎片绑定
                MRect fragBoundInLayout1 = curChosenView.getFragBoundInLayout(0);
                MRect fragBoundInLayout2 = curChosenView.getFragBoundInLayout(1);
                if (fingers[0] != null) {
                    if (fragBoundInLayout2.contains(fingers[0])) { // 先和2比较，2画在上面，触摸到了矩形内
                        curChosenView.swapFragData();
                    } else if (fragBoundInLayout2.dis2center(fingers[0]) < fragBoundInLayout1.dis2center(fingers[0])) {
                        curChosenView.swapFragData();
                    } else {
                    }
                } else if (fingers[1] != null) {
                    if (fragBoundInLayout2.contains(fingers[1])) {

                    } else if (fragBoundInLayout1.dis2center(fingers[1])
                            < fragBoundInLayout2.dis2center(fingers[1])) {
                        curChosenView.swapFragData();
                    } else {

                    }
                }
            }
            //            Logcat.e("p1 == null || p2 == null 返回");
            return;
        }

        /********************************* 两个手指 *********************************/
        //        Logcat.d("执行");

        float fingerDis = GeoUtil.getDis(fingers[0].x, fingers[0].y, fingers[1].x, fingers[1].y);
        if (isFingerChange) { // 屏幕开始变为两指，1变2或3变2
            if (curChosenView.isReady()) {
                try {
                    curChosenView.enter_increaseCrack(fingers[0], fingers[1]);
                } catch (CacuCrackFail cacuCrackFail) {
                    LogUtil.e(cacuCrackFail.getMessage());

                    // 计算锯齿裂痕失败，相当于抬起了手指, 如果已经进入裂纹状态，则返回
                    if (curChosenView.isIn_increaseCrack()) {
                        curChosenView.returnReady();
                    }

                    if (CacuCrackFail.failNumber >= 5) {
                        ToastUtils.show(R.string.fail_to_cacu_crack_notice);
                        CacuCrackFail.failNumber = 0;
                    }
                }
                lastRendDis = 0;
            }
            // 三种状态下，都不计算相对位移
            lastTouchP = Arrays.copyOf(fingers, 2);
            lastFingerDis = fingerDis;
            lastMoveDis = 0;
            return;
        }

        // 将要进入增加crack或者移动碎片操作，如果不是在这个状态，退出
        if (!curChosenView.isIn_increaseCrack() && !curChosenView.isIn_moveFrag()) {
            //            Logcat.d("!curChosenView.isIn_increaseCrack() && !curChosenView.isIn_moveFrag() 返回");
            return;
        }

        // 得到每个碎片的范围，根据手指移动距离，计算碎片移动距离，并且处理边界，最后将碎片范围传回View
        // 进入移动碎片状态后，准备和增大裂纹状态的数据相当于都没用了
        if (curChosenView.isIn_moveFrag()) {
            for (int i = 0; i < 2; i++) {
                MPoint move = fingers[i].sub(lastTouchP[i]);
                MRect bound = curChosenView.getFragBoundInLayout(i); // 得到移动前碎片在Layout中的范围
                //                Log.e("", "移动距离= " + move);
                bound.offset(move);
                adjustBound(bound);
                curChosenView.setFragBoundInLayout(bound, i);
                lastTouchP[i].set(fingers[i]);
            }
            curChosenView.cacuLayout();
            return;
        }

        // 保持两个手指，但是距离变了
        // 如果上次移动距离过短，没有生效，这次要加上
        lastMoveDis += fingerDis - lastFingerDis;
        if (abs(lastMoveDis) < mRendGestureConfig.minValidMoveDis) {
            return;
        }

        float crackLimit = mRendGestureConfig.divideLimit;
        float newRendDis = lastRendDis + lastMoveDis;
        // 手指在往外拉
        if (0 < lastMoveDis) {
            if (0 < newRendDis && newRendDis < crackLimit) { // 没有超过开始撕开的限值
                lastRendDis = newRendDis;
                curChosenView.changeCrackWidth(newRendDis); // 只显示裂痕
            } else if (lastRendDis <= crackLimit && newRendDis >= crackLimit) { // 刚好超过，分开图片
                curChosenView.enter_moveFrag();  // 分离图片, 进入移动碎片状态
                lastRendDis = newRendDis;
                lastTouchP = Arrays.copyOf(fingers, 2);
            }
            lastMoveDis = 0;
        } else if (lastMoveDis < 0) { // 往里合,反过来
            if (newRendDis > 0) { // 裂纹状态
                lastRendDis = newRendDis;
                curChosenView.changeCrackWidth(lastRendDis);
            } else {
                curChosenView.returnReady();
                lastRendDis = newRendDis;
                //                Logcat.d("回到准备状态");
            }
            lastMoveDis = 0;
        }
        lastFingerDis = fingerDis;
    }

    /*************************************撕图相关**************************************************/

    /*************************************橡皮擦除图片相关*****************************************/

    /**
     *
     */
    public boolean startEraseTietu(PTuActivityInterface pTuActivityInterface, boolean isMakeBaozouFace) {
        if (curChosenView == null) { // 没有选中的，选择最顶上那个贴
            int top = getChildCount() - 1;
            if (top < 0) {
                return false;
            }
            curChosenView = (FloatImageView) getChildAt(top);
        }
        curChosenView.toPreview(); // 特殊情况，选择，但是不显示边框
        isInErase = curChosenView.startErase(pTuActivityInterface, isMakeBaozouFace);
        return isInErase;
    }

    public void cancelEraseTietu() {
        isInErase = false;
        if (curChosenView != null) {
            curChosenView.cancelErase();
        }
    }

    public void finishEraseTietu() {
        isInErase = false;
        if (curChosenView != null) {
            curChosenView.finishErase();
        }
    }

    public boolean isInErase() {
        return isInErase;
    }

    @Nullable
    public ViewEraser getCurTietuEraser() {
        if (curChosenView != null) {
            return curChosenView.getEraser();
        }
        return null;
    }

    /*************************************橡皮擦除图片相关*****************************************/

    @Override
    public void adjustEdge(float dx, float dy) {

    }

    @Override
    public float adjustSize(float currentRatio) {
        int maxWidth = getWidth();
        int maxHeight = getHeight();

        if (maxWidth <= 0 || maxHeight <= 0) { // 这种情况应该不会出现，写一下
            maxWidth = AllData.screenWidth;
            maxHeight = AllData.getScreenHeight();
            if (AllData.screenWidth <= 720) { // 没能获取到屏幕宽高时
                maxWidth = 720;
                maxHeight = 1080;
            }
        }

        currentRatio = Math.max(currentRatio, FloatImageView.minWidth * 1f / curChosenView.getWidth());//大于最小宽
        currentRatio = Math.min(currentRatio, maxWidth * 1.2f / curChosenView.getWidth());//小于最大宽
        currentRatio = Math.max(currentRatio, FloatImageView.minHeight * 1f / curChosenView.getHeight());//大于最小高
        currentRatio = Math.min(currentRatio, maxHeight * 1.2f / curChosenView.getHeight());
        return currentRatio;
    }

    @Override
    public boolean onLastFingerUp(float x, float y) {
        if (curChosenView == null) {
            return false;
        }
        float x_toChild = x - curChosenView.getLeft();
        float y_toChild = y - curChosenView.getTop();
        if (isInErase) {
            curChosenView.eraseUp(x_toChild, y_toChild);
        }
        curChosenView.onLastFingerUp(x_toChild, y_toChild);

        dealSureInvisible();

        return false;
    }

    private void dealSureInvisible() { // todo 暂时写这里，写到fiv里面更好
        MRect sureBound = curChosenView.getSureBound();
        sureBound.add_(curChosenView.getLeft(), curChosenView.getTop());
        int limitPad = (int) (-FloatImageView.PAD * 0.5);

        if (surePop != null) {
            surePop.dismiss();
        }
        if (sureBound.left > limitPad
                && sureBound.top > limitPad
                && sureBound.right < getWidth() - limitPad
                && sureBound.bottom < getHeight() - limitPad) {
            // nothing 范围内，没有操作

        } else {
            ImageView sure = new ImageView(getContext());
            sure.setImageBitmap(FloatImageView.sureBm);
            sure.setPadding(FloatImageView.PAD / 2, FloatImageView.PAD / 2, FloatImageView.PAD / 2, FloatImageView.PAD / 2);
            int[] location = new int[2];
            this.getLocationInWindow(location);
            surePop = TietuUtil.showSurePop(getContext(), sure, curChosenView,
                    location[0] + sureBound.left, location[1] + sureBound.top);
            sure.setRotation(curChosenView.getRotation());
            sure.setOnClickListener(view -> {
                Log.d(TAG, "onClick: 1.1");
                if (tietuChangeListener != null) {
                    tietuChangeListener.lockFloatView(curChosenView);
                }
                surePop.dismiss();
            });
        }
    }

    @Override
    public boolean onClick(float x, float y) {
        Log.d(TAG, "onClick: ");
        if (curChosenView != null && curChosenView.isInRend()) {
            return true;
        }
        if (curChosenView != null && isInErase) {
            return true;
        }

        Log.d(TAG, "onClick: 1");
        if (curChosenView != null) {
            float xInTietu = x - curChosenView.getLeft(), yInTietu = y - curChosenView.getTop();
            if (curChosenView.isOnSure(xInTietu, yInTietu)) { // 如果点击到了确定
                Log.d(TAG, "onClick: 1.1");
                if (tietuChangeListener != null) {
                    tietuChangeListener.lockFloatView(curChosenView);
                }
                return true;
            }

            if (curChosenView.isOnCancel(xInTietu, yInTietu)) { // 如果点击到了取消
                Log.d(TAG, "onClick: 1.2");
                removeFloatView(curChosenView);
                return true;
            }

            if (curChosenView.isOnTools(xInTietu, yInTietu)) { // 点击到了工具，更多
                Log.d(TAG, "onClick: 1.3");
                if (tietuChangeListener != null) {
                    Log.d(TAG, "onClick: 1.3.1");
                    tietuChangeListener.onClickTools(curChosenView);
                }
                return true;
            }
        }
        // 选中点击到的最上层的贴图
        int count = getChildCount();
        Log.d(TAG, "onClick: 2 count = " + count);
        MRect rect = new MRect();
        for (int i = count - 1; i >= 0; i--) {
            FloatImageView child = ((FloatImageView) getChildAt(i));
            if (child.getVisibility() != View.VISIBLE) continue; // 不可见的View 没有动作

            rect.left = child.getLeft();
            rect.right = child.getRight();
            rect.top = child.getTop();
            rect.bottom = child.getBottom();
            Log.d(TAG, "onClick: 2.1 i = " + i);
            if (rect.contains((int) x, (int) y)) {
                Log.d(TAG, "onClick: 3");
                if (child.isLocked()) { // 锁住的View
//                        if (Util.DoubleClick.isDoubleClick(500)) { 不能用这个, 冲突
                    Log.d(TAG, "onClick: 3.1");
                    if (mUnlockedDoubleClick.isDoubleClick_m()) {// 双击回到选中状态
                        Log.d(TAG, "onClick: 4");
                        onChosenView(child);
                    }
                    return true;
                } else if (child.isChosen()) {
                    if (tietuChangeListener != null) {
                        tietuChangeListener.onClickTools(curChosenView);
                    }
                    return true;
                } else {
                    Log.d(TAG, "onClick: 5");
                    onChosenView(child);
                    return true;
                }
            }
        }

        //  点击没有发生在tietu图片上面
        if (curChosenView != null && !curChosenView.isLocked()) {
            curChosenView.toPreview();
            if (surePop != null) { // todo 暂时写这里，写到fiv里面更好
                surePop.dismiss();
            }
        }

        return true;
    }

    @Nullable
    public FloatImageView getCurChosenView() {
        return curChosenView;
    }

    public void unChoseCurTietu() {
        if (curChosenView != null && curChosenView.isChosen()) {
            curChosenView.toPreview();
        }
        curChosenView = null;
    }

    @Override
    public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        super.updateViewLayout(view, params);
        Log.d(TAG, "updateViewLayout: ");
    }
}
