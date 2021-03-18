package com.mandi.intelimeditor.ptu.tietu.tietuImpact;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;


/**
 * Created by LiuGuicen on 2016/12/31 0031.
 */

public class PictureSynthesis {
    public static final String TAG = "PictureSynthesis";
    public static boolean success = false;

    static {
        if (!success) {
            System.loadLibrary("imageSynthesis");
            Log.e(TAG, "static initializer: 成功");
            success = true;
        }
    }

    /**
     * @param under    底部图片的大小
     * @param above    上面图片的大小
     * @param interRect 相交的矩形
     * @return 底部的结果图
     */
    public Bitmap changeBm(Bitmap under, Bitmap above, Rect interRect) {
        Log.e("创建缩放图完成", " ");
        int[] inter_rect = new int[]{interRect.left, interRect.top, interRect.right, interRect.bottom};

        Log.d("asySynthesis",
                "\n底图大小=" + under.getWidth() + " * " + under.getHeight()
                        + "贴图大小=" + above.getWidth() +" * " + above.getHeight());

        int[] rePixes = synthesisBm(under, above, inter_rect);

        //只转换上面
        if (rePixes == null) {
            Log.e(TAG, "changeBm: 图像融合失败");
            return null;
        }

        Log.e(TAG, "转换bitmap成功");
        above.setPixels(rePixes, 0, above.getWidth(), 0, 0, above.getWidth(),above.getHeight());
        return above;
    }

    /**
     * @param under             底部的bm
     * @param above             顶部的bm
     * @param intersection_rect 必须是in_rect依次是 左上右下
     * @return bm的pixes int数组
     */
    private native int[] synthesisBm(Object under, Object above, int[] intersection_rect);

}
//javah -d ../jni  -encoding UTF-8  a.baozouptu.ptu.tietu.pictureSynthesis.PictureSynthesis
