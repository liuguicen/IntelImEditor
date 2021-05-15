package com.mandi.intelimeditor.ptu.viewHolder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class PicViewHolder extends RecyclerView.ViewHolder {
    ImageView iv;
    ImageView lockView;

    public PicViewHolder(View itemView, ImageView iv) {
        super(itemView);
        this.iv = iv;
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

}