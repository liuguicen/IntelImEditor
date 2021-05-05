package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.LockUtil;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.BmobDatabaseUtil;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.network.NetWorkState;
import com.mandi.intelimeditor.user.US;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.SQLQueryListener;
import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;


/**
 * Created by LiuGuicen on 2017/2/21 0021.
 * 下载P图的图片资源的类
 * 一定注意本地文件和服务器URL的文件名相同，使用时一一对应，出错很麻烦
 * 这个类一开始封装解耦很不好，缓存管理的代码逻辑写到外面去了，而且到处写，教训啊
 * 自己以前都不会这样做的，软件工程的思想松懈了，忘记了，所以要定期的复习，要加深性的学习
 */
public class PicResourceDownloader {
    private static final int NUMBER_IN_PAGE = 500;
    private static String TAG = "PicResourceDownloader";
    /**
     * 缓存时间
     */
    public static final long CACHE_EXPIRE = TimeUnit.DAYS.toMillis(1);
    public static final int MAX_QUERY_NUMBER = 2000;

    /**
     * 根据类别查询图片资源列表
     *
     * @param secondClass 第二层类别，第一层类别现在不考虑，用第二层区分
     *                    如果为null，默认获取全部数据
     *                    否则获取具体类下数据
     * @param emitter     注意传回去的list是不可更改的
     */
    public static void queryPicResByCategory(final String firstClass, final String secondClass,
                                             @NotNull final ObservableEmitter<List<PicResource>> emitter) {

        LogUtil.d("queryTietuByCategory", "firstClass =" + firstClass + " secondClass=" + secondClass);
        AllData.queryAllPicRes(new Emitter<List<PicResource>>() {

            @Override
            public void onNext(@NonNull List<PicResource> resList) {
                try {
                    LogUtil.d(TAG, "获取贴图成功 = " + " - " + resList.size());
//                Log.e(TAG, "onNext: test error");
                    emitter.onNext(PicResSearchSortUtil.filter(resList, firstClass, secondClass));
                } catch (Exception e) {
                    // 注意，这个方法比较关键，上面的代码出错， onError 不会调用，可能是目前对RxJava emitter的使用方式有问题
                    // 文档上面说只能同步使用
                    emitter.onError(e);
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                LogUtil.d(TAG, "网络出错 = " + " - " + throwable.getMessage());
                emitter.onError(throwable);
            }

            @Override
            public void onComplete() {

            }
        });

    }
    // 参考代码，保留
//        //获取我的
//        if (PicResource.SECOND_CLASS_MY.equals(secondClass)) {
//            PicResourceDownloader.queryMyTietu(emitter);
//            return;
//        }
//        String sql = "select * from PicResource" +
//                " where category = '" + secondClass + "'" +
//                " order by heat desc" +
//                " limit " + NUMBER_IN_PAGE;
//        Observable.create((ObservableOnSubscribe<Long>)
//                emitter1 -> BmobDatabaseUtil.getServiceUpdateTime("PicResource",
//                        emitter1))
//                .subscribe(new SimpleObserver<Long>() {
//                    @Override
//                    public void onNext(Long serviceUpdateTime) {
//                        LogUtil.d("queryTietuByCategory", "sql =" + sql);
//                        BmobQuery<PicResource> query = new BmobQuery<>();
//                        query.setSQL(sql);
//                        // 如果有cache， 先从cache里面查询,然后再请求网络，然后再判断是否有更新，有更新的话再从网络查询
//                        // bmob官方有个CACHE_ELSE_NETWORK策略，不用自己写
//                        query.setMaxCacheAge(CACHE_EXPIRE);
//                        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
//                        query.doSQLQuery(
//                                new SQLQueryListener<PicResource>() {
//                                    @Override
//                                    public void done(BmobQueryResult<PicResource> result, BmobException e) {
//                                        if (e == null && result != null) { // 查询成功
//                                            LogUtil.d("queryTietuByCategory", "query pic resource list from cache success");
//                                            List<PicResource> resultList = result.getResults();
//                                            if (resultList == null)
//                                                resultList = new ArrayList<>();
//                                            if (resultList.size() > 10) { // 认为获取成功
//                                                AllData.allResList = resultList;
//                                            }
//                                            emitter.onNext(resultList);
//                                            // 主动从网络查询
////                                            long lastQueryTime = SPUtil.getQueryTimeOfTietuWithCategory(secondClass);
//                                            //  改为一天更新一次，因为热度变化，几乎每次这里都要刷新，这样不频繁刷新，
//                                            //  目前来说对图片列表实时刷新没有必要，隔段时间刷新一次就行了，这样也减少API调用量
////                                            if (serviceUpdateTime > lastQueryTime + RESOURCES_LIST_REFRESH_INTERVAL) {
////                                                queryFromNet(sql, emitter, firstClass, secondClass);
////                                            }
//                                        } else { // 获取失败，需要调用从网络查询
//                                            if (e != null) {
//                                                LogUtil.d("queryTietuByCategory", "query pic resource list from cache failed " + e.getMessage());
//                                                e.printStackTrace();
//                                            }
//                                            queryFromNet(sql, emitter, firstClass, secondClass);
//                                        }
//                                    }
//                                });
//                    }
//                });
//    }


    /**
     * 从网络查询
     */
    private static void queryFromNet(String sql, ObservableEmitter<List<PicResource>> emitter,
                                     String firstClass, String second_class) {
        BmobQuery<PicResource> query = new BmobQuery<>();
        query.setSQL(sql);
        query.setMaxCacheAge(CACHE_EXPIRE);
        query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        query.doSQLQuery(
                new SQLQueryListener<PicResource>() {
                    @Override
                    public void done(BmobQueryResult<PicResource> result, BmobException e) {
                        if (e != null || result == null) {
                            String eMsg = "获取图片资源分类列表下出错";
                            if (e != null) {
                                eMsg += ": " + e.getErrorCode() + " : " + e.getMessage();
                            }
                            LogUtil.d("query pic resource list from network failed " + eMsg);
                            US.putPicResourcesDownloadEvent(US.TIETU_DOWNLOAD_FAILED,
                                    e != null ? e.getErrorCode() : 0);
                            MobclickAgent.reportError(IntelImEditApplication.appContext,
                                    eMsg);

                            emitter.onError(new Exception(eMsg));

                        } else {
                            List<PicResource> resultList = result.getResults();
                            // 将要锁定的项存下来
                            if (resultList == null)
                                resultList = new ArrayList<>();
                            LockUtil.updateUnlockIfNeeded( resultList);
                            emitter.onNext(resultList);
//                            emitter.onComplete();
                            SPUtil.putQueryTimeOfTietuWithCategory(System.currentTimeMillis(), second_class);
                            LogUtil.d("query pic resource list from network success!");
                        }
                    }
                });

    }

    /**
     * 目前贴图热度会更新了，也就是说基本上就是每次启动应用服务器更新时间都会比上次查询列表时间大，都不能使用cache，
     * 要重新查询列表
     *
     * @param t_class 和query的类必须一致
     * @return 是否使用了cache
     */
    private static boolean setCachePolicy(BmobQuery<?> query, Class t_class,
                                          long lastQueryTime, long serviceTietuUpdateTime) {
        query.setMaxCacheAge(CACHE_EXPIRE);
        boolean isUseCache = lastQueryTime >= serviceTietuUpdateTime;
        boolean hasUsedCache = false;
        boolean hasCache = query.hasCachedResult(t_class);
        if (isUseCache && hasCache) {
            LogUtil.d("use cache");
            if (NetWorkState.detectNetworkType() == NetWorkState.NO_NET) {
                query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ONLY);  // 断网时，只能使用CACHE_ONLY，不然会报错
            } else {
                query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
            }
            hasUsedCache = true;
        } else {
            LogUtil.d("not use cache");
            query.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ELSE_CACHE);
        }

        return hasUsedCache;
    }

    /**
     * @return emitter 是否 调用过出错
     */
    public static void queryMyTietu(ObservableEmitter<List<PicResource>> emitter) {
        MyDatabase myDb = MyDatabase.getInstance();
        ArrayList<String> myTietuPathList = new ArrayList<>();
        myDb.queryAllMyTietu(myTietuPathList);
        List<PicResource> picResourceList = new ArrayList<>();
        for (String s : myTietuPathList) {
            PicResource PicResource = new PicResource();
            BmobFile bmobFile = new BmobFile();
            bmobFile.setUrl(s);
            PicResource.setUrl(bmobFile);
            picResourceList.add(PicResource);
        }
        emitter.onNext(picResourceList);
        emitter.onComplete();
    }

    public static void queryAllPicRes(@NotNull final ObservableEmitter<List<PicResource>> emitter) {
        // 首先查询行数 也就是图片数量
        BmobQuery<PicResource> query = new BmobQuery<>();
        query.setMaxCacheAge(CACHE_EXPIRE);
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        Log.d(TAG, "查询数量，bmob缓存情况 = " + query.hasCachedResult(PicResource.class));
        query.count(PicResource.class, new CountListener() {

            @Override
            public void done(Integer total, BmobException e) {
                if (e == null) {
                    if (total != null) {//开发者需要根据返回结果自行解析数据
                        queryOnePage(total, 0, emitter);
                    } else {
                        Log.e(TAG, "bmob查询图片张数成功，无数据");
                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                }
            }
        });
    }

    private static void queryOnePage(int totalCount, int start,
                                     @NotNull final ObservableEmitter<List<PicResource>> emitter) {
        int end = start + NUMBER_IN_PAGE;
        String sql = "select * from PicResource" +
                " order by heat desc" +
                " limit " + start + " , " + end;

        LogUtil.d("queryOnePage", "sql =" + sql);
        BmobQuery<PicResource> query = new BmobQuery<>();
        query.setSQL(sql);
        // 如果有cache， 先从cache里面查询,然后再请求网络，然后再判断是否有更新，有更新的话再从网络查询
        // bmob官方有个CACHE_ELSE_NETWORK策略，不用自己写
        query.setMaxCacheAge(CACHE_EXPIRE);
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.doSQLQuery(
                new SQLQueryListener<PicResource>() {
                    @Override
                    public void done(BmobQueryResult<PicResource> result, BmobException e) {
                        if (e == null && result != null) { // 查询成功
                            LogUtil.d("queryOnePage", "query pic resource list from cache success");
                            List<PicResource> resultList = result.getResults();
                            if (resultList == null)
                                resultList = new ArrayList<>();
                            emitter.onNext(resultList);
                            if (end < MAX_QUERY_NUMBER && end < totalCount) {
                                queryOnePage(totalCount, end, emitter);
                            } else {
                                emitter.onComplete();
                            }
                            // 主动从网络查询
//                                            long lastQueryTime = SPUtil.getQueryTimeOfTietuWithCategory(secondClass);
                            //  改为一天更新一次，因为热度变化，几乎每次这里都要刷新，这样不频繁刷新，
                            //  目前来说对图片列表实时刷新没有必要，隔段时间刷新一次就行了，这样也减少API调用量
//                                            if (serviceUpdateTime > lastQueryTime + RESOURCES_LIST_REFRESH_INTERVAL) {
//                                                queryFromNet(sql, emitter, firstClass, secondClass);
//                                            }
                        } else { // 获取失败，需要调用从网络查询
                            if (e != null) {
                                LogUtil.d("queryOnePage", "分页查询失败 start = " + start + " end =" + end + e.getMessage());
                            }
                            emitter.onError(new Exception("分页查询失败 start = " + start + " end =" + end +
                                    (e != null ? e.getMessage() : "")));
                        }
                    }
                });
    }
}
