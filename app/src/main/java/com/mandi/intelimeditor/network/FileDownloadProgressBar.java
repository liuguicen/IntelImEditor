package com.mandi.intelimeditor.network;//package a.baozouptu.network;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.content.Context;
//import android.widget.RemoteViews;
//
//import a.baozouptu.R;
//import a.baozouptu.common.util.Util;
//
///**
// * Created by Administrator on 2016/11/21 0021.
// * 暂未使用
// */
//
//public class FileDownloadProgressBar {
//    private Context context;
//
//    public FileDownloadProgressBar(Context context) {
//        this.context = context;
//    }
//
//    public void sendNotify() {
//        // 第一步：获取NotificationManager
//        NotificationManager nm = (NotificationManager)
//                context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // 第二步：定义Notification
//        Notification notification = new Notification.Builder(context)
//                .setSmallIcon(R.mipmap.icon)
//                .build();
//        // 当用户下来通知栏时候看到的就是RemoteViews中自定义的Notification布局
//        RemoteViews contentView = new RemoteViews(context.getPackageName(),
//                R.layout.notifytion_file_download);
//        contentView.setTextViewText(R.id.notify_make_name,
//                context.getResources().getString(R.string.make_expression));
//
//        contentView.setTextViewText(R.id.notify_latest_name,
//                context.getResources().getString(R.string.latest_pic));
//        contentView.setInt(R.id.notify_file_download_progress, "setProgress", 10);
//        notification.contentView = contentView;
//        notification.flags = Notification.FLAG_AUTO_CANCEL;
//        //第三步：启动通知栏，第一个参数是一个通知的唯一标识
//        nm.notify(0, notification);
//        PTuLog.d("FileDownloadProgressBar", "发送通知完成");
//    }
//}
