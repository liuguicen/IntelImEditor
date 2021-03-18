package com.mandi.intelimeditor.common;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Recyclerview点击事件
 */
public interface RcvItemClickListener {
    /**
     * @param itemHolder viewHolder
     * @param view        点击View
     * @param position   位置 需要手动传入position，不能用getLayoutPosition，getAdapterPosition等，会返回-1
     *
     */
    void onItemClick(RecyclerView.ViewHolder itemHolder, View view, int position);
}
