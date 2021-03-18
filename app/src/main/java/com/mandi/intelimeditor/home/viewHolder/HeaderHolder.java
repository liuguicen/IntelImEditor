package com.mandi.intelimeditor.home.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mathandintell.intelimeditor.R;


/**
 * 本地图片列表标题viewHolder
 */
public class HeaderHolder extends RecyclerView.ViewHolder {

    public TextView tv;

    public HeaderHolder(View itemView) {
        super(itemView);
        itemView.setClickable(false);
        itemView.setLongClickable(false);
        tv = itemView.findViewById(R.id.tv_pic_header_name);
    }
}