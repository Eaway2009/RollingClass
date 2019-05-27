package com.tanhd.rollingclass.server;

public interface RequestCallback {
    void onProgress(boolean b);
    void onResponse(String body);
    void onError(String code, String message);
}
