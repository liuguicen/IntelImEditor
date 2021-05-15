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
     * 可用内存波动很大，自己测试接近至少5倍波动
     * android无法获取机型实际可用内存不准确，然后模型实际消耗内存也不好算（单纯模型可算，但是放到移动端，框架有优化，不了解算不了了），
     * 通过实际使用不爆内存来设置
     */
    public int maxSupportContentSize;
    public float styleContentRatio = 1.5f;
    public int maxSupportGifBmSize;

    public GlobalSettings() {
        sp = IntelImEditApplication.appContext.getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE);
        performanceYear = sp.getInt(SPConstants.AppSettings.PERFORMANCE_YEAR_CLASS, SPConstants.AppSettings.PERFORMANCE_YEAR_CLASS_UNREAD);
        updateOther(performanceYear);
    }

    private void updateOther(int performanceYear) {
        maxSupportContentSize = 2800 * 3800;
        if (performanceYear >= YearClass.PERFORMANCE_8G_UP) {
            maxSupportBmSize = 6000 * 4000;
        } else if (performanceYear >= YearClass.PERFORMANCE_6G_8G) {
            maxSupportBmSize = 5000 * 4000;
            maxSupportContentSize *= 16f / 25;
        } else if (performanceYear >= YearClass.PERFORMANCE_4G_6G) {
            maxSupportBmSize = 4000 * 3000;
            maxSupportContentSize *= 9f / 25;
        } else if (performanceYear >= YearClass.PERFORMANCE_2G_4G) {
            maxSupportBmSize = 3000 * 2000;
            maxSupportContentSize *= 4f / 25;
        } else {
            maxSupportBmSize = 2000 * 1000;
            maxSupportContentSize *= 4f / 25;
        }
        maxSupportGifBmSize = maxSupportBmSize / 16;
        SPUtil.putContentMaxSupportBmSize(-1);
        int last_content_support_size = SPUtil.getContentMaxSupportBmSize();
        if (last_content_support_size > 0) {
            last_content_support_size *= 1.05; // 因为目前的最大尺寸获取方法是保证不爆内存下最小尺寸，所以尺寸会一直边笑，这里主动放大一次
            SPUtil.putContentMaxSupportBmSize(last_content_support_size);
        }
        maxSupportContentSize = last_content_support_size <= 0 ? maxSupportContentSize : last_content_support_size;
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
