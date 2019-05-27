package com.tanhd.rollingclass.server.data;

import java.util.List;

public class StudySectionData extends BaseJsonClass {
    public String StudysectionID;
    public String StudysectionName;
    public String SchoolID;
    public int StudysectionCode;
    public String Remark;
    public List<GradeData> GradeList;
    public List<SubjectData> SubjectList;
}
