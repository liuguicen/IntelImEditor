package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;

import java.util.ArrayList;
import java.util.List;


public class PicResGroupItemData extends PicResGroup {
    public List<PicResourceItemData> resItemList = new ArrayList<>();

    public PicResGroupItemData(PicResGroup picResGroup) {
        super(picResGroup);
        for (PicResource picResource : picResGroup.resList) {
            resItemList.add(new PicResourceItemData(picResource, PicResourceItemData.PicListItemType.ITEM));
        }
    }
}
