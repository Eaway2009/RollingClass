package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.MainApp;
import com.tanhd.rollingclass.R;

import java.lang.ref.WeakReference;


public class ToastUtil {

    private static Toast toast = null;
    private static WeakReference<Toast> mShowingToastRef;
    private static TextView mTextView;

    private ToastUtil() {
    }

    public static void show(CharSequence text) {
        //***************************自定义Toast*******************************
        if (TextUtils.isEmpty(text))
            return;
        if (mShowingToastRef != null) {
            toast = mShowingToastRef.get();
        }
        if (toast == null) {  //保证不重复弹出
            toast = new Toast(MainApp.getInstance().getApplicationContext());
            mShowingToastRef = new WeakReference<>(toast);
        }
        //加载Toast布局
        View toastRoot = LayoutInflater.from(MainApp.getInstance().getApplicationContext()).inflate(R.layout.toast, null);
        //初始化布局控件
        mTextView = (TextView) toastRoot.findViewById(R.id.message);
        //为控件设置属性
        mTextView.setText(text);
        //Toast的初始化
        //获取屏幕高度
        WindowManager wm = (WindowManager) MainApp.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        //Toast的Y坐标是屏幕高度的1/3，不会出现不适配的问题
        toast.setGravity(Gravity.TOP, 0, height  * 2 / 5);
        if (text.length() > 10){
            toast.setDuration(Toast.LENGTH_LONG);
        }else{
            toast.setDuration(Toast.LENGTH_SHORT);
        }

        toast.setView(toastRoot);
        toast.show();
    }

    public static void show(@StringRes int resId) {
        show(MainApp.getInstance().getApplicationContext().getString(resId));
    }

}