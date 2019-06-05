package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;

public class MicroCourseData extends BaseJsonClass {
    public String MicroCourseID;
    public String MicroCourseName;
    public String KnowledgeID;
    public String Remark;
    public String VideoUrl;
    public String TeacherID;
    public long CreateTime;
    public long UpdateTime;
    public int Duration;
    public List<String> ClassIds;
    public List<String> GradeIds;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("ClassIds") || key.equals("GradeIds")) {
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
