package com.mandi.intelimeditor.user.userSetting;

/**
 * Created by LiuGuicen on 2017/1/5 0005.
 * 通知数据的接口
 */

public interface SettingDataSource {
    void initDiskCacheDataInfo();

    void saveSendShortCutNotify(boolean isSend);

    void saveSharedWithout(boolean isWith);

    boolean getHighSolution();

    boolean getSendShortcutNotifyExit();

    boolean getSharedWithout();

    float getAppDataSize();

    /**
     * @param userChosenItems 用户选定的清除内容
     * @return 返回哪些内容清除失败
     */
    String clearAppCache(boolean[] userChosenItems);

    String[] getDataItemInfos();
}
