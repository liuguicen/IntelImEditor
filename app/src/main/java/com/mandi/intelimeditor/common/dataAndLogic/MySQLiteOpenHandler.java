package com.mandi.intelimeditor.common.dataAndLogic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mandi.intelimeditor.common.util.LogUtil;


/**
 * Created by Administrator on 2016/6/17.
 */
public class MySQLiteOpenHandler extends SQLiteOpenHelper {
    private static final String name = "mysqlite";

    public MySQLiteOpenHandler(Context context, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.d("执行了创建数据库");
        db.execSQL("create table  IF NOT EXISTS usedpic(path text primary key,time varchar(50))");
        db.execSQL("create table  IF NOT EXISTS usualypic(path text primary key,time varchar(50))");
//        db.execSQL("create table  IF NOT EXISTS prefer_share(title text primary key,time varchar(50))");别删，1.0版本的表,
        db.execSQL("create table  IF NOT EXISTS prefer_share(packageName text,title text,time varchar(50))");

        //数据库第4版用到的
        db.execSQL("create table  IF NOT EXISTS recent_style(path text primary key,time varchar(50))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.d("执行了更新数据库");
        //删除
        if (oldVersion == 2) {
            db.execSQL("DROP TABLE prefer_share");
            Log.e("暴走P图第一版的数据库", "删除表格" + "prefer_share" + "成功");
            db.execSQL("create table  IF NOT EXISTS prefer_share(packageName text primary key,title text,time varchar(50))");
            Log.e("暴走P图第二版的数据库", "创建新的表格" + "prefer_share" + "成功");
        }

        if (newVersion == 4) {
            db.execSQL("create table  IF NOT EXISTS recent_style(path text primary key,time varchar(50))");
        }

    }
}
