package com.jimmyhsu.ecnudaowei;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by jimmyhsu on 2016/10/8.
 */
public class MyApplication extends Application {
    private int id = -1;
    private String name;
    private String password;
    private boolean isLogin = false;
    private static RequestQueue requestQueue;
    private String mDeviceId = "";
    private static VolleyCache volleyCache;
    private static boolean isUserHeadNeedRefresh = false;
    private Bitmap mHead = null;

    public static MyApplication getInstance(Context context) {
        return (MyApplication) context.getApplicationContext();
    }

    public void setUserHead(Bitmap bitmap) {
        mHead = bitmap;
    }
    public Bitmap getUserHead() {
        return mHead;
    }


    public static boolean isIsUserHeadNeedRefresh() {
        return isUserHeadNeedRefresh;
    }

    public static void setIsUserHeadNeedRefresh(boolean flag) {
        isUserHeadNeedRefresh = flag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static VolleyCache getVolleyCache() {
        return volleyCache;
    }

    public String getAndroidId(Context context) {
        if ("".equals(mDeviceId)) {
            mDeviceId = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return mDeviceId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(this);
        volleyCache = new VolleyCache();
    }
    public class VolleyCache implements ImageLoader.ImageCache {

        private LruCache<String, Bitmap> mMemoryCache;

        public VolleyCache() {
            mMemoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 6f)) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String s) {
            return mMemoryCache.get(s);
        }

        @Override
        public void putBitmap(String s, Bitmap bitmap) {
            mMemoryCache.put(s, bitmap);
        }
    }
}
