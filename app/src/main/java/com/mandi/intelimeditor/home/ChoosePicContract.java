package com.mandi.intelimeditor.home;

import com.mandi.intelimeditor.BasePresenter;
import com.mandi.intelimeditor.BaseView;
import com.mandi.intelimeditor.home.localPictuture.LocalGroupedItemData;
import com.mandi.intelimeditor.home.localPictuture.LocalPicAdapter;
import com.mandi.intelimeditor.home.localPictuture.MyFileListAdapter;

import java.util.List;

/**
 * Created by LiuGuicen on 2017/1/17 0017.
 */

public interface ChoosePicContract {
    interface View extends BaseView<PicPresenter> {
        /**
         * 进入界面，并且获取到图片信息之后开始显示
         */
        void showPicList();

        void initFileInfoViewData();

        void onTogglePicList(LocalPicAdapter picAdapter);

        /**
         * picAdapter通知数据更新了,刷新图片列表视图
         */
        void refreshPicList();

        /**
         * 刷新图片文件信息列表
         */
        void refreshFileInfosList();

        void confirmDeletePicList(List<String> chosenList);

        /**
         * 多选时出现其它操作用户确认取消
         */
        void ConfirmCancelMultiChoose();

        /**
         * 从系统相册选择图片
         */
        void choosePicFromSystem();

        void onCancelChosen();

        void onOneChosen(String path);

        void onMultiChose();
    }

    interface PicPresenter extends BasePresenter {
        //常用列表相关信息
        void addUsedPathFromUI(String recent_use_pic);

        boolean isInPrefer(String path);

        boolean isInUsu();

        /**
         * 删除一个喜爱的图片
         */
        void deletePreferPath(String path);

        void addPreferPath(String path);

        //两个adapter
        LocalPicAdapter createPicAdapter();

        MyFileListAdapter createFileAdapter();


        //图片增删相关

        /**
         * 低效方法，有时间改进
         * 删除图片文件，并更新目录列表信息
         * <p>更新文件信息，文件是否还存在，图片张数，最新图片，描述信息的字符串
         * <p>注意发送删除通知
         *
         * @return 是否删除成功
         */
        boolean deletePicList(List<String> path);

        /**
         * 删除成功之后刷新的操作
         */
        void onDelMultiPicsSuccess(List<String> pathList);

        /**
         * 移除当前列表中的失效图片
         */
        void removeCurrent(String failedPath);

        void addNewPath(String newPicPath);

        void detectAndUpdateInfo();


        //当前列表相关

        /**
         * 获取当前图片列表的该位置下的图片的路径
         */
        LocalGroupedItemData getCurrentPath(int position);

        //切换列表相关
        void togglePicData(int position);

        /**
         * 切换到常用图片列表，数据层这和点击到了文件0几乎一致
         */
        void toggleToUsu();

        void switchChooseItem(int adaoterPosition);

        void cancelChosen();
    }
}
