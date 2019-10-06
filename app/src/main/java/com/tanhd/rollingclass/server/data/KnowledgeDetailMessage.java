package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KnowledgeDetailMessage extends BaseJsonClass {

    /**
     * knowledge_id
     */
    public String knowledge_id;
    /**
     * 知识点
     */
    public String knowledge_point_name;
    /**
     * 科目Id
     */
    public int subject_code;
    /**
     * 科目名
     */
    public String subject_name;
    /**
     * 记录
     */
    public List<String> class_ids;
    /**
     * 教案id
     */
    public String teaching_material_id;
    /**
     * 章的名称
     */
    public String chapter_name;
    /**
     * 章的Id
     */
    public String chapter_id;
    /**
     * 节的名称
     */
    public String section_name;

    /**
     * 节的id
     */
    public String section_id;
    /**
     * 学校id
     */
    public String school_id;
    /**
     * 学校id
     */
    public String teacher_id;
    /**
     *
     */
    public int status;
    /**
     * 课前发布
     */
    public int class_before;
    /**
     * 课中发布
     */
    public int class_process;
    /**
     * 课后发布
     */
    public int class_after;
    /**
     * 记录
     */
    public List<Record> records;
    /**
     *
     */
    public long create_time;

    public class Record extends BaseJsonClass{
        public String class_id;
        public String class_name;
        public Date time_record;
    }

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        if (key.equals("records")) {
            try {
                JSONArray array = json.optJSONArray(key);
                if(array!=null) {
                    ArrayList<Record> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.optJSONObject(i);
                        Record record = new Record();
                        record.parse(record, obj);
                        list.add(record);
                    }
                    field.set(object, list);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return knowledge_point_name;
    }
}
