package com.mandi.intelimeditor.common.util;

import android.content.Context;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/03/07
 *      version : 1.0
 * <pre>
 */
public class UIUtil {

    /**
     * 是否含有按键，虚拟或者物理的
     * @param mContext
     */
    public static boolean hasAnyKeys(Context mContext) {
        // TODO: 2019/3/7
        // android 判断手机是否有物理menu键
       // ViewConfiguration.get(getContext()).hasPermanentMenuKey();
        //然后判断虚拟按键
        return true;
    }
}
