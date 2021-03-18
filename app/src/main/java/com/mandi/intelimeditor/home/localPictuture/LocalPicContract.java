package com.mandi.intelimeditor.home.localPictuture;

import com.mandi.intelimeditor.BasePresenter;
import com.mandi.intelimeditor.BaseView;

/**
 * Created by LiuGuicen on 2017/1/17 0017.
 */

public interface LocalPicContract {
    interface View extends BaseView<PicPresenter> {
        /**
         * 进入界面，并且获取到图片信息之后开始显示
         */
        void showPicList();

        void deleteOnePic(String s);

        void showFileInfoList();

        void onTogglePicList(LocalPicAdapter picAdapter);

        /**
         * picAdapter通知数据更新了,刷新图片列表视图
         */
        void refreshPicList();

        /**
         * 刷新图片文件信息列表
         */
        void refreshFileInfosList();
    }

    interface PicPresenter extends BasePresenter {
        //常用列表相关信息
        void addUsedPath(String recent_use_pic);

        boolean isInPrefer(String path);

        boolean isInUsu();

        int getPreferStart();

        /**
         * 删除一个喜爱的图片
         */
        void deletePreferPath(String path, int finalPosition);

        void addPreferPath(String path);

        //两个adapter
        LocalPicAdapter getPicAdapter();

        MyFileListAdapter getFileAdapter();


        //图片增删相关

        /**
         * 低效方法，有时间改进
         * 删除图片文件，并更新目录列表信息
         * <p>更新文件信息，文件是否还存在，图片张数，最新图片，描述信息的字符串
         * <p>注意发送删除通知
         *
         * @return 是否删除成功
         */
        boolean onDeleteOnePicInfile(String path);

        /**
         * 删除成功之后刷新的操作
         */
        void onDelOnePicSuccess(String path);

        /**
         * 移除当前列表中的失效图片
         */
        void removeCurrent(String failedPath);

        void addUsedAndNewPath(String recent_use_pic, String newPicPath);

        void detectAndUpdateInfo();


        //当前列表相关

        /**
         * 获取当前图片列表的该位置下的图片的路径
         */
        String getCurrentPath(int position);

        /**
         * @return 当前图片列表的长度
         */
        int getCurrentSize();


        //切换列表相关
        void togglePicData(int position);

        /**
         * 切换到常用图片列表，数据层这和点击到了文件0几乎一致
         */
        void toggleToUsu();
    }

}
