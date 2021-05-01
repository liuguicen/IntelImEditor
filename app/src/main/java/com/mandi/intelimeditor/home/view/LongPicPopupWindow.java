package com.mandi.intelimeditor.home.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.R;


/**
 * Created by LiuGuicen on 2017/1/17 0017.
 */
public class LongPicPopupWindow {

    /**
     * @param imageView 被点击的view
     * @return
     */
    public static boolean setPicPopWindow(final ChoosePopWindowCallback callback,
                                          final Context context,
                                          View imageView,
                                          boolean isShowDelete,
                                          boolean isCollect) {
        final PopupWindow popWindowFile = new PopupWindow(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setDividerPadding(10);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        linearLayout.setDividerDrawable(Util.getDrawable(R.drawable.divider_picture_opration));
        linearLayout.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT));
        linearLayout.setPadding(Util.dp2Px(2), Util.dp2Px(2), Util.dp2Px(2), Util.dp2Px(2));

//        TextView frequentlyTextView = createAndAddOneTv(linearLayout, context);
//        if (callback.isInPrefer()) {
//            frequentlyTextView.setText("取消");
//            frequentlyTextView.setOnClickListener(v -> {
//                popWindowFile.dismiss();
//                callback.deleteFromPrefer();
//            });
//        } else {
//            frequentlyTextView.setText("常用");
//            frequentlyTextView.setOnClickListener(v -> {
//                popWindowFile.dismiss();
//                callback.addToPrefer();
//            });
//        }

        if (isCollect) {// 加入贴图
            TextView tvAddTietu = createAndAddOneTv(linearLayout, context);
            if (callback.isInMyTietu()) {
                tvAddTietu.setText("取消收藏可"); // 少显示一个字，没时间看了，先这样
                tvAddTietu.setOnClickListener(v -> {
                    popWindowFile.dismiss();
                    callback.deleteFromMyTietu();
                });
            } else {
                tvAddTietu.setText("收藏收藏");// 少显示一个字，没时间看了，先这样
                tvAddTietu.setOnClickListener(v -> {
                    popWindowFile.dismiss();
                    callback.addToMyTietu();
                });
            }
        }

        if (isShowDelete) {
            TextView deleteTextView = createAndAddOneTv(linearLayout, context);
            deleteTextView.setText("删除");
            deleteTextView.setOnClickListener(v -> {
                popWindowFile.dismiss();
                callback.requireDelete();
            });
        }


        int[] popWH = new int[2];
        Util.getMesureWH(linearLayout, popWH);
        popWindowFile.setContentView(linearLayout);
        popWindowFile.setWidth(popWH[0]);
        popWindowFile.setHeight(popWH[1]);
        popWindowFile.setFocusable(true);
        popWindowFile.setBackgroundDrawable(Util.getDrawable(
                R.drawable.background_pic_operation));
        popWindowFile.showAsDropDown(imageView, (imageView.getWidth() - popWH[0]) / 2,
                -imageView.getHeight());
        return true;
    }

    private static TextView createAndAddOneTv(LinearLayout linearLayout, Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(22);
        textView.setTextColor(Util.getColor(R.color.text_deep_black));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(Util.dp2Px(8), 0, Util.dp2Px(8), 0);
        linearLayout.addView(textView, layoutParams);
        return textView;
    }


    public interface ChoosePopWindowCallback {

        boolean isInPrefer();

        void deleteFromPrefer();

        void addToPrefer();

        boolean isInMyTietu();

        void addToMyTietu();

        void deleteFromMyTietu();

        void requireDelete();
    }
}
