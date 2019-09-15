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
    public ArrayList<String> StudentList;
    public String TeacherID;
    public String class_id;
    public String knowledge_id;
    public long CreateTime;
    public long UpdateTime;
    public String Remark;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        if (key.equals("QuestionList") || key.equals("StudentList")) {
            try {
                JSONArray array = new JSONArray(json.optString(key));
                ArrayList<String> list = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    list.add(array.optString(i));
                }
                field.set(object, list);
            } catch (JSONException e) {
                e.printStackTrace();
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

            return (int)(b.CreateTime - a.CreateTime);
        }
    }

    public static void sort(List<QuestionSetData> list) {
        Collections.sort(list, new SortComparator());
    }
}
