package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ChapterData extends BaseJsonClass {
    public String ChapterName;
    public List<SectionData> Sections;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Sections")) {
            JSONArray array = json.optJSONArray(key);
            ArrayList<SectionData> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                SectionData sectionData = new SectionData();
                sectionData.parse(sectionData, array.optJSONObject(i));
                list.add(sectionData);
            }

            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
