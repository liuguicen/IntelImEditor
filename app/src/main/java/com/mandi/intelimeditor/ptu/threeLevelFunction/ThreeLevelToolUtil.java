package com.mandi.intelimeditor.ptu.threeLevelFunction;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.R;

import java.util.List;



public class ThreeLevelToolUtil {

    /**
     * 第三极功能的高
     */
    public static final int FUNCTION_LAYOUT_HEIGHT_X = Util.dp2Px(120);

    /**
     * @param anchor     popupWindow的anchor
     * @param iconIdList 功能icon的id列表
     * @param bgId       icon背景id
     * @param nameIdList 功能名称的id列表
     * @return
     */
    public static PopupWindow showToolsRcvWindow(Context context,
                                                 View anchor,
                                                 int spanNumber,
                                                 RcvItemClickListener1 itemClickListener,
                                                 List<Integer> iconIdList, int bgId,
                                                 List<Integer> nameIdList) {
        FrameLayout layout = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.popup_3level_function, null);
        RecyclerView listView = layout.findViewById(R.id.cut_choose_ratio_list);

        PopupWindow popupWindow = getPopWindow_for3LevelFunction(context);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, spanNumber);
        listView.setLayoutManager(gridLayoutManager);
        ThreeLevelToolsAdapter toolsAdapter = new ThreeLevelToolsAdapter(context, iconIdList,
                nameIdList, bgId);
        listView.setAdapter(toolsAdapter);
        listView.setHasFixedSize(true);
        toolsAdapter.setOnItemClickListener((itemHolder, view) -> {
            itemClickListener.onItemClick(itemHolder, view);
            popupWindow.dismiss();
        });
        int[] popWH = new int[2];
        Util.getMesureWH(layout, popWH);
        popupWindow.setContentView(layout);
        popupWindow.setFocusable(false);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.white));
        int popUpWidth = Util.dp2Px(spanNumber * 100);
        popupWindow.setWidth(popUpWidth);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        //自己的高度，减去父布局的padding，再减去View的高度
        int top = -anchor.getHeight()
                - ((ViewGroup) anchor.getParent()).getPaddingTop()
                - popWH[1]
                - Util.dp2Px(4);

        Log.e("ThreeLevelToolUtil", "showTietuTools: top = " + top);
        int[] xy = new int[2];
        anchor.getLocationOnScreen(xy);
        // android 6.0.1 的机器，如果width太大，是的popUpWindows超出了屏幕范围，加上anchor View处于滚动视图中是，
        // popUpWindow不显示，所以这里处理之，不让popupWindow超出屏幕范围
        int left = 0;
        if (AllData.screenWidth > 100)
            left = AllData.screenWidth - Util.dp2Px(2) - popUpWidth - xy[0];


        popupWindow.showAsDropDown(anchor, left, top);
        return popupWindow;
    }

    /**
     * @param context 获取用于显示三级功能按钮的弹窗
     * @return
     */
    public static PopupWindow getPopWindow_for3LevelFunction(Context context) {
        final PopupWindow popWindow = new PopupWindow(context);
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);  //设置点击屏幕其它地方弹出框消失
        //防止与虚拟按键冲突
        //一定设置好参数之后再show,注意注意注意!!!!
        popWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.transparent));
        return popWindow;
    }

    /**
     * 设置功能子视图的布局
     * 只需要把popwindow的根据局传进来就行
     * 注意这里popupwindow的高度要加上view所在布局的padding
     */
    public static void showThreeLevelFunctionPop(Activity activity, View v, View contentView) {
        PopupWindow pop = new PopupWindow(contentView,
                WindowManager.LayoutParams.MATCH_PARENT,
                v.getHeight() + Util.dp2Px(5), true);
        pop.setTouchable(true);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        pop.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.text_popup_window_background));

        //防止与虚拟按键冲突
        //一定设置好参数之后再show,注意注意注意!!!!
        pop.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        pop.showAtLocation(v, Gravity.START | Gravity.BOTTOM, 0, 0);
    }
}
