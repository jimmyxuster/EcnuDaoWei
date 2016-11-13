package com.jimmyhsu.ecnudaowei;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.jimmyhsu.ecnudaowei.Db.UserInfoProvider;
import com.jimmyhsu.ecnudaowei.Utils.ImageUtils;
import com.jimmyhsu.ecnudaowei.Utils.TextViewUtils;
import com.jimmyhsu.ecnudaowei.View.PositionIndicator;
import com.jimmyhsu.ecnudaowei.View.ScrollViewWithViewPager;
import com.jimmyhsu.ecnudaowei.View.StatusBarCompat;
import com.jimmyhsu.ecnudaowei.fragment.DwFragment;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by jimmyhsu on 2016/10/26.
 */

public class DwDetailActivity extends AppCompatActivity {

    public static final int REQUEST_DW = 101;
    public static final String EXTRA_ITEM_ID = "extra_item_id";

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_PRICE = "extra_price";
    public static final String EXTRA_IMAGES = "extra_images";
    public static final String EXTRA_RELEASE = "extra_release";
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_CONTACT = "extra_contact";
    public static final String EXTRA_USER_ID = "extra_user_id";

    @BindView(R.id.id_dwdetail_map) ImageView mMapIv;
    @BindView(R.id.id_dwdetail_username) TextView mNameTv;
    @BindView(R.id.id_dwdetail_register_time) TextView mReleaseTimeTv;
    @BindView(R.id.id_dwdetail_price) TextView mPriceTv;
    @BindView(R.id.id_dwdetail_head) ImageView mHeadIv;
    @BindView(R.id.id_dwdetail_title) TextView mTitleTv;
    @BindView(R.id.id_dwdetail_content) TextView mContentTv;
    @BindView(R.id.id_dwdetail_image_pager)ViewPager mPager;
    @BindView(R.id.id_dwdetail_pager_indicator) PositionIndicator mIndicator;
    @BindView(R.id.id_dwdetail_contact) TextView mContactTv;
    @BindView(R.id.id_dwdetail_fab) FloatingActionButton mFab;
    @BindView(R.id.id_dwdetail_toolbar) Toolbar mToolBar;
    @BindView(R.id.id_dwdetail_scrollview) ScrollViewWithViewPager mScrollView;

    private List<String> mImgUrls = new ArrayList<>();
    private String mContact;
    private int id;
    private int userId;
    private String mUsername;
    private boolean isFabShown = false;

    public static void startActivity(Context context, String username, String title,
                                     String content, String priceStr, String imageUrls, String time,
                                     String contact, int id, int userid) {
        Intent intent = new Intent(context, DwDetailActivity.class);
        intent.putExtra(EXTRA_NAME, username);
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_PRICE, priceStr);
        intent.putExtra(EXTRA_IMAGES, imageUrls);
        intent.putExtra(EXTRA_RELEASE, time);
        intent.putExtra(EXTRA_CONTACT, contact);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_USER_ID, userid);
        context.startActivity(intent);
    }

    @OnClick(R.id.id_dwdetail_contact)
    public void onClick() {
        if (mContact != null) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("联系方式")
                    .setMessage(mContact)
                    .setPositiveButton("好", null)
                    .create();
            dialog.show();
        }
    }

    private boolean isDeleting = false;
    @OnClick(R.id.id_dwdetail_fab)
    public void deleteDw() {
        if (id >= 0) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("删除")
                    .setMessage("是否要删除这条鱼跃？(修改功能后续上线)")
                    .setNegativeButton("否", null)
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isDeleting) {
                                return;
                            }
                            isDeleting = true;
                            deleteFromNet();
                        }
                    })
                    .setCancelable(false)
                    .create();
            dialog.show();
        }
    }

    private void deleteFromNet() {
        OkHttpUtils.post().url(LoginActivity.BASE_URL + "phpprojects/remove.php")
                .addParams("username", mUsername)
                .addParams("deviceid", MyApplication.getInstance(this).getAndroidId(this))
                .addParams("type", "dw")
                .addParams("id", String.valueOf(id))
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        isDeleting = false;
                        Snackbar.make(mContactTv, "网络异常，删除失败", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            Toast.makeText(DwDetailActivity.this, "删除成功!请刷新查看", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_ITEM_ID, id);
                            finish();
                        } else if (response.equals("invalid user")){
                            Snackbar.make(mContactTv
                                    , R.string.prompt_force_logout, Snackbar.LENGTH_SHORT).show();
                            isDeleting = false;
                        } else {
                            Snackbar.make(mContactTv, "服务器去火星了~", Snackbar.LENGTH_SHORT).show();
                            isDeleting = false;
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_dw_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolBar);
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mToolBar.getLayoutParams();
        param.setMargins(0, StatusBarCompat.getStatusBarHeight(this), 0, 0);
        mToolBar.setLayoutParams(param);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        Drawable homeAsUpDrawable = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        homeAsUpDrawable.setColorFilter(0xffffff, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(homeAsUpDrawable);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        try {
            FileInputStream fis = openFileInput(DwFragment.TEMP_FILE);
            Bitmap mapBm = BitmapFactory.decodeStream(fis);
            mMapIv.setImageBitmap(mapBm);
            fis.close();
            File file = new File(getFilesDir() + "/" + DwFragment.TEMP_FILE);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
//            Log.e("dwdetailactivity", "file not exists");
            mMapIv.setVisibility(View.GONE);
        }
        mUsername = intent.getStringExtra(EXTRA_NAME);
        id = intent.getIntExtra(EXTRA_ID, -1);
        userId = intent.getIntExtra(EXTRA_USER_ID, -1);
        if (mUsername != null) {
            mNameTv.setText(mUsername);
            loadHead();
        }
        mContact = intent.getStringExtra(EXTRA_CONTACT);
        TextViewUtils.bindTextToTextView(intent.getStringExtra(EXTRA_TITLE), mTitleTv);
        TextViewUtils.bindTextToTextView(intent.getStringExtra(EXTRA_CONTENT), mContentTv);
        TextViewUtils.bindTextToTextView(intent.getStringExtra(EXTRA_PRICE), mPriceTv);
        String imgUrls = intent.getStringExtra(EXTRA_IMAGES);
        if (imgUrls != null && !imgUrls.equals("no_image")) {
            mImgUrls = new ArrayList<>(Arrays.asList(imgUrls.split(";")));
            initPager();
        } else {
            mPager.setVisibility(View.GONE);
            mIndicator.setVisibility(View.GONE);
        }
        String releaseTime = intent.getStringExtra(EXTRA_RELEASE);
        if (releaseTime != null) {
            mReleaseTimeTv.setText(releaseTime);
        }

    }

    private void showFab() {
        isFabShown = true;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1)
                .setDuration(300);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                mFab.setScaleX(val);
                mFab.setScaleY(val);
            }
        });
        mFab.setVisibility(View.VISIBLE);
        animator.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUsername.equals(MyApplication.getInstance(this).getName())) {
            showFab();
            mScrollView.setOnSrcollChangeListner(new ScrollViewWithViewPager.ScrollViewListener() {
                @Override
                public void onScrollChanged(ScrollViewWithViewPager scrollView, int x, int y, int oldx, int oldy) {
                    if (y > 0 && isFabShown) {
                        hideFab();
                    } else if (y <= 0 && !isFabShown) {
                        showFab();
                    }
                }
            });
        }
    }

    private void hideFab() {
        isFabShown = false;
        ValueAnimator animator = ValueAnimator.ofFloat(1, 0)
                .setDuration(300);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                mFab.setScaleX(val);
                mFab.setScaleY(val);
            }
        });
        animator.start();
    }

    private void initPager() {
        mIndicator.setTotalNumber(mImgUrls.size());
        mIndicator.setCurrent(0);
        mPager.setAdapter(new PagerAdapter() {
            private ImageLoader mImageLoader = new ImageLoader(MyApplication.getRequestQueue()
                    , MyApplication.getVolleyCache());
            @Override
            public int getCount() {
                return mImgUrls.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                final ImageView iv = new ImageView(DwDetailActivity.this);
                iv.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.ImageListener imageListenerItem = ImageLoader.getImageListener(iv
                        , R.drawable.picdefault, R.drawable.picerror);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImgUrls != null && mImgUrls.size() > position){
                            ActivityOptionsCompat options =
                                    ActivityOptionsCompat.makeScaleUpAnimation(iv,
                                            (int)iv.getWidth()/2, (int)iv.getHeight()/2,
                                            0, 0);
                            BigPhotoActivity.startActivity(DwDetailActivity.this
                                    , LoginActivity.BASE_URL + "phpprojects/"+ mImgUrls.get(position),
                                    options.toBundle());
                        }
                    }
                });
                mImageLoader.get(LoginActivity.BASE_URL + "phpprojects/"+ mImgUrls.get(position), imageListenerItem);
                container.addView(iv);
                return iv;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View)object);
            }
        });
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mIndicator.setCurrent(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPager.setOffscreenPageLimit(5);
    }


    private void loadHead() {
        if (userId > 0) {
            String url = LoginActivity.BASE_URL + "phpprojects/userimage/" + userId + ".jpg";
            ImageUtils.downloadToImageView(url, mHeadIv);
        }
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
