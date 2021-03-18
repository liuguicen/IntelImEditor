package com.mandi.intelimeditor.home;

import com.mandi.intelimeditor.common.BaseLazyLoadFragment;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/01/29
 *      version : 1.0
 * <pre>
 */
public abstract class NewChooseBaseFragment extends BaseLazyLoadFragment {
    protected static final String IS_ONLY_CHOOSE_PIC = "isOnlyChoosePic";
    /**
     * 只是选图，不做其他功能
     */
    protected boolean mIsOnlyChoosePic;

    protected abstract void startLoad();
}