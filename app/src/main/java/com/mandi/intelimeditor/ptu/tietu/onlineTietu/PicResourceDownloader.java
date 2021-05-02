package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.text.TextUtils;
import android.util.Log;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.BmobDatabaseUtil;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.network.NetWorkState;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.bean.GroupBean;
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
import cn.bmob.v3.listener.SQLQueryListener;
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
    private static String TAG = "PicResourceDownloader";
    /**
     * 缓存时间
     */
    public static final long CACHE_EXPIRE = TimeUnit.DAYS.toMillis(1) * 100;
    static final int PRIOR_TIETU_NUMBER = 1000;
    // 图片资源列表的刷新时间间隔
    private static final long RESOURCES_LIST_REFRESH_INTERVAL = 24 * 3600 * 1000;

    /**
     * 根据类别查询图片资源列表
     * @param secondClass 第二层类别，第一层类别现在不考虑，用第二层区分
     *                    如果为null，默认获取全部数据
     *                    否则获取具体类下数据
     * @param emitter     注意传回去的list是不可更改的
     */
    public static void queryPicResByCategory(final String firstClass, final String secondClass,
                                             @NotNull final ObservableEmitter<List<PicResource>> emitter) {
        LogUtil.d("queryTietuByCategory", "firstClass =" + firstClass + " secondClass=" + secondClass);
        //获取我的
        if (PicResource.SECOND_CLASS_MY.equals(secondClass)) {
            PicResourceDownloader.queryMyTietu(emitter);
            return;
        }
        //直接从全局数据获取
        List<PicResource> picResources = queryFormGlobalData(secondClass);
        if (picResources != null) {
            emitter.onNext(picResources);
//            emitter.onComplete();
            return;
        }
        String sql = "select * from PicResource" +
                " where category = '" + secondClass + "'" +
                " order by heat desc" +
                " limit " + PRIOR_TIETU_NUMBER;
        //默认查询所有的
        if (PicResource.ALL_STICKER_LIST.equals(secondClass)) {
            //分类不为空，则查询分类中的数据
            sql = "select * from PicResource" +
                    " where category != '" + PicResource.SECOND_CLASS_BASE + "'" +
                    " order by heat desc" +
                    " limit " + PRIOR_TIETU_NUMBER;
        }
        String finalSql = sql;
        Observable.create((ObservableOnSubscribe<Long>)
                emitter1 -> BmobDatabaseUtil.getServiceUpdateTime("PicResource",
                        emitter1))
                .subscribe(new SimpleObserver<Long>() {
                    @Override
                    public void onNext(Long serviceUpdateTime) {
                        LogUtil.d("queryTietuByCategory", "sql =" + finalSql);
                        BmobQuery<PicResource> query = new BmobQuery<>();
                        query.setSQL(finalSql);
                        // 如果有cache， 先从cache里面查询,然后再请求网络，然后再判断是否有更新，有更新的话再从网络查询
                        //bmob官方有个CACHE_ELSE_NETWORK策略，不用自己写。缓存时间时间为1天
                        query.setMaxCacheAge(RESOURCES_LIST_REFRESH_INTERVAL);
                        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_ELSE_NETWORK);
                        query.doSQLQuery(
                                new SQLQueryListener<PicResource>() {
                                    @Override
                                    public void done(BmobQueryResult<PicResource> result, BmobException e) {
                                        if (e == null && result != null) { // 查询成功
                                            LogUtil.d("queryTietuByCategory", "query pic resource list from cache success");
                                            List<PicResource> resultList = result.getResults();
                                            if (resultList == null)
                                                resultList = new ArrayList<>();
                                            if (resultList.size() > 10) { // 认为获取成功
                                                if (PicResource.SECOND_CLASS_EXPRESSION.equals(secondClass)) {
                                                    AllData.expressResList = resultList;
                                                } else if (PicResource.SECOND_CLASS_PROPERTY.equals(secondClass)) {
                                                    AllData.propertyResList = resultList;
                                                } else if (PicResource.SECOND_CLASS_BASE.equals(secondClass)) {
                                                    AllData.templateResList = resultList;
                                                } else if (PicResource.ALL_STICKER_LIST.equals(secondClass)) {
                                                    AllData.allResList = resultList;
                                                }
                                            }
                                            groupByTag(secondClass, resultList);
                                            emitter.onNext(resultList); // 更新视图
                                            long lastQueryTime = SPUtil.getQueryTimeOfTietuWithCategory(secondClass);
                                            // 要更新
                                            //  改为一天更新一次，因为热度变化，几乎每次这里都要刷新，这样不频繁刷新，
                                            //  目前来说对图片列表实时刷新没有必要，隔段时间刷新一次就行了，这样也减少API调用量
                                            if (serviceUpdateTime > lastQueryTime + RESOURCES_LIST_REFRESH_INTERVAL) {
                                                queryFromNet(finalSql, emitter, firstClass, secondClass);
                                            }
                                        } else { // 获取失败，需要调用从网络查询
                                            LogUtil.d("queryTietuByCategory", "query pic resource list from cache failed " + e.getMessage());
                                            queryFromNet(finalSql, emitter, firstClass, secondClass);
                                        }
                                    }
                                });
                    }
                });
    }


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
                            TTRewardVad.updateUnlockIfNeeded(firstClass, second_class, resultList);
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
        ArrayList<String> myTietuPathList = new ArrayList<>();
        MyDatabase.getInstance().queryAllMyTietu(myTietuPathList);
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

    /**
     * 从本地缓存中获取图片列表数据
     *
     * @param categoryName 分类名，如果为空，则获取全部的数据，否则获取对应分类下的数据
     * @return
     */
    public static List<PicResource> queryFormGlobalData(String categoryName) {
        if (AllData.expressResList != null && PicResource.SECOND_CLASS_EXPRESSION.equals(categoryName)) {
            return AllData.expressResList;
        } else if (AllData.propertyResList != null && PicResource.SECOND_CLASS_PROPERTY.equals(categoryName)) {
            return AllData.propertyResList;
        } else if (AllData.templateResList != null && PicResource.SECOND_CLASS_BASE.equals(categoryName)) {
            return AllData.templateResList;
        } else if (PicResource.ALL_STICKER_LIST.equals(categoryName) && AllData.allResList != null) {
            //获取所有的
            return AllData.allResList;
        }
        return null;
    }

    /**
     * 根据标签将图片列表分组
     */
    public static List<PicResourceItemData> groupByTag(String secondClass, List<PicResource> data) {
        long start = System.currentTimeMillis();
        if (data == null) return new ArrayList<>();
        Map<String, List<PicResource>> map = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            PicResource picRes = data.get(i);

            if (picRes.getTag() == null) {
                LogUtil.d(TAG, "updateAllTagAndGroup 数据异常" + picRes.toString());
                continue;
            }

            try {
                String[] split_tags = picRes.getTag().split("-");
                for (String tag : split_tags) {
                    if (TextUtils.isEmpty(tag)) continue;

                    List<PicResource> picResources = map.get(tag);
                    if (picResources == null) {
                        picResources = new ArrayList<>();
                    }
                    picResources.add(picRes);
                    map.put(tag, picResources);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //保存分组标题
        List<String> titles = new ArrayList<>(map.keySet());
        //保存分组数据
        List<List<PicResource>> groupList = new ArrayList<>(map.values());

        List<PicResourceItemData> dataList = new ArrayList<>();
        List<GroupBean> cacheTitles = new ArrayList<>();
        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            List<PicResource> picResList = groupList.get(i);

            if (picResList != null && picResList.size() >= 6) {
                PicResourceItemData itemData = new PicResourceItemData(title, false);
                convertPicResList2PicItemList(picResList, itemData);
                dataList.add(itemData);
                //将分组添加到内存变量中
                cacheTitles.add(new GroupBean(title, picResList));
            }
            // TODO: 2020/10/23 不要这样搞，共用！！！
            //保存分组对应数据, 这个给选图图下面的贴图？？？
            AllData.mAllCategoryList.put(title, picResList);
        }
        // 这个给P图下面的贴图列表的？？？
        AllData.mAllGroupList.put(secondClass, cacheTitles);

        Log.d(TAG, "updateAllTagAndGroup: 分类用时 = " + (System.currentTimeMillis() - start));
        return dataList;
    }


    /**
     * 更新分组数据
     *
     * @param data     分组内图片集合
     * @param itemData 分组
     */
    public static void convertPicResList2PicItemList
    (List<PicResource> data, PicResourceItemData itemData) {
        List<PicResourceItemData> itemDataList = new ArrayList<>();
        int groupHeat = 0;
        Date date = null;
        for (int i = 0; i < data.size(); i++) {
            PicResourceItemData item = new PicResourceItemData(data.get(i), PicResourceItemData.PicListItemType.ITEM);
            setLockData(item);
            groupHeat += data.get(i).getHeat();
            if (data.get(i).getCreatedAt() != null) {
                try {
                    if (date == null) {
                        date = AllData.bmobDataParser.parse(data.get(i).getCreatedAt());
                    } else {
                        Date tmp = AllData.bmobDataParser.parse(data.get(i).getCreatedAt());
                        if (date.compareTo(tmp) >= 0) {
                            date = tmp;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            itemDataList.add(item);
        }
        itemData.picResListInGroup = itemDataList;
        itemData.groupHeat = groupHeat;
        itemData.groupCreateTime = date;
    }

    /**
     * 根据分类获取分类下的所有分组列表(标题+图片)
     */
    public static List<PicResourceItemData> getTagsGroupByCategory(String secondClass) {
        List<PicResource> picResources = queryFormGlobalData(secondClass);
        return groupByTag(secondClass, picResources);
    }

    /**
     * 根据分类获取分类下的所有分组列表(标题)
     * 1、先获取分类数据
     * 2、根据分类数据获取分组列表
     */
    public static void queryTietuList(@NotNull final ObservableEmitter<List<PicResource>> emitter1) {
        Observable<List<PicResource>> observable2 = Observable.create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
            PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TEMPLATE, PicResource.ALL_STICKER_LIST, emitter);
        });
        observable2.subscribe(new SimpleObserver<List<PicResource>>() {
            @Override
            public void onError(Throwable throwable) {
                LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                emitter1.onError(throwable);
            }

            @Override
            public void onNext(List<PicResource> picResources) {
                LogUtil.d(TAG, "获取贴图成功 = " + " - " + picResources.size());
                emitter1.onNext(picResources);
            }
        });
    }

    /**
     * 根据分类获取分类下的所有分组列表(标题)
     * 1、先获取分类数据
     * 2、根据分类数据获取分组列表
     */
    public static void queryTietuTagList(@NotNull final ObservableEmitter<List<GroupBean>> emitter1) {
        Observable<List<PicResource>> observable2 = Observable.create(emitter -> {
            PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TEMPLATE, PicResource.ALL_STICKER_LIST, emitter);
        });
        observable2.subscribe(new SimpleObserver<List<PicResource>>() {
            @Override
            public void onError(Throwable throwable) {
                LogUtil.d(TAG, "queryTietuTagList 失败= " + " - " + throwable.getMessage());
                emitter1.onError(throwable);
            }

            @Override
            public void onNext(List<PicResource> picResources) {
                if (picResources != null) {
                    LogUtil.d(TAG, "queryTietuTagList 获取贴图成功 = " + picResources.size());
                    PicResourceDownloader.groupByTag(PicResource.ALL_STICKER_LIST, picResources);
                    List<GroupBean> data = AllData.mAllGroupList.get(PicResource.ALL_STICKER_LIST);
                    //分组排序
                    Collections.sort(data, new Comparator<GroupBean>() {
                        @Override
                        public int compare(GroupBean o1, GroupBean o2) {
                            return o2.getHeat() - o1.getHeat();
                        }
                    });
                    emitter1.onNext(data);
                    emitter1.onComplete();
                }
            }
        });
    }

    /**
     * 根据分类获取分类下的所有分组列表(图片)
     */
    public static List<PicResourceItemData> getTagPicListByCate(String category) {
        List<PicResource> list = AllData.mAllCategoryList.get(category);
        List<PicResourceItemData> dataList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            PicResourceItemData itemData = new PicResourceItemData(list.get(i), PicResourceItemData.PicListItemType.ITEM);
            setLockData(itemData);
            dataList.add(itemData);
        }
        return dataList;
    }

    public static String getTietuNameByFileName(String fileName) {
        int id = fileName.indexOf("_");
        if (id == -1) id = 0;
        return fileName.substring(id + 1);
    }

    private static void setLockData(PicResourceItemData item) {
        if (item.data == null) return;
        // 要解锁的
        item.isUnlock = true;
        String key = String.valueOf(item.data.getUrl().getUrl().hashCode());
        if (LogUtil.debugRewardAd) {
            Log.d("PicResourcesAdapter", "key = " + key);
        }
        if (AdData.sUnlockData.get(key) != null) {
            item.isUnlock = AdData.sUnlockData.get(key);
        }
    }

    public static void onlyDownLoadResToCache(String firstClass, String secondClass) {
        Observable.create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
            PicResourceDownloader.queryPicResByCategory(firstClass, secondClass, emitter); // 原来的代码必须传入这个参数，目前先这样做
        }).subscribe(new SimpleObserver<List<PicResource>>() {

            @Override
            public void onNext(List<PicResource> resourceList) {
                // nothing
            }
        });
    }
}
