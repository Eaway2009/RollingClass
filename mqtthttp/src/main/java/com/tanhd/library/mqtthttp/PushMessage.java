package com.tanhd.library.mqtthttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PushMessage {
    public static enum COMMAND {
        PING,
        PING_OK,
        QUERY_CLASS,//查询全班，通知上课
        ONLINE,//学生上线
        OFFLINE,//学生下线
        CLASS_BEGIN,//开始上课
        CLASS_END,//下课
        SCROLL_TO,//滚动翻页
        SCROLL_CUR,//跳转（第几页）
        MESSAGE,//指定提问

        QUESTIONING,//发起提问
        ANSWER_COMPLETED,//完成回答

        PING_TEST,
        PING_TEST_REPLY,

        OPEN_DOCUMENT,//打开学案
        SERVER_PING,
        SERVER_PING_STOP,

        COMMENT_START,//答卷开始
        COMMENT_END,//答卷结束
        SMART_PEN_DOT,//智能笔点击
        SMART_PEN_COLOR,//智能笔画

        QUERY_STATUS,
    }

    public String from;
    public List<String> to;
    public COMMAND command;
    public Map<String, String> parameters;

    public String toString() {
        JSONObject json = new JSONObject();
        try {
            json.put("from", from);
            if (to != null && to.size() > 0) {
                JSONArray array = new JSONArray();
                for (String t: to) {
                    array.put(t);
                }
                json.put("to", array);
            }
            json.put("command", command.toString());
            if (parameters != null && parameters.size() > 0) {
                JSONObject params = new JSONObject();
                for (String key: parameters.keySet()) {
                    params.put(key, parameters.get(key));
                }
                json.put("parameters", params);
            }

            return json.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PushMessage parse(String text) {
        try {
            return parse(new JSONObject(text));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static PushMessage parse(JSONObject json) {
        PushMessage message = new PushMessage();
        message.from = json.optString("from");
        message.command = COMMAND.valueOf(json.optString("command"));
        if (json.has("to")) {
            JSONArray array = json.optJSONArray("to");
            ArrayList<String> to = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                to.add(array.optString(i));
            }
            message.to = to;
        }
        if (json.has("parameters")) {
            JSONObject params = json.optJSONObject("parameters");
            message.parameters = new HashMap<>();
            Iterator<String> iter = params.keys();
            while (iter.hasNext()) {
                String key = iter.next();
                message.parameters.put(key, params.optString(key));
            }
        }

        return message;
    }
}
