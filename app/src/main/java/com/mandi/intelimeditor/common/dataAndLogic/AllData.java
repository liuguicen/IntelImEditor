package com.mandi.intelimeditor.common.dataAndLogic;

import android.content.Context;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.ad.LockUtil;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.appInfo.HasReadConfig;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.home.data.MediaInfoScanner;
import com.mandi.intelimeditor.home.data.UsuPathManger;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.R;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * 保存应用所需的一些常用通用的数据项
 *
 * @author acm_lgc
 * @version android 6.0,  sdk 23,jdk 1.8,2015.10
 */
public class AllData {

    public static final String TAG = "艺术美图";
    public static String PACKAGE_NAME = "com.mandi.intelimeditor";
    /**
     * 获取服务器数据更新时间，-1表示网络出错，需要重新获取，0表示没有数据，
     */
    public static final long GET_SERVICE_UPDATE_TIME_FAILED = -1;
    public static long latestTietuModifyTime = GET_SERVICE_UPDATE_TIME_FAILED;  // 一开始相当于没有获取
    public static boolean hasInitDataNeedAc = false;


    public static List<PicResource> allResList = new ArrayList<>();
    public static List<PicResGroup> mAllGroupList = new ArrayList<>(); // 分组列表，分组操作比较麻烦，所以存下来
    public static boolean hasLoadGuide = false;
    public static List<PicResource> curStyleList = new ArrayList<>();
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


    public static UsuPathManger usuManager;
    public static MediaInfoScanner sMediaInfoScanner;
    public static boolean hasInitScanLocalPic = false;
    private static final List<Emitter<String>> localPicQuery = new ArrayList<>();

    public static synchronized void initScanLocalPic() {
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {
                    usuManager = new UsuPathManger(AllData.appContext);
                    sMediaInfoScanner = MediaInfoScanner.getInstance();

                    // 先从数据库获取里面所有的图片信息
                    try {
                        usuManager.initFromDB();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "call: 从数据库获取数据完成");
                    // 扫描器扫描信息，然后通知UI更新，先会更新图片，再是文件的
                    sMediaInfoScanner.scanAndUpdatePicInfo();
                    emitter.onNext("");
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull String s) {
                        for (Emitter<String> emitter : localPicQuery) {
                            emitter.onNext("");
                        }
                        localPicQuery.clear();
                        hasInitScanLocalPic = true;
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        for (Emitter<String> emitter : localPicQuery) {
                            emitter.onError(e);
                        }
                        localPicQuery.clear();
                    }
                });

    }


    public static void queryLocalPicList(Emitter<String> emitter) {
        if (AllData.hasInitScanLocalPic) {
            emitter.onNext("");
        } else {
            localPicQuery.add(emitter);
        }
    }

    public static List<PicResource> allStyleList = new ArrayList<>();

    public static final int PROCESS_STATE_NO_START = 0;
    public static final int PROCESS_STATE_ING = 1; // 处理中
    public static final int PROCESS_STATE_SUCCESS = 2;
    public static final int PROCESS_STATE_FAILED = 3;
    public static int allPicRes_downloadState = PROCESS_STATE_NO_START;
    public static int allPicRes_groupState = PROCESS_STATE_NO_START;


    /**
     * 数据获取是异步的，可能还没获取完成, 现在allData里面的这个接口相当于异步网络查询了，后面扩展也是逻辑差不多，不用改太多
     */
    public static List<PicResGroup> styleGroupList = new ArrayList<>(); // 分组计算比较麻烦，所以存下来
    public static List<Emitter<List<PicResGroup>>> styleGroupQuery = new ArrayList<>();
    public static List<Emitter<List<PicResource>>> allResQuery = new ArrayList<>();

    public static synchronized void downLoadALLPicRes() {
        if (allPicRes_downloadState == PROCESS_STATE_ING || allPicRes_downloadState == PROCESS_STATE_SUCCESS)
            return;
        allPicRes_downloadState = PROCESS_STATE_ING;
        Log.e(TAG, "开始下载所有图片资源，重要log别删");
        allResList.clear();
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
                    PicResourceDownloader.downloadAllPicRes(emitter);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<PicResource>>() {
                    @Override
                    public void onError(Throwable throwable) {
                        if (allResList.size() > 0) { // 只下载了一部分,
                            // 目前先当做成功了，也可以循环再下载若干次
                            onComplete();
                        } else {
                            LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                            if (allPicRes_downloadState != PROCESS_STATE_SUCCESS) { // 可能会多次调用, 只要成功过一次，就不显示加载失败了
                                allPicRes_downloadState = PROCESS_STATE_FAILED;
                                LogUtil.e("下载图片资源失败 \n" + throwable.getCause());
                            }
                            if (allPicRes_groupState != PROCESS_STATE_SUCCESS) { // 可能会多次调用, 只要成功过一次，就不显示加载失败了
                                allPicRes_groupState = PROCESS_STATE_FAILED;
                                LogUtil.e("图片资源分组失败 \n" + throwable.getCause());
                            }

                            for (Emitter<List<PicResource>> emitter : allResQuery) {
                                emitter.onError(new Exception("下载贴图失败 \n" + throwable.getCause()));
                            }
                            allResQuery.clear();

                            for (Emitter<List<PicResGroup>> emitter : styleGroupQuery) {
                                emitter.onError(new Exception("下载贴图失败 \n" + throwable.getCause()));
                            }
                            styleGroupQuery.clear();
                        }
                    }

                    @Override
                    public void onNext(@NotNull List<PicResource> tietuMaterialList) {
                        allResList.addAll(tietuMaterialList);
                    }

                    @Override
                    public void onComplete() {
                        LockUtil.updateUnlockIfNeeded(allResList); // 加锁

                        String thePath = Environment.getExternalStorageDirectory().toString();
                        PicResource p1 = PicResource.path2PicResource(thePath + File.separator + "test1.jpg");
                        p1.setCategory(PicResource.CATEGORY_STYLE);
                        p1.setHeat(1000);
                        p1.setTag("梵高 星空");
                        allResList.add(p1);
                        p1 = PicResource.path2PicResource(thePath + File.separator + "test2.jpg");
                        p1.setCategory(PicResource.CATEGORY_STYLE);
                        p1.setHeat(100);
                        p1.setTag("动漫 新海诚");
                        allResList.add(p1);
                        p1 = PicResource.path2PicResource(thePath + File.separator + "test3.jpg");
                        p1.setCategory(PicResource.CATEGORY_STYLE);
                        allResList.add(p1);
                        p1 = PicResource.path2PicResource(thePath + File.separator + "test4.jpg");
                        p1.setCategory(PicResource.CATEGORY_STYLE);
                        allResList.add(p1);
                        p1 = PicResource.path2PicResource(thePath + File.separator + "test5.jpg");
                        p1.setCategory(PicResource.CATEGORY_STYLE);
                        allResList.add(p1);


                        for (Emitter<List<PicResource>> emitter : allResQuery) {
                            emitter.onNext(allResList);
                        }
                        allResQuery.clear();

                        Log.e(TAG, "查询所有图片资源成功");
                        allPicRes_downloadState = PROCESS_STATE_SUCCESS;
                        groupAllPicRes();
                    }
                });
    }

    /**
     * @see #downLoadALLPicRes()
     */
    public static void groupAllPicRes() {
        Log.e(TAG, "开始下载所有图片资源，重要log别删");
        allPicRes_groupState = PROCESS_STATE_ING;
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
                    styleGroupList = PicResSearchSortUtil.groupByTag(null, PicResource.CATEGORY_STYLE, allResList);
                    emitter.onNext(new ArrayList<>());
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<PicResource>>() {
                    @Override
                    public void onNext(@NotNull List<PicResource> tietuMaterialList) {
                        for (Emitter<List<PicResGroup>> emitter : styleGroupQuery) {
                            emitter.onNext(styleGroupList);
                        }
                        styleGroupQuery.clear();
                        allPicRes_groupState = PROCESS_STATE_SUCCESS;
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (allPicRes_groupState != PROCESS_STATE_SUCCESS) { // 可能会多次调用, 只要成功过一次，就不显示加载失败了
                            allPicRes_groupState = PROCESS_STATE_FAILED;
                            LogUtil.e("图片资源分组失败 \n" + e.getCause());
                        }

                        for (Emitter<List<PicResGroup>> emitter : styleGroupQuery) {
                            emitter.onError(new Exception("图片资源分组失败 \n" + e.getCause()));
                        }
                        styleGroupQuery.clear();
                    }
                });
    }

    /**
     * @see #downLoadALLPicRes()
     */
    public static void queryAllPicRes(Emitter<List<PicResource>> emitter) {
        if (AllData.allPicRes_downloadState == AllData.PROCESS_STATE_SUCCESS) {
            emitter.onNext(allResList);
        } else {
            allResQuery.add(emitter);
            if (allPicRes_downloadState == PROCESS_STATE_NO_START || allPicRes_downloadState == PROCESS_STATE_FAILED) {
                downLoadALLPicRes();
            }
        }
    }

    /**
     * @see #downLoadALLPicRes()
     */
    public static void removeResQuery(Emitter<List<PicResource>> emitter) {
        allResQuery.remove(emitter);
    }

    /**
     * @see #downLoadALLPicRes()
     */
    public static void queryPicResGroup(Emitter<List<PicResGroup>> emitter, boolean isTietu) {
        if (AllData.allPicRes_groupState == AllData.PROCESS_STATE_SUCCESS) {
            emitter.onNext(styleGroupList);
        } else {
            styleGroupQuery.add(emitter);
            if (allPicRes_downloadState == PROCESS_STATE_NO_START || allPicRes_downloadState == PROCESS_STATE_FAILED) {
                downLoadALLPicRes();
            }
        }
    }

    /**
     * @see #downLoadALLPicRes()
     */
    public static void removePicResGroupQuery(Emitter<List<PicResGroup>> emitter, boolean isTietu) {
        if (AllData.allPicRes_groupState == AllData.PROCESS_STATE_SUCCESS) {
            if (isTietu) allResQuery.remove(emitter);
            else allResQuery.remove(emitter);
        }
    }

    /**
     * 确定数据已经获取完成的情况下，直接get
     */
    public static List<?> getGroupList(@NonNull String mFirstClass, @NonNull String mSecondClass) {
        List<PicResGroup> result = new ArrayList<>();
        for (PicResGroup picResGroup : styleGroupList) {
            PicResource pic = picResGroup.resList.get(0);
            if (mSecondClass.equals(pic.getCategory())) {
                result.add(picResGroup);
            }
        }
        return result;
    }
}
