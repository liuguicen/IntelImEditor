package com.mandi.intelimeditor.home.viewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mathandintell.intelimeditor.R;


/**
 * 模版列表顶部换脸功能item
 */
public class NewFeatureHeaderHolder extends RecyclerView.ViewHolder {
    public TextView changeFaceTitleTv;
    public TextView changeFaceNewNoticeTv;

    public NewFeatureHeaderHolder(View itemView) {
        super(itemView);
        itemView.setClickable(false);
        itemView.setLongClickable(false);
        changeFaceTitleTv = itemView.findViewById(R.id.tv_chang_face_title);
        changeFaceNewNoticeTv = itemView.findViewById(R.id.change_face_new_notice);
    }
}