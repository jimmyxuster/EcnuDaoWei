package com.jimmyhsu.ecnudaowei;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.View.RoundDrawable;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

/**
 * Created by jimmyhsu on 2016/10/3.
 */
public class RegisterActivity extends AppCompatActivity {
    private static final String REGISTER_URL = LoginActivity.BASE_URL + "phpprojects/register.php";
    private static final int REQUEST_IMAGE = 1;

    @BindView(R.id.id_register_edt_name)
    EditText mNameView;
    @BindView(R.id.id_register_edt_pwd)
    EditText mPasswordView;
    @BindView(R.id.id_register_edt_stuid)
    EditText mStuIdView;
    @BindView(R.id.id_register_edt_mobile)
    EditText mMobileView;
    @BindView(R.id.id_register_edt_age)
    EditText mAgeView;
    @BindView(R.id.id_register_radio_sex)
    RadioGroup mSexGroupView;
    @BindView(R.id.id_register_button)
    Button mRegisterView;
    @BindView(R.id.id_register_mobile_verify)
    Button mVerifyView;
    @BindView(R.id.id_register_head)
    ImageView mIvHead;

    private Dialog mDialog = null;
    private ValueAnimator mAnimator = null;

    private boolean isMobileVerified = false;
    private boolean isFromRegisterButton = false;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int event = msg.arg1;
            int code = msg.arg2;
            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE && code == SMSSDK.RESULT_COMPLETE) {
                beginCountDown();
                showVerifyDialog();
            }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE && code != SMSSDK.RESULT_COMPLETE) {
                Snackbar.make(mMobileView, "获取验证码失败,请检查网络", Snackbar.LENGTH_SHORT).show();
            }
            else if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE && code == SMSSDK.RESULT_COMPLETE) {
                isMobileVerified = true;
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                }
                mVerifyView.setText("已验证");
                mVerifyView.setEnabled(false);
                mMobileView.setEnabled(false);
                Snackbar.make(mMobileView, "验证成功!", Snackbar.LENGTH_SHORT).show();
                if (isFromRegisterButton) {
                    mRegisterView.performClick();
                }
            }else{
                Snackbar.make(mMobileView, "验证失败,请检查网络或输入是否正确!", Snackbar.LENGTH_SHORT).show();
            }
            if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                SMSSDK.unregisterAllEventHandler();
            }
        }
    };

    private void beginCountDown() {
        mVerifyView.setEnabled(false);
//        mVerifyView.setBackgroundColor(0xffffff);
//        mVerifyView.setTextColor(0x777777);
        if (mAnimator != null) return;
        mAnimator = ValueAnimator.ofInt(60, 0);
        mAnimator.setDuration(60 * 1000).addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (int) animation.getAnimatedValue();
                mVerifyView.setText(String.valueOf(val));
            }
        });
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mVerifyView.setEnabled(true);
                mVerifyView.setText(R.string.action_mobile_verify);
//                mVerifyView.setTextColor(0xffffff);
//                mVerifyView.setBackground(getResources().getDrawable(R.drawable.btn_bg_big));
            }
        });
        mAnimator.start();
    }

    private void showVerifyDialog() {
        if (mDialog != null) {
            return;
        }
        Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
        LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_input_captcha, null);
        mDialog = new AlertDialog.Builder(this).setTitle("请输入验证码")
                .setView(view)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mDialog = null;
                        View view = getWindow().peekDecorView();
                        if (view != null) {
                            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                })
                .create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        final EditText one = (EditText) view.findViewById(R.id.id_dialog_captcha_1);
        final EditText two = (EditText) view.findViewById(R.id.id_dialog_captcha_2);
        final EditText three = (EditText) view.findViewById(R.id.id_dialog_captcha_3);
        final EditText four = (EditText) view.findViewById(R.id.id_dialog_captcha_4);
        view.findViewById(R.id.id_dialog_retry).setVisibility(View.GONE);
        final EditText[] mEdts = new EditText[4];
        mEdts[0] = one; mEdts[1] = two;
        mEdts[2] = three; mEdts[3] = four;
        for (int i = 0; i < mEdts.length; i++) {
            final EditText edt = mEdts[i];
            edt.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    for (EditText edt : mEdts) {
                        if (edt.getText().toString().isEmpty()) {
                            edt.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(edt, InputMethodManager.SHOW_FORCED);
                            return true;
                        }
                    }
                    return false;
                }
            });
            final int finalI = i;
            edt.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (finalI == 3 && !"".equals(edt.getText().toString())){
                        return false;
                    }
                    if (TextUtils.isEmpty(edt.getText().toString()) &&
                            keyCode == KeyEvent.KEYCODE_DEL
                            && event.getAction() == KeyEvent.ACTION_DOWN
                            && finalI > 0){
                        mEdts[finalI - 1].setText("");
                        mEdts[finalI - 1].requestFocus();
                        return true;
                    }
                    return false;
                }
            });
            edt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().length() == 1) {
                        if (finalI < 3) {
                            mEdts[finalI + 1].requestFocus();
                        } else if (finalI == 3) {
                            String code = "";
                            for (EditText edt : mEdts) {
                                code += edt.getText().toString();
                            }
                            Message msg = Message.obtain();
                            msg.arg1 = SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE;
                            msg.arg2 = SMSSDK.RESULT_COMPLETE;
                            mHandler.sendMessage(msg);
//                            SMSSDK.submitVerificationCode("86", mMobileView.getText().toString(), code);
                            mDialog.dismiss();
                        }
                    }else{
                        if (finalI > 0) {
                            mEdts[finalI-1].requestFocus();
                        }
                    }
                }
            });

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mImgPath.equals("")) {
            File file = new File(mImgPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("注册");
        SMSSDK.initSDK(this, "179f96bf79570", "76180df1b7cb9cf348f036fc3f054331");
        initDatas();
        initEvents();
    }

    private void initDatas() {
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra(LoginActivity.INTENT_EXTRA_NAME);
            if (name != null && !TextUtils.isEmpty(name)){
                mNameView.setText(name);
            }
        }
    }
    private String mImgPath = "";
    private boolean isUploading = false;

    private void initEvents() {
        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUploading) {
                    return;
                }
                isUploading = true;
                boolean isLegal = true;
                String name = mNameView.getText().toString();
                String pwd = mPasswordView.getText().toString();
                String stuId = mStuIdView.getText().toString();
                String mobile = mMobileView.getText().toString();
                String ageStr = mAgeView.getText().toString();
                int age = 0;
                View focusView = null;
                if (ageStr != null && !TextUtils.isEmpty(ageStr)) {
                    age = Integer.parseInt(ageStr);
                }
                if (TextUtils.isEmpty(mobile)) {
                    mMobileView.setError("手机号不可以为空");
                    isLegal = false;
                    focusView = mMobileView;
                }
                if (pwd.length() < 4) {
                    mPasswordView.setError("密码长度至少4位");
                    isLegal = false;
                    focusView = mPasswordView;
                }
                if (TextUtils.isEmpty(name)) {
                    mNameView.setError("用户名不可以为空");
                    focusView = mNameView;
                    isLegal = false;
                }
                if (name.length() > 10) {
                    mNameView.setError("用户名最多10个字符");
                    focusView = mNameView;
                    isLegal = false;
                }
                if (mImgPath.equals("")) {
                    Toast.makeText(RegisterActivity.this, "请选择一张头像", Toast.LENGTH_SHORT).show();
                    isLegal = false;
                    focusView = mIvHead;
                }

                
                if (!isMobileVerified) {
                    if (mAnimator != null && mAnimator.isRunning()) {
                        Snackbar.make(mMobileView, "请先验证手机!", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    isFromRegisterButton = true;
                    verifyMobile(mobile);
                    return;
                }


                int sex = 1;
                int checkedId = mSexGroupView.getCheckedRadioButtonId();
                if (checkedId == R.id.id_register_radio_male) {
                    sex = 1;
                } else {
                    sex = 2;
                }
                if (isLegal) {
                    OkHttpUtils
                            .post()
                            .url(REGISTER_URL)
                            .addParams("name",name)
                            .addParams("password",pwd)
                            .addParams("mobile",mobile)
                            .addParams("sex",String.valueOf(sex))
                            .addParams("stuId",stuId)
                            .addParams("age",String.valueOf(age))
                            .addParams("deviceId", getAndroidId(RegisterActivity.this))
                            .addFile("file", mImgPath.substring(mImgPath.lastIndexOf("/") + 1), new File(mImgPath))
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Request request, Exception e) {
                                    isUploading = false;
                                    Snackbar.make(mMobileView, "服务器去火星了%>_<%"
                                            , Snackbar.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(String response) {
                                    isUploading = false;
                                    switch (response) {
                                        case "register success":
                                            Toast.makeText(RegisterActivity.this, "注册成功!", Toast.LENGTH_SHORT).show();
                                            finish();
                                            return;
                                        case "name already exists":
                                            mNameView.setError("该用户名已被使用,请更换");
                                            break;
                                        case "mobile already exists":
                                            mMobileView.setError("该手机号已被注册,请尝试找回密码");
                                            break;
                                    }
                                    mMobileView.setEnabled(true);
                                }
                            });
                } else {
                    isUploading = false;
                    if (focusView != null) {
                        focusView.requestFocus();
                    }
                }
            }
        });
        mVerifyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = mMobileView.getText().toString();
                if (mobile.length() != 11 || mobile.charAt(0) != '1') {
                    mMobileView.setError("请输入正确的手机号");
                    mMobileView.requestFocus();
                }else{
                    isFromRegisterButton = false;
                    verifyMobile(mobile);
                }
            }
        });
    }

    @OnClick(R.id.id_register_head)
    void chooseImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/jpg;image/jpeg;image/png");
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            ReleaseNewItemActivity.compressImage(RegisterActivity.this, path, 1);
            mImgPath = getFilesDir().getAbsolutePath() + "/temp1.jpg";
            cursor.close();
            loadImage();
        }
    }

    private void loadImage() {
        Bitmap bm = BitmapFactory.decodeFile(mImgPath);
        RoundDrawable drawable = new RoundDrawable(bm);
        mIvHead.setImageDrawable(drawable);
    }

    private void verifyMobile(String mobile) {
        if (mobile == null || TextUtils.isEmpty(mobile)) {
            return;
        }
        if (mobile.length() != 11 || mobile.charAt(0) != '1'){
            return;
        }
        EventHandler eh=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                Message msg = Message.obtain();
                msg.arg1 = event;
                msg.arg2 = result;
                mHandler.sendMessage(msg);
            }
        };
        SMSSDK.registerEventHandler(eh);
        SMSSDK.getVerificationCode("86", mobile);
    }


    private static String getAndroidId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
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
