package com.mandi.intelimeditor.common.appInfo;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.kaijia.adsdk.center.AdCenter;
import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTAdManagerHolder;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.GlobalSettings;

import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.BuildConfig;
import com.qq.e.comm.managers.GDTADManager;
import com.qq.e.comm.managers.setting.GlobalSetting;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.Random;

import cn.bmob.v3.Bmob;


//import com.umeng.message.IUmengRegisterCallback;
//import com.umeng.message.PushAgent;

/**
 */
public class IntelImEditApplication extends MultiDexApplication {
    final static String TAG = "IntelImEditApplication";
    public static IntelImEditApplication appContext;
    public static final boolean hasInitAppData = false;

    public IntelImEditApplication() {
        Log.e(TAG, "IntelImEditApplication: 应用创建了");
//        Debug.startMethodTracing("app-launch-trace", 80 * 1024 * 1024);
//        Log.d("start trace", "BaoZouPTuApplication: time " + System.currentTimeMillis() / 1000);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // 优化第一次启动，较为麻烦，目前安装包较小，以及暂不适配过于低端的机型，这里展不优化
            //            if (!isAsyncLaunchProcess()) {
            //                if (needWait(base)) {
            //                /*
            //                    第一次启动APP由于MultiDex将会非常缓慢，某些低端机可能ANR。
            //                    因此这里的做法是挂起主进程，开启:async_launch进程执行dexopt。
            //                    dexopt执行完毕，主进程重新变为前台进程，继续执行初始化。
            //                    主进程在这过程中变成后台进程，因此阻塞将不会引起ANR。
            //                 */
            //                    DexInstallDeamonThread thread = new DexInstallDeamonThread(this, base);
            //                    thread.start();
            //
            //                    //阻塞等待:async_launch完成加载
            //                    synchronized (lock) {
            //                        try {
            //                            lock.wait();
            //                        } catch (InterruptedException e) {
            //                            e.printStackTrace();
            //                        }
            //                    }
            //                    thread.exit();
            //                    Log.d("BaseApplication", "dexopt finished. alloc MultiDex.install()");
            //                }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        LogUtil.printMemoryInfo(TAG,this);
        initAppData();
    }

    private void initAppData() {
        //初始化全局数据
        AllData.appConfig = new AppConfig(this);
        AllData.hasReadConfig = new HasReadConfig();
        AllData.globalSettings = new GlobalSettings();
        AllData.sRandom = new Random(System.currentTimeMillis());
        //重置域名，必须在初始化前重置
        Bmob.resetDomain("http://ptusdk.musiclake.cn/8/");
        //初始化bmob
        Bmob.initialize(this, AppConfig.Bmob_ID); // 再是网络初始化
        //BmobDatabaseUtil.getServiceUpdateTime("PicResource",null); // 不能放到异步线程中，不然线程中运行bmob初始化没完成而出错，
        new InstallPolicy().processPolicy();  //执行第一次安装或更新新版本所需的东西
        //Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());//设置APP运行异常捕捉器，不用了，目前使用友盟的
        initLocalUserInfo();
        AdData.checkAdClose(this);
        Log.e("------------", "init: 应用初始化成功");
        initUm();
        initAd();
        initBugly();
    }

    private void initBugly() {
        Bugly.init(getApplicationContext(), "2d6e0ddfa6", BuildConfig.DEBUG);
    }

    private void initAd() {
        //穿山甲SDK初始化
        //强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
        TTAdManagerHolder.init(this);
        GDTADManager.getInstance().initWith(this, AdData.GDT_APP_ID);

        String channel = AnalyticsConfig.getChannel(this);
        int channelCode = 999;
        switch (channel) {
            case AppConfig.CHANNEL_YINGYONGBAO:
                channelCode = 9;
                break;
            case AppConfig.CHANNEL_HUAWEI:
                channelCode = 8;
                break;
            case AppConfig.CHANNEL_XIAOMI:
                channelCode = 10;
                break;
            case AppConfig.CHANNEL_OPPO:
                channelCode = 6;
                break;
            case AppConfig.CHANNEL_VIVO:
                channelCode = 7;
                break;
            case AppConfig.CHANNEL__360:
                channelCode = 999;
                break;
            case AppConfig.CHANNEL_BAIDU:
                channelCode = 1;
                break;

        }
        // 设置渠道号,渠道号信息主要用来协助平台提升流量变现效果及您的收益,请如实填写,渠道和渠道号的映射关系见下面《渠道号与渠道的对应关系》
        GlobalSetting.setChannel(channelCode);

        String curData = TimeDateUtil.getTodayData();
        String lastUseData = SPUtil.getLastUseData();
        if (curData.equals(lastUseData)) {  // 如果是同一天使用，无动作
        } else { // 新的一天的使用, 重置曝光次数
            SPUtil.putLastUseData(curData); // 启动放入和退出时放入是一样的
            AdData.resetAdSpaceExposeNumber();
        }

        // 铠甲广告
        AdCenter adCenter = AdCenter.getInstance(this);
        adCenter.onCreate();
        adCenter.setAppID(this, "3d1506dc");
        adCenter.onResume();
        adCenter.setOaid(false, "");
    }

    /**
     * 获取存储在本地的用户信息
     */
    private void initLocalUserInfo() {
        AllData.localUserId = SPUtil.getUserId();
        if (TheUserUtil.hasLoggedLastTime()) {  // 注册过
            AllData.localUserVipExpire = SPUtil.getUserVipExpire();
            // 由于启动过程需要，这里先暂时判断，后面会在非主线程里面获取网络时间再进行一次判断
            AllData.isVip = AllData.localUserVipExpire > System.currentTimeMillis();
        }
    }

    /**
     * 初始化友盟
     */
    private void initUm() {
        if (!AllData.hasReadConfig.hasAgreeAppPrivacy()) {
            UMConfigure.preInit(this, AppConfig.UM_ID, AnalyticsConfig.getChannel(this));
            return;
        }
        delayInitUM(this);
    }

    /**
     * app合规检查，必须在用户同意隐私政策之后初始化统计sdk
     */
    public static void delayInitUM(Context context) {
        /**
         * 初始化common库
         * 参数1:上下文，不能为空
         * 参数2:设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
         * 参数3:Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
         */
        UMConfigure.init(context, UMConfigure.DEVICE_TYPE_PHONE,  AppConfig.UM_PUSH_ID);
        // 开启调试
        UMConfigure.setLogEnabled(false);
        // 选用AUTO页面采集模式，不用手动调用ac的resume和pause
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

        // //获取消息推送代理示例
        // PushAgent mPushAgent = PushAgent.getInstance(this);
        // //注册推送服务，每次调用register方法都会回调该接口
        // mPushAgent.register(new IUmengRegisterCallback() {
        //     @Override
        //     public void onSuccess(String deviceToken) {
        //         //注册成功会返回deviceToken deviceToken是推送消息的唯一标志
        //         Log.i(TAG, "注册成功：deviceToken：-------->  " + deviceToken);
        //     }
        //
        //     @Override
        //     public void onFailure(String s, String s1) {
        //         Log.e(TAG, "注册失败：-------->  " + "s:" + s + ",s1:" + s1);
        //     }
        // });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
