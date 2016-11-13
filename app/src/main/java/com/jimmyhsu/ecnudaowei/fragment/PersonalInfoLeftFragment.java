package com.jimmyhsu.ecnudaowei.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jimmyhsu on 2016/10/16.
 */
public class PersonalInfoLeftFragment extends Fragment {

    private ListView mContainer;
    private List<Map<String, String>> mDatas = new ArrayList<>();
    private SimpleAdapter mAdapter;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContainer = new ListView(getActivity());
        mContainer.setId(R.id.id_stickynavlayout_innerscrollview);
        mContainer.setPadding(0, LoginActivity.dp2px(10, getActivity()), 0, 0);
        mContainer.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        return mContainer;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new SimpleAdapter(getActivity(), mDatas, R.layout.item_key_value,
                new String[]{"key", "value"}, new int[]{R.id.id_kv_key, R.id.id_kv_value});
        Map<String, String> map = new HashMap<>();
        map.put("key", "key");
        map.put("value", "value");
        mDatas.add(map);
        mContainer.setAdapter(mAdapter);
    }

    public void addPersonInfo(String key, String value) {
        if (mDatas.size() ==  1) {
            mDatas.clear();
        }
        Map<String, String> map = new HashMap<>();
        map.put("key", key);
        map.put("value", value);
        mDatas.add(map);
        mAdapter.notifyDataSetChanged();
    }


}
