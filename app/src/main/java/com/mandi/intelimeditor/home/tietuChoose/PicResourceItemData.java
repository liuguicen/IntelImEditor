package com.mandi.intelimeditor.home.tietuChoose;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.home.data.PicDirInfo;
import com.mandi.intelimeditor.home.localPictuture.LocalGroupedItemData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/05/26
 *      version : 1.0
 * <pre>
 */
public class PicResourceItemData implements Comparable<PicResourceItemData> {

    /**
     * {@link PicResourceItemData.PicListItemType}
     */
    public int type; // 类型
    public PicResource data;

    public String headerTitle = ""; // 标题
    public List<PicResourceItemData> picResListInGroup; //分组列表
    public int groupHeat; //分组总热度
    public Date groupCreateTime; //分组最近更新时间

    public PicDirInfo picDirInfo;

    public boolean isUnlock = true; //解锁

    public PicResourceItemData(String headerTitle, boolean isNewFeature) {
        this.headerTitle = headerTitle;
        if (isNewFeature) {
            this.type = PicListItemType.NEW_FEATURE_HEADER;
        } else {
            this.type = PicListItemType.GROUP_HEADER;
        }
        this.isUnlock = false;
    }

    public PicResourceItemData(PicDirInfo data, int type, boolean isUnlock) {
        super();
        this.picDirInfo = data;
        this.type = type;
        this.isUnlock = isUnlock;
    }

    public PicResourceItemData(@Nullable PicResource data, int type) {
        super();
        this.data = data;
        this.type = type;
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

    @Override
    public int compareTo(PicResourceItemData itemData) {
        if (itemData == null)
            return 1;
        //分组热度排序
        if (groupHeat != 0 && AllData.sortByGroup == 0) {
            Integer heat1 = this.groupHeat;
            Integer heat2 = itemData.groupHeat;
            return heat1 - heat2;
        }
        //分组时间排序
        if (groupHeat != 0 && AllData.sortByGroup == 1 && groupCreateTime != null && itemData.groupCreateTime != null) {
            return itemData.groupCreateTime.compareTo(groupCreateTime);
        }
        if (itemData.data == null && this.data == null)
            return 0;
        if (this.data == null)
            return -1;
        if (itemData.data == null)
            return 1;
        //普通列表排序
        if (AllData.sortByGroup == 1) {
            try {
                Date date = AllData.bmobDataParser.parse(this.data.getCreatedAt());
                Date date1 = AllData.bmobDataParser.parse(itemData.data.getCreatedAt());
                if (date != null && date1 != null) {
                    return date1.compareTo(date);
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else {
            Integer heatA = this.data.getHeat();
            Integer heatB = itemData.data.getHeat();
            if (heatA != null && heatB != null) {
                return heatA - heatB;
            }
            return 0;
        }
    }

    /**
     * <pre>
     *      author : liuguicen
     *      time : 2019/05/26
     *      version : 1.0
     * <pre>
     */
    public static class PicListItemType {
        public static final int GROUP_HEADER = 0;//标题
        public static final int ITEM = 1;//图片
        public static final int TX_PIC_AD = 2;//腾讯广告
        public static final int SHORT_VIDEO = 4;
        public static final int FEED_AD = 5;//广告
        public static final int ITEM_FOLDER = 6;//文件夹
        public static final int NEW_FEATURE_HEADER = 7;//模板列表顶部新功能
    }
}
