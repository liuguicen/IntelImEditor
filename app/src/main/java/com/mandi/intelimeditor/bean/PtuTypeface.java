package com.mandi.intelimeditor.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 作者：yonglong
 * 包名：a.baozouptu.bean
 * 时间：2019/7/25 10:06
 * 描述：
 */
public class PtuTypeface extends BmobObject {
    private String name;
    private String pic;
    private String size;
    private BmobFile typeface;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BmobFile getTypeface() {
        return typeface;
    }

    public void setTypeface(BmobFile typeface) {
        this.typeface = typeface;
    }

    @Override
    public String toString() {
        return "PtuTypeface{" +
                "name='" + name + '\'' +
                ", pic='" + pic + '\'' +
                ", size='" + size + '\'' +
                ", typeface=" + typeface +
                '}';
    }
}
