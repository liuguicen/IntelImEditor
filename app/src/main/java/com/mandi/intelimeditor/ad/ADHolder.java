package com.mandi.intelimeditor.ad;

import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2020/01/04
 *      version : 1.0
 * <pre>
 *     目前只起到类别识别作用
 */
public class ADHolder extends RecyclerView.ViewHolder {
    public FrameLayout container;
    public int adWidth;
    public ADHolder(@NonNull View itemView, FrameLayout container) {
        super(itemView);
        this.container = container;
    }
}
