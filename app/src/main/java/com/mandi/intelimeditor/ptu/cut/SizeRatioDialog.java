package com.mandi.intelimeditor.ptu.cut;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.R;


class SizeRatioDialog {
    private EditText widthText, heightText;
    private AlertDialog dialog;
    private Context mContext;

    /**
     * 获取并设置参数，
     *
     * @return 有效返回true，否则返回false;
     */
    private boolean getAndSetParams() {
        if (type == 0) {
            String s = widthText.getText().toString();
            if (s.equals("")) return false;
            w = Float.valueOf(s);
            s = heightText.getText().toString();
            if (s.equals("")) return false;
            h = Float.valueOf(s);
            if (w / h > MAX_SIZE)
                ToastUtils.show( "超过最大尺寸");
            if (w == 0 || h == 0) {
                return false;
            }
            return true;
        } else if (type == 1) {
            String s = widthText.getText().toString();
            if (s.equals("")) return false;
            w = Float.valueOf(s);
            s = heightText.getText().toString();
            if (s.equals("")) return false;
            h = Float.valueOf(s);
            if (w / h > MAX_RATIO || w / h < MIN_RATIO) {
                ToastUtils.show( "超过最大比例");
                return false;
            }
            if (w == 0 || h == 0) {
                return false;
            }
            return true;
        }
        return false;
    }


    interface ActionListener {
        void onSure(float w, float h);
    }

    private ActionListener actionListener;

    /**
     * 长宽或者长款的比例数
     */
    private float w = 0, h = 0;
    /**
     * 固定尺寸或者比例
     * <P>0:固定尺寸
     * <p>1:固定比例
     */
    private int type;
    private static final float MAX_SIZE = 25 * 1000000;
    private static final float MAX_RATIO = 50;
    private static final float MIN_RATIO = 0.02f;

    SizeRatioDialog(Context context, int type) {
        mContext = context;
        this.type = type;
    }

    void createDialog() {
        //判断对话框是否已经存在了
        if (dialog != null && dialog.isShowing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_size_or_ratio, null);
        widthText = view.findViewById(R.id.cut_width_text);
        widthText.setInputType(InputType.TYPE_CLASS_NUMBER);
        widthText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (!str.equals("")) {
                    h = Float.valueOf(str);
                }
            }
        });

        heightText = view.findViewById(R.id.cut_height_text);
        heightText.setInputType(InputType.TYPE_CLASS_NUMBER);
        heightText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (!str.equals("")) {
                    h = Float.valueOf(str);
                }
            }
        });

        Button sureBn = view.findViewById(R.id.cut_sr_sure);
        sureBn.setOnClickListener(v -> {
            if (getAndSetParams())
                actionListener.onSure(w, h);
            dialog.dismiss();
        });
        Button cancelBn = view.findViewById(R.id.cut_sr_cancle);
        cancelBn.setOnClickListener(v -> dialog.dismiss());


        dialog = builder.setView(view)
                .create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

