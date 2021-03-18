package com.mandi.intelimeditor.ptu;

import android.graphics.Bitmap;

/**
 * 表示item信息的类
 */
public class MicroButtonData {
    public float x;
    public float y;
    public int mode;
    public String name;
    public Bitmap bitmap;

    public MicroButtonData(float x, float y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }
}
