package com.tanhd.rollingclass.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by YangShlai on 2019-10-21.
 */
public class TimeUtils {
    public static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * 将时间戳转为时间字符串
     * <p>格式为format</p>
     * @param millis 毫秒时间戳
     * @param format 时间格式
     * @return 时间字符串
     */
    public static String longToStr(long millis, DateFormat format) {
        try {
            return format.format(new Date(millis));
        } catch (Exception e) {
            return "";
        }

    }
}
