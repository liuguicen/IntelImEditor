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

}
