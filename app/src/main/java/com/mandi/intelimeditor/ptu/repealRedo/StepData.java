package com.mandi.intelimeditor.ptu.repealRedo;

import com.mandi.intelimeditor.ptu.PtuUtil;

/**
 * Created by Administrator on 2016/7/8.
 * 从数据的角度看，这就是用户的P图行为的数据化，
 * 这个数据就代表了用户的一次P图行为，然后利用这个数据进行撤销重做等
 */
public class StepData {
    /**
     * 来自PtuActivity中的几个常量
     */
    public int EDIT_MODE;

    public float rotateAngle;
    public volatile String picPath;
    // 指明这一步操作之后的图片是否包含透明度
    public boolean hasTransparency = false;

    /**
     * @param editMode {@link PtuUtil}
     */
    public StepData(int editMode) {
        this.EDIT_MODE = editMode;
    }
}
