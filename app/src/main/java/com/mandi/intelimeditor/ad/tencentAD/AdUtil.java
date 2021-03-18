package com.mandi.intelimeditor.ad.tencentAD;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.mandi.intelimeditor.common.util.Util;
import com.mathandintell.intelimeditor.R;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/12
 *      version : 1.0
 * <pre>
 */
public class AdUtil {
    public static final String SKIP_TEXT = "点击跳过 %d";

    public static TextView createAdMark(Context context) {
        return createAdMarkInternal(context, context.getResources().getDimension(R.dimen.text_very_small), Util.getColor(R.color.text_ad_mark));
    }

    public static TextView createAdMark_small(Context context) {
        return createAdMarkInternal(context, context.getResources().getDimension(R.dimen.text_very_very_small), Util.getColor(R.color.text_ad_mark));
    }

    private static TextView createAdMarkInternal(Context context, float textSize, @ColorInt int color) {
        TextView textView = new TextView(context);
        textView.setTextColor(color);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        textView.setText(context.getString(R.string.ad));
        textView.setBackgroundResource(R.drawable.background_ad_mark);
        int pad = Util.dp2Px(1);
        textView.setPadding(pad * 3, 0, 0, 0);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        textView.setLayoutParams(layoutParams);
        textView.setVisibility(View.GONE);
        return textView;
    }

}
