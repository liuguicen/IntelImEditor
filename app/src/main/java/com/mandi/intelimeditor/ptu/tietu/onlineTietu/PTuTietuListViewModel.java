package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mandi.intelimeditor.R;
import com.mandi.intelimeditor.common.Constants.EventBusConstants;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.dialog.BottomTietuListDialog;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;

/**
 * 图片ViewModel，负责获取数据，更新数据
 */
public class PTuTietuListViewModel extends ViewModel {
    private String TAG = "PicResourceViewModel";
    private MutableLiveData<List<PicResourceItemData>> picResList;
    private MutableLiveData<List<PicResGroup>> groupList;
    public MutableLiveData<String> loadStatus = new MutableLiveData<>();

    /**
     * 获取图片资源LiveData
     */
    public LiveData<List<PicResourceItemData>> getPicResources() {
        if (picResList == null) {
            picResList = new MutableLiveData<>();
        }
        return picResList;
    }

    /**
     * 获取分组列表LiveData
     */
    public LiveData<List<PicResGroup>> getGroupList() {
        if (groupList == null) {
            groupList = new MutableLiveData<>();
            loadGroupList();
        }
        return groupList;
    }

    /**
     * 获取分组列表
     */
    public void loadGroupList() {
        LogUtil.d(TAG, "loadPicTagList");
        AllData.queryPicResGroup(new Emitter<List<PicResGroup>>() {
            @Override
            public void onNext(@NonNull List<PicResGroup> value) {
                LogUtil.d("加载贴图分组完成");
                groupList.postValue(AllData.tieTuGroupList);
            }

            @Override
            public void onError(@NonNull Throwable error) {
                LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + error.getMessage());
                loadStatus.postValue("图片加载失败，网络或服务器异常");
            }

            @Override
            public void onComplete() {

            }
        }, true);
    }

    public void loadOtherGroup(String title) {
        Observable
                .create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
                    if (BottomTietuListDialog.TITLE_MY.equals(title)) {
                        PicResourceDownloader.queryMyTietu(emitter);
                    } else if (BottomTietuListDialog.TITLE_HOTEST.equals(title)) {
                        PicResSearchSortUtil.getHotestTietuList(emitter);
                    } else if (BottomTietuListDialog.TITLE_NEWEST.equals(title)) {
                        PicResSearchSortUtil.getNewestTietuList(emitter);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new SimpleObserver<List<PicResource>>() {
                            @Override
                            public void onNext(@NonNull List<PicResource> picResources) {
                                picResList.postValue(PicResourceItemData.picResList2PicResItemList(picResources));
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                String msg = BottomTietuListDialog.TITLE_MY.equals(title)
                                        ? "暂无使用过的贴图"
                                        : IntelImEditApplication.appContext.getString(R.string.network_error_try_latter);
                                loadStatus.postValue(msg);
                            }
                        }
                );
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 100)
    public void onEventMainThread(Integer event) {
        if (EventBusConstants.DOWNLOAD_ALL_PIC_RES_FINISH.equals(event)) {
            Log.d(TAG, "通过EventBus 更新图片资源");
            EventBus.getDefault().unregister(this);
            groupList.postValue(AllData.tieTuGroupList);
        }
    }
}
