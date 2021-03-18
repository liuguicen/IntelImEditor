package com.mandi.intelimeditor.common.dataAndLogic;

import android.graphics.Bitmap;

import androidx.collection.LruCache;

import com.mandi.intelimeditor.common.util.BitmapUtil;

import org.jetbrains.annotations.NotNull;

/**
 * 主要用来存放PTU过程中临时BitMap的数据池
 */
public class BitmapPool {
    /**
     * 使用LRU算法，用key-value形式查找对象；
     */
    public static LruCache<String, Bitmap> imageCache;

    /**
     *
     */
    public BitmapPool() {
        // 另一种方法
        int maxMemory = (int) (Runtime.getRuntime().maxMemory()  / 1024);
        imageCache = new LruCache<String, Bitmap>(maxMemory / 16) {
            @Override
            public int sizeOf(@NotNull String key, @NotNull Bitmap value) {
                return (int) (BitmapUtil.getSize(value) / 1024);
            }
        };
    }

    /**
     * 同步的直接获取Bitmap, 全尺寸的
     */
    public Bitmap get(String url) {
        Bitmap bitmap = imageCache.get(url);
        // 先移除失效的
        if (bitmap != null && bitmap.isRecycled()) {
            removeBitmap(url);
            bitmap = null;
        }
        return bitmap;
    }

    public void putBitmap(String url, Bitmap bm) {
        if (bm != null) {
            imageCache.put(url, bm);
        }
    }

    public Bitmap removeBitmap(String url) {
        return imageCache.remove(url);
    }

    /**
     * 同一个Bitmap路径更改时调用
     * 1 换成2
     * @param url1
     * @param url2
     */
    public void replaceUrl(String url1, String url2) {
        Bitmap remove = imageCache.remove(url1);
        if (remove != null) {
            imageCache.put(url2, remove);
        }
    }

    /**
     * 清除调用的内存
     */
    public void evitAll() {
        imageCache.evictAll();
    }

}
