package com.jimmyhsu.ecnudaowei.Utils;

import android.widget.TextView;

/**
 * Created by jimmyhsu on 2016/10/26.
 */

public class TextViewUtils {
    public static void bindTextToTextView(String s, TextView tv) {
        if (s != null && tv != null) {
            tv.setText(s);
        }
    }
}
