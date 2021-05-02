package com.mandi.intelimeditor.common;


import com.mandi.intelimeditor.common.util.LogUtil;

/**
 * 用于控制ViewPager中的Fragment实现懒加载
 * 懒加载定义：viewpager加载fragment时，fragment会初始化UI，但是不会调用接口去请求数据，已经加载过数据的fragment也不会去重新请求数据。
 */
public abstract class BaseLazyLoadFragment extends BaseFragment {

    /**
     * 是否加载过数据
     */
    protected boolean isLoaded;

    /**
     * androidx Fragment+ViewPage2时，fragment加载时会走onResume,所以在这里处理懒加载
     */
    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            loadData(true);
            isLoaded = true;
        } else {
            loadData(false);
        }
        LogUtil.d(TAG, "onResume");
    }

    /**
     * 加载数据
     */
    public void loadData(boolean isFirstVisible) {
        LogUtil.d(TAG, "懒加载 加载数据 " + isFirstVisible);
    }

    /******************************************************************************
     * 公共抽象方法
     ***************************************************************************************/
    public abstract boolean onBackPressed();

    public void cancelChosen() {
    }
}
