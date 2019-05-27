package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ContextData extends BaseJsonClass {
    public String Analysis;
    public String Answer;
    public int OrderIndex;
    public int QuestionCategoryId;
    public String QuestionCategoryName;
    public int QuestionDisplayId;
    public String Stem;
    public List<OptionData> Options;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Options")) {
            JSONArray array = json.optJSONArray(key);
            if (array == null)
                return;

            ArrayList<OptionData> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                OptionData optionData = new OptionData();
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
