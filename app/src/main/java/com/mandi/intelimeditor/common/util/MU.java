package com.mandi.intelimeditor.common.util;

import java.math.BigDecimal;

/**
 * 高精度运算
 */
public class MU {
    //默认除法运算精度

    private static final int DEFAULT_DIV_SCALE = 15;


    /**
     * 提供精确的加法运算。
     *
     * @return 两个参数的和
     */

    public static double add(double v1, double v2)

    {

        BigDecimal b1 = new BigDecimal(Double.toString(v1));

        BigDecimal b2 = new BigDecimal(Double.toString(v2));

        return b1.add(b2).doubleValue();

    }

    /**
     * 提供精确的加法运算
     *
     * @return 两个参数数学加和，以字符串格式返回
     */
    public static String add(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).toString();
    }

    public static String add(float v1, float v2) {
        return add(Float.toString(v1), Float.toString(v2));
    }

    public static String add(float v1, String v2) {
        return add(Float.toString(v1), v2);
    }

    /**
     * 提供精确的减法运算。
     *
     * @return 两个参数的差
     */
    public static double su(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算
     *
     * @return 两个参数数学差，以字符串格式返回
     */
    public static String su(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).toString();
    }

    public static String su(float v1, float v2) {
        return su(Float.toString(v1), Float.toString(v2));
    }

    public static String su(float v1, String v2) {
        return su(Float.toString(v1), v2);
    }

    public static int co(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.compareTo(b2);
    }

    public static int co(float v1, float v2) {
        return co(Float.toString(v1), Float.toString(v2));
    }

    public static int co(String v1, float v2) {
        return co(v1, Float.toString(v2));
    }

    public static int co(float v1, String v2) {
        return co(Float.toString(v1), v2);
    }


    /**
     * 提供精确的乘法运算
     *
     * @return 两个参数的数学积，以字符串格式返回
     */
    public static String mu(String v1, String v2) {
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2).toString();
    }

    public static String mu(float v1, float v2) {
        return mu(Float.toString(v1), Float.toString(v2));
    }

    public static String mu(String v1, float v2) {
        return mu(v1, Float.toString(v2));
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * <p/>
     * 小数点以后10位，以后的数字四舍五入,舍入模式采用
     * 常数ROUND_HALF_EVEN
     *
     * @return 两个参数的商
     */
    public static String di(double v1, double v2) {
        return di(Double.toString(v1), Double.toString(v2), DEFAULT_DIV_SCALE);
    }

    public static String di(String v1, double v2) {
        return di(v1, Double.toString(v2));
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * <p/>
     * 定精度，以后的数字四舍五入。舍入模式采用ROUND_HALF_EVEN
     *
     * @param scale 表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double di(double v1, double v2, int scale) {
        return di(v1, v2, scale, BigDecimal.ROUND_HALF_EVEN);
    }

    public static String di(float v1, float v2) {
        return di(Float.toString(v1), Float.toString(v2));
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * <p/>
     * 定精度，以后的数字四舍五入。舍入模式采用用户指定舍入模式
     *
     * @param scale      表示需要精确到小数点以后几位
     * @param round_mode 表示用户指定的舍入模式
     * @return 两个参数的商
     */

    public static double di(double v1, double v2, int scale, int round_mode) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, round_mode).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * <p/>
     * 小数点以后10位，以后的数字四舍五入,舍入模式采用ROUND_HALF_EVEN
     *
     * @return 两个参数的商，以字符串格式返回
     */

    public static String di(String v1, String v2) {
        return di(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * <p/>
     * 定精度，以后的数字四舍五入。舍入模式采用ROUND_HALF_EVEN
     *
     * @param scale 表示需要精确到小数点以后几位
     * @return 两个参数的商，以字符串格式返回
     */
    public static String di(String v1, String v2, int scale) {
        return di(v1, v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_EVEN);
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * <p/>
     * 定精度，以后的数字四舍五入。舍入模式采用用户指定舍入模式
     *
     * @param scale      表示需要精确到小数点以后几位
     * @param round_mode 表示用户指定的舍入模式
     * @return 两个参数的商，以字符串格式返回
     */

    public static String di(String v1, String v2, int scale, int round_mode) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.divide(b2, scale, round_mode).toString();
    }

}




