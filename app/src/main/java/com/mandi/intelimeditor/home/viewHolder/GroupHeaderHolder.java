package com.mandi.intelimeditor.home.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.home.view.PicGridView;
import com.mandi.intelimeditor.R;

/**
 * 分组列表viewHolder
 */
public class GroupHeaderHolder extends RecyclerView.ViewHolder {

    public TextView titleTv, moreTv;
    public PicGridView picGridView, picGridView2, picGridView3;

    public GroupHeaderHolder(View itemView) {
        super(itemView);
        itemView.setClickable(false);
        itemView.setLongClickable(false);
        titleTv = itemView.findViewById(R.id.tv_pic_header_name);
        moreTv = itemView.findViewById(R.id.tv_pic_header_more);
        picGridView = itemView.findViewById(R.id.iv_pic_1);
        picGridView2 = itemView.findViewById(R.id.iv_pic_2);
        picGridView3 = itemView.findViewById(R.id.iv_pic_3);
    }
}