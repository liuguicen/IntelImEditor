package com.mandi.intelimeditor.common.appInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.mandi.intelimeditor.ad.AdStrategyUtil;
import com.mandi.intelimeditor.user.userSetting.SPConstants;
import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.R;


/**
 * Created by liuguicen on 2016/8/13.
 * <p> 本App的一些信息，特别升级的时候挺多的信息要记录，因为你不知道是从哪个版本升级过来的
 * <p> app版本，数据库版本,..
 * <p>
 * <p><p>
 * //各个历史版本，别删
 * <p>public final static float APP_VERSION_1 = 1.0f;
 * <p>public final static float APP_VERSION_2 = 1.1f;
 * <p>public final static int DATABASE_VERSION_2 = 2;
 * <p>public final static int DATABASE_VERSION_3=3;
 */
public class AppConfig {

    /**
     * 腾讯应用宝的ID
     */
    public static final String ID_IN_YINGYONGBAO = "1105572903";
    public static final String ID_IN_WEIXIN = "wxcc5a4597cfb83496";
    public static final String ALIPAY_ID = "2019081866286486";

    public static final String QQ_GROUP_COMMUNICATE_KEY = "JFJ4O1BEnyk6gcDdtBv05vAsgu3gA04r";

    public static final String QQ_GROUP_FEEDBACK_KEY = "jiXCpPWsr-A7RA5d5rYDeZlrYCSUtuP_";

    // 资源解锁数据相关的版本
    public static final int CUR_LOCK_VERSION = 2;


    public static boolean isCloseVipFunction = true; // 不打开VIP功能
    /**
     * 一些应用市场由于特殊情况，暂时关闭广告，比如华为检查直接下载类的广告，由于目前腾讯广告包含此类，且不能关闭
     * 只能手动避免
     */
    public static boolean isCloseTencentAd = false;
    public static boolean isCloseTTAd = false;

    //各个历史版本，别删
    private static PackageInfo pi;

    static {
        PackageManager pm = IntelImEditApplication.appContext.getPackageManager();
        try {
            pi = pm.getPackageInfo(IntelImEditApplication.appContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public final static String CUR_VERSION_NAME = pi.versionName;
    public final static int DATABASE_VERSION_2 = 2;
    public final static int DATABASE_VERSION_3 = 3;
    public final static int DATABASE_VERSION_4 = 4;
    public final static int CUR_DATABASE_VERSION = DATABASE_VERSION_4;
    public static final String CHANNEL_YINGYONGBAO = "yingyongbao";
    public static final String CHANNEL_HUAWEI = "huawei";
    public static final String CHANNEL_XIAOMI = "xiaomi";
    public static final String CHANNEL_OPPO = "oppo";
    public static final String CHANNEL_VIVO = "vivo";
    public static final String CHANNEL__360 = "_360";
    public static final String CHANNEL_BAIDU = "baidu";

    private SharedPreferences sp;

    /**
     * 开屏广告优先使用的服务商 腾讯or头条
     * {@link AdStrategyUtil}
     */
    public String splash_ad_strategy = AdStrategyUtil.DEFAULT_SPLASH_AD_STRATEGY; // 默认情况

    /**
     * 图片资源列表下广告显示策略
     * 默认优量汇、穿山甲都显示
     * APP启动后，后台通过网络更新，有可能更新时已经显示默认策略，概率很低，可忽略
     * {@link AdStrategyUtil}
     */
    public String pic_res_ad_strategy = AdStrategyUtil.DEFAULT_PIC_RES_LIST_STRATEGY;
    public String ptu_result_ad_strategy = AdStrategyUtil.DEFAULT_PTU_RESULT_AD_STRATEGY;

    /**
     * {@link AdStrategyUtil}
     */
    public String reward_vad_strategy = AdStrategyUtil.DEFAULT_REWARD_VAD_STRATEGY;

    public static int getDatabaseVersion() {
        return CUR_DATABASE_VERSION;
    }

    public static int getAppversion() {
        return BuildConfig.VERSION_CODE;
    }

    public AppConfig(Context appContext) {
        sp = appContext.getSharedPreferences(SPConstants.APP_CONFIG, Context.MODE_PRIVATE);
        splash_ad_strategy = getLocalFirstSplashAd(AdStrategyUtil.DEFAULT_SPLASH_AD_STRATEGY);
    }

    public int readAppVersion() {
        return sp.getInt(SPConstants.APP_VERSION, -1);
    }

    public void writeCurAppVersion() {
        sp.edit().putInt("app_version", BuildConfig.VERSION_CODE).apply();
    }

    public long readConfig_LastUseData() {
        return sp.getLong("last_used_date", 0);
    }

    public void writeConfig_LastUsedData(long data) {
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putLong("last_used_date", data);
        spEditor.apply();
    }


    /**
     * 开屏广告广告商选择
     */
    public void putLocalFirstSplashAd(String strategy) {
        sp.edit().putString(SPConstants.SPLASH_AD_STRATEGY, strategy).apply();
    }

    public String getLocalFirstSplashAd(String default_strategy) {
        return sp.getString(SPConstants.SPLASH_AD_STRATEGY, default_strategy);
    }

    public void clearOldVersionInfo_1_0() {
        SharedPreferences.Editor spEditor = sp.edit();
        //移除1.0版本的ptu上的配置信息，当时模块划分不清晰，也没考虑到模块会变大，变大之后这里变得复杂难写了
        spEditor.remove("text_rubber");
        spEditor.remove("go_send");
        spEditor.remove("usu_pic_use");
        spEditor.remove("isNewInstall");
        if (!spEditor.commit()) {
            Log.e(IntelImEditApplication.appContext.getResources().getText(R.string.app_name).toString(), "移除1.0版本Config信息失败");
        }
    }

    public boolean isVersion1_0() {
        return sp.contains("isNewInstall");
    }

    public void writeSendDeviceInfo(boolean isSend) {
        SharedPreferences.Editor spEditor = sp.edit();
        //移除1.0版本的ptu上的配置信息，当时模块划分不清晰，也没考虑到模块会变大，变大之后这里变得复杂难写了
        spEditor.putBoolean("has_send_device", isSend).apply();
    }

    public boolean hasSendDeviceInfos() {
        return sp.getBoolean("has_send_device", false);
    }

    public void putOptionPermissionCount(int count) {
        SharedPreferences.Editor spEditor = sp.edit();
        //移除1.0版本的ptu上的配置信息，当时模块划分不清晰，也没考虑到模块会变大，变大之后这里变得复杂难写了
        spEditor.putInt(SPConstants.OPTION_PERMISSION_READ_COUNT, count).apply();
    }

    public int getOptionalPermissionCount() {
        return sp.getInt(SPConstants.OPTION_PERMISSION_READ_COUNT, 0);
    }

}
