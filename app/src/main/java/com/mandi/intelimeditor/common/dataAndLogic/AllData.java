package com.mandi.intelimeditor.common.dataAndLogic;

import android.content.Context;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.appInfo.HasReadConfig;
import com.mandi.intelimeditor.ptu.changeFace.LevelsAdjuster;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.bean.GroupBean;
import com.mandi.intelimeditor.R;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * 保存应用所需的一些常用通用的数据项
 *
 * @author acm_lgc
 * @version android 6.0,  sdk 23,jdk 1.8,2015.10
 */
public class AllData {

    public static final String TAG = "暴走P图";
    public static String PACKAGE_NAME = "com.mandi.intelimeditor";
    /**
     * 获取服务器数据更新时间，-1表示网络出错，需要重新获取，0表示没有数据，
     */
    public static final long GET_SERVICE_UPDATE_TIME_FAILED = -1;
    public static long latestTietuModifyTime = GET_SERVICE_UPDATE_TIME_FAILED;  // 一开始相当于没有获取
    public static boolean hasInitDataNeedAc = false;

    /**
     * 一些应用市场由于特殊情况，暂时关闭广告，比如华为检查直接下载类的广告，由于目前腾讯广告包含此类，且不能关闭
     * 只能手动避免
     */
    public static boolean isCloseTencentAd = false;
    public static boolean isCloseTTAd = false;
    public static boolean isCloseVipFunction = false; // 不打开VIP功能
    // 目前图片资源的数量不会太多，2020年6月，没到700条，直接把查到的数据做成全局的
    // 因为目前多个地方使用，更方便，也更快
    public static List<PicResource> expressResList;
    public static List<PicResource> propertyResList;
    public static List<PicResource> templateResList;
    public static List<PicResource> allResList;
    /**
     * 所有分组列表
     */
    public static int sortByGroup = 0; //0：默认热度排序，1：更新时间排序
    // TODO: 2020/10/23 每个分类列表 存一遍 放太多数据了，不要这样搞
    public static Map<String, List<GroupBean>> mAllGroupList = new HashMap<>();
    public static Map<String, List<PicResource>> mAllCategoryList = new HashMap<>();
    public static boolean hasLoadGuide = false;
    public static List<PicResource> styleList = new ArrayList<>();
    public static List<PicResource> contentList = new ArrayList<>();
    private static BitmapPool sPTuBmPool;


    /***用户账号相关***/
    public static long localUserVipExpire = 0;
    public static String localUserId = "";
    /**
     * 用于本次启动运行过程中判断是否是VIP
     */
    public static boolean isVip = false;
    public static String floor_vip_price = VipUtil.getFloorPriceString(VipUtil.DEFAULT_FLOOR_PRICE);

    /**
     * 用户刚才开通了VIP，由于界面跳转比较复杂，不好控制，这里只好用全局变量处理一下
     */
    public static boolean hasOpenVipJust = false;

    /**
     * 一些其它的静态变量，同一起来好查看和管理
     */
    public static Paint sTransparentPaint;

    public static SimpleDateFormat bmobDataParser = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    private static IWXAPI wxAPI;

    static {
        Log.e(TAG, "static initializer: 静态初始化了");
    }

    /**
     * 屏幕的宽高, 注意存在少数情况，手机获取到不到宽高
     */
    public static int screenWidth, screenHeight;

    /**
     * 注意！对于绝对要使用屏幕真实高度的地方，这个方法不适用
     * 获取屏幕高度值，如果屏幕高度获取失败，会返回一个默认值，最常见的宽高
     */
    public static int getScreenWidth() {// 此方法可做复制代码用，不要删了
        return screenWidth <= 100 ? 1080 : screenWidth;
    }

    /**
     * 注意！对于绝对要使用屏幕真实高度的地方，这个方法不适用
     * 获取屏幕高度值，如果屏幕高度获取失败，会返回一个默认值，最常见的宽高
     */
    public static int getScreenHeight() {// 此方法可做复制代码用，不要删了
        return screenHeight <= 100 ? 1920 : screenHeight;
    }

    /**
     * 示例在ac的onCreate注册，实际上使用前注册应该就行
     */
    @Nullable
    public static IWXAPI getWXAPI() {
        if (wxAPI == null) {
            // 通过WXAPIFactory工厂，获取IWXAPI的实例
            wxAPI = WXAPIFactory.createWXAPI(IntelImEditApplication.appContext, AppConfig.ID_IN_WEIXIN, true);

            // 将应用的appId注册到微信
            boolean isRegisterSuccess = wxAPI.registerApp(AppConfig.ID_IN_WEIXIN);
            if (!isRegisterSuccess) // 注册失败
                wxAPI = null;  // 赋值为空，后面再次调用前面的create和register也没问题
        }
        return wxAPI;
    }

    /**
     * 常用的图片的格式
     */
    public final static String[] normalPictureFormat = new String[]{"png", "gif", "bmp", "jpg", "jpeg", "tiff", "jpeg2000", "psd", "icon"};
    public static float thumbnailSize = 10000.0f;

    /**
     * 图片内存最小值,单位byte，是图片存储在sd卡的大小，不是长乘以宽，也不是解析成bm之后的大小
     * 这里自己以前似乎是弄错了，理解成长乘以宽了，100 * 100的jpg也不到5k
     */
    public final static int PIC_FILE_SIZE_MIN = 2 * 1024;

    /**
     * 图片内存最大值,单位byte
     */
    public final static int PIC_FILE_SIZE_MAX = 50 * 1024 * 1024;

    /**
     * 短视频最短时间
     */
    public final static int SHORT_VIDEO_DURATION_MIN = 1 * 1000;

    /**
     * 短视频最长时间
     */
    public final static int SHORT_VIDEO_DURATION_MAX = 30 * 1000 + 500;


    public static final String appFilePathDefault = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
            IntelImEditApplication.appContext.getResources().getString(R.string.app_name) + "/";
    public static final String zitiDir = appFilePathDefault + "字体/";

    public static final String picDir = appFilePathDefault + IntelImEditApplication.appContext.getResources().getString(R.string.app_name) + "-制作图片/";
    public static final Context appContext = IntelImEditApplication.appContext;

    //一些基本配置
    public static AppConfig appConfig;
    public static HasReadConfig hasReadConfig;
    public static GlobalSettings globalSettings;
    public static Random sRandom; // 全局使用的随机数生成器，多次创建麻烦

    @NotNull
    public static BitmapPool getPTuBmPool() {
        if (sPTuBmPool == null) {
            sPTuBmPool = new BitmapPool();
        }
        return sPTuBmPool;
    }

    /**
     * 用完一定要置空，用来传递全局数据的
     * 因为对应的数量大，不能用intent等传
     */
    public static LevelsAdjuster levelsAdjuster;
}
