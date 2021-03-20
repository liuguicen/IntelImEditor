package com.mandi.intelimeditor.ad;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.mandi.intelimeditor.ad.tencentAD.InsertAd;
import com.mandi.intelimeditor.ad.tencentAD.TxFeedAd;
import com.mandi.intelimeditor.ad.tencentAD.TxFeedAdPool;
import com.mandi.intelimeditor.ad.ttAD.feedad.TTFeedAdPool;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userSetting.SPConstants;
import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.R;
import com.umeng.analytics.AnalyticsConfig;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 还要解决的几个问题：
 * 在RecyclerView这种List里面加载原生广告，数量不定怎么做？？
 * 用户滑动过程每次加载View的时候，就调用一次refresh吗？为什么自己写的代码不能获取到
 * 原生模板同一个ID可以多处使用，但是显示的是同一个广告
 * 广告的策略是多久刷新一次广告内容？
 * 首页的广告计时过了点击是否有效 -- 有效
 * 创建同一个广告对象，可以放到不同的View中吗
 * <p>
 * 另一点注意，而目前腾讯广告价格计算是价格与点击率是二次函数类似的增长关系，而不是线性关系
 * 由此有：（1）应该以点击率为第一，尽最大力降低展示次数，展示量一般都是够的，一个用户一次都够多了，所以不用担心这个
 * （2）不能使用平均化，使用相同ID，让点击率高的广告位和点击率低的广告位混合，提高总收入，反而会降低
 * 而是应该尽量的将每个展示位都使用不同的ID，这样收入才高
 * 这个东西类似于均值不等式，或者说凹函数，可以推下，
 * 已知f(x)为凹函数，x为点击率，f(x)表示千次展示收入，a，b为点击千次数
 * 证明af(x1) + bf(x2) > (a + b) f( (ax1+bx2) / (a + b) )
 * 同时除以（a+b), 化成了凹函数的定义定理：ef(x1) + (1-e)f(x2) > f(ex1+(1-e)x2)
 *
 * <p></p>
 * 经验：
 * 一个广告位最终用到多家的广告，所以设计的就要考虑面向接口，多个实现类
 * 这个自己很长时间没想到
 */
public class AdData {
    // ad写成a_d防破解
    // public final static String GDT_APP_ID = "1108129078"; // 广点通的媒体位ID,个人的，已弃用
    //    public static final String GDT_LAUNCH_POS_ID = "9030257108920435";
    //    public static final String GDT_ID_PURE_PIC_1 = "5000966062921425";
    //    public static final String GDT_ID_PURE_PIC_2 = "8000462082148518";
    //    public static final String GDT_ID_PURE_PIC_3 = "8010264059852353";
    //    public static final String GDT_ID_PURE_PIC_NEW = "5010865593238216";

    //    public static final String GDT_ID_INSERT_SCREEN_1 = "7040160122578131";

    public final static String GDT_APP_ID = "1109743631"; // 企业的

    // 优量汇企业的
    public static final String GDT_ID_LAUNCH_QY = "9070881492546044";
    public static final String GDT_ID_PURE_PIC_QY_1 = "8070584482441036";
    public static final String GDT_ID_PURE_PIC_QY_2 = "4030481412642067";
    public static final String GDT_ID_INSERT_SCREEN_AD = "3081864377490467";
    public static final String GDT_ID_INSERT_SCREEN_PURE_VAD = "8001861378935287";
    public static final String GDT_ID_FEED_PIC_RES_LIST = "8011617467628352"; // 信息流，图片资源列表
    public static final String GDT_ID_REWARD_VAD = "1011710417927966";

    /*** 各处广告的ID，各处广告数据的处理以此为核心*/
    public static final int DEFAULT_AD_STATE_COUNT = 2;
    public static final double PROBABILITY_VAD_PTU_RESULT = 1 / 13f;

    /********************各处广告上次点击或者展示时间*****************/
    public static long lastClickTime_ptuResult = 0;
    public static long lastClickTime_ptuOperation = 0;
    /**
     * 上次视频广告展示时间，包括激励视频和全屏视频
     */
    public static long lastVideoAdShowTime = 0;

    // 一个视频广告显示之后，不要没多久就再显示另一个视频广告（激励除外），不然对用户干扰太大
    public static final long VIEDO_AD_SHOW_INTERVAL = 12 * 60 * 1000;
    public static final long LAUNCH_AD_TIME_INTERVAL = 10 * 60 * 1000; // 两次开屏广告的间隔时间
    // 点击率和价格的关系是二次函数式增长，不是线性的，所以应该提升点击率，而不是单纯曝光次数，
    // 即目前要降低曝光次数，增加点击率
    public static final long NATIVE_AD_LOAD_TIME_INTERVAL = 45 * 60 * 1000; // 加载原生广告的间隔时间


    /**
     * 激励视频广告锁住的位置
     */
    public static final List<Integer> templateLockedPosition = new ArrayList<Integer>() {{
        add(3);
        add(6);
        add(8);
        add(11);
        add(13);
        add(14);
        add(17);
        add(19);
        add(22);
        add(25);
        add(28);
        add(30);
        add(33);
        add(36);
        add(38);
        add(44);
        add(47);
        add(50);
        add(53);
        add(58);
        add(60);
        add(63);
        add(67);
        add(71);
        add(76);
        add(80);
        add(85);
        add(87);
        add(94);
        add(98);
        add(103);
        add(108);
        add(111);
        add(118);
        add(122);
        add(126);
        add(132);
        add(134);
        add(141);
        add(146);
        add(154);
        add(162);
        add(168);
        add(173);
        add(176);
        add(184);
        add(188);
        add(190);
        add(198);
        add(205);
        add(210);
        add(214);
        add(219);
        add(221);
        add(229);
        add(231);
        add(233);
        add(237);
        add(244);
        add(249);
        add(257);
        add(264);
        add(267);
        add(274);
        add(277);
        add(279);
        add(282);
        add(290);
        add(296);
    }};
    public static final List<Integer> tietuLockedPosition = new ArrayList<Integer>() {{
        add(5);
        add(7);
        add(10);
        add(13);
        add(15);
        add(18);
        add(20);
        add(23);
        add(25);
        add(28);
        add(30);
        add(34);
        add(38);
        add(41);
        add(45);
        add(50);
        add(53);
        add(59);
        add(62);
        add(65);
        add(69);
        add(72);
        add(78);
        add(85);
        add(91);
        add(97);
        add(103);
        add(108);
        add(103);
        add(119);
        add(125);
        add(131);
        add(139);
        add(145);
        add(151);
        add(159);
        add(162);
        add(170);
        add(183);
        add(195);
        add(199);
        add(204);
        add(210);
        add(215);
        add(221);
        add(228);
        add(233);
        add(237);
        add(241);
        add(250);
        add(259);
        add(271);
        add(285);
        add(290);
        add(296);
    }};

    static {
        int start = 300;
        for (int i = 1; i < 20; i++)
            for (int j = 4; j < 9; j++) {
                start += j;
                templateLockedPosition.add(start);
                tietuLockedPosition.add(start);
            }
    }

    /**
     * 包含了需要解锁的资源，即key，用图片url的hashcode生成，
     * 以及需要解锁的资源的解锁结果
     * 判断一个资源是否锁住时，就判断是否包含它的key并且value = false
     */
    public static Map<String, Boolean> sUnlockData = new HashMap<>();

    /**
     * 是否显示广告，这里的策略是贴图和模板只要点击了，整个List的广告去除，
     * 然后也是一个间隔时间之后显示，目前等于开屏的时间间隔的一半
     */
    public static long lastTietuAdCloseTime = 0;
    public static long lastTemplateAdCloseTime = 0;
    public static long lastLocalAdCloseTime = 0;
    public static TxFeedAd sPtuResultAd;
    public static int PTU_AD_CLICK_INTERVAL = 6;
    /**
     * 判断刚才点击了广告，由于从Ac里面获取点击事件比较麻烦，这里设置全局变量给Ac获取
     */
    public static boolean hasClikedAdJust = false;
    private static TxFeedAdPool sTxPicAdPool;
    private static TxFeedAdPool sTxFeedAdPool;
    private static TTFeedAdPool sTTFeedAdPool_template;
    private static TTFeedAdPool sTTFeedAdPool_PicList;
    private static int sPtuResultCount;
    public static boolean isRewardVadError = false;
    public static int tryRewarVadNumber = 0;

    /**
     * 初始化广告数据，异步初始化没有关系，没初始化出来说明完全关闭应用后启动的，使用默认值0，然后显示广告是可以的
     */
    public static void readData() {
        SharedPreferences sp = IntelImEditApplication.appContext.getSharedPreferences(
                SPConstants.user_configs, Context.MODE_PRIVATE);
        lastClickTime_ptuOperation = sp.getLong(SPConstants.LAST_CLICK_TIME_PTU_OPERATION, 0);
        lastClickTime_ptuResult = sp.getLong(SPConstants.LAST_CLICK_TIME_PTU_RESULT, 0);
        sPtuResultCount = sp.getInt(SPConstants.PTU_RESULT_COUNT, 0);
        lastVideoAdShowTime = SPUtil.getLastRadShowTime();

        // 读取解锁数据
        if (!AdData.judgeAdClose(TT_AD)) {
            SharedPreferences unlockSp = IntelImEditApplication.appContext.getSharedPreferences(
                    SPConstants.unlock_data_sp, Context.MODE_PRIVATE);

            Map<String, ?> templateAll = unlockSp.getAll();
            for (Map.Entry<String, ?> entry : templateAll.entrySet()) {
                if (LogUtil.debugRewardAd) {
                    Log.d(TTRewardVad.TAG, "读出解锁数据 " + entry.getKey() + " : " + entry.getValue());
                }
                sUnlockData.put(entry.getKey(), ((Boolean) entry.getValue()));
            }
        } else { // 解锁关闭的话，预设的解锁位置也得清空，因为第一次下载图片资源列表时可能会直接使用预设列表设置解锁位置
            tietuLockedPosition.clear();
            templateLockedPosition.clear();
        }
    }

    public static void writeLastPtuResultAdClickTime(long time) {
        lastClickTime_ptuResult = time;
        IntelImEditApplication.appContext
                .getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE)
                .edit()
                .putLong(SPConstants.LAST_CLICK_TIME_PTU_RESULT, time)
                .apply();
    }

    public static void writeLastPtuOperationAdClickTime(long time) {
        lastClickTime_ptuOperation = time;
        IntelImEditApplication.appContext
                .getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE)
                .edit()
                .putLong(SPConstants.LAST_CLICK_TIME_PTU_OPERATION, time)
                .apply();
    }

    public static void writePtuFunctionClickCount(int count) {
        IntelImEditApplication.appContext
                .getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE)
                .edit()
                .putInt(SPConstants.PTU_FUNCTION_CLICK_COUNT, count)
                .apply();
    }

    private static void writePTuResultCount(int ptuResultCount) {
        IntelImEditApplication.appContext
                .getSharedPreferences(SPConstants.user_configs, Context.MODE_PRIVATE)
                .edit()
                .putInt(SPConstants.PTU_RESULT_COUNT, ptuResultCount)
                .apply();
    }


    public static String getAdIDByPicResourceClass(String picResourceClass) {
        if (PicResource.FIRST_CLASS_TEMPLATE.equals(picResourceClass))
            return AdData.GDT_ID_PURE_PIC_QY_1;
        return GDT_ID_PURE_PIC_QY_1;
        //        switch (picResourceClass) {
        //            case PicResource.FIRST_CLASS_TIETU:
        //                return TIETU_LIST_ID;
        //                return TEMPLATE_LIST_ID;
        //        }
        //        return TIETU_LIST_ID;
    }

    //    public static TxFeedAd prepareAdForPtuResult(Context context) {
    //        LogUtil.d(context);
    //        if (!AdData.isShowPtuResultAd()) {
    //            destroyPtuResultAd();
    //            return null;
    //        }
    //        // 同一个Activity下，不要销毁，减少曝光量，增加点击率
    //        if (sPtuResultAd != null && sPtuResultAd.isInvalid()) {
    //            destroyPtuResultAd();
    //        }
    //
    //        if (sPtuResultAd == null) {
    //            destroyPtuResultAd();
    //            sPtuResultAd = new TxFeedAd(null,
    //                    AdData.PTU_RESULT_ID, EventName.ptu_result_ad,
    //                    "", null);
    //            sPtuResultAd.setMaxExposureNumber(4);
    //            sPtuResultAd.setCloseListener(null);
    //            sPtuResultAd.loadAdResources(null);
    //        }
    //        return sPtuResultAd;
    //    }

    public static void destroyPtuResultAd() {
        if (sPtuResultAd != null) {
            sPtuResultAd.destroy();
            sPtuResultAd = null;
        }
    }

    public static void destroyPtuInsertAd() {
        InsertAd.destroyAd();
    }


    public static String getPicResourceAd_PositionName(String firstClass) {
        switch (firstClass) {
            case PicResource.FIRST_CLASS_LOCAL:
                return "LocalPicListAd";
            case PicResource.FIRST_CLASS_TIETU:
                return "TietuListAd";
            case PicResource.FIRST_CLASS_TEMPLATE:
                return "TemplateListAd";
        }
        return "";
    }

    /**
     * 目前策略，
     */
    public static boolean isShowPtuResultAd() {
        // 前两次PTu不显示
        if (sPtuResultCount <= 2) {
            return false;
        }

        if (System.currentTimeMillis() - lastClickTime_ptuResult < TimeDateUtil.HOUR_MILS * 1) { // 点击后的x小时内不显示
            return false;
        }

        if (Math.random() < 0.60) { // xx%的几率不显示
            return false;
        }
        return true;
    }

    /**
     * 目前策略，点击后的半小时内不显示
     */
    public static boolean isShowPtuAd() {
        // if (System.currentTimeMillis() - lastClickTime_ptuOperation < TimeDataUtil.HOUR_MILS / 2) {
        //     return false;
        // }
        return true;
    }

    public static void setClicked(String adId) {
        if (adId.equals(GDT_ID_INSERT_SCREEN_AD) || adId.equals(GDT_ID_INSERT_SCREEN_PURE_VAD)) {
            writeLastPtuOperationAdClickTime(System.currentTimeMillis());
        }
    }

    /**
     * 测试版的，貌似View不使用Activity的Context也能用，这样的话可以很大的减少曝光量，增加点击率
     */
    public static synchronized TxFeedAdPool getTxPicAdPool() {
        if (sTxPicAdPool == null) {
            sTxPicAdPool = new TxFeedAdPool(IntelImEditApplication.appContext, true);
        }
        return sTxPicAdPool;
    }

    /**
     * 测试版的，貌似View不使用Activity的Context也能用，这样的话可以很大的减少曝光量，增加点击率
     */
    public static synchronized TxFeedAdPool getTxFeedAdPool(Context context) {
        if (sTxFeedAdPool == null) {
            sTxFeedAdPool = new TxFeedAdPool(context, false);
        }
        return sTxFeedAdPool;
    }

    public static void preLoadTTFeedAd_template(Activity activity, String adID) {
        if (sTTFeedAdPool_template == null) {
            sTTFeedAdPool_template = new TTFeedAdPool(activity, TTFeedAdPool.DEFAULT_MAX_AD_NUMBER);
        }
        int width = (int) (AllData.screenWidth / 2 - activity.getResources().getDimension(R.dimen.pic_resource_list_template_margin) * 2);
        sTTFeedAdPool_template.preloadOneFeedAd(adID,
                AdData.getPicResourceAd_PositionName(PicResource.FIRST_CLASS_TEMPLATE),
                width);
        if (LogUtil.debugPicListFeedAd) {
            Log.d("TTFeedAdPool", "preLoadTTFeedAd: 预加载头条信息流广告");
        }
    }

    public static synchronized TTFeedAdPool getTTFeedAdPool_template(Activity activity) {
        if (sTTFeedAdPool_template == null) {
            sTTFeedAdPool_template = new TTFeedAdPool(activity, TTFeedAdPool.DEFAULT_MAX_AD_NUMBER);
        }
        return sTTFeedAdPool_template;
    }

    public static TTFeedAdPool getTTFeedAdPool_picList(Activity activity) {// TODO: 2020/6/1 似乎会内存泄露
        if (sTTFeedAdPool_PicList == null) {
            sTTFeedAdPool_PicList = new TTFeedAdPool(activity, TTFeedAdPool.DEFAULT_MAX_AD_NUMBER);
        }
        return sTTFeedAdPool_PicList;
    }

    public static void addPTuResultCount() {
        if (sPtuResultCount < 3) {
            sPtuResultCount++;
            writePTuResultCount(sPtuResultCount);
        }
    }

    /**
     * 用户开通VIP成功之后，清除一些广告相关的数据
     */
    public static void onOpenVipSuccess() {
        sUnlockData.clear();
        // 解锁关闭的话，预设的解锁位置也得清空，因为第一次下载图片资源列表时可能会直接使用预设列表设置解锁位置
        tietuLockedPosition.clear();
        templateLockedPosition.clear();
    }

    /**
     * 广告商代号，最好统一，特殊的地方除外
     */
    public static final int TT_AD = 1;
    public static final int TENCENT_AD = 2;


    /**
     * 是否全局关闭广告
     * 不传参数表示广告位可能是任意类型, 任意一个广告关闭，那个这个广告位就关闭
     */
    public static boolean judgeAdClose() {
        return judgeAdClose(TENCENT_AD) || judgeAdClose(TT_AD);
    }

    /**
     * 是否全局关闭广告
     *
     * @param adType {@link #TENCENT_AD} {@link #TT_AD} 广告位类型，可能某种类型关闭而另外的类型不用关闭
     */
    public static boolean judgeAdClose(int adType) {
        // VIP没到期

        if (AllData.isVip) {
            if (Math.random() < 0.001) {
                US.putOpenVipEvent(US.OPEN_VIP_SUCCESS);
            }
            return true;
        }
        if (adType == TENCENT_AD && AllData.isCloseTencentAd) {
            return true;
        }

        if (adType == TT_AD && AllData.isCloseTTAd) {
            return true;
        }
        return false;
    }


    public static void checkAdClose(Context context) {
        String channel = AnalyticsConfig.getChannel(context);
        long adOpenTime = 0;
        if ("huawei".equals(channel)) { // 华为渠道检测期间不显示腾讯广告
            AllData.isCloseVipFunction = true;
            Calendar calendar = Calendar.getInstance();
            // 一定注意！！！calendar月份从0开始算的， 10月1日要写成9月1日，不然多一个月延期，一个月的广告费
            calendar.set(2020, 10, 18, 20, 0, 0);
            adOpenTime = calendar.getTimeInMillis();
        }
        if ("vivo".equals(channel)) {
            Calendar calendar = Calendar.getInstance();
            // 一定注意！！！calendar月份从0开始算的， 10月1日要写成9月1日，不然多一个月延期，一个月的广告费
            calendar.set(2020, 10, 18, 12, 0, 0);
            adOpenTime = calendar.getTimeInMillis();
        }

        if ("oppo".equals(channel)) {
            Calendar calendar = Calendar.getInstance();
            // 一定注意！！！calendar月份从0开始算的， 10月1日要写成9月1日，不然多一个月延期，一个月的广告费
            calendar.set(2020, 10, 18, 15, 0, 0);
            adOpenTime = calendar.getTimeInMillis();
        }
        closeAdForStoreCheck(adOpenTime);

        //        if ("alibaba".equals(channel)) {
        //            Calendar calendar = Calendar.getInstance();
        //            calendar.set(2019, 9, 3, 0, 0, 0);
        //            AllData.isCloseTTAd = true;
        //            AllData.isCloseTencentAd = true;
        //        }

        //        if ("oppo".equals(channel)) {
        //            Calendar calendar = Calendar.getInstance();
        //            // 一定注意！！！calendar月份从0开始算的， 10月1日要写成9月1日，不然多一个月延期，一个月的广告费
        //            calendar.set(2020, 0, 0, 0, 0, 0);
        //
        //            long endTime = calendar.getTimeInMillis();
        //            if (BuildConfig.DEBUG) {
        //                long hours = (endTime - System.currentTimeMillis()) / 3600 / 1000;
        //                Log.e("--", "小时 = " + hours + " 天数 = " + (hours / 24));
        //                long ttHours = (endTime - System.currentTimeMillis()) / 3600 / 1000;
        //                Log.e("--", "小时 = " + ttHours + " 天数 = " + (ttHours / 24));
        //            }
        //            if (System.currentTimeMillis() < endTime) {
        //                AllData.isCloseTencentAd = true;
        //                AllData.isCloseTTAd = true;
        //                US.putOtherEvent("OPPO skip tencent ad");
        //            }
        //        }
    }

    public static void closeAdForStoreCheck(long adOpenTime) {
        if (BuildConfig.DEBUG) {
            long hours = (adOpenTime - System.currentTimeMillis()) / 3600 / 1000;
            Log.e("--", "小时 = " + hours + " 天数 = " + (hours / 24));
            long ttHours = (adOpenTime - System.currentTimeMillis()) / 3600 / 1000;
            Log.e("--", "小时 = " + ttHours + " 天数 = " + (ttHours / 24));
        }
        if (System.currentTimeMillis() < adOpenTime) {
            AllData.isCloseTencentAd = true;
            AllData.isCloseTTAd = true;
            US.putOtherEvent(US.SKIP_AD_FOR_CHECK);
        }
        // 华为的对头条广告直到10月7日都关闭 // 10月7日之后提醒用户升级
        // if (System.currentTimeMillis() < ttEndTime) {
        //     AllData.isCloseTTAd = true; // 特殊原因 2.0.0 版本关闭，后面不用
        // } else {
        //     Toast.makeText(this, "目前部分服务不可用，请您到应用市场升级最新版本，感谢您的配合！", Toast.LENGTH_LONG).show();
        // }
    }

    public static void resetAdSpaceExposeNumber() {
        if (LogUtil.debugAdDStrategy) {
            Log.d("AdStrategy", "重置广告位广告曝光次数");
        }
        for (String name : AdSpaceName.AD_SPACE_LIST) {
            SPUtil.putAdSpaceExposeNumber(name, 0);
        }
    }

    /**
     * 广告位置的名称
     */
    public static final String TYPEFACE_REWARD_AD = "typeface_reward_ad";
    public static final String REWARD_AD_NAME_TEMPLATE = "template_reward_ad";
    public static final String REWARD_AD_NAME_TIETU = "tietu_reward_ad";

    public static void onRewardVadShow() {
        AdData.lastVideoAdShowTime = System.currentTimeMillis();
        SPUtil.putLastRadShowTime(AdData.lastVideoAdShowTime);
        SPUtil.addAndPutAdSpaceExposeNumber(AdData.AdSpaceName.REWARD_VAD);// 设置广告源策略需要的
    }

    /**
     * APP中所有广告位名称集合
     */
    public static class AdSpaceName {
        public static final String SPLASH = "Splash";
        public static final String LOCAL_PIC = "LocalPic"; // 本地图片列表纯图广告
        public static final String LOCAL_FEED = "LocalFeed"; // 本地图片列表信息流
        public static final String PIC_RES_PIC = "PicResPic"; //
        public static final String PIC_RES_FEED = "PicResFeed";
        public static final String PTU_PIC_RES_PIC = "PTuPicResPic";
        public static final String REWARD_VAD = "REWARD_VAD";
        public static final String PTU_INSERT = "PTuInsertAD";
        public static final String IN_LOAD_SHORT_VIDEO = "InLoadShortVideo";
        public static final String PTU_RESULT = "PTuResult";
        public static final String PTU_RESULT_RETURN = "PTuResultReturn";

        public static final String[] AD_SPACE_LIST = new String[]{
                SPLASH,
                LOCAL_PIC,
                LOCAL_FEED,
                PIC_RES_PIC,
                PIC_RES_FEED,
                PTU_PIC_RES_PIC,
                REWARD_VAD,
                PTU_INSERT,
                IN_LOAD_SHORT_VIDEO,
                PTU_RESULT,
                PTU_RESULT_RETURN,
        };
    }
}
