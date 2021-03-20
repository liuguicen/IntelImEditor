package com.mandi.intelimeditor.ptu.draw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.MotionEvent;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;
import com.mandi.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;



/**
 * 涂鸦View
 * Created by yonglong on 2016/7/2.
 */
public class DrawView extends BaseDrawView {
    public static final int DEFAULT_MAX_PAINT_STROKE_SIZE = 128;
    private final Bitmap enlargeBm;

    private Context context;
    private Bitmap mBitmap, mMosaicImage;
    private Canvas mCanvas;
    private Path mPath, mPicPath;
    public Paint mBitmapPaint;// 画布的画笔
    private float mX, mY, mPicX, mPicY;// 临时点坐标
    private static final float TOUCH_TOLERANCE = 4;
    // 保存Path路径的集合,用List集合来模拟栈
    private static List<DrawPath> savePath, picSavePath;
    // 保存已删除Path路径的集合
    private static List<DrawPath> deletePath, picDeletePath;
    // 记录Path路径的对象
    private DrawPath dp, picDp;
    private int screenWidth, screenHeight;
    //原图
    public PtuSeeView ptuSeeView = null;
    private Rect totalBound, picBound;
    private RepealRedoListener repealRedoListener;
    /**
     * 处理点击时画一个圆点
     */
    private boolean hasMove;
    private PTuActivityInterface pTuActivityInterface;
    private boolean isDown = false;

    public List<DrawPath> getResultData() {
        return picSavePath;
    }

    public Bitmap getResultBm() {
        return mBitmap;
    }

    public void setPtuActivityInterface(PTuActivityInterface pTuActivityInterface) {
        this.pTuActivityInterface = pTuActivityInterface;
    }

    public class DrawPath {
        public Path path;// 路径
        public MPaint paint;// 画笔
        public int stroke;// 画笔粗细
    }

    /**
     * @param context
     */
    public DrawView(Context context, Rect totalBound, PtuSeeView ptuSeeView) {
        super(context);
        this.context = context;
        this.ptuSeeView = ptuSeeView;
        this.totalBound = totalBound;
        this.picBound = ptuSeeView.getPicBound();
        screenWidth = picBound.width();
        screenHeight = picBound.height();

        setLayerType(LAYER_TYPE_SOFTWARE, null); // 设置默认样式，去除dis-in的黑色方框以及clear模式的黑线效果
        initCanvas();
        picSavePath = new ArrayList<>();
        savePath = new ArrayList<>();
        picDeletePath = new ArrayList<>();
        deletePath = new ArrayList<>();
        enlargeBm = Bitmap.createBitmap(PtuFrameLayout.DEFAULT_ENLARGE_W, PtuFrameLayout.DEFAULT_ENLARGE_W, Bitmap.Config.ARGB_8888);
    }

    public void initCanvas() {
        setPaintStyle();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(0);
        mBitmap.setDensity(getResources().getDisplayMetrics().densityDpi);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
    }


    /**
     * 清除绘制的数据，但不会重置画笔等
     */
    public void clearDrawData() {
        mBitmap.eraseColor(0);
        picSavePath.clear();
        savePath.clear();
        picDeletePath.clear();
        deletePath.clear();
        invalidate();
    }

    public Bitmap convertViewToBitmap() {
        this.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        this.layout(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
        this.buildDrawingCache();
        Bitmap bitmap = this.getDrawingCache();
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        float[] pxy = PtuUtil.getLocationAtBaseBm(x + getLeft(), y + getTop(),
                ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 每次down下去重新new一个Path
                hasMove = false;
                isDown = true;
                mPath = new Path();
                mPicPath = new Path();
                picDp = new DrawPath();
                picDp.path = mPicPath;
                picDp.paint = mPaint;
                //每一次记录的路径对象是不一样的
                dp = new DrawPath();
                dp.path = mPath;
                dp.paint = mPaint;
                touch_start(x, y);
                pic_touch_start(pxy[0], pxy[1]);
                invalidate();
                showEnlargeView(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (GeoUtil.getDis(mX, mY, x, y) >= 2) {
                    hasMove = true;
                    isDown = false;
                    touch_move(x, y);
                    pic_touch_move(pxy[0], pxy[1]);
                    invalidate();
                    showEnlargeView(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!hasMove) {  //处理点击时画一个圆点
                    mX += 1;
                    mY += 1;
                    mPicX += 1;
                    mPicY += 1;
                }
                isDown = false;
                hasMove = false;
                touch_up();
                pic_touch_up();
                invalidate();
                if (pTuActivityInterface != null) {
                    pTuActivityInterface.showTouchPEnlargeView(null, -1, -1, -1);
                }
                break;
        }
        return true;
    }

    private void touch_start(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void pic_touch_start(float x, float y) {
        mPicPath.moveTo(x, y);
        mPicX = x;
        mPicY = y;
    }

    private void touch_move(float x, float y) {
        // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也可以)
        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        //mPath.lineTo(mX,mY);
        mX = x;
        mY = y;
    }

    private void pic_touch_move(float x, float y) {
        // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也可以)
        mPicPath.quadTo(mPicX, mPicY, (x + mPicX) / 2, (y + mPicY) / 2);
        //mPath.lineTo(mX,mY);
        mPicX = x;
        mPicY = y;
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint);
        //将一条完整的路径保存下来(相当于入栈操作)
        savePath.add(dp);
        mPath = null;// 重新置空
        refreshRepealRedo();
    }

    private void pic_touch_up() {
        mPicPath.lineTo(mPicX, mPicY);
        //将一条完整的路径保存下来(相当于入栈操作)
        picSavePath.add(picDp);
        mPath = null;// 重新置空
    }

    @Override
    public void onDraw(Canvas canvas) {
        //canvas.drawColor(0xFFAAAAAA);
        // 将前面已经画过得显示出来
//        mCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
//        for (DrawPath drawPath : savePath) {
//            canvas.drawPath(drawPath.path, drawPath.paint);
//        }
        if (mPath != null) {
            // 实时的显示
            canvas.save();
            canvas.drawPath(mPath, mPaint);
            canvas.restore();
        }
        if (isDown) { // 不知为何，path只有一个点是paint无法绘制，只能手动加上这个点
            canvas.drawPoint(mX, mY, mPaint);
        }
    }

    public int getOperationNumber() {
        if (savePath == null) return 0;
        return savePath.size();
    }

    /**
     * 撤销
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo() {
        if (savePath != null && savePath.size() > 0) {
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            DrawPath picdrawPath = picSavePath.get(picSavePath.size() - 1);
            deletePath.add(drawPath);
            picDeletePath.add(picdrawPath);
            savePath.remove(savePath.size() - 1);
            picSavePath.remove(picSavePath.size() - 1);
            refreshRepealRedo();
            redrawOnBitmap();
        }
    }

    public void refreshRepealRedo() {
        if (savePath != null && savePath.size() > 0) {
            repealRedoListener.canRepeal(true);
        } else {
            repealRedoListener.canRepeal(false);
        }
        if (deletePath != null && deletePath.size() > 0) {
            repealRedoListener.canRedo(true);
        } else {
            repealRedoListener.canRedo(false);
        }
    }

    /**
     * 重做
     */
    public void redo() {
        if (savePath != null && savePath.size() > 0) {
            repealRedoListener.canRedo(true);
            repealRedoListener.canRepeal(true);

            savePath.clear();
            picSavePath.clear();
            refreshRepealRedo();

            redrawOnBitmap();
        }
    }

    private void redrawOnBitmap() {
        initCanvas();
        for (DrawPath drawPath : savePath) {
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
    }

    /**
     * 恢复，恢复的核心就是将删除的那条路径重新添加到savapath中重新绘画即可
     */
    public void recover() {
        if (deletePath.size() > 0) {
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            DrawPath picdp = picDeletePath.get(picDeletePath.size() - 1);
            savePath.add(dp);
            picSavePath.add(picdp);
            //将取出的路径重绘在画布上
            mCanvas.drawPath(dp.path, dp.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            picDeletePath.remove(picDeletePath.size() - 1);

            refreshRepealRedo();

            invalidate();
        }
    }

    private void showEnlargeView(float x, float y) {
        if (pTuActivityInterface != null) {
            float effect_w;
            if (mPaint.isErase) {
                effect_w = ERASE_WIDTH;
            } else {
                effect_w = currentSize;
            }
            float baseX = ptuSeeView.getDstRect().left + x, baseY = ptuSeeView.getDstRect().top + y;
            float enlargeWidth = effect_w * 3;
            PtuUtil.enlargeViewPoint(ptuSeeView, baseX, baseY, enlargeWidth, enlargeBm, true, effect_w, 0);
            PtuUtil.enlargeViewPoint(this, x, y, enlargeWidth, enlargeBm, false, effect_w, 0);
            pTuActivityInterface.showTouchPEnlargeView(enlargeBm, effect_w, baseX, baseY);
        }
    }

    /**
     * 透明颜色要特殊绘制, 即绘制成P图背景色一样的颜色，表现出透明的效果，
     * 这里面不把bitmap对应像素变成透明，不好变， 然后最后生成的时候
     */
    @Override
    public void dealTransparentColor() {
        if (currentColor == 0) {
            if (ptuSeeView.isShowTransparentBg()) { //
                Bitmap transparentBitmap = BitmapFactory.decodeResource(IntelImEditApplication.appContext.getResources(), R.drawable.imitate_transparent);
                BitmapShader transparentShader = new BitmapShader(transparentBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                mPaint.setDither(true);
                mPaint.setAntiAlias(true);
                mPaint.setShader(transparentShader);
            } else {
                mPaint.setColor(Util.getColor(R.color.default_ptu_bg));
            }
            mPaint.isTransparent = true;
        }
    }

    public int getCurPaintColor() {
        return currentColor;
    }

    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.repealRedoListener = repealRedoListener;
    }
}
