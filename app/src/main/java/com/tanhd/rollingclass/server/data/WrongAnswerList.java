package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class WrongAnswerList extends BaseJsonClass {

    public List<AnswerModel> questions;
    public List<AnswerSet> error_set;
    public List<AnswerSet> correct_set;
    public List<AnswerSet> unanswer_set;
    public int error_cnt;
    public int correct_cnt;
    public int unanswer_cnt;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("error_set")||key.equals("correct_set")||key.equals("unanswer_set")) {
            JSONArray array = json.optJSONArray(key);
            if (array == null)
                return;

            ArrayList<AnswerSet> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                AnswerSet optionData = new AnswerSet();
                optionData.parse(optionData, array.optJSONObject(i));
                list.add(optionData);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (key.equals("questions")) {
            JSONArray array = json.optJSONArray(key);
            if (array == null)
                return;

            ArrayList<AnswerModel> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                AnswerModel optionData = new AnswerModel();
                optionData.parse(optionData, array.optJSONObject(i));
                list.add(optionData);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
