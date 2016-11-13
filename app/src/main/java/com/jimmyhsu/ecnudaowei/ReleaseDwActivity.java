package com.jimmyhsu.ecnudaowei;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jimmyhsu.ecnudaowei.View.FishProgressBar;
import com.jimmyhsu.ecnudaowei.View.FullyDisplayGridView;
import com.jimmyhsu.ecnudaowei.View.SquareImageView;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jimmyhsu on 2016/10/23.
 */

public class ReleaseDwActivity extends AppCompatActivity implements AMapLocationListener {

    private static final String EXTRA_TYPE = "extra_type";
    private static final int REQUEST_IMAGE = 1;
    //Views
    private EditText mTitleEdt;
    private EditText mContentEdt;
    private Spinner mTypeSpinner;
    private EditText mPriceEdt;
    private FullyDisplayGridView mGridView;
    private FrameLayout mProgress;
    private EditText mContactEdt;

    private boolean isUploading = false;
    private String[] mTypes = new String[]{"请选择"};
    private ArrayAdapter mAdapter;
    private SpinnerAdapter mAdapterSpinner;
    private List<String> mImgPaths = new ArrayList<>();
    private MyApplication mApplication;

    //AMap
    public AMapLocationClientOption mLocationOption = null;
    private AMapLocationClient mlocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dw_release);
        mApplication = (MyApplication) getApplicationContext();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("发布");
        initViews();
        initDatas();
        initEvents();
    }

    private void initAMapAndUpload() {
        mProgress.setVisibility(View.VISIBLE);
        mlocationClient = new AMapLocationClient(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
//设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        mLocationOption.setOnceLocationLatest(true);
// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
// 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
// 在定位结束后，在合适的生命周期调用onDestroy()方法
// 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
//启动定位
        mlocationClient.startLocation();
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                double lat = amapLocation.getLatitude();//获取纬度
                double longt = amapLocation.getLongitude();//获取经度
                upLoad(lat, longt);
            } else {
                Snackbar.make(mGridView, "无法获得定位" + amapLocation.getErrorCode()
                        , Snackbar.LENGTH_SHORT).show();
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
                mProgress.setVisibility(View.GONE);
            }
        }
    }

    private void initEvents() {
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position == 0) {
                    if (mImgPaths.size() > 5) {
                        Snackbar.make(mGridView, "最多上传5张图片", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setType("image/jpg;image/jpeg;image/png");
                    startActivityForResult(intent, REQUEST_IMAGE);
                } else {
                    Dialog dialog = new AlertDialog.Builder(ReleaseDwActivity.this).setTitle("确认删除?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mImgPaths.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("取消", null).create();
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            ReleaseNewItemActivity.compressImage(ReleaseDwActivity.this, path, mImgPaths.size());
            mImgPaths.add(getFilesDir().getAbsolutePath() + "/temp" + mImgPaths.size() + ".jpg");
            cursor.close();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 1; i < mImgPaths.size(); i++) {
            File file = new File(mImgPaths.get(i));
            file.delete();
        }
    }

    private boolean checkInput() {
        if (mTitleEdt.getText().toString().equals("")) {
            mTitleEdt.setError("不能为空");
            mTitleEdt.requestFocus();
            return false;
        }
        if (mTitleEdt.getText().toString().length() > 10) {
            mTitleEdt.setError("最长10个字");
            mTitleEdt.requestFocus();
            return false;
        }
        if (mContactEdt.getText().toString().equals("")) {
            mContactEdt.setError("不能为空");
            mContactEdt.requestFocus();
            return false;
        }
        if (mContactEdt.getText().toString().length() > 50) {
            mContactEdt.setError("最长50个字");
            mContactEdt.requestFocus();
            return false;
        }
        if (mContentEdt.getText().toString().equals("")) {
            mContentEdt.setError("不能为空");
            mContentEdt.requestFocus();
            return false;
        }
        if (mContentEdt.getText().toString().length() > 1000) {
            mContentEdt.setError("最长1000字");
            mContentEdt.requestFocus();
            return false;
        }
        if (mTypeSpinner.getSelectedItemId() == 0) {
            Snackbar.make(mGridView, "请选择类别", Snackbar.LENGTH_SHORT).show();
            mTypeSpinner.performClick();
            return false;
        }
        if (mPriceEdt.getText().toString().equals("")) {
            mPriceEdt.setError("不能为空，无赏金请填0");
            mPriceEdt.requestFocus();
            return false;
        }
        try {
            int i = Integer.parseInt(mPriceEdt.getText().toString());
            if (i < 0) {
                mPriceEdt.setError("赏金必须大于等于0");
                mPriceEdt.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mPriceEdt.setError("赏金必须为数字");
            mPriceEdt.requestFocus();
            return false;
        }
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.slide_out);
    }

    private void initDatas() {
        OkHttpUtils.get().url(LoginActivity.BASE_URL + "phpprojects/gettypes.php")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mGridView, "网络异常", Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(String response) {
                        String[] types = response.split(":");
                        mTypes = new String[types.length + 1];
                        mTypes[0] = "请选择";
                        System.arraycopy(types, 0, mTypes, 1, types.length);
                        mTypeSpinner.setAdapter(mAdapterSpinner = new TestArrayAdapter(ReleaseDwActivity.this, mTypes));
                    }
                });
        mTypeSpinner.setAdapter(mAdapterSpinner = new TestArrayAdapter(this, mTypes));
        mImgPaths.clear();
        mImgPaths.add("empty");
        mAdapter = new ArrayAdapter(this, -1, mImgPaths) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SquareImageView iv = new SquareImageView(ReleaseDwActivity.this);
                iv.setScaleType(ImageView.ScaleType.FIT_XY);
                if (getItem(position).equals("empty")) {
                    iv.setImageResource(R.drawable.add);
                } else {
//                    iv.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    decodeBitmap(mImgPaths.get(position), iv);
                }
                return iv;
            }
        };
        mGridView.setAdapter(mAdapter);
    }

    private void decodeBitmap(String path, final ImageView iv) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int targetWidth = iv.getMeasuredWidth();
        if (targetWidth <= 0) {
            targetWidth = iv.getMaxWidth();
        }
        if (targetWidth <= 0) {
            targetWidth = getScreenWidth();
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.max(width, height) / targetWidth;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        iv.setImageBitmap(bm);
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(Menu.NONE, Menu.FIRST + 1, 0, "发布").setIcon(R.drawable.action_send);
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST + 1) {
            if (!checkInput()) {
                return true;
            }
            initAMapAndUpload();
        } else if (item.getItemId() == android.R.id.home) {
            if (!isUploading) {
                setResult(RESULT_CANCELED);
                finish();
            } else {
                Toast.makeText(this, "上传数据中，请耐心等待", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isUploading) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void upLoad(double lat, double longt) {
        PostFormBuilder mBuilder = OkHttpUtils.post();
        for (int i = 1; i < mImgPaths.size(); i++) {
            ReleaseNewItemActivity.compressImage(this, mImgPaths.get(i), i);
            mBuilder.addFile("file" + i, mImgPaths.get(i).substring(mImgPaths.get(i).lastIndexOf("/") + 1),
                    new File(mImgPaths.get(i)));
        }
        mBuilder.addParams("username", mApplication.getName());
        mBuilder.addParams("deviceid", LoginActivity.getAndroidId(ReleaseDwActivity.this));
        mBuilder.addParams("imagecount", String.valueOf(mImgPaths.size() - 1));
        mBuilder.addParams("title", mTitleEdt.getText().toString());
        mBuilder.addParams("content", mContentEdt.getText().toString());
        mBuilder.addParams("type", String.valueOf(mTypeSpinner.getSelectedItemPosition() - 1));
        mBuilder.addParams("price", String.valueOf(mPriceEdt.getText().toString()));
        mBuilder.addParams("lat", String.valueOf(lat));
        mBuilder.addParams("contact", mContactEdt.getText().toString());
        mBuilder.addParams("longt", String.valueOf(longt));
        mBuilder.url(LoginActivity.BASE_URL + "phpprojects/releasedw.php")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mGridView, "网络异常", Snackbar.LENGTH_SHORT).show();
                        isUploading = false;
                        mProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success")) {
                            overridePendingTransition(0, R.anim.slide_out);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(ReleaseDwActivity.this, "服务器去火星了~", Toast.LENGTH_SHORT).show();
                            Log.e("releasedw", "error = " + response);
                        }
                        mProgress.setVisibility(View.GONE);
                    }
                });
    }

    private void initViews() {
        mTitleEdt = (EditText) findViewById(R.id.id_release_dw_title);
        mContentEdt = (EditText) findViewById(R.id.id_release_dw_content);
        mTypeSpinner = (Spinner) findViewById(R.id.id_release_dw_typespinner);
        mPriceEdt = (EditText) findViewById(R.id.id_release_dw_price);
        mGridView = (FullyDisplayGridView) findViewById(R.id.id_release_dw_photogrid);
        mProgress = (FrameLayout) findViewById(R.id.id_progress);
        mContactEdt = (EditText) findViewById(R.id.id_release_dw_contact);
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ReleaseDwActivity.class);
        context.startActivity(intent);
    }

    public class TestArrayAdapter extends ArrayAdapter<String> {
        private Context mContext;
        private String[] mStringArray;
        public TestArrayAdapter(Context context, String[] stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mStringArray=stringArray;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //修改Spinner展开后的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.my_simple_drop_down_item_view, parent,false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray[position]);
            tv.setTextColor(Color.DKGRAY);
            return convertView;

        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray[position]);
            if (position > 0) {
                tv.setTextColor(Color.parseColor("#ff000000"));
            } else {
                tv.setTextColor(Color.parseColor("#ffd6d6d6"));
            }
            tv.setTextSize(18);
            return convertView;
        }

    }
}
