package com.mandi.intelimeditor.common.util;


import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;

public abstract class SimpleGlideTarget<T> extends CustomTarget<T> {
    public SimpleGlideTarget(int outWidth, int outHeight) {
        super(outWidth, outHeight);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {

    }
}
