package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ChaptersResponse extends BaseJsonClass {

    public List<Chapter> Chapters;
    public int GradeCode;
    public String GradeName;
    public int StudySectionCode;
    public String StudySectionName;
    public int SubjectCode;
    public String SubjectName;
    public int TeachingMaterialCode;
    public String TeachingMaterialID;
    public String TeachingMaterialName;

    public static class Chapter extends BaseJsonClass implements MultiLevelModel<Section>{
        public String ChapterID;
        public String ChapterName;
        public List<Section> Sections;

        @Override
        public List<Section> getChildren() {
            return Sections;
        }

        @Override
        protected void onDealListField(Object object, Field field, JSONObject json, String key) {
            super.onDealListField(object, field, json, key);
            if (key.equals("Sections")) {
                JSONArray array = json.optJSONArray(key);
                ArrayList<Section> list = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    Section section = new Section();
                    section.parse(section, obj);
                    list.add(section);
                }
                try {
                    field.set(object, list);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Section extends BaseJsonClass{
        public boolean isChecked;
        public String SectionID;
        public String SectionName;
        public String TeachingMaterialID;
    }

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Chapters")) {
            JSONArray array = json.optJSONArray(key);
            ArrayList<Chapter> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                Chapter chapter = new Chapter();
                chapter.parse(chapter, obj);
                list.add(chapter);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
