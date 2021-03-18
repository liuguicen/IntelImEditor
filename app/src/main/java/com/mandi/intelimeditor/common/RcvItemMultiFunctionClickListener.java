package com.mandi.intelimeditor.common;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 规范命名
 */
public interface RcvItemMultiFunctionClickListener {
    /**
     * @param itemHolder viewHolder
     * @param view       点击View
     */
    void onItemClick(RecyclerView.ViewHolder itemHolder, View view, String function);
}
