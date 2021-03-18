package com.mandi.intelimeditor.ptu.tietu.tietuEraser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.util.BitmapUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PicGestureListener;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.draw.MPaint;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import view.TSRView;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/11/02
 *   更通用的可以用来擦除View的内容，有待优化
 *      用来擦除贴图的内容的，很多时候可能用户贴上的图多了一部分不能很好地贴合到底图上面，或者提供的贴图有瑕丝，
 *      这时候做出来的图效果就差了不少，需要用橡皮擦掉多余的部分
 *      类比PS的橡皮擦
 *   对于有模糊度效果的橡皮来说，多画一次效果会不同，因为模糊是减少一部分点的透明度，多次改变效果就有问题了，
 *   在处理手势滑动绘画的过程中，自己就没有注意到这一点，导致出了问题
 * <pre>
 */
public class ViewEraser extends View implements TSRView {
    public static final String TAG = "TietuEraser";
    public static final int DEFAULT_PAINT_WITDH = Util.dp2Px(20);
    public static final int DEFAULT_BLUR_WITDH = 0;
    private PTuActivityInterface pTuActivityInterface = null;
    private float paintWidth;
    private float blurWidth;

    private RepealRedoManager<Pair<Path, MPaint>> repealRedoManager;
    /**
     * path和paint要一起创建防止null
     */
    private Path path;
    /**
     * path和paint要一起创建防止null
     */
    private MPaint paint;
    private boolean hasMove;
    private boolean isUp;
    private float lastX;
    private float lastY;
    private float originalTouchx;
    private float originalTouchY;

    private ViewEraserInterface eraserInterface;
    private View targetView;
    private RepealRedoListener mRepealRedoListener;

    private float mScaleRatioForBm;
    private Context mContext;

    private Bitmap enlargeBm;
    /**
     * 不知为何，path只有一个点是paint无法绘制，只能用这个标志，手动加上这个点的绘制
     */
    private boolean isDown = false;

    private boolean isInErasing;
    private PicGestureListener mPicGestureListener;

    public ViewEraser(Context context) {
        super(context);
    }

    public ViewEraser(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewEraser(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewEraser(Context context, @NotNull ViewEraserInterface eraserInterface,
                      RepealRedoListener repealRedoListener,
                      PTuActivityInterface pTuActivityInterface) {
        super(context);
        this.eraserInterface = eraserInterface;
        this.targetView = eraserInterface.getView();
        this.mContext = context;
        mRepealRedoListener = repealRedoListener;
        this.pTuActivityInterface = pTuActivityInterface;
        initData();
        reInitData();
    }

    private void initData() {
        enlargeBm = Bitmap.createBitmap(PtuFrameLayout.DEFAULT_ENLARGE_W, PtuFrameLayout.DEFAULT_ENLARGE_W, Bitmap.Config.ARGB_8888);
        repealRedoManager = new RepealRedoManager<>(1000);
        mPicGestureListener = new PicGestureListener();
    }

    public void reInitData() {
        mScaleRatioForBm = 1 / (eraserInterface.getBmScaleRatio() > 0.01 ? eraserInterface.getBmScaleRatio() : 0.01f);
        paintWidth = (targetView.getWidth() - targetView.getPaddingLeft() - targetView.getPaddingRight()) / 7f;
        blurWidth = paintWidth * 1 / 2f;// 要相对于原图的坐标，贴图发生的旋转，缩放两种动作需要反向变回去
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "resetData: 贴图右上角" + new MPoint(targetView.getLeft(), targetView.getTop()));
        }
        repealRedoManager.clear(mContext);
        Bitmap srcBm = eraserInterface.getSrcBitmap();
        // 让被擦除View内的bm支持透明度
        // 注意图片不可修改
        if (srcBm.getConfig() != Bitmap.Config.ARGB_8888 || !srcBm.isMutable()) {
            Bitmap tempBm = Bitmap.createBitmap(srcBm.getWidth(), srcBm.getHeight(), Bitmap.Config.ARGB_8888);
            new Canvas(tempBm).drawBitmap(srcBm, 0, 0, BitmapUtil.getBitmapPaint());
            eraserInterface.setBitmap(tempBm);
        }
        refreshRepealRedoView();
        isInErasing = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mPicGestureListener.onTouchEvent(this, event);
        return true;
    }

    public void eraserDown(float x, float y) {
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "down touch: " + x + " " + y);
        }
        originalTouchx = x;
        originalTouchY = y;
        MPoint oldP = new MPoint(x, y);
        oldP.rotate_radius_(targetView.getWidth() / 2f, targetView.getHeight() / 2f, -targetView.getRotation() / 180f * Math.PI);
        x = oldP.x;
        y = oldP.y;
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "down: " + x + " " + y);
        }
        hasMove = false;
        isUp = false;
        Pair<Path, MPaint> pair = getPathPaintPair();
        path = pair.first;
        paint = pair.second;
        if (blurWidth > 0) {
            paint.blurWidth = blurWidth;
            paint.setMaskFilter(new BlurMaskFilter(blurWidth, BlurMaskFilter.Blur.NORMAL));
        }
        isDown = true;
        lastX = x;
        lastY = y;
        path.moveTo(x, y);
        showEnlargeView(x, y);
        // 对于有模糊度效果的橡皮来说，多画一次效果会不同，因为模糊是减少一部分点的透明度，多次改变效果就有问题了，
        // 在处理手势滑动绘画的过程中，自己就没有注意到这一点，导致出了问题
        targetView.invalidate();
    }

    private Pair<Path, MPaint> getPathPaintPair() {
        path = new Path();
        paint = getNewPaint(paintWidth); // 注意画到原图上一定要乘相应缩放比
        return new Pair<>(path, paint);
    }

    public void eraserMove(float dx, float dy) {
        isDown = false;
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "move dx dy: " + dx + " " + dy);
        }
        if (GeoUtil.getDis(0, 0, dx, dy) >= 1) {
            hasMove = true;
        } else {
            return;
        }
        // 由于接口传进来的是dx，dy，又涉及到了旋转缩放，不能直接得到原来的x、y，只好这里通过dx，dy加回去
        float x = originalTouchx + dx;
        float y = originalTouchY + dy;
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "move touch: " + x + " " + y);
        }
        originalTouchx = x;
        originalTouchY = y;
        MPoint oldP = new MPoint(x, y);
        oldP.rotate_radius_(targetView.getWidth() / 2f, targetView.getHeight() / 2f, -targetView.getRotation() / 180f * Math.PI);

        x = oldP.x;
        y = oldP.y;
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "move: " + x + " " + y);
        }
        //20201025 修复友盟崩溃bug path 空指针异常
        if (path == null) {
            Pair<Path, MPaint> pair = getPathPaintPair();
            path = pair.first;
            paint = pair.second;
            path.moveTo(lastX, lastY);
        }
        path.quadTo(lastX, lastY, x, y);
        lastX = x;
        lastY = y;
        targetView.invalidate();
        showEnlargeView(x, y);
    }

    public void eraserUp(float x, float y) {
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "up touch: " + x + " " + y);
        }
        isDown = false;
        MPoint oldP = new MPoint(x, y);
        oldP.rotate_radius_(targetView.getWidth() / 2f, targetView.getHeight() / 2f, -targetView.getRotation() / 180f * Math.PI);
        x = oldP.x;
        y = oldP.y;
        if (LogUtil.debugTietuEraser) {
            Log.d(TAG, "up: " + x + " " + y);
        }
        isUp = true;
        if (!hasMove) {
            x += 1;
            y += 1;
        }
        hasMove = false;
        //20201025 修复友盟崩溃bug path 空指针异常
        if (path == null) {
            Pair<Path, MPaint> pair = getPathPaintPair();
            path = pair.first;
            paint = pair.second;
            path.moveTo(lastX, lastY);
        }
        path.quadTo(lastX, lastY, x, y);
        repealRedoManager.commit(new Pair<>(path, paint));
        path = null; // 根据这里的绘画流程，先绘制历史路径，再绘制当前还没有up的路径会绘制，up之后提交的历史路径就不用在绘制了，置空
        paint = null;
        refreshRepealRedoView();
        targetView.invalidate();
        if (pTuActivityInterface != null) {
            pTuActivityInterface.showTouchPEnlargeView(null, -1, -1, -1);
        }
    }

    private void showEnlargeView(float x, float y) {
        if (pTuActivityInterface != null) {
            float effectW = (paintWidth + blurWidth * 2);
            float enlargeWidth = effectW * 2;
            //            使用橡皮放大图里面无法看到底图，有点问题，暂不实现
            PtuUtil.enlargeViewPoint(pTuActivityInterface.getPtuSeeView(),
                    originalTouchx + targetView.getLeft(), originalTouchY + targetView.getTop(),
                    enlargeWidth, enlargeBm, true, paintWidth, blurWidth);
            PtuUtil.enlargeViewPoint(targetView, x, y, enlargeWidth, enlargeBm, false, paintWidth, blurWidth);

            pTuActivityInterface.showTouchPEnlargeView(enlargeBm, effectW,
                    originalTouchx + targetView.getLeft(), originalTouchY + targetView.getTop());
        }
    }

    /**
     * 从图片上面擦除
     */
    public void eraseInView(Canvas canvas) {
        int currentIndex = repealRedoManager.getCurrentIndex();
        for (int i = 0; i <= currentIndex; i++) {
            Pair<Path, MPaint> sd = repealRedoManager.getStepdata(i);
            canvas.drawPath(sd.first, sd.second);
            if (LogUtil.debugTietuEraser) {
                Log.d(TAG, "eraseInView: 绘制加入历史记录的路径");
            }
        }
        if (path != null && paint != null) {
            if (LogUtil.debugTietuEraser) {
                Log.d(TAG, "eraseInView: 绘制没有加入历史记录的路径");
            }
            canvas.drawPath(path, paint);
            if (isDown) { // 不知为何，path只有一个点是paint无法绘制，只能手动加上这个点
                canvas.drawPoint(lastX, lastY, paint);
            }
        }
    }

    /**
     * 有一个初步的想法，操作数据化，就是用户的各种P图操作，我们不要把它们与view或者moveAction什么的强联系起来
     * 更合适的是我们把操作看成一系列操作数据，然后加以使用
     */
    public List<Pair<Path, MPaint>> getOperateList() {
        List<Pair<Path, MPaint>> operateList = new ArrayList<>();
        int currentIndex = repealRedoManager.getCurrentIndex();
        if (currentIndex < 0) return operateList;
        Matrix mToBmMatrix = generateToBmMatrix();
        for (int i = 0; i <= currentIndex; i++) {
            Pair<Path, MPaint> sd = repealRedoManager.getStepdata(i);
            Path path = new Path(sd.first);
            path.transform(mToBmMatrix);
            MPaint paint = new MPaint(sd.second);
            paint.setStrokeWidth(paint.getStrokeWidth() * mScaleRatioForBm);
            if (paint.blurWidth > 0 && mScaleRatioForBm > 0) {
                paint.setMaskFilter(new BlurMaskFilter(paint.blurWidth * mScaleRatioForBm, BlurMaskFilter.Blur.NORMAL));
            }
            operateList.add(new Pair<>(path, paint));
        }
        return operateList;
    }

    private Matrix generateToBmMatrix() {
        /**
         * 将相对于TietuView的点变成相对于原始bm的点
         */
        float centerX = targetView.getWidth() / 2f;
        float centerY = targetView.getHeight() / 2f;
        Matrix mToBmMatrix = new Matrix();
        // 变成相对于中心点的坐标,对postRotate方法理解有误，这一步不用，方法本来就用参数指定了旋转中心，不需要绕原点转，绕原点的话那两个参数就没用了
        // mMatrix.postTranslate(-centerX, -centerY);
        // view旋转了，点要绕着中心点转回去,因为后面用到的点已经旋转了，这里不再旋转
        //        mToBmMatrix.postRotate(-tietuView.getRotation(), centerX, centerY);
        // 再变成相对于左上角的坐标
        // mMatrix.postTranslate(centerX, centerY);
        mToBmMatrix.postTranslate(-eraserInterface.getBmLeft(), -eraserInterface.getBmTop());

        // 缩放
        mToBmMatrix.postScale(mScaleRatioForBm, mScaleRatioForBm);
        return mToBmMatrix;
    }

    private void refreshRepealRedoView() {
        if (mRepealRedoListener != null) {
            mRepealRedoListener.canRedo(repealRedoManager.canRedo());
            mRepealRedoListener.canRepeal(repealRedoManager.canRepeal());
        }
    }

    public void smallRedo() {
        if (repealRedoManager.canRedo()) {
            repealRedoManager.redo();
            refreshRepealRedoView();
            targetView.invalidate();
        }
    }

    public void smallRepeal() {
        if (repealRedoManager.canRepeal()) {
            repealRedoManager.repealPrepare();
            refreshRepealRedoView();
            targetView.invalidate();
        }
    }

    public void setPaintWidth(float paintWidth) {
        this.paintWidth = paintWidth;
    }

    public float getPaintWidth() {
        return paintWidth;
    }

    public void setBlurWidth(float blurWidth) {
        this.blurWidth = blurWidth;
    }

    public float getBlurWidth() {
        return blurWidth;
    }


    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.mRepealRedoListener = repealRedoListener;
        refreshRepealRedoView();
    }

    private MPaint getNewPaint(float width) {
        MPaint paint = new MPaint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘(Paint.Cap.ROUND);
        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        return paint;
    }

    public void destroy() {

    }

    public void cancelErase() {
        isInErasing = false;
        repealRedoManager.clear(mContext);
    }

    public boolean onBackPressed() {
        if (Util.DoubleClick.isDoubleClick(3000)) { // 连续点两次，允许返回，不消耗返回事件
            return false;
        }
        if (repealRedoManager.getSize() >= 4) { // 绘制超过一定次数，防止用户不小点点到返回，丢失数据
            ToastUtils.show("请再按一次");
            return true;
        }
        return false;
    }

    public void releaseResource() {
        repealRedoManager.clear(mContext);
    }

    public void setInErasing(boolean inErasing) {
        isInErasing = inErasing;
    }

    public boolean isInErasing() {
        return isInErasing;
    }

    public void enlargePaintWidth(double ratio) {
        paintWidth *= ratio;
        blurWidth *= ratio;
    }

    @Override
    public void onFirstFingerDown(float x, float y) {
        eraserDown(x, y);
    }

    @Override
    public boolean onLastFingerUp(float x, float y) {
        eraserUp(x, y);
        return true;
    }

    @Override
    public void scale(float centerX, float centerY, float ratio) {

    }

    @Override
    public void move(float dx, float dy, boolean isMultiFingers) {
        eraserMove(dx, dy);
    }

    @Override
    public void rotate(float touchCenterX, float toucheCenterY, float angle) {

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
        return false;
    }

    @Override
    public void onMultiFingerDown() {

    }

    public interface ViewEraserInterface {
        View getView();

        /**
         * 图片展示出来的bm的宽（注意不一定等于View宽) 除以 bm原来的宽度
         **/
        float getBmScaleRatio();

        Bitmap getSrcBitmap();

        void setBitmap(Bitmap tempBm);

        float getBmLeft();

        float getBmTop();
    }
}
