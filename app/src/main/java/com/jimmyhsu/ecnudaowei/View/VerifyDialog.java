package com.jimmyhsu.ecnudaowei.View;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.ForgetActivity;
import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.R;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by jimmyhsu on 2016/10/4.
 */
public class VerifyDialog extends Dialog {
    private static final String CHANGE_USER_INFO_URL = LoginActivity.BASE_URL + "phpprojects/changePersonalInfo.php/";
    private static final String USER_EXISTS_URL = LoginActivity.BASE_URL + "phpprojects/checkUserExists.php/";
    public VerifyDialog(Context context, String phoneNumber, String newPassword) {
        super(context);
        this.mNewPwd = newPassword;
        this.mPhoneNumber = phoneNumber;
        SMSSDK.initSDK(getContext(), "179f96bf79570", "76180df1b7cb9cf348f036fc3f054331");
    }
    private String mNewPwd;
    private Dialog mDialog = null;
    private ValueAnimator mAnimator = null;
    private String mPhoneNumber;
    public interface VerifyListner {
        void onSuccess();
        void onError();
    }
    private VerifyListner mListner;
    public void setListner(VerifyListner l) {
        mListner = l;
    }
    public void showDialog(final String name, final String prompt) {
//        EventHandler eh = new EventHandler(){
//            @Override
//            public void afterEvent(int event, int result, Object o) {
//                super.afterEvent(event, result, o);
//                if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE && result == SMSSDK.RESULT_COMPLETE) {
//                    Toast.makeText(getContext(), "验证码已发送", Toast.LENGTH_SHORT).show();
//                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE && result == SMSSDK.RESULT_ERROR) {
//                    Toast.makeText(getContext(), "发送失败,请检查网络", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//        SMSSDK.registerEventHandler(eh);
        OkHttpUtils
                .post()
                .url(USER_EXISTS_URL)
                .addParams("name", name.equals("")?mPhoneNumber:name)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        if (response.equals("exists")) {
                            SMSSDK.getVerificationCode("86", mPhoneNumber);
                            Toast.makeText(getContext(), "验证码已发送", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "用户不存在", Toast.LENGTH_SHORT).show();
                            if (mDialog != null && mDialog.isShowing()) {
                                mDialog.dismiss();
                            }
                        }
                    }
                });
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.dialog_input_captcha, null);
        mDialog = new AlertDialog.Builder(getContext()).setTitle(prompt)
                .setView(view)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mDialog = null;
                        if (mAnimator != null && mAnimator.isRunning()) {
                            mAnimator.cancel();
                        }
                        View view = getWindow().peekDecorView();
                        if (view != null) {
                            InputMethodManager inputmanger = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    }
                }).create();
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        final EditText one = (EditText) view.findViewById(R.id.id_dialog_captcha_1);
        final EditText two = (EditText) view.findViewById(R.id.id_dialog_captcha_2);
        final EditText three = (EditText) view.findViewById(R.id.id_dialog_captcha_3);
        final EditText four = (EditText) view.findViewById(R.id.id_dialog_captcha_4);
        final TextView retryView = (TextView) view.findViewById(R.id.id_dialog_retry);
        retryView.setEnabled(false);
        final EditText[] mEdts = new EditText[4];
        registerAnimation(retryView);
        retryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerAnimation(retryView);
//                Log.e("TAG", "reget verify code");
                SMSSDK.getVerificationCode("86", mPhoneNumber);
            }
        });
        mEdts[0] = one;
        mEdts[1] = two;
        mEdts[2] = three;
        mEdts[3] = four;
        for (int i = 0; i < mEdts.length; i++) {
            final EditText edt = mEdts[i];
            edt.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    for (EditText edt : mEdts) {
                        if (edt.getText().toString().isEmpty()) {
                            edt.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
                            && finalI > 0) {
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
                            OkHttpUtils
                                    .post()
                                    .url(CHANGE_USER_INFO_URL)
                                    .addParams("name", name)
                                    .addParams("code", code)
                                    .addParams("mobile", mPhoneNumber)
                                    .addParams("deviceId", LoginActivity.getAndroidId(getContext()))
                                    .addParams("newPwd", mNewPwd)
                                    .build()
                                    .execute(new StringCallback() {
                                        @Override
                                        public void onError(Request request, Exception e) {
                                            Toast.makeText(getContext(), "网络异常", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onResponse(String response) {
                                            if(response.equals("success")){
                                                if (mListner != null) {
                                                    mListner.onSuccess();
                                                }
                                                if (mDialog != null && mDialog.isShowing()) {
                                                    mDialog.dismiss();
                                                }
                                            }else if (response.startsWith("dbfail")){
                                                Log.e("TAG", "dbfail");
                                            }else{
                                                if (mListner != null) {
                                                    mListner.onError();
                                                }
                                                Toast.makeText(getContext(), "验证码错误,请重试", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            //使用自己服务器验证
//                            SMSSDK.submitVerificationCode("86", mPhoneNumber, code.toString());
                        }
                    } else {
                        if (finalI > 0) {
                            mEdts[finalI - 1].requestFocus();
                        }
                    }
                }
            });

        }
    }

    private void registerAnimation(final TextView view) {
        view.setEnabled(false);
        mAnimator = ValueAnimator.ofInt(60, 0);
        mAnimator.setDuration(60 * 1000);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (int) animation.getAnimatedValue();
                view.setText(val + "秒后重试");
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setEnabled(true);
                view.setText("再次发送");
                mAnimator = null;
            }
        });
        mAnimator.start();
    }

}
