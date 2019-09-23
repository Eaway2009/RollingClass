package com.tanhd.rollingclass.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.RxTimerUtil;
import com.tanhd.rollingclass.utils.ScreenUtils;

/**
 * 从上方弹出的PopupWindow,仿iphone效果，增加半透明蒙层。
 * <p>
 * 实现原理：<br>
 * 在弹出自定义的PopupWindow时，增加一个半透明蒙层view到窗口，并置于PopupWindow下方。
 * </p>
 * <p>
 * 使用方法：<br>
 * 继承TopPushPopupWindow，编写generateCustomView添加自定义的view，调用show方法显示。
 * </p>
 *
 * @author y
 */
public abstract class TopPushPopupWindow<T> extends PopupWindow {

    protected Activity activity;
    protected Context context;
    private WindowManager wm;
    private View maskView;
    protected boolean hasMask = true;
    private int maskYOffset = 0; //背景偏移量


    @SuppressWarnings("deprecation")
    public TopPushPopupWindow(Context context, Activity activity, T t) {
        super(context);
        this.context = context;
        this.activity = activity;
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initType();
        setContentView(generateCustomView(t));
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOutsideTouchable(true);
        setFocusable(true);
        setBackgroundDrawable(context.getResources().getDrawable(android.R.color.transparent));
        setAnimationStyle(R.style.Animations_TopPush);
    }


    protected abstract View generateCustomView(T t);

    @TargetApi(23)
    private void initType() {
        // 解决华为手机在home建进入后台后，在进入应用，蒙层出现在popupWindow上层的bug。
        // android4.0及以上版本都有这个hide方法，根据jvm原理，可以直接调用，选择android6.0版本进行编译即可。
        setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
    }

    public int getMaskYOffset() {
        return maskYOffset;
    }

    public void setMaskYOffset(int maskYOffset) {
        this.maskYOffset = maskYOffset;
    }

    /**
     * 设置是否有蒙层
     *
     * @param hasMask
     */
    public TopPushPopupWindow<T> showMask(boolean hasMask) {
        this.hasMask = hasMask;
        return this;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        try {
            if (hasMask) {
                addMaskView(parent.getWindowToken());
            }
            super.showAtLocation(parent, gravity, x, y);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        try {
            if (hasMask) {
                addMaskView(anchor.getWindowToken());
            }
            super.showAsDropDown(anchor, xoff, yoff);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        try {
            RxTimerUtil.cancel();
            removeMaskView();
            super.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示在界面的底部
     */
    public void show(Activity activity) {
        showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void setOnDismissListener(final OnDismissListener onDismissListener) {
        super.setOnDismissListener(onDismissListener);
    }

    private void addMaskView(final IBinder token) {
        RxTimerUtil.timer(200, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                WindowManager.LayoutParams p = new WindowManager.LayoutParams();
                p.width = WindowManager.LayoutParams.MATCH_PARENT;
                //p.height = WindowManager.LayoutParams.MATCH_PARENT;
                //p.height = p.height - maskYOffset;
                p.gravity = Gravity.BOTTOM;
                p.height = ScreenUtils.getScreenHeight(context) - maskYOffset;
                p.format = PixelFormat.TRANSLUCENT;
                p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
                p.token = token;
                p.windowAnimations = android.R.style.Animation_Toast;
                maskView = new View(context);
                maskView.setBackgroundColor(Color.parseColor("#90000000"));
                maskView.setFitsSystemWindows(false);
                // 华为手机在home建进入后台后，在进入应用，蒙层出现在popupWindow上层，导致界面卡死，
                // 这里新增加按bug返回。
                // initType方法已经解决该问题，但是还是留着这个按back返回功能，防止其他手机出现华为手机类似问题。
                maskView.setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            removeMaskView();
                            return true;
                        }
                        return false;
                    }
                });
                wm.addView(maskView, p);
            }
        });
    }

    private void removeMaskView() {
        if (maskView != null) {
            maskView.setVisibility(View.GONE);
            wm.removeViewImmediate(maskView);
            maskView = null;
        }
    }


}