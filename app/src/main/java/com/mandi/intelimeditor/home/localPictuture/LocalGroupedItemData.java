package com.mandi.intelimeditor.home.localPictuture;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;

/**
 * 列表视图的分组列表的数据结构
 */
public class LocalGroupedItemData {
    public static final int MEDIA_STATIC_PIC = 1;
    public static final int MEDIA_GIF = 2;
    public static final int MEDIA_SHORT_VIDEO = 3;

    public String url; // 数据，url或问title
    /**
     * {@link PicResourceItemData.PicListItemType}
     */
    public int type; // 类型
    public int mediaType;
    public boolean isChosen;

    public LocalGroupedItemData(String url, int type) {
        this.url = url;
        this.type = type;
        isChosen = false;
        mediaType = MEDIA_STATIC_PIC;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof LocalGroupedItemData) {
            LocalGroupedItemData b = (LocalGroupedItemData) obj;
            if (b.type == type && TextUtils.equals(url, b.url)) {
                return true;
            }
        }
        return false;
    }
}