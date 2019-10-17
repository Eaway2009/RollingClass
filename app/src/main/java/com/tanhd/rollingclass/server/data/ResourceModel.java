package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.db.KeyConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ResourceModel extends ResourceBaseModel {
    public List<QuestionModel> mResourceList;
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
    public int pptIndex;

    public ArrayList<String> thumbs;

    public ResourceModel(){}

    public ResourceModel(List<QuestionModel> resourceList, String resource_name){
        resource_type = KeyConstants.ResourceType.QUESTION_TYPE;
        name = resource_name;
        mResourceList = resourceList;
    }

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("thumbs")) {
            JSONArray array = json.optJSONArray(key);
            ArrayList<String> list = new ArrayList<>();
            if(array!=null) {
                for (int i = 0; i < array.length(); i++) {
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
}
