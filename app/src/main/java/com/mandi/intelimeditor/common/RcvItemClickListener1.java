package com.mandi.intelimeditor.common;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface RcvItemClickListener1 {
    /**
     * @param itemHolder viewHolder
     * @param view       点击View
     */
    void onItemClick(RecyclerView.ViewHolder itemHolder, View view);
}
