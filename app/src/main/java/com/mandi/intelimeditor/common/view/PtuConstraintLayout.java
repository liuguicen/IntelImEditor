package com.mandi.intelimeditor.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.TietuRecyclerAdapter;
import com.mathandintell.intelimeditor.R;


/**
 * Created by LiuGuicen on 2017/2/18 0018.
 */

public class PtuConstraintLayout extends ConstraintLayout {

    public static final String TAG_TIETU_RCV = "tietuRecyclerView";

    public PtuConstraintLayout(Context context) {
        super(context);
    }

    public PtuConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PtuConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        View tietuRcv = findViewWithTag(TAG_TIETU_RCV);
        View functionRcv = findViewById(R.id.main_function_rcv);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN && tietuRcv != null) {
            int[] xy = new int[2];
            getLocationOnScreen(xy);
            xy[0] += event.getX();
            xy[1] += event.getY();
            if (!Util.pointInView(xy[0], xy[1], tietuRcv) && !Util.pointInView(xy[0], xy[1], functionRcv )) {
                ((RecyclerView) tietuRcv).setAdapter(null);
                removeView(tietuRcv);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public void addPicResourceLv(RecyclerView tietuRecyclerView) {
        View fragmentView = findViewById(R.id.fragment_main_function);
//        LogUtil.d("PtuConstraintLayout", "addPicResourceLv");
        if (tietuRecyclerView != null) {
            addView(tietuRecyclerView, getTietuListLayoutParams(fragmentView));
        }
    }

    private LayoutParams getTietuListLayoutParams(View fragmentView) {
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_CONSTRAINT, (int) (fragmentView.getHeight() * TietuRecyclerAdapter.DEFAULT_ROW_NUMBER * 1.4
                + Util.dp2Px(18)));
        layoutParams.leftToLeft = LayoutParams.PARENT_ID;
        layoutParams.rightToRight = LayoutParams.PARENT_ID;
        layoutParams.bottomToTop = R.id.fragment_main_function;
        layoutParams.setMargins(0, 0, 0, Util.dp2Px(4f));
        return layoutParams;
    }
}
