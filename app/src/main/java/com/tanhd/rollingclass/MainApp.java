package com.tanhd.rollingclass;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import com.bumptech.glide.util.LogTime;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tanhd.rollingclass.utils.Logger;
import com.tanhd.rollingclass.utils.ScreenUtils;
import com.tanhd.rollingclass.utils.configs.BaiduCrashConfigs;
import com.tanhd.rollingclass.utils.langeuage.MultiLanguageUtil;

@ReportsCrashes(
        logcatArguments = { "-t", "1000", "-v", "long", "*:S", "LogEng"},
        reportSenderFactoryClasses={com.tanhd.rollingclass.utils.MyMailSenderfactory.class},
        mode = ReportingInteractionMode.TOAST,
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE,
                ReportField.PHONE_MODEL,
                ReportField.BUILD,
                ReportField.BRAND,
                ReportField.PRODUCT,
                ReportField.TOTAL_MEM_SIZE,
                ReportField.AVAILABLE_MEM_SIZE,
                ReportField.BUILD_CONFIG,
                ReportField.CUSTOM_DATA,
                ReportField.INITIAL_CONFIGURATION,
                ReportField.CRASH_CONFIGURATION,
                ReportField.DISPLAY,
                ReportField.USER_COMMENT,
                ReportField.USER_APP_START_DATE,
                ReportField.USER_CRASH_DATE,
                ReportField.DUMPSYS_MEMINFO,
                ReportField.LOGCAT,
                ReportField.EVENTSLOG,
                ReportField.RADIOLOG,
                ReportField.DEVICE_ID,
                ReportField.INSTALLATION_ID,
                ReportField.DEVICE_FEATURES,
                ReportField.ENVIRONMENT,
                ReportField.SETTINGS_SYSTEM,
                ReportField.SETTINGS_SECURE,
                ReportField.SETTINGS_GLOBAL,
                ReportField.SHARED_PREFERENCES,
                ReportField.APPLICATION_LOG,
                ReportField.MEDIA_CODEC_LIST,
                ReportField.THREAD_DETAILS,
                ReportField.USER_IP
        },
        resToastText = R.string.toast_crash
)
public class MainApp extends Application {
    private static final String TAG = "MainApp";
    
    private static final int THREAD_COUNT = 2;
    private static final int PRIORITY = 2;
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;
    private static final int CONNECTION_TIME_OUT = 5 * 1000;
    private static final int READ_TIME_OUT = 30 * 1000;
    private static Application mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Logger.setDebug(BuildConfig.DEBUG);
        initImageLoader(getApplicationContext());
        //Crash统计
        BaiduCrashConfigs.init(this);
        //多语言初始化
        MultiLanguageUtil.init(this);
        
        Logger.i(TAG,"分辨率>>>" + ScreenUtils.getScreenHeight(this) + "x" + ScreenUtils.getScreenWidth(this) + "|sw:" + getString(R.string.base_dpi));
    }

    public static Context getInstance() {
        return mInstance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        ACRA.init(this);
    }

    private void initImageLoader(Context context){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(context)
                .threadPoolSize(THREAD_COUNT)
                .threadPriority(Thread.NORM_PRIORITY - PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(DISK_CACHE_SIZE)
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候URL加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .defaultDisplayImageOptions(getDefaultOptions())
                .imageDownloader(new BaseImageDownloader(context, CONNECTION_TIME_OUT,READ_TIME_OUT))
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * 默认的图片显示Options,可设置图片的缓存策略，编解码方式等，非常重要
     *
     * @return
     */
    private DisplayImageOptions getDefaultOptions() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)//设置下载的图片是否缓存在内存中, 重要，否则图片不会缓存到内存中
                .cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中, 重要，否则图片不会缓存到硬盘中
                .considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)//设置图片以如何的编码方式显示
                .bitmapConfig(Bitmap.Config.RGB_565)//设置图片的解码类型//
                .decodingOptions(new BitmapFactory.Options())//设置图片的解码配置
                .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
                .displayer(new FadeInBitmapDisplayer(300))//设置加载图片的task（这里是渐现）
                .build();
        return options;
    }
}
