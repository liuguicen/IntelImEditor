/*
 * @(#)Logcat.java	1.00 11/04/20
 *
 * Copyright (c) 2011-2013  New Element Inc.
 * 9/10f, Building 2, Financial Base, No.6 Keyuan Road,
 * Nanshan District, Shenzhen 518057
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * New Element Medical Equipment Technology Development CO., Ltd
 * ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with New Element.
 */

package com.mandi.intelimeditor.common.util;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.common.dataAndLogic.MemoryManager;


/**
 *
 */
public final class LogUtil {
    public static final boolean debugText = false;
    public static final boolean debugRewardAd = false;
    public static final boolean debugPicListFeedAd = false;
    public static final boolean debugPicResource = false;
    public static final boolean debugTxFeedAd = false;
    public static final boolean debugRendPic = false;
    public static final boolean debugTietuEraser = false;
    public static final boolean debugTouchEnlarge = false;
    public static final boolean debugTencentSplashAd = false;
    public static final boolean debugBaoZouFaceFuse = false;
    public static final boolean debugAdjustLevels = false;
    public static final boolean debugAdDStrategy = false;
    public static final boolean debugRecommend = false;
    public static final boolean debugTietuGesture = false;
    public static boolean debugDeformation = false;
    public static final boolean debugFace = false; // 人脸检测 对齐 相关的调试
    public static boolean debugGif = false;
    public static boolean testSplashAd = false;
    public static boolean debugPtuTietuList = false;
    public static boolean debugStyleTransfer = true;

    /**
     * 控制一些调试操作，发版本时记得修改
     */
    public static boolean isTestAll = BuildConfig.DEBUG;

    private static boolean LOG_PRINT_FLAG = true;

    private static String TAG_PREFIX = "LGC";

    public static void setTag(String tag) {
        TAG_PREFIX = tag;
    }

    private LogUtil() {
    }

    /**
     * 打印日志
     *
     * @param msg
     */
    public static void v(String msg) {
        if (isTestAll)
            Log.v("-", getInvokeInfo(2) + " : " + msg);
    }

    /**
     * 只打印函数名和行号
     */
    public static void d() {
        if (isTestAll) {
            d("-", "", 2);
        }
    }

    public static void d(String msg) {
        if (isTestAll) {
            d("-", msg, 2);
        }
    }

    public static void d(Object obj) {
        if (isTestAll) {
            d("-", obj.toString(), 2);
        }
    }

    public static void d(float[] objs) {
        StringBuilder msg = new StringBuilder();

        for (int i = 0; i < objs.length; i++) {
            if (i > 100) {
                msg.append("too long...");
                break;
            } else {
                msg.append(objs[i]).append(",");
            }
        }
        Log.d("-", msg.toString());
    }

    public static void w(Object obj) {
        if (isTestAll)
            w("-", obj.toString(), 2);
    }

    public static void d(int msg) {
        if (isTestAll)
            d("-", msg + "", 1);
    }

    public static void d(long msg) {
        if (isTestAll)
            d("-", msg + "", 2);
    }

    public static void d(float msg) {
        if (isTestAll)
            d("-", msg + "", 2);
    }

    public static void d(double msg) {
        if (isTestAll)
            d("-", msg + "", 2);
    }

    public static void d(boolean msg) {
        if (isTestAll)
            d("-", msg + "", 2);
    }

    public static void d(String TAG, String msg) {
        if (isTestAll) d(TAG, msg, 2);
    }

    public static void d(String TAG, int msg) {
        if (isTestAll) d(TAG, msg + "", 2);
    }

    public static void d(String tag, String msg, boolean isOutput) {
        if (isOutput) {
            Log.d(tag, msg != null ? msg : ""); // 空会崩溃
        }
    }

    public static void e(String tag, String msg, boolean isOutput) {
        if (isOutput) {
            Log.e(tag, msg);
        }
    }

    private static void d(String tag, String msg, int layer) {
        if (isTestAll)
            Log.d(tag, getInvokeInfo(layer + 1) + " : " + msg);
    }

    public static void i(String msg) {
        i("-", msg, 2);
    }

    public static void i(String TAG, String msg) {
        i(TAG, msg, 2);
    }

    private static void i(String TAG, String msg, int layer) {
        Log.i(TAG, getInvokeInfo(layer + 1) + ": " + msg);
    }


    public static void w(String msg) {
        if (isTestAll)
            w("-", msg, 2);
    }

    public static void w(String TAG, String msg, int layer) {
        if (isTestAll)
            Log.w(TAG, getInvokeInfo(layer + 1) + ": " + msg);
    }

    public static void w(String TAG, Throwable tr) {
        if (isTestAll)
            w(TAG, tr.getMessage(), 2);
    }

    public static void e(String msg) {
        Log.e("log util", msg);
    }

    public static void e(String TAG, String msg) {
        Log.e(TAG, msg);
    }


    /**
     * 获取打印信息所在方法名，行号等信息
     *
     * @return
     */
    private static String getInvokeInfo(int layer) {
        String infos = "";
        StackTraceElement[] elements = new Throwable().getStackTrace();
        if (elements.length < layer + 1) {
            Log.e("MyLogger", "Stack is too shallow!!!");
            return infos;
        } else {
            infos += elements[layer].getClassName().substring(
                    elements[layer].getClassName().lastIndexOf(".") + 1);
            infos += "." + elements[layer].getMethodName() + "()";
            //            infos += " at (" + elements[4].getClassName() + ".java:"
            //                    + elements[4].getLineNumber() + ")";
            //            return infos;
        }
        return infos;
    }

    public static long startTime = 0;

    public static void recordTime() {
        startTime = System.currentTimeMillis();
    }

    public static void logTimeConsumeAndRecord(String msg) {
        d("-", msg + "，耗时 = " + (System.currentTimeMillis() - startTime), 3);
        startTime = System.currentTimeMillis();
    }

    public static void logTimeConsume(String msg) {
        d("-", msg + "，耗时 = " + (System.currentTimeMillis() - startTime), 2);
    }

    public static void printMemoryInfo(String tag, Context context) {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(memInfo);

        d(tag, "方法一： \n 总内存  ：" + memInfo.totalMem
                + "\n 可用内存：" + memInfo.availMem
                + "\n 内存阈值：" + memInfo.threshold);
        d(tag, "方法二： \n 最大内存: " + Runtime.getRuntime().maxMemory()
                + " \n总内存  : " + Runtime.getRuntime().totalMemory()
                + " \n可用内存 : " + Runtime.getRuntime().freeMemory() + "\n\n\n");
    }
}