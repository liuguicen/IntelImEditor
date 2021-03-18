package com.mandi.intelimeditor;

/**
 * Created by cyl on 2019/4/13.
 * 事件统计ID
 * 命名规范：模块_功能_show/模块_功能_click （点击次数，展示次数）
 * <p>
 * 能够见名知意。看见事件id就能知道 当前事件埋点真实的意图
 */
public class EventName {
    //开屏广告
    public static final String splash_ad = "SplashAD";

    //图片资源列表广告
    public static final String pic_resource_ad = "PicResourceAD";

    //图片资源列表广告
    public static final String pic_resource_ad_tx_feed = "PicResourceAD_TX_Feed";

    // unlock的dialog上的广告
    public static final String ad_in_unlock_dialog = "AdInUnlockDialog";

    public static final String search_sort = "SearchSort";

    //图片资源列表广告
    public static final String pic_resource_ad_tt = "PicResourceAD_TT";

    //P图结果页广告
    public static final String ptu_result_ad = "PTuResultAd";
    //P图页插屏广告
    public static final String ptu_insert_ad = "PTuInsertAd";
    //字体处激励视频
    public static final String typeface_reward_ad = "typefaceAd";
    //模板处激励视频
    public static final String template_reward_ad = "templateRewardAd";
    //贴图处激励视频
    public static final String tietu_reward_ad = "tietuRewardAd";

    //全屏视频广告
    public static final String result_fs_video_ad = "resultFScreenVAd";

    //图片资源下载
    public static final String pic_resource_download = "PicResourcesDownloadStatus"; // 服务器上l写成了大写，改不了了
    //编辑图片来源
    public static final String edit_pic = "EditPic";

    //主要功能点击
    public static final String main_function = "MainFunction";
    //文字子功能使用
    public static final String main_function_text = "PTuText";
    //贴图子功能使用
    public static final String main_function_tietu = "PTuTietu";
    //涂鸦子功能使用
    public static final String main_function_draw = "PTuDraw";
    //抠图子功能使用
    public static final String main_function_dig = "PTuDig";
    //撕图子功能使用
    public static final String main_function_rend = "PTuRend";
    //撕图子功能使用
    public static final String main_function_deformation = "PTuDeformation";

    // 表情换脸≈制作暴走脸
    public static final String expression_change_face = "ExpressionChangeFace";

    //编辑Gif
    public static final String edit_gif = "Gif";

    //保存图片
    public static final String save_and_share = "SaveAndShare";

    //设置
    public static final String setting = "Setting";

    // P图结果处理
    public static final String ptu_result_deal = "PTuResultDeal";

    // 用户登录
    public static final String user_login = "user_login";

    // 用户开通VIP
    public static final String user_open_vip = "user_open_vip";

    //其他事件统计
    public static final String others = "Others";
}
