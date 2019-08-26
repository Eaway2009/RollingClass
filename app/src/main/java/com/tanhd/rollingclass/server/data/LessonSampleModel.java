package com.tanhd.rollingclass.server.data;

import java.util.List;

public class LessonSampleModel extends BaseJsonClass {
    public String lesson_sample_name;
    public String knowledge_id;
    public String lesson_sample_id;
    public int lesson_type;
    public int number;
    public int status;
    public List<String> ppt_set;
    public List<String> question_set;
    public List<String> doc_set;
    public List<String> image_set;
    public List<String> video_set;
}
