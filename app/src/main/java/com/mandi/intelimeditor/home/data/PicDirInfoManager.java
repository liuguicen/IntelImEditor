package com.mandi.intelimeditor.home.data;//package com.mandi.intelimeditor.home.data;
//
//import android.text.SpannableString;
//import android.text.Spanned;
//import android.text.style.ForegroundColorSpan;
//import android.text.style.RelativeSizeSpan;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import com.mandi.intelimeditor.R;
//import com.mandi.intelimeditor.common.util.FileTool;
//import com.mandi.intelimeditor.common.util.Util;
//
///**
// * Created by LiuGuicen on 2017/1/18 0018.
// */
//
//public class PicDirInfoManager {
//    /**
//     * 文件的信息
//     */
//    private List<PicDirInfo> picDirInfos;
//
//    public PicDirInfoManager() {
//        picDirInfos = new ArrayList<>();
//        picDirInfos.add(new PicDirInfo("ddd", formatDescribeInfo("0", 0), "sdf"));
//    }
//
//    public void updateUsuInfo(List<String> usuPaths) {
//        // 处理文件信息,将要显示的文件信息获取出来
//        //常用图的信息，
//        String representPath = usuPaths.size() == 0 ? null : usuPaths.get(1);
//        PicDirInfo picDirInfo = new PicDirInfo("aaaaa",
//                formatDescribeInfo("常用图片", usuPaths.size() - 3),
//                representPath);
//        picDirInfos.set(0, picDirInfo);
//    }
//
//    /**
//     * 短视频文件夹，显示所有的短视频
//     */
//    public void updateShortVideoInfo(Set<String> shortVideoSet) {
//        if (shortVideoSet.size() > 0) {
//            String representPath = shortVideoSet.iterator().next();
//            PicDirInfo picDirInfo = new PicDirInfo(MediaInfoScanner.SHORT_VIDEO_TAG,
//                    formatDescribeInfo("短视频(制作GIF）", shortVideoSet.size(), "条"),
//                    representPath);
//            if (picDirInfos.size() >= 2) {
//                picDirInfos.set(1, picDirInfo);
//            } else {
//                picDirInfos.add(picDirInfo);
//            }
//        } else { // 没有短视频，移除
//            if (picDirInfos.size() >= 2 &&
//                    MediaInfoScanner.SHORT_VIDEO_TAG.equals(picDirInfos.get(1).getDirPath())) {
//                picDirInfos.remove(1);
//            }
//        }
//    }
//
//    /**
//     * 多处使用，便于统一格式，免得更改时到处改
//     *
//     * @param name   文件名称
//     * @param number 文件数量
//     */
//    private SpannableString formatDescribeInfo(String name, int number) {
//        return formatDescribeInfo(name, number, null);
//    }
//
//    /**
//     * 多处使用，便于统一格式，免得更改时到处改
//     *
//     * @param name   文件名称
//     * @param number 文件数量
//     */
//    private SpannableString formatDescribeInfo(String name, int number, @Nullable String unit) {
//        if (unit == null) {
//            unit = " 张";
//        }
//        String infos = " " + name + "\n" + " " + number + unit;
//        SpannableString sps = new SpannableString(infos);
//        int s = infos.indexOf("\n") + 1, t = infos.length();
//        sps.setSpan(new RelativeSizeSpan(0.8f), s, t, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        sps.setSpan(new ForegroundColorSpan(Util.getColor(R.color.text_light_black)), s, t, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        return sps;
//    }
//
//    /**
//     * 更新图片文件的信息，在drawer中的，包括文件目录信息，文件中图片数目，最新图片的路径
//     *
//     * @param picFileNumberMap    文件中图片张数信息
//     * @param PicFileRepresentMap 文件代表图片信息
//     */
//    public void updateAllFileInfo(Map<String, Integer> picFileNumberMap, Map<String, String> PicFileRepresentMap) {
//        for (Map.Entry<String, Integer> entry : picFileNumberMap.entrySet()) {
//            String path = entry.getKey();
//            String representPath = PicFileRepresentMap.get(path);
//            String name = path.substring(path.lastIndexOf("/") + 1, path.length());
//            SpannableString numInfo = formatDescribeInfo(name, entry.getValue());
//            picDirInfos.add(new PicDirInfo(path, numInfo, representPath));
//        }
//    }
//
//    public void clear() {
//        picDirInfos.clear();
//        picDirInfos.add(new PicDirInfo("asdas", formatDescribeInfo("0", 0), "sdfsd"));
//    }
//
//    public String getDirPath(int position) {
//        if (picDirInfos == null) return "";
//        if (position >= picDirInfos.size()) {
//            position = picDirInfos.size() - 1;
//        } else if (position < 0) {
//            position = 0;
//        }
//        return picDirInfos.get(position).getDirPath();
//    }
//
//    /**
//     * 根据目录路径找到其的位置
//     *
//     * @param dirPath 目录路径
//     */
//    private int findDirPathId(String dirPath) {
//        for (int i = 0; i < picDirInfos.size(); i++) {
//            if (picDirInfos.get(i).getDirPath().equals(dirPath))
//                return i;
//        }
//        return -1;
//    }
//
//    /**
//     * 新增一张图片时改变相应的目录信息
//     *
//     * @return 针对添加图片时，图片目录尚不存在的情况，刷新图片列表
//     */
//    public boolean onAddNewPic(String newPicPath) {
//        String parentPath = FileTool.getParentPath(newPicPath);
//        int id = findDirPathId(parentPath);
//        if (id == -1) {//如果没找到,尚未加入此目录，需要先添加
//            addOneDirInfo(newPicPath, parentPath);
//            return true;
//        }
//        String info = picDirInfos.get(id).getPicNumInfo().toString();
//        int number = Integer.valueOf(info.substring(info.indexOf('\n') + 2, Util.lastDigit(info) + 1));
//        number++;
//        SpannableString new_info = formatDescribeInfo(info.substring(0, info.indexOf('\n')), number);
//        picDirInfos.set(id, new PicDirInfo(parentPath, new_info, newPicPath));
//        return false;
//    }
//
//    private void addOneDirInfo(String picPath, String parentPath) {
//        picDirInfos.add(new PicDirInfo(parentPath, formatDescribeInfo(FileTool.getFileNameInPath(parentPath), 1), picPath));
//    }
//
//    /**
//     * 删除图片文件，并更新目录列表信息
//     * <p>更新文件信息，文件是否还存在，图片张数，最新图片，描述信息的字符串
//     * <p>注意发送删除通知
//     *
//     * @return 删除成功的列表
//     */
//    @NotNull
//    public List<String> deletePicList(List<String> pathList) {
//        List<String> successList = new ArrayList<>();
//        if (pathList.size() < 1) return successList;
//
//        String dirPath = new File(pathList.get(0)).getParent(); // 目前只删除同一个目录下的
//
//        for (String picPath : pathList) {
//            if (FileTool.deletePicFile(picPath)) {
//                successList.add(picPath);
//            }
//        }
//
//        //更新文件目录信息 // 低效方法，有时间改进
//        int id = findDirPathId(dirPath);//图片所在目录的位置id
//        if (id != -1) {
//            List<String> paths = new ArrayList<>();
//            FileTool.getOrderedPicListInFile(dirPath, paths);
//            if (paths.size() == 0) {
//                picDirInfos.remove(id);//如果此目录下面已经没有图片
//            } else {//还有图片则更新信息
//                String representPath = paths.get(0);
//                String name = dirPath.substring(dirPath.lastIndexOf("/") + 1);
//                SpannableString info = formatDescribeInfo(name, paths.size());
//                picDirInfos.set(id, new PicDirInfo(dirPath, info, representPath));
//            }
//        }
//        return successList;
//    }
//
//    public List<PicDirInfo> getPicDirInfos() {
//        return picDirInfos;
//    }
//}
