package com.jimmyhsu.ecnudaowei;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.jimmyhsu.ecnudaowei.View.MyViewPager;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.jimmyhsu.ecnudaowei.View.StatusBarCompat;
import com.jimmyhsu.ecnudaowei.View.StickyNavLayout;
import com.jimmyhsu.ecnudaowei.fragment.PersonalInfoLeftFragment;
import com.jimmyhsu.ecnudaowei.fragment.ShHistoryFragment;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jimmyhsu on 2016/10/16.
 */
public class PersonalInfoActivity extends AppCompatActivity {

    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_ID = "extra_id";//item id
    private String mOtherName;
    private String mMyName;
    private int id;
    private MyApplication mApplication;
    private Fragment[] mFragments = new Fragment[2];
    private String[] mTitles = new String[]{"个人信息", "出售记录"};

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
        mOtherName = getIntent().getStringExtra(EXTRA_NAME);
        id = getIntent().getIntExtra(EXTRA_ID, -1);
        mApplication = (MyApplication) getApplicationContext();
        mMyName = mApplication.getName();
        //not Meizu
        mStickyNavLayout.setOnScrollListner(new StickyNavLayout.OnScrollListner() {

            @Override
            public void onScroll(float offset) {
                tv_singature.setAlpha(1 - offset);
                iv_head.setAlpha(1 - offset);
                tv_regdate.setAlpha(1 - offset);
                tv_name.setAlpha(1 - offset);
            }
        });

        final MyViewPager pager = (MyViewPager) findViewById(R.id.id_stickynavlayout_viewpager);

        pager.setScrollble(false);
        TabLayout tab = (TabLayout) findViewById(R.id.id_stickynavlayout_indicator);
        initFragments();
        tv_name.setText(mOtherName);
        initViewPager(pager, tab);
        requestDataFromNetwork(iv_head, tv_regdate, tv_singature);
    }

    private void requestDataFromNetwork(final ImageView iv_head, final TextView tv_regdate, final TextView tv_singature) {
        OkHttpUtils
                .post()
                .url(LoginActivity.BASE_URL + "phpprojects/getpersoninfo.php")
                .addParams("myname", mMyName)
                .addParams("othername", mOtherName)
                .addParams("deviceid", LoginActivity.getAndroidId(this))
                .addParams("id", String.valueOf(id))
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Toast.makeText(PersonalInfoActivity.this, "网络未连接", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
//                            tv_name.setText(obj.getString("name"));
                            tv_regdate.setText("注册日期: " + new SimpleDateFormat("yyyy年MM月dd日")
                                    .format(new Date(obj.getLong("regdate"))));
                            int sex = obj.getInt("sex");
                            if (sex == 1) {
                                iv_sex.setImageResource(R.drawable.male);
                            } else if (sex == 2) {
                                iv_sex.setImageResource(R.drawable.female);
                            }
                            tv_singature.setText(obj.getString("sign"));
                            loadImage(LoginActivity.BASE_URL + "phpprojects/" + obj.getString("url"), iv_head, true);
                            String contact = obj.getString("contact");
                            ((PersonalInfoLeftFragment)(mFragments[0])).addPersonInfo("联系方式", contact);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("TAG", "json error: " + response);
                        }
                    }
                });
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

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mStickyNavLayout != null && position == 0 && !mStickyNavLayout.getIsAtTop()) {
                    mStickyNavLayout.smoothScrollToTop();
                    ShHistoryFragment fragment = (ShHistoryFragment) mFragments[1];
                    fragment.scrollToTop();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initFragments() {
        mFragments[0] = new PersonalInfoLeftFragment();
        mFragments[1] = ShHistoryFragment.getInstance(mOtherName);
    }

    private void initTitleBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);

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

    public static void loadImage(final String url, final ImageView iv, final boolean isRound) {
        final MyApplication.VolleyCache mCache = MyApplication.getVolleyCache();
        Bitmap cachedBm = mCache.getBitmap(url);
        if (cachedBm != null) {
            if (isRound) {
                RoundDrawable drawable = new RoundDrawable(cachedBm);
                iv.setImageDrawable(drawable);
            } else {
                iv.setImageBitmap(cachedBm);
            }
        } else {
            ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    if (isRound) {
                        RoundDrawable drawable = new RoundDrawable(bitmap);
                        iv.setImageDrawable(drawable);
                    } else {
                        iv.setImageBitmap(bitmap);
                    }
                    mCache.putBitmap(url, bitmap);
                }
            }, 500, 500, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    iv.setImageResource(R.drawable.picdefault);
                }
            });
            MyApplication.getRequestQueue().add(request);
        }
    }

    public static void startActivity(Context context, String name, int id) {
        Intent intent = new Intent(context, PersonalInfoActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
