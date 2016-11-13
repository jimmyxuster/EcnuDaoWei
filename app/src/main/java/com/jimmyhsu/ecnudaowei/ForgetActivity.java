package com.jimmyhsu.ecnudaowei;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.jimmyhsu.ecnudaowei.View.CaptchaGenerator;
import com.jimmyhsu.ecnudaowei.View.VerifyDialog;

import java.util.regex.Pattern;

/**
 * Created by jimmyhsu on 2016/10/5.
 */
public class ForgetActivity extends AppCompatActivity {

    private CaptchaGenerator mGenerator;

    private EditText mEdtMobile;
    private EditText mEdtCaptcha;
    private Button mBtnGo;
    private EditText mEdtNewPwd;
    private ImageView mCapcha;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        getSupportActionBar().setTitle("更改密码");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        mCapcha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCaptcha();
            }
        });
        mBtnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkMobile(mEdtMobile.getText().toString())) {
                    mEdtMobile.setError("不是合法的手机号");
                    mEdtMobile.requestFocus();
                    return;
                }
                String captcha = mEdtCaptcha.getText().toString();
                if (!captcha.equals(mGenerator.getCode())) {
                    mEdtCaptcha.setError("验证码错误");
                    mEdtCaptcha.requestFocus();
                    refreshCaptcha();
                    return;
                }
                VerifyDialog dialog = new VerifyDialog(ForgetActivity.this, mEdtMobile.getText().toString(), mEdtNewPwd.getText().toString());
                dialog.setListner(new VerifyDialog.VerifyListner() {
                    @Override
                    public void onSuccess() {
                        Snackbar.make(mEdtNewPwd, "更换密码成功", Snackbar.LENGTH_SHORT).show();
                        clearInput();
                    }

                    @Override
                    public void onError() {
                        Snackbar.make(mEdtNewPwd, "更换密码失败", Snackbar.LENGTH_SHORT).show();
                    }
                });
                dialog.showDialog("", "请输入验证码");
            }
        });
    }

    private void clearInput() {
        mEdtMobile.setText("");
        mEdtCaptcha.setText("");
        mEdtNewPwd.setText("");
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void refreshCaptcha() {
        if (mGenerator == null) {
            mGenerator = CaptchaGenerator.getInstance();
        }
        mCapcha.setImageBitmap(mGenerator.getBitmap());
    }

    public static boolean checkMobile(String mobile) {
        return Pattern.matches("^((13[0-9])|(14[5,7,9])|(15[^4,\\D])|(17[0,1,3,5-8])|(18[0-9]))\\d{8}$", mobile);
    }

    private void initViews() {
        mEdtMobile = (EditText) findViewById(R.id.id_forget_mobile);
        mEdtCaptcha = (EditText) findViewById(R.id.id_forget_edt_captcha);
        mBtnGo = (Button) findViewById(R.id.id_forget_go);
        mCapcha = (ImageView) findViewById(R.id.id_forget_captcha);
        mEdtNewPwd = (EditText) findViewById(R.id.id_forget_new_pwd);
        mGenerator = CaptchaGenerator.getInstance();
        Bitmap capchaBm = mGenerator.getBitmap();
        mCapcha.setImageBitmap(capchaBm);
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
