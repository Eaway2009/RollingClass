package com.tanhd.rollingclass.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class FakeData {
    private final Context mContext;
    private HashMap<String, String> mDataMap = new HashMap<>();

    public FakeData(Context context) {
        mContext = context;
        load();
    }

    private String generateKey(String url, ServerRequest.METHOD method, Map<String, String> params) {
        String key = url + method.toString();
        if (params != null) {
            for (String k: params.keySet()) {
                key = key + k + params.get(k);
            }
        }
        key = AppUtils.md5(key);

        return key;
    }

    public void write(String url, ServerRequest.METHOD method, Map<String, String> params, String response) {
        String key = generateKey(url, method, params);
        mDataMap.put(key, response);
    }

    public String read(String url, ServerRequest.METHOD method, Map<String, String> params) {
        String key = generateKey(url, method, params);
        return mDataMap.get(key);
    }

    public void save() {
        JSONArray array = new JSONArray();
        for (String key: mDataMap.keySet()) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("key", key);
                obj.put("data", mDataMap.get(key));
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String text = array.toString();
        String savePath = mContext.getApplicationContext().getFilesDir().getAbsolutePath()
                + "/fakeData.json";
        try {
            File file = new File(savePath);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(text.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        String savePath = mContext.getApplicationContext().getFilesDir().getAbsolutePath()
                + "/fakeData.json";
        String content = "";
        try {
            File file = new File(savePath);
            if (!file.exists()) {
                return;
            }

            InputStream instream = new FileInputStream(file);
            if (instream != null) {
                InputStreamReader inputreader = new InputStreamReader(instream, "UTF-8");
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = "";
                while ((line = buffreader.readLine()) != null) {
                    content += line + "\n";
                }
                instream.close();
            }
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(content))
            return;

        mDataMap.clear();
        try {
            JSONArray array = new JSONArray(content);
            for (int i=0; i<array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj == null)
                    continue;

                mDataMap.put(obj.optString("key"), obj.optString("data"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
