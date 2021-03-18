package com.mandi.intelimeditor.ptu;

import android.graphics.PointF;
import android.view.MotionEvent;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import view.TSRView;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/25
 *      version : 1.0
 * <pre>
 */
public class PicGestureListener {

    private float lastX = -1;
    private float lastY = -1;
    private float lastDis = 0;
    private float lastAngle = 0;
    private float downCenterX = 0;
    private float downCenterY = 0;
    public static final int clickInternalTime = 300;
    private Util.DoubleClick mDoubleClick = new Util.DoubleClick(clickInternalTime); // 公用类，不使用静态的双击判断，避免冲突

    public boolean onTouchEvent(TSRView view, MotionEvent event) {
        float x = event.getX(), y = event.getY();
        float x0 = event.getX(0), y0 = event.getY(0);
        float x1 = 0, y1 = 0;
        float touchCenterX = 0, touchCenterY = 0;
        PointF p1 = new PointF(x0, y0), p2 = null;
        if (event.getPointerCount() > 1) {
            x1 = event.getX(1);
            y1 = event.getY(1);
            touchCenterX = (x0 + x1) / 2;
            touchCenterY = (y0 + y1) / 2;
            p2 = new PointF(x1, y1);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
//                LogUtil.e("经过了ACTION_DOWN");
                lastX = x;
                lastY = y;
                mDoubleClick.isDoubleClick_m();
                view.onFirstFingerDown(x0, y0);
                view.twoFingerDisChange(p1, p2, true);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
//                LogUtil.e("ACTION_POINTER_DOWN");
                // 缩放
                Util.DoubleClick.cancel();//点击事件取消
                lastDis = GeoUtil.getDis(x0, y0, x1, y1);
                view.onMultiFingerDown();
                view.twoFingerDisChange(p1, p2, true);
                downCenterX = touchCenterX;
                downCenterY = touchCenterY;

                // 旋转
                lastAngle = GeoUtil.getAngleDegree(x0, y0, x1, y1);

                // 双指移动
                lastX = (x0 + x1) / 2;
                lastY = (y0 + y1) / 2;
            case MotionEvent.ACTION_MOVE:
                //这里doubleclick用来判断点击，移动很短的距离，仍然算作点击，up的时候点击生效，
                // 移动距离远，点击则取消点击
                // 有些手机屏幕不稳定，手指按下去之后的时候一定发生move动作
//                LogUtil.e("ACTION_MOVE");
                if (GeoUtil.getDis(event.getX(), event.getY(), lastX, lastY) > 5)
                    Util.DoubleClick.cancel();

                if (event.getPointerCount() == 1) {
                    // 单指移动
                    view.move(x - lastX, y - lastY, false);
                    lastX = x;
                    lastY = y;
                } else {
                    //缩放
                    float endD = GeoUtil.getDis(x0, y0, x1, y1);
                    float currentRatio = endD / lastDis;
                    lastDis = endD;
                    // 双指距离改变
                    view.twoFingerDisChange(p1, p2, false);

                    // 默认按照用户第一次两个手指触摸到屏幕进行旋转
                    view.scale(downCenterX, downCenterY, currentRatio);

                    //旋转
                    float curAngle = GeoUtil.getAngleDegree(x0, y0, x1, y1);

                    // 默认按照当前触摸的手指的中心旋转
                    view.rotate(touchCenterX, touchCenterY, curAngle - lastAngle);
                    lastAngle = curAngle;
                    // 双指移动
                    view.move(touchCenterX - lastX, touchCenterY - lastY, true);
                    lastX = touchCenterX;
                    lastY = touchCenterY;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
//                Logcat.e("ACTION_POINTER_UP");
                // 剩下一个手指时，获取该手指的位置，后面跟着能进行移动
                int index = event.getActionIndex();
                if (event.getPointerCount() == 2) { // 两个手指变一个，更新一个手指相关参数
                    lastX = event.getX(index == 0 ? 1 : 0); // 起来一个，位置拿另一个的
                    lastY = event.getY(index == 0 ? 1 : 0);
                    view.twoFingerDisChange(p1, null, true);
                } else if (event.getPointerCount() == 3) { // 3个手指变两个手指，更新两个手指相关的参数
                    int i0, i1;
                    if (index == 0) {
                        i0 = 1;
                        i1 = 2;
                    } else if (index == 1) {
                        i0 = 0;
                        i1 = 2;
                    } else {
                        i0 = 0;
                        i1 = 1;
                    }
                    //缩放
                    float nx0 = event.getX(i0), ny0 = event.getY(i0), nx1 = event.getX(i1), ny1 = event.getY(i1);
                    lastDis = GeoUtil.getDis(nx0, ny0, nx1, ny1);

                    view.twoFingerDisChange(new PointF(nx0, ny0), new PointF(nx1, ny1), true);

                    //旋转
                    lastAngle = GeoUtil.getAngleDegree(event.getX(i0), event.getY(i0), event.getX(i1), event.getY(i1));

                    //移动
                    lastX = (event.getX(i0) + event.getX(i1)) / 2;
                    lastY = (event.getY(i0) + event.getY(i1)) / 2;
                }
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.e("ACTION_UP");
                view.twoFingerDisChange(null, null, true);
                view.onLastFingerUp(x, y);
                if (mDoubleClick.isDoubleClick_m())//判断发生点击事件, 注意是判断的单击事件，不是双击
                {
                    view.onClick(x, y);
                }
                break;
            //如果点击到了tietu上面
            default:
                break;
        }
        return true;
    }
}
