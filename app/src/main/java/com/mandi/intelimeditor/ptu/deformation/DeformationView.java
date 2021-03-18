package com.mandi.intelimeditor.ptu.deformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.device.YearClass;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.RepealRedoListener;
import com.mandi.intelimeditor.ptu.imageProcessing.BmDeformation;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeformationView extends View {
    public static final String TAG = "DeformationView";
    private static int MAX_DEFOR_WIDTH = 1024;
    public static int MAX_DEFOR_HEIGHT = 1536;

    static {
        if (AllData.globalSettings.performanceYear < YearClass.PERFORMANCE_2G_4G) {
            MAX_DEFOR_WIDTH *= 0.40;
            MAX_DEFOR_HEIGHT *= 0.40;
        } else if (AllData.globalSettings.performanceYear < YearClass.PERFORMANCE_4G_6G) {
            MAX_DEFOR_WIDTH *= 0.66;
            MAX_DEFOR_HEIGHT *= 0.66;
        } else if (AllData.globalSettings.performanceYear < YearClass.PERFORMANCE_6G_8G) {
            MAX_DEFOR_WIDTH *= 0.80;
            MAX_DEFOR_HEIGHT *= 0.80;
        }
    }

    private PtuSeeView ptuSeeView;
    private float startX, startY;
    private float curX, curY;
    private float thresholdRatio = 0.1f;

    private float deformationRadius;
    private Paint indicatorPaint;
    private int bmW;
    private int bmH;
    private float[] verts;
    private float[] lastVerts;
    @Nullable
    private Bitmap src;
    private Bitmap deforedBm;
    private boolean showIndicator = true;
    private boolean isTouchUp = true;
    private RepealRedoListener rrListener;
    private int optionNumber = 0;
    private int meshW;
    private int meshH;
    private int meshCount;
    private Canvas deforCanvas;
    private RepealRedoManager<List<DeforOperData>> rrManager;
    private List<DeforOperData> deforOperList;

    public DeformationView(Context context, PtuSeeView ptuSeeView) {
        super(context);
        init(ptuSeeView);
    }

    private void init(PtuSeeView ptuSeeView) {
        this.ptuSeeView = ptuSeeView;
        rrManager = new RepealRedoManager<>(100);
        initBmData();
        deformationRadius = ptuSeeView.getWidth() / 7f;
        initIndicatorPaint();
    }

    private void initBmData() {
        src = adjustSize(ptuSeeView);
        if (src == null || src.isRecycled()) {
            Log.e(TAG, "initBmData: 获取bm为空或者出错");
            ToastUtils.show("出错了，您可以尝试保存，再重新进入应用！");
            return;
        }
        rrManager.setBaseBm(src);
        //        if (!src.isMutable()) {
        //            src = src.copy(null, true);
        //        }
        //  遍历扭曲半径内的像素，通过遍历包裹它的正方形，

        bmW = src.getWidth();
        bmH = src.getHeight();
        deforedBm = src.copy(src.getConfig(), true);
        deforCanvas = new Canvas(deforedBm);
        ptuSeeView.replaceSourceBm(deforedBm);

        meshW = bmW / 5;
        meshH = bmH / 5;
        meshCount = (meshW + 1) * (meshH + 1);
        verts = new float[meshCount * 2];
        lastVerts = new float[meshCount * 2];
        int index = 0;
        for (int i = 0; i < meshH + 1; i++) {
            float fy = bmH * 1f / meshH * i;  // bm高度除以格子数量 = 格子宽度 * 格子数 = 格子的线的坐标
            for (int j = 0; j < meshW + 1; j++) {
                float fx = bmW * 1f / meshW * j;
                //X轴坐标 放在偶数位
                verts[index * 2] = fx;
                lastVerts[index * 2] = verts[index * 2];
                //Y轴坐标 放在奇数位
                verts[index * 2 + 1] = fy;
                lastVerts[index * 2 + 1] = verts[index * 2 + 1];
                index += 1;
            }
        }
    }


    /**
     * 变形操作过程中计算量很大，对于大图来说基本不行，
     * 目前调整尺寸，后期采取其它方法
     */
    private Bitmap adjustSize(PtuSeeView ptuSeeView) {
        Bitmap sourceBm = ptuSeeView.getSourceBm();
        if (sourceBm == null) return null;
        int[] dstWh = new int[]{sourceBm.getWidth(), sourceBm.getHeight()};
        boolean isExceed = GeoUtil.adjustWhIn(dstWh, MAX_DEFOR_WIDTH, MAX_DEFOR_HEIGHT);
        Bitmap scaledBitmap = sourceBm;
        if (isExceed) {
            scaledBitmap = Bitmap.createScaledBitmap(sourceBm, dstWh[0], dstWh[1], true);
            ptuSeeView.replaceSourceBm(scaledBitmap);
        }
        return scaledBitmap;
    }

    private void initIndicatorPaint() {
        indicatorPaint = new Paint();
        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStrokeWidth(Util.dp2Px(2));
        indicatorPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (src == null) return false;
        curX = event.getX();
        curY = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isTouchUp = false;
                showIndicator = false;
                startX = curX;
                startY = curY;
                deforOperList = new ArrayList<>();
                break;

            case MotionEvent.ACTION_MOVE:
                float moveDis = GeoUtil.getDis(curX, curY, startX, startY);
                if (LogUtil.debugDeformation) {
                    Log.d(TAG, "onTouchEvent: move dis " + moveDis);
                }
                if (moveDis > thresholdRatio * deformationRadius) {
                    DeforOperData operData = deformation();
                    if (operData != null)
                        deforOperList.add(operData);
                }
                break;

            case MotionEvent.ACTION_UP:
                isTouchUp = true;
                optionNumber++;
                rrManager.commit(deforOperList);
                //                src = deforedBm; // 手指抬起之后，底图变了，以这个图为准进行变形
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    private DeforOperData deformation() {
        float endX, endY;
        if (LogUtil.debugDeformation) {
            Log.d(TAG, "deformation: start ");
        }
        // 拖动的距离必须比半径大很多，变形看上去才会平滑, 所以这里按照拖动方向放大end点坐标
        // 不知为何，难道算法代码写错了
        float moveDis = GeoUtil.getDis(curX, curY, startX, startY);
        if (moveDis <= 0.5) return null;
        float scaleRatio = deformationRadius * 2 / moveDis;
        endX = startX + (curX - startX) * scaleRatio;
        endY = startY + (curY - startY) * scaleRatio;

        MPoint start = PtuUtil.getLocationAtBaseBm(startX, startY, ptuSeeView);
        MPoint end = PtuUtil.getLocationAtBaseBm(endX, endY, ptuSeeView);
        float deforR_in_pic = deformationRadius / ptuSeeView.getTotalRatio();
        BmDeformation.deformation(verts, bmW, bmH,
                start,
                end,
                deforR_in_pic);
        //        warp(start.x, start.y, end.x, end.y);
        long drawStartTime = System.currentTimeMillis();
        if (LogUtil.debugDeformation) {
            Log.d(TAG, "deformation: ");
        }
        deforCanvas.drawBitmapMesh(src, meshW, meshH, verts, 0, null, 0, null);
        if (LogUtil.debugDeformation) {
            Log.d(TAG, "deformation: draw time = " + (System.currentTimeMillis() - drawStartTime));
        }
        ptuSeeView.invalidate();
        startX = curX;
        startY = curY;
        if (rrListener != null)
            rrListener.canRepeal(true);
        //        ptuSeeView.meshDeformation();
        if (LogUtil.debugDeformation) {
            Log.d(TAG, "deformation: finish");
        }
        return new DeforOperData(start, end, deforR_in_pic);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isTouchUp) {
            canvas.drawCircle(curX, curY, deformationRadius, indicatorPaint);
        } else if (showIndicator) {
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, deformationRadius, indicatorPaint);
        }
    }

    public float getDeformationRadius() {
        return deformationRadius;
    }

    public void setDeformationRadius(float deformationRadius) {
        this.deformationRadius = deformationRadius;
        showIndicator = true;
        invalidate();
    }

    public void setRrListener(RepealRedoListener rrListener) {
        this.rrListener = rrListener;
    }

    public void repeal() {
        if (rrManager.canRepeal()) {
            if (LogUtil.debugDeformation) {
                Log.d(TAG, "repeal: start");
            }
            long startTime = System.currentTimeMillis();
            Bitmap baseBitmap = rrManager.getBaseBitmap();
            if (baseBitmap == null) { // 有问题，无法撤销，这样比崩溃好很多
                LogUtil.e("repeal redo base bm is null");
                return;
            }
            verts = Arrays.copyOf(lastVerts, lastVerts.length);
            rrManager.repealPrepare();
            // 实质上是重新做最后一步外的所有操作
            int index = rrManager.getCurrentIndex();
            for (int i = 0; i <= index; i++) {
                List<DeforOperData> odList = rrManager.getStepdata(i);
                for (DeforOperData odData : odList) {
                    BmDeformation.deformation(verts, bmW, bmH, odData.start, odData.end, odData.r);
                }
            }
            if (LogUtil.debugDeformation) {
                Log.d(TAG, "repeal: calculate time = " + (System.currentTimeMillis() - startTime));
            }
            deforCanvas.drawBitmapMesh(src, meshW, meshH, verts, 0, null, 0, null);
            if (LogUtil.debugDeformation) {
                Log.d(TAG, "repeal: draw time = " + (System.currentTimeMillis() - startTime));
            }
            if (rrListener != null) {
                rrListener.canRepeal(rrManager.canRepeal());
                rrListener.canRedo(rrManager.canRedo());
            }
            ptuSeeView.invalidate();
        }
    }

    public void redo() {
        if (rrManager.canRedo()) {
            List<DeforOperData> odList = rrManager.redo();
            for (DeforOperData od : odList) {
                BmDeformation.deformation(verts, bmW, bmH, od.start, od.end, od.r);
            }
            deforCanvas.drawBitmapMesh(src, meshW, meshH, verts, 0, null, 0, null);
            if (rrListener != null) {
                rrListener.canRepeal(true);
                rrListener.canRedo(false);
            }
            ptuSeeView.invalidate();
        }
    }

    public Bitmap getResultBm() {
        return ptuSeeView.getSourceBm();
    }

    public int getOperationNumber() {
        return optionNumber;
    }

    public void testDeformation() {
        startX = getWidth() / 2f;
        startY = getHeight() / 2f;
        curX = startX + 720;
        curY = startY;
        deformation();
    }

    public void setShowIndicator(boolean showIndicator) {
        this.showIndicator = showIndicator;
        invalidate();
    }

    public Bitmap getOriginalBm() {
        return src;
    }

    public boolean hasChange() {
        return rrManager.hasChange();
    }
}
