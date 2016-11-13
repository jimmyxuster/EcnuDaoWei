package com.jimmyhsu.ecnudaowei;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.jimmyhsu.ecnudaowei.View.FishProgressBar;
import com.jimmyhsu.ecnudaowei.View.StatusBarCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jimmyhsu on 2016/10/14.
 */
public class GeneralWebViewActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "extra_url";

    @BindView(R.id.id_general_webview)
    WebView mWebView;
    @BindView(R.id.id_progress)
    FishProgressBar mProgress;

//    private Toolbar mToolbar;
    private String mUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_webview);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mToolbar.setElevation(0);
//        }
//        mToolbar.getBackground().setAlpha(0);
//        mToolbar.setTitle("");
        mUrl = getIntent().getStringExtra(EXTRA_URL);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgress.dismiss();
            }
        });
        mWebView.loadUrl(mUrl);
    }


    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, GeneralWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        context.startActivity(intent);
    }
//    public static int getStatusBarHeight(Context context) {
//        int result = 0;
//        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = context.getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
