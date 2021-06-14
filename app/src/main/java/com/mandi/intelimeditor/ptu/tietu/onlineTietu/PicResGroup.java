package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import java.util.ArrayList;
import java.util.List;

public class PicResGroup {
    public String title = "";
    public int heat = 0;
    public List<PicResource> resList = new ArrayList<>();
    public static final int MIN_GROUP_SIZE = 1;

    public PicResGroup(String title) {
        this.title = title;
    }

    public PicResGroup(PicResGroup group) {
        this.title = group.title;
        this.heat = group.heat;
        this.resList.addAll(group.resList);
    }

    public void addPicRes(PicResource picRes) {
        if (picRes == null) return;
        resList.add(picRes);
        if (picRes.getHeat() == null) return;
        heat += picRes.getHeat();
    }

    public void addPicResList(List<PicResource> resList) {
        if (resList == null) return;
        for (PicResource one : resList) {
            addPicRes(one);
        }
    }
}
