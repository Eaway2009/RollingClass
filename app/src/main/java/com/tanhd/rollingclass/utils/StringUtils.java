package com.tanhd.rollingclass.utils;

import com.tanhd.rollingclass.MainApp;
import com.tanhd.rollingclass.R;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StringUtils {

    /**
     * 将double转为数值，并最多保留num位小数。例如当num为2时，1.268为1.27，1.2仍为1.2；1仍为1，而非1.00;100.00则返回100。
     *
     * @param d
     * @param num 小数位数
     * @return
     */
    public static String double2String(double d, int num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(num);//保留两位小数
        nf.setGroupingUsed(false);//去掉数值中的千位分隔符

        String temp = nf.format(d);
        if (temp.contains(".")) {
            String s1 = temp.split("\\.")[0];
            String s2 = temp.split("\\.")[1];
            for (int i = s2.length(); i > 0; --i) {
                if (!s2.substring(i - 1, i).equals("0")) {
                    return s1 + "." + s2.substring(0, i);
                }
            }
            return s1;
        }
        return temp;
    }

    /**
     * 将double转为数值，并最多保留num位小数。
     *
     * @param d
     * @param num      小数个数
     * @param defValue 默认值。当d为null时，返回该值。
     * @return
     */
    public static String double2String(Double d, int num, String defValue) {
        if (d == null) {
            return defValue;
        }

        return double2String(d, num);
    }

    public static String secondToDate(long second) {
        return secondToDate(second, MainApp.getInstance().getString(R.string.lbl_ymd));
    }

    public static String secondToDate(long second,String patten) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(second);//转换为毫秒
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(patten);
        String dateString = format.format(date);
        return dateString;
    }

    public static String getFormatDate(Date resourse) {
        if (resourse != null) {
            SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
            return time.format(resourse);
        }
        return "";
    }

    public static String getFormatYear(Date resourse) {
        if (resourse != null) {
            SimpleDateFormat time = new SimpleDateFormat("yyyy年");
            return time.format(resourse);
        }
        return "";
    }

    public static String getFormatMonth(Date resourse) {
        if (resourse != null) {
            SimpleDateFormat time = new SimpleDateFormat(MainApp.getInstance().getString(R.string.lbl_moneth));
            return time.format(resourse);
        }
        return "";
    }

    public static String getFormatDate2(Date resourse) {
        if (resourse != null) {
            SimpleDateFormat time = new SimpleDateFormat(MainApp.getInstance().getString(R.string.lbl_m_d));
            return time.format(resourse);
        }
        return "";
    }

    public static String getFormatTime(Date resourse) {
        if (resourse != null) {
            SimpleDateFormat time = new SimpleDateFormat("HH : mm");
            return time.format(resourse);
        }
        return "";
    }
}
