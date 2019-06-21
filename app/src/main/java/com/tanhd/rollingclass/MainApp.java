package com.tanhd.rollingclass;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ACRA.init(this);
    }
}
