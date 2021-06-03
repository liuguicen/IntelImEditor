package com.mandi.intelimeditor.ad;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVad;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.user.userSetting.SPConstants;
import com.mandi.intelimeditor.user.userVip.OpenVipActivity;


import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class LockUtil {
    /**
     * 激励视频广告锁住的位置
     */
    public static final List<Integer> picResLockedPosition = new ArrayList<Integer>() {{
        add(5);
        add(7);
        add(10);
        add(13);
        add(15);
        add(18);
        add(20);
        add(23);
        add(25);
        add(28);
        add(30);
        add(34);
        add(38);
        add(41);
        add(45);
        add(50);
        add(53);
        add(59);
        add(62);
        add(65);
        add(69);
        add(72);
        add(78);
        add(85);
        add(91);
        add(97);
        add(103);
        add(108);
        add(103);
        add(119);
        add(125);
        add(131);
        add(139);
        add(145);
        add(151);
        add(159);
        add(162);
        add(170);
        add(183);
        add(195);
        add(199);
        add(204);
        add(210);
        add(215);
        add(221);
        add(228);
        add(233);
        add(237);
        add(241);
        add(250);
        add(259);
        add(271);
        add(285);
        add(290);
        add(296);
    }};

    /**
     * 包含了需要解锁的资源，即key，用图片url的hashcode生成，
     * 以及需要解锁的资源的解锁结果
     * 判断一个资源是否锁住时，就判断是否包含它的key并且value = false
     */
    public static Map<String, Boolean> sUnlockData = new HashMap<>();

    static {
        int start = 300;
        for (int i = 1; i < 40; i++)
            for (int j = 4; j < 9; j++) { // 不能用随机，那样每次位置不同，错的
                start += j;
                LockUtil.picResLockedPosition.add(start);
            }
    }
    /**
     * 长时间没看激励视频, 资源加锁的
     * 目前策略是，点击之后加锁，用户下次进入APP就会锁上了
     * 但是不能加太多，比如用户用了7-8张，那么会导致用户后面解锁7-8次
     * 所以一次不能加锁太多
     */
    public static int numberOfLockLongTimeNoSee = 0;

    public static boolean checkLock(Context context, @Nullable String url, boolean isTietu, Runnable taskAfterUnlocked
            , boolean isLockForLongTimeNoSee) {
        String adName;
        if (isTietu) {
            adName = AdData.REWARD_AD_NAME_TIETU;
        } else {
            adName = AdData.REWARD_AD_NAME_TEMPLATE;
        }
        boolean isLocked = RewardVadUtil.showAdIfLocked(context, url, adName, taskAfterUnlocked,
                view -> OpenVipActivity.Companion.startOpenVipAc((Activity) context));
        long lastRadShowTime = SPUtil.getLastRadShowTime();
        if (!isLocked && isLockForLongTimeNoSee && System.currentTimeMillis() - lastRadShowTime > TimeDateUtil.DAY_MILS * 4) {
            lockUrlForLongTimeNoSee(context, url);
            return isLocked;
        } else {
            return isLocked;
        }
    }

    /**
     * 如果用户长时间有观看激励视频广告，那么将这个Url锁上，下次用户打开就是锁上的，
     * 以期用户后面能够看这个广告
     * 这个技巧还是比较好的
     */
    private static void lockUrlForLongTimeNoSee(Context context, String url) {
        if (url == null) return;
        if (numberOfLockLongTimeNoSee >= 1) return;
        if (FileTool.urlType(url) != FileTool.UrlType.URL) return;
        numberOfLockLongTimeNoSee++;

        String key = String.valueOf(url.hashCode());
        context.getSharedPreferences(SPConstants.unlock_data_sp, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, false)
                .apply();
    }

    public static void updateUnlockIfNeeded(List<PicResource> picList) {
        if (picList == null) return;
        if (picList.size() == 0) return;

        int lastLockVersion = SPUtil.getLockVersion();
        int lastSize = SPUtil.getLastLockDataSize();
        if (picList.size() > lastSize + 10 || AppConfig.CUR_LOCK_VERSION > lastLockVersion) {
            SharedPreferences unlockSp = AllData.appContext.getSharedPreferences(
                    SPConstants.unlock_data_sp, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = unlockSp.edit();

            // 1 将url取出来，装入Set便于比较
            HashSet<String> picHashCodeSet = new HashSet<>();
            for (PicResource PicResource : picList) {
                picHashCodeSet.add(String.valueOf(PicResource.getUrl().getUrl().hashCode()));
            }

            // 2 暂存已经解锁的数据中， 要和现在的list依然匹配的，因为list里面有些可能已经改变了
            Map<String, Boolean> tempUnlockMap = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : sUnlockData.entrySet()) {
                if (picHashCodeSet.contains(entry.getKey())) {
                    tempUnlockMap.put(entry.getKey(), entry.getValue());
                }
            }

            // 3 删除旧的解锁数据
            sUnlockData.clear();
            editor.clear().apply();

            // 4 加入这一次的数据
            for (Integer position : picResLockedPosition) {
                if (position >= picList.size()) {
                    break;
                }
                String key = String.valueOf(picList.get(position).getUrl().getUrl().hashCode());
                Boolean res = tempUnlockMap.get(key); // 前一次已经是解锁的，则加入前一次的数据，包括已经解锁或者没有
                if (res == null) res = false;

                editor.putBoolean(key, res);
                sUnlockData.put(key, res);
                if (LogUtil.debugRewardAd) {
                    LogUtil.d(TTRewardVad.TAG, "存入解锁数据 " + key + " : " + res);
                }
            }
            editor.apply();

            // 如果是因为版本号更新的，写入当前的版本号
            if (AppConfig.CUR_LOCK_VERSION > lastLockVersion) {
                SPUtil.putLockVersion(AppConfig.CUR_LOCK_VERSION);
            }
            // 更新了，则Size肯定更新，写入
            SPUtil.putLastLockDataSize(picList.size());
        }
    }
}
