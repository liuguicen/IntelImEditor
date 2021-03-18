package com.mandi.intelimeditor.ptu.changeFace;

import java.util.Arrays;
import java.util.List;

public class ChangeFaceUtil {
    /**
     * 支持换脸的tag列表，这些tag也就代表了支持换年的图的集合
     */
    public static List<String> changeFaceTagList = Arrays.asList("熊猫头", "蘑菇头", "换脸");

    public static String  getChangeFaceTagsString() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < changeFaceTagList.size(); i++) {
            String tag = changeFaceTagList.get(i);
            content.append(tag);
            if (i < changeFaceTagList.size() - 1)
                content.append('-');
        }
        return content.toString();
    }
}
