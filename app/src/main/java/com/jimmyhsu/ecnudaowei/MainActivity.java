package com.jimmyhsu.ecnudaowei;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.Bean.User;
import com.jimmyhsu.ecnudaowei.Db.UserInfoProvider;
import com.jimmyhsu.ecnudaowei.Utils.ImageUtils;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.jimmyhsu.ecnudaowei.fragment.DwFragment;
import com.jimmyhsu.ecnudaowei.fragment.SecondHandFragment;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int LOADER_ID = 1;
    private static final int MSG_REFRESH_HEAD = 0x110;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE_LOC = 101;
    public static final int REQUEST_RELEASE_SH = 1;
    public static final int REQUEST_RELEASE_DW = 2;

    private static int[] NAV_IDS = {R.id.nav_secondhand,  R.id.nav_daowei, R.id.nav_me};
//    private RelativeLayout mContainer;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawer;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    ImageView mNavHead;
    TextView mNavNameTv;
    TextView mNavSignTv;

    private SecondHandFragment mSecondHandFragment;
    private DwFragment mDwFragment;
    private MyApplication mApplication;
    private int mGoto = R.id.nav_secondhand;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_REFRESH_HEAD) {

                Bitmap userHead = mApplication.getUserHead();
                if (userHead != null) {
                    RoundDrawable drawable = new RoundDrawable(userHead);
                    mNavHead.setImageDrawable(drawable);
                }

            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mApplication = (MyApplication) getApplicationContext();
        boolean needLogin = checkLogin();
        if (needLogin) {
            return;//        mContainer = (RelativeLayout) findViewById(R.id.id_main_container);
        }
        mSecondHandFragment = new SecondHandFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.id_main_container, mSecondHandFragment).commit();
        mSecondHandFragment.initDatas(this);
        initEvents();
        initNavigation();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            checkPermission();
        }
    }

    private void checkPermission() {
        Toast.makeText(this, "用于缓存图片和定位功能，请授予哦~", Toast.LENGTH_SHORT).show();
        PermissionGen.with(MainActivity.this)
                .addRequestCode(PERMISSION_REQUEST_CODE)
                .permissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE)
                .request();
    }

//    @PermissionSuccess(requestCode = PERMISSION_REQUEST_CODE)
//    public void onSuccessPermission(){
//        Log.d("MainActivity", "permission granted");
//    }

    @PermissionFail(requestCode = PERMISSION_REQUEST_CODE)
    public void onFailPermission(){
        Toast.makeText(this, "呜呜~", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void downloadUserHead(final int id) {
        if (id > 0) {
            OkHttpUtils.get().url(LoginActivity.BASE_URL + "phpprojects/userimage/" +
                    id + ".jpg").build().execute(new BitmapCallback() {
                @Override
                public void onError(Request request, Exception e) {

                }

                @Override
                public void onResponse(Bitmap response) {
                    mApplication.setUserHead(response);
                    mHandler.sendEmptyMessage(MSG_REFRESH_HEAD);
                }
            });
        }
    }

    private void initNavigation() {
        String name = mApplication.getName();
        View headerView = mNavigationView.getHeaderView(0);
        mNavNameTv = ButterKnife.findById(headerView, R.id.id_nav_name);
        mNavSignTv = ButterKnife.findById(headerView, R.id.id_nav_signature);
        mNavHead = ButterKnife.findById(headerView, R.id.id_nav_head);
        mNavigationView.setCheckedItem(R.id.nav_secondhand);
        if (name != null) {
            mNavNameTv.setText(name);
            initLoader();
        }
    }

    private void initLoader() {
        getLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader loader = new CursorLoader(MainActivity.this, UserInfoProvider.URI_USER_CURRENT,
                        null, null, null, null);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (loader.getId() == LOADER_ID) {
                    if (data.getCount() <= 0) {
                        insertEmptyInfo();
                    } else {
                        data.moveToFirst();
                        String name = data.getString(data.getColumnIndex(User.COL_NAME));
                        String sign = data.getString(data.getColumnIndex(User.COL_SIGNATURE));
                        mNavNameTv.setText(name);
                        mNavSignTv.setText(sign);

                    }
                    downloadUserInfo();
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });
    }

    private void insertEmptyInfo() {
        ContentValues values = new ContentValues();
        values.put(User.COL_NAME, mApplication.getName());
        values.put(User.COL_AGE, 18);
        values.put(User.COL_MOBILE, "unknown");
        values.put(User.COL_REGDATE, new SimpleDateFormat("yyyy年MM月dd日").format(new Date(System.currentTimeMillis())));
        values.put(User.COL_SEX, 1);
        values.put(User.COL_SIGNATURE, "快来填写吧~");
        values.put(User.COL_STU_ID, "unknown");
        values.put(User.COL_USERINFO_ID, 0);
        getContentResolver().insert(UserInfoProvider.URI_USER_CURRENT, values);
    }

    private void downloadUserInfo() {
        OkHttpUtils.post().url(LoginActivity.BASE_URL + "phpprojects/getMyInfo.php")
                .addParams("username", mApplication.getName())
                .addParams("deviceid", mApplication.getAndroidId(this))
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mDrawer, "网络异常", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getInt("status") == 200) {
                                int id = obj.getInt("id");
                                String mobile = obj.getString("mobile");
                                String sign = obj.getString("signature");
                                long regdate = obj.getLong("regdate");
                                int sex = obj.getInt("sex");
                                String stuId = obj.getString("studentid");
                                int age = obj.getInt("age");
                                ContentValues values = new ContentValues();
                                values.put(User.COL_NAME, mApplication.getName());
                                values.put(User.COL_AGE, age);
                                values.put(User.COL_MOBILE, mobile);
                                values.put(User.COL_REGDATE, new SimpleDateFormat("yyyy年MM月dd日").format(new Date(regdate)));
                                values.put(User.COL_SEX, sex);
                                values.put(User.COL_SIGNATURE, sign);
                                values.put(User.COL_STU_ID, stuId);
                                values.put(User.COL_USERINFO_ID, id);
                                mApplication.setId(id);
                                downloadUserHead(id);
                                mHandler.sendEmptyMessage(MSG_REFRESH_HEAD);
                                getContentResolver().update(UserInfoProvider.URI_USER_CURRENT,
                                        values,
                                        "_id=1", null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("MainActivity", "json err: " + response);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MSG_REFRESH_HEAD);
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RELEASE_SH && resultCode == RESULT_OK) {
            if (mGoto == R.id.nav_secondhand && mSecondHandFragment != null) {
                mSecondHandFragment.refreshData(this);
            }
        } else if (requestCode == REQUEST_RELEASE_DW && resultCode == RESULT_OK) {
            if (mGoto == R.id.nav_daowei && mDwFragment != null) {
                mDwFragment.refresh();
            }
        }
    }

    private void initEvents() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGoto == R.id.nav_secondhand) {
                    Intent intent = new Intent(MainActivity.this, ReleaseNewItemActivity.class);
                    startActivityForResult(intent, REQUEST_RELEASE_SH);
                } else if (mGoto == R.id.nav_daowei) {
                    Intent intent = new Intent(MainActivity.this, ReleaseDwActivity.class);
                    startActivityForResult(intent, REQUEST_RELEASE_DW);
                }
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        mDrawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset == 0) {
                    switch (mGoto) {
                        case R.id.nav_secondhand:
//                            mSecondHandFragment.initDatas(MainActivity.this);
                            getSupportFragmentManager().beginTransaction()
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .replace(R.id.id_main_container, mSecondHandFragment)
                                    .commit();
                            break;
                        case R.id.nav_daowei:
                            checkLocationPermission();

                            break;

                    }
                }
            }
        });
    }

    private void checkLocationPermission() {
        PermissionGen.with(MainActivity.this)
                .addRequestCode(PERMISSION_REQUEST_CODE_LOC)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .request();
    }

    @PermissionSuccess(requestCode = PERMISSION_REQUEST_CODE_LOC)
    public void gotoDw(){
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.id_main_container, mDwFragment)
                .commit();
    }

    @PermissionFail(requestCode = PERMISSION_REQUEST_CODE_LOC)
    public void failPermissionLoc(){
        mNavigationView.setCheckedItem(R.id.nav_daowei);
        mNavigationView.getMenu().findItem(R.id.nav_daowei).setCheckable(false);
    }

    private boolean checkLogin() {
        SharedPreferences sp = getSharedPreferences(LoginActivity.SP_NAME, MODE_PRIVATE);
        if (!sp.getBoolean(LoginActivity.SP_KEY_ISLOGIN, false)) {
            mApplication.setLogin(false);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else {
            mApplication.setName(sp.getString(LoginActivity.SP_KEY_NAME, ""));
            mApplication.setPassword(sp.getString(LoginActivity.SP_KEY_PASSWORD, ""));
            mApplication.setLogin(true);
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            setResult(RESULT_OK);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        if (mGoto == R.id.nav_daowei) {
            MenuItem refreshMenu = menu.add(Menu.NONE, Menu.FIRST + 1, 0, "刷新");
            refreshMenu.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_refresh_white_24dp));
            refreshMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == Menu.FIRST + 1) {
            if (mDwFragment != null) {
                mDwFragment.refresh();
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private String mCurrentTitle = "鱼尾巴";
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == mGoto) {
            mDrawer.closeDrawer(GravityCompat.START);
            return true;
        }
        if (id == R.id.nav_secondhand) {
            mGoto = R.id.nav_secondhand;
            mSecondHandFragment = new SecondHandFragment();
            mDrawer.closeDrawer(GravityCompat.START);
            supportInvalidateOptionsMenu();
        } else if (id == R.id.nav_daowei) {
            mGoto = R.id.nav_daowei;
            mDwFragment = new DwFragment();
            mDrawer.closeDrawer(GravityCompat.START);
            supportInvalidateOptionsMenu();
        } else if (id == R.id.nav_me) {
            MeActivity.startActivity(this);
        } else if (id == R.id.nav_logout) {
            logout();
        }

        return true;
    }

    private void logout() {
        SharedPreferences sp = getSharedPreferences(LoginActivity.SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(LoginActivity.SP_KEY_NAME);
        edit.remove(LoginActivity.SP_KEY_PASSWORD);
        edit.remove(LoginActivity.SP_KEY_ISLOGIN);
        edit.apply();
        LoginActivity.goToLogin(this, false);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }
}
