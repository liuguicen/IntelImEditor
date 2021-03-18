package com.mandi.intelimeditor.common.util;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

import java.util.Map;

public class StatisticsUtils {
    public static void onEven(Context context, String eventName, String value) {
        MobclickAgent.onEvent(context, eventName, value);
    }

    public static void onEven(Context context, String eventName) {
        MobclickAgent.onEvent(context, eventName);
    }


    public static void onEven(Context context, String eventName, Map<String, String> map) {
        MobclickAgent.onEvent(context, eventName, map);
    }

    public static void onEven(Context context, String eventName, Map<String, String> map, int value) {
        MobclickAgent.onEventValue(context, eventName, map, value);
    }
}
