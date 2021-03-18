package com.mandi.intelimeditor.user.userSetting;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.GlobalSettings;
import com.mandi.intelimeditor.common.util.FileTool;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by LiuGuicen on 2017/1/5 0005.
 * 设置的数据，数据从作用域来看可以分两部分，
 * 一部分是全局的设置，是全局变量，应用运行中都有可能用到
 * 另一部分是到了设置界面才需要的设置数据
 */

public class SettingDataSourceImpl implements SettingDataSource {
    private final GlobalSettings globalSettings;
    Context appContext;

    /**
     * 清除数据相关
     */
    private String[] DATA_DIRS;
    private float totalSize;
    private final String[] DATA_NAMES;

    private String[] dataItemInfos;


    /**
     * 传入app的context
     * 需要全局使用，这里只加载全局需要的数据
     */
    public SettingDataSourceImpl(@NonNull Context appContext) {
        this.appContext = appContext;
        this.globalSettings = AllData.globalSettings;
        /**
         * 写死的部分
         */
        // 字体缓存， glide下载来的贴图缓存
        DATA_DIRS = new String[]{AllData.zitiDir, appContext.getCacheDir() + "/" + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR};

        DATA_NAMES = new String[]{"字体文件", "贴图文件"};
    }

    /**
     * 初始化app的磁盘数据存储信息，比如字体下载，图片缓存
     * 注意清除之后要调用一次刷新信息
     */
    @Override
    public void initDiskCacheDataInfo() {
        totalSize = 0;
        //获取大小信息
        String[] sizeStrings = new String[DATA_DIRS.length];
        DecimalFormat df = new DecimalFormat("#.0");
        for (int i = 0; i < DATA_DIRS.length; i++) {
            File file = new File(DATA_DIRS[i]);
            if (file.exists()) {
                double size = FileTool.getFileSize(file) * 1d / 1000 / 1000;
                //格式化一下
                if (size < 0.05) {//对于0，系统处理有点问题
                    sizeStrings[i] = "0";
                } else {
                    sizeStrings[i] = df.format(size);
                    if (sizeStrings[i].length() == 2) {
                        sizeStrings[i] = "0" + sizeStrings[i];
                    }
                }
                totalSize += (float) size;
            } else
                sizeStrings[i] = "0";
        }
        //说明的信息
        dataItemInfos = new String[DATA_DIRS.length];
        for (int i = 0; i < DATA_DIRS.length; i++)
            dataItemInfos[i] = DATA_NAMES[i] + "(" + sizeStrings[i] + "M)";
        totalSize = Float.parseFloat(df.format(totalSize));
    }

    @Override
    public void saveSendShortCutNotify(boolean isSend) {
        globalSettings.saveSendShortCutNotify(isSend);
    }


    @Override
    public void saveSharedWithout(boolean isWith) {
        globalSettings.saveSharedWithout(isWith);
    }

    @Override
    public boolean getSendShortcutNotify() {
        return globalSettings.getSendShortcutNotify();
    }

    @Override
    public boolean getSendShortcutNotifyExit() {
        return globalSettings.getSendShortcutNotifyExit();
    }

    public String[] getDataItemInfos() {
        return dataItemInfos;
    }

    /**
     * @return 默认是要带 false
     */
    @Override
    public boolean getSharedWithout() {
        return globalSettings.getSharedWithout(); // 默认是要带 false
    }

    @Override
    public float getAppDataSize() {
        return totalSize;
    }

    /**
     * 涉及到glide清除缓存，需要在其它线程调用
     *
     * @param userChosenItems 用户选定的清除内容
     */
    @Override
    public String clearAppCache(boolean[] userChosenItems) {
        String failRes = "";
        // 清除字体文件
        if (userChosenItems[0]) {
            if (!clearTypeface())
                failRes += DATA_NAMES[0];
        }
        // 清除缓存的贴图
        if (userChosenItems[1]) {
            if (!clearTietuCache())
                failRes += DATA_NAMES[1];
        }
        initDiskCacheDataInfo();
        return failRes;
    }

    private boolean clearTietuCache() {
        Glide.get(appContext).clearDiskCache();
        return true;
    }

    private boolean clearTypeface() {
        return FileTool.deleteAllChileFile(new File(AllData.zitiDir));
    }
}
