package com.mandi.intelimeditor.common.util;

import org.jetbrains.annotations.Nullable;

public class BmobUtil {
    /**
     * 又拍云提供的服务，可以将其它类型的文件转换成webp格式，减少体积
     * 只要转换url即可
     * 只能用于列表展示过程中使用，原因1是webp格式模式P图之后保存等有问题，2是web图片分享时，其它应用可能不支持，
     * 另外，所有列表展示的地方都应该转换url，否则会多下载一次，以及增加缓存
     */
    @Nullable
    public static String getUrlOfSmallerSize(@Nullable String url) {
        if (url == null) return null;
        if (FileTool.urlType(url) != FileTool.UrlType.URL) return url;
        // Android 4.0 以上提供不包含透明度的静态webp的原生支持，
        // 初步测试，jpg的压缩率35%左右，主要是模板，只要用户对模板的使用率低于35%，那个流量就得到了节省
        // 这个应该是可以节省的，目前（2020.4)假设目前平均使用率15%，节省20%流量，乘以jpg模板的系数 15% = 3% 流量费
        if (url.endsWith(".jpg") || url.endsWith(".jpeg")) {
            url += "!/format/webp";
        }
        // gif是流量的大头，不过目前难以处理
        // png转换貌似没有太大用，用户贴图使用比例很大，当前来看png转换成无损webp只减少了20%左右，也就是流量消耗是原来的80%
        // 用户贴图使用率超过20%时， 流量就超过原来的了
//        if (url.endsWith(".png")) {
//            url += "!/format/webp/lossless/true";
//        }
        // Android 4.2.1 + 支持带有透明度的webp，webp体积会减少不少，使用又拍云的方法转换成webp
        return url;
    }
}
