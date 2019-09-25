package com.tanhd.rollingclass.db;

public class KeyConstants {
    public class KnowledgeStatus {
        public static final int FRE_CLASS = 1; //课前
        public static final int AT_CLASS = 2; //课中
        public static final int AFTER_CLASS = 3; //课后
    }
    public class ClassPageType {
        public static final int TEACHER_CLASS_PAGE = 1;
        public static final int STUDENT_LEARNING_PAGE = 2;
        public static final int STUDENT_CLASS_PAGE = 3;
    }
    public class ClassStatus {
        public static final int CLASS_UNABLE = 0;
        public static final int CLASS_WARNING = 1;
        public static final int CLASS_ING = 2;
    }
    public class ResourceType {
        public static final int PPT_TYPE = 1;
        public static final int IMAGE_TYPE = 3;
        public static final int WORD_TYPE = 2;
        public static final int VIDEO_TYPE = 4;
        public static final int QUESTION_TYPE = 5;
        public static final int ANSWER_TYPE = 6;
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

    public static enum SYNC_MODE {
        NONE,
        MASTER,
        SLAVE,
    }

}
