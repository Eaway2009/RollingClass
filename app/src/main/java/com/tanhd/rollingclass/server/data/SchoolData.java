package com.tanhd.rollingclass.server.data;

import java.util.List;
import java.util.UUID;

public class SchoolData extends BaseJsonClass {
    public String SchoolID;
    public String SchoolName;
    public String Remark;
    public List<StudySectionData> SectionList;

    public ClassData queryClass(String classID) {
        for (StudySectionData sectionData: SectionList) {
            if (sectionData.GradeList == null)
                continue;

            for (GradeData gradeData: sectionData.GradeList) {
                if (gradeData.ClassList == null)
                    return null;

                for (ClassData classData: gradeData.ClassList) {
                    if (classData.ClassID.equals(classID))
                        return classData;
                }
            }
        }

        return null;
    }

    public List<SubjectData> querySubjects(int studySectionCode) {
        if (SectionList == null)
            return null;

        for (StudySectionData sectionData: SectionList) {
            if (sectionData.StudysectionCode == studySectionCode)
                return sectionData.SubjectList;
        }

        return null;
    }
}
