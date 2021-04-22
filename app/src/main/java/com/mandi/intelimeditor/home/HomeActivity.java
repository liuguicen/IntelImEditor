package com.mandi.intelimeditor.home;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
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
import com.mandi.intelimeditor.ad.ttAD.videoAd.FullScreenVadManager;
import com.mandi.intelimeditor.ad.ttAD.videoAd.TTRewardVadManager;
import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.CommonConstant;
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
import com.mandi.intelimeditor.home.data.MediaInfoScanner;
import com.mandi.intelimeditor.home.localPictuture.LocalPicFragment;
import com.mandi.intelimeditor.home.tietuChoose.PicResourcesFragment;
import com.mandi.intelimeditor.home.tietuChoose.TextureFragment;
import com.mandi.intelimeditor.ptu.PtuActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultData;
import com.mandi.intelimeditor.ptu.tietu.TietuFragment;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.ViewPager2FragmentAdapter;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;
import com.mandi.intelimeditor.user.userSetting.AboutAppActivity;
import com.mandi.intelimeditor.user.userSetting.FeedBackActivity;
import com.mandi.intelimeditor.user.userSetting.SettingActivity;
import com.mandi.intelimeditor.user.userVip.OpenVipActivity;
import com.mandi.intelimeditor.user.userVip.VipUtil;
import com.mandi.intelimeditor.user.useruse.tutorial.HelpActivity;
import com.mandi.intelimeditor.bean.PicInfoEvent;
import com.mandi.intelimeditor.dialog.LoginDialog;
import com.mandi.intelimeditor.home.search.SearchActivity;
import com.mandi.intelimeditor.R;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import util.CoverLoader;

/**
 * 显示所选的最近的或某个文件夹下面的所有图片
 * 并且有选择文件夹，相机，空白图画图的功能
 */
public class HomeActivity extends BaseActivity implements HomeContract.View, View.OnClickListener {

    private String TAG = "HomeActivity";

    // 下面这两个可以合并复用
    public static final String PTU_ACTION_CHOOSE_TIETU = "choose_tietu";
    public static final String PTU_ACTION_CHOOSE_BASE = "action_choose_base";
    public static final String CHOOSE_PIC_CATEGORY_STYLE = "action_choose_base";
    public static final String CHOOSE_PIC_CATEGORY_CONTENT = "action_choose_base";

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
     * 跳转到这个页面时指定ID
     */
    public static final String INTENT_EXTRA_FRAGMENT_ID = "FRAGMENT_ID";

    public static final int LOCAL_FRAG_ID = 0;
    public static final int TEMPLATE_FRAG_ID = 1;
    public static final int TIETU_FRAG_ID = 2;

    boolean mIsFromCreate = false;
    static String intentAction = "";
    private boolean isChooseTietu = false;
    private boolean isChooseBase = false;
    private boolean isMakeTietu = false;

    private ChooseBaseFragment currentFrag;

    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private ViewPager2FragmentAdapter mViewPagerFragmentAdapter;
    private PicResourcesFragment mTemplateChooseFragment;
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
     * 过程描述：启动一个线程获取所有图片的路径，再启动一个子线程设置好GridView，而且要求这个子线程必须在ui线程之前启动
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
//测试代码不要提交
//        test();
    }

    /**
     * 不在onRestart调用，这个ac还需要处理launchAc的，因为launchAc点击之后就会关闭，
     * 所以提示开通VIP的对话框放到这里，同时处理launchAc的和本Ac的
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
    }

    /**
     * 初始化数据
     */
    private void initIntentData() {
        Intent sourceIntent = getIntent();
        if (sourceIntent != null) {
            if (sourceIntent.getData() != null) { // 如果是从其它应用过来需要编辑图片的
                US.putOtherEvent(US.OTHERS_EDIT_FROM_THIRD_APP);
                sourceIntent.setComponent(new ComponentName(this, PtuActivity.class));
                sourceIntent.setAction(PtuActivity.PTU_ACTION_THIRD_APP);
                startActivity(sourceIntent);
                finish();
                return;
            }
            intentAction = sourceIntent.getAction();
            if (PTU_ACTION_CHOOSE_TIETU.equals(intentAction)) {
                isChooseTietu = true;
            } else if (PTU_ACTION_CHOOSE_BASE.equals(intentAction)) {
                isChooseBase = true;
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
            AllData.globalSettings.readDeviceInfo();// 这个方法需要存储权限
            TTRewardVadManager.getInstance().initAd(this); // 头条广告的
            startBackgroundService(this);
        }
    }


    /**
     * 启动后台服务，
     *
     * @see AppIntentService
     * <p>1.在后台发送用户使用信息
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
        if (isMakeTietu) return;
//        switchFragment(TEMPLATE_FRAG_ID);

        Intent testIntent = new Intent(this, PtuActivity.class);
        testIntent.putExtras(getIntent());
        String thePath = Environment.getExternalStorageDirectory() + "/test.gif";


        PtuUtil.PTuIntentBuilder pTuIntentBuilder = PtuUtil.buildPTuIntent(this, thePath)
                .putExtras(getIntent());
        pTuIntentBuilder.getIntent().putExtra("is_test", true);
        pTuIntentBuilder.startActivityForResult(REQUEST_CODE_NORMAL);

//        toChangeFace(null);
//         不显示广告，频繁开发时需要
        TheUserUtil.updateVipExpire(AllData.localUserId, (System.currentTimeMillis() + TimeDateUtil.DAY_MILS), new TheUser.UpdateVipExpireListener() {
            @Override
            public void success() {

            }

            @Override
            public void failed(String msg) {
                Log.e(TAG, "更新失败");
            }
        });
    }


    /**
     * 初始化侧边栏
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

        //判断会员时间是否过期
        if (AllData.isCloseVipFunction) {
            mVipStatusTv.setVisibility(View.GONE);
        }
        if (!AllData.isVip) {
            mVipStatusTv.setText("点击开通VIP\n会员特权，去广告，使用所有功能");
        } else {
            mVipStatusTv.setText("会员特权，去广告，使用所有功能\n到期时间：" + TimeDateUtil.time2EnglishFormat(AllData.localUserVipExpire));
        }
        mUserInfoView.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
            mDrawerLayout.closeDrawers();
        });
        //帮助
        mHelpView.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HelpActivity.class));
            mDrawerLayout.closeDrawers();
        });
        //关于
        mAboutView.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AboutAppActivity.class));
            mDrawerLayout.closeDrawers();
        });
        //反馈
        mFeedbackView.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, FeedBackActivity.class));
            mDrawerLayout.closeDrawers();
        });
        //设置
        mSettingsView.setOnClickListener(v -> {
            US.putSettingEvent(US.SETTING_ENTER_SETTING);
            Intent intent = new Intent(this, SettingActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN);
            mDrawerLayout.closeDrawers();
        });
    }

    /**
     * 初始化View
     */
    private void initView() {
        mMenuIv = findViewById(R.id.iv_left_btn);
        mFilterIv = findViewById(R.id.iv_action_filter);
        mSearchIv = findViewById(R.id.iv_action_search);
        mMenuIv.setImageResource(R.drawable.ic_menu_black);
        //如果选择贴图，或者选择图片状态,从贴图过来时，就切换到贴图的情况
        if (isOnlyChoosePic()) {
            mMenuIv.setImageResource(R.drawable.ic_arrow_back_white);
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
                // ViewPager里面的Fragment生命周期无法准确获取，在这里手动监听
                currentFrag = getFragByID(position);
                if (position == 0) {
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
        Log.e(TAG, "启动了 mLocalPicFragment == null:" + (mLocalPicFragment == null));
        if (mLocalPicFragment == null) {
            mLocalPicFragment = LocalPicFragment.newInstance();
        }

        Log.e(TAG, "启动了 mTemplateChooseFragment == null:" + (mLocalPicFragment == null));
        if (mTemplateChooseFragment == null) {
            mTemplateChooseFragment = PicResourcesFragment.newInstance(PicResource.FIRST_CLASS_TEMPLATE,
                    PicResource.SECOND_CLASS_BASE, isOnlyChoosePic());
        }

        Log.e(TAG, "启动了 mTietuChooseFragment == null:" + (mLocalPicFragment == null));
        mViewPagerFragmentAdapter = new ViewPager2FragmentAdapter(this);
        mViewPagerFragmentAdapter.addFragment(mTemplateChooseFragment, "风格");
        mViewPagerFragmentAdapter.addFragment(mLocalPicFragment, "本地");
        mViewPager.setAdapter(mViewPagerFragmentAdapter);
        mViewPager.setOffscreenPageLimit(3);
        //使用viewpager+tabLayout界面切换
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            tab.setText(mViewPagerFragmentAdapter.getCurTitle(position));
        }).attach();

        int fragID = getIntent().getIntExtra(INTENT_EXTRA_FRAGMENT_ID, LOCAL_FRAG_ID);
        // 要放在setAdapter后面，不然没效，自己猜也知道，当前Fragment序号是放在Adapter里面的，没传入为空，不设置
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
                currentFrag = mTemplateChooseFragment;
                return;
        }
    }

    private ChooseBaseFragment getFragByID(int fragId) {
        switch (fragId) {
            case LOCAL_FRAG_ID:
                return mLocalPicFragment;
            case TEMPLATE_FRAG_ID:
                return mTemplateChooseFragment;
        }
        return mLocalPicFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("onActivityResult:开始处理其它activity的返回.requestCode=" + requestCode + " resultCode =" + resultCode);

        if (resultCode == PtuActivity.RESULT_CODE_NORMAL_RETURN && data != null) {
            String action = data.getAction();
            if (action != null && action.equals(PTuResultData.FINISH)) {//直接结束了
                setResult(0, new Intent(action));
//                if (mLocalPicFragment != null) {
//                    mLocalPicFragment.addUsedPath(
//                            data.getStringExtra(PTuResultData.RECENT_USE_PIC_PATH));
//                }
                finish();
                overridePendingTransition(0, R.anim.go_send_exit);
            } else if (action != null && action.equals(PtuActivity.LOAD_FAILED)) {//加载失败
                String failedPath = data.getStringExtra(PTuResultData.FAILED_PATH);
                if (mLocalPicFragment != null) {
                    mLocalPicFragment.removeCurrent(failedPath);
                }
            } else if (action != null && action.equals(PTuResultData.SAVE_AND_LEAVE)) {//保存了图片
                String newPicPath = data.getStringExtra(PTuResultData.NEW_PIC_PATH);
                if (mLocalPicFragment != null) {
                    mLocalPicFragment.addNewPath(newPicPath);
                }
            } else if (action != null && action.equals(PTuResultData.LEAVE)) {//离开没有保存图片
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

        //搜索返回
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
                    choosePic(picResource, null);
                }
            }
            return;
        }

        //登录相关
        if (requestCode == 11101 && mHomePresenter != null) {
            mHomePresenter.onActivityResult(requestCode, resultCode, data);
        }

        // 开通会员解锁, 开通成功后隐藏列表，用户重新点击，重新将列表加入adapter，一尺排除广告数据
        // 有可能从设置界面登录，登录之后异步获知用户是VIP，此时也需要处理广告的问题，这里加以判断，
        if (AllData.hasOpenVipJust) {
            // 重新启动Activity，清除广告相关的数据
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
        if (currentFrag != mLocalPicFragment) {
            switchFragment(LOCAL_FRAG_ID);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        //        如果设置退出后关闭应用，就需要在用户主动退出应用后关闭
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
    }


    /**
     * 是否属于只选图模式
     */
    public boolean isOnlyChoosePic() {
        return isChooseTietu || isChooseBase || isMakeTietu ||
                INTENT_ACTION_ONLY_CHOSE_PIC.equals(getIntent().getAction());
    }

    public void onChosenTietuOrBase(PicResource picResource, String chosenPath) {
        Intent intent = new Intent();
        if (isChooseTietu) {
            intent.putExtra(TietuFragment.INTENT_EXTRA_CHOSE_TIETU_RES, picResource);
            setResult(PtuActivity.RESULT_CODE_CHOSE_TIETU, intent);
            try {
                MyDatabase.getInstance().insertMyTietu(chosenPath, System.currentTimeMillis());
            } catch (Exception e) {
                // nothing
            } finally {
                MyDatabase.getInstance().close();
            }
            // 用户兴趣标签在另一个地方添加了
        } else if (isChooseBase) {
            intent.putExtra(PtuActivity.INTENT_EXTRA_CHOSE_BASE_PIC_RES, picResource);
            setResult(PtuActivity.RESULT_CODE_CHOSE_BASE, intent);
        }
        finish();
    }

    public Fragment getCurFragment() {
        return currentFrag;
    }

    public void deletePreferPath(String path) {
        if (mLocalPicFragment != null) {
            mLocalPicFragment.getPresenter().deletePreferPath(path);
            MyDatabase.getInstance().deletePreferPicPath(path);
        } else {
            ToastUtils.show("删除失败");
        }
    }

    public void addPreferPath(String path) {
        if (mLocalPicFragment != null) {
            mLocalPicFragment.getPresenter().addPreferPath(path);
        } else {
            ToastUtils.show("添加失败");
        }
    }

    /**
     * @param categoryList 选择的图片所属的类别或者文件夹下面的图片列表
     */
    public void choosePic(@NotNull PicResource picResource, @Nullable List<PicResource> categoryList) {
        BmobFile bmobFile = picResource.getUrl();
        if (bmobFile == null) {
            return; // 无动作
        }
        String path = bmobFile.getUrl();
        String action = getIntent().getAction();
        if (isChooseTietu || isChooseBase) {//选择贴图,不是一般的选择图片

        } else if (INTENT_ACTION_ONLY_CHOSE_PIC.equals(action)) {
            if (getIntent().getCategories().contains(CHOOSE_PIC_CATEGORY_STYLE)) {
                AllData.styleList = categoryList;
            }
            if (getIntent().getCategories().contains((CHOOSE_PIC_CATEGORY_STYLE))) {
                AllData.contentList = categoryList;
            }
            Intent data = new Intent();
            data.putExtra(INTENT_EXTRA_CHOSEN_PIC_RES, picResource);
            setResult(RESULT_OK, data);
            finish();
        } else {
            startPTuActivity(path, picResource);
        }
    }

    public void toMakeGif(@NotNull List<String> picList) {
        //  检查所选列表是否可以制作GIF
        progressDialog = LoadingDialog.newInstance("请稍后，正在解析文件...");
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
                            // 先把url地址转化成本地图片路径，一般情况下用户选中的图片，都已经下载好了，
                            // 所以这里时间应该不会很长
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

                            // 检查是否包含不能用于制作GIF的资源
                            // 比如短视频
                            if (MediaInfoScanner.getInstance().isShortVideo(picPath)) {
                                emitter.onError(new Exception("3"));
                                return;
                            }
                            // 检查图片尺寸差别是否过大，不合适
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
                    public void onNext(List<String> picList) {
                        Intent intent = new Intent(HomeActivity.this, PtuActivity.class);
                        intent.setAction(PtuActivity.PTU_ACTION_PICS_MAKE_GIF);
                        intent.putStringArrayListExtra(PtuActivity.PTU_DATA_GIF_PIC_LIST, (ArrayList<String>) picList);
                        intent.putExtra(PtuActivity.INTENT_EXTRA_TO_CHILD_FUNCTION, PtuUtil.CHILD_FUNCTION_GIF);
                        startActivityForResult(intent, HomeActivity.REQUEST_CODE_MAKE_GIF);
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

    private void startPTuActivity(String chosenPath, PicResource picResource) {
        Intent intent = new Intent(this, PtuActivity.class);
        String action = getIntent().getAction();
        mLocalPicFragment.addUsedPath(chosenPath);
        intent.setAction(PtuActivity.PTU_ACTION_NORMAL);
        intent.putExtra(PtuActivity.INTENT_EXTRA_PIC_PATH, chosenPath);
        intent.putExtra(PtuActivity.INTENT_EXTRA_CHOSEN_TAGS, picResource.getTag());

        // intent.putExtra(PtuActivity.PTU_DATA_PIC_INFO, picResource);
        startActivityForResult(intent, HomeActivity.REQUEST_CODE_NORMAL);
    }

    /**
     * 再次获取屏幕宽高，防止没获取到
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
        });   // 还有会有很大的几率获取不到屏幕的宽高，接近1%，这个方法没调用，或者finalRoot的宽高仍然=0,
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
            AllData.screenWidth = activityLayout.getWidth();
            AllData.screenHeight = activityLayout.getHeight();
        }
    }

    /********************************应用初始化相关代码 ********************************/

    // TODO: 2017/3/9 0009  其中一个如果多窗口时用AC的context获取是当前宽高，用application的获取的是整个
    void getScreenSize() {
        if (AllData.screenWidth <= 0 || AllData.screenHeight <= 0) {
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            AllData.screenWidth = metric.widthPixels; // 屏幕宽度（像素）
            AllData.screenHeight = metric.heightPixels;
        }
        if (AllData.screenWidth <= 100 || AllData.screenHeight <= 100) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            AllData.screenWidth = dm.widthPixels; // 屏幕宽（像素，如：3200px）
            AllData.screenHeight = dm.heightPixels; // 屏幕高（像素，如：1280px）
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
        //如果设置为不允许则不发送
        if (!AllData.globalSettings.getSendShortcutNotify())
            return;
        // 第一步：获取NotificationManager
        nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Android 8.0及以上版本必须添加channel，否则不能发送通知，还会弹很烦的文字弹窗
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
        // 第二步：定义Notification
        Intent intentChose = new Intent(this, HomeActivity.class);
        intentChose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentChose.setAction("notify_ptu");
        //PendingIntent是待执行的Intent
        PendingIntent piChoose = PendingIntent.getActivity(this, 0, intentChose,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // 当用户下来通知栏时候看到的就是RemoteViews中自定义的Notification布局
        RemoteViews contentView = new RemoteViews(this.getPackageName(),
                R.layout.layout_notification);
        contentView.setImageViewResource(R.id.notify_icon, R.mipmap.icon);
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
        //PendingIntent是待执行的Intent
        PendingIntent piLatest = PendingIntent.getActivity(this, 0, latestIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        contentView.setOnClickPendingIntent(R.id.notify_layout_latest, piLatest);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, initChannelId())
                .setCustomContentView(contentView)
                .setSmallIcon(R.mipmap.icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CommonConstant.QUICKLY_PTU_NOTICE_CHANNEL_ID);
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        //第三步：启动通知栏，第一个参数是一个通知的唯一标识
        nm.notify(0, notification);
        //        PTuLog.d(TAG, "发送通知完成");
    }


    /**
     * 创建Notification ChannelID
     *
     * @return 频道id
     */
    private String initChannelId() {
        // 通知渠道的id
        String id = "baozoutptu";
        // 用户可以看到的通知渠道的名字.
        CharSequence name = "暴走P图";
        // 用户可以看到的通知渠道的描述
        String description = "通知栏快捷按钮";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel;
            mChannel = new NotificationChannel(id, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            //最后在notificationmanager中创建该通知渠道
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

        //隐藏登录按钮
        mSignInView.setVisibility(View.GONE);
        mUserInfoView.setVisibility(View.VISIBLE);

        ((TextView) mHeaderView.findViewById(R.id.userNameTv)).setText(userInfo.name);
        //加载头像
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

        //隐藏登录按钮
        mSignInView.setVisibility(View.VISIBLE);
        mUserInfoView.setVisibility(View.GONE);

        ((TextView) mHeaderView.findViewById(R.id.userNameTv)).setText(R.string.app_name);

        if (loginDialog != null) {
            loginDialog.dismissAllowingStateLoss();
        }

        ImageView headerImage = mHeaderView.findViewById(R.id.headerCoverIv);
        headerImage.setImageResource(R.mipmap.icon);
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
            ToastUtils.show("会员已开通");
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