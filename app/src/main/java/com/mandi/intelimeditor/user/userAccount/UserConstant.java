package com.mandi.intelimeditor.user.userAccount;

import android.content.Context;
import android.os.Environment;

import com.mandi.intelimeditor.common.util.FileTool;

import java.io.File;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/07/20
 *      version : 1.0
 * <pre>
 */
public class UserConstant {
    public static final String LOGIN_WAY_QQ = "qq";
    public static final String LOGIN_WAY_WEIXIN = "weixin";
    public static final String LOGIN_WAY_WEIBO = "weibo";
    public static final String HEAD_IMAGE_FILE_NAME = "UserHeadImage.png";

    public static final String getUserHeadImagePath(Context context) {
        File cacheDir = FileTool.getExternalCacheDirectory(context, Environment.DIRECTORY_PICTURES);
        return cacheDir.getAbsolutePath() + File.separator + UserConstant.HEAD_IMAGE_FILE_NAME;
    }
}
