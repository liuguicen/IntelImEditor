/**********************************************************************
 * AUTHOR：YOLANDA
 * DATE：2015年3月7日下午2:18:51
 * Copyright © 56iq. All Rights Reserved
 * ======================================================================
 * EDIT HISTORY
 * ----------------------------------------------------------------------
 * |  DATE      | NAME       | REASON       | CHANGE REQ.
 * ----------------------------------------------------------------------
 * | 2015年3月7日    | YOLANDA    | Created      |
 * <p>
 * DESCRIPTION：create the File, and add the content.
 ***********************************************************************/
package com.mandi.intelimeditor.ptu.saveAndShare;

import android.graphics.drawable.Drawable;

import java.io.Serializable;

/**
 * 用于显示到分享的ListView的ItemData
 * 包含包名，应用，图标，图标是Drawable对象
 */
public class ShareItemData implements Serializable {

    private static final long serialVersionUID = 1L;

    private CharSequence packageName;
    private CharSequence title;
    private Drawable icon;

    /**
     * @return the title
     * @author YOLANDA
     */
    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getPackageName() {
        return packageName;
    }

    /**
     * @param title the title to set
     * @author YOLANDA
     */
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    /**
     * @return the icon
     * @author YOLANDA
     */
    public Drawable getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     * @author YOLANDA
     */
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    /**
     * @param packageName
     * @param title
     * @param icon
     * @author YOLANDA
     */
    public ShareItemData(CharSequence packageName, CharSequence title, Drawable icon) {
        super();
        this.packageName = packageName;
        this.title = title;
        this.icon = icon;
    }

    /**
     * @author YOLANDA
     */
    public ShareItemData() {
        super();
    }

}
