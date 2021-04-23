package com.mandi.intelimeditor.home.tietuChoose;

import android.content.Context;
import android.text.TextUtils;

import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/02/01
 *      version : 1.0
 * <pre>
 */
public class PicResourcesPresenter implements TietuChooseContract.Presenter {
    private PicResourcesAdapter mPicResourceAdapter;
    private String TAG = "PicResourcesPresenter";

    private final Context mContext;
    private TietuChooseContract.View mView;

    private String mFirstClass;
    private String mSecondClass;
    private String mCategory;

    /**
     * 贴图分类是否加载成功，对于分类下的贴图，如果加载失败，用户点击分类可以重新加载，暂时不做判断
     * 另外，可能会多次获取资源，目前缓存一次，网络如果有更新一次，只要其中某次成功了，就算成功了
     * 就不加载了
     */
    private boolean mIsDownloadSuccess = false;

    /**
     * @param firstClass  一级分类
     * @param secondClass 二级分类
     * @param category    分组名
     */
    public PicResourcesPresenter(Context context, TietuChooseContract.View view, String firstClass, String secondClass, String category) {
        mContext = context;
        mView = view;
        mFirstClass = firstClass;
        mSecondClass = secondClass;
        mCategory = category;
    }

    /**
     * 开始加载数据
     * mCategory不为空，加载分组数据，否则加载模版等数据
     */
    @Override
    public void start() {
        if (TextUtils.isEmpty(mCategory)) {
            loadTietuByCategory(mSecondClass);
        } else {
            getTagPicListByCate(mCategory);
        }
    }

    @Override
    public void refresh() {
        if (PicResource.SECOND_CLASS_MY.equals(mSecondClass)) {
            loadTietuByCategory(mSecondClass);
        }
    }

    @NotNull
    @Override
    public PicResourcesAdapter createPicAdapter() {
        mPicResourceAdapter = new PicResourcesAdapter(mContext, mFirstClass);
        mPicResourceAdapter.initAdData(false);
        return mPicResourceAdapter;
    }

    @Override
    public void deleteOneMyTietu(@NotNull String path) {
        MyDatabase.getInstance().deleteMyTietu(path);
        if (PicResource.SECOND_CLASS_MY.equals(mSecondClass)) {
            mPicResourceAdapter.deleterecent_style(path);
            mPicResourceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadTietuByCategory(@NotNull String second_class) {
        LogUtil.d(TAG, "当前分类: loadTietuByCategory = " + " - " + second_class);
        Observable.create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
            PicResourceDownloader.queryPicResByCategory(mFirstClass, second_class, emitter);
        }).subscribe(new SimpleObserver<List<PicResource>>() {
            @Override
            public void onError(Throwable throwable) {
                LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                if (!mIsDownloadSuccess) { // 可能会多次调用, 只要成功过一次，就不显示加载失败了
                    mView.onDownloadStateChange(false, null);
                    LogUtil.e("下载贴图失败 \n" + throwable.getCause());
                }
            }

            @Override
            public void onNext(@NotNull List<PicResource> tietuMaterialList) {
                try {
                    LogUtil.d(TAG, "获取贴图成功 = " + " - " + tietuMaterialList.size());
//                Log.e(TAG, "onNext: test error");
                    mIsDownloadSuccess = true;
                    tietuMaterialList = new ArrayList<>(tietuMaterialList);
                    PicResourcesAdapter.randomInsertForHeat(tietuMaterialList);
                    mPicResourceAdapter.setImageUrls(tietuMaterialList, null);
                    mView.onDownloadStateChange(true, tietuMaterialList);
                } catch (Exception e) {
                    // 注意，这个方法比较关键，上面的代码出错， onError 不会调用，可能是目前对RxJava emitter的使用方式有问题
                    // 文档上面说只能同步使用
                    mView.onDownloadStateChange(false, null);
                    LogUtil.e("下载贴图失败 \n" + e.getCause());
                }
            }
        });
    }

    @Override
    public boolean isDownloadSuccess() {
        return mIsDownloadSuccess;
    }

    public void getTagsByCate(String secondClass) {
        List<PicResourceItemData> data = PicResourceDownloader.getTagsGroupByCategory(secondClass);
        mView.setCategoryList(data);
    }


    public void getTagPicListByCate(String category) {
        List<PicResourceItemData> data = PicResourceDownloader.getTagPicListByCate(category);
        mView.setCategoryList(data);
    }
}
