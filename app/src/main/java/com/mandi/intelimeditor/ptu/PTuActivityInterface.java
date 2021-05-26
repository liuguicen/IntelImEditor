package com.mandi.intelimeditor.ptu;

import android.graphics.Bitmap;
import android.view.View;

import com.mandi.intelimeditor.ptu.common.SecondFuncController;
import com.mandi.intelimeditor.ptu.gif.GifManager;
import com.mandi.intelimeditor.ptu.repealRedo.RepealRedoManager;
import com.mandi.intelimeditor.ptu.view.PtuSeeView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/04/29
 *      version : 1.0
 * <pre>
 */
public interface PTuActivityInterface {
    GifManager getGifManager();

    RepealRedoListener getRepealRedoListener();

    PtuSeeView getPtuSeeView();

    void showLoading(String msg);

    void dismissLoading();

    @NotNull
    View getActivityViewRoot();

    void switchFragment(int editMode, SecondFuncController secondFuncControl);

    /**
     * 替换P图的底图, 为空则无动作
     */
    void replaceBase(String url);

    /**
     * @param srcBm            P图操作的底图，放大的来源图,为空表示不显示
     * @param effect_w         画笔影响区域宽度，用于判断放大区域是否放大视图重合
     * @param xInEnlargeParent 触摸点相对于放大View父布局所在的位置
     */
    void showTouchPEnlargeView(Bitmap srcBm, float effect_w,
                               float xInEnlargeParent, float yInEnlargeParent);

    void showGuideDialog(List<String> keyWord);

    /**
     * 收集用户使用过的tag，用于推荐P图素材
     *
     * @param isTemplate 模板使用过的Tag，模板使用过的权重更大
     */
    void addUsedTags(boolean isTemplate, String resTags);

    String getBasePicPath();

    void hidePtuNotice();

    void showProgress(int progress);

    RepealRedoManager getRepealRedoRManager();

    void dismissProgress();
}
