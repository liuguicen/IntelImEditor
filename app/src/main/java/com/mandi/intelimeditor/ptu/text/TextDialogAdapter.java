package com.mandi.intelimeditor.ptu.text;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.RcvItemClickListener;
import com.mandi.intelimeditor.common.util.Util;

/**
 * 给文字下面加一个对话框功能
 * 对话框列表
 */
public class TextDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    final int[] dialogIdList;
    private RcvItemClickListener itemClickListener;

    TextDialogAdapter(Context context, int[] dialogIdList) {
        mContext = context;
        this.dialogIdList = dialogIdList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        ImageView imageView = new ImageView(mContext);
        int pad = Util.dp2Px(4);
        imageView.setPadding(pad, pad, pad, pad);
        ViewHolder viewHolder = new ViewHolder(imageView);
        imageView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(viewHolder, v, position);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ((ImageView) viewHolder.itemView).setImageResource(dialogIdList[position]);
    }

    @Override
    public int getItemCount() {
        return dialogIdList.length;
    }

    void setOnItemClickListener(RcvItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
