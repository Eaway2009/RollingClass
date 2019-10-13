package com.tanhd.rollingclass.db;

public class KeyConstants {
    public class KnowledgeStatus {
        public static final int FRE_CLASS = 1; //课前
        public static final int AT_CLASS = 2; //课中
        public static final int AFTER_CLASS = 3; //课后
    }
    public class ClassPageType {
        public static final int TEACHER_CLASS_PAGE = 1; //老师上课页面
        public static final int STUDENT_LEARNING_PAGE = 2; //学生自学页面
        public static final int STUDENT_CLASS_PAGE = 3; //学生上课页面
    }
    public class ClassStatus {
        public static final int CLASS_UNABLE = 0;
        public static final int CLASS_WARNING = 1;
        public static final int CLASS_ING = 2;
    }
    public class ResourceType {
        public static final int PPT_TYPE = 1; //PPT
        public static final int IMAGE_TYPE = 3; //图片
        public static final int WORD_TYPE = 2; //文档
        public static final int VIDEO_TYPE = 4; //视频
        public static final int QUESTION_TYPE = 5; //习题
        public static final int ANSWER_TYPE = 6; //答案
    }

    public static class LevelType {
        public static final int ALL_LEVEL = 0;
        public static final int SCHOOL_LEVEL = 1;
        public static final int PRIVATE_LEVEL = 2;
        public static final int PUBLIC_LEVEL = 3;
    }
    //0: 空闲    1:提示上课    2:上课中
    public static class ClassLearningStatus {
        public static final int REST = 0;
        public static final int WARNING_UP = 1;
        public static final int CLASSING = 2;
    }
    //3: 错题本    1:学生学情    2:老师学情
    public static class QuestionDisplayPage {
        public static final int CLASS_STATISTICS = 2;
        public static final int STUDENT_STATISTICS = 1;
        public static final int WRONG_ANSWER = 3;
    }
    //0: 空闲    1:提示上课    2:上课中
    public static class AnswerStatus {
        public static final int WRONG = 1;
        public static final int RIGHT = 2;
        public static final int NO_ANSWER = 3;
    }

    public static enum SYNC_MODE {
        NONE,
        MASTER,
        SLAVE,
    }

}
