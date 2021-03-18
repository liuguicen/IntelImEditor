package com.mandi.intelimeditor.common;

/**
 * Created by Administrator on 2016/11/25 0025.
 */
// 不用了，目前使用友盟的
//public class CrashHandler implements Thread.UncaughtExceptionHandler {
//    private Thread.UncaughtExceptionHandler defaultHandler;
//
//    public CrashHandler() {
//        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
//    }
//
//    @Override
//    public void uncaughtException(Thread thread, Throwable ex) {
//        Log.e("-----------------", "uncaughtException: 发生了Crash");
//        new CrashLog().commit(thread, ex);
//        if (defaultHandler != null) {
//            defaultHandler.uncaughtException(thread, ex);
//        } else {
//            Process.killProcess(Process.myPid());
//        }
//    }
//}
