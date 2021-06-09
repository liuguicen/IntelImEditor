package com.mandi.intelimeditor.home.tietuChoose;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Emitter;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/02/01
 *      version : 1.0
 * <pre>
 */
public class PicResourcesPresenter implements TietuChooseContract.Presenter {
    private PicResourcesAdapter picResAdapter;
    private String TAG = "PicResourcesPresenter";

    private final Context mContext;
    private TietuChooseContract.View mView;

    private String firstClass;
    private String secondClass;

    /**
     * 贴图分类是否加载成功，对于分类下的贴图，如果加载失败，用户点击分类可以重新加载，暂时不做判断
     * 另外，可能会多次获取资源，目前缓存一次，网络如果有更新一次，只要其中某次成功了，就算成功了
     * 就不加载了
     */
    private boolean mIsDownloadSuccess = false;
    //排序方式，0:默认排序，按热度降序，1:按时间降序，4:分组
    /**
     * {@link com.mandi.intelimeditor.home.search.PicResSearchSortUtil#SORT_TYPE_HOT 等}
     */
    private int curSortType = 0;
    private boolean isStop = false;

    /**
     * @param firstClass  一级分类
     * @param secondClass 二级分类
     */
    public PicResourcesPresenter(Context context, TietuChooseContract.View view, String firstClass, String secondClass) {
        mContext = context;
        mView = view;
        this.firstClass = firstClass;
        this.secondClass = secondClass;
    }


    private List<PicResource> originList = new ArrayList<>(); // 里面采用的默认排序，和其它排序不太一样

    /**
     * 开始加载数据
     * mCategory不为空，加载分组数据，否则加载模版等数据
     */
    @Override
    public void start() {
        LogUtil.d(TAG, "当前分类: loadTietuByCategory = " + " - " + secondClass);
        AllData.queryAllPicRes(new Emitter<List<PicResource>>() {

            @Override
            public void onNext(@NonNull List<PicResource> resList) {
                try {
//                Log.e(TAG, "onNext: test error");
                    mIsDownloadSuccess = true;
                    originList = PicResSearchSortUtil.filter(resList, firstClass, secondClass);
                    LogUtil.d(TAG, "获取图片资源成功 = " + " - " + originList.size());

                    PicResourcesAdapter.randomInsertForHeat(originList);
                    picResAdapter.setImageUrls(originList, null);
                    mView.onDownloadStateChange(true, originList);
                } catch (Exception e) {
                    // 注意，这个方法比较关键，上面的代码出错， onError 不会调用，可能是目前对RxJava emitter的使用方式有问题
                    // 文档上面说只能同步使用
                    mView.onDownloadStateChange(false, null);
                    LogUtil.e("下载贴图失败 \n" + e.getCause());
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                if (!mIsDownloadSuccess) { // 可能会多次调用, 只要成功过一次，就不显示加载失败了
                    mView.onDownloadStateChange(false, null);
                    LogUtil.e("下载贴图失败 \n" + throwable.getCause());
                }
            }

            @Override
            public void onComplete() {

            }
        });
    }


    public void refreshPicList() {
        LogUtil.d(TAG, "排序方式 = " + curSortType);
        int sortType = getNextSorType(curSortType);
        refreshPicList(sortType, true);
    }

    /**
     * 下拉刷新更新图片列表
     */
    public void refreshPicList(int sortType, boolean isReduce) {
        curSortType = sortType;
        LogUtil.d(TAG, "排序方式 = " + curSortType);
        if (curSortType == PicResSearchSortUtil.SORT_TYPE_GROUP) {
            showGroup();
        } else {
            picResAdapter.sortPicList(originList, curSortType, isReduce);
        }
        //切换完成之后滚动顶部
        mView.afterSort(getNextSorType(sortType));
    }

    public void showGroup() {
        AllData.queryPicResGroup(new Emitter<List<PicResGroup>>() {
            @Override
            public void onNext(@NonNull List<PicResGroup> value) {
                if (!isStop) {
                    picResAdapter.setImageUrls(value, null);
                }
            }

            @Override
            public void onError(@NonNull Throwable error) {

            }

            @Override
            public void onComplete() {

            }
        }, PicResource.FIRST_CLASS_TIETU.equals(firstClass));
    }

    public void stop() {
        isStop = true;
    }

    private int getNextSorType(int sortType) {
        // 顺序不定，不好用取余
        switch (sortType) {
            case PicResSearchSortUtil.SORT_TYPE_HOT:
                return PicResSearchSortUtil.SORT_TYPE_TIME;
            case PicResSearchSortUtil.SORT_TYPE_TIME:
                return PicResSearchSortUtil.SORT_TYPE_GROUP;
            case PicResSearchSortUtil.SORT_TYPE_GROUP:
                return PicResSearchSortUtil.SORT_TYPE_HOT;
        }
        return sortType;
    }

    @NotNull
    @Override
    public PicResourcesAdapter createPicAdapter() {
        picResAdapter = new PicResourcesAdapter(mContext, 2);
        picResAdapter.setShowPreview(false);
        picResAdapter.initAdData(false);
        return picResAdapter;
    }

    @Override
    public void deleteOneMyTietu(@NotNull String path) {
        MyDatabase.getInstance().deleteMyTietu(path);
        if (PicResource.SECOND_CLASS_MY.equals(secondClass)) {
            picResAdapter.deleterecent_style(path);
            picResAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean isDownloadSuccess() {
        return mIsDownloadSuccess;
    }

    public List<PicResource> getOriginList() {
        return originList;
    }
}
