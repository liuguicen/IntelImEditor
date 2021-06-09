package com.mandi.intelimeditor.home.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.util.Pair;

import com.mandi.intelimeditor.common.dataAndLogic.AllData;

import com.mandi.intelimeditor.common.util.LogUtil;
import com.mandi.intelimeditor.home.localPictuture.LocalPicFragment;
import com.mandi.intelimeditor.ptu.tietu.onlineTietu.PicResource;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by LiuGuicen on 2017/1/18 0018.
 * 图片（主要）和小视频信息的扫描器，处理各种sd卡的媒体文件的扫描的问题
 * 里面还包含了扫描的到的数据
 * 重要注释，看{@link MediaInfoScanner#scanAndUpdatePicInfo()}
 */

public class MediaInfoScanner {
    public static final String SHORT_VIDEO_TAG = "short_video";
    private long lastScanTime = 0;
    //    按时间排序的时间路径对应列表
    private List<Pair<Long, String>> sortedPicPathsByTime;
    //    文件中图片张数信息
    private Map<String, Integer> picFileNumberMap;
    //    文件代表图片信息
    private Map<String, String> picFileRepresentMap;
    //    短视频的路径、时长(毫秒），
    private Map<String, Integer> shortVideoMap = new HashMap<>();
    private static final int PICTURE = 1;
    private static final int SHORT_VIDEO = 2;
    static final String TAG = "MediaInfoScanner";

    /**
     * Created by LiuGuicen on 2017/1/17 0017.
     */

    public enum PicUpdateType {
        NO_CHANGE,
        /**
         * 改变所有图片
         */
        CHANGE_ALL_PIC,
        CHANGE_ALL_FILE,
        CHANGE_PIC,
        CHANGE_FILE,
        CHANGE_RECENT
    }

    private int lastTotalNumber = 0;

    private static final class InstanceHolder {
        private static MediaInfoScanner instance = new MediaInfoScanner();
    }

    public static MediaInfoScanner getInstance() {
        return InstanceHolder.instance;
    }

    private MediaInfoScanner() {
    }

    /**
     * 查询所有的图片,先清空以前的list里面的数据
     * 检测是否有更新信息，有就更新信息，
     * <p>这个方法比较有难度，要能随时检测图片并更新显示，目前常出问题
     * 里面log不要删了，便于查看问题
     *
     *
     * <p>注意发生更新时文件信息会被全部更新掉
     *
     * <P>注意!!有些情况下系统的media也扫描不到图片，即保存图片的时候没有发送图片更新通知，比如从电脑直接复制图片
     * <p>那需要监听存储器，或者扫描整个存储器，这个目前即使专业的APP不好做，没有做
     * <p>遇到类似问题不要重复找bug，找半天发现没问题
     * <p>刚保存的图片，虽然发送了媒体更新，也需要1、2s的时间才能查到,手动添加，{@link LocalPicFragment#addNewPath(String)}
     *
     * @return 返回是否需要更新
     */
    public boolean scanAndUpdatePicInfo() {
        //首先查出所有信息
        // 里面存放pair，第一个是时间，第二个是路径
        LogUtil.e("扫描了图片，耗时操作，log保留");
        sortedPicPathsByTime = new ArrayList<>();
        picFileNumberMap = new TreeMap<>();
        picFileRepresentMap = new TreeMap<>();
        queryPicInfoInSD(sortedPicPathsByTime,
                picFileNumberMap, picFileRepresentMap,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        queryPicInfoInSD(sortedPicPathsByTime,
                picFileNumberMap, picFileRepresentMap,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        // 短视频
        if (AllData.globalSettings.isShowShortVideo()) {
            shortVideoMap.clear();
            queryVideoInfo(sortedPicPathsByTime,
                    picFileNumberMap, picFileRepresentMap,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            queryVideoInfo(sortedPicPathsByTime,
                    picFileNumberMap, picFileRepresentMap,
                    MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        }
        LogUtil.d("scanAndUpdatePicInfo: 获取到短视频数量= " + shortVideoMap.size());
        long scanTime = System.currentTimeMillis();
        Collections.sort(sortedPicPathsByTime, (o1, o2) -> o2.first.compareTo(o1.first));
        if (sortedPicPathsByTime.size() == 0) {
            return false;
        }
        if (sortedPicPathsByTime.get(0).first < lastScanTime &&
                lastTotalNumber == sortedPicPathsByTime.size()) {  // 没有更新
            lastScanTime = scanTime;
            LogUtil.d("已扫描完成，没有检测到新图片");
            return false;
        }
        lastScanTime = scanTime;
        lastTotalNumber = sortedPicPathsByTime.size();
        return true;
    }

    public List<PicResource> convertRecentPath2PicResList() {
        Log.d(TAG, "convertRecentPath2PicResList: 耗时操作，少调用，log保留");
        ArrayList<PicResource> picResources = new ArrayList<>();
        for (Pair<Long, String> timePath : sortedPicPathsByTime) {
            picResources.add(PicResource.path2PicResource(timePath.second));
        }
        return picResources;
    }

    /**
     * 更新图片文件的信息，在drawer中的，包括文件目录信息，文件中图片数目，最新图片的路径
     */
    public PicUpdateType updateAllFileInfo(PicDirInfoManager picDirInfoManager, UsuPathManger usuPathManger) {
        //处理文件信息
        picDirInfoManager.clear();//清理
        picDirInfoManager.updateUsuInfo(usuPathManger.getUsuPaths());//给常用图片添加信息
        picDirInfoManager.updateAllFileInfo(picFileNumberMap, picFileRepresentMap);//添加其他文件的信息
        picDirInfoManager.updateShortVideoInfo(shortVideoMap.keySet());
        Log.d("Rx更新", "updateAllFileInfo: 发送文件更新信息");
        picFileNumberMap.clear();
        picFileRepresentMap.clear();
        return PicUpdateType.CHANGE_ALL_FILE;
    }

    /**
     * 启动一个新线程从图片数据库中获取图片信息,
     * 重要注释，看{@link MediaInfoScanner#scanAndUpdatePicInfo()}
     *
     * @param sortPictureList  里面存放pair，第一个是时间，第二个是路径
     * @param fileNumberMap    文件内图片张数,文件路径为key
     * @param fileRepresentMap 文件的代表图片的路径
     */
    private void queryPicInfoInSD(final List<Pair<Long, String>> sortPictureList,
                                  Map<String, Integer> fileNumberMap,
                                  Map<String, String> fileRepresentMap,
                                  final Uri uri) {
        if (uri == null) return;//不为空，放入图片
        String[] projection = {MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE};

        // 文件夹路径，和文件夹代表文件的修改时间
        Map<String, Long> fileRepresentTime = new HashMap<>();

        Cursor cursor = AllData.appContext.getContentResolver().query(uri,
                projection, null, null, null);
        if (cursor != null) {// 从contentProvider之中取出图片
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int size = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Images.Media.SIZE));
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                // Logcat.d("get path from the media. path = " + path);
                long modifyTime = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)) * 1000;// 最近修改时间
                if (AllData.PIC_FILE_SIZE_MIN < size && size < AllData.PIC_FILE_SIZE_MAX) {// 图片符合条件
                    sortPictureList.add(new Pair<>(modifyTime, path));
                    String parentPath = path.substring(0,
                            path.lastIndexOf('/'));
                    if (fileRepresentTime.containsKey(parentPath)) {
                        fileNumberMap.put(parentPath, fileNumberMap.get(parentPath) + 1);
                        if (modifyTime > fileRepresentTime.get(parentPath)) {
                            fileRepresentTime.put(parentPath, modifyTime);
                            fileRepresentMap.put(parentPath, path);
                        }
                    } else {
                        fileNumberMap.put(parentPath, 1);
                        fileRepresentTime.put(parentPath, modifyTime);
                        fileRepresentMap.put(parentPath, path);
                    }
                }
            }
            cursor.close();
        }
    }


    private void queryVideoInfo(final List<Pair<Long, String>> sortPictureList,
                                Map<String, Integer> fileNumberMap,
                                Map<String, String> fileRepresentMap,
                                final Uri uri) {
        if (uri == null) return;//不为空，放入
        String[] projection = new String[]{
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION
        };
        // 文件夹路径，和文件夹代表文件的修改时间
        Map<String, Long> fileRepresentTime = new HashMap<>();

        Cursor cursor = AllData.appContext.getContentResolver().query(uri,
                projection, null, null, null);
        if (cursor != null) {// 从contentProvider之中取出图片
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int duration = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION)); // 毫秒
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                long modifyTime = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)) * 1000;// 最近修改时间
                if (AllData.SHORT_VIDEO_DURATION_MIN < duration &&
                        duration < AllData.SHORT_VIDEO_DURATION_MAX) {// 符合条件
                    sortPictureList.add(new Pair<>(modifyTime, path));
                    String parentPath = path.substring(0,
                            path.lastIndexOf('/'));
                    if (fileRepresentTime.containsKey(parentPath)) {
                        fileNumberMap.put(parentPath, fileNumberMap.get(parentPath) + 1);
                        if (modifyTime > fileRepresentTime.get(parentPath)) {
                            fileRepresentTime.put(parentPath, modifyTime);
                            fileRepresentMap.put(parentPath, path);
                        }
                    } else {
                        fileNumberMap.put(parentPath, 1);
                        fileRepresentTime.put(parentPath, modifyTime);
                        fileRepresentMap.put(parentPath, path);
                    }
                    shortVideoMap.put(path, duration);
                }
            }
            cursor.close();
        }
    }


    /**
     * @return 路径以及是否是短视频
     */
    @NotNull
    public static Pair<String, Boolean> getLatestPicPath() {
        LogUtil.d("准备获取最新图片线程开始执行");
        // 排序处理得到的图片的map,treeMap有序的
        TreeMap<Long, String> sortedPicPathsByTime = new TreeMap<>();
        queryPicInfoInSD(sortedPicPathsByTime, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        LogUtil.d("准备获取最新图片线程开始执行2");
        queryPicInfoInSD(sortedPicPathsByTime, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        LogUtil.d("准备获取最新图片线程开始执行3");
        // 短视频
        //    短视频的路径的集合
        Set<String> shortVideoSet = new HashSet<>();
        queryVideoInfoInSD(shortVideoSet, sortedPicPathsByTime, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        queryVideoInfoInSD(shortVideoSet, sortedPicPathsByTime, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        List<String> list = new ArrayList<>(sortedPicPathsByTime.values());
        LogUtil.d("准备获取最新图片线程开始执行4" + "图片已获取到");
        if (list.size() == 0) return null;
        String latestPath = list.get(list.size() - 1);
        return new Pair<>(latestPath, shortVideoSet.contains(latestPath));
    }

    private static void queryVideoInfoInSD(Set<String> shortVideoSet,
                                           TreeMap<Long, String> sortedVideoPathsByTime, Uri uri) {
        if (uri == null) return;//不为空，放入
        String[] projection = new String[]{
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION
        };

        Cursor cursor = AllData.appContext.getContentResolver().query(uri,
                projection, null, null, null);
        if (cursor != null) {// 从contentProvider之中取出图片
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int duration = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Video.Media.DURATION));
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                long modifyTime = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)) * 1000;// 最近修改时间
                if (AllData.SHORT_VIDEO_DURATION_MIN < duration && duration < AllData.SHORT_VIDEO_DURATION_MAX) {// 图片符合条件
                    sortedVideoPathsByTime.put(modifyTime, path);
                    shortVideoSet.add(path);
                }
            }
            cursor.close();
        }
    }

    /**
     * 从某张sd卡中获取所有图片路径，并放入有序的TreeMap中
     *
     * @param sortPictureList 有序的TreeMap中
     * @param uri             URI
     */
    private static void queryPicInfoInSD(TreeMap<Long, String> sortPictureList, Uri uri) {
        if (uri == null) return;//不为空，放入图片
        String[] projection = {MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE};

        Cursor cursor = AllData.appContext.getContentResolver().query(uri,
                projection, null, null, null);
        if (cursor != null) {// 从contentProvider之中取出图片
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int size = cursor.getInt(cursor
                        .getColumnIndex(MediaStore.Images.Media.SIZE));
                String path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                long modifyTime = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)) * 1000;// 最近修改时间
                if (AllData.PIC_FILE_SIZE_MIN < size && size < AllData.PIC_FILE_SIZE_MAX) {// 图片符合条件
                    sortPictureList.put(modifyTime, path);
                }
            }
            cursor.close();
        }
    }

    public List<Pair<Long, String>> getSortedPicPathsByTime() {
        return sortedPicPathsByTime;
    }

    /**
     * 判断文件是否是视频，从扫描结果中判断
     * 原因是：
     * 直接从文件本身判断文件类型不太方便，现在没有找到相应的可行代码
     * 另一个目前由于前面很多代码都是直接使用path在处理，在扫描返回结果加入类型字段改变太大，包括以前的数据库，
     * 所以想在想到的处理方式是保存扫描结果，然后判断
     */
    public boolean isShortVideo(String path) {
        return shortVideoMap.containsKey(path);
    }

    public void removeSv(String path) {
        shortVideoMap.remove(path);
    }

    @NotNull
    public Map<String, Integer> getShortVideoMap() {
        return shortVideoMap;
    }
}
