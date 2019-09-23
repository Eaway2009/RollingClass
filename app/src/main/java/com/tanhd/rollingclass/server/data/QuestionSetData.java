package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QuestionSetData extends BaseJsonClass {
    public String QuestionSetID;
    public String LessonSampleID;
    public String SetName;
    public ArrayList<String> QuestionList;
    public List<Group> groups;
    public String TeacherID;
    public String class_id;
    public String knowledge_id;
    public long CreateTime;
    public long UpdateTime;
    public String Remark;

    public static class Group extends BaseJsonClass {
        String group_name;
        public List<String> students;

        @Override
        protected void onDealListField(Object object, Field field, JSONObject json, String key) {
            super.onDealListField(object, field, json, key);
            if (key.equals("students")) {
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

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        if (key.equals("QuestionList") || key.equals("StudentList") || key.equals("student_list")) {
            try {
                JSONArray array = new JSONArray(json.optString(key));
                ArrayList<String> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    list.add(array.optString(i));
                }
                field.set(object, list);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (key.equals("groups")) {
            JSONArray array = json.optJSONArray(key);
            ArrayList<Group> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                Group groupData = new Group();
                groupData.parse(groupData, obj);
                list.add(groupData);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            QuestionSetData a = (QuestionSetData) lhs;
            QuestionSetData b = (QuestionSetData) rhs;

            return (int) (b.CreateTime - a.CreateTime);
        }
    }

    public static void sort(List<QuestionSetData> list) {
        Collections.sort(list, new SortComparator());
    }
}
