package com.mandi.intelimeditor.ptu.rendpic;

import com.mandi.intelimeditor.common.util.geoutil.MPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成锯齿路径用到的数据
 */
public class SawtoothPathData {
    public List<MPoint> testPoints = new ArrayList<>();
    public List<MPoint> stPoints = new ArrayList<>();
    public List<MPoint> points1 =  new ArrayList<>(), points2 =  new ArrayList<>();

    public SawtoothPathData() {

    }

}
