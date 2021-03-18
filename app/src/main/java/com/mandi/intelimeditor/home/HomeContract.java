package com.mandi.intelimeditor.home;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.BasePresenter;
import com.mandi.intelimeditor.BaseView;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;

/**
 * Created by LiuGuicen on 2017/1/5 0005.
 * 侧边栏
 */

public interface HomeContract {
    interface View extends BaseView<Presenter> {
        void switch2UserInfoView(LocalUserInfo userInfo, @Nullable String toastMsg);

        void switch2LoginView(@Nullable String toastMsg);
    }

    interface Presenter extends BasePresenter {

        void loginByQQ();

        void onActivityResult(int requestCode, int resultCode, Intent data);

        void loginOut();
    }

}
