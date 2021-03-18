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
     * 本地引导数据
     */
//    public static List<Tutorial> allLocalGuideUseData = new ArrayList<>();

    /**
     * 根据功能名获取指定的功能引导数据。每个功能对应一个gif或者多个图片
     */
//    public static List<Tutorial> getGuideUseDataByTitle() {
//        allLocalGuideUseData.clear();
//        allLocalGuideUseData.add(new Tutorial("撕图", "撕图-P图", "双指按住图片，往外拉即可撕开图片哦"));
//        allLocalGuideUseData.add(new Tutorial("图片列表-长按选择", "选图", "图片列表-长按选择\t长按图片多选，即可制作GIF，设为常用等哦"));
//        allLocalGuideUseData.add(new Tutorial("GIF动图制作", "贴图-GIF", "下方是帧列表，双击选择单帧，长按选择多帧，可以只对选中帧P图哟"));
//        allLocalGuideUseData.add(new Tutorial("文字-橡皮擦", "文字", "滑动即可擦除背景,可在左边选择颜色和粗细哟"));
//        allLocalGuideUseData.add(new Tutorial("贴图橡皮", "贴图", "你还可以使用橡皮擦除部分贴图哦"));
//        allLocalGuideUseData.add(new Tutorial("编辑", "编辑", "按住编辑框四周调整尺寸，按住中间即可移动"));
//        allLocalGuideUseData.add(new Tutorial("抠脸-模糊半径", "扣脸", "调节模糊半径，可使抠图后贴图效果更逼真哦"));
//        allLocalGuideUseData.add(new Tutorial("缩放", "贴图", "选图后，双指按住图片，即可缩放和旋转哦"));
//        allLocalGuideUseData.add(new Tutorial("抠脸", "扣脸-P图", "绕着人脸画圈，即可实现智能抠脸哦"));
//        allLocalGuideUseData.add(new Tutorial("文字", "选图", "选择本地图片或者搞笑模板进行P图吧"));
//        allLocalGuideUseData.add(new Tutorial("选择图片", "选图", "点击右下角图标可选择文件夹内图片哦"));
//        allLocalGuideUseData.add(new Tutorial("图片列表-添加收藏", "选图", "图片选择页长按图片即可收藏到我的贴图哦"));
//        return allLocalGuideUseData;
//    }

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
                if (PtuUtil.CHILD_FUNCTION_CHANGE_FACE == childFunction) {
                    keywordList.add("换脸");
                }
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
