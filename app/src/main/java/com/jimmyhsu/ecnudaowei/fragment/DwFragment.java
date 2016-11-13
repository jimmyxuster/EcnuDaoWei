package com.jimmyhsu.ecnudaowei.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.jimmyhsu.ecnudaowei.DwDetailActivity;
import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.MyApplication;
import com.jimmyhsu.ecnudaowei.PersonalInfoActivity;
import com.jimmyhsu.ecnudaowei.R;
import com.jimmyhsu.ecnudaowei.SecondHandDetailActivity;
import com.jimmyhsu.ecnudaowei.View.DwItemView;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by jimmyhsu on 2016/10/21.
 */
public class DwFragment extends Fragment implements LocationSource, AMapLocationListener, TabLayout.OnTabSelectedListener {

    private static final String KEY_NAME = "key_name";
    private static final String KEY_TITLE = "key_title";
    private static final String KEY_PRICE = "key_price";
    private static final String KEY_TYPE = "key_type";
    private static final String KEY_DESC = "key_desc";
    private static final String KEY_HEAD_URL = "key_head_url";
    private static final String KEY_LONGTITUDE = "key_longtitude";
    private static final String KEY_LATITUDE = "key_latitude";
    private static final String KEY_TIME = "key_time";
    private static final String KEY_IMAGES = "key_iamges";
    private static final String KEY_CONTACT = "key_contact";
    public static final String TEMP_FILE = "temp_file";
    private static final String KEY_ID = "key_id";
    private static final String KEY_USER_ID = "key_user_id";

    private MapView mMapView;
    private AMap mAMap;
    private UiSettings mUiSettings;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private double mLatitude;
    private double mLongtitude;

    private DwItemView mContainer;
    private TabLayout mTab;
    private FrameLayout mProgress;
    private LayoutInflater mInflater;

    private String[] mTypes;
    private int mContainerWidth;
    private boolean isOnce = false;


    private List<Map<String, Object>> mDatasAll = new ArrayList<>();
    private List<Map<String,Object>> mDatasCurrent = new ArrayList<>();
    private MyApplication mApplication;
    private LatLng mLatLng = null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_dw, container, false);
        getActivity().setTitle("鱼跃");
        mInflater = LayoutInflater.from(getActivity());
        mMapView = (MapView) v.findViewById(R.id.id_dw_mapview);
        mProgress = (FrameLayout) v.findViewById(R.id.id_progress);
        mMapView.onCreate(savedInstanceState);
        if (mAMap == null) {
            mAMap = mMapView.getMap();
        }
        mContainer = (DwItemView) v.findViewById(R.id.id_dw_itemview);
        mTab = (TabLayout) v.findViewById(R.id.id_dw_tab);
        mTab.setOnTabSelectedListener(this);
        initContainerWidth();
        mUiSettings = mAMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
        mUiSettings.setCompassEnabled(true);
        mAMap.setLocationSource(this);// 设置定位监听
        mUiSettings.setMyLocationButtonEnabled(true); // 显示默认的定位按钮
        mAMap.setMyLocationEnabled(true);// 可触发定位并显示定位层
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setLogoPosition(AMapOptions.LOGO_MARGIN_LEFT);
        mAMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    int pos = Integer.parseInt(marker.getTitle());
                    mContainer.scrollToPos(pos);
                    Map<String, Object> map = mDatasCurrent.get(pos);
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng((double)map.get(KEY_LATITUDE),
                            (double)map.get(KEY_LONGTITUDE))));
                }catch (NumberFormatException e){}

                return true;
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDatasAll();
        mContainer.setOnItemSelectListner(new DwItemView.OnItemSelectListner() {
            @Override
            public void onSelectChange(int pos) {
//                Toast.makeText(getActivity(), "select " + pos, Toast.LENGTH_SHORT).show();
                Map<String, Object> map = mDatasCurrent.get(pos);
                mAMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng((double)map.get(KEY_LATITUDE),
                        (double)map.get(KEY_LONGTITUDE))));
            }
        });

    }

    private void initItemView() {
        mContainer.removeAllViews();
        clearAllMarker();
        for (int i = 0; i < mDatasCurrent.size(); i++) {
            View itemView = mInflater.inflate(R.layout.dw_item, mContainer, false);
            TextView mNameTv = (TextView) itemView.findViewById(R.id.id_dwitem_name);
            TextView mTitleTv = (TextView) itemView.findViewById(R.id.id_dwitem_title);
            TextView mDescTv = (TextView) itemView.findViewById(R.id.id_dwitem_desc);
            TextView mPriceTv = (TextView) itemView.findViewById(R.id.id_dwitem_price);
            ImageView mHeadIv = (ImageView) itemView.findViewById(R.id.id_dwitem_head);
            TextView mDistanceTv = (TextView) itemView.findViewById(R.id.id_dwitem_dis);
            Map<String, Object> map = mDatasCurrent.get(i);
            if (map == null) {
                continue;
            }
            mNameTv.setText((String)map.get(KEY_NAME));
            mTitleTv.setText((String)map.get(KEY_TITLE));
            mDescTv.setText((String)map.get(KEY_DESC));
            mPriceTv.setText(map.get(KEY_PRICE) + "元");
            PersonalInfoActivity.loadImage((String)map.get(KEY_HEAD_URL), mHeadIv, true);
            if (mLatLng != null) {
                float dis = AMapUtils.calculateLineDistance(new LatLng((double) map.get(KEY_LATITUDE), (double) map.get(KEY_LONGTITUDE))
                        , mLatLng);
                mDistanceTv.setText((int)dis + "m");
            }
            showMarker((double)map.get(KEY_LATITUDE), (double)map.get(KEY_LONGTITUDE), (String)map.get(KEY_HEAD_URL), i);
            ViewGroup.LayoutParams lp = itemView.getLayoutParams();
            lp.width = mContainerWidth;
            final int finalI = i;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Map<String, Object> map = mDatasCurrent.get(finalI);
                    if (map == null) {
                        return;
                    }
                    mAMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                        @Override
                        public void onMapScreenShot(Bitmap bitmap) {

                        }

                        @Override
                        public void onMapScreenShot(Bitmap bitmap, int i) {
                            if (bitmap == null) {
                                Snackbar.make(mContainer, "地图加载中...", Snackbar.LENGTH_SHORT).show();
                                return;
                            }
                            try {
                                FileOutputStream fos = getActivity().openFileOutput(TEMP_FILE, Context.MODE_PRIVATE);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                                fos.close();
                                DwDetailActivity.startActivity(getActivity(), (String)map.get(KEY_NAME),
                                        (String)map.get(KEY_TITLE), (String)map.get(KEY_DESC)
                                        , "奖赏" + map.get(KEY_PRICE) + "元",
                                        (String)map.get(KEY_IMAGES), (String)map.get(KEY_TIME)
                                        , (String)map.get(KEY_CONTACT),
                                        (int)map.get(KEY_ID), (int)map.get(KEY_USER_ID));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    });
                }
            });

            mContainer.addView(itemView, i);
        }
        mContainer.scrollTo(0, 0);
        isOnce = true;
        mProgress.setVisibility(View.GONE);

    }

    private void clearAllMarker() {
        if (mAMap != null) {
            mAMap.clear();
        }
    }

    private void showMarker(double lat, double longt, final String url, final int pos) {
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(lat, longt));
        markerOptions.title(String.valueOf(pos));
        markerOptions.visible(true);
        final MyApplication.VolleyCache mCache = MyApplication.getVolleyCache();
        final Bitmap cachedBm = mCache.getBitmap(url);
        if (cachedBm != null) {
            generateMarker(pos, markerOptions, cachedBm);
        } else {
            ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    generateMarker(pos, markerOptions, bitmap);
                    mCache.putBitmap(url, bitmap);
                }
            }, 500, 500, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            });
            MyApplication.getRequestQueue().add(request);
        }

    }

    private void generateMarker(int pos, MarkerOptions markerOptions, Bitmap bitmap) {
        RoundDrawable drawable = new RoundDrawable(bitmap);
        View marker = mInflater.inflate(R.layout.map_marker, null);
        ImageView iv = (ImageView) marker.findViewById(R.id.id_marker_head);
        iv.setImageDrawable(drawable);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromView(marker);
        markerOptions.icon(bitmapDescriptor);
        markerOptions.perspective(true);
        markerOptions.title(String.valueOf(pos));
        mAMap.addMarker(markerOptions);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (MyApplication) getActivity().getApplicationContext();
//        initDatasAll();
    }

    private void initDatasAll() {
        mDatasAll.clear();
        mDatasCurrent.clear();
        OkHttpUtils.get().url(LoginActivity.BASE_URL + "phpprojects/gettypes.php")
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mContainer, "网络异常", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        String[] types = response.split(":");
                        mTypes = new String[types.length + 1];
                        mTypes[0] = "全部";
                        for (int i = 0; i < types.length; i++) {
                            mTypes[i + 1] = types[i];
                        }
                        for (String s : mTypes) {
                            mTab.addTab(mTab.newTab().setText(s));
                        }

                    }
                });

        OkHttpUtils.post().url(LoginActivity.BASE_URL + "phpprojects/getDwItem.php")
                .addParams("username", mApplication.getName())
                .addParams("deviceid", mApplication.getAndroidId(getActivity()))
                .addParams("type", "0")
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mContainer, "网络异常", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = (JSONObject) arr.get(i);
                                Map<String, Object> map = new HashMap<>();
                                map.put(KEY_TITLE, obj.getString("title"));
                                map.put(KEY_DESC, obj.getString("content"));
                                map.put(KEY_PRICE, obj.getString("price"));
                                map.put(KEY_USER_ID, obj.getInt("userid"));
                                map.put(KEY_HEAD_URL, LoginActivity.BASE_URL
                                        + "phpprojects/userimage/"
                                        + obj.getInt("userid")
                                        + ".jpg");
                                map.put(KEY_ID, obj.getInt("id"));
                                map.put(KEY_NAME, obj.getString("username"));
                                map.put(KEY_LATITUDE, obj.getDouble("lat"));
                                map.put(KEY_LONGTITUDE, obj.getDouble("longt"));
                                map.put(KEY_TYPE, obj.getInt("type"));
                                map.put(KEY_TIME, SecondHandDetailActivity.getTimeDes(obj.getLong("time")));
                                map.put(KEY_IMAGES, obj.getString("imageurls"));
                                map.put(KEY_CONTACT, obj.getString("contact"));
                                mDatasAll.add(map);
                            }
                            if (mLatLng != null) {
                                sortDataAll();
                            }
                            mDatasCurrent = mDatasAll;

                            initItemView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("dwfragment", "json error : " + response);
                        }

                    }
                });

//        for (int i = 0; i < 10; i++) {
//            Map<String, String> map = new HashMap<>();
//            map.put(KEY_TITLE, "标题" + i);
//            map.put(KEY_DESC, "描述" + i);
//            map.put(KEY_PRICE, String.valueOf((int)(Math.random() * 100)));
//            map.put(KEY_HEAD_URL, LoginActivity.BASE_URL + "phpprojects/userimage/jimmyxuster.jpg");
//            map.put(KEY_NAME, "jimmyxuster");
//            map.put(KEY_DISTANCE, String.valueOf((int)(Math.random() * 1000)));
//            map.put(KEY_TYPE, String.valueOf((int)(Math.random() * 6)));
//            mDatasAll.add(map);
//        }
    }

    private void sortDataAll() {
        Collections.sort(mDatasAll, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                double lat1 = (double) o1.get(KEY_LATITUDE);
                double lon1 = (double) o1.get(KEY_LONGTITUDE);
                double lat2 = (double) o2.get(KEY_LATITUDE);
                double lon2 = (double) o2.get(KEY_LONGTITUDE);
                double dis1 = AMapUtils.calculateLineDistance(mLatLng,
                        new LatLng(lat1, lon1));
                double dis2 = AMapUtils.calculateLineDistance(mLatLng,
                        new LatLng(lat2, lon2));
                return (int) (dis1 * 1000 - dis2 * 1000);
            }
        });
        refreshDatasCurrent(mTab.getSelectedTabPosition());
    }

    private void initContainerWidth() {
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        int xRight = fab.getLeft();
        int width = xRight - LoginActivity.dp2px(20, getActivity());
        ViewGroup.LayoutParams lp = mContainer.getLayoutParams();
        lp.width = width;
        mContainerWidth = width;
    }

    public void refresh() {
        if (isOnce) {
            mProgress.setVisibility(View.VISIBLE);
            initDatasAll();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mAMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        deactivate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setInterval(4000);
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private boolean isSorted = false;
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mAMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                double latitude = aMapLocation.getLatitude();
                double longtitude = aMapLocation.getLongitude();

                mLatLng = new LatLng(latitude, longtitude);
                if (!isSorted) {
                    isSorted = true;
                    sortDataAll();
                    initItemView();
                }
                refreshDistance();
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
                Log.e("dwfragment","AMap Error: " + errText);
            }
        }
    }

    private void refreshDistance() {
        int childCount = mContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mContainer.getChildAt(i);
            TextView disTv = (TextView) child.findViewById(R.id.id_dwitem_dis);
            Map<String, Object> map = mDatasCurrent.get(i);
            float dis = AMapUtils.calculateLineDistance(new LatLng((double) map.get(KEY_LATITUDE), (double) map.get(KEY_LONGTITUDE))
                    , mLatLng);
            disTv.setText((int)dis + "m");
        }
    }

    private int mCurrPosition = 0;
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int position = tab.getPosition();
        if (mCurrPosition == position) {
            return;
        }
        mCurrPosition = position;
        refreshDatasCurrent(position);
        initItemView();
    }

    private void refreshDatasCurrent(int position) {
        if (position == 0) {
            mDatasCurrent = mDatasAll;
        } else {
            mDatasCurrent = new ArrayList<>();
            for (Map<String, Object> map : mDatasAll) {
                try {
                    int type = (int) map.get(KEY_TYPE);
                    if (type + 1 == position) {
                        mDatasCurrent.add(map);
                    }
                } catch (Exception e){}
            }
        }
    }


    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
