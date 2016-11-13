package com.jimmyhsu.ecnudaowei.View;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;

/**
 * Created by jianghejie on 15/9/7.
 */

public class FishProgressBar extends View
{
    private Path path;
    private Path mDst;
    private PathMeasure mPathMeasure;
    private Paint paint;
    private float length;
    private Paint mPaintRect;
    private RectF mRectF = new RectF();
    private int mCenterX;
    private int mCenterY;
    private boolean once = false;
    private ObjectAnimator animator;

    public FishProgressBar(Context context)
    {
        super(context);
    }

    public FishProgressBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FishProgressBar(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public void init()
    {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        mPaintRect = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRect.setColor(0xEE9F9F9F);
        mPaintRect.setStrokeCap(Paint.Cap.ROUND);
        mPaintRect.setStyle(Paint.Style.FILL);


        path = new Path();
        path.moveTo(mCenterX - 110, mCenterY + 15);
        path.quadTo(mCenterX - 30, mCenterY - 75, mCenterX + 70 ,mCenterY + 15);
        path.quadTo(mCenterX + 90, mCenterY + 50, mCenterX + 150, mCenterY + 65);
        path.lineTo(mCenterX + 150, mCenterY - 35);
        path.quadTo(mCenterX + 90, mCenterY - 25, mCenterX + 70, mCenterY + 5);
        path.quadTo(mCenterX - 30, mCenterY + 95, mCenterX - 110, mCenterY + 15);
        path.close();

        mDst = new Path();

        mPathMeasure = new PathMeasure(path, false);

        // Measure the path
        PathMeasure measure = new PathMeasure(path, false);
        length = measure.getLength();

        animator = ObjectAnimator.ofFloat(this, "phase", 0, 1);
        animator.setDuration(1500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.start();
    }

    //is called by animtor object
    public void setPhase(float phase)
    {
//        Log.d("pathview", "setPhase called with:" + String.valueOf(phase));
//        paint.setPathEffect(createPathEffect(length, phase, 0.0f, once));
        mDst.reset();
        mPathMeasure.getSegment(Math.max(0, phase * length - 3f * phase * length
                + 3f * phase * phase * length), phase * length, mDst, true);
        invalidate();//will calll onDraw
    }

    public void dismiss() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        this.setVisibility(GONE);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectF.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
        mCenterX = getMeasuredWidth() / 2;
        mCenterY = getMeasuredHeight() / 2;
        init();
    }

    @Override
    public void onDraw(Canvas c)
    {
        super.onDraw(c);
        c.drawRoundRect(mRectF, 20, 20, mPaintRect);
        c.drawPath(mDst, paint);
    }
}


