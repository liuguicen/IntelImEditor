package com.mandi.intelimeditor.user.userSetting;

import com.mandi.intelimeditor.common.appInfo.OnlineAppConfig;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/01/21
 *      version : 1.0
 * <pre>
 */
public class SPConstants {
    /**
     * 注意sp的常量不要轻易改，涉及到版本升级，以前的不能删除问题
     */
    public static final String user_configs = "user_config";
    public static final String unlock_data_sp = "unlock_data_sp";

    public static final String LAST_LAUNCH_AD_SHOW_TIME = "last_ad_show_time";

    public static final String DATA_SYNCH = "data_synch"; // 数据同步相关，比如贴图表更新

    public static final String APP_CONFIG = "appConfig";

    public static final String APP_VERSION = "app_version";
    public static final String LAST_TIETU_LIST_AD_CLOSE_TIME = "last_tietu_native_ad_close_time";
    public static final String LAST_TEMPLATE_LIST_AD_CLOSE_TIME = "last_template_native_ad_close_time";
    public static final String LAST_LOCAL_AD_CLOSE_TIME = "last_local_list_ad_close_time";
    public static final String LAST_CLICK_TIME_PTU_OPERATION = "last_click_time_ptu_operation";
    public static final String LAST_CLICK_TIME_PTU_RESULT = "last_click_time_ptu_result";
    public static final String PTU_FUNCTION_CLICK_COUNT = "ptu_function_click_time";
    public static final String PTU_RESULT_COUNT = "ptu_result_count";
    public static final String LATEST_TIETU_CATEGORY_QUERY_TIME = "LATEST_TIETU_CATEGORY_QUERY_TIME";
    public static final String QUERY_TIME_OF_TIETU_WITH_CATEGORY = "QUERY_TIME_OF_TIETU_WITH_CATEGORY";
    public static final String SPLASH_AD_STRATEGY = OnlineAppConfig.SPLASH_AD_V270;

    public static final String SEARCH_HISTORY = "search_history";
    public static final String LAST_USE_DATA = "last_user_use_data";  // 上次用户使用日期
    public static final String OPTION_PERMISSION_READ_COUNT = "option_permission_read_count";


    /**
     * 功能或者资源解锁
     */
    public static final class FunctionsUnlock {
        public static final String TYPEFACE_UNLOCK = "typeface_unlock";
        public static final String LAST_LOCK_DATA_SIZE = "last_lock_data_size";
        public static final String LAST_LOCK_VERSION = "last_lock_version";
    }

    public static final class Ad {
        public static final String  AD_SPACE_EXPOSE_NUMBER = "ad_space_expose_number";
        public static final String  LAST_VAD_SHOW_TIME = "LAST_VAD_SHOW_TIME";
    }

    /**
     * 用户登陆相关，注意不能分散，删除时要全部用到
     */
    public static final class UserLogin {
        public static final String USER_LOGIN_WAY = "user_login_way";
        public static final String USER_ID = "user_id";
        public static final String USER_TOKEN = "user_token";
        public static final String USER_NAME = "user_name";
        public static final String USER_EXPIRE = "user_expire";
        public static final String USER_VIP_EXPIRE = "user_vip_expire";
    }

    // 一些设置项
    public static final class AppSettings {
        public static final String SEND_SHORTCUT_NOTIFY = "send_shortcut_notify";
        public static final String SEND_SHORTCUT_NOTIFY_EXIT = "send_shortcut_notify_exit";
        public static final String SHARED_WHTHOUT_LABEL = "shared_without_label";
        public static final String GIF_AUTO_ADD = "gif_auto_add";
        public static final String PERFORMANCE_YEAR_CLASS = "performance_year_class"; // 设备性能分级
        public static final int PERFORMANCE_YEAR_CLASS_UNREAD = -1000;
        public static final String CONTENT_MAX_SUPPORT_BM_SIZE = "CONTENT_MAX_SUPPORT_BM_SIZE";
    }
}
