package com.mandi.intelimeditor.home.localPictuture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewPager2界面适配器
 */
public class ViewPagerFragmentAdapter extends FragmentStateAdapter {
    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<String> mTitleList = new ArrayList<>();

    public ViewPagerFragmentAdapter(AppCompatActivity activity) {
        super(activity);
    }
    public ViewPagerFragmentAdapter(Fragment fragment) {
        super(fragment);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mTitleList.add(title);
    }

    public List<String> getTitleList() {
        return mTitleList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }
}
