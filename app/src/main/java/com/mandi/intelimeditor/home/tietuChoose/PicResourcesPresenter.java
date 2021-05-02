package com.mandi.intelimeditor.home.tietuChoose;

import android.content.Context;
import android.os.Environment;

import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceDownloader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
    }

    @Override
    public void loadData() {
        // 如果在注册之前完成下载，那么不会收到事件
        EventBus.getDefault().register(this);
        // 如果在注册之后的这里完成下载，那么会收到事件，会调用显示方法两次，但是比放到if判断后面有可能漏掉事件好
        if (AllData.allPicRes_downloadState == AllData.DOWNLOAD_STATE_SUCCESS) {
            showResListByHot();
        }
    }

    private void showResListByHot() {
        ArrayList<PicResource> styleList = new ArrayList<>(AllData.allStyleList);
        PicResourcesAdapter.randomInsertForHeat(styleList);
        mPicResourceAdapter.setImageUrls(styleList, null);
        mView.onDownloadStateChange(true, styleList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        if (EventBusConstants.DOWNLOAD_ALL_PIC_RES_FINISH.equals(event)) {
            showResListByHot();
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
        mPicResourceAdapter = new PicResourcesAdapter(mContext, 1);
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

                    String thePath = Environment.getExternalStorageDirectory().toString();
                    PicResource p1 = PicResource.path2PicResource(thePath + File.separator + "test1.jpg");
                    p1.setHeat(1000);
                    p1.setTag("梵高 星空");
                    tietuMaterialList.add(p1);
                    PicResource p2 = PicResource.path2PicResource(thePath + File.separator + "test2.jpg");
                    p1.setHeat(100);
                    p1.setTag("动漫 新海诚");
                    tietuMaterialList.add(p2);
                    tietuMaterialList.add(PicResource.path2PicResource(thePath + File.separator + "test3.jpg"));
                    tietuMaterialList.add(PicResource.path2PicResource(thePath + File.separator + "test4.jpg"));
                    tietuMaterialList.add(PicResource.path2PicResource(thePath + File.separator + "test5.jpg"));

                    PicResourcesAdapter.randomInsertForHeat(tietuMaterialList);
                    mPicResourceAdapter.setImageUrls(tietuMaterialList, null);
                    mView.onDownloadStateChange(true, tietuMaterialList);
                } catch (Exception e) {
                    // 注意，这个方法比较关键，上面的代码出错， onError 不会调用，可能是目前对RxJava emitter的使用方式有问题
                    // 文档上面说只能同步使用
                    mView.onDownloadStateChange(false, null);
                    e.printStackTrace();
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
