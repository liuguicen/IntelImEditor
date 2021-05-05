package com.mandi.intelimeditor.ptu.tietu.onlineTietu;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mandi.intelimeditor.common.BaseLazyLoadFragment;
import com.mandi.intelimeditor.common.RcvItemClickListener1;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.dialog.BottomTietuListDialog;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getArguments().getString(EXTRA_TITLE);
        isTagGroup = getArguments().getBoolean(EXTRA_IS_TAG);
    }

    /**
     * @param isFirstVisible
     */
    @Override
    public void loadData(boolean isFirstVisible) {
        super.loadData(isFirstVisible);
        if (getArguments() != null && isFirstVisible) {
            LogUtil.d(TAG, "title=" + title);
            mViewModel = new ViewModelProvider(this).get(PTuTietuListViewModel.class);
            /**
             * {@link PTuTietuListViewModel#loadGroupList()}
             */
            mViewModel.getPicResources().observe(getViewLifecycleOwner(), new Observer<List<PicResourceItemData>>() {
                @Override
                public void onChanged(List<PicResourceItemData> picResources) {
                    tietuListAdapter.setItemList(picResources);

                    LogUtil.logTimeConsume(title + " 完成数据加载，放入了Adapter中");
                    if (picResources.size() > 0) {
                        showLoading(true, null, Color.WHITE);
                    } else {
                        onEmptyResult();
                    }
                }
            });
            mViewModel.loadStatus.observe(
                    getViewLifecycleOwner(), s ->

                    {
                        showLoading(true, s, Color.WHITE);
                    });

            loadPicListByTitle(title);
        }
    }

    private void onEmptyResult() {
        String msg;
        if (BottomTietuListDialog.TITLE_MY.equals(title)) {
            msg = mContext.getString(R.string.no_my_tietu_notice);
        } else if (BottomTietuListDialog.TITLE_SEARCH.equals(title)) {
            msg = mContext.getString(R.string.no_search_result);
        } else {
            msg = mContext.getString(R.string.no_network_tietu_notice);
        }
        showLoading(true, msg, Color.WHITE);
    }

    /**
     * 当前布局Id
     */
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_tietu_list;
    }

    @Override
    public void initView() {
        super.initView();
        tietuRcv = rootView.findViewById(R.id.tietuRcv);
        tietuRcv.setNestedScrollingEnabled(false);
        if (!BottomTietuListDialog.TITLE_SEARCH.equals(title)) {
            showLoading(false, null, Color.WHITE);
        }
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
            if (LogUtil.debugPtuTietuList)
                LogUtil.d(TAG, "获取贴图成功 = " + title + " - " + picResList.size());
            tietuListAdapter.setList(picResList);
            if (LogUtil.debugPtuTietuList)
                LogUtil.logTimeConsume(title + " 完成数据加载，放入了Adapter中");
            if (picResList.size() > 0) {
                showLoading(true, null, Color.WHITE);
            } else {
                showLoading(true, "暂无数据", Color.WHITE);
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

    public void refresh(List<PicResource> resList) {
        if (resList == null || resList.isEmpty()) onEmptyResult();
        picResList = resList;
        tietuListAdapter.setItemList(PicResourceItemData.picResList2PicResItemList(picResList));
        tietuListAdapter.notifyDataSetChanged();
    }
}
