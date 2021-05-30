package com.mandi.intelimeditor;

import android.graphics.Rect;

import com.mandi.intelimeditor.ptu.transfer.StyleTransferTensorflow;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.mandi.intelimeditor.ptu.transfer.StyleTransferTensorflow.getNodePosFill;

public class AUnitTest {
    @Test
    public void test() {
        // testTransferPathPosGenerator();
        testRectGenerate(400, 800);
        testRectGenerate(1024, 1920);
        testRectGenerate(100, 500);
        testRectGenerate(385, 800);
        testRectGenerate(700, 800);
    }

    private void testRectGenerate(int srcW, int srcH) {
        ArrayList<Integer> wPos = getNodePosFill(srcW);
        ArrayList<Integer> hPos = getNodePosFill(srcH);
        for (int i = 0; i < wPos.size() - 1; i += 2) {
            for (int j = 0; j < hPos.size() - 1; j += 2) {
                Rect expendPatchInContent = new Rect(wPos.get(i), hPos.get(j), wPos.get(i + 1), hPos.get(j + 1));
                System.out.println(expendPatchInContent);
            }
        }
    }

    @Test
    public void testTransferPathPosGenerator() {
        System.out.println(list2String(getNodePosFill(100)));
        System.out.println(list2String(getNodePosFill(400)));
        System.out.println(list2String(getNodePosFill(600)));
        System.out.println(list2String(getNodePosFill(800)));
        System.out.println(list2String(getNodePosFill(1024)));
        System.out.println(list2String(getNodePosFill(1400)));
        System.out.println(list2String(getNodePosFill(2400)));
    }

    private String list2String(ArrayList<Integer> nodePos) {
        StringBuilder sb = new StringBuilder();
        for (final Integer nodePo : nodePos) {
            sb.append(nodePo.toString() + " , ");
        }
        return sb.toString();
    }
}
