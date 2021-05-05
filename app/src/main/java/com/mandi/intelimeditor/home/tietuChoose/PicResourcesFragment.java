package com.mandi.intelimeditor.home.tietuChoose;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mandi.intelimeditor.ad.LockUtil;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.WrapContentLinearLayoutManager;
import com.mandi.intelimeditor.home.ChooseBaseFragment;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.home.search.PicResSearchSortUtil;
import com.mandi.intelimeditor.home.view.LongPicPopupWindow;
import com.mandi.intelimeditor.home.view.PicRefreshHeader;
import com.mandi.intelimeditor.home.viewHolder.GroupHolder;
import com.mandi.intelimeditor.home.viewHolder.NewFeatureHeaderHolder;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResGroupItemData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.R;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.mandi.intelimeditor.home.search.PicResSearchSortUtil.SORT_TYPE_GROUP;

/**
 * 目前贴图用这个Fragment显示，通过传入不同的参数控制
 * 使用懒加载，解决初次加载全部导致卡顿问题
 */
public class PicResourcesFragment extends ChooseBaseFragment implements TietuChooseContract.View {

    public static final String FIRST_CLASS = "first_class";
    public static final String SECOND_CLASS = "second_class";

    private String mFirstClass;
    private String mSecondClass;

    private OnFragmentInteractionListener mListener;
    private PicResourcesPresenter presenter;
    private Context mContext;
    private HomeActivity mActivity;
    private RecyclerView picResRcv;
    private View mGroupHeaderView;
    private TextView mIvBackList;
    private TextView mTvGroupTitle;
    private TextView mTvDownloadInfo;
    private FloatingActionButton showFilterMenu;
    private PicRefreshHeader refreshHeader;
    private PicResourcesAdapter picResAdapter;

    private RefreshLayout refreshLayout;
    private boolean isTietu;
    private boolean isShowPicInGroup;


    //当前分组位置
    private int lastGroupPosition = 0;
    private int lastOffset = 0;//偏移量

    /**
     * @param firstClass  贴图或者模板这种大的类别.
     * @param secondClass 二级分类
     */
    public static PicResourcesFragment newInstance(String firstClass, String secondClass, boolean isOnlyChoosePic) {
        PicResourcesFragment fragment = new PicResourcesFragment();
        Bundle args = new Bundle();
        args.putString(FIRST_CLASS, firstClass);
        args.putString(SECOND_CLASS, secondClass);
        args.putBoolean(IS_ONLY_CHOOSE_PIC, isOnlyChoosePic);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFirstClass = getArguments().getString(FIRST_CLASS);
            isTietu = PicResource.FIRST_CLASS_TIETU.equals(mFirstClass);
            mSecondClass = getArguments().getString(SECOND_CLASS);
            mIsOnlyChoosePic = getArguments().getBoolean(IS_ONLY_CHOOSE_PIC);
        }
        setHasOptionsMenu(true);
        mContext = getActivity();
        mActivity = (HomeActivity) getActivity();
    }

    /**
     * 当前布局
     */
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_pic_resource;
    }

    @Override
    public void initView() {
        super.initView();
        LogUtil.d(TAG, "bindView= ");
        mGroupHeaderView = rootView.findViewById(R.id.view_group_header);
        mIvBackList = rootView.findViewById(R.id.tv_back_list);
        mTvGroupTitle = rootView.findViewById(R.id.tv_group_title);
        picResRcv = rootView.findViewById(R.id.rv_tietu_choose);
        mTvDownloadInfo = rootView.findViewById(R.id.tv_pic_load_info);
        showFilterMenu = rootView.findViewById(R.id.showFilterMenu);
        refreshHeader = rootView.findViewById(R.id.refreshHeader);
        //默认最热，初始
        refreshHeader.setNextUpdateText("最新列表");
        showFilterMenu.setOnClickListener(this::showPopMenu);
    }

    private void showPopMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
        popupMenu.inflate(R.menu.main_online_pic);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.action_sort_by_hot_desc:
                    presenter.refreshPicList(PicResSearchSortUtil.SORT_TYPE_HOT, true);
                    US.putSearchSortEvent(US.SORT_HOT);
                    break;
                case R.id.action_sort_by_time_asc:
                    US.putSearchSortEvent(US.SORT_NEWEST);
                    presenter.refreshPicList(PicResSearchSortUtil.SORT_TYPE_TIME, true);
                    break;
                case R.id.action_sort_by_group:
                    US.putSearchSortEvent(US.SORT_GROUP);
                    presenter.showGroup();
                    break;
            }
            return true;
        });
        popupMenu.show();
    }


    @Override
    public void loadData(boolean isFirstVisible) {
        super.loadData(isFirstVisible);
        if (isFirstVisible) {
            LogUtil.d(TAG, " mLoadingView show");
            showLoading(true);
            presenter = new PicResourcesPresenter(mContext, this, mFirstClass, mSecondClass);
            initLocalView();
            presenter.start();
        }
    }

//    @Override
//    public void onFragmentVisibleChange(boolean isVisible, boolean isFirstVisible) {
//        LogUtil.d(TAG, " Menu show = " + mSecondClass + " Visible" + isVisible);
//        if (isVisible && !isFirstVisible && !mPresenter.isDownloadSuccess()) {
//            mLoadingView.setVisibility(View.VISIBLE);
//            mPresenter.start();
//        } else if (isVisible) {
//            mPresenter.refresh();
//        }
//    }

    public void initLocalView() {
        initPicListView();
        mTvDownloadInfo.setVisibility(View.VISIBLE);
        /* 较长时间没有加载出图片时提醒用户，尤其第一次加载时，似乎bmob请求图片列表较慢，等待时间较长，*/
//        new Handler().postDelayed(() -> mTvDownloadInfo.setText(R.string.download_pic_resources_note), 1200);
    }

    /**
     * 跳转到抠脸界面
     */
    private void toChangeFace() {
    }

    private void initPicListView() {
        initPicAdapter();
        //下拉刷新控件
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            refreshlayout.finishRefresh(200/*,false*/);//传入false表示刷新失败
            rootView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter.refreshPicList();
                }
            }, 100);
        });
    }

    private String getSortName(int sortType) {
        switch (sortType) {
            case PicResSearchSortUtil.SORT_TYPE_HOT:
                return getString(R.string.hotest_pic);
            case PicResSearchSortUtil.SORT_TYPE_TIME:
                return getString(R.string.newest_pic);
            case SORT_TYPE_GROUP:
                return getString(R.string.sort_by_group);
        }
        return getString(R.string.hotest_pic);
    }

    private void initPicAdapter() {
        picResAdapter = presenter.createPicAdapter();
        int spanCount = 2;
        if (isTietu) {
            spanCount = 3;
        }
        WrapContentLinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(getContext());
//        GridLayoutManager gridLayoutManager = new WrapContentGridLayoutManager(getContext(), spanCount);
//        int finalSpanCount = spanCount;
//        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//            @Override
//            public int getSpanSize(int position) {
//                if (position <= mPicResourceAdapter.getItemCount()
//                        && (mPicResourceAdapter.getItemViewType(position) == PicResourceItemData.PicListItemType.FEED_AD
//                        || mPicResourceAdapter.getItemViewType(position) == PicResourceItemData.PicListItemType.GROUP_HEADER
//                        || mPicResourceAdapter.getItemViewType(position) == PicResourceItemData.PicListItemType.NEW_FEATURE_HEADER)
//                ) {
//                    return finalSpanCount;
//                }
//                return 1;
//            }
//        });
        picResRcv.setLayoutManager(linearLayoutManager);
        picResRcv.setAdapter(picResAdapter);
        picResAdapter.setClickListener((itemHolder, view) -> {
            int position = itemHolder.getLayoutPosition();
            if (position == -1) return;
            if (itemHolder instanceof NewFeatureHeaderHolder) {
                US.putChangeFaceEvent(US.CHANGE_FACE_FUNCTION_BTN_ENTER);
            } else if (itemHolder instanceof GroupHolder) {
                PicResGroupItemData picResGroup = picResAdapter.getImageUrlList().get(position).picResGroup;
                if (view.getId() == R.id.tv_pic_header_more) {
                    lastGroupPosition = linearLayoutManager.findLastVisibleItemPosition();
                    lastOffset = linearLayoutManager.findViewByPosition(lastGroupPosition).getTop();
                    showPicsInGroup(picResGroup.title, picResGroup.resItemList);
                    return;
                }
                PicResourceItemData itemData = picResAdapter.getImageUrlList().get(position);
                if (view.getId() == R.id.iv_pic_1) {
                    itemData = picResGroup.resItemList.get(0);
                } else if (view.getId() == R.id.iv_pic_2) {
                    itemData = picResGroup.resItemList.get(1);
                } else if (view.getId() == R.id.iv_pic_3) {
                    itemData = picResGroup.resItemList.get(2);
                }
                PicResource chosenResource = itemData.data;
                if (chosenResource == null) return;
                choosePic(chosenResource);
            } else {
                PicResourceItemData itemData = picResAdapter.getImageUrlList().get(position);
                PicResource chosenResource = itemData.data;
                if (chosenResource == null) return;
                choosePic(chosenResource);
            }
        });
        picResAdapter.setLongClickListener((itemHolder) -> {
                    int position = itemHolder.getLayoutPosition();
                    if (position == -1) return true;
                    PicResourceItemData itemData = picResAdapter.getImageUrlList().get(position);
                    if (itemData.data == null) return true;
                    String url = itemData.data.getUrl().getUrl();
                    Observable
                            .create((ObservableOnSubscribe<String>) emitter -> {
                                String picPath = Glide.with(mContext)
                                        .load(url)
                                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                        .get()
                                        .getAbsolutePath();
                                emitter.onNext(picPath);
                                emitter.onComplete();
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SimpleObserver<String>() {
                                @Override
                                public void onNext(String path) {
                                    showPopMenu(itemHolder.itemView, url, path, position);
                                }
                            });
                    return true;
                }
        );

        mIvBackList.setOnClickListener(v -> {
            returnGroupList();
        });

    }

    private void choosePic(@NotNull PicResource chosenResource) {
        chosenResource.updateHeat();
        if (mActivity == null) {
            ToastUtils.show(R.string.no_momery_notice);
            MobclickAgent.reportError(mContext, new NullPointerException(this.getClass().getSimpleName() + ".mInteractListener is null"));
        } else {
            if (!mActivity.isOnlyChoosePic()) {
                if (PicResource.FIRST_CLASS_TEMPLATE.equals(mFirstClass)) {
                    US.putEditPicEvent(US.EDIT_PIC_FROM_TEMPLATE);
                } else if (isTietu) {
                    US.putEditPicEvent(US.EDIT_PIC_FROM_TIETU);
                }
            }
            mActivity.choosePic(chosenResource, null, true);
        }
    }

//    private void toChangeFace(PicResource chosenResource) {
//        chosenResource.updateHeat();
//        mActivity.toChangeFace(chosenResource);
//    }

    /**
     * 显示分组列表
     *
     * @param title        标题
     */
    private void showPicsInGroup(String title, List<PicResourceItemData> picList) {
        isShowPicInGroup = true;
        mGroupHeaderView.setVisibility(View.VISIBLE);
        mTvGroupTitle.setText(title);
        picResAdapter.initAdData(false);
        picResAdapter.setImageUrls(picList, null);
        refreshLayout.setEnableRefresh(false);
        showFilterMenu.setVisibility(View.GONE);
    }


    /**
     * 隐藏分组列表
     */
    private void returnGroupList() {
        isShowPicInGroup = false;
        picResAdapter.initAdData(true);
        mGroupHeaderView.setVisibility(View.GONE);
        picResAdapter.setImageUrls(AllData.getGroupList(mFirstClass, mSecondClass), null);
        //隐藏分组列表后返回到前一个状态
        ((LinearLayoutManager) picResRcv.getLayoutManager()).scrollToPositionWithOffset(lastGroupPosition, lastOffset);
        refreshLayout.setEnableRefresh(true);
        showFilterMenu.setVisibility(View.VISIBLE);
    }

    /**
     * @param url  注意图片资源列表下面的路径是url
     * @param path 添加常用时用本地路径形式
     */
    private void showPopMenu(View anchorView, String url, String path, int position) {
        LongPicPopupWindow.ChoosePopWindowCallback choosePopWindowCallback =
                new LongPicPopupWindow.ChoosePopWindowCallback() {

                    @Override
                    public boolean isInPrefer() {
                        return MyDatabase.getInstance().isInPrefer(path);
                    }

                    @Override
                    public void deleteFromPrefer() {
                        mActivity.deletePreferPath(path);
                    }

                    @Override
                    public void addToPrefer() {
                        Runnable taskAfterUnlocked = () -> {
                            mActivity.addPreferPath(path);
                            picResAdapter.getImageUrlList().get(position).isUnlock = true;
                            picResAdapter.notifyItemChanged(position);
                        };
                        if (LockUtil.checkLock(getActivity(), url, isTietu, taskAfterUnlocked, true)) {
                        } else {
                            mActivity.addPreferPath(path);
                        }
                    }

                    @Override
                    public boolean isInMyTietu() {
                        return MyDatabase.getInstance().isInMyTietu(url);
                    }

                    @Override
                    public void addToMyTietu() {
                        MyDatabase.getInstance().insertMyTietu(url, System.currentTimeMillis());
                    }

                    @Override
                    public void deleteFromMyTietu() {
                        presenter.deleteOneMyTietu(url);
                    }

                    @Override
                    public void requireDelete() {

                    }
                };
        LongPicPopupWindow.setPicPopWindow(choosePopWindowCallback, mContext,
                anchorView, false, true);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // rootView.getParent() != null, 制作贴图的时候，滑走调用一次这里，退出ac又调用一次，getParent就空了
        if (null != rootView && rootView.getParent() != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void setPresenter(TietuChooseContract.Presenter presenter) {
    }

    @Override
    public void startLoad() {

    }

    @Override
    public void onDownloadStateChange(boolean isSuccess, @Nullable List<PicResource> list) {
        showLoading(false);
        if (!isSuccess && getActivity() != null) {
            mTvDownloadInfo.setVisibility(View.VISIBLE);
            mTvDownloadInfo.setText(R.string.pic_download_failed_info);
        } else if (list != null && list.size() == 0 && getActivity() != null && PicResource.SECOND_CLASS_MY.equals(mSecondClass)) {
            mTvDownloadInfo.setText("暂未保存表情包,快去添加吧！");
            mTvDownloadInfo.setVisibility(View.VISIBLE);
        } else {
            mTvDownloadInfo.setText("");
            mTvDownloadInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterSort(int nextSortType) {
        picResRcv.scrollToPosition(0);
        refreshHeader.setNextUpdateText(getSortName(nextSortType));
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    @Override
    public boolean onBackPressed() {
        if (isShowPicInGroup) {
            returnGroupList();
            return true;
        }
        return false;
    }

}
