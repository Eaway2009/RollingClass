package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewEx extends WebView {
    public WebViewEx(Context context) {
        this(context,null);
    }

    public WebViewEx(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WebViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;//super.onTouchEvent(event);
    }
}
