package com.mandi.intelimeditor.ptu.imageProcessing;

public class MathUtil {
    public static boolean isInRange(float x, float a, float b) {
        return a <= x && x <= b || b <= x && x <= a;
    }
}
