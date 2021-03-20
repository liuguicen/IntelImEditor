package com.mandi.intelimeditor.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.appInfo.TheUser;

import com.mandi.intelimeditor.common.appInfo.TheUserUtil;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.BitmapUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;
import com.mandi.intelimeditor.user.userAccount.QQLogin;
import com.mandi.intelimeditor.user.userAccount.ServerLoginCallback;
import com.mandi.intelimeditor.user.userAccount.ThirdLoginCallback;
import com.mandi.intelimeditor.user.userAccount.UserConstant;
import com.mandi.intelimeditor.R;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import util.CoverLoader;

/**
 * Created by LiuGuicen on 2017/1/5 0005.
 * 侧边栏
 */

public class HomePresenter implements HomeContract.Presenter, ThirdLoginCallback {
    private HomeContract.View mView;
    private Context mContext;
    private QQLogin mQQLogin;
    private LocalUserInfo mTempUserInfo;

    HomePresenter(HomeContract.View mainView) {
        mView = mainView;
        mContext = (Context) mainView;
        mTempUserInfo = new LocalUserInfo();
    }

    @Override
    public void start() {
        LogUtil.d("HomePresenter", TheUserUtil.hasLoggedLastTime() + "");
        if (TheUserUtil.hasLoggedLastTime()) {
            //  异步获取用户数据，如头像的bitmap等
            Observable
                    .create((ObservableOnSubscribe<String>) emitter -> {
                        mTempUserInfo.coverUrl = UserConstant.getUserHeadImagePath(mContext);
                        mTempUserInfo.name = SPUtil.getUserName();
                        emitter.onNext("");
                        emitter.onComplete();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<String>() {

                        @Override
                        public void onNext(String ignore) {
                            mView.switch2UserInfoView(mTempUserInfo, null);
                        }

                        @Override
                        public void onError(Throwable e) { // 获取出错也显示，用户名应该是不会错的
                            super.onError(e);
                            mView.switch2UserInfoView(mTempUserInfo, null);
                        }
                    });
        } else {
            mView.switch2LoginView(null);
        }
    }

    public void loginByQQ() {
        mQQLogin = new QQLogin((Activity) mContext, this);
        mQQLogin.loginByQQ();
        US.putUserLoginEvent(US.USER_LOGIN_BY_QQ);
    }

    @Override
    public void loginOut() {
        TheUserUtil.clearLocalLoginInfo(mContext);
        mView.switch2LoginView(null);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mQQLogin != null) {
            mQQLogin.onActivityResultData(requestCode, resultCode, data);
        }
    }

    @Override
    public void onGetToken(String token) {

    }

    @Override
    public void onGetId(String userId) {
        mTempUserInfo.id = userId;
    }

    @Override
    public void onGetName(@Nullable String name) {
        mTempUserInfo.name = name;
    }

    @Override
    public void onDownloadHeadImageFinish(@Nullable String coverUrl) {
        mTempUserInfo.coverUrl = coverUrl;
    }


    @Override
    public void onThirdLoginFailed(String msg) {
        mView.switch2LoginView(msg);
    }

    @Override
    public void onThirdLoginCancel() {
        mView.switch2LoginView(mContext.getString(R.string.login_has_canceled));
    }

    @Override
    public void startLoadUserInfo() {
        ToastUtils.show(R.string.on_loading_info);
    }

    @Override
    public void onThirdLoginFinish() {
        // 执行本应用的服务器的登录
        ToastUtils.show(R.string.on_logging_server);
        TheUserUtil.createOrPullUserOnServer(mContext, mTempUserInfo, new ServerLoginCallback() {

            @Override
            public void onServerRegisterSuccess() {
                // 完全登录成功，第三方授权登录成功，本应用服务器登录成功，然后才算登录成功，
                // 界面切换显示，并保存相关登陆数据到本地
                mTempUserInfo.vipExpire = 0; // 没开通
                onFinallyLoginSuccess();
            }

            @Override
            public void onServerLoginSuccess(TheUser serverUser) {
                // 先更新本地的不同的信息
                mTempUserInfo.vipExpire = serverUser.getVipExpireTime();
                onFinallyLoginSuccess();
            }

            private void onFinallyLoginSuccess() {
                updateLocalUserInfo(mTempUserInfo);
                mView.switch2UserInfoView(mTempUserInfo, mContext.getString(R.string.register_success));
                US.putUserLoginEvent(US.USER_REGISTER_SUCCESS);
            }

            @Override
            public void onServerLoginFailed(@Nullable String msg) {
                ToastUtils.show(mContext.getString(R.string.server_create_user_failed_by_other_error));
            }

            @Override
            public void onConnectServerFailed() {
                ToastUtils.show(R.string.failed_to_access_server);
            }
        });
    }

    private void updateLocalUserInfo(LocalUserInfo userInfo) {
        TheUserUtil.updateLocalUserId(userInfo.id);
        TheUserUtil.updateLocalUserVipExpire(userInfo.vipExpire);

        SPUtil.putUserName(userInfo.name);
        if (userInfo.coverUrl != null) {
            CoverLoader.INSTANCE.loadBitmap(mContext, userInfo.coverUrl, bitmap -> {
                new Thread(() -> {
                    String userHeadImagePath = UserConstant.getUserHeadImagePath(mContext);
                    BitmapUtil.SaveResult saveResult = BitmapUtil.saveBitmap(mContext, bitmap, userHeadImagePath);
                    LogUtil.d("用户头像保存,结果为 " + saveResult.result);
                }).start();
                return null;
            });
        }
    }
}
