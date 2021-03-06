package com.mandi.intelimeditor.home.localPictuture;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.home.ChoosePicContract;
import com.mandi.intelimeditor.home.data.MediaInfoScanner;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.home.data.PicDirInfoManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by LiuGuicen on 2017/1/17 0017.
 * 本地图片的路径数据是一开始就加载，并在整个应用周期中保活的，因为很多地方用到
 */
public class LocalPicPresenter implements ChoosePicContract.PicPresenter {
    private final String TAG = "LocalPicPresenter";
    private ChoosePicContract.View mView;
    private Context mContext;
    /**
     * 当前要显示的所有图片的路径
     */
    private List<String> currentPicPathList;
    private LocalPicAdapter picAdapter;

    private final PicDirInfoManager picDirInfoManager;
    private List<String> picPathInFile;
    private MyFileListAdapter fileInfosAdapter;
    private String latestPic;
    //    private final ListAdPool mAdPool;

    public LocalPicPresenter(ChoosePicContract.View view, Context context) {
        this.mView = view;
        mContext = context;

        picDirInfoManager = PicDirInfoManager.INSTANCE;
        picPathInFile = new ArrayList<>();
        //        mAdPool = new ListAdPool(mContext);
    }

    /**
     * 获取所有图片的文件信息，最近的图片，
     * <p>并且为图片grid，文件列表加载数据
     */
    @Override
    public void start() {
        AllData.queryLocalPicList(new Emitter<String>() {

            @Override
            public void onNext(@NonNull String value) {
                initSetAndShowPicList();
                mView.dismissLoading();
            }

            @Override
            public void onError(@NonNull Throwable error) {
                ToastUtils.show("扫描图片出错了");
                mView.dismissLoading();
            }

            @Override
            public void onComplete() {
                mView.dismissLoading();
            }
        });
    }

    private void initSetAndShowPicList() {
        AllData.usuManager.updateRecentInfoInUsu(AllData.sMediaInfoScanner.getSortedPicPathsByTime());
        currentPicPathList = AllData.usuManager.getUsuPaths();
        Log.d(TAG, "currentPicPathList:" + currentPicPathList.size());
        picAdapter.setImageUrls(currentPicPathList, true);
        mView.showPicList();

        AllData.sMediaInfoScanner.updateAllFileInfo(picDirInfoManager, AllData.usuManager);
        mView.initFileInfoViewData();

        LogUtil.d(TAG, "初始化显示图片完成");
    }

    @Override
    public void detectAndUpdateInfo() {
        Observable.create(
                new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        if (AllData.sMediaInfoScanner.scanAndUpdatePicInfo()) {
                            emitter.onNext(1);//更新图片
                            emitter.onNext(2);//更新文件信息
                            emitter.onComplete();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Integer, MediaInfoScanner.PicUpdateType>() {
                    @Override
                    public MediaInfoScanner.PicUpdateType apply(Integer type) throws Exception {
                        if (type == 1) {
                            Log.d(TAG, "call: 进行图片更新了");
                            AllData.usuManager.updateRecentInfoInUsu(AllData.sMediaInfoScanner.getSortedPicPathsByTime());
                            if (latestPic != null && !AllData.usuManager.hasRecentPic(latestPic) &&
                                    !AllData.usuManager.getUsuPaths().contains(latestPic))//解决最新添加的图片扫描不到的问题，手动添加
                                AllData.usuManager.addRecentPathStart(latestPic);
                            return MediaInfoScanner.PicUpdateType.CHANGE_ALL_PIC;
                        } else {
                            LogUtil.d("更新图片文件信息");
                            return AllData.sMediaInfoScanner.updateAllFileInfo(picDirInfoManager, AllData.usuManager);
                        }
                    }
                })
                .subscribe(new Observer<MediaInfoScanner.PicUpdateType>() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LogUtil.e("onError: 跑出了错误", throwable.getMessage());
                        mView.refreshFileInfosList();
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(MediaInfoScanner.PicUpdateType updateType) {
                        switch (updateType) {
                            case CHANGE_ALL_PIC:
                                if (AllData.usuManager.isUsuPic(currentPicPathList)) {
                                    picAdapter.setImageUrls(AllData.usuManager.getUsuPaths(), true);
                                    mView.refreshPicList();
                                }
                                LogUtil.d(TAG, "初始化显示图片完成");
                                break;
                            case CHANGE_ALL_FILE:
                                if (!AllData.usuManager.isUsuPic(currentPicPathList))
                                    mView.refreshFileInfosList();
                        }
                    }
                });

    }

    @Override
    public void addUsedPathFromUI(String recent_use_pic) {
        // 可以在一屏类显示出来，不添加
        // if (AllData.usuManager.isAdd2Used(recent_use_pic))
        //         //     return;

        AllData.usuManager.addUsedPath(recent_use_pic);
        picDirInfoManager.updateUsuInfo(AllData.usuManager.getUsuPaths());
        if (fileInfosAdapter != null) {
            fileInfosAdapter.notifyDataSetChanged();
            if (currentPicPathList == AllData.usuManager.getUsuPaths()) { //当前在常用图片下
                picAdapter.setImageUrls(AllData.usuManager.getUsuPaths(), true);
                mView.refreshPicList();
            }
        }
    }

    @Override
    public LocalPicAdapter createPicAdapter() {
        picAdapter = new LocalPicAdapter(mContext);
        if (!AdData.judgeAdClose() && mView != null) {
            picAdapter.initAdData(AdData.getTxPicAdPool());
        }
        return picAdapter;
    }

    @Override
    public MyFileListAdapter createFileAdapter() {
        fileInfosAdapter = new MyFileListAdapter(mContext, picDirInfoManager.getAllPicDirInfo());
        return fileInfosAdapter;
    }

    @Override
    public void deletePreferPath(String path) {
        AllData.usuManager.deletePreferPath(path);
        picDirInfoManager.updateUsuInfo(AllData.usuManager.getUsuPaths());
        fileInfosAdapter.notifyDataSetChanged();
        if (currentPicPathList == AllData.usuManager.getUsuPaths()) {
            picAdapter.deletePreferPic(path);
            //            mView.refreshPicList(); //adapter内部调用刷新，这里不用了
        }
    }

    @Override
    public void addPreferPath(String path) {
        US.putOtherEvent(US.OTHERS_ADD_TO_PREFER);
        AllData.usuManager.addPreferPath(path);
        picDirInfoManager.updateUsuInfo(AllData.usuManager.getUsuPaths());
        fileInfosAdapter.notifyDataSetChanged();
        if (currentPicPathList == AllData.usuManager.getUsuPaths()) {
            picAdapter.addPreferPath(path);
            mView.refreshPicList();
        }
    }

    public boolean deletePicList(List<String> pathList) {
        List<String> successList = picDirInfoManager.deletePicList(pathList);// 删除图片文件并更新目录列表信息
        onDelMultiPicsSuccess(successList);
        return successList.size() == pathList.size();
    }

    @Override
    public void togglePicData(int position) {
        //picAdapter空指针问题
        if (picAdapter == null) return;
        if (position == picDirInfoManager.getAllPicDirInfo().size()) {
            mView.choosePicFromSystem();
        } else if (position == 0) { // 切换到常用图片
            currentPicPathList = AllData.usuManager.getUsuPaths();
            picAdapter.setImageUrls(AllData.usuManager.getUsuPaths(), true);
            picAdapter.notifyDataSetChanged();
            mView.onTogglePicList(picAdapter);
        } else if (position == 1 && MediaInfoScanner.SHORT_VIDEO_TAG
                .equals(picDirInfoManager.getDirPath(1))) { // 切换到短视频
            picPathInFile.clear();
            picPathInFile.addAll(MediaInfoScanner.getInstance().getShortVideoMap().keySet());
            currentPicPathList = picPathInFile;
            picAdapter.setImageUrls(picPathInFile, false);
            mView.onTogglePicList(picAdapter);
        } else {
            //获取将要显示的图片的列表，并且将当前要显示的列表{@code currentPicPathList}和adpter内的数据指向获取的列表
            String picDirPath = picDirInfoManager.getDirPath(position);
            picPathInFile.clear();
            FileTool.getOrderedPicListInFile(picDirPath, picPathInFile);
            currentPicPathList = picPathInFile;
            picAdapter.setImageUrls(picPathInFile, false);
            picAdapter.notifyDataSetChanged();
            mView.onTogglePicList(picAdapter);
        }
    }

    /**
     * 打开文件夹
     */
    public void togglePicData(String picDirPath) {
        if (TextUtils.isEmpty(picDirPath)) return;
        //picAdapter空指针问题
        if (picAdapter == null) return;
        //获取将要显示的图片的列表，并且将当前要显示的列表{@code currentPicPathList}和adpter内的数据指向获取的列表
        picPathInFile.clear();
        FileTool.getOrderedPicListInFile(picDirPath, picPathInFile);
        currentPicPathList = picPathInFile;
        picAdapter.setImageUrls(picPathInFile, false);
        picAdapter.notifyDataSetChanged();
        mView.onTogglePicList(picAdapter);
    }

    public List<String> getCurrentPicPathList() {
        return currentPicPathList;
    }

    @Override
    @Nullable
    public LocalGroupedItemData getCurrentPath(int position) {
        return picAdapter.getItem(position);
    }

    @Override
    public void onDelMultiPicsSuccess(List<String> pathList) {
        boolean isDeleteUsu = false;
        boolean isDeleteShortVideo = false;
        for (String path : pathList) {
            if (AllData.usuManager.getUsuPaths().contains(path)) { //包含在常用列表里面，删除常用列表中的信息
                AllData.usuManager.onDeleteUsuallyPicture(path);
                isDeleteUsu = true;
            }
            if (AllData.sMediaInfoScanner.isShortVideo(path)) {
                AllData.sMediaInfoScanner.removeSv(path);
                isDeleteShortVideo = true;
            }
            currentPicPathList.remove(path);
            picAdapter.deleteOnePic(path);
        }
        if (isDeleteUsu) {
            picDirInfoManager.updateUsuInfo(AllData.usuManager.getUsuPaths());
        }
        if (isDeleteShortVideo) {
            picDirInfoManager.updateShortVideoInfo(AllData.sMediaInfoScanner.getShortVideoMap().keySet());
        }
        picAdapter.notifyDataSetChanged();
        fileInfosAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeCurrent(String failedPath) {
        if (currentPicPathList != null) {
            currentPicPathList.remove(failedPath);
        }
    }

    /**
     * 刚保存的图片，虽然发送了媒体更新，也需要1、2s的时间才能查到，自己的PTu界面保存下来的图片，手动添加一遍
     * 手动添加后，如果扫描到了，刷新整个列表，如果没扫描到，则不刷新，所以手动和这里的扫描不会冲突
     *
     * @param newPicPath
     */
    @Override
    public void addNewPath(String newPicPath) {
        if (newPicPath != null) {//最新图片不为空，最新图等于它，并且添加到常用列表，更新文件信息
            latestPic = newPicPath;
            AllData.usuManager.addRecentPathStart(newPicPath);
            // 这里图片没有保存到当前文件夹下面
            //更新文件信息
            if (picDirInfoManager.onAddNewPic(newPicPath)) {
                mView.refreshPicList();
            }
        }
        if (currentPicPathList == AllData.usuManager.getUsuPaths()) {
            picAdapter.setImageUrls(AllData.usuManager.getUsuPaths(), true);
            mView.refreshPicList();
        }

    }

    @Override
    public boolean isInPrefer(String path) {
        return AllData.usuManager.isInPrefer(path);
    }

    @Override
    public boolean isInUsu() {
        return currentPicPathList == AllData.usuManager.getUsuPaths();
    }

    @Override
    public void toggleToUsu() {
        togglePicData(0);//就像点击到文件0一样
    }

    public void requireDeleteChosenPics() {
        List<String> chosenPath = getChosenUrlList(false);
        mView.confirmDeletePicList(chosenPath);
    }

    /************************** 多选相关  **************************/
    /**
     * 注意这个位置时adapter中的位置，和这个类中的列表的位置不一样
     */
    @Override
    public void switchChooseItem(int adapterPosition) {
        picAdapter.switchChooseItem(adapterPosition);
        int count = picAdapter.getLimitedChosenCount();
        if (count == 0) {
            mView.onCancelChosen();
        } else if (count == 1) {
            String path = picAdapter.getOnlyChosenPath();
            mView.onOneChosen(path);
        } else if (count >= 2) {
            mView.onMultiChose();
        }
    }

    @Override
    public void cancelChosen() {
        picAdapter.cancelMultiChoose();
        mView.onCancelChosen();
    }

    @NotNull
    public List<String> getChosenUrlList(boolean isMakeGif) {
        return picAdapter.getChooseUrlList(isMakeGif);
    }
}