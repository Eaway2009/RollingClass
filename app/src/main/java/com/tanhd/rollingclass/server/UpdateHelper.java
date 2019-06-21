package com.tanhd.rollingclass.server;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor;
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

    private static final String DOWN_PATH = "/sdcard/rollingclass/";
    private static final String VERSION_FILE_PATH = "version.json";
    private static final String VERSION_JSON_URL = "https://raw.githubusercontent.com/Eaway2009/GitTest/master/fanzhuan_version.json";
    private static UpdateHelper instance;
    private final Handler mHandler;

    private OkHttpClient okHttpClient;

    private boolean mDownloading;

    private DownloadTask mVersionDownloadTask;

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

    public void update(RequestCallback requestCallback) {
        getVersionTask(requestCallback).execute();
    }

    private DownloadTask getVersionTask(RequestCallback requestCallback) {
        if (mVersionDownloadTask == null) {
            mVersionDownloadTask = new DownloadTask(VERSION_JSON_URL, VERSION_FILE_PATH, requestCallback);
        }
        return mVersionDownloadTask;
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
                File file = new File(mFilePath);
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
                call(callback, "onResponse", mFilePath);
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

    public static VersionMessage getVersion(String filePath){
        if(!TextUtils.isEmpty(filePath)){
            VersionMessage versionMessage = new VersionMessage();
            versionMessage.parse(versionMessage, readTxtFile(filePath));
            return versionMessage;
        }
        return null;
    }
}
