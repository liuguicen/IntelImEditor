package com.mandi.intelimeditor.home;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mandi.intelimeditor.R;
import com.mandi.intelimeditor.ad.ttAD.videoAd.FullScreenVadManager;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVadManager;
import com.mandi.intelimeditor.bean.PicInfoEvent;
import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.CommonConstant;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.appInfo.AppIntentService;
import com.mandi.intelimeditor.common.appInfo.TheUser;
import com.mandi.intelimeditor.common.appInfo.TheUserUtil;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.FileTool;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.SimpleObserver;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.dialog.LoadingDialog;
import com.mandi.intelimeditor.dialog.LoginDialog;
import com.mandi.intelimeditor.home.data.MediaInfoScanner;
import com.mandi.intelimeditor.home.localPictuture.LocalPicFragment;
import com.mandi.intelimeditor.home.search.SearchActivity;
import com.mandi.intelimeditor.home.tietuChoose.PicResourcesFragment;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultData;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ViewPager2FragmentAdapter;
import com.mandi.intelimeditor.ptu.transfer.StyleTransferFragment;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;
import com.mandi.intelimeditor.user.userSetting.AboutAppActivity;
import com.mandi.intelimeditor.user.userSetting.FeedBackActivity;
import com.mandi.intelimeditor.user.userSetting.SettingActivity;
import com.mandi.intelimeditor.user.userVip.OpenVipActivity;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.user.useruse.tutorial.HelpActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.bmob.v3.datatype.BmobFile;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.mandi.intelimeditor.common.util.CoverLoader;

/**
 * ???????????????????????????????????????????????????????????????
 * ????????????????????????????????????????????????????????????
 */
public class HomeActivity extends BaseActivity implements HomeContract.View, View.OnClickListener {

    private String TAG = "HomeActivity";

    // ?????????????????????????????????
    public static final String PTU_ACTION_CHOOSE_TIETU = "choose_tietu";

    public static final int REQUEST_CODE_NORMAL = 0;
    public static final int REQUEST_CODE_CHOOSE_PIC_FROM_SYSTEM = 12;
    private static final int REQUEST_CODE_MAKE_GIF = 13;
    private static final int REQUEST_CODE_LOGIN = 14;
    public static final int REQUEST_CODE_SEARCH = 15;
    private static final int REQUEST_CODE_DIG_FACE = 20;
    private static final int REQUEST_CODE_ERASE_EXPRESSION = 21;
    public static final String INTENT_ACTION_ONLY_CHOSE_PIC = "INTENT_ACTION_ONLY_CHOSE_PIC";
    public static final String INTENT_EXTRA_CHOSEN_PIC_RES = "INTENT_EXTRA_CHOSEN_PIC_RES";

    /**
     * ??????????????????????????????ID
     */
    public static final String INTENT_EXTRA_FRAGMENT_ID = "FRAGMENT_ID";

    public static final int TEMPLATE_FRAG_ID = 1;
    public static final int LOCAL_FRAG_ID = 0;

    boolean mIsFromCreate = false;
    static String intentAction = "";
    private boolean isChooseTietu = false;

    private ChooseBaseFragment currentFrag;

    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private ViewPager2FragmentAdapter mViewPagerFragmentAdapter;
    private PicResourcesFragment mTemplateFragment;
    private LocalPicFragment mLocalPicFragment;
    private DrawerLayout mDrawerLayout;
    private TextView mVipStatusTv;
    private ImageView mMenuIv, mFilterIv, mSearchIv;

    private int curPicResourceClassID = 0;
    private ViewGroup activityLayout;
    private LoadingDialog progressDialog;

    private HomePresenter mHomePresenter;

    public FloatingActionButton mFloatActionBtn;
    private View mHeaderView;
    private View mUserInfoView;
    private View mSignInView;
    private LoginDialog loginDialog;

    /**
     * Called when the activity is first created.
     * ????????????????????????????????????????????????????????????????????????????????????????????????GridView???????????????????????????????????????ui??????????????????
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAppDataNeedAc();
        Log.e(TAG, "onCreate");
        initIntentData();
        EventBus.getDefault().register(this);
        initData();
        initView();
        initFragment();
        mIsFromCreate = true;
        LogUtil.printMemoryInfo(TAG + "AC ??????", this);
        // ????????????????????????
        // test();
    }

    /**
     * ??????onRestart???????????????ac???????????????launchAc????????????launchAc???????????????????????????
     * ??????????????????VIP???????????????????????????????????????launchAc?????????Ac???
     */
    @Override
    protected void onStart() {
        VipUtil.judeShowToOpenVip_forAdClick(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHomePresenter.start();
        // Android 8.0 ???????????????????????????APP??????????????????????????????????????????????????????
        // ??????????????????????????????????????????onCreate??????????????????????????????
        if (!AllData.hasInitBackgroundService ) {
           AllData.hasInitBackgroundService = true;
           startBackgroundService(this);
        }
    }

    /**
     * ???????????????
     */
    private void initIntentData() {
        Intent sourceIntent = getIntent();
        if (sourceIntent != null) {
            if (sourceIntent.getData() != null) { // ???????????????????????????????????????????????????
                US.putOtherEvent(US.OTHERS_EDIT_FROM_THIRD_APP);
                PtuUtil.PTuIntentBuilder.fromOtherApp(this, sourceIntent).startActivity();
                finish();
                return;
            }
            intentAction = sourceIntent.getAction();
            if (PTU_ACTION_CHOOSE_TIETU.equals(intentAction)) {
                isChooseTietu = true;
            }
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_home;
    }

    void initAppDataNeedAc() {
        if (!AllData.hasInitDataNeedAc) {
            AllData.hasInitDataNeedAc = true;
            getScreenSize();
//            sendNotify();
            AllData.globalSettings.readDeviceInfo();// ??????????????????????????????
            TTRewardVadManager.getInstance().initAd(this); // ???????????????
        }
    }


    /**
     * ?????????????????????
     *
     * @see AppIntentService
     * <p>1.?????????????????????????????????
     */
    private void startBackgroundService(Activity initAc) {
        Intent intent = new Intent("start");
        intent.setAction("com.mandi.intelimeditor.common.appInfo.AppIntentService");
        intent.setPackage(initAc.getPackageName());
        initAc.startService(intent);
    }

    private void initData() {
        FullScreenVadManager.getInstance().initAd(this);
        mHomePresenter = new HomePresenter(this);
    }

    @TestOnly
    private void test() {
        if (isOnlyChoosePic()) return;
//        switchFragment(TEMPLATE_FRAG_ID);

        String thePath = Environment.getExternalStorageDirectory() + "/test1.jpg";
        PtuUtil.PTuIntentBuilder.build(this)
                .setPicPath(thePath)
                .putExtras(getIntent())
                .setTest()
                .startActivity();

//        toChangeFace(null);
//         ???????????????????????????????????????
        TheUserUtil.updateVipExpire(AllData.localUserId, (System.currentTimeMillis() + TimeDateUtil.DAY_MILS), new TheUser.UpdateVipExpireListener() {
            @Override
            public void success() {

            }

            @Override
            public void failed(String msg) {
                Log.e(TAG, "????????????");
            }
        });
    }


    /**
     * ??????????????????
     */
    private void initNavView() {
        mDrawerLayout = findViewById(R.id.draw_container);
        NavigationView mNavigationView = findViewById(R.id.home_nav_view);

        mHeaderView = mNavigationView.getHeaderView(0);
        mVipStatusTv = mHeaderView.findViewById(R.id.tv_vip_status);

        mSignInView = mHeaderView.findViewById(R.id.signInView);
        mUserInfoView = mHeaderView.findViewById(R.id.userInfoView);

        View mAboutView = mHeaderView.findViewById(R.id.aboutView);
        View mSettingsView = mHeaderView.findViewById(R.id.settingsView);
        View mFeedbackView = mHeaderView.findViewById(R.id.feedbackView);
        View mHelpView = mHeaderView.findViewById(R.id.helpView);
//        View mTagsManagerView = mHeaderView.findViewById(R.id.tagsManagerView);

        //??????????????????????????????
        if (AppConfig.isCloseVipFunction) {
            mVipStatusTv.setVisibility(View.GONE);
        }
        if (!AllData.isVip) {
            mVipStatusTv.setText("????????????VIP\n?????????????????????????????????????????????");
        } else {
            mVipStatusTv.setText("?????????????????????????????????????????????\n???????????????" + TimeDateUtil.time2EnglishFormat(AllData.localUserVipExpire));
        }
        mUserInfoView.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
            mDrawerLayout.closeDrawers();
        });
        //??????
        mHelpView.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HelpActivity.class));
            mDrawerLayout.closeDrawers();
        });
        //??????
        mAboutView.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AboutAppActivity.class));
            mDrawerLayout.closeDrawers();
        });
        //??????
        mFeedbackView.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, FeedBackActivity.class));
            mDrawerLayout.closeDrawers();
        });
        //??????
        mSettingsView.setOnClickListener(v -> {
            US.putSettingEvent(US.SETTING_ENTER_SETTING);
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
            mDrawerLayout.closeDrawers();
        });
    }

    /**
     * ?????????View
     */
    private void initView() {
        mMenuIv = findViewById(R.id.iv_left_btn);
        mFilterIv = findViewById(R.id.iv_action_filter);
        mSearchIv = findViewById(R.id.iv_action_search);
        mMenuIv.setImageResource(R.drawable.ic_menu_black);
        //?????????????????????????????????????????????,????????????????????????????????????????????????
        if (isOnlyChoosePic()) {
            mMenuIv.setImageResource(R.drawable.round_arrow_back);
        }
        mMenuIv.setOnClickListener(this);
        mSearchIv.setOnClickListener(this);
        mFilterIv.setOnClickListener(this);

        initNavView();

        activityLayout = findViewById(R.id.layout_choose_picture_activity);
        getScreenSizeAgain(activityLayout);

        mFloatActionBtn = findViewById(R.id.fab_file);
        mFloatActionBtn.setOnClickListener(v -> {
            if (currentFrag == mLocalPicFragment && mLocalPicFragment != null) {
                mLocalPicFragment.updateSideDrawer(false);
            }
        });

        mViewPager = findViewById(R.id.choose_content_view_pager);
        mTabLayout = findViewById(R.id.choose_tabLayout);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // ViewPager?????????Fragment??????????????????????????????????????????????????????
                currentFrag = getFragByID(position);
                if (position == LOCAL_FRAG_ID) {
                    mFloatActionBtn.show();
                } else {
                    mFloatActionBtn.hide();
                }
//                mViewPager.setUserInputEnabled(position != 2);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void changeMenu() {
        if (mViewPager.getCurrentItem() == 0) {
            mFilterIv.setVisibility(View.INVISIBLE);
        } else if (mViewPager.getCurrentItem() == 1) {
            mFilterIv.setVisibility(View.VISIBLE);
        } else {
            mFilterIv.setVisibility(View.INVISIBLE);
        }
    }

    void showPopMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.main_online_pic);
        popupMenu.setOnMenuItemClickListener(item -> {
            int i = item.getItemId();
            return true;
        });
        popupMenu.show();
    }

    private void initFragment() {
        Log.e(TAG, "????????? mLocalPicFragment == null:" + (mLocalPicFragment == null));
        if (mLocalPicFragment == null) {
            mLocalPicFragment = LocalPicFragment.newInstance();
        }

        Log.e(TAG, "????????? mTemplateChooseFragment == null:" + (mLocalPicFragment == null));
        if (mTemplateFragment == null) {
            mTemplateFragment = PicResourcesFragment.newInstance(null, PicResource.CATEGORY_STYLE, isOnlyChoosePic());
        }

        Log.e(TAG, "????????? mTietuChooseFragment == null:" + (mLocalPicFragment == null));
        mViewPagerFragmentAdapter = new ViewPager2FragmentAdapter(this);
        mViewPagerFragmentAdapter.addFragment(mLocalPicFragment, "??????");
        mViewPagerFragmentAdapter.addFragment(mTemplateFragment, "??????");
        mViewPager.setAdapter(mViewPagerFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);
        //??????viewpager+tabLayout????????????
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            tab.setText(mViewPagerFragmentAdapter.getCurTitle(position));
        }).attach();

        int fragID = getIntent().getIntExtra(INTENT_EXTRA_FRAGMENT_ID, 0);
        // ?????????setAdapter???????????????????????????????????????????????????Fragment???????????????Adapter???????????????????????????????????????
        switchFragment(fragID);
    }

    public ViewPager2 getMainViewPager() {
        return mViewPager;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_left_btn:
                if (!isOnlyChoosePic()) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    super.onBackPressed();
                }
                break;
            case R.id.iv_action_search:
                US.putSearchSortEvent(US.SEARCH);
                Intent intent1 = new Intent(this, SearchActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent1, REQUEST_CODE_SEARCH);
                break;
            case R.id.iv_action_filter:
                showPopMenu(v);
                break;
        }
    }


    private void switchFragment(int fragId) {
        switch (fragId) {
            case LOCAL_FRAG_ID:
                mViewPager.setCurrentItem(LOCAL_FRAG_ID);
                currentFrag = mLocalPicFragment;
                return;
            case TEMPLATE_FRAG_ID:
                mViewPager.setCurrentItem(TEMPLATE_FRAG_ID);
                currentFrag = mTemplateFragment;
                return;
        }
    }

    private ChooseBaseFragment getFragByID(int fragId) {
        switch (fragId) {
            case LOCAL_FRAG_ID:
                return mLocalPicFragment;
            case TEMPLATE_FRAG_ID:
                return mTemplateFragment;
        }
        return mLocalPicFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("onActivityResult:??????????????????activity?????????.requestCode=" + requestCode + " resultCode =" + resultCode);

        if (resultCode == PtuActivity.RESULT_CODE_NORMAL_RETURN && data != null) {
            String action = data.getAction();
            if (action != null && action.equals(PTuResultData.FINISH)) {//???????????????
                setResult(0, new Intent(action));
//                if (mLocalPicFragment != null) {
//                    mLocalPicFragment.addUsedPath(
//                            data.getStringExtra(PTuResultData.RECENT_USE_PIC_PATH));
//                }
                finish();
                overridePendingTransition(0, R.anim.go_send_exit);
            } else if (action != null && action.equals(PtuActivity.LOAD_FAILED)) {//????????????
                String failedPath = data.getStringExtra(PTuResultData.FAILED_PATH);
                if (mLocalPicFragment != null) {
                    mLocalPicFragment.removeCurrent(failedPath);
                }
            } else if (action != null && action.equals(PTuResultData.SAVE_AND_LEAVE)) {//???????????????
                String newPicPath = data.getStringExtra(PTuResultData.NEW_PIC_PATH);
                if (mLocalPicFragment != null) {
                    mLocalPicFragment.addNewPath(newPicPath);
                }
            } else if (action != null && action.equals(PTuResultData.LEAVE)) {//????????????????????????
//                if (mLocalPicFragment != null) {
//                    mLocalPicFragment.addUsedPath(
//                            data.getStringExtra(PTuResultData.RECENT_USE_PIC_PATH));
//                }
            }
        }

        if (requestCode == REQUEST_CODE_DIG_FACE || requestCode == REQUEST_CODE_ERASE_EXPRESSION) {
            setResult(resultCode, data);
            finish();
        }

        //????????????
        if (requestCode == REQUEST_CODE_SEARCH && resultCode == RESULT_OK) {
            String path = data.getStringExtra(SearchActivity.Companion.getINTENT_EXTRA_SEARCH_FOLDER_PATH());
            if (path != null) {
                mViewPager.setCurrentItem(0);
                if (mLocalPicFragment != null) {
                    mLocalPicFragment.togglePicData(path);
                }
            } else {
                PicResource picResource = (PicResource) data.getSerializableExtra(SearchActivity.Companion.getINTENT_EXTRA_SEARCH_PIC_RES());
                US.putEditPicEvent(US.EDIT_PIC_FROM_SEARCH);
                if (picResource != null) {
                    choosePic(picResource, null, true);
                }
            }
            return;
        }

        //????????????
        if (requestCode == 11101 && mHomePresenter != null) {
            mHomePresenter.onActivityResult(requestCode, resultCode, data);
        }

        // ??????????????????, ????????????????????????????????????????????????????????????????????????adapter???????????????????????????
        // ??????????????????????????????????????????????????????????????????VIP???????????????????????????????????????????????????????????????
        if (AllData.hasOpenVipJust) {
            // ????????????Activity??????????????????????????????
            AllData.hasOpenVipJust = false;
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (isOnlyChoosePic()) {
            super.onBackPressed();
            return;
        }
        if (currentFrag != null) {
            if (currentFrag.onBackPressed()) {
                return;
            }
        }
        if (mViewPager.getCurrentItem() != 0) {
            switchFragment(0);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        //        ?????????????????????????????????????????????????????????????????????????????????
        if (!AllData.globalSettings.getSendShortcutNotifyExit()) {
            NotificationManager nm = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(0);
        }
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        // ???????????? ????????????
        MyDatabase.getInstance().close();
    }


    /**
     * ???????????????????????????
     */
    public boolean isOnlyChoosePic() {
        return isChooseTietu ||
                INTENT_ACTION_ONLY_CHOSE_PIC.equals(getIntent().getAction());
    }

    public Fragment getCurFragment() {
        return currentFrag;
    }

    public void deletePreferPath(String path) {
        if (mLocalPicFragment != null) {
            mLocalPicFragment.getPresenter().deletePreferPath(path);
            MyDatabase.getInstance().deletePreferPicPath(path);
        } else {
            ToastUtils.show("????????????");
        }
    }

    public void addPreferPath(String path) {
        if (mLocalPicFragment != null) {
            mLocalPicFragment.getPresenter().addPreferPath(path);
        } else {
            ToastUtils.show("????????????");
        }
    }

    /**
     * @param categoryList ??????????????????????????????????????????????????????????????????
     */
    public void choosePic(@NotNull PicResource picResource, @Nullable List<PicResource> categoryList, boolean isStyle) {
        BmobFile bmobFile = picResource.getUrl();
        if (bmobFile == null) {
            return; // ?????????
        }
        if (categoryList == null) categoryList = new ArrayList<>();
        String path = bmobFile.getUrl();
        String action = getIntent().getAction();
        Set<String> categories = getIntent().getCategories();
        if (categories != null && categories.contains(StyleTransferFragment.CHOOSE_PIC_CATEGORY_STYLE) && categoryList.size() > 0) {
            AllData.curStyleList = categoryList;
        }
        if (categories != null && categories.contains((StyleTransferFragment.CHOOSE_PIC_CATEGORY_CONTENT))) {
            AllData.curContentList = categoryList;
        }

        if (INTENT_ACTION_ONLY_CHOSE_PIC.equals(action)) {

            Intent data = new Intent();
            data.putExtra(INTENT_EXTRA_CHOSEN_PIC_RES, picResource);
            setResult(RESULT_OK, data);
            finish();
        } else {
            startPTuActivity(path, picResource, isStyle);
        }
    }

    public void toMakeGif(@NotNull List<String> picList) {
        //  ????????????????????????????????????GIF
        progressDialog = LoadingDialog.newInstance("??????????????????????????????...");
        progressDialog.showIt(HomeActivity.this);
        Observable
                .create(new ObservableOnSubscribe<List<String>>() {
                    @Override
                    public void subscribe(@NotNull ObservableEmitter<List<String>> emitter) throws Exception {
                        if (picList.size() > 50) {
                            emitter.onError(new Exception("1"));
                            return;
                        }
                        List<String> localPathList = new ArrayList<>();
                        for (String picPath : picList) {
                            // ??????url???????????????????????????????????????????????????????????????????????????????????????????????????
                            // ????????????????????????????????????
                            if (FileTool.urlType(picPath) == FileTool.UrlType.URL) {
                                try {
                                    picPath = Glide.with(HomeActivity.this)
                                            .load(picPath)
                                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                            .get()
                                            .getAbsolutePath();
                                } catch (Exception e) {
                                    emitter.onError(new Exception("2"));
                                    return;
                                }
                            }

                            // ????????????????????????????????????GIF?????????
                            // ???????????????
                            if (MediaInfoScanner.getInstance().isShortVideo(picPath)) {
                                emitter.onError(new Exception("3"));
                                return;
                            }
                            // ????????????????????????????????????????????????
                            localPathList.add(picPath);
                        }
                        emitter.onNext(localPathList);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onNext(@NotNull List<String> picList) {
                        PtuUtil.PTuIntentBuilder
                                .build(HomeActivity.this)
                                .setAction(PtuActivity.PTU_ACTION_PICS_MAKE_GIF)
                                .putStringArrayListExtra(PtuActivity.PTU_DATA_GIF_PIC_LIST, (ArrayList<String>) picList)
                                .putExtra(PtuActivity.INTENT_EXTRA_TO_CHILD_FUNCTION, PtuUtil.CHILD_FUNCTION_GIF)
                                .startActivityForResult(HomeActivity.REQUEST_CODE_MAKE_GIF);

                        currentFrag.cancelChosen();
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        US.putGifEvent(US.MULTI_PICS_MAKE_GIF);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if ("1".equals(e.getMessage())) {
                            showToast(R.string.too_many_pic_to_mack_gif);
                        } else if ("2".equals(e.getMessage())) {
                            showToast(R.string.part_pic_cannot_be_load);
                        } else if ("3".equals(e.getMessage())) {
                            showToast(R.string.contain_video_cannot_mack_gif);
                        } else {
                            showToast(R.string.unkown_error);
                        }
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                    }
                });


        //        }
    }

    private void startPTuActivity(String chosenPath, PicResource picResource, boolean isStyle) {
        PtuUtil.PTuIntentBuilder.build(this)
                .setPicPath(chosenPath)
                .putExtra(PtuActivity.INTENT_EXTRA_CHOSEN_TAGS, picResource.getTag())
                .putExtra(PtuActivity.INTENT_EXTRA_IS_STYLE, isStyle)
                .startActivityForResult(HomeActivity.REQUEST_CODE_NORMAL);

        mLocalPicFragment.addUsedPath(chosenPath);
        LogUtil.printMemoryInfo(TAG + "?????????P???", this);
        // intent.putExtra(PtuActivity.PTU_DATA_PIC_INFO, picResource);
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void getScreenSizeAgain(View root) {
        root.post(() -> {
            if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
                AllData.screenWidth = activityLayout.getWidth();
                AllData.screenHeight = activityLayout.getHeight();
            }
        });
        while (root.getParent() instanceof View) {
            root = (View) root.getParent();
        }
        final View finalRoot = root;
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
                    AllData.screenWidth = finalRoot.getWidth();
                    AllData.screenHeight = finalRoot.getHeight();
                }
                finalRoot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });   // ???????????????????????????????????????????????????????????????1%?????????????????????????????????finalRoot???????????????=0,
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
            AllData.screenWidth = activityLayout.getWidth();
            AllData.screenHeight = activityLayout.getHeight();
        }
    }

    /********************************??????????????????????????? ********************************/

    // TODO: 2017/3/9 0009  ?????????????????????????????????AC???context???????????????????????????application?????????????????????
    void getScreenSize() {
        if (AllData.screenWidth <= 0 || AllData.screenHeight <= 0) {
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            AllData.screenWidth = metric.widthPixels; // ????????????????????????
            AllData.screenHeight = metric.heightPixels;
        }
        if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            AllData.screenWidth = dm.widthPixels; // ???????????????????????????3200px???
            AllData.screenHeight = dm.heightPixels; // ???????????????????????????1280px???
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getRealSize(size);
                AllData.screenHeight = size.y;
                AllData.screenWidth = size.x;
            }
        }
    }

    NotificationManager nm;

    private void sendNotify() {
        //????????????????????????????????????
        if (!AllData.globalSettings.getSendShortcutNotify())
            return;
        // ??????????????????NotificationManager
        nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Android 8.0???????????????????????????channel????????????????????????????????????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CommonConstant.QUICKLY_PTU_NOTICE_CHANNEL_ID,
                    getText(R.string.quickly_ptu_notice_chanel_name), importance);
            mChannel.setDescription(getText(R.string.quickly_ptu_notice_chanel_description).toString());
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setShowBadge(false);
            nm.createNotificationChannel(mChannel);
        }
        // ??????????????????Notification
        Intent intentChose = new Intent(this, HomeActivity.class);
        intentChose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentChose.setAction("notify_ptu");
        //PendingIntent???????????????Intent
        PendingIntent piChoose = PendingIntent.getActivity(this, 0, intentChose,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // ?????????????????????????????????????????????RemoteViews???????????????Notification??????
        RemoteViews contentView = new RemoteViews(this.getPackageName(),
                R.layout.layout_notification);
        contentView.setImageViewResource(R.id.notify_icon, R.mipmap.app_logo);
        contentView.setImageViewResource(R.id.notify_make_image, R.mipmap.notify_make);
        contentView.setTextViewText(R.id.notify_make_name,
                getResources().getString(R.string.make_expression));

        contentView.setImageViewResource(R.id.notify_latest_image, R.mipmap.notify_latest);
        contentView.setTextViewText(R.id.notify_latest_name,
                getResources().getString(R.string.latest_pic));

        contentView.setOnClickPendingIntent(R.id.notify_layout_choose, piChoose);
        Intent latestIntent = new Intent(this, PtuActivity.class);
        latestIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        latestIntent.setAction(PtuActivity.PTU_ACTION_LATEST_PIC);
        //PendingIntent???????????????Intent
        PendingIntent piLatest = PendingIntent.getActivity(this, 0, latestIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notify_layout_latest, piLatest);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, initChannelId())
                .setCustomContentView(contentView)
                .setSmallIcon(R.mipmap.app_logo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CommonConstant.QUICKLY_PTU_NOTICE_CHANNEL_ID);
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //???????????????????????????????????????????????????????????????????????????
        nm.notify(0, notification);
        //        PTuLog.d(TAG, "??????????????????");
    }


    /**
     * ??????Notification ChannelID
     *
     * @return ??????id
     */
    private String initChannelId() {
        // ???????????????id
        String id = "baozoutptu";
        // ??????????????????????????????????????????.
        CharSequence name = "????????????";
        // ??????????????????????????????????????????
        String description = "?????????????????????";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel;
            mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            //?????????notificationmanager????????????????????????
            nm.createNotificationChannel(mChannel);
        }
        return id;
    }

    @Override
    public void switch2UserInfoView(LocalUserInfo
                                            userInfo, @androidx.annotation.Nullable String toastMsg) {
        if (toastMsg != null && !toastMsg.isEmpty()) {
            ToastUtils.show(toastMsg);
        }

        //??????????????????
        mSignInView.setVisibility(View.GONE);
        mUserInfoView.setVisibility(View.VISIBLE);

        ((TextView) mHeaderView.findViewById(R.id.userNameTv)).setText(userInfo.name);
        //????????????
        CoverLoader.INSTANCE.loadImageView(this, userInfo.coverUrl, mHeaderView.findViewById(R.id.headerCoverIv));

        if (loginDialog != null) {
            loginDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void switch2LoginView(@androidx.annotation.Nullable String toastMsg) {
        if (toastMsg != null && !toastMsg.isEmpty()) {
            ToastUtils.show(toastMsg);
        }

        //??????????????????
        // mSignInView.setVisibility(View.VISIBLE);
        mUserInfoView.setVisibility(View.VISIBLE);

        ((TextView) mHeaderView.findViewById(R.id.userNameTv)).setText(R.string.app_name);

        if (loginDialog != null) {
            loginDialog.dismissAllowingStateLoss();
        }

        ImageView headerImage = mHeaderView.findViewById(R.id.headerCoverIv);
        headerImage.setImageResource(R.mipmap.app_logo);
        headerImage.setOnClickListener(v ->
                startActivity(new Intent(this, SettingActivity.class)));
    }

    @Override
    public void setPresenter(HomeContract.Presenter presenter) {

    }

    public void openVip(View view) {
        mDrawerLayout.closeDrawers();
        if (AllData.localUserVipExpire < System.currentTimeMillis()) {
            US.putOpenVipEvent(US.CLICK_NOTICE_TO_VIP);
            OpenVipActivity.Companion.startOpenVipAc(this);
        } else {
            ToastUtils.show("???????????????");
        }
    }

    public void signIn(View view) {
        if (Util.DoubleClick.isDoubleClick(1000)) return;
        loginDialog = LoginDialog.Companion.newInstance();
        loginDialog.setQqLoginListener(() -> {
            mHomePresenter.loginByQQ();
            return null;
        });
        loginDialog.showIt(HomeActivity.this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPicInfoEvent(PicInfoEvent event) {
        if ("add".equals(event.getOp())) {
            addPreferPath(event.getPath());
        } else if ("delete".equals(event.getOp())) {
            deletePreferPath(event.getPath());
        }
    }
}