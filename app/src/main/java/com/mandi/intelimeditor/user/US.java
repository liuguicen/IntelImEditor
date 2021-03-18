package com.mandi.intelimeditor.user;

import android.content.Context;

import com.mandi.intelimeditor.EventName;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.umeng.analytics.MobclickAgent;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/11
 *      version : 1.0
 * <pre>
 *     友盟，APP使用相关统计数据
 */
public class US { // UseStatistic

    /**
     * 广告商类型
     **/
    public static final String TENCENT_AD = "_tencent";
    public static final String TT_AD = "_tt";
    public static final String KJ_AD = "_KJ";

    /*** 开屏广告   ***/
    public static final String SPLASH_AD_NOT_SHOW = "not_show";
    public static final String SPLASH_AD_PAUSE_TO_SEE = "pause_to_see";
    public static final String AD_SKIP = "skip";
    public static final String SPLASH_AD_PAUSE_AND_CLICK = "pause_and_click";
    public static final String SPLASH_AD_ENTER_APP = "enter_app";
    public static final String OPENED = "opened";
    public static final String RECEIVED = "received";
    public static final String ACTIVITY_DESTROYED = "activity_destroyed";
    public static final String CHOOSE_SPLASH_AD = "choose_splash_ad";

    public static void putSplashADEvent(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.splash_ad, eventKey);
    }

    public static void putSplashADEvent(String eventKey, String value) {
        if (value == null) value = "";
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.splash_ad, eventKey + " :  " + value);
    }

    /************   广告通用  ************/
    public static final String LOAD = "load";
    public static final String EXPOSURE = "exposure";
    public static final String CLICK = "click";
    public static final String FAILED = "failed";
    public static final String TIME_OUT = "time_out";
    public static final String CLOSE = "close";
    public static final String load_success = "load_success";

    /**** 图片广告，目前图片列表里面 ***/
    public static final String REND_ERROR = "rend_error";

    /***解锁和激励视频广告*/
    public static final String SHOW_UNLOCK_DIALOG = "show_unlock_dialog";
    public static final String CLICK_UNLOCK = "click_unlock";
    public static final String VIDEO_PLAY_COMPLETE = "video_play_complete";
    // 头条的点击了视频的下载条，一般是弹出对话框，询问下载
    public static final String CLICK_VIDEO_BAR = "click_video_bar";
    public static final String START_DOWNLOAD = "start_download";
    public static final String DOWNLOAD_COMPLETE = "download_complete";
    public static final String INSTALL_SUCCESS = "install_success";
    public static final String CHOOSE_REWARD_VAD = "choose_reward_vad";
    public static final String SHOW_BY_LONG_TIME_NO_SEE = "show_by_long_time_no_see";

    /**
     * 图片列表中的广告事件统计
     *
     * @param context
     */
    public static void putADEvent(Context context, String eventName, String label) {
        MobclickAgent.onEvent(context, eventName, label);
    }

    /**
     * 图片列表中的广告事件统计
     */
    public static void putPicListFeedADEvent_tt(String label) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.pic_resource_ad_tt, label);
    }

    /**
     * 插屏广告
     */
    /**
     * 图片列表中的广告事件统计
     *
     * @param context
     * @param eventKey
     * @param errorCode 广告请求失败码
     */
    public static void putPTuInsertAdEvent(Context context, String eventKey, int errorCode) {
        MobclickAgent.onEvent(context, EventName.ptu_insert_ad,
                eventKey + (errorCode != 0 ? errorCode : ""));
    }


    public static void putPTuResultAdEvent(String eventKey) {
        putPTuResultAdEvent(eventKey, 0);
    }

    public static void putPTuResultAdEvent(String eventKey, int errorCode) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.ptu_result_ad,
                eventKey + (errorCode != 0 ? errorCode : ""));
    }

    /**
     * 字体位置视频广告
     */
    public static void putTypefaceRewardAd(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.typeface_reward_ad, eventKey);
    }

    /**
     * 模板位置视频广告
     */
    public static void putTemplateRewardAd(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.template_reward_ad, eventKey);
    }

    /**
     * 贴图位置视频广告
     */
    public static void putTietuRewardAd(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.tietu_reward_ad, eventKey);
    }


    /**
     * 全屏视频广告
     */
    public static void putFullScreenVideoAdEvent(String value) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.result_fs_video_ad, value);
    }

    /*************************图片资源下载******************************/
    public static final String PIC_RESOURCES_DOWNLOAD_FAILED = "failed";
    public static final String TIETU_DOWNLOAD_FAILED = "tietu_failed";

    public static void putPicResourcesDownloadEvent(String key, int errorCode) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.pic_resource_download,
                key + (errorCode == 0 ? " " : errorCode));
    }

    /***********************编辑图片*****************************/
    public static final String EDIT_PIC_FROM_LOCAL = "from_local";
    public static final String EDIT_PIC_FROM_TEMPLATE = "from_template";
    public static final String EDIT_PIC_FROM_TIETU = "from_tietu";
    public static final String EDIT_PIC_FROM_COMMEND = "from_commend";
    public static final String EDIT_PIC_FROM_SEARCH = "from_search";

    public static void putEditPicEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.edit_pic, key);
    }


    /*********  主功能统计 ******/
    public static final String MAIN_FUNCTION_EDIT = "edit";
    public static final String MAIN_FUNCTION_TEXT = "text";
    public static final String MAIN_FUNCTION_TIETU = "tietu";
    public static final String MAIN_FUNCTION_DRAW = "draw";
    public static final String MAIN_FUNCTION_DIG = "dig";
    public static final String MAIN_FUNCTION_REND = "rend";
    public static final String MAIN_FUNCTION_GIF_SUFFIX = "-gif";
    public static final String MAIN_FUNCTION_CHANGE_FACE = "change_face";
    public static final String MAIN_FUNCTION_DEFORMATION = "deformation";

    public static void putMainFunctionEvent(String eventKey) {
        putMainFunctionEvent(IntelImEditApplication.appContext, eventKey);
    }

    public static void putMainFunctionEvent(Context context, String eventKey) {
        MobclickAgent.onEvent(context, EventName.main_function, eventKey);
    }

    /********* 保存和分享 *******/
    public static final String SAVE_AND_SHARE_SAVE = "save";
    public static final String SAVE_AND_SHARE_SHARE = "share";
    public static final String SAVE_AND_SHARE_CHANGE_PIC_SIZE = "change_pic_size";
    public static final String SAVE_AND_SHARE_GO_SEND = "go_send";

    public static void putSaveAndShareEvent(String key, String value) {
        putSaveAndShareEvent(key + ": " + value);
    }

    public static void putSaveAndShareEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.save_and_share, key);
    }


    /********************************P图各项子功能*****************************************/
    /**
     * 文字
     */
    public static final String PTU_TEXT_TYPEFACE = "typeface";
    public static final String PTU_TEXT_RUBBER = "eraser";
    public static final String PTU_TEXT_COLOR = "color";
    public static final String PTU_TEXT_BIG_AND_SMALL = "big_and_small";
    public static final String PTU_TEXT_REVERSAL = "reversal";
    public static final String PTU_TEXT_FLIP = "flip";
    public static final String PTU_TEXT_VERTICAL = "vertical";
    public static final String PTU_TEXT_DIALOG = "dialog";
    public static final String PTU_TEXT_STYLE = "style";
    public static final String PTU_TEXT_TRANSPARENT = "transparent";

    public static void putPTuTextEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.main_function_text, key);
    }


    /**
     * 贴图
     */
    public static final String PTU_TIETU_EXPRESSION = "expression";
    public static final String PTU_TIETU_PROPERTY = "property";
    public static final String PTU_TIETU_MY = "my";
    public static final String PTU_TIETU_MORE = "more";
    public static final String PTU_TIETU_MAKE = "make";
    public static final String PTU_TIETU_REND = "rend";
    public static final String PTU_TIETU_ERASE = "erase";
    public static final String PTU_TIETU_CLOSE_AUTO = "close_auto";
    public static final String PTU_TIETU_OPEN_AUTO = "open_auto";
    public static final String PTU_TIETU_AUTO_ADD = "auto_add";
    public static final String PTU_TIETU_FLIP = "flip"; // 翻转
    public static final String PTU_TIETU_FUSE = "fuse"; // 翻转
    public static final String PTU_TIETU_STRETCH = "stretch";//拉伸

    public static void putPTuTietuEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.main_function_tietu, key);
    }


    /**
     * 绘图
     */
    public static final String PTU_DRAW_STYLE = "style";
    public static final String PTU_DRAW_COLOR = "color";
    public static final String PTU_DRAW_SIZE = "size";
    public static final String PTU_DRAW_ERASE = "erase";

    public static void putPTuDrawEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.main_function_draw, key);
    }


    /**
     * 抠图
     */
    public static final String PTU_DIG_PREVIEW = "preview";
    public static final String PTU_DIG_BLUR_RADIUS = "blur_radius";

    public static void putPTuDigEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.main_function_dig, key);
    }


    /**
     * 撕图
     */
    public static final String PTU_REND_BG_COLOR = "bg";
    public static final String PTU_REND_BASE_PIC = "base_pic";
    public static final String PTU_REND_REND_AGAIN = "rend_again";
    public static final String PTU_REND_ADD_TEXT = "add_text";
    public static final String PTU_REND_GO_EDIT = "go_edit";

    public static void putPTuRendEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.main_function_rend, key);
    }

    /**
     * 变形
     */
    public static final String PTU_DEFOR_SIZE = "size";
    public static final String PTU_DEFOR_TO_GIF = "to_gif";
    public static final String PTU_DEFOR_EXAMPLE = "example";

    public static void putPTuDeforEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.main_function_deformation, key);
    }

    /**
     * gif编辑
     */
    public static final String GIF_USE = "use";
    public static final String MULTI_PICS_MAKE_GIF = "multi_pics_make_gif";
    public static final String SHORT_VIDEO_MAKE_GIF = "short_video_make_gif";
    public static final String GIF_ADJUST_SPEED = "gif_adjust_speed";
    public static final String GIF_ADD_PIC = "gif_add_pic";
    public static final String GIF_DEL_PIC = "gif_del_pic";

    public static void putGifEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.edit_gif, key);
    }

    /**
     * 换脸
     */
    public static final String CHANGE_FACE_FUNCTION_BTN_ENTER = "function_btn_enter";
    public static final String CHANGE_FACE_DIG_ENTER = "dig_enter";
    public static final String CHANGE_FACE_PIC_ENTER = "pic_enter";
    public static final String CHANGE_FACE_CHOOSE_BG = "choose_bg";
    public static final String CHANGE_FACE_ADJUST_LEVELS = "adjust_levels";
    public static final String CHANGE_FACE_ERASE = "erase";
    public static final String CHANGE_FACE_TOOLS = "tools";
    public static final String CHANGE_FACE_ERASE_BG = "erase_bg";

    public static void putChangeFaceEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.expression_change_face, key);
    }


    /***************************** 设置***************************/
    public static final String SETTING_ENTER_SETTING = "enter_setting";
    public static final String SETTING_CLOSE_SHORT_CUT_NOTIFICATION = "close_short_cut_notification";
    public static final String SETTING_SHARE_WITHOUT_APPLICATION_LABEL = "share_without_application_label";
    public static final String SETTING_GIVE_GOOD_COMMENTS = "give_good_comments";

    public static void putSettingEvent(String key) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.setting, key);
    }

    /*********************PTu结果处理相关的***************************/
    public static final String PTU_RESULT_SHARE_DELETE = "share_and_delete";
    public static final String PTU_RESULT_SAVE_DELETE = "save_and_delete";
    public static final String PTU_RESULT_RETURN_CHOOSE = "return_choose";
    public static final String PTU_RESULT_COTINUE_PTU = "continue_ptu";
    public static final String PTU_RESULT_BACK_PRESSED = "back_pressed";

    public static void putPTuResultEvent(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.ptu_result_deal, eventKey);
    }

    /***************************搜索排序**************************/
    public static final String SEARCH = "search";
    public static final String SORT_HOT = "sort_hot";
    public static final String SORT_NEWEST = "sort_newest";
    public static final String SORT_GROUP = "sort_group";

    public static void putSearchSortEvent(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.search_sort, eventKey);
    }

    /***************************用户 ****************************/
    /********登录********/
    public static final String USER_LOGIN_BY_QQ = "user_login_by_qq";
    public static final String QQ_LOGIN_CANCEL = "qq_login_cancel";
    public static final String QQ_LOGIN_ERROR = "qq_login_error";
    public static final String QQ_LOGIN_SUCCESS = "qq_login_success";
    public static final String USER_LOGIN_BY_WEIXIN = "user_login_by_weixin";
    public static final String WEIXIN_LOGIN_ERROR = "weixin_login_error";
    public static final String WEIXIN_LOGIN_CANCEL = "weixin_login_cancel";
    public static final String WEIXIN_LOGIN_SUCCESS = "weixin_login_success";
    public static final String USER_REGISTER_SUCCESS = "user_register_success";
    public static final String USER_RELOGIN_SUCCESS = "user_relogin_success";

    public static void putUserLoginEvent(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.user_login, eventKey);
    }

    /***开通会员相关*/
    public static final String CLICK_AD_TO_VIP = "click_ad_to_vip";
    public static final String CLICK_LOCKED_RESOURCES_TO_VIP = "click_locked_resources_to_vip";
    public static final String CLICK_NOTICE_TO_VIP = "click_notice_to_vip";
    public static final String CLICK_PAY_FOR_OPEN_VIP = "click_pay_for_open_vip";
    public static final String USER_CHOSE_VIP_PRICE = "user_chose_vip_price";
    public static final String OPEN_VIP_SUCCESS = "open_vip_success";
    public static final String OPEN_VIP_CANCEL_IN_LOGIN = "open_vip_cancel_in_login";
    public static final String OPEN_VIP_IN_PAY = "open_vip_in_pay";
    public static final String OPEN_VIP_FAILED_IN_LOGIN = "open_vip_failed_in_login";
    public static final String OPEN_VIP_FAILED_IN_PAY = "open_vip_failed_in_pay";
    public static final String OPEN_VIP_CANCEL_IN_PAY = "open_vip_cancel_in_pay";
    public static final String VIP_SKIP_AD_div_1000 = "vip_skip_ad_div_1000"; // 用户因为vip跳过广告的次数， 除以了1000

    public static void putOpenVipEvent(String value) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.user_open_vip, value);
    }

    /********* 其它小事件 ******/
    public static final String OTHERS_EDIT_FROM_THIRD_APP = "edit_from_third_app";
    public static final String OTHERS_LONG_CLICK_PIC = "long_click_pic";
    public static final String OTHERS_ADD_TO_PREFER = "add_to_prefer";
    public static final String USE_NOTIFY_LATEST_PIC = "use_notify_latest_pic";
    public static final String CHOOSE_PIC_FROM_SYSTEM = "choose_pic_from_system";
    public static final String CHOOSE_BASE = "choose_base";
    public static final String OTHERS_DIALOG_2_STAR = "dialog_2_star";
    public static final String SKIP_AD_FOR_CHECK = "skip_a_d_for_check";
    public static final String OTHERS_UPLOAD_PTU_WORK = "upload_ptu_work"; // 上传P图作品

    public static final String OTHERS_FORBID_PERMISSION_FOREVER = "forbid_permission_forever";
    public static final String OTHERS_FORBID_PERMISSION = "forbid_permission";

    public static final String USER_LOOK_GUIDE = "user_look_guide";


    public static void putOtherEvent(String eventKey) {
        MobclickAgent.onEvent(IntelImEditApplication.appContext, EventName.others, eventKey);
    }
}
