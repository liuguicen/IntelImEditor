package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.TimeDateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 重要：这是APP重要的基础数据类
 * Created by LiuGuicen on 2017/2/17 0017.
 * 图片素材
 *
 * @notice 类名不能改，要和后台一致
 */
public class PicResource extends BmobObject {
    public static final String SECOND_CLASS_EXPRESSION = "expression"; // 名称不能改了，要和后台一致
    //    public static final String SECOND_CLASS_NAME_EXPRESSION = "表情";
    public static final String SECOND_CLASS_PROPERTY = "property"; // 名称不能改了，要和后台一致
    public static final String CATEGORY_STYLE = "style"; // 名称不能改了，要和后台一致
    //    public static final String CATEGORY_NAME_PROPERTY = "道具";
    public static final String SECOND_CLASS_MY = "my";
    public static final String SECOND_CLASS_MY_NAME = "我的";
    public static final String SECOND_CLASS_BASE = "base";
    //    public static final String SECOND_CLASS_NAME_BASE = "基本";
    public static final String SECOND_CLASS_DEFORMATION = "deformation"; // 人脸等变形需要的

    public static final String FIRST_CLASS_TIETU = "tietu";  // 名称不能改了，要和后台一致
    public static final String FIRST_CLASS_TEMPLATE = "template";  // 名称不能改了，要和后台一致
    public static final String FIRST_CLASS_LOCAL = "local";  // 本地标识

    public static final String ALL_STICKER_LIST = "ALL_STICKER_LIST";//所有贴图和道具列表

    public static final String PIC_STICKER_HOT_LIST = "pic_hot_list";//表情、道具最热列表
    public static final String PIC_STICKER_LATEST_LIST = "pic_latest_list";//表情、道具最新列表

    private BmobFile url;
    private String category;//
    private Integer heat; // 热度
    private String resourceClass; // 大的资源类别
    private String tag; // 图片标签

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @notice 开始命名没注意这个不是真的url，是bmobFile类，用的时候别被误导了
     * {@link #getUrlString()}
     */
    @Nullable
    public BmobFile getUrl() {
        return url;
    }

    @Nullable
    public String getUrlString() {
        if (url == null) return null;
        return this.url.getUrl();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setUrl(BmobFile url) {
        this.url = url;
    }

    public Integer getHeat() {
        return heat;
    }

    public void setHeat(Integer heat) {
        this.heat = heat;
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public void setResourceClass(String resourceClass) {
        this.resourceClass = resourceClass;
    }

    public String getTheOnlyName() {
        return url.getUrl().substring(url.getUrl().lastIndexOf("/") + 1);
    }

    /**
     * 更新热度
     */
    public void updateHeat() {
        if (TextUtils.isEmpty(getObjectId())) { // 有可能本地方便使用创建的这个对象，不更新热度
            return;
        }
        float updateRatio = 0.05f;
        try { // 根据创建时间，创建时间新更新率越高
            Date createDate = AllData.bmobDataParser.parse(getCreatedAt());
            if (createDate != null) {
                long interval = System.currentTimeMillis() - createDate.getTime();
                if (interval > TimeDateUtil.DAY_MILS * 90) {
                    // updateRatio *= 1  == nothing
                } else if (interval > TimeDateUtil.DAY_MILS * 60) {
                    updateRatio *= 2;
                } else if (interval > TimeDateUtil.DAY_MILS * 30) {
                    updateRatio *= 3;
                } else if (interval > TimeDateUtil.DAY_MILS * 10) {
                    updateRatio *= 4;
                } else if (interval > TimeDateUtil.DAY_MILS * 5) {
                    updateRatio *= 5;
                } else if (interval > TimeDateUtil.DAY_MILS * 3) {
                    updateRatio *= 6;
                } else {
                    updateRatio *= 7;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (AllData.sRandom.nextDouble() > updateRatio) {
            return;
        }

        // 更低的概率更新，减少服务器压力
        BmobQuery<PicResource> query = new BmobQuery<>();
        query.addWhereEqualTo("objectId", getObjectId());
        //先查询，查到了更新
        query.findObjects(
                new FindListener<PicResource>() {
                    @Override
                    public void done(List<PicResource> list, BmobException e) {
                        if (e != null || list == null || list.size() == 0) {
                            return;
                        }

                        PicResource servicePR = list.get(0);
                        heat = servicePR.getHeat();
                        if (heat == null) {
                            return;
                        }

                        heat++;
                        PicResource.super.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {

                            }
                        });
                    }
                });

    }

    /**
     * 只比较url，它作为唯一的区分
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj instanceof PicResource) {
            PicResource b = (PicResource) obj;
            if (url == b.url) return true; // 两个空或者相同对象
            if (url != null && b.url != null) { // 两个非空
                if (url.getUrl() == b.url.getUrl()) return true; // 同上
                return url.getUrl() != null && url.getUrl().equals(b.url.getUrl());

            } // 一个空，一个非空 = false
        }
        return false;
    }

    public static PicResource path2PicResource(String path) {
        PicResource picResource = new PicResource();
        picResource.initForNonnull();
        BmobFile url = new BmobFile();
        url.setUrl(path);
        picResource.setUrl(url);
        return picResource;
    }

    public static List<PicResource> pathList2PicResList(List<String> pathList) {
        if (pathList == null) return null;
        List<PicResource> picResourceList = new ArrayList<>(pathList.size());
        for (String path : pathList) {
            picResourceList.add(path2PicResource(path));
        }
        return picResourceList;
    }

    private void initForNonnull() {
        tag = "";
        category = "";
        heat = 0;
    }

}
