package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class QuestionStatistics extends BaseJsonClass{
    public List<QuestionInfo> question_info;
    public String knowledge_id;
    public String class_id;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("question_info")) {
            Logger.i("ysl","question_info解析===");
        }
    }
}
