package com.mandi.intelimeditor.ptu.dig;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;

/**
 * Created by liuguicen on 2016/8/2.
 *
 * @description 抠图视图
 */
public class MatView extends PtuSeeView {

    private static final int NO = 0;
    private static final String TAG = "MatView";
    private int CUR_STATUS;
    private static final int PEN = 0x00000008;
    private static final int SMEAR = 2;
    private static final int SHAPE = 3;
    private static final int RUBBER = 4;
    private static final int PEN_MOVE = 0x00000009;
    private static final int PEN_DRAW_LINE = 0x0000000a;

    Context mContext;
    Rect totalBound;
    private Pen pen;
    private final MatPathManager matPathManager;

    /**
     * 必须在
     *
     * @param sourceBitmap 显示的图片
     * @param totalBound   整个PtuFragment的bound
     */
    public MatView(Context context, Bitmap sourceBitmap, Rect totalBound) {
        super(context, sourceBitmap, totalBound);

        mContext = context;
        this.totalBound = totalBound;
        CUR_STATUS = PEN;
        pen = new Pen(context, totalBound);
        matPathManager = new MatPathManager();
    }

    //考虑各种各样的触摸事件发生
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if ((CUR_STATUS & PEN) != 0) {
//                处于pen状态,根据点击发生的位置设置状态，状态设置好之后，移动和离开根据状态进行处理，一般都不变的
//                    点击发生在钢笔上面
                    if (pen.contain(event.getX(), event.getY())) {
                        pen.prepareMove(event.getX(), event.getY());//不管哪种状态，按下之后就可能移动，所以准备好移动的操作
                        if (pen.isDoubleClick()) {//检测是否是双击，如果是，设置isDrawLine变量
                            if (pen.isDrawLine()) {//转换到划线状态
                                CUR_STATUS = PEN_DRAW_LINE;
                                String[] sxy = PtuUtil.getLocationAtBaseBm(Float.toString(pen.pointLeft), Float.toString(pen.pointTop),
                                        srcRect, dstRect);
                                matPathManager.startDrawLine(new float[]{Float.valueOf(sxy[0]), Float.valueOf(sxy[1])});
                            } else {//转换到move状态
                                CUR_STATUS = PEN_MOVE;
                                matPathManager.finishDrawLine();
                            }
                        } else {//不是双击，只是点击，
                            //pen的状态由如果是PEN，PEN变回MOVE或者DRAW_LINE
                            if (pen.isDrawLine()) {
                                CUR_STATUS = PEN_DRAW_LINE;
                                String[] sxy = PtuUtil.getLocationAtBaseBm(Float.toString(pen.pointLeft), Float.toString(pen.pointTop),
                                        srcRect, dstRect);
                                matPathManager.startDrawLine(new float[]{Float.valueOf(sxy[0]), Float.valueOf(sxy[1])});
                            } else {
                                CUR_STATUS = PEN_MOVE;
                            }
                        }
                    } else {//没点到PEN上面，状态变为PEN，此时缩放移动底图
                        CUR_STATUS = PEN;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                CUR_STATUS = PEN;//多个手指，进行缩放
            case MotionEvent.ACTION_MOVE:
                float h = event.getHistorySize();
                for (int i = 0; i < h; i += 3) {
                    float x = event.getHistoricalX(i), y = event.getHistoricalY(i);
                    if ((CUR_STATUS & PEN) != 0) {//在PEN相关状态下发生移动
                        if (pen.contain(x, y)) {
                            if (CUR_STATUS == PEN) {//从外面移入了钢笔,不管,钢笔不动

                            } else {
                                pen.move(x, y, dstRect);
                                if (CUR_STATUS == PEN_DRAW_LINE)//如果是划线状态，添加移动到路径上
                                {
                                    String[] sxy = PtuUtil.getLocationAtBaseBm(Float.toString(pen.pointLeft), Float.toString(pen.pointTop),
                                            srcRect, dstRect);
                                    matPathManager.startDrawLine(new float[]{Float.valueOf(sxy[0]), Float.valueOf(sxy[1])});
                                }
                            }
                        } else if (CUR_STATUS != PEN) {
                            //移出了pen,移动太快没检测到,或者移到了手指不能到的地方，
                            // 不改变状态，用户仍然想移动钢笔
                            pen.outTouch();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                LogUtil.d(TAG, "接受到了cancel事件");
            case MotionEvent.ACTION_UP:
                switch (CUR_STATUS) {
                    case PEN:
                        break;
                    case PEN_MOVE:
                        pen.outTouch();
                        break;
                    case PEN_DRAW_LINE:
                        pen.outTouch();
                        matPathManager.finishDrawLine();
                }
                break;
        }
        if (CUR_STATUS == NO || CUR_STATUS == PEN)
            return super.onTouchEvent(event);
        invalidate();
        return true;
    }


    @Override
    public void onDraw(Canvas canvas) {
        switch (CUR_STATUS) {
            case PEN:
                super.onDraw(canvas);
                pen.drawPen(canvas);
                break;
            case PEN_MOVE:
                super.onDraw(canvas);
                pen.drawPen(canvas);
                break;
            case PEN_DRAW_LINE:
                matPathManager.drawLine(sourceCanvas);
                super.onDraw(canvas);
                pen.drawPen(canvas);
        }
    }


    public void startDrawLine() {
        if (CUR_STATUS != PEN) {
            CUR_STATUS = PEN;
            if (pen != null)
                pen.reSet(totalBound);
            else
                pen = new Pen(mContext, totalBound);
            invalidate();
        }
    }

    public void startSmear() {
        if (CUR_STATUS != SMEAR) {
            CUR_STATUS = SMEAR;
            invalidate();
        }
    }

    public void startMatByShape() {
        if (CUR_STATUS != SHAPE) {
            CUR_STATUS = SHAPE;
            invalidate();
        }
    }

    public void startRubber() {
        if (CUR_STATUS != RUBBER) {
            CUR_STATUS = RUBBER;
            invalidate();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        releaseResource();
        super.onDetachedFromWindow();
    }

    @Override
    public void releaseResource() {
        pen.releaseResource();
    }

}
