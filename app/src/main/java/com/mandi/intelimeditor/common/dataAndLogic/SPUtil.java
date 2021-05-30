package com.mandi.intelimeditor.common.dataAndLogic;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.user.userSetting.SPConstants;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/02/15
 *      version : 1.0
 *      有个工具MMKV，专门替代sp的，适用于大量高并发的key-value读写操作，有兴趣可以看看
 *      这里目前不需要
 * <pre>
 */
public class SPUtil {

    private static SharedPreferences userSp =
            AllData.appContext.getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE);

    public static long getLastTietuCategoryQueryTime() {
        SharedPreferences sp = AllData.appContext.getSharedPreferences(SPConstants.DATA_SYNCH, Context.MODE_PRIVATE);
        return sp.getLong(SPConstants.LATEST_TIETU_CATEGORY_QUERY_TIME, 0);
    }

    public static void putLastTietuCategoryQueryTime(Long time) {
        SharedPreferences sp = AllData.appContext.getSharedPreferences(SPConstants.DATA_SYNCH, Context.MODE_PRIVATE);

        sp.edit().putLong(SPConstants.LATEST_TIETU_CATEGORY_QUERY_TIME, time).apply();

    }

    public static long getQueryTimeOfTietuWithCategory(String category) {
        String key = SPConstants.QUERY_TIME_OF_TIETU_WITH_CATEGORY + category;
        SharedPreferences sp = AllData.appContext.getSharedPreferences(SPConstants.DATA_SYNCH, Context.MODE_PRIVATE);
        return sp.getLong(key, 0);
    }

    public static void putQueryTimeOfTietuWithCategory(long time, String category) {
        String key = SPConstants.QUERY_TIME_OF_TIETU_WITH_CATEGORY + category;
        SharedPreferences sp = AllData.appContext.getSharedPreferences(SPConstants.DATA_SYNCH, Context.MODE_PRIVATE);
        sp.edit().putLong(key, time).apply();
    }

    /**************************************用户信息相关***************************************/

    public static void putUserInfo(@NonNull String token, @NonNull long expires_in) {
        SharedPreferences.Editor editor = userSp.edit();
        editor.putString(SPConstants.UserLogin.USER_TOKEN, token);
        editor.putLong(SPConstants.UserLogin.USER_EXPIRE, expires_in);
        editor.apply();
    }

    /**
     * @param openId 用户唯一保存标识符，十分重要
     */
    public static void putUserId(String openId) {
        userSp.edit().putString(SPConstants.UserLogin.USER_ID, openId).apply();
    }

    public static String getUserId() {
        return userSp.getString(SPConstants.UserLogin.USER_ID, "");
    }

    public static void putUserName(String name) {
        userSp.edit().putString(SPConstants.UserLogin.USER_NAME, name).apply();
    }

    public static String getUserName() {
        return userSp.getString(SPConstants.UserLogin.USER_NAME, "");
    }

    public static void putUserLoginMay(String loginWay) {
        userSp.edit().putString(SPConstants.UserLogin.USER_LOGIN_WAY, loginWay).apply();
    }

    public static String getAllSearchHistory() {
        return userSp.getString(SPConstants.SEARCH_HISTORY, "");
    }

    public static void putSearchHistory(String queryInfo) {
        userSp.edit().putString(SPConstants.SEARCH_HISTORY, queryInfo).apply();
    }

    /**
     * 注意一定要清除所有的，将{@link SPConstants.UserLogin}
     * 里面的所有属性用矩形复制复制过来
     * 然后后面还要删除头像文件
     */
    public static void clearAllUserLoginInfos() {
        SharedPreferences.Editor editor = userSp.edit();
        editor.remove(SPConstants.UserLogin.USER_ID);
        editor.remove(SPConstants.UserLogin.USER_NAME);
        editor.remove(SPConstants.UserLogin.USER_LOGIN_WAY);
        editor.remove(SPConstants.UserLogin.USER_TOKEN);
        editor.remove(SPConstants.UserLogin.USER_EXPIRE);
        editor.remove(SPConstants.UserLogin.USER_VIP_EXPIRE);
        editor.apply();
    }

    /**
     * 用户VIP到期时间
     */
    public static long getUserVipExpire() {
        return userSp.getLong(SPConstants.UserLogin.USER_VIP_EXPIRE, 0);
    }

    public static void putUserVipExipre(long time) {
        userSp.edit().putLong(SPConstants.UserLogin.USER_VIP_EXPIRE, time).apply();
    }

    /************************ 解锁功能或者资源的记录*******************************/
    //字体解锁
    public static boolean getTypefaceUnlock(String id) {
        return userSp.getBoolean(SPConstants.FunctionsUnlock.TYPEFACE_UNLOCK + id, false);
    }

    public static void putTypefaceUnlock(String id, boolean isUnlock) {
        userSp.edit().putBoolean(SPConstants.FunctionsUnlock.TYPEFACE_UNLOCK + id, isUnlock).apply();
    }

    /**
     * @return 目前解锁位置存放在本地，用贴图或者模板的资源列表的长度变化判断是否更新位置，这里存取上次长度
     */
    @Deprecated
    public static int getLastLockDataSize(String secondClass) {
        return userSp.getInt(SPConstants.FunctionsUnlock.LAST_LOCK_DATA_SIZE + secondClass, -1);
    }

    @Deprecated
    public static void putLastLockDataSize(String secondClass, int size) {
        userSp.edit().putInt(SPConstants.FunctionsUnlock.LAST_LOCK_DATA_SIZE + secondClass, size).apply();
    }

    public static void removeLastLockDataSize(String secondClass) {
        userSp.edit().remove(SPConstants.FunctionsUnlock.LAST_LOCK_DATA_SIZE + secondClass).apply();
    }

    @Deprecated
    public static int getLastLockVersion(String secondClass) {
        return userSp.getInt(SPConstants.FunctionsUnlock.LAST_LOCK_VERSION + secondClass, -1);
    }

    @Deprecated
    public static void putLastLockVersion(String secondClass, int version) {
        userSp.edit().putInt(SPConstants.FunctionsUnlock.LAST_LOCK_VERSION + secondClass, version).apply();
    }

    public static void removeLastLockVersion(String secondClass) {
        userSp.edit().remove(SPConstants.FunctionsUnlock.LAST_LOCK_VERSION + secondClass).apply();
    }

    public static void putLockVersion(int version) {
        userSp.edit().putInt(SPConstants.FunctionsUnlock.LAST_LOCK_VERSION, version).apply();
    }

    public static int getLockVersion() {
        return userSp.getInt(SPConstants.FunctionsUnlock.LAST_LOCK_VERSION, -1);
    }

    /**
     * @return 目前解锁位置存放在本地，用贴图或者模板的资源列表的长度变化判断是否更新位置，这里存取上次长度
     */
    public static int getLastLockDataSize() {
        return userSp.getInt(SPConstants.FunctionsUnlock.LAST_LOCK_DATA_SIZE, -1);
    }

    public static void putLastLockDataSize(int size) {
        userSp.edit().putInt(SPConstants.FunctionsUnlock.LAST_LOCK_DATA_SIZE, size).apply();
    }

    public static long getLastSplashAdShowTime() {
        return userSp.getLong(SPConstants.LAST_LAUNCH_AD_SHOW_TIME, 0);
    }

    public static void putSplashAdShowTime(long lastADShowTime) {
        userSp.edit().putLong(SPConstants.LAST_LAUNCH_AD_SHOW_TIME, lastADShowTime).apply();
    }

    /**
     * 广告位广告展示次数
     */
    public static void addAndPutAdSpaceExposeNumber(String name) {
        int number = getAdSpaceExposeNumber(name) + 1;
        putAdSpaceExposeNumber(name, number);
    }

    public static void putAdSpaceExposeNumber(String name, int number) {
        userSp.edit().putInt(SPConstants.Ad.AD_SPACE_EXPOSE_NUMBER + name, number).apply();
    }

    /**
     * @param name {@link AdData.AdSpaceName}
     * @return
     */
    public static int getAdSpaceExposeNumber(String name) {
        return userSp.getInt(SPConstants.Ad.AD_SPACE_EXPOSE_NUMBER + name, 0);
    }

    public static void putLastUseData(String useData) {
        userSp.edit().putString(SPConstants.LAST_USE_DATA, useData).apply();
    }


    public static String getLastUseData() {
        return userSp.getString(SPConstants.LAST_USE_DATA, "0");
    }

    public static void putLastRadShowTime(long time) {
        userSp.edit().putLong(SPConstants.Ad.LAST_VAD_SHOW_TIME, time).apply();
    }


    public static long getLastRadShowTime() {
        return userSp.getLong(SPConstants.Ad.LAST_VAD_SHOW_TIME, 0);
    }

    public static void putGifAutoAdd(boolean isAdd) {
        userSp.edit().putBoolean(SPConstants.AppSettings.GIF_AUTO_ADD, isAdd).apply();
    }


    public static boolean get_isGifAutoAddOn() {
        return userSp.getBoolean(SPConstants.AppSettings.GIF_AUTO_ADD, true);
    }

    public static int getContentMaxSupportBmSize() {
        return userSp.getInt(SPConstants.AppSettings.CONTENT_MAX_SUPPORT_BM_SIZE, -1);
    }
    public static void putContentMaxSupportBmSize(int size) {
        userSp.edit().putInt(SPConstants.AppSettings.CONTENT_MAX_SUPPORT_BM_SIZE, size).apply();
    }


    public static void putTransferFinish(boolean success) {
        userSp.edit().putBoolean(SPConstants.AppSettings.transfer_success, success).apply();
    }

    public static boolean getTransferSuccess() {
        return userSp.getBoolean(SPConstants.AppSettings.transfer_success, true);
    }

    public static void putHighResolutionMode(boolean checked) {
        userSp.edit().putBoolean(SPConstants.AppSettings.open_high_resolution_mode, checked).apply();
    }

    public static boolean getHighResolutionMode() {
        return userSp.getBoolean(SPConstants.AppSettings.open_high_resolution_mode, false);
    }
}
