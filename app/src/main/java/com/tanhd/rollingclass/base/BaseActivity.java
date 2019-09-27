package com.tanhd.rollingclass.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.tanhd.rollingclass.db.model.EventTag;
import com.tanhd.rollingclass.utils.AutoHideKeyboard;
import com.tanhd.rollingclass.utils.BarTextColorUtils;
import com.tanhd.rollingclass.utils.Logger;
import com.tanhd.rollingclass.utils.StatusBarUtil;
import com.tanhd.rollingclass.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 基础Activity
 * Created by YangShlai on 2019-09-22.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        AutoHideKeyboard.init(this);
        setStatusBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.i("onResume",TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Toast
     * @param toast
     */
    protected void showToast(String toast){
        ToastUtil.show(toast);
    }

    /**
     * 状态栏设置
     */
    protected void setStatusBar() {
        //ImmersionBar.with(this).hideBar(BarHide.FLAG_HIDE_STATUS_BAR).init();
//        StatusBarUtil.setTransparentForImageView(this, null);
//        BarTextColorUtils.setDarkStatusIcon(this, false);
    }

}
