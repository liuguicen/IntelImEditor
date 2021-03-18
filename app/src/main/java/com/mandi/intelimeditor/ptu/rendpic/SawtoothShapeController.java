package com.mandi.intelimeditor.ptu.rendpic;

import com.mandi.intelimeditor.common.util.geoutil.MPoint;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/11/06
 *      version : 1.0
 * <pre>
 */
public abstract class SawtoothShapeController {
    public static final double DEFAULT_SUB_PIC_MIN_WIDTH = 20;
    public static final int STYLE_NUMBER = 3;
    static int cur_style = 0;

    abstract double next_aloneLine_Width();

    abstract double next_Vertical_height(double sawWidth);

    public static SawtoothShapeController nextStyleSawtooth(MPoint start, MPoint end, double subPicMinWidth) {
        if (cur_style < 2) {
            cur_style = (cur_style + 1) % 3;
            return SawtoothShapeController_Steep.nextStyleSawtooth(start, end, subPicMinWidth);
        } else {
            cur_style = (cur_style + 1) % 3;
            return nextStyleSawtooth(start, end, subPicMinWidth);
        }
    }
}
