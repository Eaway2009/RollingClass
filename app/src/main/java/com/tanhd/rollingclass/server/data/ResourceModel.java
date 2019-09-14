package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ResourceModel extends BaseJsonClass {
    public long create_time;
    public long level;
    public String name;
    public String pdf_url;
    public String remark;
    public String resource_id;
    public String teaching_material_id;
    public String teacher_id;
    public int resource_type;
    public long size;
    public long update_time;
    public String url;
    public boolean isChecked;

    public ArrayList<String> thumbs;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("thumbs")) {
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
