package com.mandi.intelimeditor.ptu.rendpic;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;

import java.util.Random;

/**
 * 配置锯齿的宽高方位，
 * 并获取宽高的随机值
 * <p>小经验，一开始这个类是没有的，后面写着写着抽象出了这么一个对象，
 * 生成锯齿过程是一个偏算法性质的东西，写起代码来是很面向过程的，从这里得出经验，即使是过程性很强的算法代码，
 * 也从中抽象出合适的对象的，比如部分过程代码
 */
public class SawtoothShapeController_Steep extends SawtoothShapeController {
    public final double DEFAULT_MIN_WIDTH_RATIO = 1.0f / 6;
    public final double DEFAULT_MIN_HEIGHT_RATIO = 0.4;
    /**
     * 锯齿的最大宽度
     */
    public double maxWidth;
    /**
     * 锯齿最大高度，
     */
    public double maxHeight;

    public double widthRange;

    public double minWidth;
    private Random random;

    public static double DEFAULT_SUB_PIC_MIN_WIDTH = 20;
    /**
     * 撕开的子图最小宽度，小于这个值就不撕开
     */
    public double subPicMinWidth = DEFAULT_SUB_PIC_MIN_WIDTH;
    // 用于形成不同风格的锯齿，即长宽不同, 低中高，三种，不要太多，用户数不过来，尝试法数据，没有具体依据
    public static final int[] SAW_NUMBER_LIST = new int[]{0, 3};
    public static final float[] SAW_H_RATIO_LIST = new float[]{0, 0.5f};
    public static int sStyleID = 0;
    private int highSign;
    private double minHRatio = DEFAULT_MIN_HEIGHT_RATIO;

    private SawtoothShapeController_Steep(double maxW, double maxH, double subPicMinWidth) {
        init(maxW,maxW * DEFAULT_MIN_WIDTH_RATIO,  maxH, subPicMinWidth);
    }

    private void init(double maxWidth, double minWidth, double maxHeight, double subPicMinWidth) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.minWidth = minWidth;
        widthRange = maxWidth - minWidth;
        random = new Random(System.currentTimeMillis());
        this.subPicMinWidth = subPicMinWidth;
        highSign = 1;
        minHRatio = 0.2;
    }

    /**
     * 锯齿沿着连线方向上的宽度在x,y轴上波动范围, 最小宽度到最大宽度之间
     */
    public double next_aloneLine_Width() {
        // * 增大锯齿的不均匀性
        return minWidth + random.nextDouble() * widthRange * (random.nextBoolean() ? 1f/2 : 4 / 3);
    }

    /**
     * 计算方法，类似于三条斜线\\\, 算出从中间的线垂直的加减的高度
     */
    public double next_Vertical_height(double sawWidth) {
        // 最大高度 * （随机比例, 至少minHRatio，为0不好看）* 算出高，然后改变符号
        return maxHeight * (minHRatio + (random.nextDouble() * (1 - minHRatio))) * (highSign *= -1);
    }

    /**
     * 可以生成不同风格的锯齿
     * 就是锯齿的宽和高进行相对的变化，比如宽度大，高度小，或者相反
     */
    public static SawtoothShapeController_Steep nextStyleSawtooth(MPoint start, MPoint end, double subPicMinWidth) {
        float realLong = GeoUtil.getDis(start, end);
        float w = realLong / SAW_NUMBER_LIST[sStyleID];
        float h = w * SAW_H_RATIO_LIST[sStyleID];
        LogUtil.d("锯齿数量 = " + SAW_NUMBER_LIST[sStyleID] +  +w + "  +  " + h);
        sStyleID = (sStyleID + 1) % SAW_NUMBER_LIST.length;
        return new SawtoothShapeController_Steep(w, h, subPicMinWidth);
    }

}