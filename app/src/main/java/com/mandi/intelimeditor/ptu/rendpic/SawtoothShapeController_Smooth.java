package com.mandi.intelimeditor.ptu.rendpic;

import android.util.Log;


import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.geoutil.GeoUtil;
import com.mandi.intelimeditor.common.util.geoutil.MPoint;

import java.util.Random;

/**
 * 这个锯齿是比较平滑的，比较仿真的像碎鸡蛋边缘的
 * <p>
 * 生成的比较自然的路径，类似于什么东西被撕开的样子，鸡蛋壳裂痕的边缘比较像，模仿这个，纸被撕开锯齿效果太差，不合适
 * android 有类似的api，paint.setPathEffect(new DiscretePathEffect(...)),这个效果实际上很像，
 * 但是有一些问题：不能预览，效果是随机的，预览效果不能生成到最后撕出的图中，
 * 还有撕完图最后抠图的时候它又作用于整个路径，会把图片四周锯齿化，但是目前没找到（没用足够时间找）处理方法
 * <p>
 * 根据鸡蛋壳裂缝或者DiscretePathEffect的效果，锯齿是几个锯齿偏向一边，然后接着几个又偏向于另一边，
 * 不能用random.nextDouble()类似的算法，然后x，y是分别随机的， 这样做出来的效果
 * 很不自然，学了一次概率论的东西，然后仔细研究了怎么样的形状才自然，结合起来，发现问题应该是
 * 一、宽高的分布不是均匀分布，而是其它的，正态？或者其变种
 * 二、dx，dy的分布不是独立的，分开随机有问题的，尤其dx很小，dy很大，最后形成一个很尖的尖牙，这个实际情况中不太可能发生的，所以不太自然
 * <p>
 * 所以，知识，数学，的应用啊。
 * <p>
 * 配置锯齿的宽高等，控制撕图形成的锯齿边缘的形状
 * 并获取宽高的随机值
 * <p>小经验，一开始这个类是没有的，后面写着写着抽象出了这么一个对象，
 * 生成锯齿过程是一个偏算法性质的东西，写起代码来是很面向过程的，从这里得出经验，即使是过程性很强的算法代码，
 * 也从中抽象出合适的对象的，比如部分过程代码
 * <p>
 * 锯齿边缘的形状大概是：
 * 在一段儿范围（称大锯齿）中的小锯齿的高即y总体向上，然后有总体向下，
 * 在总体向上的过程中，向上的增幅大，数量稍多，向下的反之
 * 大锯齿包含小锯齿的数量，均值为u， 方差为d正态分布
 */
public class SawtoothShapeController_Smooth extends SawtoothShapeController {

    // 锯齿形状控制参数
    public double subPicMinWidth = SawtoothShapeController.DEFAULT_SUB_PIC_MIN_WIDTH; // 撕开的子图最小宽度，小于这个值就不撕开
    public static final int[] SMALL_SAW_NUMBER_LIST = new int[]{60}; // 控制宽度范围，也即小锯齿数量
    public static final int[] BIG_SAW_NUMBER_LIST = new int[]{20}; // 控制大锯齿数量
    public static final double[] SAW_H_RATIO_LIST = new double[]{0.4}; // 控制高度相对于宽度的比例范围
    public static final double HIGT_VARIANCE = 18; // 增加的高度dh的方差，即高度变化的剧烈程度
    // dh均值，根据正态分布的3d法则，控制dh正负比例
    // 也即控制大锯齿上小锯齿向上和向下的比例, 配合highSign就控制与大锯齿同向增长的比例
    private static final double HIGT_EVEN = Math.sqrt(HIGT_VARIANCE) * 0.3;
    // 反向小锯齿高度缩小比例
    private static final double REVERSER_RAW_HIGHT_RATIO = 0.25;
    public static final double WIDTH_VARIANCE = 2; // 宽度的方差

    private final int totalSmallRawNumber;
    private final int bigRawNumber;
    /**
     * 锯齿高度对宽度的比例
     */
    public double highRatio;


    /**
     * 锯齿平均宽度
     */
    public double smallRawWidthEven;


    private Random random;
    public static int sStyleID = 0;
    private int highSign;
    private double mLastHeight;


    private int numberOfSmallRawInBigRaw;
    private int numberOfSmallRawInBigRaw_even;

    private SawtoothShapeController_Smooth(double lineLong, int smallRawNumber, int bigSawNumber,
                                           double highRatio, double subPicMinWidth) {
        this.totalSmallRawNumber = smallRawNumber;
        this.bigRawNumber = bigSawNumber;
        this.highRatio = highRatio;
        this.subPicMinWidth = subPicMinWidth;

        random = new Random(System.currentTimeMillis());
        highSign = random.nextBoolean() ? 1 : 0;
        smallRawWidthEven = SMALL_SAW_NUMBER_LIST[sStyleID] != 0 ? lineLong / SMALL_SAW_NUMBER_LIST[sStyleID] : lineLong;
        numberOfSmallRawInBigRaw_even = smallRawNumber / bigSawNumber;
        numberOfSmallRawInBigRaw = nextSmallSawNumber(true);
        if (LogUtil.debugRendPic) {
            Log.d("---",
                    "\n撕图直线总长" + lineLong +
                            "\n小锯齿总量: " + smallRawNumber +
                            "\n大锯齿数量" + bigSawNumber +
                            "\n小锯齿平均宽度 " + smallRawWidthEven +
                            "\n大锯齿上小锯齿平均数量" + numberOfSmallRawInBigRaw_even +
                            "\n第一个大锯齿上小锯齿数量" + numberOfSmallRawInBigRaw +
                            "\n高度比例" + highRatio
            );
            Log.d("---", highSign > 0 ? "高度开始增加" : "高度开始降低");
        }
    }

    private int nextSmallSawNumber(boolean isFirstBigRaw) {
        int numberOfSmallRawInBigRaw = (int) nextGaussian(numberOfSmallRawInBigRaw_even, (numberOfSmallRawInBigRaw_even - 1) / 3.2);
        if (numberOfSmallRawInBigRaw <= 1)
            numberOfSmallRawInBigRaw = 1; // 正态分布的3d法则，<=1 的会很小，变成1的也会很少，不影响;
        if (!isFirstBigRaw) { // 第一次之外的大锯齿要折返两倍才对，经验，搞算法，某个基本的或者方向性的，大的地方错了，在细节上，比如参数上，怎么搞都达不到效果，要注意这个
            numberOfSmallRawInBigRaw *= 2;
        }
        if (LogUtil.debugRendPic) {
            Log.d(" --- ", "nextSmallSawNumber:  = " + numberOfSmallRawInBigRaw);
        }
        return numberOfSmallRawInBigRaw;
    }

    /**
     * 锯齿沿着连线方向上的宽度
     */
    public double next_aloneLine_Width() {
        return Math.abs(nextGaussian(0, WIDTH_VARIANCE)) * smallRawWidthEven;
    }

    /**
     * @param width 锯齿的宽高不是相互独立的，高应该基于宽进行随机
     */
    public double next_Vertical_height(double width) {
        double dh; // 相对增加的高度
        // 让dh与大锯齿同向（即与highSign同号）的数量多些
        if (highSign > 0) { // 如果
            dh = width * highRatio * nextGaussian(HIGT_EVEN, HIGT_VARIANCE);
        } else {
            dh = width * highRatio * nextGaussian(-HIGT_EVEN, HIGT_VARIANCE);
        }
        // 让dh与大锯齿不同向的增幅小些，即同向的数量多些
        if (dh * highSign < 0) {
            dh *= REVERSER_RAW_HIGHT_RATIO;
            if (Math.abs(dh) > HIGT_VARIANCE) {
                dh *= REVERSER_RAW_HIGHT_RATIO;
            }
        }
        numberOfSmallRawInBigRaw--; // 大锯齿上面的小锯齿数量减一，减到0之后，大锯齿边缘向反方向移动
        if (numberOfSmallRawInBigRaw == 0) {
            numberOfSmallRawInBigRaw = nextSmallSawNumber(false);
            // 出现一个问题，锯齿一直像一个方向偏，不好处理，只能硬处理，highSign不是一正一负，而是根据上一次的高的正负来设置
            if (mLastHeight > 0) {
                highSign = -1;
            } else {
                highSign = 1;
            }
            if (LogUtil.debugRendPic) {
                if (highSign > 0) {
                    Log.d("next_Vertical_height", "高度开始增加");
                } else {
                    Log.d("next_Vertical_height", "高度开始降低");
                }
            }
        }
        mLastHeight += dh;
        if (LogUtil.debugRendPic) {
            Log.d("next_Vertical_height", "height =  " + mLastHeight);
        }
        return mLastHeight;
    }

    /**
     * 可以生成不同风格的锯齿
     * 就是锯齿的宽和高进行相对的变化，比如宽度大，高度小，或者相反
     */
    public static SawtoothShapeController_Smooth nextStyleSawtooth(MPoint start, MPoint end, double subPicMinWidth) {
        if (LogUtil.debugRendPic) {
            Log.d("--", "nextStyleSawtooth:开始撕图");
        }
        float realLong = GeoUtil.getDis(start, end);

        int smallRawNumber = SMALL_SAW_NUMBER_LIST[sStyleID];
        int bigRawNumber = BIG_SAW_NUMBER_LIST[sStyleID];
        double highRatio = SAW_H_RATIO_LIST[sStyleID];

        sStyleID = (sStyleID + 1) % SMALL_SAW_NUMBER_LIST.length;
        return new SawtoothShapeController_Smooth(realLong, smallRawNumber, bigRawNumber, highRatio, subPicMinWidth);
    }


    /**
     * 均值和方差
     */
    public double nextGaussian(double u, double d) {
        return random.nextGaussian() * Math.sqrt(d) + u;
    }

}