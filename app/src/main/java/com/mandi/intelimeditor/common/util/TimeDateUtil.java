package com.mandi.intelimeditor.common.util;

import android.util.Log;

import com.mathandintell.intelimeditor.BuildConfig;

import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/05
 *      version : 1.0
 * <pre>
 */
public class TimeDateUtil {

    /**
     * 一小时的毫秒数
     */
    public static long HOUR_MILS = 60 * 60 * 1000;
    public static long DAY_MILS = 24 * HOUR_MILS;

    /**
     * @return 获取当前时间相对于一天开始的值
     */
    public static long getTimeInDay() {
        long cur = System.currentTimeMillis();
        return cur - cur % DAY_MILS; // 当前时间，减掉当前时间对一天毫秒数取余的值
    }

    /**
     * 将时间转换成中文日期
     */
    public static String time2ChineseData(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("y年M月d日", Locale.CHINA);
        Date curDate = new Date(time);//获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 将时间转换成英文时间格式
     * y/M/d hh:mm:ss
     */
    public static String time2EnglishFormat(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("y/M/d hh:mm:ss", Locale.CHINA);
        Date curDate = new Date(time);//获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 获取当前日期，y/M/d形式
     */
    public static String getTodayData() {
        SimpleDateFormat formatter = new SimpleDateFormat("y/M/d", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        return formatter.format(curDate);
    }

    /**
     * 不能在UI线程调用
     *
     * @return 获取网络标准时间，用于需要准确时间，防止本地时间不准确或者被修改的情况
     * 如果网络获取失败，使用本地时间
     */
    public static long getNetworkStandardTime() {
        long time = -1, start = System.currentTimeMillis();
        URL url = null;//取得资源对象
        try {
            url = new URL("http://www.taobao.com");
            URLConnection uc = url.openConnection();//生成连接对象
            uc.connect(); //发出连接
            time = uc.getDate(); //取得网站日期时间
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (BuildConfig.DEBUG) {
            Log.d("getNetwordTime: ", "获取到的网络时间为 " + time + "手机时间为 " + System.currentTimeMillis());
            Log.d("getNetwordTime: 耗时 ", (System.currentTimeMillis() - start) + "ms");
        }
        if (time <= 0) {
            time = System.currentTimeMillis();
        }
        return time;
    }
}
