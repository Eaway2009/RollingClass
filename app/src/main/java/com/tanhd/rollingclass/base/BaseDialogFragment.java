package com.tanhd.rollingclass.base;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.KeyboardUtils;
import com.tanhd.rollingclass.utils.annotate.ViewAnnotationUtil;

/**
 * 基本 DialogFragment
 * Created by YangShlai on 2018/7/23.
 */
public abstract class BaseDialogFragment extends DialogFragment {
    protected Handler handler = new Handler();
    protected Window window;
    protected View contentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentView = createContentView(getContentView());
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return contentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(contentView);
        initData();
    }

    /**
     * 创建视图
     *
     * @param layoutResID
     * @return
     */
    public View createContentView(int layoutResID) {
        View view = getActivity().getLayoutInflater().inflate(layoutResID, null);
        ViewAnnotationUtil.autoInjectAllField(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            window = dialog.getWindow();

            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setLayout((int) (dm.widthPixels * 0.26), ViewGroup.LayoutParams.WRAP_CONTENT);
            setAnim();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置动画
     */
    protected void setAnim() {
        //添加这一行
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setWindowAnimations(R.style.dialogAnim);
    }

    /**
     * 为了解决:mainActivity调用onSaveInstanceState以后又调用了show方法,
     * 出现的Can not perform this action after onSaveInstanceState
     * 这个异常(不应该用commit ,而是用commitAllowingStateLoss)
     *
     * @param manager
     * @param tag
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
//            if (manager == null || getActivity() == null || getActivity().isDestroyed() || isAdded()) return;
            super.show(manager, tag);
        } catch (IllegalStateException ignore) {
            //  容错处理,不做操作
        }
    }

    /**
     * 注意,不要用super.dismiss(),bug 同上show()
     * super.onDismiss就没问题
     */
    public void dismissDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            super.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        KeyboardUtils.hideSoftInput(getActivity());
    }

    /**
     * 初始化视图
     */
    protected abstract int getContentView();

    /**
     * 初始化布局
     */
    protected abstract void initView(View view);

    /**
     * 初始化数据
     */
    protected abstract void initData();
}
