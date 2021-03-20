package com.mandi.intelimeditor.ad.ttAD.videoAd;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/09/27
 *      version : 1.0
 * <pre>
 */

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mandi.intelimeditor.R;


/**
 * 选择是否观看广告的对话框
 * 待完成
 */
public class DialogChooseSeeAd {

    private AlertDialog mDialog;
    private Context mContext;
    private int time;
    private Runnable mAdTask;
    private TextView mHaveALookTv;

    public void show(Context context, String title, String adName, Runnable task) {
        this.mContext = context;
        if (mDialog!=null) {
            mDialog.dismiss();
            mDialog = null;
        }
        time = 5;
        mAdTask = task;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_choose_see_ad, null);

        mDialog = builder.setView(view)
                .create();
        mDialog.getWindow();

        mDialog.setCanceledOnTouchOutside(false);//设置点击Dialog外部任意区域关闭Dialog
        // if (title != null) {
        //     TextView titleTv = new TextView(context);
        //     titleTv.setText(title);
        //     titleTv.setGravity(Gravity.CENTER);
        //     int pad = Util.dp2Px(6);
        //     titleTv.setPadding(0, pad, 0, 0);
        //     titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.text_size_big));
        //     titleTv.setTextColorWithOpacity(Color.BLACK);
        //     dialog.setCustomTitle(titleTv);
        // } else
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        TextView contentTv = view.findViewById(R.id.choose_see_ad_content);
        contentTv.setText(title);
        TextView doNotSeeBtn = view.findViewById(R.id.do_not_see);
        doNotSeeBtn.setOnClickListener(v -> {
            mDialog.dismiss();
        });
        mHaveALookTv = view.findViewById(R.id.have_a_look);
        mHaveALookTv.setText(mContext.getString(R.string.have_a_look, time));
        mHaveALookTv.setOnClickListener(v -> {
            task.run(); // 显示广告
            mDialog.dismiss();
        });
        mDialog.show();

        mHaveALookTv.postDelayed(new showAdCountDown(), 1000);
    }

    class showAdCountDown implements Runnable{
        @Override
        public void run() {
            if (!mDialog.isShowing()) {
                return;
            }
            time--;
            if (time <= 0) {
                mHaveALookTv.post(mAdTask);
                mDialog.dismiss();
            } else {
                mHaveALookTv.setText(mContext.getString(R.string.have_a_look, time));
                mHaveALookTv.postDelayed(new showAdCountDown(), 1000);
            }
        }
    };

    public void clear() {

    }
}
