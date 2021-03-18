package com.mandi.intelimeditor.ptu.common;

/**
 * 进入P图二级功能的特殊控制信息，这个是进入贴图的，比如抠图到贴图，需要显示选择底图的
 */
public class TietuController extends SecondFuncController {
    public boolean isChangeFace = false;
    public boolean needChooseBase = true;
    public String tietuUrl;
    public boolean needSaveTietu = false;
    public boolean needAdjustLevel = true;
    public boolean isEraseFace = false;

    public TietuController() {
    }
}
