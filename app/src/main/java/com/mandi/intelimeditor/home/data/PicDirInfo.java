package com.mandi.intelimeditor.home.data;

import android.text.SpannableString;

/**
 * Created by LiuGuicen on 2017/1/18 0018.
 */
public class PicDirInfo {
    private String dirPath;
    private SpannableString picNumInfo;
    private String representPicPath;

    public SpannableString getPicNumInfo() {
        return picNumInfo;
    }

    public String getDirPath() {
        return dirPath;
    }

    public String getRepresentPicPath() {
        return representPicPath;
    }

    public PicDirInfo(String dirPath, SpannableString picNumInfo, String representPicPath) {
        this.dirPath = dirPath;
        this.picNumInfo = picNumInfo;
        this.representPicPath = representPicPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PicDirInfo))
            return false;
        PicDirInfo info = (PicDirInfo) obj;
        return (dirPath == info.dirPath || dirPath != null && dirPath.equals(info.dirPath))
                && (picNumInfo == info.picNumInfo || picNumInfo != null && picNumInfo.equals(info.picNumInfo))
                && (representPicPath == info.representPicPath || representPicPath != null && representPicPath.equals(info.representPicPath));
    }

    @Override
    public int hashCode() {
        return (dirPath + picNumInfo + representPicPath).hashCode();
    }
}
