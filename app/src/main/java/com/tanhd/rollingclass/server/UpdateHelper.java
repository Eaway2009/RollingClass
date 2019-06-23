package com.tanhd.rollingclass.server;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor;
import com.tanhd.rollingclass.BuildConfig;
import com.tanhd.rollingclass.server.data.VersionMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.concurrent.TimeUnit.SECONDS;

public class UpdateHelper {

    public static final String TAG = "UpdateHelper";

    private static final String DOWN_PATH = "/sdcard/rollingclass/";
    private static final String VERSION_FILE_PATH = "version.json";
    private static final String APK_FILE_PATH = "newest_flat.apk";
    private static final String VERSION_JSON_URL = "https://raw.githubusercontent.com/Eaway2009/GitTest/master/fanzhuan_version.json";
    private static UpdateHelper instance;
    private final Handler mHandler;

    private OkHttpClient okHttpClient;

    private boolean mDownloading;
    private boolean mChecking;

    private AlertDialog mDownDialog;

    public static final UpdateHelper getInstance() {
        if (instance == null) {
            instance = new UpdateHelper();
        }
        return instance;
    }

    private UpdateHelper() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new LogInterceptor())
                    .callTimeout(30, SECONDS)
                    .readTimeout(30, SECONDS)
                    .writeTimeout(30, SECONDS)
                    .connectTimeout(30, SECONDS)
                    .build();
        }


        mHandler = new Handler();
    }

    public void update(Context context, boolean autoCheck) {
        if (!mChecking) {
            new DownloadTask(VERSION_JSON_URL, VERSION_FILE_PATH, new VersionCheckRequestCallback(context, autoCheck)).execute();
            mChecking = true;
        }
    }

    public void downloadApk(Context context, String downUrl) {
        if(!mDownloading){
            new DownloadTask(downUrl, APK_FILE_PATH, new UpdateRequestCallback(context)).execute();
            mDownloading = true;
        }
    }

    protected class DownloadTask extends AsyncTask<Void, Void, Void> {
        private final RequestCallback callback;
        private final String mUrl;
        private final String mFilePath;

        public DownloadTask(String url, String filePath, RequestCallback cb) {
            mUrl = url;
            mFilePath = filePath;
            callback = cb;
        }

        @Override
        protected void onPreExecute() {
            callback.onProgress(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onProgress(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Request request = new Request.Builder()
                    .url(mUrl)
                    .build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                File path = new File(DOWN_PATH);
                if (!path.exists())
                    path.mkdir();

                InputStream is = response.body().byteStream();
                File file = new File(DOWN_PATH+mFilePath);
                if(file.exists()) {
                    file.delete();
                }
                long total = response.body().contentLength();
                FileOutputStream fos = new FileOutputStream(file);
                long sum = 0;
                int len = 0;
                byte[] buf = new byte[2048];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                }
                fos.flush();
                call(callback, "onResponse", DOWN_PATH+mFilePath);
            } catch (IOException e) {
                call(callback, "onError", "-1", e.getMessage());
            }

            return null;
        }
    }

    protected void call(final Object var1, String methodName, final Object... objects) {
        Method[] methods = var1.getClass().getMethods();
        for (final Method method : methods) {
            if (method.getName().equals(methodName)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(var1, objects);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
            }
        }
    }

    public static String readTxtFile(String strFilePath) {
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            Log.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            } catch (java.io.FileNotFoundException e) {
                Log.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }

    public class VersionCheckRequestCallback implements RequestCallback{

        private Context mContext;

        private boolean mAutoCheck;

        public VersionCheckRequestCallback(Context context, boolean autoCheck) {
            mContext = context;
            mAutoCheck = autoCheck;
        }

        @Override
        public void onProgress(boolean b) {
        }

        @Override
        public void onResponse(String body) {
            mChecking = false;
            VersionMessage versionMessage = UpdateHelper.getVersion(body);
            if (versionMessage == null) {
                if(!mAutoCheck) {
                    Toast.makeText(mContext, "检查新版本出错，请稍后重试", Toast.LENGTH_LONG).show();
                }
                return;
            }
            if(BuildConfig.VERSION_CODE > Integer.valueOf(versionMessage.versionCode)) {
                Toast.makeText(mContext, "已安装最新版本", Toast.LENGTH_LONG).show();
            } else {
                if(!mAutoCheck) {
                    warningUpdate(mContext, versionMessage.apkUrl);
                }
            }
        }

        @Override
        public void onError(String code, String message) {
            mChecking = false;
            Toast.makeText(mContext, "检查新版本出错，请稍后重试", Toast.LENGTH_LONG).show();
        }
    }

    public void warningUpdate(final Context context, final String url){

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("版本更新")
                .setMessage("检查到有新版本，是否下载更新")
                .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadApk(context, url);
                    }
                })
                .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDownDialog.dismiss();
                    }
                })
                .setCancelable(false)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mDownDialog = null;
                    }
                });
        mDownDialog = builder.create();
        mDownDialog.show();
    }

    public class UpdateRequestCallback implements RequestCallback{

        private Context mContext;

        public UpdateRequestCallback(Context context) {
            mContext = context;
        }

        @Override
        public void onProgress(boolean b) {

        }

        @Override
        public void onResponse(String filePath) {
            mDownloading = false;
            if(!TextUtils.isEmpty(filePath)&&new File(filePath).exists()) {
                install(mContext, filePath);
            }
        }

        @Override
        public void onError(String code, String message) {
            mDownloading = false;
            Toast.makeText(mContext, "下载新版本出错，请稍后重试", Toast.LENGTH_LONG).show();
        }
    }

    private void install(Context mContext, String filePath) {
        Log.i(TAG, "开始执行安装: " + filePath);
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.w(TAG, "版本大于 N ，开始使用 fileProvider 进行安装");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    mContext
                    , "com.tanhd.rollingclass.fileprovider"
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            Log.w(TAG, "正常进行安装");
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }

    public static VersionMessage getVersion(String filePath){
        if(!TextUtils.isEmpty(filePath)){
            VersionMessage versionMessage = new VersionMessage();
            if (new File(filePath).exists()) {
                versionMessage.parse(versionMessage, readTxtFile(filePath));
                return versionMessage;
            }
        }
        return null;
    }
}
