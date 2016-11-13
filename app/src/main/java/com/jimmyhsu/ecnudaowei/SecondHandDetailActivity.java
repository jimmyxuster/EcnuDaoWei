package com.jimmyhsu.ecnudaowei;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.jimmyhsu.ecnudaowei.View.PositionIndicator;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.jimmyhsu.ecnudaowei.View.ScrollViewWithViewPager;
import com.jimmyhsu.ecnudaowei.View.StatusBarCompat;
import com.jimmyhsu.ecnudaowei.fragment.SecondHandFragment;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jimmyhsu on 2016/10/11.
 */
public class SecondHandDetailActivity extends AppCompatActivity {

    private static final String DETAIL_URL = LoginActivity.BASE_URL + "phpprojects/getshdetail.php/";
    public static final String[] CONDITIONS = new String[]{"极差", "普通", "良好", "非常好"
            , "几近全新", "全新"};
    private MyApplication mApplication;
    private int mItemNumber;
    private List<String> mImageUrls = new ArrayList<>();
    private ViewPager mPager;
    private PositionIndicator mIndicator;
    private PagerAdapter mAdapter;
    private String mName;

    @BindView(R.id.id_shdetail_head)
    ImageView headIv;
    @BindView(R.id.id_shdetail_username)
    TextView usernameTv;
    @BindView(R.id.id_shdetail_time)
    TextView timeTv;
    @BindView(R.id.id_shdetail_price)
    TextView priceTv;
    @BindView(R.id.id_shdetail_title)
    TextView bigTitleTv;
    @BindView(R.id.id_shdetail_title_small)
    TextView smallTitleTv;
    @BindView(R.id.id_shdetail_desc)
    TextView contentTv;
    @BindView(R.id.id_shdetail_count)
    TextView countTv;
    @BindView(R.id.id_shdetail_condition)
    TextView conditionTv;
    @BindView(R.id.id_shdetail_postage)
    TextView postageTv;
    @BindView(R.id.id_shdetail_comment_editor)
    EditText commentEdt;
    @BindView(R.id.id_shdetail_comments)
    LinearLayout commentWrapper;
    @BindView(R.id.id_shdetail_send_comment)
    Button commentBtn;
    @BindView(R.id.id_shdetail_contact)
    TextView contactTv;
    @BindView(R.id.id_shdetail_link)
    TextView linkTv;
    @BindView(R.id.id_shdetail_fab)
    FloatingActionButton fab;
    @BindView(R.id.id_shdetail_toolbar)
    Toolbar mToolBar;
    @BindView(R.id.id_shdetail_scrollview)
    ScrollViewWithViewPager mScrollView;
    private boolean isFabShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_secondhand_detail);
        ButterKnife.bind(this);
        StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimaryDark));
        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) mToolBar.getLayoutParams();
        param.setMargins(0, StatusBarCompat.getStatusBarHeight(this), 0, 0);
        mToolBar.setLayoutParams(param);
        setSupportActionBar(mToolBar);
        Drawable homeAsUpDrawable = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        homeAsUpDrawable.setColorFilter(0xffffff, PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(homeAsUpDrawable);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mItemNumber = getIntent().getIntExtra(SecondHandFragment.EXTRA_ITEM_NO, -1);
        mApplication = (MyApplication) getApplicationContext();
        Drawable drawableLeft = ContextCompat.getDrawable(this, R.drawable.userdefault);
        drawableLeft.setBounds(0, 0, 80, 80);
        commentEdt.setCompoundDrawables(drawableLeft, null, null, null);
        mIndicator = (PositionIndicator) findViewById(R.id.id_shdetail_pager_indicator);
        mPager = (ViewPager) findViewById(R.id.id_shdetail_pager);

        setPagerAdapter();
        downloadData();
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment = commentEdt.getText().toString().trim();
                if (TextUtils.isEmpty(comment)) {
                    Snackbar.make(commentEdt, "评论不能为空", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                OkHttpUtils
                        .post()
                        .url(LoginActivity.BASE_URL + "phpprojects/uploadcomment.php")
                        .addParams("name", mApplication.getName())
                        .addParams("itemid", String.valueOf(mItemNumber))
                        .addParams("comment", comment)
                        .addParams("itemtype", "2")
                        .addParams("deviceid", LoginActivity.getAndroidId(SecondHandDetailActivity.this))
                        .tag(SecondHandDetailActivity.this)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Request request, Exception e) {
                                Snackbar.make(commentEdt, "评论发送失败", Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(String response) {
//                                Log.e("detail", response);
                                if (response.equals("success")) {
                                    commentEdt.setText("");
                                    View commentView = LayoutInflater.from(SecondHandDetailActivity.this)
                                            .inflate(R.layout.comment_item,
                                            commentWrapper, false);
                                    commentWrapper.addView(commentView);
                                    ImageView head = (ImageView) commentView.findViewById(R.id.id_comment_head);
                                    if (mUserHeadBm != null) {
                                        head.setImageDrawable(new RoundDrawable(mUserHeadBm));
                                    } else {
                                        loadUserImage(LoginActivity.BASE_URL + "phpprojects/userimage/" +
                                            mApplication.getName() + ".jpg", head);
                                    }
                                    TextView nameTv = (TextView) commentView.findViewById(R.id.id_comment_name);
                                    TextView timeTv = (TextView) commentView.findViewById(R.id.id_comment_time);
                                    TextView contentTv = (TextView) commentView.findViewById(R.id.id_comment_content);
                                    nameTv.setText(mApplication.getName());
                                    timeTv.setText("0分钟前");
                                    contentTv.setText(comment);
                                }else{
                                    Snackbar.make(commentEdt, "您没有权限评论", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void downloadData() {
        OkHttpUtils
                .post()
                .url(DETAIL_URL)
                .addParams("name", mApplication.getName())
                .addParams("deviceid", LoginActivity.getAndroidId(this))
                .addParams("itemno", String.valueOf(mItemNumber))
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Toast.makeText(SecondHandDetailActivity.this, "服务器君喝茶去了~", Toast.LENGTH_SHORT).show();
                    }
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
                    @Override
                    public void onResponse(String response) {
                        try {
                            final JSONObject obj = new JSONObject(response);
                            if (obj.getInt("status") == 200) {
                                mName = obj.getString("name");
                                final int itemId = obj.getInt("id");
                                usernameTv.setText(mName);
                                initFab();
                                setScrollAdapter();
                                priceTv.setText(obj.getString("price") + " 元");
                                timeTv.setText(dateFormat.format(new Date(obj.getLong("time"))));
                                mImageUrls = new ArrayList<>(Arrays.asList(obj.getString("urls").split(";")));
                                loadItemImages();
                                String userheadUrl = LoginActivity.BASE_URL + "phpprojects/" + obj.getString("userimage");
                                loadUserImage(userheadUrl, headIv);
                                bigTitleTv.setText(obj.getString("title"));
                                smallTitleTv.setText(obj.getString("title"));
                                contentTv.setText(obj.getString("content"));
                                countTv.setText("× " + String.valueOf(obj.getInt("count")));
                                conditionTv.setText(CONDITIONS[obj.getInt("condition") - 1]);
                                postageTv.setText(obj.getInt("postage") + " 元");
                                final String link = obj.getString("link");
                                if ("no_link".equals(link)) {
                                    linkTv.setVisibility(View.GONE);
                                } else {
                                    linkTv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            GeneralWebViewActivity.startActivity(SecondHandDetailActivity.this, link);
                                        }
                                    });
                                }
                                loadComments(obj.getJSONArray("comment"), commentWrapper);
                                contactTv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PersonalInfoActivity.startActivity(SecondHandDetailActivity.this, mName, itemId);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("detail", "json error: " + response);
                        }
                    }
                });
    }

    private void setScrollAdapter() {
        if (mName.equals(mApplication.getName())) {
            mScrollView.setOnSrcollChangeListner(new ScrollViewWithViewPager.ScrollViewListener() {
                @Override
                public void onScrollChanged(ScrollViewWithViewPager scrollView, int x, int y, int oldx, int oldy) {
                    if (y > 0 && isFabShown) {
                        isFabShown = false;
                        hideFab();
                    } else if (y <=0 && !isFabShown) {
                        isFabShown = true;
                        initFab();
                    }
                }
            });
        }
    }

    private void hideFab() {
        if (fab.getVisibility() == View.VISIBLE) {
            isFabShown = false;
            ValueAnimator animator = ValueAnimator.ofFloat(1, 0)
                    .setDuration(300);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float val = (float) animation.getAnimatedValue();
                    fab.setScaleX(val);
                    fab.setScaleY(val);
                }
            });
            animator.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    private void initFab() {
        if (mName != null && mName.equals(mApplication.getName())) {
            isFabShown = true;
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1)
                    .setDuration(300);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float val = (float) animation.getAnimatedValue();
                    fab.setScaleX(val);
                    fab.setScaleY(val);
                }
            });
            fab.setVisibility(View.VISIBLE);
            animator.start();

        }
    }

    private void loadComments(JSONArray comment, LinearLayout commentWrapper) {
        for (int i = 0; i < comment.length(); i++) {
            try {
                JSONObject c = (JSONObject) comment.get(i);
                View commentView = LayoutInflater.from(SecondHandDetailActivity.this)
                        .inflate(R.layout.comment_item,
                                commentWrapper, false);
                commentWrapper.addView(commentView);
                ImageView head = (ImageView) commentView.findViewById(R.id.id_comment_head);
                loadUserImage(LoginActivity.BASE_URL + "phpprojects/" + c.getString("userhead"), head);
                TextView nameTv = (TextView) commentView.findViewById(R.id.id_comment_name);
                TextView timeTv = (TextView) commentView.findViewById(R.id.id_comment_time);
                TextView contentTv = (TextView) commentView.findViewById(R.id.id_comment_content);
                nameTv.setText(c.getString("name"));
                timeTv.setText(getTimeDes(c.getLong("time")));
                contentTv.setText(c.getString("content"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap mUserHeadBm;

    private void loadUserImage(final String userheadUrl, final ImageView headIv) {
        ImageRequest request = new ImageRequest(userheadUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                RoundDrawable drawable = new RoundDrawable(bitmap);
                headIv.setImageDrawable(drawable);
                mUserHeadBm = bitmap;
                MyApplication.getVolleyCache().putBitmap(userheadUrl, bitmap);
            }
        }, 500, 500, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                headIv.setImageResource(R.drawable.userdefault);
            }
        });
        MyApplication.getRequestQueue().add(request);
    }

    private void setPagerAdapter() {
        mPager.setAdapter(mAdapter = new PagerAdapter() {
            private ImageLoader mImageLoader = new ImageLoader(MyApplication.getRequestQueue()
                    , MyApplication.getVolleyCache());
            @Override
            public int getCount() {
                return mImageUrls.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                final ImageView imageView = new ImageView(SecondHandDetailActivity.this);
                imageView.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageLoader.ImageListener imageListenerItem = ImageLoader.getImageListener(imageView
                        , R.drawable.picdefault, R.drawable.picerror);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mImageUrls != null && mImageUrls.size() > position){
                            ActivityOptionsCompat options =
                                    ActivityOptionsCompat.makeScaleUpAnimation(imageView,
                                            (int)imageView.getWidth()/2, (int)imageView.getHeight()/2,
                                            0, 0);//拉伸开始的区域大小，这里用（0，0）表示从无到全屏
                            BigPhotoActivity.startActivity(SecondHandDetailActivity.this, LoginActivity.BASE_URL + "phpprojects/"+ mImageUrls.get(position), options.toBundle());
                        }
                    }
                });
                mImageLoader.get(LoginActivity.BASE_URL + "phpprojects/"+ mImageUrls.get(position), imageListenerItem);
                container.addView(imageView);
                return imageView;
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
        mPager.setOffscreenPageLimit(8);
    }

    private void loadItemImages() {
        mAdapter.notifyDataSetChanged();
        mIndicator.setTotalNumber(mImageUrls.size());
        mIndicator.setCurrent(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static String getTimeDes(long ms) {
        int ss = 1000;
        long originalMs = ms;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;
        ms = System.currentTimeMillis() - ms;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;

        if(day>0){
            if (day == 1){
                return "昨天";
            }
            if (day > 7) {
                return new SimpleDateFormat("MM-dd").format(new Date(originalMs));
            }else {
                return day + "天前";
            }
        }
        if(hour>0){
            return hour + "小时前";
        }
        if(minute>=0){
            return minute + "分钟前";
        }
        return "未知时间";
    }
    private boolean isDeleting = false;
    @OnClick(R.id.id_shdetail_fab)
    public void deleteSh() {
        if (mItemNumber >= 0) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("删除")
                    .setMessage("是否要删除这条二手信息？(修改功能后续上线)")
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
        OkHttpUtils.post()
                .url(LoginActivity.BASE_URL + "phpprojects/remove.php")
                .addParams("username", mApplication.getName())
                .addParams("deviceid", mApplication.getAndroidId(this))
                .addParams("type", "sh")
                .addParams("id", String.valueOf(mItemNumber))
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        isDeleting = false;
                        Snackbar.make(conditionTv, "网络异常", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        isDeleting = false;
                        if (response.equals("success")) {
                            Toast.makeText(SecondHandDetailActivity.this, "删除成功!",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            intent.putExtra(MeActivity.EXTRA_ITEM_ID, mItemNumber);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else if (response.equals("invalid user")) {
                            Snackbar.make(conditionTv
                                    , R.string.prompt_force_logout, Snackbar.LENGTH_SHORT).show();
                         } else {
                            Snackbar.make(conditionTv, "服务器去火星了~", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
