package com.mandi.intelimeditor.common.appInfo;

import android.util.Log;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.pay.alipay.PayConstants;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/01
 *      version : 1.0
 * <pre>
 */
public class OnlineAppConfig extends BmobObject {

    /**
     * 不能随便改，需要和以前的版本保持一致，并且要服务器要保持一致
     */
    // 图片资源列表下
    // TT_FEED = 1; TENCENT_PIC = 2; TENCENT_FEED = 3; TENCENT_PIC_TENCENT_FEED=99;TENCENT_PIC_TT_FEED=100；
    public static final int TT_FEED = 1;
    public static final int TX_PIC = 2;
    public static final int TX_FEED = 3;
    public static final int TX_PIC_TX_FEED = 99; // 腾讯纯图腾讯信息流
    public static final int TX_PIC_TT_FEED = 100;

    public static final int HY_REWARD_VAD = 10;
    public static final int TT_AD = AdData.TT_AD_ID;
    public static final int TENCENT_AD = AdData.TENCENT_AD_ID;


    public static final String TAG = "OnlineAppConfig";

    //广告选择部分，260版本的，代码保留
    public static final String SPLASH_AD_V260 = "splash_ad_v260";
    public static final String PIC_RES_AD_V260 = "pic_res_ad_v260"; // 新版本
    public static final String REWARD_VAD_V260 = "reward_vad_v260";
    public static final String PTU_RESULT_AD_V260 = "ptu_result_ad_v260";

    // 260广告策略代码出问题，只能新建v270了
    public static final String SPLASH_AD_V270 = "splash_ad_v270";
    public static final String REWARD_VAD_V270 = "reward_vad_v270";
    public static final String PIC_RES_AD_V270 = "pic_res_ad_v270"; // 新版本
    public static final String PTU_RESULT_AD_V270 = "ptu_result_ad_v270";

    /**
     * 支付宝会员相关url
     */
    public static final String PAY_URL = "pay_url";

    public static void loadAppConfigFromServer() {
        BmobQuery<OnlineAppConfig> query = new BmobQuery<>();
        query.findObjects(new FindListener<OnlineAppConfig>() {
            @Override
            public void done(List<OnlineAppConfig> list, BmobException e) {
                if (list != null && list.size() != 0) {
                    Log.d(TAG, "getAppConfig size = " + list.size());
                    for (OnlineAppConfig config : list) {
                        String key = config.key;
                        if (OnlineAppConfig.SPLASH_AD_V270.equals(key)) {
                            // 服务器更改选项之后的，第一次启动app时是从主页才会调用到这里，那时设置开屏优先无效
                            AllData.appConfig.splash_ad_strategy = config.value;
                            // 写入本地存储
                            AllData.appConfig.putLocalFirstSplashAd(config.value);
                        } else if (OnlineAppConfig.PIC_RES_AD_V270.equals(key)) { // 信息流广告首选
                            AllData.appConfig.pic_res_ad_strategy = config.value;
                        } else if (OnlineAppConfig.REWARD_VAD_V270.endsWith(key)) { // 激励视频
                            AllData.appConfig.reward_vad_strategy = config.value;
                        } else if (OnlineAppConfig.PTU_RESULT_AD_V270.endsWith(key)) { // P图结果处
                            AllData.appConfig.ptu_result_ad_strategy = config.value;
                        } else if (OnlineAppConfig.PAY_URL.equals(key)) { // 配置vip的url预防url变动
                            PayConstants.URL_PREPARE_ORDER_VIP = config.value;
                        }
                    }
                } else {
                    Log.e(TAG, "获取APP配置出错或者列表为空 " + e.getMessage());
                }
            }
        });
    }

    private String key;
    private String value;
    private String remarks;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
