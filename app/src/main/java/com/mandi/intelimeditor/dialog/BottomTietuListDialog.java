package com.mandi.intelimeditor.dialog;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.ptu.tietu.TietuFragment;

import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PTuTietuListViewModel;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroup;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PtuTietuListFragment;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ViewPager2FragmentAdapter;
import com.mandi.intelimeditor.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Emitter;

public class BottomTietuListDialog extends BottomSheetDialogFragment implements View.OnClickListener {

    private BottomSheetBehavior mBehavior;
    private View rootView;
    private TabLayout mTabs;
    private ViewPager2 viewPager;
    private FrameLayout mSearchBarLayout;
    private View mSearchBtn;
    private TabLayoutMediator tabLayoutMediator;
    private String TAG = getClass().getSimpleName();
    private PTuTietuListViewModel mViewModel;
    private ViewPager2FragmentAdapter pagerAdapter;
    private int selectIndex = 0;
    private boolean isMyTietu = false;
    private List<PicResGroup> groupLlist = new ArrayList<>();
    public static final String TITLE_MY = "??????";
    public static final String TITLE_HOTEST = "??????";
    public static final String TITLE_NEWEST = "??????";
    public static final String TITLE_SEARCH = "??????";
    private PtuTietuListFragment searchResultFrag;
    private View serchResultView;
    private FragmentManager fm;
    private EditText searchContentTv;

    public void setSelectIndex(int selectIndex) {
        if (selectIndex == 0) {//??????
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
            //???????????????
//            if (newState == BottomSheetBehavior.STATE_DRAGGING) {
//                //?????????????????????
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
        if (LogUtil.debugPtuTietuList)
            LogUtil.logTimeConsume("????????????P?????????????????????");
        //??????dialog??????
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        WindowManager.LayoutParams layoutParams1 = dialog.getWindow().getAttributes();
        layoutParams1.dimAmount = 0.0f;
        dialog.getWindow().setAttributes(layoutParams1);
        //??????diglog????????????
        FrameLayout bottomSheet = dialog.getDelegate().findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            //??????????????????LayoutParams??????
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
            layoutParams.height = getPeekHeight();
            //?????????????????????????????????????????????????????????????????????
            bottomSheet.setLayoutParams(layoutParams);

            final BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
            //peekHeight????????????????????????
            behavior.setPeekHeight(getPeekHeight());
            // ?????????????????????
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            ImageView mReBack = rootView.findViewById(R.id.closeDialogTv);
//            //????????????
//            mReBack.setOnClickListener(view -> {
//                //????????????
//                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            });
            bottomSheet.setBackgroundColor(0xaa000000);
        }
        if (LogUtil.debugPtuTietuList)
            LogUtil.logTimeConsume("??????????????????");
    }


    /**
     * ???????????????????????????????????????????????????
     * ??????????????????????????????peekHeight
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


    private void initViews(View view) {
        mTabs = view.findViewById(R.id.tietuListTabs);
        viewPager = view.findViewById(R.id.tietuViewPager);
        viewPager.setSaveEnabled(false);
        mSearchBarLayout = view.findViewById(R.id.searchBarLayout);
        mSearchBtn = view.findViewById(R.id.searchBtn);
        view.findViewById(R.id.localPicIv).setOnClickListener(this);
        view.findViewById(R.id.clearSearchIv).setOnClickListener(this);
        searchContentTv = view.findViewById(R.id.tietu_search_content);
        serchResultView = view.findViewById(R.id.fragment_layout_search_result);
        searchContentTv.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // ????????????????????????
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    //????????????????????????
                    search(v.getText().toString());
                    return true;
                }
                return false;
            }
        });

        mSearchBtn.setOnClickListener(this);
        viewPager.registerOnPageChangeCallback(pageChangeCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        //???????????????????????????????????????????????????viewpager??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        LogUtil.logTimeConsume("????????????resume");
        if (mViewModel == null && getActivity() != null) {
            mViewModel = new ViewModelProvider(getActivity()).get(PTuTietuListViewModel.class);
        }
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        // ????????????????????????????????????
        if (groupLlist.size() == 0) {
            mViewModel.getOrLoadGroupList().observe(getViewLifecycleOwner(), this::updateTabList);
            mViewModel.AllGroupLoadStatus.observe(getViewLifecycleOwner(), s ->
            {
//                baseLoadingView.showLoading(true, s, Color.WHITE);
                // ????????????????????????????????????????????????
                updateTabList(groupLlist);
            });
        } else {
            updateTabList(groupLlist);
        }
    }

    private void updateTabList(List<PicResGroup> groupList) {
        try {
            if (LogUtil.debugPtuTietuList)
                LogUtil.logTimeConsume("????????????ViewPage???Fragment");
            if (getActivity() != null) {
                groupLlist = groupList;
                if (LogUtil.debugPtuTietuList)
                    LogUtil.d(TAG, "updateTabList pagerAdapter == null");
                if (pagerAdapter == null) {
                    pagerAdapter = new ViewPager2FragmentAdapter(this);
                }
                pagerAdapter.clearAll();
                //????????????

                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("??????????????????Fragment");
                pagerAdapter.addFragment(PtuTietuListFragment.newInstance(TITLE_MY, false), TITLE_MY);
                pagerAdapter.addFragment(PtuTietuListFragment.newInstance(TITLE_HOTEST, false), TITLE_HOTEST);
                pagerAdapter.addFragment(PtuTietuListFragment.newInstance(TITLE_NEWEST, false), TITLE_NEWEST);
                for (int i = 0; i < groupList.size(); i++) {
                    PicResGroup picResGroup = groupList.get(i);
                    PtuTietuListFragment fragment = PtuTietuListFragment.newInstance(picResGroup.title, true);
                    fragment.setPicList(picResGroup.resList);
                    pagerAdapter.addFragment(fragment, picResGroup.title);
                }
                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("????????????Fragment??????");
                viewPager.setAdapter(pagerAdapter);
                if (isMyTietu) {
                    viewPager.setCurrentItem(0, false);
                } else {
                    viewPager.setCurrentItem(selectIndex, false);
                }
                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("ViewPage??????adapter??????????????????");
                viewPager.setOffscreenPageLimit(pagerAdapter.getItemCount());
                //??????viewpager+tabLayout????????????
                LogUtil.d(TAG, "updateTabList tabLayoutMediator ==null");
                if (tabLayoutMediator == null) {
                    tabLayoutMediator = new TabLayoutMediator(mTabs, viewPager, true, (tab, position) -> {
                        //??????????????????position??????tab?????????????????????
                        tab.setText(pagerAdapter.getCurTitle(position));
                        mTabs.setScrollPosition(position, 0, true, true);
                    });
                    tabLayoutMediator.attach();
                }
                if (LogUtil.debugPtuTietuList)
                    LogUtil.logTimeConsume("ViewPage??????????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogUtil.d(TAG, "updateTabList pagerAdapter finish");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy");
        if (viewPager != null) viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        if (pagerAdapter != null) pagerAdapter.clearAll();
        pagerAdapter = null;
        tabLayoutMediator = null;

    }

    private ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            LogUtil.d(TAG, "????????? " + position);
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
                cancelSearch();
                break;
            case R.id.searchBtn:
                if (mSearchBarLayout.getVisibility() == View.VISIBLE) {
                    search(searchContentTv.getText().toString());
                } else {
                    prepareSearch();
                }
                break;
            case R.id.localPicIv:
                if (getParentFragment() != null && getParentFragment() instanceof TietuFragment) {
                    ((TietuFragment) getParentFragment()).onClickMore();
                }
                dismiss();
                break;
        }
    }

    private void cancelSearch() {
        mTabs.setVisibility(View.VISIBLE);
        mSearchBarLayout.setVisibility(View.GONE);
        serchResultView.setVisibility(View.GONE);
        rootView.findViewById(R.id.fragment_layout_search_result).setVisibility(View.GONE);
        viewPager.setVisibility(View.VISIBLE);
        fm.beginTransaction().remove(searchResultFrag).commitAllowingStateLoss();
        Util.hideInputMethod(getActivity(), searchContentTv);
        searchContentTv.setText("");
    }

    private void prepareSearch() {
        mTabs.setVisibility(View.GONE);
        mSearchBarLayout.setVisibility(View.VISIBLE);
        serchResultView.setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.fragment_layout_search_result).setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
        fm = getChildFragmentManager();
        searchResultFrag = PtuTietuListFragment.newInstance(TITLE_SEARCH, false);
        fm.beginTransaction()
                .replace(R.id.fragment_layout_search_result, searchResultFrag)
                .commitAllowingStateLoss();
        searchContentTv.requestFocus();
        Util.showInputMethod(searchContentTv);
    }


    private void search(String searchString) {
        PicResSearchSortUtil.searchPicResByQueryString(searchString, PicResource.FIRST_CLASS_TIETU, new Emitter<List<PicResource>>() {
            @Override
            public void onNext(@io.reactivex.annotations.NonNull List<PicResource> value) {
                if (isDetached()) return;
                searchResultFrag.refresh(value);
                FragmentActivity activity = getActivity();
                if (activity != null) // ??????????????????
                    Util.hideInputMethod(activity, searchContentTv);

            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable error) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

}