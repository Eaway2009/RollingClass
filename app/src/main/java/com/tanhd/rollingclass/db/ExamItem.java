package com.tanhd.rollingclass.db;

import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.BaseJsonClass;
import com.tanhd.rollingclass.server.data.QuestionData;

import java.util.List;

public class ExamItem extends BaseJsonClass {
    public String examID;
    public String lessonSampleID;
    public String studentID;
    public String toName;
    public List<QuestionData> questions;
    public List<AnswerData> answers;
    public int flag;
    public long time;
}
