package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeachingMaterialData extends BaseJsonClass {
    public String TeachingMaterialID;
    public int TeachingMaterialCode;
    public String TeachingMaterialName;
    public int Version;
    public String SubjectName;
    public int SubjectCode;
    public String StudySectionName;
    public int StudySectionCode;
    public String Remark;
    public String GradeName;
    public int GradeCode;
    public int UseType;
    public List<ChapterData> Chapters;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Chapters")) {
            JSONArray array = json.optJSONArray(key);
            ArrayList<ChapterData> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                ChapterData chapterData = new ChapterData();
                chapterData.parse(chapterData, array.optJSONObject(i));
                list.add(chapterData);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
