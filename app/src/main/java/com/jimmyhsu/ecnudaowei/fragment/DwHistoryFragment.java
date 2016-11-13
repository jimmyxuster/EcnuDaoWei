package com.jimmyhsu.ecnudaowei.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jimmyhsu.ecnudaowei.DwDetailActivity;
import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.MyApplication;
import com.jimmyhsu.ecnudaowei.R;
import com.jimmyhsu.ecnudaowei.SecondHandDetailActivity;
import com.jimmyhsu.ecnudaowei.Utils.ImageUtils;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;

/**
 * Created by jimmyhsu on 2016/10/28.
 */

public class DwHistoryFragment extends Fragment {

    private static final String TAG = "DwHistoryFragment";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ITEM_ID = "item_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_PRICE = "price";
    private static final String KEY_IMAGE_URLS = "image_urls";
    private static final String KEY_TIME_STR = "time_str";
    private static final String KEY_CONTACT = "contact";


    private String mName;


    @BindView(R.id.id_dwhistory_rv)
    RecyclerView mRv;
    @BindView(R.id.id_dwhistory_emptytip)
    TextView mTvEmpty;
    private DwHistoryAdapter mAdapter;

    private List<Map<String, Object>> mDatas = new ArrayList<>();
    private Unbinder mUnBinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_dwhistory, container, false);
        mUnBinder = ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
    }

    private void initAdapter() {
        mRv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRv.setItemAnimator(new DefaultItemAnimator());
        mRv.setAdapter(mAdapter = new DwHistoryAdapter());
    }

    @Override
    public void onResume() {
        super.onResume();
        initDatas();
    }

    private void initDatas() {
        mName = MyApplication.getInstance(getActivity()).getName();
        OkHttpUtils.post().url(LoginActivity.BASE_URL + "phpprojects/getmydw.php")
                .addParams("username", mName)
                .addParams("deviceid", MyApplication.getInstance(getActivity()).getAndroidId(getActivity()))
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mRv, "网络异常", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            mDatas.clear();
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                Map<String, Object> map = new HashMap<>();
                                map.put(KEY_ITEM_ID, obj.getInt("id"));
                                map.put(KEY_USER_ID, obj.getInt("userid"));
                                map.put(KEY_TITLE, obj.getString("title"));
                                map.put(KEY_CONTENT, obj.getString("content"));
                                map.put(KEY_PRICE, obj.getInt("price"));
                                map.put(KEY_IMAGE_URLS, obj.getString("imageurls"));
                                map.put(KEY_CONTACT, obj.getString("contact"));
                                map.put(KEY_TIME_STR, SecondHandDetailActivity.getTimeDes(obj.getLong("time")));
                                mDatas.add(map);
                            }
                            if (mDatas.size() > 0) {
                                mTvEmpty.setVisibility(View.GONE);
                            }
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "json err: " + response);
                        }
                    }
                });
    }

    void onItemClick(int position) {
        Map<String, Object> map = mDatas.get(position);
        navigateToDetailActivity(mName,
                (String)map.get(KEY_TITLE), (String)map.get(KEY_CONTENT)
                , map.get(KEY_PRICE) + "元",
                (String)map.get(KEY_IMAGE_URLS), (String)map.get(KEY_TIME_STR)
                , (String)map.get(KEY_CONTACT),
                (int)map.get(KEY_ITEM_ID), (int)map.get(KEY_USER_ID));
    }

    public void removeItemByItemId(int itemId) {
        for (int i = 0; i < mDatas.size(); i++) {
            Map<String, Object> map = mDatas.get(i);
            if ((int)map.get(KEY_ITEM_ID) == itemId) {
                mDatas.remove(i);
                if (mAdapter != null) {
                    mAdapter.notifyItemRemoved(i);
                }
                if (mDatas.size() == 0) {
                    mTvEmpty.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    private void navigateToDetailActivity(String username, String title,
                                          String content, String priceStr, String imageUrls, String time,
                                          String contact, int id, int userid) {
        Intent intent = new Intent(getActivity(), DwDetailActivity.class);
        intent.putExtra(DwDetailActivity.EXTRA_NAME, username);
        intent.putExtra(DwDetailActivity.EXTRA_CONTENT, content);
        intent.putExtra(DwDetailActivity.EXTRA_TITLE, title);
        intent.putExtra(DwDetailActivity.EXTRA_PRICE, priceStr);
        intent.putExtra(DwDetailActivity.EXTRA_IMAGES, imageUrls);
        intent.putExtra(DwDetailActivity.EXTRA_RELEASE, time);
        intent.putExtra(DwDetailActivity.EXTRA_CONTACT, contact);
        intent.putExtra(DwDetailActivity.EXTRA_ID, id);
        intent.putExtra(DwDetailActivity.EXTRA_USER_ID, userid);
        getActivity().startActivityForResult(intent, DwDetailActivity.REQUEST_DW);
    }

    class DwHistoryAdapter extends RecyclerView.Adapter<DwHistoryViewHolder> {
        private LayoutInflater mInflater = LayoutInflater.from(getActivity());


        @Override
        public DwHistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = mInflater.inflate(R.layout.dwhistory_item, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    onItemClick(pos);
                }
            });
            return new DwHistoryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(DwHistoryViewHolder holder, int position) {
            Map<String, Object> item = mDatas.get(position);
            ImageUtils.downloadToImageView(LoginActivity.BASE_URL + "phpprojects/userimage/" +
                    item.get(KEY_USER_ID) + ".jpg", holder.mIvHead);
            holder.mTvName.setText(mName);
            holder.mTvTitle.setText((String) item.get(KEY_TITLE));
            holder.mTvContent.setText((String) item.get(KEY_CONTENT));
            holder.mTvPrice.setText(item.get(KEY_PRICE) + "元");
            holder.mItemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }
    class DwHistoryViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.id_dwhistory_head)
        ImageView mIvHead;
        @BindView(R.id.id_dwhistory_name)
        TextView mTvName;
        @BindView(R.id.id_dwhistory_title)
        TextView mTvTitle;
        @BindView(R.id.id_dwhistory_content)
        TextView mTvContent;
        @BindView(R.id.id_dwhistory_price)
        TextView mTvPrice;

        View mItemView;
        DwHistoryViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}
