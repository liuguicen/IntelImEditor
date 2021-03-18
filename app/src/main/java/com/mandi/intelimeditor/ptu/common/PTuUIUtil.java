package com.mandi.intelimeditor.ptu.common;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;


public class PTuUIUtil {
    /**
     * 添加功能子视图的布局的PopUpWindow在功能布局上面
     * 注意这里popupwindow的高度要加上view所在布局的padding
     */
    public static void addPopOnFunctionLayout(Context context, View contentView, View anchor) {
        PopupWindow pop = new PopupWindow(contentView,
                WindowManager.LayoutParams.MATCH_PARENT,
                anchor.getHeight() + Util.dp2Px(5), true);
        pop.setTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        pop.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.text_popup_window_background));

        //防止与虚拟按键冲突
        //一定设置好参数之后再show,注意注意注意!!!!
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        pop.showAtLocation(anchor, Gravity.START | Gravity.BOTTOM, 0, 0);
    }
}
