package com.mandi.intelimeditor.common.view;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.R;

public class SimplePaddingDecoration extends RecyclerView.ItemDecoration {

    private int dividerHeight;

    public SimplePaddingDecoration(Context context) {
        dividerHeight = context.getResources().getDimensionPixelSize(R.dimen.dp_8);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = dividerHeight;//类似加了一个bottom padding
        outRect.left = dividerHeight;//类似加了一个bottom padding
    }
}