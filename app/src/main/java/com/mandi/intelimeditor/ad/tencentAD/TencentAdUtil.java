package com.mandi.intelimeditor.ad.tencentAD;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.qq.e.ads.cfg.VideoOption;

public class TencentAdUtil {

    public static VideoOption getVideoOption() {
        VideoOption videoOption = null;
        VideoOption.Builder builder = new VideoOption.Builder();

        builder.setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS);
        builder.setAutoPlayMuted(true);
        builder.setDetailPageMuted(false);

        videoOption = builder.build();
        return videoOption;
    }

    /**	设置本次拉取的视频广告，从用户角度看到的视频播放策略；可选项包括自VideoOption.VideoPlayPolicy.AUTO(在用户看来，
     * 视频广告是自动播放的)和VideoOption.VideoPlayPolicy.MANUAL(在用户看来，视频广告是手动播放的)；
     * 如果广告位支持视频，强烈建议调用此接口设置视频广告的播放策略，有助于提高eCPM值；如果广告位不支持视频，忽略本接口
     * @param autoPlayPolicy {@link VideoOption.AutoPlayPolicy}
     */
    public static int getVideoPlayPolicy(int autoPlayPolicy, Context context){
        if(autoPlayPolicy == VideoOption.AutoPlayPolicy.ALWAYS){
            return VideoOption.VideoPlayPolicy.AUTO;
        }else if(autoPlayPolicy == VideoOption.AutoPlayPolicy.WIFI){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifiNetworkInfo != null && wifiNetworkInfo.isConnected() ? VideoOption.VideoPlayPolicy.AUTO
                    : VideoOption.VideoPlayPolicy.MANUAL;
        }else if(autoPlayPolicy == VideoOption.AutoPlayPolicy.NEVER){
            return VideoOption.VideoPlayPolicy.MANUAL;
        }
        return VideoOption.VideoPlayPolicy.UNKNOWN;
    }
}
