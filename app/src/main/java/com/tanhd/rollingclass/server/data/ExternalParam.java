package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.db.AppCacheInfo;

import java.util.List;

public class ExternalParam {
    private UserData mUserData;
    private SubjectData mSubject;
    private KnowledgeData mKnowledge;
    private LessonSampleData mLessonSample;
    private List<ClassData> TeachingClass;
    private ClassData mClassData;
    private SchoolData mSchoolData;
    private int mStatus = 0;     //0: 空闲    1:提示上课    2:上课中
    private String mQuestionSetID;
    private static ExternalParam mInstance = null;

    public static ExternalParam getInstance() {
        if (mInstance == null)
            mInstance = new ExternalParam();
        return mInstance;
    }

    private ExternalParam() {

    }

    public void setSubject(SubjectData subject) {
        mSubject = subject;
    }

    public SubjectData getSubject() {
        return mSubject;
    }

    public void setUserData(UserData userData) {
        mUserData = userData;
        AppCacheInfo.getInstance().setUserData(userData);
    }

    public UserData getUserData() {
        if (mUserData == null){
            mUserData = AppCacheInfo.getInstance().getUserData();
        }
        return mUserData;
    }

    public void setLessonSample(LessonSampleData lessonSample) {
        mLessonSample = lessonSample;
    }

    public LessonSampleData getLessonSample() {
        return mLessonSample;
    }

    public void setClassData(ClassData classData) {
        mClassData = classData;
        AppCacheInfo.getInstance().setClassData(classData);
    }

    public ClassData getClassData() {
        if (mClassData == null){
            mClassData = AppCacheInfo.getInstance().getClassData();
        }
        return mClassData;
    }

    public void setKnowledge(KnowledgeData knowledgeData) {
        mKnowledge = knowledgeData;
    }

    public KnowledgeData getKnowledge() {
        return mKnowledge;
    }

    public void setTeachingClass(List<ClassData> teachingClass) {
        TeachingClass = teachingClass;
    }

    public List<ClassData> getTeachingClass() {
        return TeachingClass;
    }

    public SchoolData getSchoolData() {
        return mSchoolData;
    }

    public void setSchoolData(SchoolData schoolData) {
        mSchoolData = schoolData;
    }

    public StudentData queryStudent(String studentID) {
        if (TeachingClass == null)
            return null;
        for (ClassData classData: TeachingClass) {
            if (classData.Groups == null)
                continue;

            for (GroupData groupData: classData.Groups) {
                if (groupData.StudentList == null)
                    continue;

                for (StudentData studentData: groupData.StudentList) {
                    if (studentData.StudentID.equals(studentID))
                        return studentData;
                }
            }
        }

        return null;
    }

    public void setStatus(int b) {
        mStatus = b;
    }

    public int getStatus() {
        return mStatus;
    }

    public void empty() {
        mUserData = null;
        mSubject = null;
        mKnowledge = null;
        mLessonSample = null;
        TeachingClass = null;
        mClassData = null;
        mSchoolData = null;
        mStatus = 0;
    }

    public void setQuestionSetID(String questionSetID) {
        this.mQuestionSetID = questionSetID;
    }

    public String getQuestionSetID() {
        return mQuestionSetID;
    }
}
