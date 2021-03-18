package com.mandi.intelimeditor.ptu.rendpic;

import android.util.Log;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/28
 *      version : 1.0
 * <pre>
 */
public class RendState {
    /** 三个状态 **/
    public static final int NOT_REND = 0;
    public static final int READY = 1;
    public static final int INCREASE_CRACK = 2;
    public static final int MOVING_FRAG = 3;
    public int rendState;

    public RendState() {
        rendState = NOT_REND;
    }

    public void startRendAndEnterReady() {
        rendState = READY;
//        Logcat.e("enter ready");
    }

    public boolean isIn_Ready() {
        return rendState == READY;
    }

    /**
     * 停止裂纹，从裂纹状态变回完整状态
     */
    public void return_Ready() {
        rendState = RendState.READY;
//        Logcat.e("");
    }

    public void enter_increaseCrack() {
        rendState = INCREASE_CRACK;
        Log.e("---+", "enter_increaseCrack: ");
    }


    /**
     * 从裂纹状态变到分开状态
     */
    public void enter_moveFrag() {
        rendState = RendState.MOVING_FRAG;
//        Logcat.e();
    }

    public boolean isIn_moveFrag() {
        return rendState == RendState.MOVING_FRAG;
    }
    
    /**
     * 分开变回裂纹
     */
    public void stopDivid() {
        rendState = RendState.INCREASE_CRACK;
    }

    /**
     * 撕图模式下，且图没被撕开，屏幕上出现两个手指开始即进入裂痕阶段，
     * 两个手指变成单个，退出裂痕阶段
     * 滑动距离达到限制，变成撕开，退出裂痕阶段
     * @return
     */
    public boolean isIn_increaseCrack() {
        return rendState == RendState.INCREASE_CRACK;
    }


    public boolean isIn_Rend() {
        return rendState != NOT_REND;
    }

    public void stopRendAndRest() {
        rendState = NOT_REND;
    }

    public void return_increaseCrack() {
        rendState = INCREASE_CRACK;
    }

    public void return_notRend() {
        rendState = NOT_REND;
    }
}
