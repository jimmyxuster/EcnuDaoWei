package com.jimmyhsu.ecnudaowei.Db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.jimmyhsu.ecnudaowei.Bean.User;

/**
 * Created by jimmyhsu on 2016/10/27.
 */

public class UserInfoProvider extends ContentProvider {

    private UserDbHelper mHelper;
    private SQLiteDatabase mDataBase;

    private static final String AUTHORITY ="com.jimmyhsu.ecnudaowei.provider.UserProvider";
    public static final Uri URI_USER_CURRENT = Uri.parse("content://"+AUTHORITY+"/current");
    private static UriMatcher mMatcher;
    private static final int USER_CURRENT = 0;
    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(AUTHORITY, "current", USER_CURRENT);
    }
    @Override
    public boolean onCreate() {
        mHelper = UserDbHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = mMatcher.match(uri);
        if (match != USER_CURRENT) {
            throw new IllegalArgumentException("Wrong Uri: " + uri.toString());
        }
        mDataBase = mHelper.getReadableDatabase();
        Cursor cursor = mDataBase.query(User.TB_NAME, projection, selection, selectionArgs, null,
                null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), URI_USER_CURRENT);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int match = mMatcher.match(uri);
        if (match != USER_CURRENT) {
            throw new IllegalArgumentException("Wrong Uri: " + uri.toString());
        }
        mDataBase = mHelper.getWritableDatabase();
        long insertId = mDataBase.insert(User.TB_NAME, null, values);
        if (insertId > 0) {
            notifyDataSetChanged();
            return ContentUris.withAppendedId(uri, insertId);
        }
        return null;
    }

    private void notifyDataSetChanged() {
        getContext().getContentResolver().notifyChange(URI_USER_CURRENT, null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int match = mMatcher.match(uri);
        if (match != USER_CURRENT) {
            throw new IllegalArgumentException("Wrong Uri: " + uri.toString());
        }
        mDataBase = mHelper.getWritableDatabase();
        int updateId = mDataBase.update(User.TB_NAME, values, selection, selectionArgs);
        notifyDataSetChanged();
        return updateId;
    }
}
