package com.mandi.intelimeditor.user.userSetting;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.BasePresenter;
import com.mandi.intelimeditor.BaseView;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;

/**
 * Created by LiuGuicen on 2017/1/5 0005.
 * 主持者和View契约类
 */

public interface SettingContract {
    interface View extends BaseView<Presenter> {
        void switchHightResolution(boolean isSend);

        void switchSendShortCutNotifyExit(boolean isSend);

        void switchSharedWithout(boolean isWith);

        void showAppCache(String cacheString);

        void showClearDialog(String[] infos, boolean[] preChosen);

        void showClearResult(String res);

        void switch2UserInfoView(LocalUserInfo userInfo, @Nullable String toastMsg);

        void switch2LoginView(@Nullable String toastMsg);

        void onUserLoginSuccess();

        Intent getIntent();
    }

    interface Presenter extends BasePresenter {
        /**
         * 是联动的
         * 当发送快捷通知改变时，要设置退出时是否发生
         */
        void onShortCutNotifyChanged(boolean checked);

        /**
         * 清除缓存
         */
        void clearAppData();

        void realClearData(boolean[] userChosenItems);

        /**
         * 当前直跳转到应用宝
         */
        void gotoMark();

        void loginByQQ();

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void loginOut();
    }

}
