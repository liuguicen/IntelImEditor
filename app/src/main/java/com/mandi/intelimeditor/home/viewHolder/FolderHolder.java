package com.mandi.intelimeditor.home.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mathandintell.intelimeditor.R;


/**
 * 文件夹ViewHolder
 */
public class FolderHolder extends RecyclerView.ViewHolder {
    public ImageView mIvFile;
    public TextView mTvInfo;
    public TextView mTvTitle;

    public FolderHolder(View itemView) {
        super(itemView);
        mIvFile = itemView.findViewById(R.id.iv_pic);
        mTvInfo = itemView.findViewById(R.id.tv_pic_file_name);
        mTvTitle = itemView.findViewById(R.id.tv_title);
    }
}