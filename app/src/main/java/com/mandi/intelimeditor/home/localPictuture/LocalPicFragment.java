package com.mandi.intelimeditor.home.localPictuture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.FileTool;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.WrapContentGridLayoutManager;
import com.mandi.intelimeditor.common.util.WrapContentLinearLayoutManager;
import com.mandi.intelimeditor.common.view.MyDividerItemDecoration;
import com.mandi.intelimeditor.home.ChooseBaseFragment;
import com.mandi.intelimeditor.home.ChoosePicContract;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.home.tietuChoose.PicResourceItemData;
import com.mandi.intelimeditor.home.view.PopupMenu;

import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.FirstUseUtil;
import com.mandi.intelimeditor.R;
import com.umeng.analytics.MobclickAgent;

import java.util.List;


import cn.bmob.v3.datatype.BmobFile;

/**
 * 本地图片列表
 */
public class LocalPicFragment extends ChooseBaseFragment implements ChoosePicContract.View {

    private RecyclerView pictureGridView;
    private Context mContext;
    private HomeActivity mActivity;
    private DrawerLayout fileListDrawer;
    private LocalPicPresenter mPresenter;
    private LocalPicAdapter picAdapter;
    private GridLayoutManager gridLayoutManager;
    private MyFileListAdapter mFileListAdapter;
    private View mNavHeaderView;
    private RecyclerView picFileListView;
    private NavigationView mNavView;
    private boolean isInitFileInfoViewData = false;
    private boolean isInMultiChoose = false;
    private PopupMenu mPopupMenu;

    public static LocalPicFragment newInstance() {
        Bundle args = new Bundle();
        LocalPicFragment fragment = new LocalPicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getContext();
        mActivity = (HomeActivity) getActivity();
    }

    /**
     * 当前布局Id
     */
    @Override
    public int getLayoutResId() {
        return R.layout.fragment_local_picture;
    }

    @Override
    public void loadData(boolean isFirstVisible) {
        super.loadData(isFirstVisible);
        if (isFirstVisible) {
            mPresenter = new LocalPicPresenter(this, mContext);
            initLocalView();
            startLoad();
        } else {
            if (mPresenter != null) {
                mPresenter.detectAndUpdateInfo();
            }
        }
    }

    @Override
    public void initView() {
        super.initView();
        bindView();
    }

    private void bindView() {
        pictureGridView = rootView.findViewById(R.id.gv_photolist);
        fileListDrawer = rootView.findViewById(R.id.drawer_layout_show_picture);
        mNavView = rootView.findViewById(R.id.pic_directory_nav_view);
        mNavHeaderView = mNavView.getHeaderView(0);
    }

    public void initLocalView() {
        initPicListView();
    }

    /**
     * 打开或者关闭Drawer
     */
    public void updateSideDrawer(boolean close) {
        if (fileListDrawer != null) {
            if (close) {
                fileListDrawer.closeDrawer(GravityCompat.END);
            } else {
                if (fileListDrawer.isDrawerOpen(GravityCompat.END)) {
                    fileListDrawer.closeDrawer(GravityCompat.END);
                } else if (!isInMultiChoose) {
                    fileListDrawer.openDrawer(GravityCompat.END);
                }
            }
        }
    }

    private void initPicListView() {
        if (mPresenter != null) {
            picAdapter = mPresenter.createPicAdapter();
            gridLayoutManager = new WrapContentGridLayoutManager(getContext(), 3);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (picAdapter.getItemViewType(position) == PicResourceItemData.PicListItemType.GROUP_HEADER)
                        return 3;
                    if (picAdapter.getItemViewType(position) == PicResourceItemData.PicListItemType.FEED_AD)
                        return 3;
                    return 1;
                }
            });
            pictureGridView.setLayoutManager(gridLayoutManager);
            picAdapter.setClickListener((itemHolder, view) -> {
                int position = itemHolder.getLayoutPosition();
                if (position == -1) return;
                if (!isInMultiChoose) {
                    LocalGroupedItemData chosenItem = mPresenter.getCurrentPath(position);
                    chooseItem(chosenItem);
                } else {
                    mPresenter.switchChooseItem(position);
                }
            });
            picAdapter.setLongClickListener((itemHolder) -> {
                        int position = itemHolder.getLayoutPosition();
                        if (position == -1) return true;
                        createOrShowPopupMenu();
                        if (isInMultiChoose) {
                            mPresenter.switchChooseItem(position);
                            return true;
                        } else {
                            isInMultiChoose = true;
                            mPresenter.switchChooseItem(position);
                            US.putOtherEvent(US.OTHERS_LONG_CLICK_PIC);
                            return true;
                        }
                    }
            );

        }
    }

    private void createOrShowPopupMenu() {
        if (mPopupMenu == null) {
            PopupMenu.ChoosePopWindowCallback choosePopWindowCallback =
                    new PopupMenu.ChoosePopWindowCallback() {

                        @Override
                        public boolean isInPrefer(String path) {
                            return mPresenter.isInPrefer(path);
                        }

                        @Override
                        public void deletePreferPath(String path) {
                            mPresenter.deletePreferPath(path);
                            mPresenter.cancelChosen();
                        }

                        @Override
                        public void addPreferPath(String path) {
                            mPresenter.addPreferPath(path);
                            mPresenter.cancelChosen();
                        }

                        @Override
                        public boolean isInMyTietu(String path) {
                            return MyDatabase.getInstance().isInMyTietu(path);
                        }

                        @Override
                        public void addMyTietu(String path) {
                            MyDatabase.getInstance().insertMyTietu(path, System.currentTimeMillis());
                            mPresenter.cancelChosen();
                        }

                        @Override
                        public void deleteMyTietu(String path) {
                            MyDatabase.getInstance().deleteMyTietu(path);
                            mPresenter.cancelChosen();
                        }

                        @Override
                        public void requireDeleteChosenPics() {
                            mPresenter.requireDeleteChosenPics();
                        }

                        @Override
                        public void toMakeGif() {
                            mActivity.toMakeGif(mPresenter.getChosenUrlList(true));
                        }
                    };
            mPopupMenu = new PopupMenu(choosePopWindowCallback, mContext, ((ViewGroup) rootView));
        } else {
            mPopupMenu.show();
        }
    }

    private void chooseItem(@Nullable LocalGroupedItemData chosenItem) {
        if (mActivity == null) {
            ToastUtils.show(R.string.no_momery_notice);
            MobclickAgent.reportError(mContext, new NullPointerException(this.getClass().getSimpleName() + ".mInteractListener is null"));
        } else if (chosenItem != null) {
            BmobFile bmobFile = new BmobFile();
            bmobFile.setUrl(chosenItem.url);
            PicResource picResource = new PicResource();
            picResource.setUrl(bmobFile); // 包装一下，方便统一处理
            US.putEditPicEvent(US.EDIT_PIC_FROM_LOCAL);
            mActivity.choosePic(picResource, chosenItem.mediaType == LocalGroupedItemData.MEDIA_SHORT_VIDEO);
        }
    }

    public LocalPicPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        LogUtil.d(TAG, "Menu show = local");
        menu.clear();
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void showPicList() {
        LogUtil.d("开始显示图片列表");
        if (mActivity != null && gridLayoutManager != null && gridLayoutManager.findViewByPosition(2) == null) {
            FirstUseUtil.usuPicUseGuide(mActivity, mActivity.mFloatActionBtn, gridLayoutManager.findViewByPosition(2));
        }
        pictureGridView.setAdapter(picAdapter);
        Debug.stopMethodTracing();
        Log.d("end trace", "time " + System.currentTimeMillis() / 1000);
        showLoading(false);
    }

    /**
     * 删除一张图片
     */
    @Override
    public void ConfirmCancelMultiChoose() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog alertDialog = builder.setTitle(mContext.getString(R.string.cancel_multi_choose_confirm))
                .setPositiveButton("不取消", (dialog, which) -> {
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    if (isInMultiChoose && mPresenter != null) {
                        mPresenter.cancelChosen();
                    }
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void onCancelChosen() {
        if (mPopupMenu != null) {
            mPopupMenu.hide();
        }
        isInMultiChoose = false;
    }

    @Override
    public void onOneChosen(String chosenPath) {
        if (mPopupMenu != null) {
            mPopupMenu.onOneChosen(chosenPath);
        }
    }

    @Override
    public void onMultiChose() {
        if (mPopupMenu != null) {
            mPopupMenu.hidePrefer();
            mPopupMenu.hideTietu();
            if (!mActivity.isOnlyChoosePic()) {
                mPopupMenu.showGifBtn();
            }
        }
    }

    /**
     * 删除一张图片
     */
    @Override
    public void confirmDeletePicList(final List<String> pathList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        AlertDialog alertDialog = builder.setTitle("从您的手机永久删除这些图片吗")
                .setPositiveButton("取消", (dialog, which) -> {
                })
                .setNegativeButton("删除", (dialog, which) -> {
                    // 先从文件中删除，不能删除则不行
                    boolean deleteResult = mPresenter.deletePicList(pathList);
                    if (!deleteResult) {
                        AlertDialog alertDialog1 = new AlertDialog.Builder(mContext)
                                .setTitle("删除失败，此图片无法删除")
                                .setPositiveButton("确定", (dialog1, which1) -> {
                                })
                                .create();
                        alertDialog1.show();
                        return;
                    }
                    mPresenter.cancelChosen();
                })
                .create();
        alertDialog.show();
    }

    @Override
    public void initFileInfoViewData() {
        LogUtil.d(TAG, "初始化文件选择列表 ：initFileInfoViewData");
        if (mContext == null) return; // 调用时这个对象可能为空
        isInitFileInfoViewData = true;
        picFileListView = mNavHeaderView.findViewById(R.id.drawer_picture_file_list);
        mFileListAdapter = mPresenter.createFileAdapter();
        mFileListAdapter.setOnItemClickListener((viewHolder, position) -> {
            LogUtil.d(TAG, "选择文件 " + position);
            if (position == -1) return;
            fileListDrawer.closeDrawer(GravityCompat.END);
            if (mPresenter != null) {
                mPresenter.togglePicData(position);//切换数据，然后会切换视图
            }
        });
        picFileListView.addItemDecoration(new MyDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL, false));
        picFileListView.setLayoutManager(new WrapContentLinearLayoutManager(mContext));
        picFileListView.setAdapter(mFileListAdapter);
    }

    public void backIndexPage() {
        if (mPresenter != null) {
            mPresenter.togglePicData(0);//切换数据，然后会切换视图
        }
    }

    /**
     * 跳转到对应文件夹
     */
    public void togglePicData(String picDirPath) {
        if (mPresenter != null) {
            mPresenter.togglePicData(picDirPath);//切换数据，然后会切换视图
        }
    }

    @Override
    public void onTogglePicList(LocalPicAdapter picAdapter) {
        pictureGridView.setAdapter(picAdapter);
    }


    @Override
    public void refreshPicList() {
        if (picAdapter != null) {
            picAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void refreshFileInfosList() {
        if (!isInitFileInfoViewData) {
            initFileInfoViewData();
        }
        mFileListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(ChoosePicContract.PicPresenter presenter) {

    }

    @Override
    public void startLoad() {
        showLoading(true);
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    public void addUsedPath(String recent_use_pic) {
        if (mPresenter != null)
            mPresenter.addUsedPathFromUI(recent_use_pic);
    }

    public void removeCurrent(String failedPath) {
        if (mPresenter != null)
            mPresenter.removeCurrent(failedPath);
        if (mActivity != null && mActivity.getCurFragment() != null && mActivity.getCurFragment() == this) {
            refreshPicList();
        }
    }

    public void addNewPath(String newPicPath) {
        if (mPresenter != null) {
            mPresenter.addNewPath(newPicPath);
        }
    }

    public boolean isInUsu() {
        if (mPresenter != null)
            return mPresenter.isInUsu();
        return true;
    }

    public void toggleToUsu() {
        if (mPresenter != null) {
            mPresenter.toggleToUsu();
        }
    }

    @Override
    public void choosePicFromSystem() {
        Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intentToPickPic, HomeActivity.REQUEST_CODE_CHOOSE_PIC_FROM_SYSTEM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == HomeActivity.REQUEST_CODE_CHOOSE_PIC_FROM_SYSTEM) {
            if (data == null) return;
            Uri uri = data.getData();
            if (uri == null) return;
            US.putOtherEvent(US.CHOOSE_PIC_FROM_SYSTEM);
            String filePath = FileTool.getImagePathFromUri((Activity) mContext, uri);
            chooseItem(new LocalGroupedItemData(filePath, PicResourceItemData.PicListItemType.ITEM));
        }
    }

    @Override
    public boolean onBackPressed() {
        if (fileListDrawer != null && fileListDrawer.isDrawerOpen(GravityCompat.END)) {
            fileListDrawer.closeDrawer(GravityCompat.END);
            return true;
        }
        if (isInMultiChoose) {
            mPresenter.cancelChosen();
            return true;
        }
        if (!isInUsu()) {
            toggleToUsu();
            return true;
        }
        return false;
    }

    @Override
    public void cancelChosen() {
        if (mPresenter != null) {
            mPresenter.cancelChosen();
        }
    }
}
