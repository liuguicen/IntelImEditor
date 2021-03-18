package com.mandi.intelimeditor.user.userAccount;

import androidx.annotation.Nullable;

/**
 * 因为多个平台的登录接口差别很大，所以登录的回调一个一个的调用
 */
public interface ThirdLoginCallback {
    public static final String THIRD_LOGIN_RESULT_KEY = "third_login_result_key";
    int LOGIN_STATE_SUCCESS = 1;
    int LOGIN_STATE_CANCEL = 2;
    int LOGIN_STATE_FAILED = 3;
    int LOGIN_STATE_UNSUPPORT = 3;

    void onGetId(String userId);
    void onGetToken(String token);

    /**
     * 名称，昵称
     */
    void onGetName(@Nullable String name);

    /**
     * 头像
     * @param coverUrl 如果为空，表示获取失败
     */
    void onDownloadHeadImageFinish(@Nullable String coverUrl);

    void onThirdLoginFailed(String msg);

    void startLoadUserInfo();

    void onThirdLoginCancel();

    /**
     * 整个第三方登录的流程执行完成
     */
    void onThirdLoginFinish();
}
