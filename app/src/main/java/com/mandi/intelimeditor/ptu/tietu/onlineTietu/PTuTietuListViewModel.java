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

import java.util.ArrayList;
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
    private MutableLiveData<List<PicResourceItemData>> picResList; // 为了让所有Fragement共用这个list
    private MutableLiveData<List<PicResGroup>> groupList;
    public MutableLiveData<String> loadStatus = new MutableLiveData<>();

    public PTuTietuListViewModel() {
        if (LogUtil.debugPtuTietuList)
            LogUtil.logTimeConsume("创建P图贴图列表ViewModel");
    }

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
        LogUtil.logTimeConsume("开始加载分组列表");
        AllData.queryPicResGroup(new Emitter<List<PicResGroup>>() {
            @Override
            public void onNext(@NonNull List<PicResGroup> value) {
                LogUtil.logTimeConsume("加载贴图分组完成");
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
        if (LogUtil.debugPtuTietuList)
            LogUtil.logTimeConsume("加载其它分组");
//        Observable
//                .create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
        Emitter<List<PicResource>> emitter = new Emitter<List<PicResource>>() {
            @Override
            public void onNext(@NonNull List<PicResource> picResources) {
                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("开始转换最热分组数据");
                List<PicResourceItemData> value = PicResourceItemData.picResList2PicResItemList(picResources);
                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("转换最热分组数据完成");
                picResList.postValue(value);
                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("加载其它分组  " + title + " 完成");
            }

            @Override
            public void onError(Throwable e) {
                String msg = BottomTietuListDialog.TITLE_MY.equals(title)
                        ? "暂无使用过的贴图"
                        : IntelImEditApplication.appContext.getString(R.string.network_error_try_latter);
                loadStatus.postValue(msg);
            }

            @Override
            public void onComplete() {

            }
        };
        if (BottomTietuListDialog.TITLE_MY.equals(title)) {
            PicResourceDownloader.queryMyTietu(emitter);
        } else if (BottomTietuListDialog.TITLE_HOTEST.equals(title)) {
            LogUtil.logTimeConsume("开始获取最热分组");
            PicResSearchSortUtil.getHotestTietuList(emitter);
            LogUtil.logTimeConsume("获取最热分组完成");
        } else if (BottomTietuListDialog.TITLE_NEWEST.equals(title)) {
            LogUtil.logTimeConsume("开始获取最新分组");
            PicResSearchSortUtil.getNewestTietuList(emitter);
            LogUtil.logTimeConsume("获取最新分组完成");
        } else {
            emitter.onNext(new ArrayList<PicResource>());
        }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(

//                );
    }
}
