package com.mandi.intelimeditor.common.dataAndLogic;

import androidx.annotation.Nullable;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/08/15
 *      version : 1.0
 *      用来P图的文件的类型，目前有一般的静态图，PNG，JPG
 *      动图：GIF
 *      短视频
 * <pre>
 *
 */
public class PTuFile {
    public static final int TYPE_STATIC_PIC = 1;
    public static final int TYPE_GIF = 2;
    public static final int SHORT_VIDEO = 3;
    public String path;
    public int type;

    /**
     * @param type {@link #TYPE_STATIC_PIC}等
     */
    public PTuFile(String path, int type) {
            this.path = path;
            this.type = type;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof PTuFile)) {
            return false;
        }

        PTuFile b = (PTuFile) obj;
        return type == b.type && (path == b.path || (path != null && path.equals(b.path))) ;
    }
}
