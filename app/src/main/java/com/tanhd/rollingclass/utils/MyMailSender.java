package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

public class MyMailSender implements ReportSender {

    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData crashReportData) throws ReportSenderException {
        //发送邮件
//        Log.i("YourOwnSender", "send: " + crashReportData.toJSON());

        Mail mail=new Mail("shen21cn@163.com","shine563638");
        mail.set_to(new String[]{"1040731200@qq.com"});//接受者邮箱 可以是多个
        mail.set_from("shen21cn@163.com");//邮件来源
        mail.set_subject("翻转课堂错误日志");//设置主题标题
        mail.setBody(crashReportData.toString());
        try {
            if( mail.send()){
                Log.i("YourOwnSender", "send: 发送成功");
            }else{
                Log.i("YourOwnSender", "send: 发送失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
