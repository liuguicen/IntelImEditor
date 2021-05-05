package com.mandi.intelimeditor.ad;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.mandi.intelimeditor.user.userSetting.SPConstants;
import com.mandi.intelimeditor.dialog.UnlockDialog;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.Nullable;



public class RewardVadUtil {

    /**
     * @param taskAfterUnlocked 看完视频，解锁完以后调用
     *                          为了方便，直接用的这个接口，不用自己再写一个
     * @return 返回是否需要解锁才能使用
     */
    public static boolean showAdIfLocked(Context context, @Nullable String url, String adName,
                                         @Nullable Runnable taskAfterUnlocked,
                                         View.OnClickListener toOpenVipListener) {
        if (url == null) return false;
        String key = String.valueOf(url.hashCode());
        Boolean isUnlock = LockUtil.sUnlockData.get(key);
        if (isUnlock != null && !isUnlock) {
            UnlockDialog unlockDialog = new UnlockDialog();
            unlockDialog.setTitle(context.getString(R.string.unlock_title_resource));
            unlockDialog.setAdPositionName(adName);
            unlockDialog.setUnlockListener(isReward -> {
                        // 先放入sp
                        // 再放入内存的map
                        if (isReward) {
                            context.getSharedPreferences(SPConstants.unlock_data_sp, Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean(key, true)
                                    .apply();
                            LockUtil.sUnlockData.put(key, true);
                            if (taskAfterUnlocked != null) {
                                taskAfterUnlocked.run();
                            }
                        }
                        return null;
                    }
            );
            unlockDialog.setToOpenVipListener(toOpenVipListener);
            unlockDialog.showIt((FragmentActivity) context);
            return true;
        }
        return false;
    }
}
