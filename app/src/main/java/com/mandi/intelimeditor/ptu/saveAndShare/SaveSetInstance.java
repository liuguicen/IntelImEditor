package com.mandi.intelimeditor.ptu.saveAndShare;

import android.content.Context;

/**
 * Created by Administrator on 2016/11/22 0022.
 * 通过代理的非静态单例模式
 */

public class SaveSetInstance {
    private SaveShareManager saveShareManager;
    public boolean hasShared;

    public SaveSetInstance() {
        hasShared = false;
    }

    public synchronized SaveShareManager getSaveShare_DialogManager_Instance(Context context) {
        if (saveShareManager == null)
            saveShareManager = new SaveShareManager(context);
        return saveShareManager;
    }

}
