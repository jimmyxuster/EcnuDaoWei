package com.jimmyhsu.ecnudaowei.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by jimmyhsu on 2016/10/13.
 */
public class ScrollViewWithViewPager extends ScrollView {

    private float xDistance;
    private float yDistance;
    private float xLast;
    private float yLast;

    public ScrollViewWithViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollViewWithViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollViewWithViewPager(Context context) {
        super(context);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0.0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();

                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);

                if(xDistance > yDistance)
                    return false;

                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    public interface ScrollViewListener {

        void onScrollChanged(ScrollViewWithViewPager scrollView, int x, int y, int oldx, int oldy);

    }
    private ScrollViewListener mListner;
    public void setOnSrcollChangeListner(ScrollViewListener l) {
        mListner = l;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (mListner != null) {
            mListner.onScrollChanged(this, x, y, oldx, oldy);
        }
    }
}