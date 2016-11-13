package com.jimmyhsu.ecnudaowei.Db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jimmyhsu.ecnudaowei.Bean.User;

/**
 * Created by jimmyhsu on 2016/10/27.
 */

public class UserDbHelper extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;
    private static String DB_NAME = "big_fish_db";
    private UserDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    private static UserDbHelper mInstance;
    public static UserDbHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (UserDbHelper.class) {
                if (mInstance == null) {
                    mInstance = new UserDbHelper(context);
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + User.TB_NAME +
                "(_id integer primary key autoincrement, " +
                User.COL_NAME + " text not null, " +
                User.COL_AGE + " integer not null, " +
                User.COL_MOBILE + " text not null, " +
                User.COL_REGDATE + " text not null, " +
                User.COL_SEX + " integer not null, " +
                User.COL_STU_ID + " text not null, " +
                User.COL_USERINFO_ID + " integer not null, " +
                User.COL_SIGNATURE + " text not null" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
