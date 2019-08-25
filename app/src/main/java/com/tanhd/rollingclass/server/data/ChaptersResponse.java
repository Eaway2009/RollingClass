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

    public static class TeachingMaterial {

        public boolean isFirstItem;
        public int GradeCode;
        public String GradeName;
        public int StudySectionCode;
        public String StudySectionName;
        public int SubjectCode;
        public String SubjectName;
        public int TeachingMaterialCode;
        public String TeachingMaterialID;
        public String TeachingMaterialName;

        public TeachingMaterial(boolean isFirstItem,
                 int GradeCode,
                 String GradeName,
                 int StudySectionCode,
                 String StudySectionName,
                 int SubjectCode,
                 String SubjectName,
                 int TeachingMaterialCode,
                 String TeachingMaterialID,
                 String TeachingMaterialName){
             this.isFirstItem =isFirstItem ;
             this.GradeCode = GradeCode;
             this.GradeName = GradeName;
             this.StudySectionCode = StudySectionCode;
             this.StudySectionName = StudySectionName;
             this.SubjectCode = SubjectCode;
             this.SubjectName = SubjectName;
             this.TeachingMaterialCode = TeachingMaterialCode;
             this.TeachingMaterialID = TeachingMaterialID;
             this.TeachingMaterialName = TeachingMaterialName;
        }
    }

    public static class Chapter extends BaseJsonClass implements MultiLevelModel<Section>{
        public String ChapterID;
        public String ChapterName;
        public List<Section> Sections;

        public TeachingMaterial teachingMaterial;

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
