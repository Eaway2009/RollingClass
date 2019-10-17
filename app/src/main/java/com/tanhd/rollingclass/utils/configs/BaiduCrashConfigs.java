package com.tanhd.rollingclass.utils.configs;

import android.app.Application;
import com.baidu.crabsdk.CrabSDK;

/**
 * 百度 bug 统计
 * Created by YangShlai
 */
public class BaiduCrashConfigs {
    public static void init(Application application){
        //线下环境 - 线上环境
        CrabSDK.init(application,"e952df96bc40e1db");
        CrabSDK.setCollectScreenshot(true);  // 开启截屏收集功能，默认关闭
    }

}
