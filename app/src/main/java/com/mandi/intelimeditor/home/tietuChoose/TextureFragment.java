package com.mandi.intelimeditor.home.tietuChoose;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.home.ChooseBaseFragment;

import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ViewPager2FragmentAdapter;
import com.mathandintell.intelimeditor.R;


/**
 * 用来持有贴图的多个分类的列表
 * 内部的每个列表是用{@link PicResourcesFragment}来持有
 */
public class TextureFragment extends ChooseBaseFragment {

    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private PicResourcesFragment mTietuExpressionFragment;
    private PicResourcesFragment mTietuPropertyFragment;
    private PicResourcesFragment mMyTietuFragment;

    public static TextureFragment newInstance(String name, boolean isOnlyChoosePic) {

        Bundle args = new Bundle();

        TextureFragment fragment = new TextureFragment();
        fragment.setArguments(args);
        args.putBoolean(IS_ONLY_CHOOSE_PIC, isOnlyChoosePic);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIsOnlyChoosePic = getArguments().getBoolean(IS_ONLY_CHOOSE_PIC);
        }
    }

    /**
     * Fragment第一次可见时加载，且Fragment从创建到销毁期间只加载一次
     */
//    @Override
//    public void onFragmentFirstVisible() {
//
//    }

//    /**
//     * 当可见情况改变时调用，第一次可见会在它之前调用
//     *
//     * @param isVisible
//     * @param isFirstVisible
//     */
//    @Override
//    public void onFragmentVisibleChange(boolean isVisible, boolean isFirstVisible) {
//
//    }
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_chose_texture;
    }

    @Override
    public void initView() {
        super.initView();
        mTabLayout = rootView.findViewById(R.id.second_tabLayout);
        mViewPager = rootView.findViewById(R.id.choose_content_view_pager);
    }

    @Override
    public void loadData(boolean isFirstVisible) {
        super.loadData(isFirstVisible);
        if (isFirstVisible) {
            if (mTabLayout != null) {
                mTietuExpressionFragment = PicResourcesFragment.newInstance(PicResource.FIRST_CLASS_TIETU,
                        PicResource.SECOND_CLASS_EXPRESSION, mIsOnlyChoosePic);
                mTietuPropertyFragment = PicResourcesFragment.newInstance(PicResource.FIRST_CLASS_TIETU,
                        PicResource.SECOND_CLASS_PROPERTY, mIsOnlyChoosePic);
                mMyTietuFragment = PicResourcesFragment.newInstance(PicResource.FIRST_CLASS_TIETU,
                        PicResource.SECOND_CLASS_MY, mIsOnlyChoosePic);

                ViewPager2FragmentAdapter pagerAdapter = new ViewPager2FragmentAdapter(this);
                pagerAdapter.addFragment(mTietuExpressionFragment, getString(R.string.expression));
                pagerAdapter.addFragment(mTietuPropertyFragment, getString(R.string.prop));
                pagerAdapter.addFragment(mMyTietuFragment, "我的");
                mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    private int currentPosition = 0;
                    private int oldPosition = 0;

                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                        LogUtil.d(TAG, "pos =" + position + " positionOffset:" + positionOffset + " positionOffsetPixels:" + positionOffsetPixels);
                    }

                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
//                        mViewPager.setUserInputEnabled(position != 0);
                    }
                });
                mViewPager.setAdapter(pagerAdapter);
                mViewPager.setOffscreenPageLimit(3);
                //使用viewpager+tabLayout界面切换
                //使用viewpager+tabLayout界面切换
                new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
                    tab.setText(pagerAdapter.getCurTitle(position));
                }).attach();
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        // 云真机测试这里很多报错
        ChooseBaseFragment currentFrag = getCurrentFrag();
        if (currentFrag != null) {
            return currentFrag.onBackPressed();
        }
        return false;
    }

    private ChooseBaseFragment getCurrentFrag() {
        if (mViewPager != null) { // 应该是frg重新导致空指针
            switch (mViewPager.getCurrentItem()) {
                case 0:
                    return mTietuExpressionFragment;
                case 1:
                    return mTietuPropertyFragment;
                case 2:
                    return mMyTietuFragment;
            }
        }
        return mTietuExpressionFragment;
    }

    @Override
    public void startLoad() {

    }

    public int getCurrentItem() {
        if (mViewPager != null) {
            return mViewPager.getCurrentItem();
        }
        return 0;
    }
}
