package com.mandi.intelimeditor.user.userVip;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.bean.VipSetMeal;
import com.mathandintell.intelimedit.dialog.ToOpenVipDialog;
import com.mathandintell.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;



/**
 * VIP价格相关的处理
 */
public class VipUtil {
    public static final float DEFAULT_FLOOR_PRICE = 2;

    /**
     * 半个月价格
     */
    public static final double half_month_price = 2.33;
    /**
     * 不打折半个月价格
     */
    public static final double original_half_month_pirce = 3;

    /**
     * 一个月价格
     */
    public static final double one_month_price = 3.33;

    /**
     * 一个季度价格
     */
    public static final double three_months_price = 6.66;
    public static List<VipSetMeal> setMealNameList;

    //  后期考虑从网络下载价格
    public static List<VipSetMeal> getSetMealInfoList(Context context) {
        if (setMealNameList == null) {
            setMealNameList = new ArrayList<>();
        }
        setMealNameList.clear();
        setMealNameList.add(new VipSetMeal(context.getString(R.string.set_meal_half_month), half_month_price, original_half_month_pirce, 14));
        setMealNameList.add(new VipSetMeal(context.getString(R.string.set_meal_one_month), one_month_price, original_half_month_pirce * 2, 30));
        setMealNameList.add(new VipSetMeal(context.getString(R.string.set_meal_three_moths), three_months_price, original_half_month_pirce * 6, 90));
        return setMealNameList;
    }

    public static String getFloorPriceString(double floorPrice) {
        long round = Math.round(floorPrice);
        if (0 <= floorPrice - round && floorPrice - round <= 0.4) {
            return String.valueOf(round);
        } else {
            return String.format("%.2f", floorPrice);
        }

    }

    public static void judgeShowToOpenVip(FragmentActivity fragmentActivity, double prob) {
        if (Math.random() < prob) {
            ToOpenVipDialog.Companion.newInstance(fragmentActivity).showIt();
        }
    }

    /**
     *
     */
    public static void judeShowToOpenVip_forAdClick(FragmentActivity fragmentActivity) {
        if (AdData.hasClikedAdJust) {
            judgeShowToOpenVip(fragmentActivity, 1.0 / 5);
        }
        AdData.hasClikedAdJust = false;
    }
}
