package com.mandi.intelimeditor.common.appInfo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;
import com.mandi.intelimeditor.user.userAccount.ServerLoginCallback;
import com.mandi.intelimeditor.user.userAccount.UserConstant;
import com.mandi.intelimeditor.BuildConfig;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;


/**
 * 相关方法不写在bmob之类里面，不能混淆，
 * 写在其它类增强防破解能力
 */
public class TheUserUtil {

    /**
     * 更新用户VIP到期时间，包含服务器，本地，以及相关状态等所有位置
     * 需要通过userInfo传入用户Id和到期时间
     */
    public static void updateVipExpire(String uid, long vipExpireTime, TheUser.UpdateVipExpireListener updateVipExpireListener) {
        BmobQuery<TheUser> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("userId", uid);
        bmobQuery.findObjects(new FindListener<TheUser>() {
            @Override
            public void done(List<TheUser> resultList, BmobException e) {
                if (e != null) { // 网络等出错,查询用户信息失败
                    Log.e("TheUser", "网络等错误，更新会员信息失败");
                    if (updateVipExpireListener != null) {
                        updateVipExpireListener.failed(e.getMessage());
                    }
                } else if (resultList.size() == 0) {
                    Log.e("TheUser", "没查询到用户，更新VIP失败");
                    if (updateVipExpireListener != null) {
                        updateVipExpireListener.failed("用户不存在");
                    }
                } else { // 查询成功，更新
                    TheUser serverUser = resultList.get(0);
                    serverUser.setVipExpireTime(vipExpireTime);
                    serverUser.update(
                            new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        if (updateVipExpireListener != null) {
                                            updateVipExpireListener.success();
                                        }
                                        new Thread(() -> updateLocalUserVipExpire(vipExpireTime)).start();
                                    } else {
                                        if (updateVipExpireListener != null) {
                                            updateVipExpireListener.failed(e.getMessage());
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    /**
     * 功能是在服务器上创建用户，如果服务器上面已有该用户，那么拉取该用户的信息，然后更新
     * 具体流程：<p>
     * 先从服务器上面查询用户信息，
     * 如果没有查询到，如果是网络等其它问题，那么提醒用户查询失败，检查网络等，尝试重新登录，
     * 如果排除是网络等其它问题，但是没有查询到，那是用户没有在服务器上面注册过，那么在服务器上创建该用户
     * 初始化用户信息，并且写入本地和网络
     * 如果查询到了，说明用户原来登陆过，那么从服务器上面获取用户信息，并且更新本地各个状态
     */
    public static void createOrPullUserOnServer(Context context, LocalUserInfo userInfo,
                                                @Nullable ServerLoginCallback serverLoginCallback) {
        if (userInfo.id == null || userInfo.id.isEmpty()) {
            Log.e("TheUser", "pullOrCreateUserOnServer: 传入用户信息ID有误，无法与服务器同步");
            return;
        }

        BmobQuery<TheUser> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("userId", userInfo.id);
        bmobQuery.setCachePolicy(BmobQuery.CachePolicy.NETWORK_ONLY);
        bmobQuery.findObjects(new FindListener<TheUser>() {
            @Override
            public void done(List<TheUser> resultList, BmobException e) {
                if (e != null) { // 网络等出错,查询用户信息失败
                    e.printStackTrace();
                    if (serverLoginCallback != null) {
                        MobclickAgent.reportError(context, "用户登录失败，访问服务器失败");
                        serverLoginCallback.onConnectServerFailed();
                    }
                } else if (resultList.size() == 0) { // 后台要设置唯一键，不会出现多个，但是会出现0个或1个
                    // 服务器上面没有这个用户，因为没查询到，又不是网络等其它原因引起
                    TheUser theUser = new TheUser();
                    theUser.setUserId(userInfo.id);
                    theUser.setVipExpireTime(userInfo.vipExpire);
                    theUser.registerOnServer(new SimpleObserver<String>() {
                        @Override
                        public void onNext(String s) {
                            // 服务器创建用户信息成功，然后才能将用户信息写入本地
                            if (serverLoginCallback != null) {
                                serverLoginCallback.onServerRegisterSuccess();
                            }
                        }

                        @Override
                        public void onError(Throwable e) { // 服务器创建用户失败
                            super.onError(e);
                            MobclickAgent.reportError(context, "服务器创建用户信息失败 " + e.getMessage());
                            if (serverLoginCallback != null) {
                                serverLoginCallback.onServerLoginFailed(null);
                            }
                        }
                    });
                } else { // 查询成功，更新本地用户信息
                    // Toast.makeText(getApplicationContext(), "查询成功：" + object.size(), Toast.LENGTH_LONG).show();
                    TheUser serverUser = resultList.get(0);
                    if (serverLoginCallback != null) {
                        serverLoginCallback.onServerLoginSuccess(serverUser);
                    }
                }
            }
        });
    }

    /**
     * 启动APP或者APP回到前台的时候，与服务器同步用户信息
     */
    public static void syncUserWithServer() {
        if (hasLoggedLastTime()) {
            LocalUserInfo userInfo = new LocalUserInfo();
            userInfo.id = AllData.localUserId;
            userInfo.vipExpire = AllData.localUserVipExpire;
            createOrPullUserOnServer(IntelImEditApplication.appContext, userInfo, new ServerLoginCallback() {

                @Override
                public void onServerLoginSuccess(TheUser serverUser) {
                    if (AllData.localUserVipExpire != serverUser.getVipExpireTime()) { // 更新VIP信息，本地的不等于服务上面的
                        new Thread(() -> updateLocalUserVipExpire(serverUser.getVipExpireTime())).start();
                    }
                }

                @Override
                public void onServerRegisterSuccess() {

                }


                @Override
                public void onServerLoginFailed(@Nullable String msg) {

                }

                @Override
                public void onConnectServerFailed() {
                    // 需要时提示用户，获取用户信息失败
                    // if () {
                    //     Toast.makeText(getApplicationContext(),
                    //             getApplication().getString(R.string.get_user_info_failed_by_other_error),
                    //             Toast.LENGTH_SHORT).show();
                    // }
                }
            });
        }
    }

    public static void clearLocalLoginInfo(Context context) {
        AllData.localUserId = "";
        SPUtil.clearAllUserLoginInfos();
        try {
            boolean delete = new File(UserConstant.getUserHeadImagePath(context)).delete();
        } catch (Exception ignored) {

        }
    }

    public static void updateLocalUserId(String userId) {
        AllData.localUserId = userId;
        SPUtil.putUserId(userId);
    }

    /**
     * @return 上次退出前用户是否是登录状态，
     * 如果上次退出前，没有登录，或者用户登录了，但是又退出了，都不算登录状态
     */
    public static boolean hasLoggedLastTime() {
        return AllData.localUserId != null && AllData.localUserId.length() > 0;
    }

    /**
     * 必须在非UI线程里面调用
     * <p> 登录成功之后更新
     * <p> 开通VIP的时候更新
     * <p> 启动APP的时候可能与服务器同步数据然后更新
     */
    public static void updateLocalUserVipExpire(long vipExpire) {
        if (BuildConfig.DEBUG) {
            Log.d("TheUser", "updateLocalUserVipExpire: isMainThread = " + Util.isMainThread());
        }
        AllData.localUserVipExpire = vipExpire;
        SPUtil.putUserVipExipre(vipExpire);

        // 设置是否是VIP的标志位
        long time = TimeDateUtil.getNetworkStandardTime();
        AllData.isVip = vipExpire > time;
        if (AllData.isVip) {
            AdData.onOpenVipSuccess();
        }
    }
}
