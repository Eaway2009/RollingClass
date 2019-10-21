package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
     * 上课记录
     */
    public List<Record> records;
    /**
     * 学习记录
     */
    public List<KnowledgeRecord> knowledge_records;
    /**
     *
     */
    public long create_time;


    public class KnowledgeRecord{
        /**
         * knowledge_learn_record_id : 5dabe76937a49e3a6dc3291c
         * student_id : 5c9dcfdd37a49e47876bd0bd
         * knowledge_id : 5da67e2437a49e227984cf72
         * rate : 8
         * date_time : 2019-10-20
         * learn_time : 12:49:45
         * create_time : 1571546985893
         */
        private String knowledge_learn_record_id;
        private String student_id;
        private String knowledge_id;
        private int rate; //进度
        private String date_time;  //日期
        private String learn_time; //学习时间
        private long create_time;

        public String getKnowledge_learn_record_id() {
            return knowledge_learn_record_id;
        }

        public void setKnowledge_learn_record_id(String knowledge_learn_record_id) {
            this.knowledge_learn_record_id = knowledge_learn_record_id;
        }

        public String getStudent_id() {
            return student_id;
        }

        public void setStudent_id(String student_id) {
            this.student_id = student_id;
        }

        public String getKnowledge_id() {
            return knowledge_id;
        }

        public void setKnowledge_id(String knowledge_id) {
            this.knowledge_id = knowledge_id;
        }

        public int getRate() {
            return rate;
        }

        public void setRate(int rate) {
            this.rate = rate;
        }

        public String getDate_time() {
            return date_time;
        }

        public void setDate_time(String date_time) {
            this.date_time = date_time;
        }

        public String getLearn_time() {
            return learn_time;
        }

        public void setLearn_time(String learn_time) {
            this.learn_time = learn_time;
        }

        public long getCreate_time() {
            return create_time;
        }

        public void setCreate_time(long create_time) {
            this.create_time = create_time;
        }
    }

    public class Record extends BaseJsonClass{
        public String class_id;
        public String class_name;
        public Date time_record;
        private String year;
        private String month;

        public String getYear() {
            if (time_record == null) return  "";
            year = StringUtils.getFormatYear(time_record);
            return year;
        }

        public String getMonth() {
            if (time_record == null) return  "";
            month = StringUtils.getFormatMonth(time_record);
            return month;
        }
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
                        try {
                            String timeStr = obj.getString("time_record");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意格式化的表达式
                            String[] timeStrArray = timeStr.split("T");
                            record.time_record = format.parse(timeStrArray[0] + " " + timeStrArray[1].substring(0,8));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        list.add(record);
                    }
                    field.set(object, list);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Date parseServerTime(String serverTime, String format) {
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINESE);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date date = null;
        try {
            date = sdf.parse(serverTime);
        } catch (Exception e) {
        }
        return date;
    }

    @Override
    public String toString() {
        return knowledge_point_name;
    }
}
