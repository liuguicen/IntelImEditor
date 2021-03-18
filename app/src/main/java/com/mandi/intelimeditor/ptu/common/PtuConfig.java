package com.mandi.intelimeditor.ptu.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

/**
 * Created by LiuGuicen on 2016/12/26 0026.
 * P图的用户设置选项
 */

public class PtuConfig {
    private final SharedPreferences sp;

    public PtuConfig() {
        sp = AllData.appContext.getSharedPreferences("ptu_config", Context.MODE_PRIVATE);
    }

    public boolean hasReadTextRubber() {
        return sp.getBoolean("text_rubber", false);
    }

    public void writeConfig_TextRubber(boolean isRead) {
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putBoolean("text_rubber", isRead);
        spEditor.apply();
    }
}
