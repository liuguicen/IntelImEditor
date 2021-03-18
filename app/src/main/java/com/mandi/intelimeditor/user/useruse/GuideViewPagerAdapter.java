package com.mandi.intelimeditor.user.useruse;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class GuideViewPagerAdapter extends PagerAdapter {

    private final Context context;
    private final ImageView[] imageViews;

    GuideViewPagerAdapter(Context context, ImageView[] imageViews){
        this.context = context;
        this.imageViews = imageViews;
    }

    @Override
    public int getCount() {
        return imageViews.length;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ImageView view = imageViews[position];
        container.removeView(view);
    }

    @Override
    public Object instantiateItem(View container, int position) {
        try {
            if (imageViews[position].getParent() == null) {
                ((ViewPager) container).addView(imageViews[position], 0);
            } else {
                ((ViewGroup) (imageViews[position].getParent()))
                        .removeView(imageViews[position]);
            }
        } catch (Exception e) {
        }
        return imageViews[position % imageViews.length];
    }
}