package com.mandi.intelimeditor.ptu.dig;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import com.mandi.intelimeditor.common.util.BitmapUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.gif.GifFrame;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.view.ColorPicker;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by Administrator on 2016/11/19 0019.
 */

public class DigView extends View {
    public static final String TAG = "DigView";
    private final int DEFAULT_DIG_PAINT_WIDTH = Util.dp2Px(10);

    private PtuSeeView ptuSeeView;
    private Context mContext;
    /**
     * 用户一次触摸操作过程中的触摸到的点，用于撤销重做
     */
    private List<MPoint> mPointList;
    private Path totalPath;
    /**
     * 手指是否触摸到了屏幕
     */
    private boolean isTouched = false;
    private boolean hasMove;

    private RepealRedoListener repealRedoListener;

    private final RepealRedoManager<List<MPoint>> repealRedoManager;
    private Paint mMongoliaDigPaint;
    private float mBlurRadius = 30;
    private float mMongoliaDigPaintWidth = DEFAULT_DIG_PAINT_WIDTH;
    private boolean mIsPreView;

    private PathMeasure mPathMeasure;
    private Bitmap mMongoliaBm;
    private Paint mBitmapPaint;
    /**
     * 放大用于观察的图
     */
    private Bitmap mEnlargeBgBitmap;
    private MPoint mLastP;
    /**
     * 放大触摸点时，要从原图中提取的用来放大的小方块的宽度
     */
    private float enlargePickWidth;
    private Paint mTouchCenterPaint;
    /**
     * 触摸中心圆圈大小
     */
    private int mTouchCenterCircleW;
    private PTuActivityInterface pTuActivityInterface;
    private Bitmap originalBm;
    private Bitmap enlargeBm;
    private boolean isFirstUp = true;

    public DigView(Context context, PtuSeeView ptuSeeView) {
        super(context);
        mContext = context;
        setLayerType(LAYER_TYPE_HARDWARE, null); // 这个必须开启，不然Xfermode无效，会变黑色等
        this.ptuSeeView = ptuSeeView;

        mBitmapPaint = BitmapUtil.getBitmapPaint();

        repealRedoManager = new RepealRedoManager<>(100);
        mPointList = new ArrayList<>();
        totalPath = new Path();
        mIsPreView = false;

        mPathMeasure = new PathMeasure();
        initMongolia();
        initEnlarge();
    }

    private void initEnlarge() {
        mTouchCenterPaint = new Paint();
        mTouchCenterPaint.setXfermode(null);
        mTouchCenterPaint.setColor(0xffe0e0e0);
        mTouchCenterPaint.setStyle(Paint.Style.STROKE);
        mTouchCenterPaint.setStrokeWidth(Util.dp2Px(2f));

        mTouchCenterCircleW = Util.dp2Px(16);

        enlargePickWidth = Util.dp2Px(98);
        enlargePickWidth /= ptuSeeView.getTotalRatio();
        enlargeBm = Bitmap.createBitmap(PtuFrameLayout.DEFAULT_ENLARGE_W, PtuFrameLayout.DEFAULT_ENLARGE_W, Bitmap.Config.ARGB_8888);
    }

    /**
     * 蒙层的抠图，
     */
    void initMongolia() {
        // 蒙层的Bm,不能直接在View给的Canvas里面画，目前所知的是，
        // 对于xformode和模糊效果，一个要硬件加速，一个不能要，所以只能用手动创建Bitmap和canvas画
        int width = ptuSeeView.getDstRect().width();
        int height = ptuSeeView.getDstRect().height();
        mMongoliaBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        // 其中画笔用来扣出透明区域的
        mMongoliaDigPaint = new Paint();
        mMongoliaDigPaint.setDither(true);
        mMongoliaDigPaint.setAntiAlias(true);
        mMongoliaDigPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mMongoliaDigPaint.setColor(Color.WHITE);
        mMongoliaDigPaint.setStrokeWidth(mMongoliaDigPaintWidth);
        mMongoliaDigPaint.setStrokeCap(Paint.Cap.ROUND);
        mMongoliaDigPaint.setStrokeJoin(Paint.Join.ROUND);
        mMongoliaDigPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mMongoliaDigPaint.setMaskFilter(new BlurMaskFilter(mBlurRadius, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        MPoint point = new MPoint(x, y);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                hasMove = false;
                isTouched = true;
                mPointList = new ArrayList<>();
                mPointList.add(point);
                addDownPoint(point, mLastP);
                break;
            case MotionEvent.ACTION_MOVE:
                if (GeoUtil.getDis(mLastP.x, mLastP.y, x, y) >= 1) {
                    hasMove = true;
                }
                mPointList.add(point);
                addMovePoint(point, mLastP);
                break;
            case MotionEvent.ACTION_UP:
                isTouched = false;
                if (!hasMove) { // 按下，直接起来，加1，能形成点
                    x += 1;
                    y += 1;
                }
                mPointList.add(point);
                addUpPoint(point, mLastP);
                repealRedoManager.commit(mPointList);
                repealRedoListener.canRepeal(repealRedoManager.canRepeal());
                repealRedoListener.canRedo(repealRedoManager.canRepeal());
                if (isFirstUp && hasMove) {
                    setIsPreView(true);
                    isFirstUp = false;
                }
                hasMove = false;
                break;
        }
        mLastP = new MPoint(x, y);
        if (isTouched) {
            float effectW = (mMongoliaDigPaintWidth + mBlurRadius * 2);
            float enlargeWidth = effectW * 3;
            PtuUtil.enlargeViewPoint(ptuSeeView, x, y, enlargeWidth, enlargeBm, true, mMongoliaDigPaintWidth, mBlurRadius);
            PtuUtil.enlargeViewPoint(this, x, y, enlargeWidth, enlargeBm, false, mMongoliaDigPaintWidth, mBlurRadius);
            pTuActivityInterface.showTouchPEnlargeView(enlargeBm, effectW, x, y);
        } else {
            pTuActivityInterface.showTouchPEnlargeView(null, 0, 0, 0);
        }
        invalidate();
        return true;
    }

    private void addDownPoint(MPoint p, @Nullable MPoint lastP) {
        mPathMeasure.setPath(totalPath, false);
        if (mPathMeasure.isClosed() || mPathMeasure.getLength() == 0) { // 已经形成闭合区域，重启一段区域
            totalPath.moveTo(p.x, p.y);
        } else {
            if (lastP == null) {
                totalPath.moveTo(p.x, p.y);
            } else {
                totalPath.quadTo(lastP.x, lastP.y, p.x, p.y);
            }
        }
    }

    private void addMovePoint(MPoint p, @NotNull MPoint lastP) {
        totalPath.quadTo(lastP.x, lastP.y, p.x, p.y);
    }

    private void addUpPoint(MPoint p, @NotNull MPoint lastP) {
        totalPath.quadTo(lastP.x, lastP.y, p.x, p.y);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawMongoliaLayer(canvas);
    }

    /**
     * 绘制蒙层，蒙层里面透明区域相当于抠图区域
     */
    private void drawMongoliaLayer(Canvas canvas) {
        Canvas mongoliaCanvas = new Canvas(mMongoliaBm);
        mMongoliaBm.eraseColor(0); // 先清除前面绘制的
        // 绘制蒙版底色
        if (mIsPreView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 5.0 以下的机器这里会出问题，不知道原因
                mongoliaCanvas.drawRect(
                        0,
                        0,
                        mongoliaCanvas.getWidth(),
                        mongoliaCanvas.getHeight(),
                        ColorPicker.getTransparentPaint());
            } else { // 使用默认底色达到预览效果
                mongoliaCanvas.drawColor(Util.getColor(mContext, R.color.default_ptu_bg));
            }
        } else {
            mongoliaCanvas.drawColor(0x77000000);
        }

        // clear的方式，将抠图区域透明，变成抠图效果
        mongoliaCanvas.save();
        int mongoliaLeft = ptuSeeView.getDstRect().left;
        int mongoliaTop = ptuSeeView.getDstRect().top;
        mongoliaCanvas.translate(-mongoliaLeft, -mongoliaTop);
        mongoliaCanvas.drawPath(totalPath, mMongoliaDigPaint);

        canvas.drawBitmap(mMongoliaBm, mongoliaLeft, mongoliaTop, mBitmapPaint);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int enlargeWidth = Math.round(w / 4f);
        mEnlargeBgBitmap = Bitmap.createBitmap(enlargeWidth, enlargeWidth, Bitmap.Config.ARGB_8888);
    }

    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.repealRedoListener = repealRedoListener;
    }

    public void smallRedo() {
        if (repealRedoManager.canRedo()) {
            repealRedoManager.redo();
            reconnectTotalPath();
            repealRedoListener.canRepeal(repealRedoManager.canRepeal());
            repealRedoListener.canRedo(repealRedoManager.canRedo());
            invalidate();
        }
    }

    public void smallRepeal() {
        if (repealRedoManager.canRepeal()) {
            repealRedoManager.repealPrepare();
            reconnectTotalPath();
            repealRedoListener.canRepeal(repealRedoManager.canRepeal());
            repealRedoListener.canRedo(repealRedoManager.canRedo());
            invalidate();
        }
    }

    /**
     * 重新连接形成总的路径
     */
    private void reconnectTotalPath() {
        totalPath.reset();
        int index = repealRedoManager.getCurrentIndex();
        MPoint lastP = null;
        for (int i = 0; i <= index; i++) {
            List<MPoint> pList = repealRedoManager.getStepdata(i);
            for (int j = 0; j < pList.size(); j++) {
                MPoint curP = pList.get(j);
                if (j == 0) {
                    addDownPoint(curP, lastP);
                } else if (j < pList.size() - 1) {
                    addMovePoint(curP, lastP);
                } else {
                    addUpPoint(curP, lastP);
                }
                lastP = curP;
            }
        }
    }

    public void setIsPreView(boolean preView) {
        mIsPreView = preView;
        invalidate();
    }

    public boolean isInPreview() {
        return mIsPreView;
    }

    public Bitmap getResultBm() {
        // 首先缩放画笔宽度和模糊半径
        Rect srcRect = ptuSeeView.getSrcRect();
        Rect dstRect = ptuSeeView.getDstRect();
        float scaleRatio = srcRect.width() * 1f / dstRect.width();
        float picPaintWidth = mMongoliaDigPaintWidth * scaleRatio;
        float picBlurRadius = mBlurRadius * scaleRatio;
        // 然后移动、缩放路径到原图上
        Matrix matrix = new Matrix();
        // 首先得到相对于目标矩形右上角的坐标位置
        matrix.postTranslate(-dstRect.left, -dstRect.top);
        matrix.postScale(scaleRatio, scaleRatio);
        matrix.postTranslate(srcRect.left, srcRect.top);

        totalPath.transform(matrix);

        return BitmapUtil.digBitmap(ptuSeeView.getSourceBm(), totalPath, null,
                true, picPaintWidth, picBlurRadius);
    }

    public void digGif(GifManager gifManager) {
        // 首先缩放画笔宽度和模糊半径
        Rect srcRect = ptuSeeView.getSrcRect();
        Rect dstRect = ptuSeeView.getDstRect();
        float scaleRatio = srcRect.width() * 1f / dstRect.width();
        float picPaintWidth = mMongoliaDigPaintWidth * scaleRatio;
        float picBlurRadius = mBlurRadius * scaleRatio;
        // 然后移动、缩放路径到原图上
        Matrix matrix = new Matrix();
        // 首先得到相对于目标矩形右上角的坐标位置
        matrix.postTranslate(-dstRect.left, -dstRect.top);
        matrix.postScale(scaleRatio, scaleRatio);
        matrix.postTranslate(srcRect.left, srcRect.top);

        totalPath.transform(matrix);

        for (GifFrame frame : gifManager.getFrames()) {
            frame.bm = BitmapUtil.digBitmap(frame.bm, totalPath, null,
                    true, picPaintWidth, picBlurRadius);
            // oldFm.recycle(); 应该不能回收这里，因为异步线程，PTuSeeView可能接下来就会显示这个Bm，回收了会引起gg
        }
        //重新绘制
        ptuSeeView.post(() -> {
            ptuSeeView.replaceSourceBm(gifManager.getFirstFrameBm());
            gifManager.startAnimation();
        });
    }

    public void releaseResource() {
        if (mEnlargeBgBitmap != null) {
            mEnlargeBgBitmap.recycle();
            mEnlargeBgBitmap = null;
        }
    }

    public void setBlurRadius(float blurRadius) {
        if (blurRadius <= 0) {
            mMongoliaDigPaint.setMaskFilter(null);
            return;
        }
        this.mBlurRadius = blurRadius;
        mMongoliaDigPaint.setMaskFilter(new BlurMaskFilter(mBlurRadius, BlurMaskFilter.Blur.NORMAL));
        postInvalidate();
    }

    public float getBlurRadius() {
        return mBlurRadius;
    }

    public boolean hasOperation() {
        return !totalPath.isEmpty();
    }

    public void setPTuActivityInterface(PTuActivityInterface pTuActivityInterface) {
        this.pTuActivityInterface = pTuActivityInterface;
    }
}
