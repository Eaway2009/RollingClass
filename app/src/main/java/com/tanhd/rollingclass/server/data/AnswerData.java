package com.tanhd.rollingclass.server.data;

import java.util.List;

public class AnswerData extends BaseJsonClass {
    public boolean answer_right;
    public String AnswerID;
    public String AnswerName;
    public int AnswerType;
    public String AnswerUserID;
    public String AnswerUserName; //作答学生名字
    public long CreateTime;
    public String LessonSampleID;
    public String KnowledgeID;
    public String KnowledgeName;
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
    public String GoodAnswer; //正确答案
    public String AnswerText; //学生作答答案
    public String Analysis;
    public String AnswerUrl;
    public int ErrCode;
    public int Modify;  //批改字段 1:已批改   2:未批改
    public String QuestionSetID;
    public String knowledge_id;
    public String knowledge_name;
    public QuestionCoordinate QuestionCoordinate;

    public static class QuestionCoordinate{
        public List<Region> region;


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
