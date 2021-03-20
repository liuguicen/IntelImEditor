package com.mandi.intelimeditor.ptu.saveAndShare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.user.US;
import com.mandi.intelimeditor.R;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

/**
 * Created by LiuGuicen on 2016/12/26 0026.
 * 专门用于qq分享的类
 */
public class MyQQShare extends Fragment {
    static final String TAG = "MyQQShare";
    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e("QQShare", "OnCreate发生调用");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Log.e("QQShare", "onDestroy发生调用");
        super.onDestroy();
    }

    public void share(String picPath, Context context) {
        LogUtil.d("分享到QQ的路径为 = " + picPath);
        Tencent myTencent;
        mContext = context;
        myTencent = Tencent.createInstance(AppConfig.ID_IN_YINGYONGBAO, mContext.getApplicationContext());
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, picPath);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, IntelImEditApplication.appContext.getResources().getString(R.string.app_name));
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        myTencent.shareToQQ((AppCompatActivity) mContext, params, new BaseUiListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Tencent.onActivityResultData(requestCode, resultCode, data, new BaseUiListener());
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(this).commitAllowingStateLoss();
        }
    }

    private class BaseUiListener implements IUiListener {
        @Override
        public void onComplete(Object o) {
            //ToastUtils.show("onComplete:", Toast.LENGTH_LONG).show();
            if (o instanceof JSONObject) {
                doComplete((JSONObject) o);
            }
        }

        public void doComplete(JSONObject values) {
        }

        @Override
        public void onError(UiError e) {
          /*  ToastUtils.show("onError:" + "code:" + e.errorCode + ", msg:"
                    + e.errorMessage + "+detail:" + e.errorDetail, Toast.LENGTH_LONG).show();*/
            LogUtil.e(e.errorMessage);
            US.putSaveAndShareEvent("qq_sdk_share_error" + e.errorCode);
            ToastUtils.show("分享出错了！");
        }

        @Override
        public void onCancel() {
            //  ToastUtils.show("onCancel", Toast.LENGTH_LONG).show();
            US.putSaveAndShareEvent("qq_sdk_share_cancel");
            Log.e(TAG, "onCancel ");
        }
    }
}
