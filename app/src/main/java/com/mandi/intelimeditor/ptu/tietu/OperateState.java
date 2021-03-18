package com.mandi.intelimeditor.ptu.tietu;

/**
 * 贴图的操作状态
 */
public class OperateState {

    /**
     * 选中状态，也是显示边框的状态
     */
    public static final int CHOSEN = 214;

    /**
     * 预览状态，不显示边框，点击之后返回Chosen状态
     */
    public static final int PREVIEW = 908;

    /**
     * 锁住状态，一遍不能进行编辑操作
     * 主要用于支持gif的撤销重做
     * 解锁操作暂未想出
     */
    public static final int LOCKED = 940;

    private int curState;

    OperateState() {
        curState = CHOSEN;
    }


    public boolean isIn_Chosen() {
        return curState == CHOSEN;
    }

    public boolean isIn_preview() {
        return curState == PREVIEW;
    }

    public boolean isIn_locked() {
        return curState == LOCKED;
    }

    public void toChosen() {
        curState = CHOSEN;
    }

    public void toPreview() {
        curState = PREVIEW;
    }

    public void toLocked() {
        curState = LOCKED;
    }

}
