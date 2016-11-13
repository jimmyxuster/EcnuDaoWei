package com.jimmyhsu.ecnudaowei.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.jimmyhsu.ecnudaowei.Bean.SHItemBriefBean;
import com.jimmyhsu.ecnudaowei.GeneralWebViewActivity;
import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.MyApplication;
import com.jimmyhsu.ecnudaowei.R;
import com.jimmyhsu.ecnudaowei.RegisterActivity;
import com.jimmyhsu.ecnudaowei.SecondHandDetailActivity;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;


/**
 * Created by jimmyhsu on 2016/10/9.
 */
public class SecondHandFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "SecondHandFragment";
    private static final String SECOND_HAND_URL = LoginActivity.BASE_URL + "phpprojects/getsecondhand.php/";
    public static final String EXTRA_ITEM_NO = "extra_item_no";
    private static final String[] STATIC_URLS = new String[]{"http://115.159.79.195:8080/wordpress/2016/11/05/shrule/"
            , "http://115.159.79.195:8080/wordpress/2016/11/05/announcement"};

    private List<SHItemBriefBean> mDatas = new ArrayList<>();

    @BindView(R.id.id_secondhand_srl)
    SwipeRefreshLayout mSrl;
    @BindView(R.id.id_secondhand_grid)
    GridView mMainGrid;


    private LayoutInflater mInflater;
    private MyGridAdapter mAdapter;
    private int mItemHeight;
    private int mPage = 1;
    private boolean mHasMore = true;
    private MyApplication mApplication;
    private boolean isOnce = false;
    private Unbinder mUnBinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_second_hand, container, false);
        mUnBinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInflater = LayoutInflater.from(getActivity());
        mApplication = (MyApplication) getActivity().getApplicationContext();
        getActivity().setTitle("鱼尾巴");
        mSrl.setColorSchemeColors(ContextCompat.getColor(getContext(), android.R.color.holo_blue_bright),
                ContextCompat.getColor(getContext(), android.R.color.holo_green_light),
                ContextCompat.getColor(getContext(), android.R.color.holo_orange_light),
                ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
        mSrl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initDatas(getActivity());
            }
        });
        calculateImageHeight();
        mAdapter = new MyGridAdapter(mDatas);
//        initDatas();
        mMainGrid.setAdapter(mAdapter);
        mMainGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    //到底
                    if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                        loadMore();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void loadMore() {
        if (mHasMore) {
            mSrl.setRefreshing(true);
            mPage++;
            OkHttpUtils.post().url(SECOND_HAND_URL)
                    .addParams("user", mApplication.getName())
                    .addParams("deviceid", LoginActivity.getAndroidId(getActivity()))
                    .addParams("page", String.valueOf(mPage))
                    .addParams("targetuser", "")
                    .tag(this).build().execute(new StringCallback() {
                @Override
                public void onError(Request request, Exception e) {
                    Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(String response) {
//                Log.e(TAG, response);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getInt("status") == 200) {
                            mHasMore = obj.getBoolean("hasMore");
                            JSONArray jsonArray = obj.getJSONArray("items");
//                            Log.e(TAG, "loaded " + jsonArray.length() + " items");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject item = (JSONObject) jsonArray.get(i);
                                String url = item.getString("itemimg");
                                url = LoginActivity.BASE_URL + "phpprojects/" + url.split(";")[0];
                                String headUrl = item.getString("touxiang");
                                headUrl = LoginActivity.BASE_URL + "phpprojects/" + headUrl;
                                SHItemBriefBean bean = new SHItemBriefBean(item.getInt("id"),
                                        item.getString("name"),
                                        item.getLong("time"),
                                        headUrl,
                                        url,
                                        item.getInt("price"),
                                        item.getString("title"));
                                mDatas.add(bean);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        mSrl.setRefreshing(false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mSrl.setRefreshing(false);
                        Log.e(TAG, "json 转换异常:"+response);
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isOnce) {
            initDatas(getActivity());
            isOnce = true;
        }
    }

    private void calculateImageHeight() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mItemHeight = (int) ((outMetrics.widthPixels - LoginActivity.dp2px(12f, getActivity())) / 2f);
    }


    public void initDatas(final Context context) {
        mPage = 1;
        mApplication = (MyApplication) context.getApplicationContext();
        if (mApplication.getName() == null) {
            SharedPreferences sp = getActivity().getSharedPreferences(LoginActivity.SP_NAME, Context.MODE_PRIVATE);
            mApplication.setName(sp.getString(LoginActivity.SP_KEY_NAME, ""));
        }

        OkHttpUtils.post().url(SECOND_HAND_URL)
                .addParams("user", mApplication.getName())
                .addParams("deviceid", LoginActivity.getAndroidId(context))
                .addParams("page", String.valueOf(mPage))
                .addParams("targetuser", "")
                .tag(this).build().execute(new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(context, "请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.getInt("status") == 200) {
                        mHasMore = obj.getBoolean("hasMore");
                        mDatas.clear();
                        mDatas.add(new SHItemBriefBean());
                        mDatas.add(new SHItemBriefBean());
                        JSONArray jsonArray = obj.getJSONArray("items");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject item = (JSONObject) jsonArray.get(i);
                            String url = item.getString("itemimg");
                            url = LoginActivity.BASE_URL + "phpprojects/" + url.split(";")[0];
                            SHItemBriefBean bean = new SHItemBriefBean(item.getInt("id"),
                                    item.getString("name"),
                                    item.getLong("time"),
                                    LoginActivity.BASE_URL + "phpprojects/" + item.getString("touxiang"),
                                    url,
                                    item.getInt("price"),
                                    item.getString("title"));
                            mDatas.add(bean);
                        }
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (mSrl != null && mSrl.isRefreshing()) {
                        mSrl.setRefreshing(false);
                    }
                    isOnce = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    LoginActivity.goToLogin(context, true);
                    Log.e(TAG, "json 转换异常:"+response);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    @OnItemClick(R.id.id_secondhand_grid)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position > 1) {
            Intent intent = new Intent(getActivity(), SecondHandDetailActivity.class);
            intent.putExtra(EXTRA_ITEM_NO, mDatas.get(position).getId());
            startActivity(intent);
        } else {
            GeneralWebViewActivity.startActivity(getActivity(), STATIC_URLS[position]);
        }
    }

    public void refreshData(Context context) {
        initDatas(context);
    }

    class MyGridAdapter extends BaseAdapter {

        private List<SHItemBriefBean> mDatas;
        private ImageLoader mImageLoader;
        private RequestQueue mQueue;
        private MyApplication.VolleyCache mCache;

        public MyGridAdapter(List<SHItemBriefBean> mDatas) {
            this.mDatas = mDatas;
            mQueue = MyApplication.getRequestQueue();
            mCache = MyApplication.getVolleyCache();
            mImageLoader = new ImageLoader(MyApplication.getRequestQueue(), mCache);
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0 || position == 1) {
                convertView = mInflater.inflate(R.layout.frag_secondhand_header, parent, false);
                TextView tv = (TextView) convertView.findViewById(R.id.id_secondhand_header);
                tv.setText(position == 0 ? R.string.prompt_transact_rule
                        : R.string.prompt_transact_announce);
                return convertView;
            }
            final ViewHolder vh;
            if (convertView == null || convertView.getTag() == null) {
                convertView = mInflater.inflate(R.layout.secondhand_item, parent, false);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            }else{
                vh = (ViewHolder) convertView.getTag();
            }
            final SHItemBriefBean bean = mDatas.get(position);
            vh.mTouXiang.setImageResource(R.drawable.userdefault);
            vh.mItemIv.setImageResource(R.drawable.picdefault);
            vh.mNickname.setText(bean.getName());
            vh.mTime.setText(bean.getTime());
            vh.mTitle.setText(bean.getTitle());
            vh.mPriceTv.setText("¥" + String.valueOf(bean.getPrice()) + " ");//for italic text
            if (vh.mItemIv.getLayoutParams().height != mItemHeight) {
                vh.mItemIv.getLayoutParams().height = mItemHeight;
            }
            //images
            Bitmap cachedBm = mCache.getBitmap(bean.getTouXiangUrl());
            if (cachedBm != null) {
                RoundDrawable drawable = new RoundDrawable(cachedBm);
                vh.mTouXiang.setImageDrawable(drawable);
            } else {
                ImageRequest request = new ImageRequest(bean.getTouXiangUrl(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        RoundDrawable drawable = new RoundDrawable(bitmap);
                        vh.mTouXiang.setImageDrawable(drawable);
                        mCache.putBitmap(bean.getTouXiangUrl(), bitmap);
                    }
                }, 500, 500, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        vh.mTouXiang.setImageResource(R.drawable.userdefault);
                    }
                });
                mQueue.add(request);
            }

            ImageLoader.ImageListener imageListenerItem = ImageLoader.getImageListener(vh.mItemIv
                    , R.drawable.picdefault, R.drawable.picerror);
            mImageLoader.get(bean.getImageUrl(), imageListenerItem);



            return convertView;
        }
    }
    class ViewHolder {
        @BindView(R.id.id_shitem_touxiang)
        ImageView mTouXiang;
        @BindView(R.id.id_shitem_nickname)
        TextView mNickname;
        @BindView(R.id.id_shitem_time)
        TextView mTime;
        @BindView(R.id.id_shitem_title)
        TextView mTitle;
        @BindView(R.id.id_shitem_image)
        ImageView mItemIv;
        @BindView(R.id.id_shitem_price)
        TextView mPriceTv;

        ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }


}
