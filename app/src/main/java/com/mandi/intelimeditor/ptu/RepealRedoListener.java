package com.mandi.intelimeditor.ptu;

/**
 * Created by Administrator on 2016/11/20 0020.
 */

public interface RepealRedoListener {
    /**
     * 同通知能否重做
     */
    void canRedo(boolean canRedo);

    /**
     * 通知能否撤销
     */
    void canRepeal(boolean canRepeal) ;
}
