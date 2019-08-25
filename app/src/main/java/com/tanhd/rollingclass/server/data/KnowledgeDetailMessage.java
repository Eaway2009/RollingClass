package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeDetailMessage extends BaseJsonClass {

    /**
     * knowledge_id
     */
    public String knowledge_id;
    /**
     * 学校id
     */
    public String school_id;
    /**
     * 学校id
     */
    public String teacher_id;
    /**
     * 章的Id
     */
    public String chapter_id;
    /**
     * 章的名称
     */
    public String chapter_name;
    /**
     * 知识点
     */
    public String knowledge_point_name;

    /**
     * 节的id
     */
    public String section_id;
    /**
     * 节的名称
     */
    public String section_name;
    /**
     * 科目Id
     */
    public int subject_code;
    /**
     * 课后发布
     */
    public int class_after;
    /**
     * 课前发布
     */
    public int class_before;
    /**
     * 课中发布
     */
    public int class_process;
    /**
     *
     */
    public int status;
    /**
     * 科目名
     */
    public String subject_name;
    /**
     * 教案id
     */
    public String teaching_material_id;
    /**
     *
     */
    public String remark;
    /**
     * 记录
     */
    public List<Record> records;
    /**
     * 记录
     */
    public List<String> class_ids;

    public class Record extends BaseJsonClass{
        public String class_id;
        public String class_name;
        public String time_record;
    }

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        if (key.equals("records")) {
            try {
                JSONArray array = json.optJSONArray(key);
                ArrayList<Record> list = new ArrayList<>();
                for (int i=0; i<array.length(); i++) {
                    JSONObject obj = array.optJSONObject(i);
                    Record record = new Record();
                    record.parse(record, obj);
                    list.add(record);
                }
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
