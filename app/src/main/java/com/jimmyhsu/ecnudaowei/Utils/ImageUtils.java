package com.jimmyhsu.ecnudaowei.Utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.jimmyhsu.ecnudaowei.MyApplication;
import com.jimmyhsu.ecnudaowei.R;
import com.jimmyhsu.ecnudaowei.View.RoundDrawable;

/**
 * Created by jimmyhsu on 2016/10/24.
 */

public class ImageUtils {

    public static void downloadToImageView(final String userheadUrl, final ImageView headIv) {
        Bitmap cachedBm = MyApplication.getVolleyCache().getBitmap(userheadUrl);
        if (cachedBm != null) {
            RoundDrawable drawable = new RoundDrawable(cachedBm);
            headIv.setImageDrawable(drawable);
            return;
        }
        ImageRequest request = new ImageRequest(userheadUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap bitmap) {
                RoundDrawable drawable = new RoundDrawable(bitmap);
                headIv.setImageDrawable(drawable);
                MyApplication.getVolleyCache().putBitmap(userheadUrl, bitmap);
            }
        }, 500, 500, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                headIv.setImageResource(R.drawable.userdefault);
            }
        });
        MyApplication.getRequestQueue().add(request);
    }

    public static Bitmap drawableToBitamp(Drawable drawable)
    {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w,h,config);
        //注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}
