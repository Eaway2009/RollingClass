package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class QuestionInfo extends BaseJsonClass {

    public AnswerModel question;
    public List<AnswerData> error_set;
    public List<AnswerData> correct_set;
    public List<AnswerData> unanswer_set;
    public int error_cnt; //错误人数
    public int correct_cnt; //正确人数
    public int unanswer_cnt; //未作答人数

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("error_set")||key.equals("correct_set")||key.equals("unanswer_set")) {
            JSONArray array = json.optJSONArray(key);
            if (array == null)
                return;

            ArrayList<AnswerData> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                AnswerData optionData = new AnswerData();
                optionData.parse(optionData, array.optJSONObject(i));
                list.add(optionData);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (key.equals("question")) {
            AnswerModel optionData = new AnswerModel();
            optionData.parse(optionData, json.optJSONObject(key));
            try {
                field.set(object, optionData);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}