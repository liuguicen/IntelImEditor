package com.mandi.intelimeditor.common.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.Window;

import com.mathandintell.intelimeditor.R;

import org.jetbrains.annotations.Nullable;



/**
 * Created by liuguicen on 2016/8/30.
 *
 * @description
 */
public class DialogFactory {
    /**
     * 没有标题，只有确定有效
     *
     * @param context      上下文
     * @param msg          消息
     * @param sureListener 确定函数
     */
    public static Dialog noTitle(Activity context,
                                 String msg,
                                 @Nullable String sureText,
                                 @Nullable String cancelText,
                                 @Nullable AlertDialog.OnClickListener sureListener) {
        if (sureText == null) sureText = context.getString(R.string.sure);
        if (cancelText == null) cancelText = context.getString(R.string.cancel);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Dialog dialog = builder.setMessage(msg)
                .setNegativeButton(sureText, sureListener)
                .setPositiveButton(cancelText, null)
                .create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        return dialog;
    }

}
