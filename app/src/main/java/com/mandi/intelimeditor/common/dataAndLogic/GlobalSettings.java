package com.mandi.intelimeditor.common.dataAndLogic;

import android.content.Context;
import android.content.SharedPreferences;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.device.YearClass;
import com.mandi.intelimeditor.user.userSetting.SPConstants;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/08/16
 *      version : 1.0
 *      全局设置，包含应用的全局设置的信息
 * <pre>
 */
public class GlobalSettings {
    private final SharedPreferences sp;
    /**
     * 设备性能分级
     */
    public int performanceYear;
    public int maxSupportBmSize;
    /**
     * 难以保证各个机型的适配，通过实际使用不爆内存来设置
     */
    public int contentMaxSupportBmSize;
    public float styleContentRatio = 1.5f;
    public int maxSupportGifBmSize;

    public GlobalSettings() {
        sp = IntelImEditApplication.appContext.getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE);
        performanceYear = sp.getInt(SPConstants.AppSettings.PERFORMANCE_YEAR_CLASS, SPConstants.AppSettings.PERFORMANCE_YEAR_CLASS_UNREAD);
        updateOther(performanceYear);
    }

    private void updateOther(int performanceYear) {
        contentMaxSupportBmSize = 800 * 800;
        if (performanceYear >= YearClass.PERFORMANCE_8G_UP) {
            maxSupportBmSize = 6000 * 4000;
        } else if (performanceYear >= YearClass.PERFORMANCE_6G_8G) {
            maxSupportBmSize = 5000 * 4000;
            contentMaxSupportBmSize *= 16f / 25;
        } else if (performanceYear >= YearClass.PERFORMANCE_4G_6G) {
            maxSupportBmSize = 4000 * 3000;
            contentMaxSupportBmSize *= 9f / 25;
        } else if (performanceYear >= YearClass.PERFORMANCE_2G_4G) {
            maxSupportBmSize = 3000 * 2000;
            contentMaxSupportBmSize *= 4f / 25;
        } else {
            maxSupportBmSize = 2000 * 1000;
            contentMaxSupportBmSize *= 4f / 25;
        }
        maxSupportGifBmSize = maxSupportBmSize / 16;
        // 难以保证各个机型的适配，通过实际使用不爆内存来设置
        SPUtil.putContentMaxSupportBmSize(-1);
        int experimental_size = SPUtil.getStyleMaxSupportBmSize();
        contentMaxSupportBmSize = experimental_size <= 0 ? contentMaxSupportBmSize : experimental_size;
    }

    public void saveSendShortCutNotify(boolean isSend) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SPConstants.AppSettings.SEND_SHORTCUT_NOTIFY, isSend);
        editor.apply();
    }

    public void save_ifNotifyWhenExit(boolean isSend) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SPConstants.AppSettings.SEND_SHORTCUT_NOTIFY_EXIT, isSend);
        editor.apply();
    }

    public void saveSharedWithout(boolean isOut) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SPConstants.AppSettings.SHARED_WHTHOUT_LABEL, isOut);
        editor.apply();
    }

    /**
     * @return 默认是要带 false
     */
    public boolean getSharedWithout() {
        return sp.getBoolean(SPConstants.AppSettings.SHARED_WHTHOUT_LABEL, false);//默认是要带 false
    }

    public boolean getSendShortcutNotify() {
        return sp.getBoolean(SPConstants.AppSettings.SEND_SHORTCUT_NOTIFY, true);
    }

    public boolean getSendShortcutNotifyExit() {
        return sp.getBoolean(SPConstants.AppSettings.SEND_SHORTCUT_NOTIFY_EXIT, true);
    }

    /**
     * 需要存储权限，授权之后才能执行此方法
     * 只有应用第一次安装或重装或者数据被清除，才会去这个读取数据，否则使用历史数据
     */
    public void readDeviceInfo() {
        if (performanceYear == SPConstants.AppSettings.PERFORMANCE_YEAR_CLASS_UNREAD) {
            performanceYear = YearClass.get(IntelImEditApplication.appContext);
            if (performanceYear == YearClass.CLASS_UNKNOWN) { // 未知年份设置成 2g-4g
                performanceYear = YearClass.PERFORMANCE_2G_4G;
            }
            sp.edit().putInt(SPConstants.AppSettings.PERFORMANCE_YEAR_CLASS, performanceYear).apply();
            updateOther(performanceYear);
        }
    }

    public boolean isShowShortVideo() {
//        return performanceYear >= YearClass.PERFORMANCE_2G_4G;
        return false;
    }
}
