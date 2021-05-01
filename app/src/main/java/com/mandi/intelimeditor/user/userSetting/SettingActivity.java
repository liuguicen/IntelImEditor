package com.mandi.intelimeditor.user.userSetting;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.common.CommonConstant;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.common.util.Util;
import com.mandi.intelimeditor.dialog.FirstUseDialog;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.user.userAccount.LocalUserInfo;

import util.CoverLoader;
import com.mandi.intelimeditor.common.view.SettingsItemView;

import com.mandi.intelimeditor.user.userVip.OpenVipActivity;
import com.mandi.intelimeditor.user.useruse.tutorial.HelpActivity;
import com.mandi.intelimeditor.dialog.LoginDialog;
import com.mandi.intelimeditor.R;
import com.umeng.analytics.AnalyticsConfig;

import java.util.Arrays;

/**
 * Created by LiuGuicen on 2017/1/4 0004.
 */
public class SettingActivity extends BaseActivity implements SettingContract.View {
    public static final String ACTION_LOOK_GUIDE = "look_guide";
    SettingContract.Presenter mPresenter;
    private SettingsItemView clearCacheView;
    private SettingsItemView showNotifyToolsView;
    private SettingsItemView exitDisplayNotify;
    private SettingsItemView shareWithNoLabelView;
    private Button mBtnLogin;
    private View mUserInfoLayout;
    private LoginDialog loginDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new SettingPresenter(this);

        clearCacheView = findViewById(R.id.cacheView);

        showNotifyToolsView = findViewById(R.id.showNotifyToolsView);
        exitDisplayNotify = findViewById(R.id.exitDisplayNotify);
        shareWithNoLabelView = findViewById(R.id.shareWithNoLabelView);

        mBtnLogin = findViewById(R.id.setting_user_login);
        mUserInfoLayout = findViewById(R.id.layout_user_info);
        setClick();
        otherInit();
        mBtnLogin.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (CommonConstant.ACTION_LOGIN_FOR_OPEN_VIP.equals(getIntent().getAction())) {
                    // 后台数据显示直接调用登录的话用户就取消了，认为是在盗号，不信任？
                    final FirstUseDialog firstUseDialog = new FirstUseDialog(SettingActivity.this);
                    firstUseDialog.createDialog("提示", "我们需要您登录账号来开通VIP，请点击登录\n使用此账号可以跨设备享受VIP服务", () -> {
                        popupLoginWindow();
                    });
                }
                mBtnLogin.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // test();
    }

    private void otherInit() {
        if ("vivo".equals(AnalyticsConfig.getChannel(this))) { // VIVO登录相关不显示，登录不上，检测不让通过
            //            findViewById(R.id.setting_user_title).setVisibility(View.GONE);
            //            findViewById(R.id.setting_user_layout).setVisibility(View.GONE);
        }
    }

    private void test() {
        if ("test".equals(getIntent().getAction())) {
            OpenVipActivity.Companion.startOpenVipAc(this);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_setting;
    }

    private void setClick() {
        showNotifyToolsView.setOnItemClickListener(v -> {
            NotificationManager nm = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.cancel(0);
            }
            mPresenter.onShortCutNotifyChanged(showNotifyToolsView.isChecked());
        });
        exitDisplayNotify.setOnItemClickListener(v -> {
            AllData.globalSettings.save_ifNotifyWhenExit(exitDisplayNotify.isChecked());
        });
        shareWithNoLabelView.setOnItemClickListener(v -> {
            // 注意此时的状态是切换之后的
            boolean checked = shareWithNoLabelView.isChecked();
            if (checked) {
                US.putSettingEvent(US.SETTING_SHARE_WITHOUT_APPLICATION_LABEL);
            }
            AllData.globalSettings.saveSharedWithout(checked);
            // mPresenter.saveSharedWithout(checked);
        });
        clearCacheView.setOnClickListener(v -> mPresenter.clearAppData());
        mBtnLogin.setOnClickListener(v -> {
                    popupLoginWindow();
                }
        );
    }

    private void popupLoginWindow() {
        if (Util.DoubleClick.isDoubleClick(1000)) return;
        loginDialog = LoginDialog.Companion.newInstance();
        loginDialog.setQqLoginListener(() -> {
            mPresenter.loginByQQ();
            return null;
        });
        loginDialog.setDismissListener(() -> {
            US.putOpenVipEvent(US.OPEN_VIP_CANCEL_IN_LOGIN);
            return null;
        });
        loginDialog.showIt(this);
    }

    private void loginByWeiXin() {
        US.putUserLoginEvent(US.USER_LOGIN_BY_WEIXIN);
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    //确保qq登陆sdk，能接收到回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (loginDialog != null) {
            loginDialog.dismiss();
        }
        if (mPresenter != null)
            mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void switchSendShortCutNotify(boolean isSend) {
        showNotifyToolsView.setChecked(isSend);
    }

    @Override
    public void switchSendShortCutNotifyExit(boolean isSend) {
        exitDisplayNotify.setChecked(isSend);
    }

    @Override
    public void switchSharedWithout(boolean isWith) {
        shareWithNoLabelView.setChecked(isWith);
    }

    @Override
    public void showAppCache(String cacheString) {
        clearCacheView.setSubTitle(cacheString);
    }

    @Override
    public void showClearDialog(String[] infos, boolean[] preChosen) {
        final boolean[] userChosenItems = Arrays.copyOf(preChosen, preChosen.length);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("清除缓存")
                .setNegativeButton("确定", (dialog, which) -> mPresenter.realClearData(userChosenItems))
                .setPositiveButton("取消", (dialog, which) -> {
                })
                .setMultiChoiceItems(infos, preChosen, (dialog, which, isChecked) -> userChosenItems[which] = isChecked);
        builder.create().show();
    }

    @Override
    public void showClearResult(String res) {
        ToastUtils.show(res);
    }

    @Override
    public void setPresenter(SettingContract.Presenter presenter) {
    }

    @Override
    public void switch2UserInfoView(LocalUserInfo userInfo, @Nullable String toastMsg) {
        if (toastMsg != null && !toastMsg.isEmpty()) {
            ToastUtils.show(toastMsg);
        }
        mBtnLogin.setVisibility(View.GONE);
        mUserInfoLayout.setVisibility(View.VISIBLE);
        View toOpenVipView = mUserInfoLayout.findViewById(R.id.open_vip);
        if (AppConfig.isCloseVipFunction) {
            toOpenVipView.setVisibility(View.GONE);
        }
        toOpenVipView.setOnClickListener(v -> {
            if (AllData.localUserVipExpire == 0) { // 测试版，统计次数暂时用这个过滤多次点击
                AllData.localUserVipExpire = 1;
                US.putOpenVipEvent(US.CLICK_NOTICE_TO_VIP);
            }
            //            showToast("正在加紧完成此功能，敬请期待...");
            OpenVipActivity.Companion.startOpenVipAc(this);
        });
        mUserInfoLayout.findViewById(R.id.login_out_tv).setOnClickListener(v -> mPresenter.loginOut());

        ((TextView) mUserInfoLayout.findViewById(R.id.user_name)).setText(userInfo.name);
        //加载头像·
        CoverLoader.INSTANCE.loadImageView(this, userInfo.coverUrl, mUserInfoLayout.findViewById(R.id.user_head_image));
        //显示退出登录按钮
        findViewById(R.id.btn_logout).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_logout).setOnClickListener(v ->
                loginOut()
        );
    }

    private void loginOut() {
        String msg = "确认退出当前账号吗";
        if (AllData.isVip) {
            msg = "您当前的账号是VIP账号，退出后将无法享受VIP服务，确认退出吗";
        }
        new AlertDialog.Builder(this)
                .setTitle("退出当前账号")
                .setMessage(msg)
                .setPositiveButton("确认退出", (dialog1, which) -> mPresenter.loginOut())
                .setNegativeButton("取消", null)
                .create().show();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void switch2LoginView(@Nullable String toastMsg) {
        if (!TextUtils.isEmpty(toastMsg)) {
            ToastUtils.show(toastMsg);
        }
        //退出成功更新UI
        mBtnLogin.setVisibility(View.VISIBLE);
        mUserInfoLayout.setVisibility(View.GONE);
        //隐藏退出登录按钮
        findViewById(R.id.btn_logout).setVisibility(View.GONE);
    }

    @Override
    public void onUserLoginSuccess() {
        setResult(CommonConstant.RESULT_CODE_HAS_USER_LOGIN_SUCCESS);
        // 如果是为了开通VIP而进入，并且登录成功
        // 那么跳转到开通VIP的界面
        if (CommonConstant.ACTION_LOGIN_FOR_OPEN_VIP.equals(getIntent().getAction())) {
            if (!AllData.isVip) { // 登录之后判断VIP是异步的，这里的判断不是绝对有效的，在开通VIP的界面再判断一次
                OpenVipActivity.Companion.startOpenVipAc(this);
                finish();
            }
        }
    }

    /**
     * 跳转到引导页
     */
    public void toAppGuidePage(View view) {
        Intent intent = new Intent(SettingActivity.this, HelpActivity.class);
        intent.setAction(ACTION_LOOK_GUIDE);
        startActivity(intent);
    }

    /**
     * 跳转到关于页
     */
    public void toAppAboutPage(View view) {
        startActivity(new Intent(SettingActivity.this, AboutAppActivity.class));
    }

    /**
     * 好评
     */
    public void toGiveStar(View view) {
        US.putSettingEvent(US.SETTING_GIVE_GOOD_COMMENTS);
        if (mPresenter != null) {
            mPresenter.gotoMark();
        }
    }

    public void toCommunicateGroup(View view) {
        toQQGroup(this, AppConfig.QQ_GROUP_COMMUNICATE_KEY);
    }

    public void toFeedBackGroup(View view) {
        toQQGroup(this, AppConfig.QQ_GROUP_FEEDBACK_KEY);
    }

    public static void toQQGroup(Context context, String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            ToastUtils.show("未安装手Q或安装的版本不支持");
        }
    }

    /**
     * 跳转到反馈页面
     */
    public void toAppFeedbackPage(View view) {
        startActivity(new Intent(SettingActivity.this, FeedBackActivity.class));
    }
}
