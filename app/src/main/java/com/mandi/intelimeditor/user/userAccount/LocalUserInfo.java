package com.mandi.intelimeditor.user.userAccount;

import androidx.annotation.Nullable;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/07/25
 *      version : 1.0
 * <pre>
 *     用于集成用户信息数据的类，便于管理
 */
public class LocalUserInfo {
    public static final String OPEN_ID_KEY = "open_id_key";
    public static final String ACCESS_TOCKEN_KEY = "access_token_key";
    public static final String REFRESH_TOCKE_KEY = "refresh_token_key";
    public static final String USER_NAME_KEY = "user_name_key";
    public static final String HEAD_IMAGE_KEY = "head_image_key";

    @Nullable
    public String id;
    @Nullable
    public String name;
    @Nullable
    public String coverUrl;
    @Nullable
    public long vipExpire;

}
