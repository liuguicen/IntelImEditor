package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.BaseLazyLoadFragment;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;



public class PtuTietuListFragment extends BaseLazyLoadFragment {
    private final static String EXTRA_TITLE = "title";
    private final static String EXTRA_IS_TAG = "tag";
    private String title;
    private boolean isTagGroup;
    private RecyclerView tietuRcv;
    private TietuRecyclerAdapter tietuListAdapter;
    private PTuTietuListViewModel mViewModel;
    private List<PicResource> picResList = new ArrayList<>();

    /**
     * @param title      分组标题
     * @param isTagGroup 是否是标签分组
     * @return
     */
    public static PtuTietuListFragment newInstance(String title, boolean isTagGroup) {
        Bundle args = new Bundle();
        PtuTietuListFragment fragment = new PtuTietuListFragment();
        args.putString(EXTRA_TITLE, title);
        args.putBoolean(EXTRA_IS_TAG, isTagGroup);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.d(TAG, "onHiddenChanged=" + hidden);
    }

    @Override
    public void loadData(boolean isFirstVisible) {
        super.loadData(isFirstVisible);
        if (getArguments() != null && isFirstVisible) {
            title = getArguments().getString(EXTRA_TITLE);
            isTagGroup = getArguments().getBoolean(EXTRA_IS_TAG);
            LogUtil.d(TAG, "title=" + title);
            mViewModel = new ViewModelProvider(this).get(PTuTietuListViewModel.class);
            mViewModel.getPicResources().observe(getViewLifecycleOwner(), picResources -> {
                tietuListAdapter.setItemList(picResources);
                if (picResources.size() > 0) {
                    showLoading(false, null, Color.WHITE);
                } else {
                    String msg;
                    if (PicResource.SECOND_CLASS_MY.equals(title)) {
                        msg = mContext.getString(R.string.no_my_tietu_notice);
                    } else {
                        msg = mContext.getString(R.string.no_network_tietu_notice);
                    }
                    showLoading(false, msg, Color.WHITE);
                }
            });
            mViewModel.loadStatus.observe(getViewLifecycleOwner(), s -> {
                showLoading(false, s, Color.WHITE);
            });
            loadPicListByTitle(title);
        }
    }

    /**
     * 当前布局Id
     */
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_ptu_tietu_list;
    }

    @Override
    public void initView() {
        super.initView();
        tietuRcv = rootView.findViewById(R.id.tietuRcv);
        tietuRcv.setNestedScrollingEnabled(false);
        showLoading(true, null, Color.WHITE);
    }

    @Override
    public void initData() {
        super.initData();
        tietuListAdapter = new TietuRecyclerAdapter(getActivity(), true);
        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(mContext, 4,
                GridLayoutManager.VERTICAL, false);
        tietuRcv.setLayoutManager(gridLayoutManager);
        tietuRcv.setAdapter(tietuListAdapter);
        tietuListAdapter.setOnItemClickListener(tietuRecyclerListener);
    }

    /**
     * 列表图片点击事件
     */
    private RcvItemClickListener1 tietuRecyclerListener = new RcvItemClickListener1() {
        @Override
        public void onItemClick(RecyclerView.ViewHolder itemHolder, View view) {
            int position = itemHolder.getLayoutPosition();
            if (position == -1) return;
            if (tietuListAdapter != null) {
                PicResource oneTietu = tietuListAdapter.get(position).data;
                if (oneTietu != null && oneTietu.getUrl() != null) {
                    String url = oneTietu.getUrl().getUrl();
                    FirstUseUtil.tietuGuide(mContext);
                    MyDatabase.getInstance().updateMyTietu(url, System.currentTimeMillis());
                    //点击图片，设置到图片上
                    if (getActivity() instanceof PtuActivity && ((PtuActivity) getActivity()).tietuFrag != null) {
                        FirstUseUtil.tietuGuide(getContext());
                        ((PtuActivity) getActivity()).tietuFrag.addTietuByMultiType(url, oneTietu.getTag());
                        ((PtuActivity) getActivity()).tietuFrag.curCategory = "-------------";
                        ((PtuActivity) getActivity()).tietuFrag.hideBottomTietuListDialog();
                    }
                } else {
                    Log.e(this.getClass().getSimpleName(), "点击贴图后获取失败");
                }
            }
        }
    };

    private void loadPicListByTitle(String title) {
        if (isTagGroup) {
            LogUtil.d(TAG, "获取贴图成功 = " + title + " - " + picResList.size());
            tietuListAdapter.setList(picResList);
            if (picResList.size() > 0) {
                showLoading(false, null, Color.WHITE);
            } else {
                showLoading(false, "暂无数据", Color.WHITE);
            }
        } else {
            mViewModel.loadOtherGroup(title);
        }
    }


    @Override
    public boolean onBackPressed() {
        return false;
    }

    public void setPicList(List<PicResource> resList) {
        picResList = resList;
    }
}
