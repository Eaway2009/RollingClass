package com.tanhd.rollingclass.server.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnswerData extends BaseJsonClass {
    public String AnswerID;
    public String AnswerName;
    public int AnswerType;
    public String AnswerUserID;
    public String AnswerUserName;
    public long CreateTime;
    public String LessonSampleID;
    public String LessonSampleName;
    public String QuestionID;
    public String QuestionName;
    public int QuestionType;
    public String Remark;
    public int Score;
    public String TeacherID;
    public String TeacherName;
    public long UpdateTime;
    public String ClassID;
    public int QuestionCategoryId;
    public String QuestionCategoryName;
    public String GoodAnswer;
    public String Analysis;
    public String AnswerText;
    public String AnswerUrl;
    public int ErrCode;
    public int Modify;  //批改字段 1:已批改   2:未批改
    public String QuestionSetID;
    public String knowledge_id;
    public String knowledge_name;
    public QuestionCoordinate QuestionCoordinate;

    public static class QuestionCoordinate{
        public List region;


    }

    public static class Region{
        public long point_x;
        public long point_y;
    }

    public static AnswerData newAnswer(UserData userData, QuestionData question) {
        AnswerData answer = new AnswerData();

        if (userData.isTeacher()) {
            TeacherData teacherData = (TeacherData) userData.getUserData();
            answer.AnswerUserID = teacherData.TeacherID;
            answer.AnswerUserName = teacherData.Username;
        } else {
            StudentData studentData = (StudentData) userData.getUserData();
            answer.AnswerUserID = studentData.StudentID;
            answer.AnswerUserName = studentData.Username;
        }
        answer.AnswerName = answer.AnswerUserName + "'s answer";
        answer.QuestionID = question.QuestionID;
        answer.QuestionName = question.QuestionName;
        answer.Remark = question.Remark;
        answer.TeacherID = question.TeacherID;
        return answer;
    }

    public void updateScore(int score) {
        Score = score;
        UpdateTime = System.currentTimeMillis();
    }
}
