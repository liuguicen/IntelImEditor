package com.mandi.intelimeditor.common.appInfo;

import cn.bmob.v3.BmobObject;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * <pre>
 *      author : liuguicen
 *      time : 2019/07/24
 *      version : 1.0
 *      表，类名，属性名不要轻易改
 * <pre>
 */
public class TheUser extends BmobObject {

    private String userId;
    private long vipExpireTime = 0;

    public void registerOnServer(Observer<String> observer) {
        saveObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getVipExpireTime() {
        return vipExpireTime;
    }

    public void setVipExpireTime(long vipExpireTime) {
        this.vipExpireTime = vipExpireTime;
    }

    public interface UpdateVipExpireListener {
        void success();

        void failed(String msg);
    }
}
