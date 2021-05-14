package com.mandi.intelimeditor.home.tietuChoose;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.home.data.PicDirInfo;
import com.mandi.intelimeditor.home.localPictuture.LocalGroupedItemData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroupItemData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/26
 *      version : 1.0
 * <pre>
 */
public class PicResourceItemData implements MultiItemEntity {

    /**
     * {@link PicResourceItemData.PicListItemType}
     */
    public int type; // 类型
    public String newFeatureTitle = ""; // 标题
    public PicResource data;
    public PicResGroupItemData picResGroup;
    public PicDirInfo picDirInfo;
    public boolean isUnlock = true; //解锁

    public PicResourceItemData(String newFeatureTitle) {
        this.newFeatureTitle = newFeatureTitle;
        this.type = PicListItemType.NEW_FEATURE_HEADER;
        this.isUnlock = false;
    }

    public PicResourceItemData(PicDirInfo data, int type, boolean isUnlock) {
        this.picDirInfo = data;
        this.type = type;
        this.isUnlock = isUnlock;
    }

    public PicResourceItemData(@Nullable PicResource data, int type) {
        this.data = data;
        this.type = type;
    }

    public PicResourceItemData(PicResGroup group, int type) {
        picResGroup = new PicResGroupItemData(group);
        this.type = type;
    }

    public PicResourceItemData(String s, int type) {
        this.type = type;
    }

    public static List<PicResourceItemData> picResList2PicResItemList(List<PicResource> resList) {
        List<PicResourceItemData> itemList = new ArrayList<>();
        for (int i = 0; i < resList.size(); i++) {
            itemList.add(new PicResourceItemData(resList.get(i), PicResourceItemData.PicListItemType.ITEM));
        }
        return itemList;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof LocalGroupedItemData) {
            LocalGroupedItemData b = (LocalGroupedItemData) obj;
            if (b.type == type) {
                if (data != null && data.equals(obj)) {
                    return true;
                }
                if (data == null && b.url == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public Integer getHeat() {
        if (type == PicListItemType.ITEM) {
            return data.getHeat();
        }
        if (type == PicListItemType.GROUP) {
            return picResGroup.heat;
        }
        return 0;
    }

    public String getCreatedAt() {
        if (type == PicListItemType.ITEM) {
            return data.getCreatedAt();
        }
        return "0000-00-00 00:00:00";
    }

    @Override
    public int getItemType() {
        return type;
    }

    /**
     * <pre>
     *      author : liuguicen
     *      time : 2019/05/26
     *      version : 1.0
     * <pre>
     */
    public static class PicListItemType {
        public static final int ITEM = 1; // 图片
        public static final int TX_PIC_AD = 2; // 腾讯广告
        public static final int GROUP = 3; // tag 得到的分组
        public static final int FEED_AD = 5; // 广告
        public static final int ITEM_FOLDER = 6; // 文件夹
        public static final int NEW_FEATURE_HEADER = 7; // 模板列表顶部新功能
    }
}
