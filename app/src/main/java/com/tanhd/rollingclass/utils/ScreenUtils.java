package com.tanhd.rollingclass.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 屏幕相关工具类
 */
public final class ScreenUtils {

    private ScreenUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断是否是全面屏
     */
    private volatile static boolean mHasCheckAllScreen;
    private volatile static boolean mIsAllScreenDevice;

    public static boolean isAllScreenDevice(Context context) {
        if (mHasCheckAllScreen) {
            return mIsAllScreenDevice;
        }
        mHasCheckAllScreen = true;
        mIsAllScreenDevice = false;
        // 低于 API 21的，都不会是全面屏。。。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            float width, height;
            if (point.x < point.y) {
                width = point.x;
                height = point.y;
            } else {
                width = point.y;
                height = point.x;
            }
            if (height / width >= 1.97f) {
                mIsAllScreenDevice = true;
            }
        }
        return mIsAllScreenDevice;
    }

    /**
     * 获得屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        // 获取宽度
        int width = dm.widthPixels;
        return width;
    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getRealMetrics(dm);
        // 获取高度
        int height = dm.heightPixels;
        return height;
    }

    /**
     * 虚拟按键是否显示 true 是  false 否
     * @param context
     * @return
     */
    public static boolean isNavigationBarShow(Context context) {
        //虚拟键的view,为空或者不可见时是隐藏状态
        Activity activity = null;
        if (context instanceof Activity){
            activity = (Activity) context;
        }
        if (activity == null) return false;

        View view = activity.findViewById(android.R.id.navigationBarBackground);
        if (view == null) {
            return false;
        }
        int visible = view.getVisibility();
        if (visible == View.GONE || visible == View.INVISIBLE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 设置 View 的宽高比例
     *
     * @param context
     * @param view
     * @param scale w/h
     * @param offsetdp 宽度偏移dp
     * @return
     */
    public static void setViewScale(Context context, View view, float offsetdp, float scale) {
        if (context == null) return;
        if (context instanceof Activity){
            Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) return;
        }

        if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.width = getScreenWidth(context) - dp2px(context, offsetdp);
            if (scale == 0){
                params.height = 1;
            }else{
                params.height = (int) (params.width / scale);
            }
            Logger.i("ysl", "比例以后>>" + params.width + "|" + params.height);
            view.setLayoutParams(params);
        } else if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
            params.width = getScreenWidth(context) - dp2px(context, offsetdp);
            if (scale == 0){
                params.height = 1;
            }else{
                params.height = (int) (params.width / scale);
            }
            Logger.i("ysl", "比例以后>>" + params.width + "|" + params.height);
            view.setLayoutParams(params);
        } else if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
            params.width = getScreenWidth(context) - dp2px(context, offsetdp);
            if (scale == 0){
                params.height = 1;
            }else{
                params.height = (int) (params.width / scale);
            }
            Logger.i("ysl", "比例以后>>" + params.width + "|" + params.height);
            view.setLayoutParams(params);
        }
    }


    /**
     * 获取当前屏幕截图，包含状态栏
     */
    public static Bitmap captureWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap ret = Bitmap.createBitmap(bmp, 0, 0, dm.widthPixels, dm.heightPixels);
        view.destroyDrawingCache();
        return ret;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity activity
     * @return Bitmap
     */
    public static Bitmap captureWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int statusBarHeight = getStatusHeight(activity);
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap ret = Bitmap.createBitmap(bmp, 0, statusBarHeight, dm.widthPixels, dm.heightPixels - statusBarHeight);
        view.destroyDrawingCache();
        return ret;
    }

    /**
     * 获得状态栏的高度
     */
    public static int getStatusHeight(Context context) {
        return getInternalDimensionSize(context.getResources(), "status_bar_height");
    }

    private static int getInternalDimensionSize(Resources res, String key) {
        int result = 0;
        int resourceId = res.getIdentifier(key, "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @SuppressLint("NewApi")
    public static boolean checkDeviceHasNavigationBar(Context activity) {
        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    /**
     * 获取虚拟键盘高度
     * @param activity
     * @return
     */
    public static int getNavigationBarHeight(Context activity) {
        //!checkDeviceHasNavigationBar(activity) ||
        if (!isNavigationBarShow(activity)) {
            return 0;
        }

        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }


    /**
     * dpתpx
     */
    public static int dp2px(Context ctx, float dpValue) {
        if (ctx == null) return 0;

        final float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * pxתdp
     */
    public static int px2dip(Context ctx, float pxValue) {
        if (ctx == null) return 0;

        final float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
