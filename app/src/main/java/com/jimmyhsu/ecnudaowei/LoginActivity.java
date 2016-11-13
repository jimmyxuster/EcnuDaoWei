package com.jimmyhsu.ecnudaowei;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jimmyhsu.ecnudaowei.View.VerifyDialog;
import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import cn.smssdk.SMSSDK;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    public static final String BASE_URL = "http://115.159.79.195:8080/";
    private static final int REQUEST_READ_CONTACTS = 0;
    public static final String SP_NAME = "userinfo";
    public static final String SP_KEY_ISLOGIN = "islogin";
    public static final String SP_KEY_PASSWORD = "password";
    public static final String SP_KEY_NAME = "username";
    public static final String INTENT_EXTRA_NAME = "intent extra name";
    private static final String LOGIN_URL = BASE_URL + "phpprojects/login.php/";
    private static final int REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 101;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mForgetView;
//    private View mLeftHand;
//    private View mRightHand;
    private View mHead;
    private View mEmailSignInButton;

    private MyApplication mAppContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(Build.VERSION.SDK_INT>=21) {
            getSupportActionBar().setElevation(0);
        }
        SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        mAppContext = (MyApplication) getApplicationContext();
        if (sp.getBoolean(SP_KEY_ISLOGIN, false)) {
            mAppContext.setLogin(true);
            mAppContext.setName(sp.getString(SP_KEY_NAME, "error"));
            mAppContext.setPassword(sp.getString(SP_KEY_PASSWORD, "error"));
//            Toast.makeText(this, "已记住登录", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivityForResult(intent, REQUEST_CODE);
        }

        mEmailView = (EditText) findViewById(R.id.email);
        mForgetView = findViewById(R.id.id_forget_pwd);
//        mLeftHand = findViewById(R.id.left_hand);
//        mRightHand = findViewById(R.id.right_hand);
        mHead = findViewById(R.id.head);
        //准备emailview adapter数据

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
//        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    if (ojAnimatorLeft != null && ojAnimatorRight != null) {
//                        ojAnimatorLeft.cancel();
//                        ojAnimatorRight.cancel();
//                        ojAnimatorLeft.start();
//                        ojAnimatorRight.start();
//                    }
//                }else{
//                    if (ojAnimatorLeft != null && ojAnimatorRight != null) {
//                        ojAnimatorLeft.cancel();
//                        ojAnimatorRight.cancel();
//                        mLeftHand.setTranslationX(0);
//                        mRightHand.setTranslationX(0);
//                        mLeftHand.setBackgroundResource(R.drawable.hand);
//                        mRightHand.setBackgroundResource(R.drawable.hand);
//                        isInSecondPicture1 = isInSecondPicture2 = false;
//                        mLeftHand.setScaleY(1);
//                        mRightHand.setScaleY(1);
//                    }
//                }
//            }
//        });

        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        Button mRegisterButton = (Button) findViewById(R.id.register_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra(INTENT_EXTRA_NAME, mEmailView.getText().toString());
                startActivity(intent);
            }
        });

        mForgetView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
                startActivity(intent);
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
//        initHandAnimation();
        checkPermission();

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this
                , Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "用于缓存图片和定位功能，请授予哦~", Toast.LENGTH_SHORT).show();
            PermissionGen.with(LoginActivity.this)
                    .addRequestCode(PERMISSION_REQUEST_CODE)
                    .permissions(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE)
                    .request();
        }
    }

    @PermissionSuccess(requestCode = PERMISSION_REQUEST_CODE)
    public void onSuccessPermission(){
        Log.d("LoginActivity", "permission granted");
    }

    @PermissionFail(requestCode = PERMISSION_REQUEST_CODE)
    public void onFailPermission(){
        Toast.makeText(this, "呜呜~", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    public static void goToLogin(Context context, boolean showTip) {
        Intent intent = new Intent(context, LoginActivity.class);
        MyApplication.getInstance(context).setLogin(false);
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SP_KEY_ISLOGIN, false);
        editor.putString(SP_KEY_NAME, "");
        editor.commit();
        if (showTip) {
            Toast.makeText(context, "请重新登录", Toast.LENGTH_SHORT).show();
        }


        context.startActivity(intent);
    }



//    private boolean mayRequestContacts() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
//        } else {
//            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//        }
//        return false;
//    }

//    /**
//     * Callback received when a permissions request has been completed.
//     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        }
//    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            OkHttpUtils
                    .post()
                    .url(LOGIN_URL)
                    .addParams("name", email)
                    .addParams("pwd", password)
                    .addParams("deviceId", getAndroidId(LoginActivity.this))
                    .tag(this)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Request request, Exception e) {
                            Toast.makeText(LoginActivity.this, "网络异常", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(String response) {
                            showProgress(false);
                            Log.e("LoginActivity", response);
                            if(response.startsWith("login succeed")){
                                Toast.makeText(LoginActivity.this, "登入成功!", Toast.LENGTH_SHORT).show();
                                mAppContext.setLogin(true);
                                mAppContext.setName(mEmailView.getText().toString());
                                mAppContext.setPassword(mPasswordView.getText().toString());
                                saveUserInfo(mEmailView.getText().toString(), mPasswordView.getText().toString());
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (response.startsWith("need verify")){
                                String phoneNumber = response.substring(12);
                                verify(phoneNumber, email);
                            } else {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            }
                        }
                    });
        }
    }

    private void saveUserInfo(String name, String password) {
        SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_KEY_NAME, name);
        editor.putString(SP_KEY_PASSWORD, password);
        editor.putBoolean(SP_KEY_ISLOGIN, true);

        editor.commit();
    }

    private boolean isEmailValid(String email) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
            mHead.setVisibility(show ? View.GONE : View.VISIBLE);
            mHead.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mHead.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
//            mLeftHand.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLeftHand.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLeftHand.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//            mRightHand.setVisibility(show ? View.GONE : View.VISIBLE);
//            mRightHand.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mRightHand.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mHead.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLeftHand.setVisibility(show ? View.GONE : View.VISIBLE);
//            mRightHand.setVisibility(show ? View.GONE : View.VISIBLE);
        }

    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return new CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE +
//                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
//                .CONTENT_ITEM_TYPE},
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
//    }
//
//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        List<String> emails = new ArrayList<>();
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            emails.add(cursor.getString(ProfileQuery.ADDRESS));
//            cursor.moveToNext();
//        }
//
//        addEmailsToAutoComplete(emails);
//    }
//
//    @Override
//    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//
//    }



    private void verify(String number, String name) {
        VerifyDialog dialog = new VerifyDialog(LoginActivity.this, number, "");
        dialog.setListner(new VerifyDialog.VerifyListner() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "更换安全设备成功!", Toast.LENGTH_SHORT).show();
                mEmailSignInButton.performClick();
            }

            @Override
            public void onError() {
                Toast.makeText(LoginActivity.this, "验证码错误", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.showDialog(name, "与上次登录设备不同,请输入短信验证码");
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static int dp2px(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp
                , context.getResources().getDisplayMetrics());
    }
//    ObjectAnimator ojAnimatorLeft = null, ojAnimatorRight = null;
//    private boolean isInSecondPicture1 = false, isInSecondPicture2 = false;
//    private void initHandAnimation() {
//        final int dis = dp2px(28, this);
//        if (ojAnimatorLeft == null) {
//            ojAnimatorLeft = ObjectAnimator.ofFloat(mLeftHand, "translationX", 0, dis);
//            ojAnimatorLeft.setDuration(500);
//            ojAnimatorLeft.setInterpolator(new LinearInterpolator());
//            ojAnimatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float val = (float) animation.getAnimatedValue();
//                    if (!isInSecondPicture1 && val > 0.75f*dis) {
//                        isInSecondPicture1 = true;
//                        mLeftHand.setScaleY((float) (1 + 0.6 * val / dis));
//                        mLeftHand.setBackgroundResource(R.drawable.left_hand);
//                    }
//                    mLeftHand.setTranslationX(val);
//                }
//            });
//        }
//        if (ojAnimatorRight == null) {
//            ojAnimatorRight = ObjectAnimator.ofFloat(mRightHand, "translationX", 0, -dis);
//            ojAnimatorRight.setDuration(500);
//            ojAnimatorRight.setInterpolator(new LinearInterpolator());
//            ojAnimatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float val = (float) animation.getAnimatedValue();
//                    if (!isInSecondPicture2 && -val > 0.75f*dis) {
//                        isInSecondPicture2 = true;
//                        mRightHand.setScaleY((float) (1 - 0.6 * val / dis));
//                        mRightHand.setBackgroundResource(R.drawable.right_hand);
//                    }
//                    mRightHand.setTranslationX(val);
//                }
//            });
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && requestCode == RESULT_OK) {
            finish();
        }
    }
}

