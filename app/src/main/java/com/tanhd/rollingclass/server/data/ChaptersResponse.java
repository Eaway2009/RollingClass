package com.tanhd.rollingclass.server.data;

import com.tanhd.library.mqtthttp.PushMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
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
            if (key.equals("Sections")||key.equals("sections")) {
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
        public String getKey(String key){
            if("chapter_id".equals(key)||"ChapterID".equals(key)) {
                return "ChapterID";
            }
            if("chapter_name".equals(key)||"ChapterName".equals(key)) {
                return "ChapterName";
            }
            if("sections".equals(key)||"Sections".equals(key)) {
                return "Sections";
            }
            return key;
        }
    }

    public static class Section extends BaseJsonClass{
        public boolean isChecked;
        public String SectionID;
        public String SectionName;
        public String TeachingMaterialID;
        public String getKey(String key){
            if("section_id".equals(key)||"SectionID".equals(key)) {
                return "SectionID";
            }
            if("section_name".equals(key)||"SectionName".equals(key)) {
                return "SectionName";
            }
            if("teaching_material_id".equals(key)||"TeachingMaterialID".equals(key)) {
                return "TeachingMaterialID";
            }
            return key;
        }
    }

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Chapters")||key.equals("chapters")) {
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

    public String getKey(String key){
        if("Chapters".equals(key)||"chapters".equals(key)) {
            return "Chapters";
        }
        if("study_section_code".equals(key)||"StudySectionCode".equals(key)) {
            return "StudySectionCode";
        }
        if("study_section_name".equals(key)||"StudySectionName".equals(key)) {
            return "StudySectionName";
        }
        if("grade_code".equals(key)||"GradeCode".equals(key)) {
            return "GradeCode";
        }
        if("grade_name".equals(key)||"GradeName".equals(key)) {
            return "GradeName";
        }
        if("subject_code".equals(key)||"SubjectCode".equals(key)) {
            return "SubjectCode";
        }
        if("subject_name".equals(key)||"SubjectName".equals(key)) {
            return "SubjectName";
        }
        if("teaching_material_code".equals(key)||"TeachingMaterialCode".equals(key)) {
            return "TeachingMaterialCode";
        }
        if("teaching_material_name".equals(key)||"TeachingMaterialName".equals(key)) {
            return "TeachingMaterialName";
        }
        return key;
    }
}
