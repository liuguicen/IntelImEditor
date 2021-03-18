package com.mandi.intelimeditor.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;


/**
 * Created by Administrator on 2016/11/19 0019.
 */
public class FirstUseDialog {
    private AlertDialog dialog;
    private Context mContext;

    public interface ActionListener {
        void onSure();
    }

    public FirstUseDialog(Context context) {
        mContext = context;
    }

    public void createDialog(String title, String msg, final ActionListener actionListener) {
        //判断对话框是否已经存在了,防止重复点击
        if (dialog != null && dialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_first_use, null);
        TextView msgView = view.findViewById(R.id.first_use_msg);
        msgView.setText(msg);
        view.findViewById(R.id.first_use_sure).setOnClickListener(v -> {
            actionListener.onSure();
            dialog.dismiss();
        });
        dialog = builder.setView(view)
                .create();
        dialog.getWindow();
        dialog.setCanceledOnTouchOutside(true);//设置点击Dialog外部任意区域关闭Dialog
        if (title != null) {
            TextView titleTv = new TextView(mContext);
            titleTv.setText(title);
            titleTv.setGravity(Gravity.CENTER);
            int pad = Util.dp2Px(6);
            titleTv.setPadding(0, pad, 0, 0);
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.text_size_big));
            titleTv.setTextColor(Color.BLACK);
            dialog.setCustomTitle(titleTv);
        } else
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }
}
