package com.mandi.intelimeditor.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.ptu.tietu.TietuFragment;

import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResourceViewModel;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PtuTietuListFragment;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ViewPager2FragmentAdapter;
import com.mathandintell.intelimedit.bean.GroupBean;
import com.mathandintell.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;

public class BottomTietuListDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private BottomSheetBehavior mBehavior;
    private View rootView;
    private TabLayout mTabs;
    private ViewPager2 mViewPager;
    private FrameLayout mSearchView;
    private View mSearchImageIv;
    private TabLayoutMediator tabLayoutMediator;
    private String TAG = getClass().getSimpleName();
    private PicResourceViewModel mViewModel;
    private ViewPager2FragmentAdapter pagerAdapter;
    private int selectIndex = 0;
    private boolean isMyTietu = false;
    private List<GroupBean> mData = new ArrayList<>();

    public void setSelectIndex(int selectIndex) {
        if (selectIndex == 0) {//我的
            isMyTietu = true;
        } else {
            isMyTietu = false;
            this.selectIndex = selectIndex;
        }
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback
            = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            //禁止拖拽，
//            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
//                //设置为收缩状态
//                mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.BottomDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        //获取dialog对象
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        WindowManager.LayoutParams layoutParams1 = dialog.getWindow().getAttributes();
        layoutParams1.dimAmount = 0.0f;
        dialog.getWindow().setAttributes(layoutParams1);
        //获取diglog的根部局
        FrameLayout bottomSheet = dialog.getDelegate().findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            //获取根部局的LayoutParams对象
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
            layoutParams.height = getPeekHeight();
            //修改弹窗的最大高度，不允许上滑（默认可以上滑）
            bottomSheet.setLayoutParams(layoutParams);

            final BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            //peekHeight即弹窗的最大高度
            behavior.setPeekHeight(getPeekHeight());
            // 初始为展开状态
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            ImageView mReBack = rootView.findViewById(R.id.closeDialogTv);
//            //设置监听
//            mReBack.setOnClickListener(view -> {
//                //关闭弹窗
//                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            });
            bottomSheet.setBackgroundColor(0xaa000000);
        }
    }


    /**
     * 弹窗高度，默认为屏幕高度的四分之三
     * 子类可重写该方法返回peekHeight
     *
     * @return height
     */
    protected int getPeekHeight() {
        int peekHeight = AllData.getScreenHeight();
        return peekHeight - peekHeight / 3;
    }


    public static BottomTietuListDialog newInstance(int selectIndex) {
        Bundle args = new Bundle();
        BottomTietuListDialog fragment = new BottomTietuListDialog();
        args.putInt("selectIndex", selectIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tietu_list_bottom_sheet_fragment_dialog, container);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBehavior = BottomSheetBehavior.from((View) rootView.getParent());
        mBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        //点击贴图时，因为分组列表多，添加到viewpager上时候会有延时，所以要延时加载，否则点击贴图时，会先卡顿一下，才会弹出对话框
        if (mViewModel == null && getActivity() != null && mData.size() == 0) {
            mViewPager.postDelayed(() -> {
                mViewModel = new ViewModelProvider(getActivity()).get(PicResourceViewModel.class);
                mViewModel.getTagList().observe(getViewLifecycleOwner(), this::updateTabList);
            }, 100);
        } else {
            updateTabList(mData);
        }
    }

    private void updateTabList(List<GroupBean> tagList) {
        try {
            LogUtil.d(TAG, "updateTabList " + tagList.size());
            if (getActivity() != null) {
                mData = tagList;
                LogUtil.d(TAG, "updateTabList pagerAdapter == null");
                if (pagerAdapter == null) {
                    pagerAdapter = new ViewPager2FragmentAdapter(this);
                }
                pagerAdapter.clearAll();
                //默认分组
                pagerAdapter.addFragment(PtuTietuListFragment.newInstance(PicResource.SECOND_CLASS_MY, false), "我的");
                pagerAdapter.addFragment(PtuTietuListFragment.newInstance(PicResource.PIC_STICKER_HOT_LIST, false), "最热");
                pagerAdapter.addFragment(PtuTietuListFragment.newInstance(PicResource.PIC_STICKER_LATEST_LIST, false), "最新");
                for (int i = 0; i < tagList.size(); i++) {
                    pagerAdapter.addFragment(PtuTietuListFragment.newInstance(tagList.get(i).getTitle(), true), tagList.get(i).getTitle());
                }
                mViewPager.setAdapter(pagerAdapter);
                if (isMyTietu) {
                    mViewPager.setCurrentItem(0, false);
                } else {
                    mViewPager.setCurrentItem(selectIndex, false);
                }
                mViewPager.setOffscreenPageLimit(pagerAdapter.getItemCount());
                //使用viewpager+tabLayout界面切换
                LogUtil.d(TAG, "updateTabList tabLayoutMediator ==null");
                if (tabLayoutMediator == null) {
                    tabLayoutMediator = new TabLayoutMediator(mTabs, mViewPager, true, (tab, position) -> {
                        //这里需要根据position修改tab的样式和文字等
                        tab.setText(pagerAdapter.getCurTitle(position));
                        mTabs.setScrollPosition(position, 0, true, true);
                    });
                    tabLayoutMediator.attach();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.d(TAG, "updateTabList pagerAdapter finish");
    }

    private void initViews(View view) {
        mTabs = view.findViewById(R.id.tietuListTabs);
        mViewPager = view.findViewById(R.id.tietuViewPager);
        mViewPager.setSaveEnabled(false);
        mSearchView = view.findViewById(R.id.searchView);
        mSearchImageIv = view.findViewById(R.id.searchImageIv);
        view.findViewById(R.id.localPicIv).setOnClickListener(this);
        view.findViewById(R.id.clearSearchIv).setOnClickListener(this);
        ((EditText) view.findViewById(R.id.searchEditText)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchString = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSearchImageIv.setOnClickListener(this);
        mViewPager.registerOnPageChangeCallback(pageChangeCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        mViewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        pagerAdapter.clearAll();
        pagerAdapter = null;
        tabLayoutMediator = null;

    }

    private ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            LogUtil.d(TAG, "选中了 " + position);
            if (!isMyTietu) {
                selectIndex = position;
            }
            isMyTietu = false;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clearSearchIv:
                mTabs.setVisibility(View.VISIBLE);
                mSearchView.setVisibility(View.GONE);
                mSearchImageIv.setVisibility(View.VISIBLE);
                break;
            case R.id.searchImageIv:
                mTabs.setVisibility(View.GONE);
                mSearchView.setVisibility(View.VISIBLE);
                mSearchImageIv.setVisibility(View.GONE);
                break;
            case R.id.localPicIv:
                if (getParentFragment() != null && getParentFragment() instanceof TietuFragment) {
                    ((TietuFragment) getParentFragment()).onClickMore();
                }
                dismiss();
                break;
        }
    }
}