package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeLessonSample extends BaseJsonClass{

    public String lesson_sample_id;
    public String lesson_sample_name;
    public int status;
    public int lesson_type;
    public String knowledge_id;
    public List<ResourceModel> ppt_set;
    public List<ResourceModel> video_set;
    public List<ResourceModel> doc_set;
    public List<ResourceModel> question_set;
    public List<ResourceModel> image_set;
    public String create_time;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        if (key.equals("ppt_set") || key.equals("video_set")|| key.equals("doc_set")|| key.equals("question_set")|| key.equals("image_set")) {
            try {
                JSONArray array = json.optJSONArray(key);
                ArrayList<ResourceModel> list = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    ResourceModel resourceModel = new ResourceModel();
                    resourceModel.parse(resourceModel, obj);
                    list.add(resourceModel);
                }
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
