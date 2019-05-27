package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TeacherData extends BaseJsonClass {
    public String Account;
    public String TeacherID;
    public String Mobile;
    public String Password;
    public String Remark;
    public int StudysectionCode;
    public int Sex;
    public int SubjectCode;
    public String Token;
    public long TokenExpiryTime;
    public String Username;
    public String SchoolID;
    public List<String> TeachingClass;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("TeachingClass")) {
            JSONArray array = json.optJSONArray(key);
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
