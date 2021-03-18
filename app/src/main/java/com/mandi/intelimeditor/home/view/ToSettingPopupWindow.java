package com.mandi.intelimeditor.home.view;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.user.userSetting.SettingActivity;
import com.mathandintell.intelimeditor.R;


/**
 * Created by LiuGuicen on 2017/1/7 0007.
 */

public class ToSettingPopupWindow {
    public static void show(View v, final Context context) {
        final PopupWindow popupWindow = new PopupWindow(context);
        View contentView = View.inflate(context, R.layout.layout_window_to_setting, null);
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                context.startActivity(new Intent(context, SettingActivity.class));
            }
        });
        popupWindow.setContentView(contentView);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(Util.getDrawable(
                R.drawable.background_round_corner_test));
        float offsetY = ((ViewGroup) v.getParent()).getHeight() - v.getY() - v.getHeight();
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAsDropDown(v, 0 - Util.dp2Px(2), (int) (offsetY + 0.5f));

    }

}
