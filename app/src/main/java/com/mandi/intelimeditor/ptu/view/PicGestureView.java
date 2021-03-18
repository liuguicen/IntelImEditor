package com.mandi.intelimeditor.ptu.view;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import view.TSRView;

import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/26
 *      version : 1.0
 * <pre>
 */
public abstract class PicGestureView extends View implements TSRView {

    public PicGestureView(Context context) {
        super(context);
    }

    public PicGestureView(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    public void scale(float centerX, float centerY, float ratio) {

    }

    @Override
    public void move(float dx, float dy, boolean isMultiFinger) {

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
        return ratio;
    }

    @Override
    public boolean onClick(float x, float y) {
        return false;
    }

    @Override
    public void onFirstFingerDown(float x0, float y0) {

    }

    @Override
    public void onMultiFingerDown() {

    }

    @Override
    public boolean onLastFingerUp(float x, float y) {
        return false;
    }
}
