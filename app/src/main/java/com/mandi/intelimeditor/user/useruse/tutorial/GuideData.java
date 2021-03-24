package com.mandi.intelimeditor.user.useruse.tutorial;

import com.mandi.intelimeditor.ptu.PtuUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuideData {
    /**
     * 远程引导数据
     * 1、数据默认使用本地引导数据（如果引导页数据很多，可以考虑在线获取）
     * 2、引导数据包括 图片在线地址、图片本地地址、功能名、功能ID（暂无用处，后续可以对不同的功能点进行编序）
     */
    public static List<Tutorial> allGuideUseData = new ArrayList<>();

    /**
     * 根据功能名获取指定的功能引导数据。每个功能对应一个gif或者多个图片
     * 使用关键字匹配的形式得到使用教程Item
     */
    public static List<Tutorial> getGuideUseDataByType(List<String> keyWordList) {
        List<Tutorial> result = new ArrayList<>();
        for (String keyword : keyWordList) {
            for (int i = 0; i < allGuideUseData.size(); i++) {
                if (allGuideUseData.get(i).getKeyword().contains(keyword) ||
                        allGuideUseData.get(i).getTitle().contains(keyword)) {
                    result.add(allGuideUseData.get(i));
                }
            }
        }
        return result;
    }

    @NotNull
    public static List<String> getKeyword(int editMode, int childFunction, boolean isGif) {
        List<String> keywordList = new ArrayList<>();
        switch (editMode) {
            case PtuUtil.EDIT_MAIN:
                if (isGif)
                    keywordList.add("GIF");
                else
                    keywordList.add("P图");
                break;
            case PtuUtil.EDIT_CUT:
                keywordList.add("编辑");
                break;
            case PtuUtil.EDIT_TEXT:
                keywordList.add("文字");
                break;
            case PtuUtil.EDIT_TIETU:
                keywordList.add("贴图");
                break;
            case PtuUtil.EDIT_DRAW:
                keywordList.add("绘图");
                break;
            case PtuUtil.EDIT_DIG:
                keywordList.add("抠脸");
                keywordList.add("换脸总述");
                break;
            case PtuUtil.EDIT_REND:
                keywordList.add("撕图");
                break;
            case PtuUtil.EDIT_DEFORMATION:
                keywordList.add("变形");
                break;
        }
        return keywordList;
    }
}
