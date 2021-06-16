package com.mandi.intelimeditor.common.dataAndLogic;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.mandi.intelimeditor.common.appInfo.AppConfig;
import com.mandi.intelimeditor.common.appInfo.IntelImEditApplication;
import com.mandi.intelimeditor.common.util.FileTool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 一定注意，使用这个类时，使用完了关闭数据库
 */
public class MyDatabase {
    // 写入到数据库，文件名的字符串，不要随便更改
    public static final String CHANGE_FACE_FACE_TAG = "换脸-人脸";
    private static MyDatabase myDatabase;
    private static MySQLiteOpenHandler dbHelper;
    private static SQLiteDatabase db;


    private MyDatabase(Context context) {
        dbHelper = new MySQLiteOpenHandler(context, AppConfig.getDatabaseVersion());
        db = dbHelper.getWritableDatabase();
    }

    public static MyDatabase getInstance() {
        if (myDatabase == null || dbHelper == null || db == null)
            myDatabase = new MyDatabase(IntelImEditApplication.appContext);
        return myDatabase;
    }

    /**
     * 目前只支持本地路径，如果是glide的，会转换成glide在本地的缓存
     * usedpic(path text primary key,time varchar(20))
     * inert时如果存在就替换，使用replace，不然就会出错，
     * 这样就不需要update了
     */
    public void insertUsedPic(String path, long time) {
        if (FileTool.urlType(path) == FileTool.UrlType.URL) {
            try {
                path = Glide.with(IntelImEditApplication.appContext)
                        .load(path)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get()
                        .getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db.execSQL("replace into usedpic(path,time) values(?,?) ", new Object[]{path, String.valueOf(time)});
    }

    /**
     * 超出最多图片数时，删除最早添加进去的图片
     * usedpic(path text primary key,time varchar(20))
     */
    public void deleteOdlestUsedPic() {
/*        Cursor cursor = db.rawQuery("select path from usedpic where time = ( select min(time) from usedpic ) ", new String[]{});
        cursor.moveToNext();String path = cursor.getString(0);
        return path;
        */
        db.execSQL("delete from usedpic where time = ( select min(time) from usedpic )", new Object[]{});
    }

    /**
     * 删除
     * usedpic(path text primary key,time varchar(20))
     *
     * @param path
     */
    public void deleteUsedPic(String path) {
        db.execSQL("delete from usedpic where path = ?", new Object[]{path});
    }

    /**
     * usedpic(path text primary key,time varchar(20))
     *
     * @param path
     * @param time
     */
    public void updateUsedPic(String path, long time) {
        insertUsedPic(path, time);
    }

    /**
     * 获取存入数据库的所有使用过得图片
     * usedpic(path text primary key,time varchar(20))
     *
     * @param pathList
     */
    //    有两个返回值，不能直接返回，传入应用获取
    public void queryAllUsedPic(int start, List<String> pathList) {
        Cursor cursor = db.rawQuery("select path from usedpic order by time desc ", new String[]{});
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            if (path == null) return;
            //从数据库读出文件路径时就检测是否已被删除，删除了则不添加，并且从数据库删除，
            if (!(new File(path).exists())) {
                deleteUsedPic(path);
            } else {
                pathList.add(start, path);
                start++;
            }
        }
        cursor.close();
    }

    /**
     * 查询图片路径加上时间
     */
    public void queryAllUsedPicWithTime(List<String> pathList) {
        Cursor cursor = db.rawQuery("select * from usedpic order by time desc ", new String[]{});
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            if (!(new File(path).exists()))
                deleteUsedPic(path);
            else
                pathList.add(path);
        }
        cursor.close();
    }

    /**
     * usualypic(path text primary key,time varchar(20))
     * inert时如果存在就替换，使用replace，不然就会出错，
     * 这样就不需要update了
     */
    public void insertPreferPic(String path, long time) {
        db.execSQL("replace into usualypic(path,time) values(?,?) ", new Object[]{path, String.valueOf(time)});
    }

    /**
     * 删除一张喜爱的图片
     *
     * @param path 路径
     * @throws IOException
     * @see MySQLiteOpenHandler usualypic(path text primary key,time varchar(20))
     */
    public void deletePreferPicPath(String path) {
        db.execSQL("delete from usualypic where path = ?", new Object[]{path});
    }


    public boolean isInPrefer(String path) {
        Cursor cursor = db.rawQuery("select * from usualypic where path = ? ", new String[]{path});
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;

    }

    /**
     * 获取存入数据库的所有的选择出常用的得图片
     * usualypic(path text primary key,time varchar(20))
     *
     * @param pathList
     */
    //    有两个返回值，不能直接返回，传入应用获取
    public void queryAllPreferPic(int start, List<String> pathList) {
        Cursor cursor = db.rawQuery("select path from usualypic order by time desc ", new String[]{});
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            if (!(new File(path).exists()))
                deletePreferPicPath(path);
            else {
                pathList.add(start, path);
                start++;
            }
        }
        cursor.close();
    }

    /**
     * 获取所有的优先分享的ac的title
     * "create table  IF NOT EXISTS prefer_share(title text primary key,time varchar(50))"
     * 按时间倒序，即越前面优先级越高
     */
    //    有两个返回值，不能直接返回，传入应用获取
    public void queryAllPreferShare_andRemoveDuplicate(List<Pair<String, String>> titleList) {
        List<Pair<String, String>> srcList = new ArrayList<>();
        List<Long> timeList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select packageName,title,time from prefer_share order by time desc ", new String[]{});
        while (cursor.moveToNext()) {
            String packageName = cursor.getString(0);
            String title = cursor.getString(1);
            long time = 0;
            try {
                time = Long.parseLong(cursor.getString(2));
            } catch (Exception e) {

            }
            srcList.add(new Pair<>(packageName, title));
            timeList.add(time);
        }
        cursor.close();

        for (int i = 0; i < srcList.size(); i++) {
            Pair<String, String> pair = srcList.get(i);
            if (!titleList.contains(pair)) {
                titleList.add(pair);
            } else {
                db.execSQL("delete from prefer_share where packageName = ? and title = ? and time = ? ",
                        new Object[]{pair.first, pair.second, "" + timeList.get(i)});
            }
        }
    }


    /**
     * db.execSQL("create table  IF NOT EXISTS prefer_share(packageName text,title text,time varchar(50))");
     * inert时如果存在就替换，使用replace，不然就会出错，
     * 这样就不需要update了
     */
    public void insertPreferShare(String packageName, String title, long time) {
    /*    Cursor cursor = db.rawQuery("select * from sqlite_master where type= ? and name= ? ",new String[]{"table","prefer_share"});
        while (cursor.moveToNext()) {
            cursor.moveToNext();
        }*/
        db.execSQL("replace into prefer_share(packageName,title,time) values(?,?,?) ", new Object[]{packageName, title, String.valueOf(time)});
    }

    /**
     * db.execSQL("create table  IF NOT EXISTS prefer_share(packageName text,title text,time varchar(50))");
     *
     * @param packageName ac的包名     * @param title ac的title
     */
    public void deletePreferShare(String packageName, String title) {
        db.execSQL("delete from prefer_share where packageName = ? and title = ? ", new Object[]{packageName, title});
    }


    /**********************************  贴图相关 ****************************************/

    /**
     * 加入一张贴图
     * recent_style(path text primary key,time varchar(20))
     * inert时如果存在就替换，使用replace，不然就会出错，
     * 这样就不需要update了
     */
    public void insertMyTietu(String path, long time) {
        db.execSQL("replace into recent_style(path,time) values(?,?) ", new Object[]{path, String.valueOf(time)});
    }

    /**
     * 删除加入的贴图
     * recent_style(path text primary key,time varchar(20))
     *
     * @param path
     */
    public void deleteMyTietu(String path) {
        db.execSQL("delete from recent_style where path = ?", new Object[]{path});
    }

    /**
     * 更新加入的贴图
     * recent_style(path text primary key,time varchar(20))
     *
     * @param path
     * @param time
     */
    public void updateMyTietu(String path, long time) {
        insertMyTietu(path, time);
    }

    public boolean isInMyTietu(String path) {
        Cursor cursor = db.rawQuery("select * from recent_style where path = ? ", new String[]{path});
        boolean result = cursor.moveToNext();
        cursor.close();
        return result;
    }

    /**
     * 获取存入数据库的所有加入的贴图
     * recent_style(path text primary key,time varchar(20))
     *
     * @param pathList
     */
    //    有两个返回值，不能直接返回，传入应用获取
    public void queryAllMyTietu(List<String> pathList) {
        Cursor cursor = db.rawQuery("select path from recent_style order by time desc ", new String[]{});
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            //从数据库读出文件路径时就检测是否已被删除，删除了则不添加，并且从数据库删除，
            if (FileTool.urlType(path) != FileTool.UrlType.URL &&
                    !new File(path).exists()) {
                deleteMyTietu(path);
            } else
                pathList.add(path);
        }
        cursor.close();
    }

    /**
     * 查询贴图图片路径加上时间
     */
    public void queryAllMyTietuAndTime(List<String> pathList) {
        Cursor cursor = db.rawQuery("select * from recent_style order by time desc ", new String[]{});
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            if (FileTool.urlType(path) != FileTool.UrlType.URL &&
                    !new File(path).exists())
                deleteUsedPic(path);
            else
                pathList.add(path);
        }
        cursor.close();
    }


    /**
     * google官方文档
     * 由于在数据库关闭时，调用 getWritableDatabase() 和 getReadableDatabase() 的成本比较高，因此只要您有可能需要访问数据库，就应保持数据库连接处于打开状态。
     * 通常情况下，最好在发出调用的 Activity 的 onDestroy() 中关闭数据库。
     */
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }

    }
}
