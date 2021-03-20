package com.mandi.intelimeditor.user.userAccount.WXLogin;// package com.mandi.intelimeditor.user.userAccount.WXLogin;
//
// import android.content.BroadcastReceiver;
// import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
//
// import com.tencent.mm.opensdk.constants.ConstantsAPI;
// import com.tencent.mm.opensdk.modelmsg.SendAuth;
// import com.tencent.mm.opensdk.openapi.IWXAPI;
// import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//
// import org.jetbrains.annotations.NotNull;
//
// import com.mandi.intelimeditor.common.appInfo.AppConfig;
// import com.mandi.intelimeditor.user.userAccount.ThirdLoginCallback;
//
// /**
//  * <pre>
//  *      author : liuguicen
//  *      time : 2019/07/26
//  *      version : 1.0
//  * <pre>
//  */
// public class WXLogin {
//     public static final int LOGIN_AC_RESULT_CODE = 3001;
//     private IWXAPI api;
//     private Context mContext;
//     private ThirdLoginCallback mLoginCallback;
//
//     public WXLogin(Context context, ThirdLoginCallback thirdLoginCallback) {
//         mContext = context;
//         mLoginCallback = thirdLoginCallback;
//     }
//
//     private void regToWx(@NotNull Context context) {
//         // 通过WXAPIFactory工厂，获取IWXAPI的实例
//         api = WXAPIFactory.createWXAPI(context, AppConfig.ID_IN_WEIXIN, true);
//
//         // 将应用的appId注册到微信
//         api.registerApp(AppConfig.ID_IN_WEIXIN);
//
//         //建议动态监听微信启动广播进行注册到微信
//         context.registerReceiver(new BroadcastReceiver() {
//             @Override
//             public void onReceive(Context context, Intent intent) {
//
//                 // 将该app注册到微信
//                 api.registerApp(AppConfig.ID_IN_WEIXIN);
//             }
//         }, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));
//
//     }
//
//
//     public void loginByWeixin() {
//         // send oauth request
//         final SendAuth.Req req = new SendAuth.Req();
//         req.scope = "snsapi_userinfo";
//         req.state = "wechat_sdk_baozouptu_login";
//         api.sendReq(req);  // 这里send之后会拿起WXEntityActivity，然后在里面做对应的处理
//     }
//
// }
