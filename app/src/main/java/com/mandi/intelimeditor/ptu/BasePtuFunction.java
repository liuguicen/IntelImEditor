package com.mandi.intelimeditor.ptu;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.ptu.repealRedo.StepData;

/**
 * ptu操作的基本功能，相应的Fragment实现
 * Created by Administrator on 2016/7/27.
 */
public interface BasePtuFunction {
    /**
     * 子功能撤销,注意目前这个方法，子功能的Fragment不可撤销时，用户点击也会产生调用，
     * 需要判断一下。待更改
     */
    void smallRepeal();


    /**
     * 子功能重做
     */
    void smallRedo();

    /**
     * 一些子功能需要在主线程生成数据，用这个方法
     */
    void generateResultDataInMain(float ratio);

    /**
     * 某个一级功能完成，比如贴图，
     * （1) 生成该功能的操作数据，并将该数据返回作为撤销重做所需的数据
     * （2）需要的话，将操作数据画到底图上面，
     * 注意，这里的函数会在非UI线程调用，做UI线程才能做的动作要post
     * 没有操作数据返回空
     */
    @Nullable
    StepData getResultDataAndDraw(float ratio);

    void releaseResource();
    /**
     * 清除数据
     */
    void clear();

    /**
     * 责任链模式，处理按键返回事件
     *
     * @return 是否消费返回按键
     */
    boolean onBackPressed(boolean isFromKey);
}
