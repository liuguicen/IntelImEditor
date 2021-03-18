package com.mandi.intelimeditor.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;

public class NetWorkState {
    public static int NO_NET = -1;
    public static int MOBILE = 0;
    public static int WIFI = 1;
    public static int OTHERS = 2;



    public static int detectNetworkType() {
        return detectNetworkType(IntelImEditApplication.appContext);
    }

    /**
     * 检测网络状态，
     *
     * @return <p>-1表示没有联网
     * <p>0表示GPRS流量
     * <p>1表示WiFi
     * <p>2表示其它网络
     */
    public static int detectNetworkType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) return NO_NET;
        else if (activeNetwork.getType() == ConnectivityManager
                .TYPE_WIFI) {
            return WIFI;
        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) return MOBILE;
        else return OTHERS;
    }
}
