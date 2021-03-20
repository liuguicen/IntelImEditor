package com.mandi.intelimeditor.ad.ttAD.videoAd;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.user.userSetting.SPConstants;
import com.mandi.intelimeditor.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/09/23
 *      version : 1.0
 * <pre>
 */
public class TTRewardVad {
    public static final String TAG = "reward ad";


    public static ImageView createLockView(Context context, ViewGroup parent, int size) {
        // 创建LinearLayout对象
        ImageView imageView = new ImageView(context);
        // imageView.setBackgroundColor(0xaa808080);
        imageView.setImageResource(R.drawable.ic_lock_outline);
        imageView.setVisibility(View.GONE);
        if (parent instanceof ConstraintLayout) {
            if (size <= 0) {
                size = Util.dp2Px(15);
            }
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ((int) (size * 1.06f)), size);
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            imageView.setLayoutParams(layoutParams);
        } else {

        }
        return imageView;
    }

    public static void updateUnlockIfNeeded(String firstClass, String secondClass, List<PicResource> resultList) {
        if (resultList == null) return;
        if (resultList.size() == 0) return;

        // 每个分类和自己比较
        int lastSize = SPUtil.getLastLockDataSize(secondClass);// 如果长度增加了x个以上，那么更新解锁数据
        int lastLockVersion = SPUtil.getLastLockVersion(secondClass);
        if (lastSize > resultList.size() + 10 || AppConfig.CUR_LOCK_VERSION > lastLockVersion) {
            boolean isTietu = PicResource.FIRST_CLASS_TIETU.equals(firstClass);
            SharedPreferences unlockSp = AllData.appContext.getSharedPreferences(
                    SPConstants.unlock_data_sp, Context.MODE_PRIVATE);

            List<Integer> lockedPositions;
            if (isTietu) {
                lockedPositions = AdData.tietuLockedPosition;
            } else {
                lockedPositions = AdData.templateLockedPosition;
            }

            SharedPreferences.Editor editor = unlockSp.edit();

            // 1 将总列表里面属于当前这个分类的ItemList取出来看，存入临时列表，

            HashSet<String> curHashCodeSet = new HashSet<>(); // 装入Set便于比较
            for (PicResource PicResource : resultList) {
                curHashCodeSet.add(String.valueOf(PicResource.getUrl().getUrl().hashCode()));
            }

            Map<String, Boolean> tempUnlockMap = new HashMap<>();
            for (Map.Entry<String, Boolean> entry : AdData.sUnlockData.entrySet()) {
                if (curHashCodeSet.contains(entry.getKey())) {
                    tempUnlockMap.put(entry.getKey(), entry.getValue());
                }
            }

            // 2 然后先从总列表中将它们移除，再对比选中位置进行更新，在加入新的选中列表到总列表中
            for (Map.Entry<String, Boolean> entry : tempUnlockMap.entrySet()) {
                AdData.sUnlockData.remove(entry.getKey());
                editor.remove(entry.getKey());
                if (LogUtil.debugRewardAd) {
                    LogUtil.d(TTRewardVad.TAG, secondClass + "删除解锁数据 " + entry.getKey() + " : " + entry.getValue());
                }
            }

            // 3 加入这一次的数据
            for (Integer position : lockedPositions) {
                if (position >= resultList.size()) {
                    break;
                }
                String key = String.valueOf(resultList.get(position).getUrl().getUrl().hashCode());
                boolean res = false;
                if (tempUnlockMap.get(key) != null) { // 前一次已经是加锁的，则加入前一次的数据，包括已经解锁或者没有
                    res = tempUnlockMap.get(key);
                }
                editor.putBoolean(key, res);
                AdData.sUnlockData.put(key, res);
                if (LogUtil.debugRewardAd) {
                    LogUtil.d(TTRewardVad.TAG, secondClass + "存入解锁数据 " + key + " : " + res);
                }
            }
            editor.apply();

            // 如果是因为版本号更新的，写入当前的版本号
            if (AppConfig.CUR_LOCK_VERSION > lastLockVersion) {
                SPUtil.putLastLockVersion(secondClass, AppConfig.CUR_LOCK_VERSION);
            }
            // 更新了，则Size肯定更新，写入
            SPUtil.putLastLockDataSize(secondClass, lastSize);
        }
    }

}
