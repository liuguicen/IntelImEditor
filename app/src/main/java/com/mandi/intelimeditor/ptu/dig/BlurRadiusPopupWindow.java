package com.mandi.intelimeditor.ptu.dig;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;

import com.mandi.intelimeditor.R;


/**
 * P图界面的PopupWindow
 */
public class BlurRadiusPopupWindow extends PopupWindow {

    /**
     * 初始化
     *
     * @param context
     */
    public BlurRadiusPopupWindow(Context context) {
        super(context);
        setFocusable(true);
        setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.transparent));
    }

    /**
     * 显示PopupWindow
     *
     * @param view
     * @param layout
     */
    public void setPopWindow(View view, ViewGroup layout) {
        this.setContentView(layout);
        this.setWidth(view.getWidth());
        this.setHeight(view.getHeight());
        //自己的高度，减去父布局的padding，再减去View的高度
        int top = -view.getHeight()
                - ((ViewGroup) view.getParent()).getPaddingTop()
                - view.getHeight();
        this.showAsDropDown(view, 0, top);
    }

}
