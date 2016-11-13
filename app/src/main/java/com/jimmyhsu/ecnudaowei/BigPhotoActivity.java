package com.jimmyhsu.ecnudaowei;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by jimmyhsu on 2016/10/13.
 */
public class BigPhotoActivity extends Activity {

    private static final String EXTRA_BITMAP_URL = "extra_bitmap_url";
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImageView contentView = new ImageView(this);
        contentView.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(contentView);
        downloadBitmap(getIntent().getStringExtra(EXTRA_BITMAP_URL), contentView, mAttacher);
        mAttacher = new PhotoViewAttacher(contentView);
    }

    public static void startActivity(Context context, String url, Bundle options) {
        Intent intent = new Intent(context, BigPhotoActivity.class);
        intent.putExtra(EXTRA_BITMAP_URL, url);
        if (options != null) {
            context.startActivity(intent, options);
        } else {
            context.startActivity(intent);
        }
    }

//    public static void startActivity(Context context, String url) {
//        Intent intent = new Intent(context, BigPhotoActivity.class);
//        intent.putExtra(EXTRA_BITMAP_URL, url);
//        context.startActivity(intent);
//    }

    public static void downloadBitmap(String url, ImageView imageView, PhotoViewAttacher attacher) {
        ImageLoader imageLoader = new ImageLoader(MyApplication.getRequestQueue(), MyApplication.getVolleyCache());
        ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView, R.drawable.picdefault, R.drawable.picerror);
        imageLoader.get(url, imageListener);
        if (attacher != null) {
            attacher.update();
        }
    }
}
