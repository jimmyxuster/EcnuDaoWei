package com.jimmyhsu.ecnudaowei.View;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.jimmyhsu.ecnudaowei.LoginActivity;
import com.jimmyhsu.ecnudaowei.R;

/**
 * Created by jimmyhsu on 2016/10/23.
 */

public class DwItemView extends LinearLayout {

    private OverScroller mScroller;
    private int mWidth;
    private float mLastX;
    private VelocityTracker mTracker;
    private int mTouchSlop;
    private int mFlingSpeed;
    private int mMaxFling;
    private int mSpace;
    private boolean isDragging = false;
    private ValueAnimator mAnimator;

    public DwItemView(Context context) {
        this(context, null);
    }

    public DwItemView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DwItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        setFocusable(true);
        mScroller = new OverScroller(getContext());
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mMaxFling = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        mFlingSpeed = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mSpace = LoginActivity.dp2px(8, context);//change this to fit divider drawable
        setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        setDividerDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider_space));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
    }

    private void initVelocityTracker() {
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mTracker != null) {
            mTracker.recycle();
        }
        mTracker = null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            mLastX = ev.getX();
        } else if (action == MotionEvent.ACTION_MOVE) {
            float dx = ev.getX() - mLastX;
            if (Math.abs(dx) > mTouchSlop) {
                isDragging = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float dx = x - mLastX;
        initVelocityTracker();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                if (mAnimator != null && mAnimator.isRunning()) {
                    mAnimator.cancel();
                    mAnimator = null;
                }
                mTracker.clear();
                mTracker.addMovement(event);
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isDragging && Math.abs(dx) > mTouchSlop) {
                    isDragging = true;
                }
                if (isDragging) {
                    scrollBy(-(int)dx, 0);
                    invalidate();
                }

                mTracker.addMovement(event);
                mLastX = x;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(dx) > mTouchSlop) {
                    mScroller.startScroll((int)mLastX, 0, -(int)dx, 0);
                    invalidate();
                }
                isDragging = false;
                mTracker.addMovement(event);
                mTracker.computeCurrentVelocity(1000, mMaxFling);
                fling(-mTracker.getXVelocity());
//                Log.e("dwitemview", "x v = " + mTracker.getXVelocity());
                recycleVelocityTracker();
                break;
        }
        return super.onTouchEvent(event);
    }

    public void scrollToPos(int pos) {
        if (pos > getChildCount() - 1) {
            return;
        }
        int x = pos * (mWidth + mSpace);
        scrollTo(x, 0);
    }

    public interface OnItemSelectListner {
        void onSelectChange(int pos);
    }

    private OnItemSelectListner mListner;

    public void setOnItemSelectListner(OnItemSelectListner l) {
        mListner = l;
    }

    private void fling(float velocity) {
        int currX = getScrollX();
        int firstVisibleItem = currX / (mWidth + mSpace);
        int exceed = currX % (mWidth + mSpace);
//        Log.e("dwitemview", "fvi = " + firstVisibleItem + ", exceed = " + exceed);
        if (velocity > mFlingSpeed) { //go to right
            animateToRight(exceed);
            if (firstVisibleItem + 1 < getChildCount()) {
                callback(firstVisibleItem + 1);
            }
        }else if (velocity < -mFlingSpeed) { //go to left
            animateToLeft(exceed);
            callback(firstVisibleItem);
        }else if (exceed > mWidth / 2) {
            animateToRight(exceed);
            if (firstVisibleItem + 1 < getChildCount()) {
                callback(firstVisibleItem + 1);
            }
        } else {
            animateToLeft(exceed);
            callback(firstVisibleItem);
        }
    }

    private void callback(int pos) {
        if (mListner != null) {
            mListner.onSelectChange(pos);
        }
    }

    private void animateToLeft(int exceed) {
        mAnimator = ValueAnimator.ofFloat(getScrollX(), getScrollX() - exceed);
        mAnimator.setDuration(300);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                scrollTo((int) val, 0);
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator = null;
            }
        });
        mAnimator.start();
    }

    private void animateToRight(int exceed) {
        mAnimator = ValueAnimator.ofFloat(getScrollX(),  mSpace + getScrollX() + mWidth - exceed);
        mAnimator.setDuration(300);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                scrollTo((int) val, 0);
            }
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator = null;
            }
        });
        mAnimator.start();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < 0) {
            x = 0;
        }
        if (x > (mWidth + mSpace) * (getChildCount() - 1)) {
            x = (mWidth + mSpace) * (getChildCount() - 1);
        }
        super.scrollTo(x, y);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        }

    }
}
