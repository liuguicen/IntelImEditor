package com.mandi.intelimeditor.ptu.repealRedo;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Administrator on 2016/7/27.
 */
public class TietuStepData extends StepData implements Iterable {

    private ArrayList<OneTietu> tietuList = new ArrayList<>();

    public TietuStepData(int mode) {
        super(mode);
    }

    public void addOneTietu(OneTietu oneTietu) {
        tietuList.add(oneTietu);
    }

    @Override
    public Iterator<OneTietu> iterator() {
        return tietuList.iterator();
    }

    public static class OneTietu {
        /**
         * rect代表view有效区域在底图上的位置的rect，相对于原始图片的左上角上下左右边的距离
         */
        private RectF boundRectInPic = new RectF();
        private float rotateAngle;


        /**
         * 路径或者Id,只能是其中之一
         */
        private String picPath;
        private int picId;

        public OneTietu(String picPath, RectF boundRectInPic, float rotateAngle) {
            this.picPath = picPath;
            this.boundRectInPic = boundRectInPic;
            this.rotateAngle = rotateAngle;
        }

        public OneTietu(int picId, RectF boundRectInPic, float rotateAngle) {
            this.picId = picId;
            this.boundRectInPic = boundRectInPic;
            this.rotateAngle = rotateAngle;
        }

        public int getPicId() {
            return picId;
        }

        public String getPicPath() {
            return picPath;
        }

        public void setPicPath(String picPath) {
            this.picPath = picPath;
        }

        public float getRotateAngle() {
            return rotateAngle;
        }

        public RectF getBoundRectInPic() {
            return boundRectInPic;
        }

    }
}
