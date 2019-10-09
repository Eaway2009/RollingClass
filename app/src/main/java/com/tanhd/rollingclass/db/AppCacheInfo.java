package com.tanhd.rollingclass.db;

import android.content.Context;
import android.content.Intent;

import com.tanhd.rollingclass.LoginActivity;
import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.utils.AppUtils;

import java.io.Serializable;

/**
 * App缓存管理
 */
public class AppCacheInfo implements Serializable {
    private AppCacheInfo(){
    }
    private static class SingletonHolder{
        private static final AppCacheInfo instance = new AppCacheInfo();
    }

    public static AppCacheInfo getInstance() {
        return SingletonHolder.instance;
    }


    /**
     * 注销登录
     */
    public void logOut(Context context){

        AppUtils.clearLoginInfo(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

}