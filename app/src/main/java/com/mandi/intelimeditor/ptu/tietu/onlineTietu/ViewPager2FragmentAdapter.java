package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/01/28
 *      version : 1.0
 * <pre>
 */
public class ViewPager2FragmentAdapter extends FragmentStateAdapter {
    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<String> mTitleList = new ArrayList<>();

    public ViewPager2FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public ViewPager2FragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mTitleList.add(title);
    }

    public void clearAll() {
        mFragmentList.clear();
        mTitleList.clear();
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragmentList.get(position);
    }

    public String getCurTitle(int position) {
        return mTitleList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }
}
