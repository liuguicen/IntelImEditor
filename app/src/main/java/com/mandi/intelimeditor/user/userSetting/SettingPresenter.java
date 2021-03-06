package com.mandi.intelimeditor.user.userSetting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.CommonConstant;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.appInfo.TheUser;
import com.mandi.intelimeditor.common.appInfo.TheUserUtil;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
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

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import com.mandi.intelimeditor.common.util.CoverLoader;

/**
 * Created by LiuGuicen on 2017/1/5 0005.
 */

public class SettingPresenter implements SettingContract.Presenter, ThirdLoginCallback {
    private SettingDataSource dataSource;
    private SettingContract.View mView;
    private Context mContext;
    private QQLogin mQQLogin;
    private LocalUserInfo mTempUserInfo;

    SettingPresenter(SettingContract.View settingView) {
        dataSource = new SettingDataSourceImpl(IntelImEditApplication.appContext);
        dataSource.initDiskCacheDataInfo();
        mView = settingView;
        mContext = (Context) mView;
        mTempUserInfo = new LocalUserInfo();
    }

    @Override
    public void onShortCutNotifyChanged(boolean checked) {
        dataSource.saveSendShortCutNotify(checked);
        if (!checked) {
            US.putSettingEvent(US.SETTING_CLOSE_SHORT_CUT_NOTIFICATION);
            mView.switchSendShortCutNotifyExit(false);
            AllData.globalSettings.save_ifNotifyWhenExit(false);
        }
    }

    private String getAppCache() {
        return dataSource.getAppDataSize() + "MB";
    }

    @Override
    public void clearAppData() {
        String[] dataItemInfos = dataSource.getDataItemInfos();
        boolean[] preChosen = new boolean[dataItemInfos.length];
        Arrays.fill(preChosen, true);
        mView.showClearDialog(dataItemInfos, preChosen);
    }

    @Override
    public void realClearData(final boolean[] userChosenItems) {
        Observable
                .create((ObservableOnSubscribe<String>) emitter -> {

                            String res = dataSource.clearAppCache(userChosenItems);
                            emitter.onNext(res);
                            emitter.onComplete();
                        }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onNext(String res) {
                        if (res.isEmpty())
                            res = "????????????";
                        else
                            res = "????????????!" + res + "?????????";
                        mView.showClearResult(res);
                        //????????????????????????????????????
                        mView.showAppCache(getAppCache());
                    }
                });
    }

    @Override
    public void gotoMark() {
        goToMarket(mContext, mContext.getPackageName());
    }

    /**
     * @param packageName ????????????????????????
     */
    private void goToMarket(Context context, String packageName) {
        try {
            Uri uri = Uri.parse("market://details?id=" + packageName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            ToastUtils.show("????????????????????????Android????????????");
            e.printStackTrace();
        }
        //        Uri uri = Uri.parse("market://details?id=" + packageName);
        //        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        //        try {
        //            //????????????????????????????????????????????????????????????qq
        //            goToMarket.setClassName("com.tencent.android.qqdownloader", "com.tencent.pangu.link.LinkProxyActivity");
        //            context.startActivity(goToMarket);
        //        } catch (ActivityNotFoundException e) {
        //            e.printStackTrace();
        //            ToastUtils.makeText((Activity) view, "???????????????????????????");
        //        }
    }

    @Override
    public void start() {
        boolean highSolution = dataSource.getHighSolution();

        if (highSolution) {
            mView.switchHightResolution(highSolution);
            // mView.switchSendShortCutNotifyExit(dataSource.getSendShortcutNotifyExit());
        } else {
            mView.switchHightResolution(false);
            // mView.switchSendShortCutNotifyExit(false);
        }
        boolean sharedWithout = dataSource.getSharedWithout();
        mView.switchSharedWithout(sharedWithout);
        mView.showAppCache(getAppCache());

        if (TheUserUtil.hasLoggedLastTime()) {
            //  ???????????????????????????????????????bitmap???
            Observable.create((ObservableOnSubscribe<String>) emitter -> {
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
                        public void onError(Throwable e) { // ??????????????????????????????????????????????????????
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
        AdData.readData(); // ????????????????????????
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
        if (CommonConstant.ACTION_LOGIN_FOR_OPEN_VIP.equals(mView.getIntent().getAction())) {
            US.putOpenVipEvent(US.OPEN_VIP_FAILED_IN_LOGIN);
        }
    }

    @Override
    public void onThirdLoginCancel() {
        mView.switch2LoginView(mContext.getString(R.string.login_has_canceled));
        if (CommonConstant.ACTION_LOGIN_FOR_OPEN_VIP.equals(mView.getIntent().getAction())) {
            US.putOpenVipEvent(US.OPEN_VIP_CANCEL_IN_LOGIN);
        }
    }

    @Override
    public void startLoadUserInfo() {
        ToastUtils.show(R.string.on_loading_info);
    }

    @Override
    public void onThirdLoginFinish() {
        // ????????????????????????????????????
        ToastUtils.show(R.string.on_logging_server);
        TheUserUtil.createOrPullUserOnServer(mContext, mTempUserInfo, new ServerLoginCallback() {

            @Override
            public void onServerRegisterSuccess() {
                // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????????
                mTempUserInfo.vipExpire = 0; // ?????????
                onFinallyLoginSuccess();
            }


            @Override
            public void onServerLoginSuccess(TheUser serverUser) {
                // ?????????????????????????????????
                mTempUserInfo.vipExpire = serverUser.getVipExpireTime();
                onFinallyLoginSuccess();
            }

            private void onFinallyLoginSuccess() {
                updateLocalUserInfo(mTempUserInfo);
                mView.switch2UserInfoView(mTempUserInfo, mContext.getString(R.string.register_success));
                mView.onUserLoginSuccess();
                US.putUserLoginEvent(US.USER_REGISTER_SUCCESS);
            }

            @Override
            public void onServerLoginFailed(@Nullable String msg) {
                ToastUtils.show(R.string.server_create_user_failed_by_other_error);
            }

            @Override
            public void onConnectServerFailed() {
                ToastUtils.show(R.string.failed_to_access_server);
            }
        });
    }

    private void updateLocalUserInfo(LocalUserInfo userInfo) {
        new Thread(() -> {
            TheUserUtil.updateLocalUserId(userInfo.id);
            TheUserUtil.updateLocalUserVipExpire(userInfo.vipExpire);
            SPUtil.putUserName(userInfo.name);
            if (userInfo.coverUrl != null) {
                CoverLoader.INSTANCE.loadBitmap(mContext, userInfo.coverUrl, bitmap -> {
                    String userHeadImagePath = UserConstant.getUserHeadImagePath(mContext);
                    BitmapUtil.SaveResult saveResult = BitmapUtil.saveBitmap(mContext, bitmap, userHeadImagePath);
                    LogUtil.d("??????????????????,????????? " + saveResult.result);

                    return null;
                });
            }
        }).start();
    }
}
