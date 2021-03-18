package com.mandi.intelimeditor.ptu.cut;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mathandintell.intelimeditor.R;

import java.util.Locale;



/**
 * Created by liuguicen on 2016/8/5.
 * <p>
 * 裁剪的框，支持缩放和拖动，在中间格子显示宽和高
 */
public class CutFrame {
    /**
     * 裁剪框上的小块可见绘图范围和触摸范围的默认比例
     */
    private static float DEFAULT_TOUCH_LUMP_RATIO = 3f;
    private static int DEFAULT_COLOR = Color.WHITE;

    private final String TAG = "CutFrame";
    /**
     * 最小剪切框的宽度
     */
    private static final float MIN_AREA = Util.dp2Px(3600);
    private final static int EXTENSE_LENGTH = Util.dp2Px(4);

    private final Rect totalBound;
    /**
     * frame的位置和宽高
     */
    float frameLeft, frameTop, frameWidth, frameHeight;

    /**
     * 裁剪框上各个小块的长宽，是触摸的宽
     */
    private float mCornerWidth, mEdgeLumpWidth;

    /**
     * 绘图的宽占触摸的宽的比例
     */
    private float mLumpTouchRatio = DEFAULT_TOUCH_LUMP_RATIO;

    private boolean onTouch;
    private final Paint mLinePaint;
    private final Paint textPaint;
    private RectF rect;
    private final Paint mLumpPaint;
    private final Paint noChoosedPaint;
    private float lastX = -1, lastY = -1;
    private MOVESTATE CUR_STATA;
    private CutView cutView;
    private Path edgeShape;
    float lastCenterX, lastCenterY;
    private int fixedWidth, fixedHeight;
    private float fixedRatio;
    @ColorInt
    private int mColor = DEFAULT_COLOR;

    private enum MOVESTATE {
        LEFT_TOP,
        RIGHT_TOP,
        LEFT_BOTTOM,
        RIGHT_BOTTOM,
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER,
        NONE
    }

    CutFrame(CutView cutView, Rect totalBound) {
        this.cutView = cutView;
        this.totalBound = totalBound;
        CUR_STATA = MOVESTATE.NONE;
        mColor = Util.getColor(R.color.cut_frame);
        frameLeft = cutView.getDstRect().left;
        frameTop = cutView.getDstRect().top;
        frameWidth = cutView.getDstRect().width();
        frameHeight = cutView.getDstRect().height();

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);

        mLumpPaint = new Paint();
        mLumpPaint.setStrokeWidth(1);
        mLumpPaint.setStyle(Paint.Style.FILL);
        mLumpPaint.setAntiAlias(true);
        mLumpPaint.setDither(true);

        noChoosedPaint = new Paint();
        noChoosedPaint.setColor(Util.getColor(R.color.half_transparent_black));

        textPaint = new Paint();//显示尺寸的文字
        textPaint.setTextSize(Util.dp2Px(15));
        textPaint.setAntiAlias(true);
        setALLPaintColor();

        //尺寸
        Resources resources = IntelImEditApplication.appContext.getResources();
        mCornerWidth = resources.getDimension(R.dimen.cut_frame_lump_width);
        mEdgeLumpWidth = mCornerWidth;


        rect = new RectF();
        edgeShape = new Path();

        fixedWidth = fixedHeight = -1;
    }

    /**
     * 初始化CutFrame,开始或重置时调用
     */
    void reInit() {
        fixedRatio = -1;
        fixedHeight = -1;
        fixedWidth = -1;
        frameLeft = cutView.getDstRect().left;
        frameTop = cutView.getDstRect().top;
        frameWidth = cutView.getDstRect().width();
        frameHeight = cutView.getDstRect().height();
    }

    /**
     * {@link CutFrame#mLinePaint}
     * @param ratio
     */
    public void setLumpTouchRatio(float ratio) {
        if (ratio > 0) {
            mLumpTouchRatio = ratio;
        }
    }

    public float getLumpTouchRatio() {
        return mLumpTouchRatio;
    }

    public void setFramColor(@ColorInt int color) {
        mColor = color;
        setALLPaintColor();
    }

    public void resetFramColor() {
        mColor = DEFAULT_COLOR;
        setALLPaintColor();
    }

    private void setALLPaintColor() {
        mLinePaint.setColor(mColor);
        mLumpPaint.setColor(mColor);
        textPaint.setColor(mColor);
        textPaint.setAlpha(192);
    }


    boolean isFixedSize() {
        return fixedWidth > 0;
    }

    int getFixedWidth() {
        return fixedWidth;
    }


    /**
     * 只会处理移动的情况，其它情况它不处理
     */
    public boolean onTouchEvent(MotionEvent event) {
        float frameX = event.getX() - frameLeft, frameY = event.getY() - frameTop;
        float x = event.getX(), y = event.getY();
        // 触摸范围比绘画范围大
        float touchCornerWidth = this.mCornerWidth * mLumpTouchRatio;
        float touchEdgeLumpWidth = this.mEdgeLumpWidth * mLumpTouchRatio;
//        if (Math.abs(frameLeft - ) < Util.dp2Px(4)) {
////
////        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;
                lastY = y;

                //中间位置
                rect.set(frameWidth / 3, frameHeight / 3, frameWidth * 2 / 3, frameHeight * 2 / 3);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.CENTER;
                    onTouch = true;
                    break;
                }

                //四个角
                // 左上角
                rect.set(-touchCornerWidth, -touchCornerWidth,
                        touchCornerWidth, 0 + touchCornerWidth);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.LEFT_TOP;
                    onTouch = true;
                    break;
                }
                //右上
                rect.set(frameWidth - touchCornerWidth, -touchCornerWidth,
                        frameWidth + touchCornerWidth, 0 + touchCornerWidth);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.RIGHT_TOP;
                    onTouch = true;
                    break;
                }
                //左下
                rect.set(-touchCornerWidth, frameHeight - touchCornerWidth,
                        touchCornerWidth, frameHeight + touchCornerWidth);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.LEFT_BOTTOM;
                    onTouch = true;
                    break;
                }
                //右下
                rect.set(frameWidth - touchCornerWidth, frameHeight - touchCornerWidth,
                        frameWidth + touchCornerWidth, frameHeight + touchCornerWidth);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.RIGHT_BOTTOM;
                    onTouch = true;
                    break;
                }

                //四边
                //四条边上
                rect.set(-touchEdgeLumpWidth, -touchEdgeLumpWidth * 3 / 4, touchEdgeLumpWidth, touchEdgeLumpWidth * 3 / 4);
                //上
                rect.offset(frameWidth / 2, 0);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.TOP;
                    onTouch = true;
                    break;
                }
                //下
                rect.offset(0, frameHeight);
                //不扩大，前面以扩大
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.BOTTOM;
                    onTouch = true;
                    break;
                }

                //右边
                rect.set(-touchEdgeLumpWidth * 3 / 4, -touchEdgeLumpWidth, touchEdgeLumpWidth * 3 / 4, touchEdgeLumpWidth + 0);
                rect.offset(frameWidth, frameHeight / 2);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.RIGHT;
                    onTouch = true;
                    break;
                }
                //左边
                rect.offset(-frameWidth, 0);
                rect.inset(-EXTENSE_LENGTH, -EXTENSE_LENGTH);
                if (rect.contains(frameX, frameY)) {
                    CUR_STATA = MOVESTATE.LEFT;
                    onTouch = true;
                    break;
                }
                onTouch = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!onTouch) return false;
                if (CUR_STATA == MOVESTATE.NONE)
                    onTouch = false;
                float dx = x - lastX, dy = y - lastY;
                if (CUR_STATA == MOVESTATE.CENTER) {//移动整个切图框
                    move(dx, dy);
                } else {
                    if (fixedRatio > 0) {//固定缩放比的情况，取移动的短边进行变化
                        float[] dxy = calculateOffsetInFixedRatio(dx, dy);
                        dx = dxy[0];
                        dy = dxy[1];
                    }
                    switch (CUR_STATA) {
                        //四个角
                        case LEFT_TOP:
                            if ((frameWidth - dx) * (frameHeight - dy) >= MIN_AREA) {
                                addLeft(dx);
                                addTop(dy);
                            } //否则不管，多次移动中的小距离移动填补差值
                            break;
                        case RIGHT_TOP:
                            if ((frameWidth + dx) * (frameHeight - dy) >= MIN_AREA) {
                                addRight(dx);
                                addTop(dy);
                            } //否则不管，多次移动中的小距离移动填补差值

                            break;
                        case LEFT_BOTTOM:
                            if ((frameWidth - dx) * (frameHeight + dy) >= MIN_AREA) {
                                addLeft(dx);
                                addBottom(dy);
                            } //否则不管，多次移动中的小距离移动填补差值

                            break;
                        case RIGHT_BOTTOM:
                            if ((frameWidth + dx) * (frameHeight + dy) >= MIN_AREA) {
                                addRight(dx);
                                addBottom(dy);
                            } //否则不管，多次移动中的小距离移动填补差值
                            break;

                        //四条边
                        case LEFT:
                            if ((frameWidth - dx) * frameHeight >= MIN_AREA) {
                                addLeft(dx);
                            }
                            break;
                        case RIGHT:
                            if ((frameWidth + dx) * frameHeight >= MIN_AREA) {
                                addRight(dx);
                            }
                            break;
                        case TOP:
                            if ((frameHeight - dy) * frameWidth >= MIN_AREA) {
                                addTop(dy);
                            }
                            break;
                        case BOTTOM:
                            if ((frameHeight + dy) * frameWidth >= MIN_AREA) {
                                addBottom(dy);
                            }
                            break;
                    }
                }
//                PTuLog.d(TAG, frameWidth * frameHeight);
                adjustEdge(frameLeft, frameTop, frameWidth, frameHeight);
                cutView.invalidate();
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (!onTouch) return false;
                //为CutView适配做准备
                lastCenterX = frameX + frameWidth / 2;
                lastCenterY = frameY + frameHeight / 2;
                autoScalePic();
                CUR_STATA = MOVESTATE.NONE;
                onTouch = false;
                break;
        }
        return onTouch;
    }

    private void autoScalePic() {
        if (CUR_STATA == MOVESTATE.CENTER) return;//如果是中间移动，这不需要自动缩放
        float ratio;
        //小于1/3或者大于2/3，自动缩放
        if (frameWidth < totalBound.width() * 1f / 3 && frameHeight < totalBound.height() * 1f / 3) {
            ratio = Math.min(totalBound.width() * 1f / 2 / frameWidth,
                    totalBound.height() * 1f / 2 / frameHeight);
        } else if (frameWidth > totalBound.width() * 2 / 3 || frameHeight > totalBound.height() * 2f / 3) {
            ratio = Math.min(totalBound.width() * 1f / 2 / frameWidth,
                    totalBound.height() * 1f / 2 / frameHeight);
        } else return;

        float centerX = frameLeft + frameWidth / 2;
        float centerY = frameTop + frameHeight / 2;
        ratio = adjustRatio(ratio);
        if (Math.abs(ratio - 1) < 0.01) return;
        String[] pxy = cutView.getLocationAtPicture(centerX, centerY);
        cutView.scale(centerX, centerY, ratio);
        scale(centerX, centerY, ratio);
        cutView.attemptMoveFrame(pxy, centerX, centerY);
    }

    /**
     * 负责缩放，适配边界
     */
    public void scale(float cx, float cy, float ratio) {
        float[] xy = new float[2];
        GeoUtil.getScaledCoord(xy, cx, cy, frameLeft, frameTop, ratio);
        adjustEdge(xy[0], xy[1], frameWidth * ratio, frameHeight * ratio);
    }

    /**
     * 缩放时取的能缩放的最大/最小倍数
     *
     * @param ratio 初始倍数
     * @return 矫正后的倍数
     */
    private float adjustRatio(float ratio) {
        ratio = cutView.getUsableScaleSize(ratio);
        if (frameWidth * ratio * frameHeight * ratio <= MIN_AREA) {
            ratio = (float) Math.sqrt(MIN_AREA / (frameWidth * frameHeight));
        }
        if (frameWidth * ratio > cutView.getDstRect().width()) {
            ratio = cutView.getDstRect().width() / frameWidth;
        }
        if (frameHeight * ratio > cutView.getDstRect().height()) {
            ratio = cutView.getDstRect().height() / frameHeight;
        }
        return ratio;
    }

    /**
     * 在固定比例的情况下重新计算位移值
     */
    private float[] calculateOffsetInFixedRatio(float dx, float dy) {
        if (CUR_STATA == MOVESTATE.RIGHT || CUR_STATA == MOVESTATE.LEFT
                || CUR_STATA == MOVESTATE.BOTTOM || CUR_STATA == MOVESTATE.TOP) {//如果点在四条边上，变化是不一样的，放在下面
        } else { //计算正负
            double angle = -Math.atan2(dy, dx) * 180 / Math.PI;
            //计算大小
            if (Math.abs(dx) < Math.abs(dy)) {
                dx = Math.abs(dx);
                dy = dx * fixedRatio;
            } else {
                dy = Math.abs(dy);
                dx = dy / fixedRatio;
            }
            //确定符号
            if (CUR_STATA == MOVESTATE.LEFT_TOP || CUR_STATA == MOVESTATE.RIGHT_BOTTOM) {
                if (angle > 45 || angle < -135) {//箱右上方移动
                    dx *= -1;
                    dy *= -1;
                }
            } else if (CUR_STATA == MOVESTATE.RIGHT_TOP || CUR_STATA == MOVESTATE.LEFT_BOTTOM) {
                {
                    if (angle > -45 && angle < 135) {
                        dy *= -1;
                    } else {
                        dx *= -1;
                    }
                }
            }
        }
        return new float[]{dx, dy};
    }

    void move(float dx, float dy) {
        //判断边界
        if (frameLeft + dx < cutView.getDstRect().left) frameLeft = cutView.getDstRect().left;
        else if (frameLeft + dx + frameWidth > cutView.getDstRect().right)
            frameLeft = cutView.getDstRect().right - frameWidth;
        else frameLeft += dx;

        if (frameTop + dy < cutView.getDstRect().top) frameTop = cutView.getDstRect().top;
        else if (frameTop + dy + frameHeight > cutView.getDstRect().bottom)
            frameTop = cutView.getDstRect().bottom - frameHeight;
        else
            frameTop += dy;
        adjustEdge(frameLeft, frameTop, frameWidth, frameHeight);
        cutView.invalidate();
    }

    /**
     * 适配并设置好位置和长宽,
     * <p> 保证不超过外边界，
     * <p>保证固定长宽比时的长宽比
     */
    private void adjustEdge(float nframeLeft, float nframeTop, float nframeWidth, float nframeHeight) {
        RectF inRect = new RectF(nframeLeft, nframeTop, nframeLeft + nframeWidth, nframeTop + nframeHeight);
        adjustRectInRect(inRect, cutView.getDstRect());
        frameLeft = inRect.left;
        frameTop = inRect.top;
        frameWidth = inRect.width();
        frameHeight = inRect.height();
        if (fixedRatio > 0) {//固定长宽比的情况,必须把长宽调整到相应的比例
            if (frameWidth * fixedRatio > frameHeight) {//以短边为准进行调整
                frameWidth = frameHeight / fixedRatio;
            } else {
                frameHeight = frameWidth * fixedRatio;
            }
        }
    }

    private void adjustRectInRect(RectF inRect, Rect edgeRect) {
        if (inRect.left < edgeRect.left)
            inRect.left = edgeRect.left;

        if (inRect.top < edgeRect.top)
            inRect.top = edgeRect.top;

        if (inRect.right > edgeRect.right)
            inRect.right = edgeRect.right;

        if (inRect.bottom > edgeRect.bottom)
            inRect.bottom = edgeRect.bottom;
    }


    /**
     * 直接传入总的canvas，里面进行移动再绘图
     */
    public void onDraw(Canvas canvas) {
        // Log.e(TAG, "draw开始 ");

        //暗色未选中背景
        canvas.drawRect(0, 0, canvas.getWidth(), frameTop, noChoosedPaint);//上下
        canvas.drawRect(0, frameTop + frameHeight, canvas.getWidth(), canvas.getHeight(), noChoosedPaint);
        canvas.drawRect(0, frameTop, frameLeft, frameTop + frameHeight, noChoosedPaint);//左右
        canvas.drawRect(frameLeft + frameWidth, frameTop, canvas.getWidth(), frameTop + frameHeight, noChoosedPaint);
        canvas.save();
        canvas.translate(frameLeft, frameTop);
        drawFrame(canvas);
        canvas.restore();
    }

    //画出剪切框
    private void drawFrame(Canvas canvas) {
        for (int i = 0; i <= 3; i++) {//格子线
            if (i == 1 || i == 2) mLinePaint.setStrokeWidth(Util.dp2Px(0.5f));
            else mLinePaint.setStrokeWidth(Util.dp2Px(1.5f));
            canvas.drawLine(0 - 1.5f, frameHeight * i / 3, frameWidth + 1.5f, frameHeight * i / 3, mLinePaint);
            canvas.drawLine(frameWidth * i / 3, 0, frameWidth * i / 3, frameHeight, mLinePaint);
        }

        //画四个角
        //左上
        canvas.drawCircle(0, 0, mCornerWidth, mLumpPaint);
        //右上
        canvas.drawCircle(frameWidth, 0, mCornerWidth, mLumpPaint);
        //左下
        canvas.drawCircle(0, frameHeight, mCornerWidth, mLumpPaint);
        //右下
        canvas.drawCircle(frameWidth, frameHeight, mCornerWidth, mLumpPaint);


        //四条边上
        //上
        edgeShape.rewind();
        edgeShape.moveTo(-mEdgeLumpWidth * 1.4f, 0);
        edgeShape.lineTo(0, -mEdgeLumpWidth / 2);
        edgeShape.lineTo(mEdgeLumpWidth * 1.4f, 0);
        edgeShape.lineTo(0, mEdgeLumpWidth / 2);
        edgeShape.offset(frameWidth / 2, 0);
        canvas.drawPath(edgeShape, mLumpPaint);

        //下
        edgeShape.rewind();
        edgeShape.moveTo(-mEdgeLumpWidth * 1.4f, 0);
        edgeShape.lineTo(0, mEdgeLumpWidth / 2);
        edgeShape.lineTo(mEdgeLumpWidth * 1.4f, 0);
        edgeShape.lineTo(0, -mEdgeLumpWidth / 2);
        edgeShape.offset(frameWidth / 2, frameHeight);
        canvas.drawPath(edgeShape, mLumpPaint);

        //左边
        edgeShape.rewind();
        edgeShape.moveTo(0, -mEdgeLumpWidth * 1.4f);
        edgeShape.lineTo(-mEdgeLumpWidth / 2, 0);
        edgeShape.lineTo(0, mEdgeLumpWidth * 1.4f);
        edgeShape.lineTo(mEdgeLumpWidth / 2, 0);
        edgeShape.offset(0, frameHeight / 2);
        canvas.drawPath(edgeShape, mLumpPaint);

        //右边
        edgeShape.rewind();
        edgeShape.moveTo(0, -mEdgeLumpWidth * 1.4f);
        edgeShape.lineTo(mEdgeLumpWidth / 2, 0);
        edgeShape.lineTo(0, mEdgeLumpWidth * 1.4f);
        edgeShape.lineTo(-mEdgeLumpWidth / 2, 0);
        edgeShape.offset(frameWidth, frameHeight / 2);
        canvas.drawPath(edgeShape, mLumpPaint);

        //中间的数字
        String show = String.format(Locale.getDefault(), "%d × %d",
                Math.round(frameWidth / cutView.getTotalRatio()),
                Math.round(frameHeight / cutView.getTotalRatio()));
        float textWidth = textPaint.measureText(show);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = frameHeight / 2 - fm.descent + (fm.bottom - fm.top) / 2;
        canvas.drawText(show, (frameWidth - textWidth) / 2, textY, textPaint);

    }


    /**
     * 增加左边，建议编译器内联的方法使用此函数
     * <p>左边加多少，宽度减多少
     */
    private void addLeft(float dx) {
        frameLeft += dx;
        frameWidth -= dx;
    }

    /**
     * 上边加多少，高度减多少
     */
    private void addTop(float dy) {
        frameTop += dy;
        frameHeight -= dy;
    }

    /**
     * 右边加多少，宽度加多少
     */
    private void addRight(float dx) {
        frameWidth += dx;
    }

    /**
     * 下边加多少，高度加多少
     */
    private void addBottom(float dy) {
        frameHeight += dy;//上边加多少，高度减多少
    }


    /********************************************************************************/
    /**
     * 固定比例，让裁剪框满足最大的塞入到显示出的图片之中
     */
    void setFixedRatio(float fixedRatio) {
        Rect dstRect = cutView.getDstRect();
        this.fixedRatio = fixedRatio;
        float dstRatio = dstRect.height() * 1f / dstRect.width();

        if (fixedRatio > dstRatio) {//高宽比图片显示部分高，水平居中
            frameHeight = dstRect.height();
            frameWidth = (int) (frameHeight / fixedRatio + 0.5f);
            frameTop = dstRect.top;
            frameLeft = dstRect.left + (dstRect.width() - frameWidth) / 2;
        } else {//否则垂直居中
            frameWidth = dstRect.width();
            frameHeight = (int) (frameWidth * fixedRatio + 0.5f);
            frameLeft = dstRect.left;
            frameTop = dstRect.top + (dstRect.height() - frameHeight) / 2;
        }
    }

    void cancelFixedRatio() {
        this.fixedRatio = -1;
    }

    /**
     * 固定尺寸,用户操作上相当于固定比例，最后获取图片时缩放即可
     */
    void setFixedSize(int width, int height) {
        fixedWidth = width;
        fixedHeight = height;
        setFixedRatio(height * 1f / width);//用户操作上相当于固定比例，
    }

    void cancelFixedSize() {
        fixedWidth = fixedHeight = -1;
        cancelFixedRatio();
    }

    /******************************************************************************/ {
        /**  @Deprecated public void scale(float centerX, float centerY, float ratio, Rect dstRect) {
        float minRatio = Math.max(MIN_WIDTH / mWidth, MIN_WIDTH / mHeight);//最小的缩放比例
        ratio = Math.max(minRatio, ratio);

        float maxRatio = Math.min(dstRect.width() / mWidth, dstRect.height() / mHeight);//最大的
        ratio = Math.min(maxRatio, ratio);

        mWidth *= ratio;
        mHeight *= ratio;
        mLeft = GeoUtil.getScaledX(mLeft, mTop, centerX, centerY, ratio);
        mTop = GeoUtil.getScaledY(mLeft, mTop, centerX, centerY, ratio);

        if (mLeft < dstRect.left) mLeft = dstRect.left;//放大时超出外边界，缩小时超出内边界，上面处理
        if (mTop < dstRect.top) mTop = dstRect.top;
        }
         */


        /**
         * 经验：此函数调试很久，功能是移动不能超过一定限度，如果超过，就移动到当前位置到限度的差值，
         * 实际上移动会产生很多次，而且这里对精确度要求不高，如果超过限度，不移动即可，效果相同的，
         * 多次移动差不不同，小长度的会填补差值
         * 保证长和宽增加后面积是最小面积，长宽增加值的 比例保持dy/dx原来的比例，
         * 解一元2次方程组
         * @return 最后的增加距离
         */
       /* private float[] addDisInMinArea(float dx, float dy) {
        float a = dy / dx;
        float b = frameWidth * a + frameHeight;
        float c = frameWidth * frameHeight - MIN_AREA;
        float dert=b*b-4*a*c;
        float[] wh =new float[]{0,0};
        if(dert<=0||Math.abs(a-0)<0.1)return wh;

        wh[0] = (-b + (float) Math.sqrt(b * b - 4 * a * c)) / 2 / a;
        wh[1] = wh[0] * a;
        PTuLog.d("调整后的差值"+wh[0]+" "+wh[1]);
        float whRatio = (frameHeight + wh[1]) / (frameWidth + wh[0]) / (frameHeight / frameWidth);
        if (frameWidth + wh[0] < 0 ||
                Math.abs(whRatio - 1) > 1) {//有两个结果，结果为负不能取，宽高比变化太大不能取
            PTuLog.d("缩放比例结果的差值1"+ Math.abs(whRatio - 1));
            wh[0] = (-b - (float) Math.sqrt(b * b - 4 * a * c)) / 2 / a;
            wh[1] = wh[0] * a;
        }
        whRatio = (frameHeight + wh[1]) / (frameWidth + wh[0]) / (frameHeight / frameWidth);
        PTuLog.d("缩放比例结果的差值2"+ Math.abs(whRatio - 1));
        if( Math.abs(whRatio - 1) > 1||Math.abs(wh[0]-0)<0.1||Math.abs(wh[1]-0)<0.1)wh[0]=wh[1]=0;
            return new float[]{0,0};
        }*/
    }
}
