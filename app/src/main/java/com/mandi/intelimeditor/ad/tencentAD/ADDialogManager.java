package com.mandi.intelimeditor.ad.tencentAD;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mathandintell.intelimeditor.R;


/**
 * Created by liuguicen on 2016/8/13.
 *
 * @description
 */
public class ADDialogManager {
    private Context mContext;
    AlertDialog dialog;
    private TextView returnBtn;
    private TextView continueBtn;
    private TextView deleteBtn;
    private clickListenerInterface listener;
    public static final int SAVE_FINISH = 1;
    public static final int SHARE_FINISH = 2;
    private TxFeedAd txFeedAd;
    private FrameLayout adContainer;

    public interface clickListenerInterface {
        void continuePtu();

        void returnChoose();

        void deleteSave();
    }

    public ADDialogManager(Context context) {
        mContext = context;
    }

    public void createDialog(int type) {
        //判断对话框是否已经存在了
        if (dialog != null && dialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_ad, null);
        returnBtn = view.findViewById(R.id.ad_dialog_first);
        continueBtn = view.findViewById(R.id.ad_dialog_second);
        deleteBtn = view.findViewById(R.id.ad_dialog_third);
        adContainer = view.findViewById(R.id.ad_dialog_container);
        setContent(type);

        dialog = builder.setView(view)
                .create();
        setStyle();

        dialog.show();
    }

    private void setContent(int type) {
        returnBtn.setText("返回");
        continueBtn.setText("继续");
        if(type == SAVE_FINISH) {
            deleteBtn.setVisibility(View.GONE);
        } else if(type == SHARE_FINISH) {
            deleteBtn.setVisibility(View.VISIBLE);
            deleteBtn.setText("删除本地");
        }
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setClickListener(final clickListenerInterface listenner) {
        this.listener = listenner;
        continueBtn.setOnClickListener(v -> {
            dialog.dismiss();
            listenner.continuePtu();
        });
        returnBtn.setOnClickListener(v -> {
            dialog.dismiss();
            listenner.returnChoose();
        });
        deleteBtn.setOnClickListener(v->{
            dialog.dismiss();
            listenner.deleteSave();
        });
    }

    /**
     * 设置风格：无标题
     */
    private void setStyle() {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }


}

