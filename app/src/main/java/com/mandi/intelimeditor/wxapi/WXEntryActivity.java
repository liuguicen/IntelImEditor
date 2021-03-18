package com.mandi.intelimeditor.wxapi;// package a.baozouptu.wxapi;
//
// import android.app.Activity;
// import android.content.Intent;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.util.Log;
// import android.widget.ImageView;
// import android.widget.Toast;
//
// import com.tencent.mm.opensdk.constants.ConstantsAPI;
// import com.tencent.mm.opensdk.modelbase.BaseReq;
// import com.tencent.mm.opensdk.modelbase.BaseResp;
// import com.tencent.mm.opensdk.modelbiz.SubscribeMessage;
// import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
// import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
// import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
// import com.tencent.mm.opensdk.modelmsg.SendAuth;
// import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
// import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
// import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
// import com.tencent.mm.opensdk.openapi.IWXAPI;
// import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
// import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//
// import org.json.JSONException;
// import org.json.JSONObject;
//
// import java.io.UnsupportedEncodingException;
// import java.lang.ref.WeakReference;
//
// import a.baozouptu.R;
// import a.baozouptu.common.appInfo.AppConfig;
// import a.baozouptu.user.userAccount.LocalUserInfo;
// import a.baozouptu.user.userAccount.ThirdLoginCallback;
// import a.baozouptu.user.userAccount.WXLogin.NetworkUtil;
// import a.baozouptu.user.userAccount.WXLogin.WXLogin;
// import okhttp3.Response;
//
// /**
//  * <pre>
//  *      author : liuguicen
//  *      time : 2019/07/26
//  *      version : 1.0
//  * <pre>
//  */
// public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
//     private static String TAG = "MicroMsg.WXEntryActivity";
//
//     private IWXAPI api;
//     private WXMsgHandler handler;
//     private Intent userIntent = new Intent();
//
//     private static class WXMsgHandler extends Handler {
//         private final WeakReference<WXEntryActivity> wxEntryActivityWeakReference;
//
//         public WXMsgHandler(WXEntryActivity wxEntryActivity) {
//             wxEntryActivityWeakReference = new WeakReference<WXEntryActivity>(wxEntryActivity);
//         }
//
//         @Override
//         public void handleMessage(Message msg) {
//             int tag = msg.what;
//             Bundle data = msg.getData();
//             JSONObject json = null;
//             WXEntryActivity ac = wxEntryActivityWeakReference.get();
//             Intent userIntent = ac.userIntent;
//             switch (tag) {
//                 case NetworkUtil.GET_TOKEN: {
//                     try {
//                         json = new JSONObject(data.getString(NetworkUtil.RESULT_KEY));
//                         String openId, accessToken, refreshToken, scope;
//                         openId = json.getString("openid");
//                         accessToken = json.getString("access_token");
//                         refreshToken = json.getString("refresh_token");
//                         scope = json.getString("scope");
//                         userIntent.putExtra(LocalUserInfo.OPEN_ID_KEY, openId);
//                         userIntent.putExtra(LocalUserInfo.ACCESS_TOCKEN_KEY, accessToken);
//                         userIntent.putExtra(LocalUserInfo.REFRESH_TOCKE_KEY, refreshToken);
//                         // 得到数据之后，然后获取简略用户信息
//                         NetworkUtil.sendWxAPI(ac.handler, String.format("https://api.weixin.qq.com/sns/auth?" +
//                                 "access_token=%s&openid=%s", accessToken, openId), NetworkUtil.CHECK_TOKEN);
//                     } catch (JSONException e) {
//                         Log.e(TAG, e.getMessage());
//                     }
//                     break;
//                 }
//                 case NetworkUtil.CHECK_TOKEN: {
//                     try {
//                         json = new JSONObject(data.getString(NetworkUtil.RESULT_KEY));
//                         int errcode = json.getInt("errcode");
//                         if (errcode == 0) {
//                             NetworkUtil.sendWxAPI(ac.handler, String.format("https://api.weixin.qq.com/sns/userinfo?" +
//                                             "access_token=%s&openid=%s", userIntent.getStringExtra(LocalUserInfo.ACCESS_TOCKEN_KEY),
//                                     userIntent.getStringExtra(LocalUserInfo.OPEN_ID_KEY)), NetworkUtil.GET_INFO);
//                         } else {
//                             NetworkUtil.sendWxAPI(ac.handler, String.format("https://api.weixin.qq.com/sns/oauth2/refresh_token?" +
//                                             "appid=%s&grant_type=refresh_token&refresh_token=%s", "wxd930ea5d5a258f4f",
//                                     userIntent.getStringExtra(LocalUserInfo.REFRESH_TOCKE_KEY)), NetworkUtil.REFRESH_TOKEN);
//                         }
//                     } catch (JSONException e) {
//                         Log.e(TAG, e.getMessage());
//                     }
//                     break;
//                 }
//                 case NetworkUtil.REFRESH_TOKEN: {
//                     try {
//                         // 先刷新，再获取用户信息
//                         json = new JSONObject(data.getString(NetworkUtil.RESULT_KEY));
//                         String openId = json.getString("openid");
//                         userIntent.putExtra(LocalUserInfo.OPEN_ID_KEY, openId);
//                         String accessToken = json.getString("access_token");
//                         userIntent.putExtra(LocalUserInfo.ACCESS_TOCKEN_KEY, accessToken);
//                         String refreshToken = json.getString("refresh_token");
//                         userIntent.putExtra(LocalUserInfo.REFRESH_TOCKE_KEY, refreshToken);
//                         String scope = json.getString("scope");
//                         NetworkUtil.sendWxAPI(ac.handler, String.format("https://api.weixin.qq.com/sns/userinfo?" +
//                                 "access_token=%s&openid=%s", accessToken, openId), NetworkUtil.GET_INFO);
//                     } catch (JSONException e) {
//                         Log.e(TAG, e.getMessage());
//                     }
//                     break;
//                 }
//                 case NetworkUtil.GET_INFO: {
//                     try {
//                         json = new JSONObject(data.getString("result"));
//                         final String nickname, sex, province, city, country, headimgurl;
//                         headimgurl = json.getString("headimgurl");
//                         String encode;
//                         encode = getcode(json.getString("nickname"));
//                         nickname = "nickname: " + new String(json.getString("nickname").getBytes(encode), "utf-8");
//                         userIntent.putExtra(LocalUserInfo.USER_NAME_KEY, nickname);
//                         sex = "sex: " + json.getString("sex");
//                         province = "province: " + json.getString("province");
//                         city = "city: " + json.getString("city");
//                         country = "country: " + json.getString("country");
//                         // 最后获取头像
//                         NetworkUtil.getImage(ac.handler, headimgurl, NetworkUtil.GET_IMG);
//                     } catch (JSONException e) {
//                         Log.e(TAG, e.getMessage());
//                     } catch (UnsupportedEncodingException e) {
//                         Log.e(TAG, e.getMessage());
//                     }
//                     break;
//                 }
//                 case NetworkUtil.GET_IMG: {
//                     byte[] imgdata = data.getByteArray("imgdata");
//                     final Bitmap bitmap;
//                     if (imgdata != null) {
//                         bitmap = BitmapFactory.decodeByteArray(imgdata, 0, imgdata.length);
//                     } else {
//                         bitmap = null;
//                     }
//                     userIntent.putExtra(LocalUserInfo.HEAD_IMAGE_KEY, bitmap);
//                     ac.setResult(WXLogin.LOGIN_AC_RESULT_CODE, userIntent);
//                     break;
//                 }
//             }
//         }
//     }
//
//     private static String getcode(String str) {
//         String[] encodelist = {"GB2312", "ISO-8859-1", "UTF-8", "GBK", "Big5", "UTF-16LE", "Shift_JIS", "EUC-JP"};
//         for (int i = 0; i < encodelist.length; i++) {
//             try {
//                 if (str.equals(new String(str.getBytes(encodelist[i]), encodelist[i]))) {
//                     return encodelist[i];
//                 }
//             } catch (Exception e) {
//
//             } finally {
//
//             }
//         }
//         return "";
//     }
//
//     @Override
//     public void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//
//         api = WXAPIFactory.createWXAPI(this, AppConfig.ID_IN_WEIXIN, false);
//         handler = new WXMsgHandler(this);
//         //注意：
//         // 第三方开发者如果使用透明界面来实现WXEntryActivity，需要判断handleIntent的返回值，
//         // 如果返回值为false，则说明入参不合法未被SDK处理，应finish当前透明界面，
//         // 避免外部通过传递非法参数的Intent导致停留在透明界面，引起用户的疑惑
//         try {
//             boolean result = api.handleIntent(getIntent(), this);
//             if (!result) {
//                 // ViseLog.d("参数不合法，未被SDK处理，退出");
//                 finish();
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//
//     }
//
//     @Override
//     protected void onNewIntent(Intent intent) {
//         super.onNewIntent(intent);
//         setIntent(intent);
//         try {
//             boolean result = api.handleIntent(getIntent(), this);
//             if (!result) {
//                 // ViseLog.d("参数不合法，未被SDK处理，退出");
//                 finish();
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
//
//     @Override
//     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//         super.onActivityResult(requestCode, resultCode, data);
//         try {
//             boolean result = api.handleIntent(data, this);
//             if (!result) {
//                 // ViseLog.d("参数不合法，未被SDK处理，退出");
//                 finish();
//             }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
//
//     /**
//      * 微信发送的请求将回调到onReq方法
//      */
//     @Override
//     public void onReq(BaseReq req) {
//         switch (req.getType()) {
//             case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
// //                goToGetMsg();
//                 break;
//             case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
// //                goToShowMsg((ShowMessageFromWX.Req) req);
//                 break;
//             default:
//                 break;
//         }
//         finish();
//     }
//
//     /**
//      * 发送到请求到微信后, 微信给予的响应结果，回调到这个onResp方法
//      */
//     @Override
//     public void onResp(BaseResp resp) {
//         int result = 0;
//
//         switch (resp.errCode) {
//             case BaseResp.ErrCode.ERR_OK:
//                 break;
//             case BaseResp.ErrCode.ERR_USER_CANCEL:
//                 userIntent.putExtra(ThirdLoginCallback.THIRD_LOGIN_RESULT_KEY, ThirdLoginCallback.LOGIN_STATE_CANCEL);
//                 break;
//             case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                 userIntent.putExtra(ThirdLoginCallback.THIRD_LOGIN_RESULT_KEY, ThirdLoginCallback.LOGIN_STATE_FAILED);
//                 break;
//             case BaseResp.ErrCode.ERR_UNSUPPORT:
//                 userIntent.putExtra(ThirdLoginCallback.THIRD_LOGIN_RESULT_KEY, ThirdLoginCallback.LOGIN_STATE_UNSUPPORT);
//                 break;
//             default:
//                 break;
//         }
//
//         Toast.makeText(this, getString(result) + ", type=" + resp.getType());
//
//
//         if (resp.getType() == ConstantsAPI.COMMAND_SUBSCRIBE_MESSAGE) {
//             SubscribeMessage.Resp subscribeMsgResp = (SubscribeMessage.Resp) resp;
//             String text = String.format("openid=%s\ntemplate_id=%s\nscene=%d\naction=%s\nreserved=%s",
//                     subscribeMsgResp.openId, subscribeMsgResp.templateID, subscribeMsgResp.scene, subscribeMsgResp.action, subscribeMsgResp.reserved);
//
//             Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//         }
//
//         if (resp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
//             WXLaunchMiniProgram.Resp launchMiniProgramResp = (WXLaunchMiniProgram.Resp) resp;
//             String text = String.format("openid=%s\nextMsg=%s\nerrStr=%s",
//                     launchMiniProgramResp.openId, launchMiniProgramResp.extMsg, launchMiniProgramResp.errStr);
//
//             Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//         }
//
//         if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_VIEW) {
//             WXOpenBusinessView.Resp launchMiniProgramResp = (WXOpenBusinessView.Resp) resp;
//             String text = String.format("openid=%s\nextMsg=%s\nerrStr=%s\nbusinessType=%s",
//                     launchMiniProgramResp.openId, launchMiniProgramResp.extMsg, launchMiniProgramResp.errStr, launchMiniProgramResp.businessType);
//
//             Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//         }
//
//         if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_WEBVIEW) {
//             WXOpenBusinessWebview.Resp response = (WXOpenBusinessWebview.Resp) resp;
//             String text = String.format("businessType=%d\nresultInfo=%s\nret=%d", response.businessType, response.resultInfo, response.errCode);
//
//             Toast.makeText(this, text, Toast.LENGTH_LONG).show();
//         }
//
//         // 登录
//         // 这里去获取token，直接用NetworkUtil工具，获取到数据，交给handler里面处理
//         if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
//             SendAuth.Resp authResp = (SendAuth.Resp) resp;
//             final String code = authResp.code;
//             NetworkUtil.sendWxAPI(handler, String.format("https://api.weixin.qq.com/sns/oauth2/access_token?" +
//                             "appid=%s&secret=%s&code=%s&grant_type=authorization_code", "wxd930ea5d5a258f4f",
//                     "1d6d1d57a3dd063b36d917bc0b44d964", code), NetworkUtil.GET_TOKEN);
//         }
//         finish();
//     }
//
// //    private void goToGetMsg() {
// //        Intent intent = new Intent(this, GetFromWXActivity.class);
// //        intent.putExtras(getIntent());
// //        startActivity(intent);
// //        finish();
// //    }
// //
// //    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
// //        WXMediaMessage wxMsg = showReq.message;
// //        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
// //
// //        StringBuffer msg = new StringBuffer();
// //        msg.append("description: ");
// //        msg.append(wxMsg.description);
// //        msg.append("\n");
// //        msg.append("extInfo: ");
// //        msg.append(obj.extInfo);
// //        msg.append("\n");
// //        msg.append("filePath: ");
// //        msg.append(obj.filePath);
// //
// //        Intent intent = new Intent(this, ShowFromWXActivity.class);
// //        intent.putExtra(Constants.ShowMsgActivity.STitle, wxMsg.title);
// //        intent.putExtra(Constants.ShowMsgActivity.SMessage, msg.toString());
// //        intent.putExtra(Constants.ShowMsgActivity.BAThumbData, wxMsg.thumbData);
// //        startActivity(intent);
// //        finish();
// //    }
// }