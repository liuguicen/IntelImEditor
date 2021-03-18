package com.mandi.intelimeditor.common.dataAndLogic;

/**
 * Created by Administrator on 2016/11/15 0015.
 * App的内存使用很重要，自然有一个内存管理器
 * 注意应用内存情况分三个：
 * 最大可获得内存
 * 已获得的内存
 * 已获得的内存总剩余可用的内存，不要搞混了
 */

public class MemoryManager {
    /**
     * @return 获取剩余可用字节数
     * 注意，和另外两个区分
     * //应用程序已获得内存
     * long totalMemory = ((int) Runtime.getRuntime().totalMemory())/1024/1024;
     * //应用程序已获得内存中未使用内存
     * long freeMemory = ((int) Runtime.getRuntime().freeMemory())/1024/1024;
     */
    public static long getUsableMemoryByte() {
        return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
    }
}
