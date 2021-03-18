package com.mandi.intelimeditor.user.userAccount;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mandi.intelimeditor.common.appInfo.AppConfig;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.user.US;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class QQLogin implements IUiListener {
    public static final String TAG = "QQLogin";
    private final Activity mActivity;
    private Tencent mTencent;
    private ThirdLoginCallback mLoginCallback;
    private UserInfo mInfo;

    public QQLogin(Activity activity, ThirdLoginCallback thirdLoginCallback) {
        this.mActivity = activity;
        mTencent = Tencent.createInstance(AppConfig.ID_IN_YINGYONGBAO, mActivity.getApplicationContext());
        mLoginCallback = thirdLoginCallback;
    }

    public void loginByQQ() {
        String SCOPE = "get_user_info";
        mTencent.login(mActivity, SCOPE, this);
    }

    @Override
    public void onComplete(Object response) {
        String loginMsg;
        if (!(response instanceof JSONObject)) {
            loginMsg = "返回为空" + "登录失败";
        } else {
            JSONObject jsonResponse = (JSONObject) response;
            if (jsonResponse.length() == 0) {
                loginMsg = "返回为空" + "登录失败";
            } else {
                doComplete(jsonResponse);
                return;
            }
        }
        // 没有登录成功
        mLoginCallback.onThirdLoginFailed(loginMsg);
        US.putUserLoginEvent(US.QQ_LOGIN_ERROR);
    }

    @Override
    public void onError(UiError uiError) {
        US.putUserLoginEvent(US.QQ_LOGIN_ERROR);
        mLoginCallback.onThirdLoginFailed("登录出错");
    }

    @Override
    public void onCancel() {
        US.putUserLoginEvent(US.QQ_LOGIN_CANCEL);
        mLoginCallback.onThirdLoginCancel();
    }

    private void doComplete(@NonNull JSONObject values) {
        Log.d("SDKQQAgentPref", "AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
        initOpenidAndToken(values);
        mLoginCallback.startLoadUserInfo();
        loadUserInfo();
    }

    public void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
            String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
            String expires_in = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires_in)
                    && !TextUtils.isEmpty(openId)) {
                if (mLoginCallback != null) {
                    mLoginCallback.onGetId(openId);
                }
                mTencent.setAccessToken(token, expires_in);
                mTencent.setOpenId(openId);
                // long realExpire = Long.parseLong(expires_in) + System.currentTimeMillis();
                // SPUtil.putUserInfo(token, realExpire);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadUserInfo() {
        if (mTencent != null && mTencent.isSessionValid()) {
            IUiListener listener = new IUiListener() {

                @Override
                public void onError(UiError e) {
                    Log.e(TAG, e.errorMessage);
                }

                @Override
                public void onComplete(final Object response) {
                    new Thread() {

                        @Override
                        public void run() {
                            JSONObject json = (JSONObject) response;

                            if (json.has("figureurl_qq_2")) {
                                String coverUrl = null;
                                String nickName = null;
                                try {
                                    coverUrl = json.getString("figureurl_qq_2");
                                    nickName = json.getString("nickname");
                                } catch (JSONException ignored) {
                                }
                                String finalNickName = nickName;
                                String finalCoverUrl = coverUrl;
                                mActivity.runOnUiThread(() -> {
                                    if (mLoginCallback != null) {
                                        mLoginCallback.onGetName(finalNickName);
                                        mLoginCallback.onDownloadHeadImageFinish(finalCoverUrl);
                                        US.putUserLoginEvent(US.QQ_LOGIN_SUCCESS);
                                        mLoginCallback.onThirdLoginFinish();
                                    }
                                });
                            } else {
                                Log.e(TAG, "无法获取用户名和头像信息");
                            }
                        }

                    }.start();
                }

                @Override
                public void onCancel() {

                }
            };
            mInfo = new UserInfo(mActivity, mTencent.getQQToken());
            mInfo.getUserInfo(listener);
        } else {
            mLoginCallback.onDownloadHeadImageFinish(null);
        }
    }

    /**
     * 根据一个网络连接(String)获取bitmap图像
     */
    public static Bitmap getbitmap(String imageUri) {
        LogUtil.v("getbitmap:" + imageUri);
        // 显示网络上的图片
        Bitmap bitmap = null;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                    .openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();

            Log.v(TAG, "image download finished." + imageUri);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            bitmap = null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "getbitmap bmp fail---");
            bitmap = null;
        }
        return bitmap;
    }

    public void onActivityResultData(int requestCode, int resultCode, Intent data) {
        Tencent.onActivityResultData(requestCode, resultCode, data, this);
    }
}
