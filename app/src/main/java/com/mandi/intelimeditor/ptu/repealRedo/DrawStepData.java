package com.mandi.intelimeditor.ptu.repealRedo;

import android.graphics.Path;
import android.util.Pair;

import com.mandi.intelimeditor.ptu.draw.DrawView;
import com.mandi.intelimeditor.ptu.draw.MPaint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/30.
 */
public class DrawStepData extends StepData {
    public DrawStepData(int editMode) {
        super(editMode);
    }


    List<DrawView.DrawPath> savePath = new ArrayList<>();
    public List<Pair<Path, MPaint>> eraseData;

    public List<DrawView.DrawPath> getSavePath() {
        return savePath;
    }

    public void setSavePath(List<DrawView.DrawPath> savePath) {
        this.savePath = savePath;
    }

    public void setEraseData(List<Pair<Path, MPaint>> eraseData) {
        this.eraseData = eraseData;
    }
}
