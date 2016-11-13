package com.jimmyhsu.ecnudaowei;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.View.FishProgressBar;
import com.jimmyhsu.ecnudaowei.View.SquareImageView;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseNewItemActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 1;
    private TextView mItemNameTv;
    private TextView mCountTv;
    private Spinner mStatusSpinner;
    private TextView mPostageTv;
    private TextView mContactTv;
    private TextView mPriceTv;
    private TextView mLinkTv;
    private TextView mDescTv;
    private GridView mSelectImageGridView;
    private FrameLayout mProgress;
    private MyApplication mApplication;
    private boolean isUploading = false;

    private List<String> mImgPaths = new ArrayList<>();

    private ArrayAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_release_new_item);
        setTitle("发布物品");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        mApplication = (MyApplication) getApplicationContext();
        mImgPaths.add("empty");
        prepareAdapter();
        prepareSpinnerAdapter();
        mSelectImageGridView.setAdapter(mAdapter);
    }

    private void prepareSpinnerAdapter() {
        mStatusSpinner.setAdapter(new TestArrayAdapter(ReleaseNewItemActivity.this,
                getResources().getStringArray(R.array.item_status)));
    }

    private void prepareAdapter() {
        mAdapter = new ArrayAdapter(this, -1, mImgPaths) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                SquareImageView iv = new SquareImageView(ReleaseNewItemActivity.this);
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
        mSelectImageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position == 0) {
                    if (mImgPaths.size() > 10) {
                        Snackbar.make(mSelectImageGridView, "最多上传10张图片", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setType("image/jpg;image/jpeg;image/png");
                    startActivityForResult(intent, REQUEST_IMAGE);
                } else {
                    Dialog dialog = new AlertDialog.Builder(ReleaseNewItemActivity.this).setTitle("确认删除?")
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
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.slide_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE){
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                Cursor c = getContentResolver().query(uri, null, null, null, null);
                c.moveToFirst();
                String path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
                compressImage(ReleaseNewItemActivity.this, path, mImgPaths.size());
                mImgPaths.add(getFilesDir().getAbsolutePath() + "/temp" + mImgPaths.size() + ".jpg");
                mAdapter.notifyDataSetChanged();
                c.close();
            }else{
//                Toast.makeText(this, "Error opening files", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void compressImage(Context context, String path, int id) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int tWidth = 1000;
        int tHeight = 1000;
        options.inSampleSize = 1;
        if (width > tWidth || height > tHeight) {
            options.inSampleSize = Math.max(width / tWidth, height / tHeight);
        }
        options.inJustDecodeBounds = false;
        Bitmap compressedBm = BitmapFactory.decodeFile(path, options);

        try {
            FileOutputStream fos = context.openFileOutput("temp" + id + ".jpg", MODE_PRIVATE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 100;
            compressedBm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            while (baos.toByteArray().length / 1024 > 500) {
                baos.reset();
                quality-=10;
                compressedBm.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }
            fos.write(baos.toByteArray());
            fos.close();
            baos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, "外部存储卡异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void decodeBitmap(String path, final ImageView iv) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;
        int targetWidth = iv.getMeasuredWidth();
        if (targetWidth <= 0) {
            targetWidth = getScreenWidth() / 4;
        }
        if (targetWidth <= 0) {
            targetWidth = iv.getMaxWidth();
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.max(width, height) / targetWidth;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        iv.setImageBitmap(bm);
    }

    private void initViews() {
        mItemNameTv = (TextView) findViewById(R.id.id_release_item_name);
        mCountTv = (TextView) findViewById(R.id.id_release_count);
        mPostageTv = (TextView) findViewById(R.id.id_release_postage);
        mContactTv = (TextView) findViewById(R.id.id_release_contact);
        mPriceTv = (TextView) findViewById(R.id.id_release_price);
        mLinkTv = (TextView) findViewById(R.id.id_release_link);
        mDescTv = (TextView) findViewById(R.id.id_release_description);
        mStatusSpinner = (Spinner) findViewById(R.id.id_release_condition);
        mSelectImageGridView = (GridView) findViewById(R.id.id_release_pic_grid);
        mProgress = (FrameLayout) findViewById(R.id.id_progress);
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
            upload();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (isUploading) {
            return;
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void upload() {
        if (checkInfoInput()) {
            mProgress.setVisibility(View.VISIBLE);
            isUploading = true;
            PostFormBuilder postBuilder = OkHttpUtils.post();
            for (int i = 1; i < mImgPaths.size(); i++) {
                postBuilder.addFile("file" + i, mImgPaths.get(i).substring(mImgPaths.get(i).lastIndexOf("/") + 1),
                        new File(mImgPaths.get(i)));
//                Log.e("uploaded", mImgPaths.get(i).substring(mImgPaths.get(i).lastIndexOf("/") + 1));
            }
//            Map<String, String> map = new HashMap<>();
//            map.put("name", mApplication.getName());
//            map.put("deviceid", LoginActivity.getAndroidId(this));
//            map.put("title", mItemNameTv.getText().toString());
//            map.put("count", String.valueOf(mCountTv.getText().toString()));
//            map.put("condition", String.valueOf(mStatusSpinner.getSelectedItemPosition()));
//            map.put("postage", String.valueOf(mPostageTv.getText().toString()));
//            map.put("contact", mContactTv.getText().toString());
//            map.put("price", mPriceTv.getText().toString());
//            map.put("link", mLinkTv.getText().toString().equals("") ? "no_link" : mLinkTv.getText().toString());
//            map.put("imgCount", String.valueOf(mImgPaths.size() - 1));
//            map.put("description", mDescTv.getText().toString());
            postBuilder
                    .url(LoginActivity.BASE_URL + "phpprojects/releaseNewShItem.php")
                    .addParams("name", MyApplication.getInstance(ReleaseNewItemActivity.this).getName())
                    .addParams("deviceid", LoginActivity.getAndroidId(this))
                    .addParams("title", mItemNameTv.getText().toString())
                    .addParams("count", String.valueOf(mCountTv.getText().toString()))
                    .addParams("condition", String.valueOf(mStatusSpinner.getSelectedItemPosition()))
                    .addParams("postage", String.valueOf(mPostageTv.getText().toString()))
                    .addParams("contact", mContactTv.getText().toString())
                    .addParams("price", mPriceTv.getText().toString())
                    .addParams("link", mLinkTv.getText().toString().equals("") ? "no_link" : mLinkTv.getText().toString())
                    .addParams("imgCount", String.valueOf(mImgPaths.size() - 1))
                    .addParams("description", mDescTv.getText().toString())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Request request, Exception e) {
                            Snackbar.make(mSelectImageGridView, "网络异常", Snackbar.LENGTH_SHORT).show();
                            isUploading = false;
                        }

                        @Override
                        public void onResponse(String response) {
                            isUploading = false;
                            if (response.equals("success")) {
                                setResult(RESULT_OK);
                                mProgress.setVisibility(View.GONE);
                                Toast.makeText(ReleaseNewItemActivity.this, "发布成功!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Log.e("release", "response = " + response);
                                Snackbar.make(mSelectImageGridView, "发布失败, 可能是账号异常或服务器异常", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean checkInfoInput() {
        if (mImgPaths.size() == 1) {
            Snackbar.make(mSelectImageGridView, "至少选择一张图片", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if ("".equals(mItemNameTv.getText().toString())) {
            mItemNameTv.setError("商品名称不得为空");
            mItemNameTv.requestFocus();
            return false;
        }
        if ("".equals(mCountTv.getText().toString())) {
            mCountTv.setError("数量不得为空");
            mCountTv.requestFocus();
            return false;
        }
        if (mStatusSpinner.getSelectedItemPosition() == 0) {
            mStatusSpinner.performClick();
            Toast.makeText(this, "请选择物品成色", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ("".equals(mPostageTv.getText().toString())) {
            mPostageTv.setError("邮费不得为空");
            mPostageTv.requestFocus();
            return false;
        }
        try {
            String postageStr = mPostageTv.getText().toString();
            int postage = Integer.parseInt(postageStr);
            if (postage < 0) {
                mPostageTv.setError("邮费需大于等于0");
                mPostageTv.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mPostageTv.setError("邮费需为数字");
            mPostageTv.requestFocus();
            return false;
        }
        if ("".equals(mContactTv.getText().toString())) {
            mContactTv.setError("联系方式必填");
            mContactTv.requestFocus();
            return false;
        }
        if ("".equals(mPriceTv.getText().toString())) {
            mPriceTv.setError("价格不得为空");
            mPriceTv.requestFocus();
            return false;
        }
        try {
            String priceeStr = mPriceTv.getText().toString();
            int price = Integer.parseInt(priceeStr);
            if (price < 0) {
                mPriceTv.setError("价格需大于等于0");
                mPriceTv.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mPriceTv.setError("价格需为数字");
            mPriceTv.requestFocus();
            return false;
        }
        if (!"".equals(mLinkTv.getText().toString())) {
            try {
                String urlStr = mLinkTv.getText().toString();
                if (!urlStr.startsWith("http")) {
                    urlStr = "http://" + urlStr;
                    mLinkTv.setText(urlStr);
                }
                if (!urlStr.contains("taobao.com")) {
                    throw new MalformedURLException("not xianyu url");
                }
                URL url = new URL(urlStr);
//                Log.e("release", "url is valid: " + url.toString());
            } catch (MalformedURLException e) {
                mLinkTv.setError("请填写一个有效的闲鱼链接地址");
                mLinkTv.requestFocus();
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (int i = 1; i < mImgPaths.size(); i++) {
            File file = new File(mImgPaths.get(i));
            file.delete();
        }
    }

    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
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

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            return convertView;
        }

    }

    public static float sp2px(float sp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp
                , context.getResources().getDisplayMetrics());
    }
}
