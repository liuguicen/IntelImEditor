package com.mandi.intelimeditor.ptu.saveAndShare;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 其中的ImageView iv会放入路径
 */
class ShareViewHolder extends RecyclerView.ViewHolder {
    ImageView icon;
    TextView title;

    public ShareViewHolder(View itemView) {
        super(itemView);
        icon = (ImageView) itemView.findViewWithTag("icon");
        title = (TextView) itemView.findViewWithTag("title");
    }
}
