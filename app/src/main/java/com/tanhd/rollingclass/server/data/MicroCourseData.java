package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarEntry;

public class MicroCourseData extends BaseJsonClass {
    public String ChapterName;
    public String GradeName;

    public MicroCourseInfo MicroCourseInfo;

    public static class MicroCourseInfo extends BaseJsonClass {
        public List<String> ClassIds;
        public List<String> GradeIds;
        public long CreateTime;
        public int Duration;
        public String KnowledgeID;
        public String MicroCourseID;
        public String MicroCourseName;
        public String Remark;
        public String TeacherID;
        public long UpdateTime;
        public String VideoUrl;

        @Override
        protected void onDealListField(Object object, Field field, JSONObject json, String key) {
            super.onDealListField(object, field, json, key);
            try {
                if (key.equals("ClassIds") || key.equals("GradeIds")) {
                    JSONArray array = json.optJSONArray(key);
                    ArrayList<String> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        list.add(array.optString(i));
                    }
                    field.set(object, list);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public String PointName;
    public String SectionName;


    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        try {
            if (key.equals("MicroCourseInfo")) {
                MicroCourseInfo sectionData = new MicroCourseInfo();
                sectionData.parse(sectionData, json.optJSONObject(key));
                field.set(object, sectionData);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
