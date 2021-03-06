package com.mandi.intelimeditor.home.tietuChoose;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

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
 * ?????????????????????Fragment??????????????????????????????????????????
 * ????????????????????????????????????????????????????????????
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


    //??????????????????
    private int lastGroupPosition = 0;
    private int lastOffset = 0;//?????????

    /**
     * @param firstClass  ????????????????????????????????????.
     * @param secondClass ????????????
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
     * ????????????
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
        //?????????????????????
        refreshHeader.setNextUpdateText("????????????");
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
    public void loadData(boolean isFirstLoad) {
        super.loadData(isFirstLoad);
        if (isFirstLoad) {
            LogUtil.d(TAG, " mLoadingView show");
            showLoading();
            presenter = new PicResourcesPresenter(mContext, this, mFirstClass, mSecondClass);
            initLocalView();
            presenter.start();
        }
    }

    public void initLocalView() {
        initPicListView();
        mTvDownloadInfo.setVisibility(View.VISIBLE);
        /* ????????????????????????????????????????????????????????????????????????????????????bmob????????????????????????????????????????????????*/
//        new Handler().postDelayed(() -> mTvDownloadInfo.setText(R.string.download_pic_resources_note), 1200);
    }

    /**
     * ?????????????????????
     */
    private void toChangeFace() {
    }

    private void initPicListView() {
        initPicAdapter();
        //??????????????????
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        refreshLayout.setEnableAutoLoadMore(false);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(refreshlayout -> {
            refreshlayout.finishRefresh(200/*,false*/);//??????false??????????????????
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
        StaggeredGridLayoutManager linearLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
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
                    int[] lastPositions = new int[((StaggeredGridLayoutManager) linearLayoutManager).getSpanCount()];
                    lastGroupPosition = linearLayoutManager.findLastVisibleItemPositions(lastPositions)[0];
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
                choosePic(chosenResource, picResGroup.resList);
            } else {
                PicResourceItemData itemData = picResAdapter.getImageUrlList().get(position);
                PicResource chosenResource = itemData.data;
                if (chosenResource == null) return;
                choosePic(chosenResource, presenter.getOriginList());
            }
        });
        picResAdapter.setLongClickListener((itemHolder) -> {
                    int position = itemHolder.getLayoutPosition();
                    if (position == -1) return true;
                    PicResourceItemData itemData = picResAdapter.getImageUrlList().get(position);
                    if (itemData.data == null || itemData.data.getUrl() == null) return true;
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

    private void choosePic(@NotNull PicResource chosenResource, List<PicResource> resList) {
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
            mActivity.choosePic(chosenResource, resList, true);
        }
    }

    /**
     * ??????????????????
     *
     * @param title ??????
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
     * ??????????????????
     */
    private void returnGroupList() {
        isShowPicInGroup = false;
        picResAdapter.initAdData(true);
        mGroupHeaderView.setVisibility(View.GONE);
        picResAdapter.setImageUrls(AllData.getGroupList(mFirstClass, mSecondClass), null);
        //?????????????????????????????????????????????
        ((LinearLayoutManager) picResRcv.getLayoutManager()).scrollToPositionWithOffset(lastGroupPosition, lastOffset);
        refreshLayout.setEnableRefresh(true);
        showFilterMenu.setVisibility(View.VISIBLE);
    }

    /**
     * @param url  ??????????????????????????????????????????url
     * @param path ????????????????????????????????????
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
                        AllData.getThreadPool_single().execute(() ->
                                MyDatabase.getInstance().insertMyTietu(url, System.currentTimeMillis()));
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
        // rootView.getParent() != null, ?????????????????????????????????????????????????????????ac??????????????????getParent?????????
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
        dismissLoading();
        if (!isSuccess && getActivity() != null) {
            mTvDownloadInfo.setVisibility(View.VISIBLE);
            if (PicResource.SECOND_CLASS_MY.equals(mSecondClass)) {
                mTvDownloadInfo.setText("?????????????????????,?????????????????????");
            } else
                mTvDownloadInfo.setText(R.string.pic_download_failed_info);
        } else if (list != null && list.size() == 0 && getActivity() != null && PicResource.SECOND_CLASS_MY.equals(mSecondClass)) {
            mTvDownloadInfo.setText("?????????????????????,??????????????????");
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
