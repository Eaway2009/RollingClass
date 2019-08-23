package com.tanhd.rollingclass.server;

import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;


import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor;
import com.tanhd.rollingclass.server.data.BaseJsonClass;
import com.tanhd.rollingclass.server.data.QuestionData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ServerRequest {
    private final static String TAG = "HTTP";

    public static enum METHOD {
        GET,
        POST
    }

    private static OkHttpClient okHttpClient;
    private Handler mHandler;

    protected ServerRequest() {
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


    protected String sendRequest(String url, METHOD method, Map<String, String> params) {
        Request request;
        Set<String> keys = null;
        if (params != null)
            keys = params.keySet();

        if (method == METHOD.POST) {
            FormBody.Builder builder = new FormBody.Builder();

            if (keys != null) {
                for (String k : keys) {
                    String value = params.get(k);
                    if (value == null)
                        continue;
                    builder.add(k, value);
                }
            }

            RequestBody body = builder.build();
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        } else {
            StringBuilder builder = new StringBuilder();
            if (keys != null) {
                int pos = 0;
                for (String k : keys) {
                    if (pos > 0) {
                        builder.append("&");
                    }
                    try {
                        builder.append(String.format("%s=%s", k, URLEncoder.encode(params.get(k), "utf-8")));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    pos++;
                }
            }

            String requestUrl = String.format("%s?%s", url, builder.toString());
            request = new Request.Builder()
                    .url(requestUrl)
                    .build();
        }

        try {
            Response response = okHttpClient.newCall(request).execute();
            String text = response.body().string();
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected String sendRequest(String url, METHOD method, String json) {
        Request request;
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            String text = response.body().string();
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected class RequestTask extends AsyncTask<Void, Void, Void> {
        private final RequestCallback callback;
        private final Map<String, String> params;
        private final String jsonBody;
        private final String path;
        private final METHOD method;

        public RequestTask(String p, METHOD m, Map<String, String> map, String json, RequestCallback cb) {
            path = p;
            params = map;
            callback = cb;
            method = m;
            jsonBody = json;
        }

        @Override
        protected void onPreExecute() {
            callback.onProgress(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String response;
            if (params != null)
                response = sendRequest(path, method, params);
            else
                response = sendRequest(path, method, jsonBody);

            if (response == null) {
                call(callback, "onError", "-2", "timeout");
                return null;
            }

            try {
                JSONObject json = new JSONObject(response);
                String errorCode = json.optString("errorCode");
                if (!TextUtils.isEmpty(errorCode) && !errorCode.equals("0")) {
                    call(callback, "onError", errorCode, json.optString("errorMessage"));
                } else {
                    call(callback, "onResponse", response);
                }
            } catch (JSONException e) {
                call(callback, "onError", "-1", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onProgress(false);
        }
    }

    protected class UploadFileTask extends AsyncTask<Void, Void, Void> {
        private final RequestCallback callback;
        private final Map<String, String> params;
        private final String fileName;
        private final String path;

        public UploadFileTask(String p, Map<String, String> map, String file, RequestCallback cb) {
            path = p;
            params = map;
            callback = cb;
            fileName = file;
        }

        @Override
        protected void onPreExecute() {
            callback.onProgress(true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String response = uploadFile(path, params, fileName);

            if (response == null) {
                call(callback, "onError", "-2", "failed!");
                return null;
            }

            call(callback, "onResponse", response);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onProgress(false);
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
                File path = new File("/sdcard/rollingclass/");
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

    protected String uploadFile(String url, Map<String, String> params, String filePath) {
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        //遍历map中所有参数到builder
        if (params != null) {
            for (String key : params.keySet()) {
                multipartBodyBuilder.addFormDataPart(key, params.get(key));
            }
        }

        File f = new File(filePath);
        String fileName = f.getName();
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
        multipartBodyBuilder.addFormDataPart("file", fileName, RequestBody.create(MEDIA_TYPE_PNG, f));
        //构建请求体
        RequestBody requestBody = multipartBodyBuilder.build();
        Request request;
        request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            String text = response.body().string();
            Log.i(TAG, text);

            if (response == null) {
                return null;
            }

            try {
                JSONObject json = new JSONObject(text);
                String errorCode = json.optString("errorCode");
                if (!TextUtils.isEmpty(errorCode) && !errorCode.equals("0")) {
                    return null;
                } else {
                    String result = json.optString("result");
                    if (TextUtils.isEmpty(result))
                        return null;
                    else
                        return result;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List jsonToList(String className, String response) {
        try {
            JSONObject json = new JSONObject(response);
            String errorCode = json.optString("errorCode");
            if (TextUtils.isEmpty(errorCode) || !errorCode.equals("0"))
                return null;

            JSONArray array = json.optJSONArray("result");
            if (array == null)
                return null;

            ArrayList list = new ArrayList();
            for (int i = 0; i < array.length(); i++) {
                if (className.equals(Integer.class.getName())) {
                    list.add(array.getInt(i));
                } else {
                    JSONObject obj = array.optJSONObject(i);
                    try {
                        Object o = Class.forName(className).newInstance();
                        if (o instanceof BaseJsonClass) {
                            BaseJsonClass bjc = (BaseJsonClass) o;
                            bjc.parse(bjc, obj);
                            list.add(o);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            return list;
        } catch (JSONException e) {

        }

        return null;
    }

    public Object jsonToModel(String className, String response) {
        try {
            JSONObject json = new JSONObject(response);
            String errorCode = json.optString("errorCode");
            if (TextUtils.isEmpty(errorCode) || !errorCode.equals("0"))
                return null;

            JSONObject obj = json.optJSONObject("result");
            if (obj == null)
                return null;

            Object o = Class.forName(className).newInstance();
            if (o instanceof BaseJsonClass) {
                BaseJsonClass bjc = (BaseJsonClass) o;
                bjc.parse(bjc, obj);
                return bjc;
            }
        } catch (JSONException e) {

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
