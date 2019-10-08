package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.tanhd.rollingclass.MainApp;
import com.tanhd.rollingclass.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class AppUtils {
    public final static String[] OPTION_NO = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
    private static String mLocalIP = null;

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 使用SharedPreferences保存用户登录信息
     *
     * @param context
     * @param username
     * @param password
     */
    public static void saveLoginInfo(Context context, String username, String password,boolean isTeacher) {
        //获取SharedPreferences对象
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor = sharedPre.edit();
        //设置参数
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("isTeacher",isTeacher);
        //提交
        editor.commit();
    }

    public static String readLoginUserName(Context context) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        String username = sharedPre.getString("username", "");
        return username;
    }

    public static String readLoginPassword(Context context) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        String password = sharedPre.getString("password", "");
        return password;
    }

    public static boolean readLoginTeacherStatus(Context context) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        boolean isTeacher = sharedPre.getBoolean("isTeacher", false);
        return isTeacher;
    }

    public static void clearLoginInfo(Context context) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPre.edit();
        editor.remove("username");
        editor.remove("password");
        editor.commit();
    }

    public static String getLocalIP(Context context) {
        if (mLocalIP != null)
            return mLocalIP;

        if (context == null)
            return null;

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        mLocalIP = (ipAddress & 0xFF) + "." + ((ipAddress >> 8) & 0xFF) + "." + ((ipAddress >> 16) & 0xFF) + "." + (ipAddress >> 24 & 0xFF);
        return mLocalIP;
    }

    public static int ipToInt(String strIp) {
        int[] ip = new int[4];

        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Integer.parseInt(strIp.substring(0, position1));
        ip[1] = Integer.parseInt(strIp.substring(position1 + 1, position2));
        ip[2] = Integer.parseInt(strIp.substring(position2 + 1, position3));
        ip[3] = Integer.parseInt(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    public static boolean compareNetmark(String ip1, String ip2) {
        int v1 = ipToInt(ip1);
        int v2 = ipToInt(ip2);

        return ((v1 & 0xFFFF0000) == (v2 & 0xFFFF0000));
    }

    public static void playBeepSoundAndVibrate(Context context, final MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    try {
                        mediaPlayer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    try {
                        mediaPlayer.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            try {
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }

    public static String dateToString(long date) {
        Date d = new Date(date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(d);
    }

    public static String getStudySectionNameByCode(int code) {
        switch (code) {
            case 1:
                return MainApp.getInstance().getString(R.string.class_primary);
            case 2:
                return MainApp.getInstance().getString(R.string.class_middle);
            case 3:
                return MainApp.getInstance().getString(R.string.class_senior);
        }

        return "Unknown";
    }

    public static String getSubjectNameByCode(int code) {
        switch (code) {
            case 1:
                return MainApp.getInstance().getString(R.string.subject_cn);
            case 2:
                return MainApp.getInstance().getString(R.string.subject_mathematics);
            case 3:
                return MainApp.getInstance().getString(R.string.subject_en);
            case 4:
                return MainApp.getInstance().getString(R.string.subject_music);
            case 5:
                return MainApp.getInstance().getString(R.string.subject_fine);
            case 6:
                return MainApp.getInstance().getString(R.string.subject_physical);
            case 7:
                return MainApp.getInstance().getString(R.string.subject_information);
            case 8:
                return MainApp.getInstance().getString(R.string.subject_synthesize);
            case 9:
                return MainApp.getInstance().getString(R.string.subject_calligraphy);
            case 10:
                return MainApp.getInstance().getString(R.string.subject_science);
            case 11:
                return MainApp.getInstance().getString(R.string.subject_thought);
            case 12:
                return MainApp.getInstance().getString(R.string.subject_sw);
            case 13:
                return MainApp.getInstance().getString(R.string.subject_zj);
            case 14:
                return MainApp.getInstance().getString(R.string.subject_wl);
            case 15:
                return MainApp.getInstance().getString(R.string.subject_hx);
            case 16:
                return MainApp.getInstance().getString(R.string.subject_xx);
            case 17:
                return MainApp.getInstance().getString(R.string.subject_ls);
            case 18:
                return MainApp.getInstance().getString(R.string.subject_dl);
            case 19:
                return MainApp.getInstance().getString(R.string.subject_xljk);
            case 20:
                return MainApp.getInstance().getString(R.string.subject_ddfz);
        }
        return "Unknown";
    }


    public static String getGradeNameByCode(int code) {
        switch (code) {
            case 1:
                return MainApp.getInstance().getString(R.string.class_one);
            case 2:
                return MainApp.getInstance().getString(R.string.class_two);
            case 3:
                return MainApp.getInstance().getString(R.string.class_three);
            case 4:
                return MainApp.getInstance().getString(R.string.class_four);
            case 5:
                return MainApp.getInstance().getString(R.string.class_five);
            case 6:
                return MainApp.getInstance().getString(R.string.class_six);
            case 7:
                return MainApp.getInstance().getString(R.string.class_middle_one);
            case 8:
                return MainApp.getInstance().getString(R.string.class_middle_two);
            case 9:
                return MainApp.getInstance().getString(R.string.class_middle_three);
            case 10:
                return MainApp.getInstance().getString(R.string.class_hight_one);
            case 11:
                return MainApp.getInstance().getString(R.string.class_hight_two);
            case 12:
                return MainApp.getInstance().getString(R.string.class_hight_three);
        }
        return "Unknown";
    }

    public static String dealHtmlText(String html) {
        String text = html;
        text = text.replace("<p>&nbsp;</p>", "");
        text = text.replace("\\\"", "\"");

        return text;
    }

    public static String saveToFile(Context context, Bitmap bitmap, String fileName) {
        String savePath = context.getApplicationContext().getFilesDir().getAbsolutePath()
                + "/" + fileName + ".png";
        try {
            File file = new File(savePath);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return savePath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap readFromFile(Context context, String fileName) {
        String savePath = context.getApplicationContext().getFilesDir().getAbsolutePath()
                + "/" + fileName;

        try {
            FileInputStream fis = new FileInputStream(savePath);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isUrl(String text) {
        if (text == null)
            return false;

        if (text.contains("Resources/"))
            return true;

        try {
            URL url = new URL(text);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static void showFragment(FragmentManager manager, Fragment fragment, int resId, String tag) {
        FragmentTransaction beginTransaction = manager.beginTransaction();
        beginTransaction.replace(resId, fragment);
        beginTransaction.addToBackStack(tag);
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        beginTransaction.commit();
    }

    public static void clearFragments(FragmentManager manager) {
        try {
            List<Fragment> fragments = manager.getFragments();
            int len = fragments.size();
            for (int i = len - 1; i >= 0; i--) {
                Fragment fragment = fragments.get(i);
                manager.beginTransaction().remove(fragment).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isTeacher(Context context) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        boolean isTeacher = sharedPre.getBoolean("isTeacher", false);
        return isTeacher;
    }

    public static void changeTeacherOrStudent(Context context, boolean isTeacher) {
        SharedPreferences sharedPre = context.getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPre.edit();
        editor.putBoolean("isTeacher", isTeacher);
        editor.commit();
    }
}
