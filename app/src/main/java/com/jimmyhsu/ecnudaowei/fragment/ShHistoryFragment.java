package com.jimmyhsu.ecnudaowei.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.MeActivity;
import com.jimmyhsu.ecnudaowei.MyApplication;
import com.jimmyhsu.ecnudaowei.PersonalInfoActivity;
import com.jimmyhsu.ecnudaowei.R;
import com.jimmyhsu.ecnudaowei.SecondHandDetailActivity;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.jimmyhsu.ecnudaowei.fragment.SecondHandFragment.EXTRA_ITEM_NO;

/**
 * Created by jimmyhsu on 2016/10/16.
 */
public class ShHistoryFragment extends Fragment {

    private static final String SECOND_HAND_URL = LoginActivity.BASE_URL + "phpprojects/getsecondhand.php/";
    private static final String TARGET_USER = "target_user";
    private MyApplication mApplication;

    private HistoryAdapter mAdapter;
    private String mTargetUser;
    private int mItemHeight;
    private List<Map<String, Object>> mDatas = new ArrayList<>();
    private boolean isClickable = false;

    @BindView(R.id.id_shhistory_emptytip)
    TextView mTvEmpty;
    @BindView(R.id.id_stickynavlayout_innerscrollview)
    RecyclerView mRecyclerView;
    private Unbinder mUnBinder;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.shhistory_recyclerview, container, false);
        mUnBinder = ButterKnife.bind(this, v);
        return v;
    }

    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    private boolean isTargetUserEnabled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments == null) {
            isTargetUserEnabled = false;
        } else {
            mTargetUser = arguments.getString(TARGET_USER);
            isTargetUserEnabled = true;
        }
        calculateImageHeight();
    }

    public ShHistoryFragment setClickable(boolean clickable) {
        isClickable = clickable;
        return this;
    }

    public static ShHistoryFragment getInstance(String targetUser) {
        ShHistoryFragment instance = new ShHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TARGET_USER, targetUser);
        instance.setArguments(bundle);
        return instance;
    }

    public static ShHistoryFragment getInstance() {
        ShHistoryFragment instance = new ShHistoryFragment();
        return instance;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mApplication = (MyApplication) getActivity().getApplicationContext();
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter = new HistoryAdapter());
        downloadDatas();
    }

    private void downloadDatas() {
        OkHttpUtils.post().url(SECOND_HAND_URL)
                .addParams("user", MyApplication.getInstance(getActivity()).getName())
                .addParams("deviceid", LoginActivity.getAndroidId(getActivity()))
                .addParams("targetuser", isTargetUserEnabled ? mTargetUser : MyApplication.getInstance(getActivity()).getName())
                .tag(this).build().execute(new StringCallback() {
            @Override
            public void onError(Request request, Exception e) {
                Toast.makeText(getActivity(), "请检查网络", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.getInt("status") == 200) {
                        mDatas.clear();
                        JSONArray jsonArray = obj.getJSONArray("items");
                        int size = jsonArray.length();
                        for (int i = 0; i < size; i++) {
                            JSONObject item = (JSONObject) jsonArray.get(i);
                            String url = item.getString("itemimg");
                            url = LoginActivity.BASE_URL + "phpprojects/" + url.split(";")[0];
                            Map<String, Object> map = new HashMap<>();
                            //"name", "head", "image", "time", "title", "price"
                            map.put("name", item.getString("name"));
                            map.put("head", LoginActivity.BASE_URL + "phpprojects/" + item.getString("touxiang"));
                            map.put("image", url);
                            map.put("time", SecondHandDetailActivity.getTimeDes(item.getLong("time")));
                            map.put("title", item.getString("title"));
                            map.put("itemid", item.getInt("id"));
                            map.put("price", item.getInt("price"));
                            mDatas.add(map);
                        }
                        if (mDatas.size() > 0) {
                            mTvEmpty.setVisibility(View.GONE);
                        }
                        mAdapter.notifyItemRangeChanged(mAdapter.getItemCount(), size);
                    }
                } catch (Exception e) {
                    Log.e("ShHistoryFragment", "json error: " + response);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mTargetUser = MyApplication.getInstance(getActivity()).getName();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttpUtils.getInstance().cancelTag(this);
        mUnBinder.unbind();
    }

    public void notifyItemRemoved(int item_id) {
        for (int i = 0; i < mDatas.size(); i++) {
            Map<String, Object> map = mDatas.get(i);
            if ((int)map.get("itemid") == item_id) {
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

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

        private LayoutInflater mInflator = LayoutInflater.from(getActivity());


        @Override
        public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = mInflator.inflate(R.layout.secondhand_item, parent, false);
            if (isClickable) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), SecondHandDetailActivity.class);
                        int id = (int) v.getTag();
                        intent.putExtra(EXTRA_ITEM_NO, id);
                        getActivity().startActivityForResult(intent, MeActivity.REQUEST_SH);//TODO
                    }
                });
            }
            return new HistoryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(HistoryViewHolder holder, int position) {
            Map<String, Object> map = mDatas.get(position);
            holder.mNameTv.setText(isTargetUserEnabled ? mTargetUser : MyApplication.getInstance(getActivity()).getName());//"name", "head", "image", "time", "title", "price"
            holder.mTitleTv.setText((String) map.get("title"));
            holder.mPriceTv.setText("¥" + map.get("price") + " ");
            holder.mTimeTv.setText((String) map.get("time"));
            holder.mItemView.setTag(mDatas.get(position).get("itemid"));
            PersonalInfoActivity.loadImage((String)map.get("head"), holder.mHeadIv, true);
            PersonalInfoActivity.loadImage((String)map.get("image"), holder.mItemIv, false);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }
    class HistoryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.id_shitem_touxiang)
        ImageView mHeadIv;
        @BindView(R.id.id_shitem_nickname)
        TextView mNameTv;
        @BindView(R.id.id_shitem_time)
        TextView mTimeTv;
        @BindView(R.id.id_shitem_image)
        ImageView mItemIv;
        @BindView(R.id.id_shitem_title)
        TextView mTitleTv;
        @BindView(R.id.id_shitem_price)
        TextView mPriceTv;
        View mItemView;

        HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItemIv.getLayoutParams().height = mItemHeight;
            mItemView = itemView;
        }
    }

    private void calculateImageHeight() {
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        mItemHeight = (int) ((outMetrics.widthPixels - LoginActivity.dp2px(12f, getActivity())) / 2f);
    }
}

