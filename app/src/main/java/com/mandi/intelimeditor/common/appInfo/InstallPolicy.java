package com.mandi.intelimeditor.common.appInfo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;

import java.util.ArrayList;

import static com.mandi.intelimeditor.common.dataAndLogic.AllData.appConfig;


/**
 * Created by liuguicen on 2016/8/13.
 */
public class InstallPolicy {
    Context mGlobalContext;
    private MyDatabase myDatabase;

    public InstallPolicy() {
        mGlobalContext = IntelImEditApplication.appContext;
    }

    /**
     * 执行第一次安装或更新新版本所需的东西
     * 注意，1.0版本没有写入版本号，需要额外判断
     */
    public void processPolicy() {

        int lastVersion = appConfig.readAppVersion();

        if (BuildConfig.VERSION_CODE == lastVersion) {//已经更新版本数据，或者新安装的版本相同
            return;
        } else if (BuildConfig.VERSION_CODE < lastVersion) {//更新的版本小于当前安装好的版本
            Toast.makeText(IntelImEditApplication.appContext, "更新的版本过低，请安装较新版本", Toast.LENGTH_LONG).show();
        } else {//执行版本更新操作=》是大于，不是==或《=

            if (lastVersion == -1) // 是新安装
            {
                Log.d(getClass().getSimpleName(), "processPolicy: ");
                createAppFile();
                setShearInfo();
                SPUtil.putLastRadShowTime(System.currentTimeMillis()); // 用户几天没看激励视频会弹出弹窗，新用户一直没看，不能弹，这里写入当前时间
            }
            //            执行版本更新操作
            // 清除旧版本信息，写入新版本信息,注意只需要秦楚或者写入其中一个
            if (appConfig.isVersion1_0())//是1.0版本
            {
                clearOldVersionInfo_1_0();
                setShearInfo();
            } else if (BuildConfig.VERSION_CODE == 17) {// 2.0.0 版本的
                AllData.hasReadConfig.write_appGuide(false); // 2.0.0 引导页有新东西，需要用户重新阅读一次
                    /*
                   if(appVersion==AppConfig.APP_VERSION_2){

                   }*/
            }

            if (lastVersion <= 32) { // 删除所有低版本中的关于解锁的sp
                ArrayList<String> clsList = new ArrayList<String>() {{
                    add(PicResource.SECOND_CLASS_BASE);
                    add(PicResource.SECOND_CLASS_EXPRESSION);
                    add(PicResource.SECOND_CLASS_EXPRESSION);
                }};
                for (String cls : clsList) {
                    SPUtil.removeLastLockDataSize(cls);
                    SPUtil.removeLastLockVersion(cls);
                }
            }

            writeCurVersionInfo();
            appConfig.writeCurAppVersion();
        }
    }

    private void writeCurVersionInfo() {
        //每次更新之后重新上传一次设备信息，因为版本已经更新，
        //顺便还有os版本等更新的检查
        //后面后台线程检测到false，就会自动更新了
        appConfig.writeSendDeviceInfo(false);
    }

    /**
     * 清除1.0的旧版本的信息
     */
    private void clearOldVersionInfo_1_0() {
        appConfig.clearOldVersionInfo_1_0();//清除旧版本配置信息
    }


    /**
     * 设置分享的信息
     */
    private void setShearInfo() {
        MyDatabase myDatabase = null;
        try {//添加分享的优先选项，2020年4月，前几个变成了qq、微信、钉钉、抖音、快手
            String[] packageNames = new String[]{
                    "com.tencent.mobileqq", "com.tencent.mm", "com.alibaba.android.rimet",
                    "com.tencent.mobileqq", "com.ss.android.ugc.aweme", "com.tencent.mm",
                    "com.smile.gifmaker",};
            String[] shareTitles = new String[]{
                    "发送给好友", "发送给朋友", "钉钉",
                    "保存到QQ收藏", "抖音短视频", "添加到微信收藏",
                    "快手"};
            myDatabase = MyDatabase.getInstance();
            long time = System.currentTimeMillis();
            for (int i = 0; i < packageNames.length; i++)
                myDatabase.insertPreferShare(packageNames[i], shareTitles[i], time--);
        } catch (Exception e) {
            Log.e("数据库", e.getMessage());
        } finally {
            if (myDatabase != null) {
                myDatabase.close();
            }
        }

    }

    /**
     * 创建App的文件
     */
    private void createAppFile() {

    }

}
