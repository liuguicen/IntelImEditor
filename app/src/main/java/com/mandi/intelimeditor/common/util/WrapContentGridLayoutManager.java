package com.mandi.intelimeditor.common.util;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 用于处理RecyclerView的LayoutManager的一个官方不好处理的bug
 * java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder{431a7450 position=1 id=-1, oldPos=-1, pLpos:-1 scrap [attachedScrap] tmpDetached no parent}
 * 此问题主要由于异步，在其它线程更新了adapter的数据导致，可以这样解决
 * 实测是有效的，云真机测试开始出现多个上述问题，使用这个方法后没发现了
 *
 * 网上 还提到一种方法
 * mRecyclerView.getRecycledViewPool().clear();
 * mAdapter.notifyDataSetChanged();
 */
public class WrapContentGridLayoutManager extends GridLayoutManager {
    public WrapContentGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public WrapContentGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    public WrapContentGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}