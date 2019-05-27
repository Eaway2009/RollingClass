package com.tanhd.library.mqtthttp;

import android.util.Log;

import com.parkingwang.okhttp3.LogInterceptor.LogInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import fi.iki.elonen.NanoHTTPD;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    private class Terminal {
        String terminalID;
        String ip;
        int httpPort;
    }

    private class DATA{
        String to;
        String data;
    }

    private HashMap<String, Terminal> mTerminalMap = new HashMap<>();
    private ArrayList<DATA> mQueue = new ArrayList<>();
    private OkHttpClient okHttpClient;

    protected HttpServer(int port) {
        super(port);
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor()).build();
    }

    @Override
    public void start() throws IOException {
        super.start();
        new SendThread().start();
    }

    @Override
    public void stop() {
        super.stop();
        mIsRuning = false;
    }

    public void addTerminal(String terminalID, String ip, int httpPort) {
        Terminal terminal = new Terminal();
        terminal.terminalID = terminalID;
        terminal.ip = ip;
        terminal.httpPort = httpPort;
        mTerminalMap.put(terminalID, terminal);
    }

    public boolean hasTerminal(String terminalID) {
        return mTerminalMap.containsKey(terminalID);
    }

    @Override
    public Response serve(IHTTPSession session) {
        HashMap<String, String> bodys = new HashMap<>();
        try {
            session.parseBody(bodys);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        String path = session.getUri();
        JSONObject reply = new JSONObject();

        if (!path.equals("/mqtt")) {
            try {
                reply.put("ErrCode", 1);
                reply.put("ErrMessage", "无效的路径!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            String data = bodys.get("postData");
            httpMessageArrived(data);

            try {
                reply.put("ErrCode", 0);
                reply.put("ErrMessage", "成功!");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newFixedLengthResponse(reply.toString());
    }

    private String sendRequest(String url, String data) {
        Request request;
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), data);
        request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Log.i(TAG, "Web Request:" + url);
        try {
            okhttp3.Response response = okHttpClient.newCall(request).execute();
            String text = new String(response.body().bytes(), "UTF-8");
            Log.i(TAG, "Web Response:" + text);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean post(String to, String message) {
        if (!mTerminalMap.containsKey(to))
            return false;

        DATA data = new DATA();
        data.to = to;
        data.data = message;

        synchronized (mQueue) {
            mQueue.add(data);
        }

        return true;
    }

    private boolean mIsRuning;
    private class SendThread extends Thread {
        @Override
        public void run() {
            mIsRuning = true;
            while (mIsRuning) {
                synchronized (mQueue) {
                    while (mQueue.size() > 0) {
                        DATA data = mQueue.remove(0);
                        Terminal terminal = mTerminalMap.get(data.to);
                        if (terminal == null) {
                            httpSendFailed(data.to, data.data);
                            continue;
                        }

                        String url = "http://" + terminal.ip + ":" + terminal.httpPort + "/mqtt";
                        if (sendRequest(url, data.data) == null) {
                            httpSendFailed(data.to, data.data);
                            mTerminalMap.remove(data.to);
                        }
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected void httpMessageArrived(String data) {

    }

    protected void httpSendFailed(String to, String data) {

    }
}
