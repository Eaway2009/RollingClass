package com.tanhd.rollingclass.base;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.tanhd.rollingclass.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * 基础Fragment
 * Created by YangShlai on 2019-09-24.
 */
public class BaseFragment extends Fragment {
    protected Handler mHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Toast
     * @param toast
     */
    protected void showToast(String toast){
        ToastUtil.show(toast);
    }

    @Override
    public void onDestroy() {
        // 移除所有消息
        if (mHandler != null) {
            mHandler.removeMessages(0);
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

}
