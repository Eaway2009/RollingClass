package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GroupData extends BaseJsonClass {
    public String GroupName;
    public List<String> Students;
    public List<StudentData> StudentList;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Students")) {
            JSONArray array = json.optJSONArray(key);
            if (array == null)
                return;

            ArrayList<String> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                list.add(array.optString(i));
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
