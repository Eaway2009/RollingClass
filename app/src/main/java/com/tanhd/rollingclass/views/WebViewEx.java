package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class WebViewEx extends WebView {
    public WebViewEx(Context context) {
        super(context);
    }

    public WebViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WebViewEx(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;//super.onTouchEvent(event);
    }
}
