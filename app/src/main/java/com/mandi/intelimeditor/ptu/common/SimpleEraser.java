package com.mandi.intelimeditor.ptu.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.ptu.PTuActivityInterface;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.view.PtuFrameLayout;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/19 0019.
 * 简单的橡皮，也可以说是画笔，用于在底图上绘制颜色的
 */
public class SimpleEraser extends View {

    private PtuSeeView ptuSeeView;
    float lastX, lastY;
    private Paint paint;
    private Paint picPaint;
    private Path path;
    private Path picPath;
    private boolean isUp = false;
    private boolean hasMove;

    private RepealRedoListener repealRedoListener;

    int color;
    int paintWidth;
    private final RepealRedoManager<Pair<Path, Paint>> repealRedoManager;
    private final RepealRedoManager<Pair<Path, Paint>> picRR_manager;
    private PTuActivityInterface pTuActivityInterface;
    private final Bitmap enlargeBm;
    private boolean isShowTouchPEnlarge = true;

    public SimpleEraser(Context context, PTuActivityInterface pTuActivityInterface) {
        super(context);
        this.pTuActivityInterface = pTuActivityInterface;
        this.ptuSeeView = pTuActivityInterface.getPtuSeeView();
        setBackground(null);
        color = Color.WHITE;
        paintWidth = Util.dp2Px(20);

        paint = getNewPaint(color, paintWidth);
        picPaint = getNewPaint(color, paintWidth * ptuSeeView.getSrcRect().height() * 1f / ptuSeeView.getDstRect().height());

        repealRedoManager = new RepealRedoManager<>(100);
        picRR_manager = new RepealRedoManager<>(100);
        path = new Path();
        picPath = new Path();
        enlargeBm = Bitmap.createBitmap(PtuFrameLayout.DEFAULT_ENLARGE_W, PtuFrameLayout.DEFAULT_ENLARGE_W, Bitmap.Config.ARGB_8888);
    }

    public void setIsShowTouchPEnlarge(boolean isShow) {
        this.isShowTouchPEnlarge = isShow;
    }

    private Paint getNewPaint(int color, float width) {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘(Paint.Cap.ROUND);
        return paint;
    }

    float[] plxy = new float[2];

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        float[] pxy = PtuUtil.getLocationAtBaseBm(x + getLeft(), y + getTop(),
                ptuSeeView.getSrcRect(), ptuSeeView.getDstRect());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                hasMove = false;
                isUp = false;
                path = new Path();
                picPath = new Path();

                lastX = x;
                lastY = y;
                plxy[0] = pxy[0];
                plxy[1] = pxy[1];
                path.moveTo(x, y);
                picPath.moveTo(pxy[0], pxy[1]);
                showTouchPEnlarge(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (GeoUtil.getDis(lastX, lastY, x, y) >= 1) {
                    hasMove = true;
                }
                path.quadTo(lastX, lastY, x, y);
                picPath.quadTo(plxy[0], plxy[1], pxy[0], pxy[1]);
                lastX = x;
                lastY = y;
                plxy[0] = pxy[0];
                plxy[1] = pxy[1];
                showTouchPEnlarge(x, y);
                break;
            case MotionEvent.ACTION_UP:
                isUp = true;
                if (!hasMove) {
                    x += 1;
                    y += 1;
                    pxy[0] += 1;
                    pxy[1] += 1;
                }
                hasMove = false;
                path.quadTo(lastX, lastY, x, y);
                picPath.quadTo(plxy[0], plxy[1], pxy[0], pxy[1]);
                repealRedoManager.commit(new Pair<>(path, paint));
                picRR_manager.commit(new Pair<>(picPath, picPaint));
                repealRedoListener.canRedo(repealRedoManager.canRedo());
                repealRedoListener.canRepeal(repealRedoManager.canRepeal());
                pTuActivityInterface.showTouchPEnlargeView(null, 0, 0, 0);
                break;
        }
        invalidate();
        return true;
    }

    private void showTouchPEnlarge(float x, float y) {
        if (!isShowTouchPEnlarge) return;
        if (pTuActivityInterface != null) {
            PtuUtil.enlargeViewPoint(ptuSeeView,
                    x + ptuSeeView.getDstRect().left, y + ptuSeeView.getDstRect().top,
                    paintWidth * 3, enlargeBm, true, paintWidth, 0);
            PtuUtil.enlargeViewPoint(this, x, y, paintWidth * 3, enlargeBm, false, paintWidth, 0);
            pTuActivityInterface.showTouchPEnlargeView(enlargeBm, paintWidth,
                    x + ptuSeeView.getDstRect().left, y + ptuSeeView.getDstRect().top);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int index = repealRedoManager.getCurrentIndex();
        for (int i = 0; i <= index; i++) {
            Pair<Path, Paint> sd = repealRedoManager.getStepdata(i);
            canvas.drawPath(sd.first, sd.second);
        }
        if (!isUp)//没有到up，最后一笔的数据没有加到撤消重做列表中，所以要单独画出来
            canvas.drawPath(path, paint);
        super.onDraw(canvas);
    }

    public void smallRedo() {
        if (repealRedoManager.canRedo()) {
            repealRedoManager.redo();
            picRR_manager.redo();
            refreshRepealRedo();
            invalidate();
        }
    }

    public void smallRepeal() {
        if (repealRedoManager.canRepeal()) {
            repealRedoManager.repealPrepare();
            picRR_manager.repealPrepare();
            refreshRepealRedo();
            invalidate();
        }
    }

    private void refreshRepealRedo() {
        if (repealRedoListener != null) {
            repealRedoListener.canRedo(repealRedoManager.canRedo());
            repealRedoListener.canRepeal(repealRedoManager.canRepeal());
        }
    }

    public ArrayList<Pair<Path, Paint>> getResultData() {
        ArrayList<Pair<Path, Paint>> pathPaintList = new ArrayList<>();
        int index = repealRedoManager.getCurrentIndex();
        for (int i = 0; i <= index; i++) {
            pathPaintList.add(picRR_manager.getStepdata(i));
        }
        return pathPaintList;
    }

    public void setColor(int color) {
        this.color = color;
        paint = getNewPaint(color, paintWidth);
        picPaint = getNewPaint(color, paintWidth * ptuSeeView.getSrcRect().height() * 1f / ptuSeeView.getDstRect().height());
    }

    public void setRubberWidth(int width) {
        this.paintWidth = width;
        paint = getNewPaint(color, width);
        picPaint = getNewPaint(color, width * ptuSeeView.getSrcRect().height() * 1f / ptuSeeView.getDstRect().height());
    }

    public int getRubberWidth() {
        return (int) paint.getStrokeWidth();
    }

    public void setRepealRedoListener(RepealRedoListener repealRedoListener) {
        this.repealRedoListener = repealRedoListener;
    }

    public int getColor() {
        return color;
    }

    public void finishErase() {
        repealRedoManager.clear(getContext());
        picRR_manager.clear(getContext());
        refreshRepealRedo();
    }

    public void cancelErase() {
        repealRedoManager.clear(getContext());
        picRR_manager.clear(getContext());
        refreshRepealRedo();
    }
}
