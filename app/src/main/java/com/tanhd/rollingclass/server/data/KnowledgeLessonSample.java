package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeLessonSample extends BaseJsonClass implements MultiLevelModel<ResourceModel> {

    public String lesson_sample_id;
    public String lesson_sample_name;
    public int status;
    public int lesson_type;
    public int number;
    public String knowledge_id;
    public List<ResourceModel> ppt_set;
    public List<ResourceModel> video_set;
    public List<ResourceModel> doc_set;
    public List<QuestionModel> question_set; //习题列表
    public List<ResourceModel> image_set;
    public String create_time;
    public String update_time;
    private List<ResourceModel> resourceModelList = new ArrayList<>();
    public boolean isSelect = true;
    public boolean isSubmitAnswer; //是否提交过答案


    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        if (key.equals("ppt_set") || key.equals("video_set") || key.equals("doc_set") || key.equals("image_set")) {
            try {
                JSONArray array = json.optJSONArray(key);
                ArrayList<ResourceModel> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    ResourceModel resourceModel = new ResourceModel();
                    resourceModel.parse(resourceModel, obj);
                    list.add(resourceModel);
                }
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return;
        }
        if(key.equals("question_set")){
            try {
                JSONArray array = json.optJSONArray(key);
                ArrayList<QuestionModel> list = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    QuestionModel questionModel = new QuestionModel();
                    questionModel.parse(questionModel, obj);
                    list.add(questionModel);
                }
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<ResourceModel> getChildren() {
        resourceModelList.clear();
        if (ppt_set != null) {
            resourceModelList.addAll(ppt_set);
        }
        if (doc_set != null) {
            resourceModelList.addAll(doc_set);
        }
        if (video_set != null) {
            resourceModelList.addAll(video_set);
        }
        if (image_set != null) {
            resourceModelList.addAll(image_set);
        }
        if(question_set!=null&&question_set.size()>0){
            resourceModelList.add(new ResourceModel(question_set, "习题"));
        }
        return resourceModelList;
    }
}
