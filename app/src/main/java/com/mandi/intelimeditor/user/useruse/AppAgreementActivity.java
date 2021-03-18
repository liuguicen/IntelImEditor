package com.mandi.intelimeditor.user.useruse;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.mandi.intelimeditor.common.BaseActivity;
import com.mathandintell.intelimeditor.R;


/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/09/24
 *      version : 1.0
 * <pre>
 */
public class AppAgreementActivity extends BaseActivity {
    public static final String INTENT_ACTION_USER_AGREEMENT = "user agreement";
    public static final String INTENT_ACTION_PRIVACY_POLICY = "privacy policy";
    public static final String INTENT_ACTION_VIP_SERVICE_AGREEMENT = "vip_service_agreement";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        if (INTENT_ACTION_USER_AGREEMENT.equals(action)) {
            setTitle(R.string.user_agreement);
            ((TextView) findViewById(R.id.app_agreement_content)).setText(R.string.user_agreement_content);
        } else if (INTENT_ACTION_PRIVACY_POLICY.equals(action)) {
            setTitle(R.string.privacy_policies);
            ((TextView) findViewById(R.id.app_agreement_content)).setText(R.string.privacy_policies_content);
        } else if (INTENT_ACTION_VIP_SERVICE_AGREEMENT.equals(action)) {
            setTitle(R.string.vip_service_agreement_title);
            ((TextView) findViewById(R.id.app_agreement_content)).setText(R.string.vip_service_agreement_content);
        }
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_app_agreement;
    }
}
