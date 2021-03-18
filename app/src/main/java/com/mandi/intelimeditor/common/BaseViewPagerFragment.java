package com.mandi.intelimeditor.common;


import com.mandi.intelimeditor.common.util.LogUtil;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/02/04
 *      version : 1.0
 *      用于控制ViewPager中的Fragment实现懒加载
 *      注意：
 *      1、ViewPager中Fragment调用的情况是，当前显示的Fragment左右两边的Fragment都会预加载走到onResume这个周期
 *      并且这个改不了，至少预加载一个
 *      2、另一个注意，当前Fragment旁边的旁边的Fragment会走到onDestroyView
 *
 * <pre>
 */
public abstract class BaseViewPagerFragment extends BaseFragment {
    private boolean isFragmentVisible;
    private boolean isReuseView;
    private boolean isFirstVisible;
    private boolean isFirstResume;
    private boolean isLoaded;


    public String TAG = getClass().getSimpleName();

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        initVariable();
//    }

//    private void initVariable() {
//        isFirstVisible = true;
//        isFragmentVisible = false;
//        rootView = null;
//        isReuseView = false;
//        isFirstResume = true;
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(isReuseView ? rootView : view, savedInstanceState);
////        dealVisibleStatus(getUserVisibleHint());
//    }

    /**
     * ViewPager里面onResume不一定是真的resume
     */
    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            isFragmentVisible = true;
            isLoaded = true;
        } else {
            isFragmentVisible = false;
        }
        loadData(isFragmentVisible);
        LogUtil.d(TAG, "onResume");
//        if (isFirstResume) {
//            isFirstResume = false;
//            return;
//        }
//        // onResume中setUserVisibleHint没有发生调用，和网上代码不一样？这里只能手动调用了可见代码了
//        dealVisibleStatus(getUserVisibleHint());
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        LogUtil.d("setUserVisibleHint = " + isVisibleToUser);
//        super.setUserVisibleHint(isVisibleToUser);
//        if (rootView == null) {
//            return;
//        }
//        dealVisibleStatus(isVisibleToUser);
//    }
/*

    public void dealVisibleStatus(boolean isUserVisible) {
        if (isUserVisible && isFirstVisible) { // 第一次可见
            loadData(true);
            isFirstVisible = false;
        } else {
            if (isUserVisible) {
                loadData(false);
            }
        }
        isFragmentVisible = isUserVisible;
    }
*/

    /**
     *
     */
    public abstract boolean onBackPressed();

    public void cancelChosen() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d(TAG, "onDestroyView");
        isLoaded = false;
    }

    /**
     * 加载数据
     */
    public void loadData(boolean isFirstVisible) {
        LogUtil.d(TAG, "懒加载 加载数据 " + isFirstVisible);
    }
}
