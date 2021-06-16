/**********************************************************************
 * AUTHOR：YOLANDA
 * DATE：2015年4月5日下午1:03:11
 * DESCRIPTION：create the File, and add the content.
 ***********************************************************************/
package com.mandi.intelimeditor.ptu.saveAndShare;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import androidx.appcompat.app.AppCompatActivity;

import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.dataAndLogic.AllData;
import com.mandi.intelimeditor.common.dataAndLogic.FileProviderToShare;
import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.dataAndLogic.ShareDBUtil;
import com.mandi.intelimeditor.common.util.ToastUtils;
import com.mandi.intelimeditor.user.US;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class ShareUtil {
    public static final String QQSHARE_TITLE = "发送给好友";
    public static final String WX_SHARE_TITLE = "发送给朋友";
    public static final String WX_TIME_LINE_TITLE = "微信朋友圈";


    public static List<ShareItemData> getShareInfo(Context context, List<ResolveInfo> resolveInfos) {
        MyDatabase myDatabase = MyDatabase.getInstance();
        List<Pair<String, String>> preferShare = new ArrayList<>();
        try {
            myDatabase.queryAllPreferShare(preferShare);
        } catch (Exception e) {
        }
        return ShareUtil.sortAndClearAcData(context, preferShare, resolveInfos);
    }


    /**
     * 将resolveInfos的Item按照preferApps的顺序排列
     * 也就是preferApps前面的在resolveInfos也在前面，preferApps中不存在的放到后面
     * preferApps中存在，但是resolveInfos中不存在的会从数据库
     *
     * @param preferApps 优先选择的应用Activity的title，越前面，优先级越高
     */
    public static List<ShareItemData> sortAndClearAcData(Context context, List<Pair<String, String>> preferApps, List<ResolveInfo> resolveInfos) {
        List<ShareItemData> appInfos = getItemDataForShow(context, resolveInfos);
        List<Pair<String, String>> delActivity = new ArrayList<>();
        int size = preferApps.size();
        for (int p = size - 1; p >= 0; p--) {
            CharSequence packageNmae = preferApps.get(p).first;
            CharSequence title = preferApps.get(p).second;
            int i = 0;
            for (; i < appInfos.size(); i++) {
                if (appInfos.get(i).getPackageName().equals(packageNmae) &&
                        appInfos.get(i).getTitle().equals(title)) {
                    resolveInfos.add(0, resolveInfos.remove(i));
                    appInfos.add(0, appInfos.remove(i));
                    break;
                }
            }
            //如果没找到这个信息,已经不存在这个开放的activity，将其从数据库删除
            if (i >= appInfos.size()) {
                delActivity.add(preferApps.get(p));
            }
        }
        for (Pair<String, String> ac : delActivity) {
            preferApps.remove(ac);
        }
        ShareDBUtil.deletePreferInfo(context, delActivity);
        return appInfos;
    }

    /**
     * 拿到要显示到分享列表的数据
     * 包含包名，title，图标
     */
    private static ArrayList<ShareItemData> getItemDataForShow(Context context, List<ResolveInfo> resolveInfos) {
        ArrayList<ShareItemData> drawableItems = new ArrayList<ShareItemData>();
        PackageManager mPackageManager = context.getPackageManager();
        for (int i = 0; i < resolveInfos.size(); i++) {
            ResolveInfo info = resolveInfos.get(i);
            ShareItemData dialogItemEntity = new ShareItemData(info.activityInfo.packageName,
                    info.loadLabel(mPackageManager), info.loadIcon(mPackageManager));
            drawableItems.add(dialogItemEntity);
        }
        return drawableItems;
    }

    /**
     * 通过系统分享内容出去
     *
     * @param resoveInfo    lxznzi
     * @param imgPathOrText 图片路径或者文字
     * @param type          分享内容的类型
     */
    public static void exeNormalShare(Context context, String chooserTitle,
                                      ResolveInfo resoveInfo, String imgPathOrText, Type type) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            switch (type) {
                case Image:
                    intent.setType("image/*");
                    File imgFile = new File(imgPathOrText);
                    Uri contentUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        contentUri = FileProviderToShare.getUriForFile(context,
                                "com.mandi.intelimeditor.FileProviderToShare", imgFile);
                    } else {
                        contentUri = Uri.fromFile(imgFile);
                    }
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    break;
                case Text:
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, imgPathOrText);
                    break;
            }
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            //参数是包名，类全限定名，注意直接用类名不行
            ComponentName cn = new ComponentName(resoveInfo.activityInfo.packageName,
                    resoveInfo.activityInfo.name);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, chooserTitle));
        } catch (Exception e) {
            ToastUtils.show(context, "分享失败");
        }
    }

    /**
     * 得到支持分享的应用的信息
     * 一个应用可能有多个
     */
    public static List<ResolveInfo> getAcInfo_SupportShare(Context context, Type type) {
        List<ResolveInfo> mApps;
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        switch (type) {
            case Image:
                intent.setType("image/*");
                break;
            default:
                intent.setType("text/plain");
                break;
        }
        PackageManager pm = context.getPackageManager();
        mApps = pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        return mApps;
    }

    /**
     * @return for代码共用，没找到更好方式，暂时这样
     */
    public static MyQQShare share(AppCompatActivity ac, ResolveInfo resolveInfo, String savePath) {
        // 将优先信息添加到数据库
        String packageName = resolveInfo.activityInfo.packageName;
        String title = resolveInfo.loadLabel(ac.getPackageManager()).toString();
        ShareDBUtil.inseartPreferInfo(ac, packageName, title);
        // 是腾讯的分享且用户设置可带有应用图标
        // 如果shareType是Image，那么分享的内容应该为图片在SD卡的路径
        if (ShareUtil.QQSHARE_TITLE.equals(title)
                && packageName.equals("com.tencent.mobileqq")
                && !AllData.globalSettings.getSharedWithout()) {
            return shareByQQSDK(ac, savePath);
        } /*else if (title.equals(ShareUtil.WX_SHARE_TITLE)
                && packageName.equals("com.tencent.mm")
                && !AllData.globalSettings.getSharedWithout()
                && !savePath.endsWith(".gif")) { // 不支持gif
            shareByWXSDK(ac, resolveInfo, savePath, packageName, title);
            return null;
        } */else {
            normalShare(ac, resolveInfo, savePath, packageName, title);
            return null;
        }
    }

    private static MyQQShare shareByQQSDK(AppCompatActivity ac, String savePath) {
        MyQQShare myQQShare = new MyQQShare();
        ac.getSupportFragmentManager().beginTransaction().add(
                myQQShare, "MyQQShare"
        ).commitAllowingStateLoss();
        myQQShare.share(savePath, ac);
        US.putSaveAndShareEvent(US.SAVE_AND_SHARE_SHARE, "QQ_SDK_Share" + " : " + "发送给好友");
        Log.d(US.SAVE_AND_SHARE_SHARE, "QQ_SDK_Share" + " : " + "发送给好友");
        return myQQShare;
    }

    private static void normalShare(Context context, ResolveInfo resolveInfo, String savePath, String packageName, String title) {
        exeNormalShare(context, "图片分享", resolveInfo, savePath, ShareUtil.Type.Image);
        US.putSaveAndShareEvent(US.SAVE_AND_SHARE_SHARE, packageName + " : " + title);
        Log.d(US.SAVE_AND_SHARE_SHARE, "normal share" + " " + title);
    }

    /**
     * 只传路径就行，支持jpg、jpeg和gif
     * 但是不支持gif，应该是微信sdk的不给力，通过Android标准的方式分享gif是可以的
     */
    private static void shareByWXSDK(Context context, ResolveInfo resolveInfo, String savePath,
                                     String packageName, String title) {

        //初始化 WXImageObject 和 WXMediaMessage 对象
        WXImageObject imgObj = new WXImageObject();
        imgObj.imagePath = savePath;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        //设置缩略图 // 不要缩略图也行
        //        final int THUMB_SIZE = 150;
        //        Bitmap thumbBmp = Bitmap.createScaledBitmap(, THUMB_SIZE, THUMB_SIZE, true);
        //        msg.thumbData = BitmapUtil.bmpToByteArray(thumbBmp, Bitmap.CompressFormat.PNG, true);

        //构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;
        req.userOpenId = AppConfig.ID_IN_WEIXIN;
        //调用api接口，发送数据到微信
        boolean isShareSuccess = false;
        if (AllData.getWXAPI() != null) { //
            try {
                isShareSuccess = AllData.getWXAPI().sendReq(req);
            } catch (Exception e) {
                Log.e("share", "shareByWXSDK error: " + e.getMessage());
            }
        }
        if (!isShareSuccess) { // 分享失败，使用正常方式
            US.putSaveAndShareEvent("share by WXSDK failed");
            normalShare(context, resolveInfo, savePath, packageName, title);
        } else {
            US.putSaveAndShareEvent(US.SAVE_AND_SHARE_SHARE, "WeiXin_SDK_Share" + " : " + "发送给朋友");
            Log.d(US.SAVE_AND_SHARE_SHARE, "WeiXin_SDK_Share" + " : " + "发送给朋友");
        }
    }

    /**
     * 微信分享的
     */
    private static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 分享类型
     *
     * @author YOLANDA
     * @Project SmartControl
     * @Class ShareUtil.java
     * @Time 2015年3月4日 上午10:21:16
     */
    public enum Type {
        /**
         * 图片
         **/
        Image,
        /**
         * 文字
         **/
        Text
    }
}

