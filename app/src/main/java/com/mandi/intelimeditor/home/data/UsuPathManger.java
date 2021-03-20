package com.mandi.intelimeditor.home.data;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import com.mandi.intelimeditor.common.dataAndLogic.MyDatabase;
import com.mandi.intelimeditor.common.util.TimeDateUtil;
import com.mandi.intelimeditor.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;




/**
 * 用来处理常用图片的类，作为activity与底层的接口。包括数据库，ContentProvider，文件等
 * 增删改查等功能
 * <p>
 * 数据的顺序，最近常用用图片
 * <p>最近图片
 * <p>常用图片图片
 * <p>
 * </p>
 */
public class UsuPathManger {
    public final static String USED_FLAG = "@%^@#GDa_USED";
    public final static String RECENT_FLAG = "@%^@#GDa_RECENT";
    public final static String PREFER_FLAG = "@%^@#GDa_PREFER";
    // 按日期分组的时间头的格式：RECENT_FLAG 接上 日期信息
    private Context mContext;
    private final int MAX_USED_NUMBER = 4;
    private final int MAX_RECENT_NUMBER = 2000;
    private MyDatabase mDB;
    /**
     * 只获取一次系统时间，以后都以它为基础相加，避免加入太快，毫秒不能记数
     */
    private long lastTime = System.currentTimeMillis();

    /**
     * 加入了所有最近图片，会很长，使用的时候注意性能，比如indexOf,能其他方法代替则代替
     */
    private List<String> mUsuallyPicPathList = new ArrayList<>();

    public UsuPathManger(Context context) {
        mContext = context;
    }

    public List<String> initFromDB() throws Exception {
        try {
            mDB = MyDatabase.getInstance();
            mUsuallyPicPathList.add(USED_FLAG);
            mDB.queryAllUsedPic(getUsedStart(), mUsuallyPicPathList);
            mUsuallyPicPathList.add(PREFER_FLAG);
            mDB.queryAllPreferPic(getPreferStart(), mUsuallyPicPathList);
            //            for (String path : usuallyFilesList) {  // 获取文件内的常用图片，暂时不采用
            //                FileTool.getOrderedPicListInFile(path, mUsuallyPicPathList);
            //            }
            mUsuallyPicPathList.add(RECENT_FLAG);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(mContext.getString(R.string.failed_to_get_data_from_local_db));
        } finally {
            mDB.close();
        }
        return mUsuallyPicPathList;
    }

    /*****************************  最近使用相关  *********************/

    /**
     * 获取常用图片的开始位置
     * 有used元素时和一个元素都没有，都返回到USED_FLAG + 1 的位置
     */
    private int getUsedStart() {
        return 1;
    }

    /**
     * @return 有used路径时，返回最后一个used路径的位置
     *         如果一个used路径都没有，会返回到USED_FLAG的位置
     */
    private int getUsedEnd() {
        for (int i = 1; i < mUsuallyPicPathList.size(); i++) {
            if (PREFER_FLAG.equals(mUsuallyPicPathList.get(i)))
                return i - 1;
        }
        return 1; // 这句不会执行，前面会保证能执行到
    }

    /**
     * 添加最近编辑过的图片
     * 同时会添加到数据库中
     */
    //注意数据库，内存双添加,以及相关参数改变
    public void addUsedPath(String path) {
        try {
            mDB = MyDatabase.getInstance();
            int start = getUsedStart(), end = getUsedEnd();
            int usedNumber = end - start + 1;
            //如果存在，需要先删除原来的
            int id = mUsuallyPicPathList.indexOf(path);
            if (id >= start && id <= end) {
                mDB.deleteUsedPic(path);
                mUsuallyPicPathList.remove(path);
                usedNumber--;
            }

            //超过最大数量时，删除一个，再添加
            if (usedNumber >= MAX_USED_NUMBER) {
                mDB.deleteOdlestUsedPic();
                mUsuallyPicPathList.remove(getUsedEnd());
            }
            mDB.insertUsedPic(path, lastTime++);
            mUsuallyPicPathList.add(getUsedStart(), path);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.close();
        }
    }

    public void deleteUsedPath(String path) {
        try {
            mDB = MyDatabase.getInstance();
            mDB.deleteUsedPic(path);
            //如果存在，需要先删除原来的
            int start = getUsedStart(), end = getUsedEnd();
            for (int i = end; i >= start; i--) {
                if (mUsuallyPicPathList.get(i).equals(path)) {
                    mUsuallyPicPathList.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.close();
        }
    }


    /*************************常用图片相关*******************/
    /**
     * 获取常用图片的开始位置
     * 有prefer元素时和一个元素都没有，都返回到PREFER_FLAG + 1 的位置
     */
    public int getPreferStart() {
        return getUsedEnd() + 2;
    }

    /**
     * @return 有prefer路径时，返回最后一个prefer路径的位置
     *         如果一个prefer路径都没有，会返回到PREFER_FLAG的位置
     */
    private int getPreferEnd() {
        for (int i = 1; i < mUsuallyPicPathList.size(); i++) {
            if (RECENT_FLAG.equals(mUsuallyPicPathList.get(i)))
                return i - 1;
        }
        return 0; // 这句不会执行，前面保证能执行到
    }

    /**
     * 添加选定的常用图片， 加在前面
     */
    public boolean addPreferPath(String path) {
        try {
            mDB = MyDatabase.getInstance();
            mDB.insertPreferPic(path, lastTime++);
            mUsuallyPicPathList.add(getPreferStart(), path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.close();
        }
        return false;
    }

    /**
     * 删除常用图片路径
     *
     * @param path 喜爱图片的途径，不是位置
     */
    public void deletePreferPath(String path) {
        try {
            mDB = MyDatabase.getInstance();
            mDB.deletePreferPicPath(path);
            int start = getPreferStart(), end = getPreferEnd();
            for (int i = end; i >= start; i--) {
                if (mUsuallyPicPathList.get(i).equals(path)) {
                    mUsuallyPicPathList.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.close();
        }
    }

    public boolean isInPrefer(String path) {
        int preferStart = getPreferStart();
        int preferEnd = getPreferEnd();
        for (int i = preferStart; i <= preferEnd; i++) {
            if (mUsuallyPicPathList.get(i).equals(path))
                return true;
        }
        return false;
    }

    /**
     * 检测最近的图片是否存在，不存在就删除
     *
     * @return 返回是否有变动的
     */
    public boolean checkRecentExit() {
        boolean has = false;
        int start = getRecentStart();
        for (int i = mUsuallyPicPathList.size() - 1; i >= start; i--) {
            String path = mUsuallyPicPathList.get(i);
            if (!new File(path).exists()) {
                mUsuallyPicPathList.remove(i);
                has = true;
            }
        }
        return has;
    }

    /**
     * 添加最近图片,最后面
     */
    private void addRecentPathEnd(String path) {
        mUsuallyPicPathList.add(path);
    }

    /**
     * 添加最近图片，最前面
     */
    public void addRecentPathStart(String path) {
        mUsuallyPicPathList.add(getRecentStart(), path);
        Log.d("---------", "addRecentPathStart: 添加最近图片成功");
    }


    public boolean hasRecentPic(String picPath) {
        int id = mUsuallyPicPathList.indexOf(picPath);
        return id >= getRecentStart();
    }

    /**
     * 更新usu列表中的最近图片信息
     *
     * @param sortedPicPathsByTime 排好序的最近图片
     */
    void updateRecentInfoInUsu(List<Pair<Long, String>> sortedPicPathsByTime) {

        //先清空所有的最近图片路径
        int recentStart = getRecentStart();
        if (mUsuallyPicPathList.size() > recentStart) { // 编译器提示的优化方法
            mUsuallyPicPathList.subList(recentStart, mUsuallyPicPathList.size()).clear();
        }

        //再添所有最近的路径
        long dayStart = Long.MAX_VALUE;
        long todayStart = TimeDateUtil.getTimeInDay();
        String todayYear = TimeDateUtil.time2ChineseData(todayStart).substring(0, 5);
        long yesterdayStart = todayStart - TimeDateUtil.DAY_MILS;
        for (int i = 0; i < sortedPicPathsByTime.size(); i++) {
            if (i > MAX_RECENT_NUMBER) break;
            Pair<Long, String> pair = sortedPicPathsByTime.get(i);
            if (pair.first < dayStart) {  // 时间小于当前天的开始时间，表示新的一天，加header
                dayStart = pair.first - pair.first % TimeDateUtil.DAY_MILS + 1;
                String dataMsg;
                if (dayStart < yesterdayStart) {
                    dataMsg = TimeDateUtil.time2ChineseData(dayStart);
                    if (dataMsg.startsWith(todayYear)) { // 今年的，不显示年份
                        dataMsg = dataMsg.substring(todayYear.length());
                    }
                } else if (dayStart < todayStart) {
                    dataMsg = mContext.getString(R.string.yesterday);
                } else {
                    dataMsg = mContext.getString(R.string.today);
                }
                mUsuallyPicPathList.add(RECENT_FLAG + dataMsg);
            }
            mUsuallyPicPathList.add(pair.second);
        }
    }

    private boolean isInRecent(String path) {
        int start = getRecentStart();
        for (int i = start; i < mUsuallyPicPathList.size(); i++) {
            if (mUsuallyPicPathList.get(i).equals(path))
                return true;
        }
        return false;
    }

    /**
     * @return 返回最近图片的开始位置，RECENT_FLAG下一个位置，
     *         没有最近图片时，会越界！注意使用时判断！
     */
    private int getRecentStart() {
        for (int i = 1; i < mUsuallyPicPathList.size(); i++) {
            if (RECENT_FLAG.equals(mUsuallyPicPathList.get(i)))
                return i + 1;
        }
        return mUsuallyPicPathList.size();
    }

    public void onDeleteUsuallyPicture(String path) {
        mDB = MyDatabase.getInstance();
        try {
            //如果包含在最近使用列表
            deleteUsedPath(path);
            //如果包含在常用图片列表
            deletePreferPath(path);
            //如果包含在最近图片列表
            mUsuallyPicPathList.remove(path); // 不用检查了，直接删就行
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.close();
        }
    }


    public boolean isUsuPic(List<String> imagUrls) {
        return imagUrls == mUsuallyPicPathList;
    }

    /**
     * @return 列表里面加入了所有最近图片，会很长，使用的时候注意性能，比如indexOf,能其他方法代替则代替
     */
    @NotNull
    public List<String> getUsuPaths() {
        return mUsuallyPicPathList;
    }

    public int lastIndexOf(String path) {
        return mUsuallyPicPathList.lastIndexOf(path);
    }

    public boolean isAdd2Used(String path) {
        int usedEnd = getUsedEnd();
        for (int i = 0; i <= usedEnd; i++) {
            if (mUsuallyPicPathList.get(i).equals(path))
                return true;
        }
        return false;
    }

    public static boolean isHeader(String url) {
        if (url == null) return false;
        if (url.startsWith(UsuPathManger.RECENT_FLAG)) { // recent多，先判断
            return true;
        }
        if (UsuPathManger.USED_FLAG.equals(url))//存在使用过的图片
            return true;
        if (UsuPathManger.PREFER_FLAG.equals(url))
            return true;
        return false;
    }
}
