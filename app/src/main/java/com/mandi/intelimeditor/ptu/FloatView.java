package com.mandi.intelimeditor.ptu;

import android.graphics.Canvas;

/**
 * Created by Administrator on 2016/5/31.
 */
public interface FloatView {
    /**
     * 透明
     */
    int STATUS_TOUMING = 1;
    /**
     * 只显示边框
     */
    int STATUS_RIM = 2;
    /**
     * 显示子项目
     */
    int STATUS_ITEM = 3;
    /**
     * 显示输入法，输入状态
     */
    int STATUS_INPUT = 4;


    /**
     * 获取中的宽度
     *
     * @return
     */
    float getmWidth();

    /**
     * 获取总的高度
     *
     * @return
     */
    float getmHeight();

    /**
     * 获取顶部的位置
     *
     * @return
     */
    float getfTop();

    /**
     * 获取左边的位置
     *
     * @return
     */
    float getfLeft();

    /**
     * 获取x的相对位移
     *
     * @return
     */
    float getRelativeX();

    /**
     * 获取y的相对位移
     *
     * @return
     */
    float getRelativeY();

    /**
     * 设置y的相对位移
     *
     * @return
     */
    void setRelativeY(float relativeY);

    /**
     * 设置y的相对位移
     *
     * @return
     */
    void setRelativeX(float relativeX);

    /**
     * 获取视图的显示状态：边框，item等
     *
     * @return
     */
    int getShowState();

    /**
     * 改变当前的显示状态，显示变换操作逻辑处理中心，有些复杂，要注意
     *
     * @param state <p>STATUS_TOUMING = 1;//透明
     *              <p>int STATUS_RIM = 2;边框
     *              <p>STATUS_ITEM = 3;//子项目
     *              <p>STATUS_INPUT = 4;//输入法
     */
    void changeShowState(int state);

    /**
     * 缩放视图，重置视图的宽和高，然后重绘
     *
     * @param ratio
     */
    void scale(float ratio);

    /**
     * @param ratio
     */
    float adjustSize(float ratio);

    /**
     * 适配floatview的位置,不能超出图片的边界,不算padding的内部就不能超出边界
     * 超出之后移动startx，starty,不影响其它数据
     *
     * @return 返回是否需要位置是否改变
     */
    boolean adjustEdgeBound(float nx, float ny);

    /**
     * 拖动floatview，利用相对点，相对点变化，
     * view根据原来他与相对点的坐标差移动，避免了拖动
     * 处理的误差
     *
     * @param nx 新的对应点的x坐标
     * @param ny 新的对应点的y坐标
     */
    void drag(float nx, float ny);

    void drawItem(Canvas canvas, MicroButtonData item);

    void initItems();

}
