package com.mandi.intelimeditor.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.mandi.intelimeditor.ad.AdData;
import com.mandi.intelimeditor.ad.AdStrategyUtil;
import com.mandi.intelimeditor.ad.ISplashAdListener;
import com.mandi.intelimeditor.ad.kjAD.MyKjSplashAd;
import com.mandi.intelimeditor.ad.tencentAD.TencentSplashAd;
import com.mandi.intelimeditor.ad.ttAD.MyTTSplashAd;
import com.mandi.intelimeditor.common.Constants.Extras;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;


import com.mandi.intelimeditor.common.dataAndLogic.SPUtil;
import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.view.DialogFactory;
import com.mandi.intelimeditor.home.HomeActivity;
import com.mandi.intelimeditor.ptu.PtuUtil;
import com.mandi.intelimeditor.ptu.saveAndShare.PTuResultData;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.useruse.AppGuideActivity;
import com.mandi.intelimeditor.dialog.PrivacyDialog;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.TestOnly;

import java.util.ArrayList;
import java.util.List;


/**
 * 入口Activity，在这里会首次调用广点通的SDK。
 */
public class LaunchActivity extends BaseActivity implements ISplashAdListener {
    public static final String TAG = "LaunchActivity";
    public static final int REQUEST_CODE_APP_GUIDE = 1001;
    public static final int RESULT_CODE_APP_GUIDE_ENTER = 10001; //从欢迎页返回
    public static final int RESULT_CODE_OUT = 10002;

    // 第一个要足够长，5s, 保证第一个广告最大程度获取到，两个都设置短时间的反而不好
    public static final int SPLASH_FIRST_TIME_OUT = 5000;
    public static final int SPLASH_SECOND_TIME_OUT = 3000;

    private ViewGroup container;
    private TextView skipView;
    private static final int PERMISSION_REQUEST_CODE = 1;

    // 权限请求相关

    // 必选权限
    private static final String[] sNecessaryPermissionsList = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] sOptionalPermissionsList = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int PERMISSION_SETTING = 3;

    private TextView pauseView;
    //这里用一个列表处理更好，目前只有两家，暂时这样做
    private TencentSplashAd mTencentSplashAd;
    private MyTTSplashAd mMyTTSplashAd;
    private MyKjSplashAd myKjSplashAd;
    /**
     * 记录ac启动时间，用于第一个广告播放失败时，计算用户等待时间，
     * 如果等待时间不长，播放第二个广告
     */
    private long adStartTime = 0;
    private int splashTryNumber = 0;
    /**
     * 有些splashad依赖resume,作为第二个选择时resume可能不被调用，需要手动调用
     */
    private boolean hadResumed = false;
    private Handler permissionExceptionHandler;
    private Runnable toPermissionSettingTask;
    private Dialog toPermissionSettingDialog;
    private AdStrategyUtil adStrategyUtil;
    private boolean isUserPause = false;
    private boolean hadStartHomeAc = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        test();
        readLocalAdData();
        initStatusBar();

        // if (!AllData.hasReadConfig.hasReadAppGuide()) {
        //     startAppGuideAc();
        // } else
        if (!AllData.hasReadConfig.hasAgreeAppPrivacy()) {
            showPrivacyDialog();
        } else {
            checkPermissionAndStart();
        }
    }

    /**
     * 对于小米机型，应用进入后台后，再次点击桌面应用图标，APP会重新启动，无法恢复到进入后台前的界面。
     * 这个放在开屏广告逻辑是否显示之后，如果用户切换到后台，然后再次进入APP时，先开屏广告逻辑，然后进入之前的界面。
     */
    private boolean initStartUp() {
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return true;
        }
        return false;
    }

    /**
     * 全屏沉淀式状态栏
     */
    private void initStatusBar() {
        // 延伸显示区域到刘海
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            // 延伸显示区域到刘海
            getWindow().setAttributes(layoutParams);
        }
        // 设置页面全屏显示
        //        getWindow().getDecorView().setSystemUiVisibility(
        //                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_STABLE
        //        );
    }


    @Override
    public int getLayoutResId() {
        return R.layout.activity_launch;
    }

    @TestOnly
    private void test() {
        String thePath = Environment.getExternalStorageDirectory() + "/test.jpg";
        PtuUtil.PTuIntentBuilder
                .build(this)
                .setPicPath(thePath)
                .putExtras(getIntent())
                .startActivity();
        //        startActivityForResult(testIntent, REQUEST_CODE_NORMAL);
        Log.e(TAG, "完成时间  " + System.currentTimeMillis());
        finish();
    }

    private void checkPermissionAndStart() {
        // 如果targetSDKVersion >= 23，就要申请好权限。如果您的App没有适配到Android6.0（即targetSDKVersion < 23），那么只需要在这里直接调用fetchSplashAD接口。
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermission();
        } else {
            afterPermissionSuccess();
        }
    }

    /**
     * 权限申请完成开始执行
     */
    private void afterPermissionSuccess() {
        if (permissionExceptionHandler != null) {
            permissionExceptionHandler.removeCallbacks(toPermissionSettingTask);
        }
        long lastADShowTime = SPUtil.getLastSplashAdShowTime();
        //新安装，首次启动，不显示广告
        //        int lastAPPVersion = this.getSharedPreferences(SPConstants.APP_CONFIG, Context.MODE_PRIVATE)
        //                .getInt(SPConstants.APP_VERSION, -1);
        //        if (lastAPPVersion == -1) {
        //            lastADShowTime = System.currentTimeMillis();
        //            sp.edit().putLong(SPConstants.LAST_LAUNCH_AD_SHOW_TIME, lastADShowTime).apply();
        //        }

        // 广告控制策略，
        // 全局关闭广告
        // 特殊情况避免检查不显示
        // AllData.isVip = true;
        if (!LogUtil.testSplashAd
                && (AllData.isVip || AdData.judgeAdClose() ||
                System.currentTimeMillis() - lastADShowTime < AdData.LAUNCH_AD_TIME_INTERVAL)) {
            US.putSplashADEvent(US.SPLASH_AD_NOT_SHOW);
            LogUtil.d("本次启动不显示开屏广告, 上次显示时间 + " + lastADShowTime);
            startHomeAC();
        } else {
            LogUtil.d("本次启动准备显示开屏广告");
            initAdView();
            adStartTime = System.currentTimeMillis();
            adStrategyUtil = new AdStrategyUtil(AdData.AdSpaceName.SPLASH, AllData.appConfig.splash_ad_strategy);
            // 特殊情况强制跳转，防止第三方开屏广告出问题等原因，导致不跳转，比如一段时间腾讯开屏直接白屏30s以上才返回广告
            container.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isUserPause)
                        startHomeAC();
                }
            }, (long) (SPLASH_FIRST_TIME_OUT * 2.5));
            chooseAd2Show();
        }
    }

    private void chooseAd2Show() {
        if (adStrategyUtil.isShow("TT")) {
            US.putSplashADEvent(US.CHOOSE_SPLASH_AD + "_TT");
            showTTSplashAd(SPLASH_FIRST_TIME_OUT);
        } else if (adStrategyUtil.isShow("KJ")) {
            US.putSplashADEvent(US.CHOOSE_SPLASH_AD + "_KJ");
            showKJSplashAd(SPLASH_FIRST_TIME_OUT);
        } else { // 默认优量汇 (adStrategyUtil.isShow("TX"))
            US.putSplashADEvent(US.CHOOSE_SPLASH_AD + "_TX");
            showTencentSplashAd(SPLASH_FIRST_TIME_OUT);
        }
    }

    private void initAdView() {
        pauseView = findViewById(R.id.tv_ad_pause_to_see);
        container = this.findViewById(R.id.splash_container);
        skipView = findViewById(R.id.skip_view);
    }

    private void showTencentSplashAd(long timeout) {
        // 重要log，别删
        Log.e(TAG, "准备显示腾讯开屏广告:");
        mTencentSplashAd = new TencentSplashAd(this, container, pauseView, skipView,
                this, timeout);
        // 如果是Android6.0以下的机器，默认在安装时获得了所有权限，可以直接调用SDK
        mTencentSplashAd.fetchSplashAD();
        if (hadResumed) {
            mTencentSplashAd.onResume();
        }
    }

    private void showTTSplashAd(long timeOut) {
        Log.e(TAG, "准备显示头条开屏广告:");
        mMyTTSplashAd = new MyTTSplashAd(this, container, this, timeOut);
        mMyTTSplashAd.fetchSplashAD();
        if (hadResumed) {
            mMyTTSplashAd.onResume();
        }
    }

    private void showKJSplashAd(long timeout) {
        Log.e(TAG, "准备显示铠甲开屏广告");
        myKjSplashAd = new MyKjSplashAd(this, container, this, timeout);
        myKjSplashAd.fetchSplashAD();
        if (hadResumed) {
            myKjSplashAd.onResume();
        }
    }

    /**
     * ----------非常重要----------
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();
        for (String onePermission : sNecessaryPermissionsList) {
            if (ContextCompat.checkSelfPermission(this, onePermission) != PackageManager.PERMISSION_GRANTED) {
                lackedPermission.add(onePermission);
            }
        }

        // 可选权限，新安装才添加，需要在sp已经写入新用户标志之前添加，否则不能判断是否是新安装
        int optionalReadCount = AllData.appConfig.getOptionalPermissionCount();
        if (optionalReadCount < 2) {
            for (String onePermission : sOptionalPermissionsList) {
                if (ContextCompat.checkSelfPermission(this, onePermission) != PackageManager.PERMISSION_GRANTED) {
                    lackedPermission.add(onePermission);
                }
            }
            AllData.appConfig.putOptionPermissionCount(optionalReadCount + 1);
        }

        // 权限都已经有了
        if (lackedPermission.size() == 0) {
            afterPermissionSuccess();
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, PERMISSION_REQUEST_CODE);
            // oppo A5 手机会出现很奇怪的问题，用户禁止权限之后，不提示用户打开权限,进而白屏的问题
            // 这个问题很奇怪，偶尔有手机出现，出现过的手机也是有时有，有时没有，很难复现，这里只能
            // 设置一个延时任务强行检测用户是否白屏
            if (isOppoA5()) {
                permissionExceptionHandler = new Handler();
                toPermissionSettingTask = () ->
                        toPermissionSettingDialog = DialogFactory.noTitle(this,
                                "若您无法打开应用，请在设置-应用管理-艺术美图-权限 中打开所有权限",
                                "知道了", "", (dialog, which) -> dialog.dismiss());

                permissionExceptionHandler.postDelayed(toPermissionSettingTask, 15 * 1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isLackOptional = false;
            boolean isBanForever = false;
            boolean isLackNecessary = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) { //选择了“始终允许”
                    ToastUtils.show(this, "" + getString(R.string.permission) + permissions[i] + getString(R.string.apply_success));
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) { // 用户选择了禁止不再询问
                        US.putOtherEvent(US.OTHERS_FORBID_PERMISSION_FOREVER);
                        isBanForever = true;
                    } else { // 选择禁止
                        US.putOtherEvent(US.OTHERS_FORBID_PERMISSION);
                    }

                    if (isNecessaryPermission(permissions[i])) {
                        isLackNecessary = true;
                    } else {
                        isLackOptional = true;
                    }
                }
            }
            // 缺乏必须权限或者 缺乏可选权限且权限弹窗阅读只有1次
            if ((isLackOptional && AllData.appConfig.getOptionalPermissionCount() <= 1) || isLackNecessary) {
                showPermissionDialog(isBanForever, isLackNecessary);
            } else {
                afterPermissionSuccess();
            }
        }
    }

    private void showPermissionDialog(boolean isBanForever, boolean isLackNecessary) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
        String msg = isLackNecessary ? getString(R.string.app_necessary_permission_notice) :
                getString(R.string.app_option_permission_notice);
        msg += "\n" + getString(R.string.permission_use_statement);
        if (isBanForever) {
            msg += getString(R.string.setting_permission_guide);
        }
        builder.setTitle(getString(R.string.permission))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.go_to_permit),
                        (dialog, id) -> {
                            if (isBanForever) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null); // 注意就是"package",不用改成自己的包名
                                intent.setData(uri);
                                startActivityForResult(intent, PERMISSION_SETTING);
                            } else {
                                checkAndRequestPermission();
                            }
                        });
        Dialog mDealBanDialog = builder.create();
        mDealBanDialog.setCancelable(false);
        mDealBanDialog.setCanceledOnTouchOutside(false);
        if (mDealBanDialog != null && !mDealBanDialog.isShowing()) {
            mDealBanDialog.show();
        }
    }

    private boolean isNecessaryPermission(String permission) {
        for (String necessary : sNecessaryPermissionsList) {
            if (TextUtils.equals(permission, necessary)) return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_SETTING) {
            checkAndRequestPermission();
        } else if (requestCode == REQUEST_CODE_APP_GUIDE) {
            if (resultCode == RESULT_CODE_APP_GUIDE_ENTER) {
                showPrivacyDialog();
            } else {
                finish();
            }
        } else {
            if (data == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            String action = data.getAction();
            if (action != null && action.equals(PTuResultData.FINISH)) {
                setResult(0, new Intent(action));
                finish();
                overridePendingTransition(0, R.anim.go_send_exit);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean isOppoA5() {
        String model = Build.MODEL;
        if ("PBBM30".equals(model) || "PBAM00".equals(model) || "A73t".equals(model) || "PBBT30".equals(model)
                || "PBAT00".equals(model))
            return true;
        return false;
    }

    /**
     * 进入APP主界面
     */
    private void startHomeAC() {
        // if (!AllData.hasReadConfig.hasReadAppGuide()) {
        //     AllData.hasReadConfig.write_appGuide(true);
        //     startAppGuideAc();
        // } else {
        //  testDB1();
        //  testDB();
        //  if (checkVersion()) {
        if (initStartUp()) {
            return;
        }
        if (hadStartHomeAc) return; // 防止强制跳转等引起的跳转两次
        Intent intent = new Intent(this, HomeActivity.class);
        Intent sourceIntent = getIntent();
        if (sourceIntent != null && sourceIntent.getData() != null) { // 如果是从其它应用过来需要编辑图片的
            intent.setData(sourceIntent.getData());
        } else {
            intent.putExtra(Extras.START_CHOOSE_PIC, "com.mandi.intelimeditor.startChoosePictureAC");
        }
        startActivity(intent);
        hadStartHomeAc = true;
        // }
        finish();
    }

    /**
     * 显示隐私政策对话框
     */
    private void showPrivacyDialog() {
        PrivacyDialog privacyDialog = new PrivacyDialog();
        privacyDialog.setAgreeListener(agree -> {
            if (!agree) {
                AlertDialog builder = new AlertDialog.Builder(LaunchActivity.this)
                        .setTitle(getString(R.string.privacy_tips_title))
                        .setMessage(getString(R.string.privacy_tips_content))
                        .setNegativeButton("退出应用", (dialog, which) -> finish())
                        .setPositiveButton("去同意", (dialog, id) -> {
                            dialog.dismiss();
                            showPrivacyDialog();
                        }).setCancelable(false)
                        .create();
                if (builder != null && !builder.isShowing()) {
                    LogUtil.d(TAG, "");
                    builder.show();
                }
            } else {
                AllData.hasReadConfig.setAgreeAppPrivacy(true);
                IntelImEditApplication.appContext.initAfterUserAgree();  // 首次启动，用户没有同意之前，这个方法是没有调用的
                checkPermissionAndStart();
            }
            return null;
        });
        privacyDialog.setCancelable(false);
        privacyDialog.showIt(this);
    }

    private void startAppGuideAc() {
        Intent intent = new Intent(this, AppGuideActivity.class);
        intent.putExtra(Extras.START_CHOOSE_PIC, "com.mandi.intelimeditor.startChoosePictureAC");
        startActivityForResult(intent, REQUEST_CODE_APP_GUIDE);
    }

    @Override
    public void onResume() {
        super.onResume();
        hadResumed = true;
        if (mTencentSplashAd != null) {
            mTencentSplashAd.onResume();
        }
        if (mMyTTSplashAd != null) {
            mMyTTSplashAd.onResume();
        }
        if (myKjSplashAd != null) {
            myKjSplashAd.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTencentSplashAd != null) {
            mTencentSplashAd.onPause();
        }
    }

    @Override
    protected void onStop() {
        if (mMyTTSplashAd != null) {
            mMyTTSplashAd.onStop();
        }
        if (myKjSplashAd != null) {
            myKjSplashAd.onStop();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTencentSplashAd != null) {
            mTencentSplashAd.destroy();
        }
        if (permissionExceptionHandler != null) {
            permissionExceptionHandler.removeCallbacks(toPermissionSettingTask);
        }
        if (toPermissionSettingDialog != null) {
            toPermissionSettingDialog.dismiss();
        }
    }

    /**
     * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onAdError(String res) {
        // 第一个播放的广告因为各种原因不能正确播放，使用第二个
        splashTryNumber++;
        // 错误的原因可能是第一个广告本来就超时了，这种情况是否继续显示第二个广告？
        // 另外的原因是第一个广告没有超时，是因为其它各种原因出错了，这时候的广告加载时间，很可能还没有超过SPLASH_FIRST_TIME_OUT，
        // 这种情况下就可以显示第二个广告
        LogUtil.e(TAG, "onAdError " + (System.currentTimeMillis() - adStartTime) + " tryAgain " + splashTryNumber);
        if (System.currentTimeMillis() - adStartTime > SPLASH_FIRST_TIME_OUT || splashTryNumber >= 3) {
            startHomeAC();
            return;
        }
        // 显示第二个广告
        adStrategyUtil.remove(res);
        if (mTencentSplashAd != null) {
            mTencentSplashAd.destroy();
            mTencentSplashAd = null;
        } else if (mMyTTSplashAd != null) {
            mMyTTSplashAd.destroy();
            mMyTTSplashAd = null;
        } else if (myKjSplashAd != null) {
            myKjSplashAd.destroy();
            myKjSplashAd = null;
        }
        if (hadStartHomeAc) return;
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);
        chooseAd2Show();
    }

    @Override
    public void setUserPause(boolean userPause) {
        isUserPause = userPause;
    }

    @Override
    public void onAdExpose(String adResName) {
        SPUtil.putSplashAdShowTime(System.currentTimeMillis());
        SPUtil.addAndPutAdSpaceExposeNumber(AdData.AdSpaceName.SPLASH);
        US.putSplashADEvent(US.EXPOSURE + adResName);
    }

    @Override
    public void onAdFinish() {
        LogUtil.d(TAG, "onAdFinish");
        startHomeAC();
    }

    private void readLocalAdData() { // 防破解，不直接写在onCreate里面，可否？
        // 读取广告相关信息
        // AdData.readAdCloseData();
        AdData.readData();
    }
}
