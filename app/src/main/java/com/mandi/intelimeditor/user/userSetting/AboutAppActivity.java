package com.mandi.intelimeditor.user.userSetting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mandi.intelimeditor.common.BaseActivity;
import com.mandi.intelimeditor.ptu.draw.MosaicView;
import com.mandi.intelimeditor.user.useruse.AppAgreementActivity;
import com.mandi.intelimeditor.BuildConfig;
import com.mandi.intelimeditor.R;
import com.tencent.bugly.beta.Beta;




public class AboutAppActivity extends BaseActivity {

    private MosaicView mosaicView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((TextView) findViewById(R.id.about_version)).
                setText(getString(R.string.version_name, BuildConfig.VERSION_NAME));

        mosaicView = findViewById(R.id.imageView4);
        findViewById(R.id.about_version).setOnClickListener(v -> mosaicView.clear());
        findViewById(R.id.textView4).setOnClickListener(v -> mosaicView.undo());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_about_app;
    }

    /**
     * 检查更新
     */
    public void checkUpdate(View view) {
        Beta.checkAppUpgrade();
    }

    /**
     * 打开隐私政策
     *
     * @param view
     */
    public void toPolicy(View view) {
        Intent intent = new Intent(this, AppAgreementActivity.class);
        intent.setAction(AppAgreementActivity.INTENT_ACTION_PRIVACY_POLICY);
        startActivity(intent);
    }

    /**
     * 打开用户协议
     *
     * @param view
     */
    public void toAgreement(View view) {
        Intent intent = new Intent(this, AppAgreementActivity.class);
        intent.setAction(AppAgreementActivity.INTENT_ACTION_USER_AGREEMENT);
        startActivity(intent);
    }
}
