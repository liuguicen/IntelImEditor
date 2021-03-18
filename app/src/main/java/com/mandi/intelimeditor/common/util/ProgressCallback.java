package com.mandi.intelimeditor.common.util;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/08/17
 *      version : 1.0
 * <pre>
 */
public interface ProgressCallback {
    void setMax(int max);
    void onProgress(int progress);
}
