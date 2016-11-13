package com.jimmyhsu.ecnudaowei.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.jimmyhsu.ecnudaowei.LoginActivity;

/**
 * Created by jimmyhsu on 2016/10/11.
 */
public class PositionIndicator extends View {

    private static final int DEFAULT_COLOR = 0xff777777;
    private static final int DEFAULT_SELECTED_COLOR = 0xffff7043;
    private static final int DEFAULT_RADIUS_IN_DP = 3;
    private static final int DEFAULT_PADDING_IN_DP = 5;
    private int mColor;
    private int mSelectedColor;
    private int mRadius;
    private int mWidth;
    private int mHeight;
    private int mPadding;
    private int mTotalNumber = 4;
    private int mCurrent = 2;

    private Paint mPaint;


    public int getTotalNumber() {
        return mTotalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        this.mTotalNumber = totalNumber;
        if (totalNumber == 1) {
            setVisibility(INVISIBLE);
        } else {
            setVisibility(VISIBLE);
        }
        invalidate();
    }

    public int getCurrent() {
        return mCurrent;
    }

    public void setCurrent(int current) {
        this.mCurrent = current;
        if (mTotalNumber > 0) {
            invalidate();
        }
    }

    public PositionIndicator(Context context) {
        this(context, null);
    }

    public PositionIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PositionIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mColor = DEFAULT_COLOR;
        mSelectedColor = DEFAULT_SELECTED_COLOR;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRadius = LoginActivity.dp2px(DEFAULT_RADIUS_IN_DP, getContext());
        mPadding = LoginActivity.dp2px(DEFAULT_PADDING_IN_DP, getContext());
        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float center = mWidth / 2f;
        float centerY = mHeight / 2f;
        int widthTotal = mRadius * mTotalNumber + mPadding * (mTotalNumber - 1);
        float left = center - widthTotal / 2f;
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(10f);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mTotalNumber; i++) {
            if (i == mCurrent) {
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mPaint.setColor(mSelectedColor);
                canvas.drawLine(left, centerY, left + 3 * mRadius, centerY, mPaint);
                left += (3 * mRadius + mPadding * 1.5f);
            } else {
                mPaint.setColor(mColor);
                canvas.drawCircle(left + mRadius / 2f, mHeight / 2f, mRadius, mPaint);
                left += (2 * mRadius + mPadding);
            }
        }
    }
}
