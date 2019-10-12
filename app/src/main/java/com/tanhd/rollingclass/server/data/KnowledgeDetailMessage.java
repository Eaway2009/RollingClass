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
