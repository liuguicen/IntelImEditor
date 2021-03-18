package com.mandi.intelimeditor.home.tietuChoose;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.mandi.intelimeditor.common.util.LogUtil;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/06/19
 *      version : 1.0
 * <pre>
 */
class MyConstraintLayout extends ConstraintLayout {
    public MyConstraintLayout(Context context) {
        super(context);
    }

    @Override
    public void setVisibility(int visibility) {
        LogUtil.d("visibility = " + visibility);
        super.setVisibility(visibility);
    }
}
