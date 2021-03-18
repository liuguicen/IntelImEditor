package view

import android.graphics.PointF
import io.reactivex.annotations.Nullable

/**
 * Created by Administrator on 2016/7/28.
 * 支持内部的字View 或者 内容 或者 自身
 * 移动，旋转，缩放的View， 和Android View有所不同
 * 配合PicGestureListener使用
 */
interface TSRView {

    fun scale(centerX: Float, centerY: Float, ratio: Float)

    fun move(dx: Float, dy: Float, isMultiFingers: Boolean)

    /**
     * 直接以View中心为中心旋转时，centerX，centerY可以不用管
     * <p>
     * 关于旋转后触摸点击的计算，就是处理View和触摸点的相对位置关系
     * View绕O旋转x度，相当于点A绕O旋转-x度，之后相当于没有旋转时的情况进行处理即可
     * 注意这里容易直接旋转View的矩形，那样很不好处理的，自己一开始就没想到这个点，浪费了很多时间
     * @param angle 注意是角度制，非弧度制PI
     */
    fun rotate(touchCenterX: Float, toucheCenterY: Float, angle: Float)

    /**
     * 两指距离改变
     * @param isFingerChange 是否是因为手指起落引起的距离改变，比如一个手指变两个，三个手指变两个
     */
    fun twoFingerDisChange(@Nullable P1: PointF, @Nullable P2: PointF, isFingerChange: Boolean)

    /**
     * 如果缩放，要在adjustSize后面调用
     * 在不超过边界情况下设置好位置
     *
     * @param dx 移动距离
     * @param dy 移动距离
     */
    fun adjustEdge(dx: Float, dy: Float)

    fun adjustSize(ratio: Float): Float {
        return ratio
    }

    fun onClick(x: Float, y: Float): Boolean {
        return false
    }

    /**
     * 第一个手指down时，后面的不调用此方法
     */
    fun onFirstFingerDown(x0: Float, y0: Float) {

    }

    fun onLastFingerUp(x: Float, y: Float): Boolean {
        return false
    }

    fun onMultiFingerDown() {

    }
}
