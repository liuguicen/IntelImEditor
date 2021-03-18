package com.mandi.intelimeditor.user.userAccount;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.appInfo.TheUser;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/07/25
 *      version : 1.0
 * <pre>
 */
public interface ServerLoginCallback {
    /**
     * 服务器注册成功，原来没有这个用户
     */
    void onServerRegisterSuccess();
    /**
     * 服务器上登录成功，原来有这个用户
     */
    void onServerLoginSuccess(TheUser serverUser);
    void onServerLoginFailed(@Nullable String msg);
    void onConnectServerFailed();

}
