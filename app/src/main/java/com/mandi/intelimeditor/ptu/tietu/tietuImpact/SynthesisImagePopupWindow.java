package com.mandi.intelimeditor.ptu.tietu.tietuImpact;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;


/**
 * Created by LiuGuicen on 2017/1/14 0014.
 */

public class SynthesisImagePopupWindow {

    private PopupWindow popupWindow;

    public void show(View v, final Context context, View.OnClickListener clickListener) {
        popupWindow = new PopupWindow(context);
        View contentView = View.inflate(context, R.layout.layout_btn_synthsis, null);
        contentView.setOnClickListener(clickListener);
        popupWindow.setContentView(contentView);
//        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(null);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        int[] wh=new int[2];
        Util.getMesureWH(contentView,wh);
        float offsetX = v.getWidth() - wh[0] - Util.dp2Px(8);
        float offsetY = v.getHeight() + wh[1] + Util.dp2Px(15);
        popupWindow.showAsDropDown(v, (int) (offsetX + 0.5f), -(int) (offsetY + 0.5f));
    }

    public void dismiss() {
        popupWindow.dismiss();
    }
}