
package com.mandi.intelimeditor.common.util;

import android.content.Context;
import android.widget.Toast;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;


/**
 * Created by LiuGuicen on 2017/1/6 0006.
 * update by caiyonglong on 2019/9/28
 * 统一Toast样式，目前使用默认样式
 */
public class ToastUtils {

    private static Toast toast;

    public static void show(int resId) {
        show(IntelImEditApplication.appContext.getString(resId), Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String info) {
        show(IntelImEditApplication.appContext, info, Toast.LENGTH_SHORT);
    }

    public static void show(Context context, String info, int length) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, info, length);
        toast.show();
    }

    public static void show(String info) {
        show(info, Toast.LENGTH_SHORT);
    }

    public static void show(String info, int length) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(IntelImEditApplication.appContext, info, length);
        toast.show();
    }

    public static void show(Context context, int resId) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT);
        toast.show();
    }
}

