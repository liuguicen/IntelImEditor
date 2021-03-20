package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.bean.GroupBean;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

/**
 * 图片ViewModel，负责获取数据，更新数据
 */
public class PicResourceViewModel extends ViewModel {
    private String TAG = "PicResourceViewModel";
    private MutableLiveData<List<PicResourceItemData>> picResources;
    private MutableLiveData<List<GroupBean>> tagList;
    public MutableLiveData<String> loadStatus = new MutableLiveData<>();

    /**
     * 获取图片资源LiveData
     */
    public LiveData<List<PicResourceItemData>> getPicResources() {
        if (picResources == null) {
            picResources = new MutableLiveData<>();
        }
        return picResources;
    }

    /**
     * 获取分组列表LiveData
     */
    public LiveData<List<GroupBean>> getTagList() {
        if (tagList == null) {
            tagList = new MutableLiveData<>();
            loadPicTagList();
        }
        return tagList;
    }

    public void loadPicResources(String sortType) {
        String secondClass = sortType;
        if (!PicResource.SECOND_CLASS_MY.equals(sortType)) {
            secondClass = PicResource.ALL_STICKER_LIST;
        }
        String finalSecondClass = secondClass;
        Observable.create((ObservableOnSubscribe<List<PicResource>>) emitter -> {
            PicResourceDownloader.queryPicResByCategory(PicResource.FIRST_CLASS_TIETU, finalSecondClass, emitter);
        }).map(new Function<List<PicResource>, List<PicResourceItemData>>() {
            /**
             * 类型转换
             */
            @Override
            public List<PicResourceItemData> apply(List<PicResource> picResources) throws Exception {
                List<PicResourceItemData> dataList = new ArrayList<>();
                for (int i = 0; i < picResources.size(); i++) {
                    dataList.add(new PicResourceItemData(picResources.get(i), PicResourceItemData.PicListItemType.ITEM));
                }
                if (PicResource.PIC_STICKER_LATEST_LIST.equals(sortType)) {
                    Collections.sort(dataList, new Comparator<PicResourceItemData>() {
                        @Override
                        public int compare(PicResourceItemData o1, PicResourceItemData o2) {
                            try {
                              Date  date1 = AllData.bmobDataParser.parse(o1.data.getCreatedAt());
                              Date  date2 = AllData.bmobDataParser.parse(o2.data.getCreatedAt());
                              return date2.compareTo(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 1;
                            }
                        }
                    });
                }
                return dataList;
            }
        }).subscribe(new SimpleObserver<List<PicResourceItemData>>() {
            @Override
            public void onError(Throwable throwable) {
                LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                loadStatus.postValue("图片加载失败，网络或服务器异常");
            }

            @Override
            public void onNext(List<PicResourceItemData> tietuMaterialList) {
                LogUtil.d(TAG, "获取贴图成功 = " + " - " + tietuMaterialList.size());
                picResources.postValue(tietuMaterialList);
            }
        });
    }

    /**
     * 获取分组列表
     */
    public void loadPicTagList() {
        LogUtil.d(TAG, "loadPicTagList");
        List<GroupBean> data = AllData.mAllGroupList.get(PicResource.ALL_STICKER_LIST);
        if (data != null && data.size() > 0) {
            tagList.postValue(data);
        } else {
            Observable.create(PicResourceDownloader::queryTietuTagList).subscribe(new SimpleObserver<List<GroupBean>>() {
                @Override
                public void onError(Throwable throwable) {
                    LogUtil.d(TAG, "网络出错，不能获取贴图 = " + " - " + throwable.getMessage());
                }

                @Override
                public void onNext(List<GroupBean> tietuMaterialList) {
                    LogUtil.d(TAG, "获取贴图分组成功 = " + " - " + tietuMaterialList.size());
                    tagList.postValue(tietuMaterialList);
                }
            });
        }
    }

}
