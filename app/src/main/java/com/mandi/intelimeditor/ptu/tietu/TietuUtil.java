package com.mandi.intelimeditor.ptu.tietu;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.mandi.intelimeditor.R;


public class TietuUtil {

    /**
     * 设置功能子视图的布局
     * 只需要把popwindow的根据局传进来就行
     * 注意这里popupwindow的高度要加上view所在布局的padding
     *
     * @return
     */
    public static PopupWindow showSurePop(Context context, View sure, View anchor, float left, float top) {
        PopupWindow pop = new PopupWindow(sure, FloatImageView.PAD * 2, FloatImageView.PAD * 2, true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        pop.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.transparent));

        // 效果 可以点击外部，点击外部之后消失
        pop.setFocusable(false);
        pop.setTouchable(true);
        pop.setOutsideTouchable(true);
        //防止与虚拟按键冲突
        //一定设置好参数之后再show,注意注意注意!!!!
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        //
        pop.showAtLocation(anchor, Gravity.START | Gravity.TOP, (int)left, (int)top);
        return pop;
    }
}
