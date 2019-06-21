package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import org.acra.config.ACRAConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

public class MyMailSenderfactory  implements ReportSenderFactory {
    /***
     * 注意这里必须要是空的构造方法
     */
    public MyMailSenderfactory() {
    }

    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull ACRAConfiguration acraConfiguration) {
        return new MyMailSender();
    }
}