package com.jimmyhsu.ecnudaowei.View;

/**
 * Created by jimmyhsu on 2016/10/17.
 */
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {

    private boolean scrollble = true;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (!scrollble) {
//
//            return true;
//        }
//        return super.onTouchEvent(ev);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!scrollble) {
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return false;
    }


    public boolean isScrollble() {
        return scrollble;
    }

    public void setScrollble(boolean scrollble) {
        this.scrollble = scrollble;
    }
}
