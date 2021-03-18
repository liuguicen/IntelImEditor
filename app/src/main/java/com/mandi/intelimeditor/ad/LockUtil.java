package com.mandi.intelimeditor.ad;

import android.app.Activity;
import android.content.Context;


import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.user.userSetting.SPConstants;
import com.mandi.intelimeditor.user.userVip.OpenVipActivity;


import org.jetbrains.annotations.Nullable;

public class LockUtil {
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
}
