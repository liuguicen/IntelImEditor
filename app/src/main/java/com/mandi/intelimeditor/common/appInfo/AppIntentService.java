package com.mandi.intelimeditor.common.appInfo;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.mandi.intelimeditor.bean.VipSetMeal;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.user.useruse.tutorial.Tutorial;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by LiuGuicen on 2016/12/25 0025.
 */

public class AppIntentService extends IntentService {
    final static String TAG = "AppIntentService";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public AppIntentService() {
        super("AppIntentService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: 异步初始化服务启动了");
        // 根据网络时间再判断一次是否是VIP, 主线程里面不能进行网络连接，只能这里获取好
        long time = TimeDateUtil.getNetworkStandardTime();
        AllData.isVip = AllData.localUserVipExpire > time;
        AllData.isVip = true;
        Log.d(TAG, "是否是VIP = " + AllData.isVip);
        // 与服务器同步用户信息
        TheUserUtil.syncUserWithServer();
        // 获取最低VIP广告价格，提示用户需要
        getFloorVipPrice();
        // 从服务器加载APP配置，比如显示那个厂商的广告
        OnlineAppConfig.loadAppConfigFromServer();
        // 从服务器加载引导图
        Tutorial.loadGuideUseFromServer(false);
        // 直接下载所有图片资源
        AllData.downLoadALLPicRes();
        // 初始扫描本地图片
        AllData.initScanLocalPic();
    }

    private void getFloorVipPrice() {
        BmobQuery<VipSetMeal> query = new BmobQuery<>();
        query.findObjects(new FindListener<VipSetMeal>() {
            @Override
            public void done(List<VipSetMeal> list, BmobException e) {
                if (list != null && list.size() != 0) {
                    Log.d(TAG, "getAllVipSetMeals " + list.size());
                    double floorPrice = 1000;
                    for (VipSetMeal vipSetMeal : list) {
                        floorPrice = Math.min(vipSetMeal.disCountPrice, floorPrice);
                    }
                    AllData.floor_vip_price = VipUtil.getFloorPriceString(floorPrice);
                }
            }
        });
    }

    /**
     * 注册到微信
     * 目前是用到再注册
     *//*
    private void initWxSDK() {
        // IWXAPI 是第三方app和微信通信的openApi接口
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        AllData.wxAPI = WXAPIFactory.createWXAPI(this, AppConfig.ID_IN_WEIXIN, true);

        // 将应用的appId注册到微信
        AllData.wxAPI.registerApp(AppConfig.ID_IN_WEIXIN);

        //建议动态监听微信启动广播进行注册到微信
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 将该app注册到微信
                AllData.wxAPI.registerApp(AppConfig.ID_IN_WEIXIN);
            }
        }, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));

    }*/
}
