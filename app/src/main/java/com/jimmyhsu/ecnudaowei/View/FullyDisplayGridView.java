package com.jimmyhsu.ecnudaowei.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Created by jimmyhsu on 2016/10/18.
 */
public class FullyDisplayGridView extends GridView {
    public FullyDisplayGridView(Context context) {
        super(context);
    }

    public FullyDisplayGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullyDisplayGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
