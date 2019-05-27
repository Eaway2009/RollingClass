package com.tanhd.rollingclass.server.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudentData extends BaseJsonClass {
        public String ClassID;
        public long CreateTime;
        public String GradeID;
        public String Mobile;
        public String Password;
        public String Remark;
        public String SchoolID;
        public int Sex;
        public String StudentCode;
        public String StudentID;
        public long UpdateTime;
        public String Username;
        public int Status;           //学生状态   0:离线   1:在线
        public SchoolData School;
        public String Token;
}
