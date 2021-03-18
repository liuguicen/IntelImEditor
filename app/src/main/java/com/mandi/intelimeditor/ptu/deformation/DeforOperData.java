package com.mandi.intelimeditor.ptu.deformation;

import com.mandi.intelimeditor.common.util.geoutil.MPoint;

public class DeforOperData {
    MPoint start,end;
    float r;

    public DeforOperData(MPoint start, MPoint end, float r) {
        this.start = start;
        this.end = end;
        this.r = r;
    }
}
