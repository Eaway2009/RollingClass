package com.tanhd.rollingclass.server.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClassData extends BaseJsonClass implements MultiLevelModel<StudentData>{
    public String ClassID;
    public String ClassName;
    public long CreateTime;
    public String GradeID;
    public int GradeCode;
    public String Remark;
    public String SchoolID;
    public String StudysectionID;
    public int StudysectionCode;
    public long UpdateTime;
    public List<GroupData> Groups;
    public List<String> teachers;

    @Override
    public List<StudentData> getChildren() {
        if(Groups!=null){
            List<StudentData> studentDataList = new ArrayList<>();
            for(GroupData groupData:Groups){
                if(groupData.StudentList!=null)
                studentDataList.addAll(groupData.StudentList);
            }
        }
        return null;
    }

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Groups")) {
            JSONArray array = json.optJSONArray(key);
            ArrayList<GroupData> list = new ArrayList<>();
            for (int i=0; i<array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                GroupData groupData = new GroupData();
                groupData.parse(groupData, obj);
                list.add(groupData);
            }
            try {
                field.set(object, list);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void setStudentState(String studentID, int state) {
        if (Groups == null)
            return;

        for (GroupData groupData: Groups) {
            if (groupData.StudentList == null)
                continue;

            for (StudentData studentData: groupData.StudentList) {
                if (studentData.StudentID.equals(studentID)) {
                    studentData.Status = state;
                    return;
                }
            }
        }
    }

    public void resetStudentState(int state) {
        if (Groups == null)
            return;

        for (GroupData groupData: Groups) {
            if (groupData.StudentList == null)
                continue;

            for (StudentData studentData: groupData.StudentList) {
                studentData.Status = state;
            }
        }
    }

    @Override
    public String getKey(String key) {
        if("class_id".equals(key)||"ClassID".equals(key)) {
            return "ClassID";
        }
        if("class_name".equals(key)||"ClassName".equals(key)) {
            return "ClassName";
        }
        if("grade_id".equals(key)||"GradeID".equals(key)) {
            return "GradeID";
        }
        if("groups".equals(key)||"Groups".equals(key)) {
            return "Groups";
        }
        if("remark".equals(key)||"Remark".equals(key)) {
            return "Remark";
        }
        if("school_id".equals(key)||"SchoolID".equals(key)) {
            return "SchoolID";
        }
        if("study_section_code".equals(key)||"StudysectionCode".equals(key)) {
            return "StudysectionCode";
        }
        if("studysection_id".equals(key)||"StudysectionID".equals(key)) {
            return "StudysectionID";
        }
        if("update_time".equals(key)||"UpdateTime".equals(key)) {
            return "UpdateTime";
        }
        return key;
    }
}
