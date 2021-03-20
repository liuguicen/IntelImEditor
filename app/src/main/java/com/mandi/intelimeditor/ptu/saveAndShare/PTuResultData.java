package com.mandi.intelimeditor.ptu.saveAndShare;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/25
 *      version : 1.0
 * <pre>
 */
public class PTuResultData {
//    /**
//     * 一般情况下，最近使用图片路径都有
//     */
//    public static final String RECENT_USE_PIC_PATH = "com.mandi.intelimeditor.recent_use_pic_path";
    /**
     * 保存并离开和新图片的路径对应
     */
    public static final String SAVE_AND_LEAVE = "save_and_leave";
    public static final String SHARE_AND_LEAVE = "share_and_leave";
    public static final String FINISH_INTERMEDIATE_PTU = "finish_intermediate_ptu";
    public static final String NEW_PIC_PATH = "com.mandi.intelimeditor.new_pic_path";
    public static final String FINISH = "finish";
    /**
     * 用户使用过的标签，注意是字符串列表
     */
    public static final String INTENT_EXTRA_USED_TAGS_LIST = AllData.PACKAGE_NAME + ".USED_TAGS_LIST";

    public static final String SAVE_FAILED_AND_LEAVE = "save_failed_and_leave";

    public static final String FAILED_PATH = "failed_path";
    public static final String LEAVE = "leave";
}
