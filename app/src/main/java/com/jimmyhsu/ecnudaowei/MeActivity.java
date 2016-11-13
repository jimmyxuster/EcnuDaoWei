package com.jimmyhsu.ecnudaowei;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.jimmyhsu.ecnudaowei.Bean.User;
import com.jimmyhsu.ecnudaowei.Db.UserInfoProvider;
import com.jimmyhsu.ecnudaowei.Utils.ImageUtils;
import com.jimmyhsu.ecnudaowei.View.MyViewPager;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.jimmyhsu.ecnudaowei.View.StatusBarCompat;
import com.jimmyhsu.ecnudaowei.View.StickyNavLayout;
import com.jimmyhsu.ecnudaowei.fragment.DwHistoryFragment;
import com.jimmyhsu.ecnudaowei.fragment.ShHistoryFragment;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jimmyhsu on 2016/10/28.
 */

public class MeActivity extends AppCompatActivity {

    private Fragment[] mFragments = new Fragment[2];
    private String[] mTitles = new String[]{"我的尾巴", "我的鱼跃"};
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final int REQUEST_SH = 100;

    public static final String TYPE_SH = "sh";
    public static final String TYPE_DW = "dw";
    private static final int LOADER_ID = 2;

    private int mUserid;

    @BindView(R.id.id_person_head)
    ImageView iv_head;
    @BindView(R.id.id_person_name)
    TextView tv_name;
    @BindView(R.id.id_person_regdate)
    TextView tv_regdate;
    @BindView(R.id.id_personal_sign)
    TextView tv_singature;
    @BindView(R.id.id_stickynavlayout)
    StickyNavLayout mStickyNavLayout;
    @BindView(R.id.id_person_sex)
    ImageView iv_sex;
    @BindView(R.id.id_stickynavlayout_viewpager)
    MyViewPager pager;
    @BindView(R.id.id_stickynavlayout_indicator)
    TabLayout tab;
    @BindView(R.id.id_toolbar)
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String carrier= android.os.Build.MANUFACTURER;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if(carrier.contains("Meizu")) { //avoid a bug that only appears on meizu devices
            setContentView(R.layout.activity_person_meizu);
        } else {
            setContentView(R.layout.activity_person);
        }
        ButterKnife.bind(this);
        initTitleBar();
        mStickyNavLayout.setOnScrollListner(new StickyNavLayout.OnScrollListner() {

            @Override
            public void onScroll(float offset) {
                tv_singature.setAlpha(1 - offset);
                iv_head.setAlpha(1 - offset);
                tv_regdate.setAlpha(1 - offset);
                tv_name.setAlpha(1 - offset);
                iv_sex.setAlpha(1 - offset);
            }
        });
        pager.setScrollble(false);
        initFragments();
        tv_name.setText(MyApplication.getInstance(this).getName());
        loadDataFromDb();
        initViewPager(pager, tab);
    }

    @Override
    protected void onResume() {
        super.onResume();

            Bitmap bm = MyApplication.getInstance(this).getUserHead();
            if (bm != null) {
                RoundDrawable drawable = new RoundDrawable(bm);
                iv_head.setImageDrawable(drawable);
            }

    }

    private void loadDataFromDb() {

        getLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader loader = new CursorLoader(MeActivity.this, UserInfoProvider.URI_USER_CURRENT,
                        null, null, null, null);
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (loader.getId() == LOADER_ID) {
                    data.moveToFirst();
                    tv_name.setText(data.getString(data.getColumnIndex(User.COL_NAME)));
                    tv_regdate.setText("注册日期: " + data.getString(data.getColumnIndex(User.COL_REGDATE)));
                    mUserid = data.getInt(data.getColumnIndex(User.COL_USERINFO_ID));
                    tv_singature.setText(
                            data.getString(data.getColumnIndex(User.COL_SIGNATURE)));
                    int sex = data.getInt(data.getColumnIndex(User.COL_SEX));
                    if (sex == 1) {
                        iv_sex.setImageResource(R.drawable.male);
                    } else if (sex == 2) {
                        iv_sex.setImageResource(R.drawable.female);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SH && resultCode == RESULT_OK && data != null) {
            int item_id = data.getIntExtra(EXTRA_ITEM_ID, -1);
            ShHistoryFragment historyFragment = (ShHistoryFragment) mFragments[0];
            historyFragment.notifyItemRemoved(item_id);
        } else if (requestCode == DwDetailActivity.REQUEST_DW && resultCode == RESULT_OK && data != null) {
            int item_id = data.getIntExtra(EXTRA_ITEM_ID, -1);
            DwHistoryFragment historyFragment = (DwHistoryFragment) mFragments[1];
            historyFragment.removeItemByItemId(item_id);
        }
    }

    private void initViewPager(ViewPager pager, TabLayout tab) {
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles[position];
            }
        });
        tab.setupWithViewPager(pager);
    }

    private void initFragments() {
        mFragments[0] = ShHistoryFragment
                .getInstance(MyApplication.getInstance(this).getName())
                .setClickable(true);

        mFragments[1] = new DwHistoryFragment();
    }

    private void initTitleBar() {

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        param.setMargins(0, StatusBarCompat.getStatusBarHeight(this), 0, 0);
        toolbar.setLayoutParams(param);

        setSupportActionBar(toolbar);
        Drawable homeAsUpDrawable = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        homeAsUpDrawable.setColorFilter(0xffffff, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(homeAsUpDrawable);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");
    }


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item = menu.add(Menu.NONE, Menu.FIRST + 1, 0, "修改个人信息");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case Menu.FIRST + 1:
                Intent intent = new Intent(MeActivity.this, ChangeInfoActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
