package com.mandi.intelimeditor.user;


import android.graphics.Bitmap;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/07/12
 *      version : 1.0
 * <pre>
 */
public interface UserLoginResult {
    void loginResult(int status, MUserInfo userInfo);

    class MUserInfo {
        /**
         * 用户表示符
         */
        String uniqueID;

        /**
         * 昵称/名字
         */
        String name;

        /**
         * 头像
         */
        Bitmap headImage;
    }
}
