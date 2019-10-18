package com.tanhd.rollingclass.db;

import android.content.Context;
import android.content.Intent;

import com.tanhd.rollingclass.LoginActivity;
import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.UserData;
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
     * 保存用户Data
     * @param userData
     */
    public void setUserData(UserData userData){
        ACache.getInstance().put(ACacheHelper.USER_DATA,userData);
    }

    /**
     * 获取用户Data
     * @return
     */
    public UserData getUserData() {
        if (null == ACache.getInstance().getAsObject(ACacheHelper.USER_DATA)) {
            return new UserData();
        }
        return (UserData) ACache.getInstance().getAsObject(ACacheHelper.USER_DATA);
    }

    /**
     * 保存ClassData
     * @param classData
     */
    public void setClassData(ClassData classData){
        ACache.getInstance().put(ACacheHelper.CLASS_DATA,classData);
    }

    /**
     * 获取ClassData
     * @return
     */
    public ClassData getClassData() {
        if (null == ACache.getInstance().getAsObject(ACacheHelper.CLASS_DATA)) {
            return new ClassData();
        }
        return (ClassData) ACache.getInstance().getAsObject(ACacheHelper.CLASS_DATA);
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
