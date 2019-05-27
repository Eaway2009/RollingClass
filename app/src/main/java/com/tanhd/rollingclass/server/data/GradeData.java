package com.tanhd.rollingclass.server.data;

import java.util.List;

public class GradeData extends BaseJsonClass {
    public String GradeID;
    public int GradeCode;
    public String GradeName;
    public String SchoolID;
    public String StudysectionID;
    public String Remark;
    public List<ClassData> ClassList;
}
