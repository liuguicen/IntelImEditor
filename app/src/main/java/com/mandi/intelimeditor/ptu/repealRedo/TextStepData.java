package com.mandi.intelimeditor.ptu.repealRedo;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/27.
 * 内部用list存下了多个的stepDate；
 */
public class TextStepData extends StepData {
    ArrayList<Pair<Path, Paint>> pathPaintList = null;
    /**
     * rect代表用来PTu的view有效区域在底图上的位置的rect，相对于原始图片的左上角上下左右边的距离
     */
    public RectF boundRectInPic = new RectF();

    public void setRubberDate(ArrayList<Pair<Path, Paint>> pathPaintList) {
        this.pathPaintList = pathPaintList;
    }

    public void addRubberDate(ArrayList<Pair<Path, Paint>> pathPaintList) {
        if (pathPaintList == null) {
            pathPaintList = new ArrayList<>();
        }
        this.pathPaintList.addAll(pathPaintList);
    }

    public ArrayList<Pair<Path, Paint>> getRubberData() {
        return pathPaintList;
    }

    public TextStepData(int mode) {
        super(mode);
    }


}
