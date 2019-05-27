package com.tanhd.rollingclass.server.data;

import org.json.JSONObject;

public class UserData {
    public static enum ROLE{
        TEACHER,
        STUDENT,
    };

    private BaseJsonClass mUserData;
    private ROLE mRole;

    public boolean isTeacher() {
        return mRole == ROLE.TEACHER;
    }

    private TeacherData getTeacherData() {
        if (isTeacher())
            return (TeacherData) mUserData;

        return null;
    }

    private StudentData getStudentData() {
        if (isTeacher())
            return null;

        return (StudentData) mUserData;
    }

    public void setData(ROLE role, JSONObject json) {
        BaseJsonClass data;
        if (role == ROLE.TEACHER) {
            data = new TeacherData();
        } else {
            data = new StudentData();
        }
        data.parse(data, json);
        setData(role, data);
    }

    public void setData(ROLE role, BaseJsonClass data) {
        mRole = role;
        mUserData = data;
    }

    public String getOwnerID() {
        if (isTeacher()) {
            return getTeacherData().TeacherID;
        } else {
            return getStudentData().StudentID;
        }
    }

    public String getOwnerName() {
        if (isTeacher()) {
            return getTeacherData().Username;
        } else {
            return getStudentData().Username;
        }
    }

    public BaseJsonClass getUserData() {
        if (isTeacher())
            return getTeacherData();
        else
            return getStudentData();
    }

}
