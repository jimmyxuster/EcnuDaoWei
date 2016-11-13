package com.jimmyhsu.ecnudaowei;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.Bean.User;
import com.jimmyhsu.ecnudaowei.Db.UserInfoProvider;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by jimmyhsu on 2016/10/29.
 */

public class ChangeInfoActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 1;
    private static final String TAG = "ChangeInfoActivity";
    @BindView(R.id.id_register_edt_name)
    EditText mEdtName;
    @BindView(R.id.id_register_edt_stuid)
    EditText mEdtStuId;
    @BindView(R.id.id_register_edt_age)
    EditText mEdtAge;
    @BindView(R.id.id_register_radio_sex)
    RadioGroup mRadioSex;
    @BindView(R.id.id_register_change_pwd)
    TextView mTvPwd;
    @BindView(R.id.id_register_button)
    Button mBtnEdit;
    @BindView(R.id.id_register_iv)
    ImageView mIvHead;
    @BindView(R.id.id_register_signature)
    EditText mEdtSign;

    private String mImgPath = "";
    private boolean isUploading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ButterKnife.bind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.id_register_button)
    void editPersonInfo() {
        if (isUploading) {
            return;
        }
        isUploading = true;
        if (!checkLegal()) {
            return;
        }
        String username = mEdtName.getText().toString();
        String stuid = mEdtStuId.getText().toString();
        int age;
        if (mEdtAge.getText().toString().equals("")) {
            age = -1;
        } else {
            age = Integer.parseInt(mEdtAge.getText().toString());
            age = Math.abs(age);
        }
        int checkedRadioButtonId = mRadioSex.getCheckedRadioButtonId();
        int sex = -1;
        if (checkedRadioButtonId == R.id.id_register_radio_male) {
            sex = 1;
        } else if (checkedRadioButtonId == R.id.id_register_radio_female){
            sex = 2;
        }
        String signature = mEdtSign.getText().toString();
        mUser = new User(username, -1, signature, -1, sex, stuid, "", age);
        initOldUser();
        uploadInfo(username, stuid, age, sex, signature);
    }

    private void initOldUser() {
        Cursor cursor = getContentResolver().query(UserInfoProvider.URI_USER_CURRENT, null, null, null, null);
        cursor.moveToFirst();
        oldUser = new HashMap<>();
        oldUser.put(User.COL_NAME, cursor.getString(cursor.getColumnIndex(User.COL_NAME)));
        oldUser.put(User.COL_REGDATE, cursor.getString(cursor.getColumnIndex(User.COL_REGDATE)));
        oldUser.put(User.COL_STU_ID, cursor.getString(cursor.getColumnIndex(User.COL_STU_ID)));
        oldUser.put(User.COL_AGE, cursor.getInt(cursor.getColumnIndex(User.COL_AGE)));
        oldUser.put(User.COL_SIGNATURE, cursor.getString(cursor.getColumnIndex(User.COL_SIGNATURE)));
        oldUser.put(User.COL_SEX, cursor.getInt(cursor.getColumnIndex(User.COL_SEX)));
        oldUser.put(User.COL_USERINFO_ID, cursor.getInt(cursor.getColumnIndex(User.COL_USERINFO_ID)));
        oldUser.put(User.COL_MOBILE, cursor.getString(cursor.getColumnIndex(User.COL_MOBILE)));
        cursor.close();
    }

    private User mUser;
    private Map<String, Object> oldUser;

    private void uploadInfo(String username, String stuid, int age, int sex, String signature) {
        PostFormBuilder pfb = OkHttpUtils.post().url(LoginActivity.BASE_URL + "phpprojects/editpersoninfo.php")
                .addParams("username", MyApplication.getInstance(this).getName())
                .addParams("deviceid", MyApplication.getInstance(this).getAndroidId(this))
                .addParams("newname", username)
                .addParams("stuid", stuid)
                .addParams("age", String.valueOf(age))
                .addParams("sex", String.valueOf(sex))
                .addParams("signature", signature);
        if (!mImgPath.equals("")) {
            pfb.addFile("file",mImgPath.substring(mImgPath.lastIndexOf("/") + 1), new File(mImgPath));
        }
        pfb.addParams("haspic", mImgPath.equals("") ? "false" : "true")
                .tag(this)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Snackbar.make(mBtnEdit, "网络异常", Snackbar.LENGTH_SHORT).show();
                        isUploading = false;
                        deletePic();
                    }

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("success info") || response.equals("success info and image")) {
                            Toast.makeText(ChangeInfoActivity.this, "更改信息成功", Toast.LENGTH_SHORT).show();
                            updateDbInfo();
                            renamePic();
                            finish();
                        } else {
                            Snackbar.make(mBtnEdit, "服务器去火星了~", Snackbar.LENGTH_SHORT).show();
                            Log.e(TAG, "network error: " + response);
                            deletePic();
                        }
                        isUploading = false;
                    }
                });
    }

    private void renamePic() {
        if (!mImgPath.equals("")) {
            Bitmap bitmap = BitmapFactory.decodeFile(mImgPath);
            MyApplication.getInstance(this).setUserHead(bitmap);
            MyApplication.setIsUserHeadNeedRefresh(true);
            deletePic();
        }
    }

    private void deletePic() {
        File file = new File(mImgPath);
        if (file.exists()) {
            file.delete();
        }
    }

    private void updateDbInfo() {
        if (mUser != null) {
            ContentValues values = new ContentValues();
            if (!mUser.getName().equals("")) {
                values.put(User.COL_NAME, mUser.getName());
                MyApplication.getInstance(this).setName(mUser.getName());
            } else {
                values.put(User.COL_NAME, (String)oldUser.get(User.COL_NAME));
            }
            if (mUser.getAge() > 0) {
                values.put(User.COL_AGE, mUser.getAge());
            } else {
                values.put(User.COL_AGE, (int)oldUser.get(User.COL_AGE));
            }
            if (mUser.getSex() > 0) {
                values.put(User.COL_SEX, mUser.getSex());
            } else {
                values.put(User.COL_SEX, (int)oldUser.get(User.COL_SEX));
            }
            if (!mUser.getSignature().equals("")) {
                values.put(User.COL_SIGNATURE, mUser.getSignature());
            } else {
                values.put(User.COL_SIGNATURE, (String)oldUser.get(User.COL_SIGNATURE));
            }
            if (!mUser.getStudentId().equals("")) {
                values.put(User.COL_STU_ID, mUser.getStudentId());
            } else {
                values.put(User.COL_STU_ID, (String)oldUser.get(User.COL_STU_ID));
            }
            values.put(User.COL_REGDATE, (String)oldUser.get(User.COL_REGDATE));
            values.put(User.COL_MOBILE, (String)oldUser.get(User.COL_MOBILE));
            values.put(User.COL_USERINFO_ID, (int)oldUser.get(User.COL_USERINFO_ID));


            getContentResolver().update(UserInfoProvider.URI_USER_CURRENT, values, "_id=1", null);
        }
    }

    @OnClick(R.id.id_register_change_pwd)
    void gotoChangePwd() {
        Intent intent = new Intent(ChangeInfoActivity.this, ForgetActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.id_register_iv)
    void chooseImage(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/jpg;image/jpeg;image/png");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private boolean checkLegal() {
        boolean isLegal = true;
        String name = mEdtName.getText().toString();
        String stuId = mEdtStuId.getText().toString();
        String ageStr = mEdtAge.getText().toString();
        int age = 0;
        View focusView = null;
        if (ageStr != null && !TextUtils.isEmpty(ageStr)) {
            try {
                age = Integer.parseInt(ageStr);
            }catch (NumberFormatException e) {
                focusView = mEdtAge;
                mEdtAge.setError("必须为数字");
                isLegal = false;
            }
        }
        if (name.length() > 10) {
            mEdtName.setError("用户名最多10个字符");
            focusView = mEdtName;
            isLegal = false;
        }
        if (!isLegal) {
            focusView.requestFocus();
        }
        return isLegal;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            ReleaseNewItemActivity.compressImage(ChangeInfoActivity.this, path, 1);
            mImgPath = getFilesDir().getAbsolutePath() + "/temp1.jpg";
            cursor.close();
            loadImage();
        }
    }

    private void loadImage() {
        Bitmap bm = BitmapFactory.decodeFile(mImgPath);
        if (bm != null) {
            RoundDrawable drawable = new RoundDrawable(bm);
            mIvHead.setImageDrawable(drawable);
        }
    }
}
