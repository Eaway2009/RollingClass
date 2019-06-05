package com.tanhd.rollingclass.server;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ChapterData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.CountClassLessonSampleData;
import com.tanhd.rollingclass.server.data.CountClassMicorcourseTimeData;
import com.tanhd.rollingclass.server.data.CountMicroCourseStudentData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GradeData;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.SectionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.StudySectionData;
import com.tanhd.rollingclass.server.data.SubjectData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;
import com.tanhd.rollingclass.server.data.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeServer extends ServerRequest {

    private static final String HOST_URL = "http://www.sea-ai.com:8001/flip";
    public static final String RESOURCE_URL = "http://www.sea-ai.com:8002/";

    private String mToken;

    private static ScopeServer mInstance = null;
    public static ScopeServer getInstance() {
        if (mInstance == null) {
            mInstance = new ScopeServer();
        }

        return mInstance;
    }

    public String loginToServer(String username, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("account", username);
        params.put("password", password);

        String response = sendRequest(HOST_URL + "/student/Studentlogin", METHOD.POST, params);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                json.put("role", "0");
            } catch (JSONException e) {
                e.printStackTrace();
                json = null;
            }

            if (json != null) {
                String errorCode = json.optString("errorCode");
                if (!TextUtils.isEmpty(errorCode) && errorCode.equals("0")) {
                    return json.toString();
                }
            }
        }

        response = sendRequest(HOST_URL + "/teacher/TeacherLogin", METHOD.POST, params);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                json.put("role", "1");
            } catch (JSONException e) {
                json = null;
            }

            if (json != null) {
                return json.toString();
            }
        }

        return null;
    }

    public void initUserData(UserData userData) {
        if (userData.isTeacher()) {
            TeacherData teacherData = (TeacherData) userData.getUserData();
            mToken = teacherData.Token;

            SchoolData schoolData = getSchoolData();
            ExternalParam.getInstance().setSchoolData(schoolData);

            List<ClassData> teachingClass = getTeachingClass(teacherData.SchoolID, teacherData.TeacherID);
            ExternalParam.getInstance().setTeachingClass(teachingClass);
        } else {
            StudentData studentData = (StudentData) userData.getUserData();
            mToken = studentData.Token;

            SchoolData schoolData = getSchoolData();
            if (schoolData != null) {
                ExternalParam.getInstance().setSchoolData(schoolData);
                ClassData classData = schoolData.queryClass(studentData.ClassID);
                ExternalParam.getInstance().setClassData(classData);
            }
        }
    }

    public List<ClassData> getTeachingClass(String schoolID, String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        params.put("teacherID", teacherID);
        String response = sendRequest(HOST_URL + "/class/QureyClassByTeacherID/" + mToken, METHOD.GET, params);
        List<ClassData> list = jsonToList(ClassData.class.getName(), response);
        if (list == null)
            return null;

        for (ClassData classData: list) {
            for (GroupData groupData: classData.Groups) {
                ArrayList<StudentData> studentList = new ArrayList<>();
                for (String studentID: groupData.Students) {
                    StudentData studentData = getStudentData(studentID);
                    if (studentData != null)
                        studentList.add(studentData);
                }
                groupData.StudentList = studentList;
            }
        }

        return list;
    }

    public StudentData getStudentData(String studentID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        String response = sendRequest(HOST_URL + "/student/QureyStudentByStudentID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<StudentData> list = jsonToList(StudentData.class.getName(), response);
            if (list != null && list.size() > 0)
                return list.get(0);
        }

        return null;
    }

    public SchoolData getSchoolData() {
        String response = sendRequest(HOST_URL + "/school/QureySchool/" + mToken, METHOD.GET, (Map<String, String>) null);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                JSONArray array = json.getJSONArray("result");
                if (array.length() > 0) {
                    SchoolData schoolData = new SchoolData();
                    schoolData.parse(schoolData, array.getJSONObject(0));
                    schoolData.SectionList = getStudySectionData(schoolData.SchoolID);
                    return schoolData;
                }
            } catch (JSONException e) {

            }
        }

        return null;
    }

    public List<StudySectionData> getStudySectionData(String schoolID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        String response = sendRequest(HOST_URL + "/studysection/QureyStudySection/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<StudySectionData> list = jsonToList(StudySectionData.class.getName(), response);
            for (StudySectionData sectionData: list) {
                sectionData.GradeList = getGradeData(schoolID, sectionData.StudysectionID);
                sectionData.SubjectList = getSubjectData(schoolID, sectionData.StudysectionCode);
            }
            return list;
        }

        return null;
    }

    public List<SubjectData> getSubjectData(String schoolID, int studysectionCode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        params.put("studysectionCode", String.valueOf(studysectionCode));
        String response = sendRequest(HOST_URL + "/subject/QureySubjectByStudySection/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<SubjectData> list = jsonToList(SubjectData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<GradeData> getGradeData(String schoolID, String studysectionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        params.put("studysectionID", studysectionID);
        String response = sendRequest(HOST_URL + "/grade/QureyGradeBySchoolID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<GradeData> list = jsonToList(GradeData.class.getName(), response);
            for (GradeData gradeData: list) {
                gradeData.ClassList = getClassData(schoolID, studysectionID, gradeData.GradeID);
            }
            return list;
        }

        return null;
    }

    public List<ClassData> getClassData(String schoolID, String studysectionID, String gradeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        params.put("studysectionID", studysectionID);
        params.put("gradeID", gradeID);
        String response = sendRequest(HOST_URL + "/class/QureyClass/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<ClassData> list = jsonToList(ClassData.class.getName(), response);
            return list;
        }

        return null;
    }

    public void UpdataTeacherPasswd(String teacherID, String oldpasswd, String newpasswd, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);
        params.put("oldpasswd", oldpasswd);
        params.put("newpasswd", newpasswd);

        new RequestTask(HOST_URL + "/teacher/UpdataTeacherPasswd/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    public void refreshExpiration(String token, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);

        new RequestTask(HOST_URL + "/student/RefreshExpiration/", METHOD.POST, params, null, callback).execute();
    }

    public void UpdataStudentPasswd(String studentID, String oldpasswd, String newpasswd, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        params.put("oldpasswd", oldpasswd);
        params.put("newpasswd", newpasswd);

        new RequestTask(HOST_URL + "/student/UpdataStudentPasswd/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    public List<TeachingMaterialData> QueryTeachingMaterialVersionList() {
        String response = sendRequest(HOST_URL + "/teachingMaterial/QueryTeachingMaterialVersionList/" + mToken, METHOD.GET, (Map<String, String>) null);
        if (response != null) {
            List<TeachingMaterialData> list = jsonToList(TeachingMaterialData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<TeachingMaterialData> QueryTeachingMaterial(int studysectioncode, int gradecode,
                                                   int subjectcode, int teachingmaterialcode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studysectioncode", String.valueOf(studysectioncode));
        params.put("gradecode", String.valueOf(gradecode));
        params.put("subjectcode", String.valueOf(subjectcode));
        params.put("teachingmaterialcode", String.valueOf(teachingmaterialcode));
        String response = sendRequest(HOST_URL + "/teachingMaterial/QueryTeachingMaterial/1/100/" + mToken, METHOD.GET, params);
        if (response != null) {

            List<TeachingMaterialData> list = jsonToList(TeachingMaterialData.class.getName(), response);
            if (list == null)
                return null;

            for (TeachingMaterialData materialData: list) {
                if (materialData.Chapters == null)
                    continue;

                for (ChapterData chapterData: materialData.Chapters) {
                    if (chapterData.Sections == null)
                        continue;

                    for (SectionData sectionData: chapterData.Sections) {
                        if (sectionData.PointIDs == null)
                            continue;

                        ArrayList<KnowledgeData> arrayList = new ArrayList<>();
                        for (String knowledgeID: sectionData.PointIDs) {
                            KnowledgeData data = QureyKnowledgeByID(knowledgeID);
                            if (data == null)
                                continue;
                            arrayList.add(data);
                        }
                        sectionData.KnowledgeList = arrayList;
                    }
                }
            }

            return list;
        }

        return null;
    }

    public KnowledgeData QureyKnowledgeByID(String knowledgeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", knowledgeID);
        String response = sendRequest(HOST_URL + "/teachingMaterial/QureyKnowledgeByID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<KnowledgeData> list = jsonToList(KnowledgeData.class.getName(), response);
            if (list != null && list.size() > 0)
                return list.get(0);
        }

        return null;
    }

    public List<LessonSampleData> QureyLessonSampleByknowledgeID(String knowledgeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", knowledgeID);
        String response = sendRequest(HOST_URL + "/teachingSample/QureyLessonSampleByknowledgeID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseByknowledgeID(String knowledgeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", knowledgeID);
        String response = sendRequest(HOST_URL + "/microcourse/QureyMicroCourseByknowledgeID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseByClassID(String classID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);
        String response = sendRequest(HOST_URL + "/microcourse/QureyMicroCourseByClassID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseByTeacherID(String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);
        String response = sendRequest(HOST_URL + "/microcourse/QureyMicroCourseByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<QuestionData> QureyQuestionByLessonSampleID(String lessonsampleID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("lessonsampleID", lessonsampleID);
        String response = sendRequest(HOST_URL + "/question/QureyQuestionByLessonSampleID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionData> list = jsonToList(QuestionData.class.getName(), response);
            return list;
        }

        return null;
    }

    public String uploadResourceFile(String filePath, int type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("type", String.valueOf(type));
        String url = uploadFile(HOST_URL + "/resource/image/upload/" + mToken, params, filePath);
        return url;
    }

    public int InsertAnswerv2(String question) {
        String response = sendRequest(HOST_URL + "/answer/InsertAnswerv2/" + mToken, METHOD.POST, question);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                return json.getInt("errorCode");
            } catch (JSONException e) {
            }
        }

        return -1;
    }

    public List<AnswerData> QureyAnswerv2ByStudentIDAndQuestionID(String studentID, String questionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        params.put("questionID", questionID);
        String response = sendRequest(HOST_URL + "/answer/QureyAnswerv2ByStudentIDAndQuestionID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }

    public int UpdataAnswerv2ByTeacher(String answerID, String score, String mark, String markurl) {
        HashMap<String, String> params = new HashMap<>();
        params.put("answerID", answerID);
        params.put("score", score);
        if (mark != null)
            params.put("mark", mark);
        else
            params.put("mark", "undefine");

        if (markurl != null)
            params.put("markurl", markurl);
        else
            params.put("markurl", "undefine");

        String response = sendRequest(HOST_URL + "/answer/UpdataAnswerv2ByTeacher/" + mToken, METHOD.POST, params);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                return json.getInt("errorCode");
            } catch (JSONException e) {
            }
        }

        return -1;
    }

    public int UpdataAnswerv2ByStudent(String answerID, String score, String mark, String markurl) {
        HashMap<String, String> params = new HashMap<>();
        params.put("answerID", answerID);
        params.put("score", score);
        params.put("mark", mark);
        params.put("markurl", markurl);
        String response = sendRequest(HOST_URL + "/answer/UpdataAnswerv2ByStudent/" + mToken, METHOD.POST, params);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                return json.getInt("errorCode");
            } catch (JSONException e) {
            }
        }

        return -1;
    }

    public int InsertMicroCourseStatistic(String data) {
        String response = sendRequest(HOST_URL + "/microCourseStatistic/InsertMicroCourseStatistic/" + mToken, METHOD.POST, data);
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getInt("errorCode");
            } catch (JSONException e) {
            }
        }

        return -1;
    }

    public List<AnswerData> QureyErrorAnswerv2ByStudentID(String studentID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        String response = sendRequest(HOST_URL + "/answer/QureyErrorAnswerv2ByStudentID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<QuestionData> QureyQuestionByID(String questionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("questionID", questionID);
        String response = sendRequest(HOST_URL + "/question/QureyQuestionByID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionData> list = jsonToList(QuestionData.class.getName(), response);
            return list;
        }

        return null;
    }

    public void downloadFile(String url, String filePath, RequestCallback callback) {
        new DownloadTask(url, filePath, callback).execute();
    }

    public TeacherData QureyTeacherByTeacherID(String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);

        String response = sendRequest(HOST_URL + "/teacher/QureyTeacherByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<TeacherData> list = jsonToList(TeacherData.class.getName(), response);
            if (list == null)
                return null;

            return list.get(0);
        }

        return null;
    }

    public List<CountClassLessonSampleData> CountClassLessonSample(String classID, String lessonSampleID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);
        params.put("lessonsampleID", lessonSampleID);

        String response = sendRequest(HOST_URL + "/question/CountClassLessonSample/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<CountClassLessonSampleData> list = jsonToList(CountClassLessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<Integer> CountClassMicorcourseTimes(String classID, String courseID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);
        params.put("courseID", courseID);

        String response = sendRequest(HOST_URL + "/microCourseStatistic/CountClassMicorcourseTimes/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<Integer> list = jsonToList(Integer.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<CountMicroCourseStudentData> QureyMicroCourseStatisticByCoureseID(String courseID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("courseID", courseID);

        String response = sendRequest(HOST_URL + "/microCourseStatistic/QureyMicroCourseStatisticByCoureseID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<CountMicroCourseStudentData> list = jsonToList(CountMicroCourseStudentData.class.getName(), response);
            return list;
        }

        return null;
    }

    public HashMap<String, List> CountStudentLessonSample(String studentID, String lessonsampleID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        params.put("lessonsampleID", lessonsampleID);

        String response = sendRequest(HOST_URL + "/question/CountStudentLessonSample/" + mToken, METHOD.GET, params);
        if (response != null) {
            try {
                JSONObject resp = new JSONObject(response);
                int errorCode = resp.getInt("errorCode");
                if (errorCode != 0)
                    return null;

                JSONObject result = resp.getJSONObject("result");
                JSONArray CorrectArray = result.optJSONArray("CorrectArray");
                ArrayList<String> list = new ArrayList<>();
                if (CorrectArray != null) {
                    for (int i=0; i<CorrectArray.length(); i++) {
                        list.add(CorrectArray.getString(i));
                    }
                }
                HashMap<String, List> out = new HashMap<>();
                out.put("CorrectArray", list);

                JSONArray ErrorArray = result.optJSONArray("ErrorArray");
                list = new ArrayList<>();
                if (ErrorArray != null) {
                    for (int i=0; i<ErrorArray.length(); i++) {
                        list.add(ErrorArray.getString(i));
                    }
                }
                out.put("ErrorArray", list);

                JSONArray UnAnswerArray = result.optJSONArray("UnAnswerArray");
                list = new ArrayList<>();
                if (UnAnswerArray != null) {
                    for (int i=0; i<UnAnswerArray.length(); i++) {
                        list.add(UnAnswerArray.getString(i));
                    }
                }
                out.put("UnAnswerArray", list);

                return out;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String InsertQuestionSet(String text) {
        String response = sendRequest(HOST_URL + "/questionset/InsertQuestionSet/" + mToken, METHOD.POST, text);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                int ret = json.getInt("errorCode");
                String result = null;
                if (ret == 0)
                    result = json.getString("result");
                return result;
            } catch (JSONException e) {
            }
        }

        return null;
    }

    public List<QuestionSetData> QureyQuestionSetByTeacherID(String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);

        String response = sendRequest(HOST_URL + "/questionset/QureyQuestionSetByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionSetData> list = jsonToList(QuestionSetData.class.getName(), response);
            if (list == null)
                return null;
            QuestionSetData.sort(list);
            return list;
        }

        return null;
    }

    public QuestionSetData QureyQuestionSetByTeacherID(String teacherID, String questionSetID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);

        String response = sendRequest(HOST_URL + "/questionset/QureyQuestionSetByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionSetData> list = jsonToList(QuestionSetData.class.getName(), response);
            if (list == null)
                return null;
            for (QuestionSetData questionSetData: list) {
                if (questionSetData.QuestionSetID.equals(questionSetID))
                    return questionSetData;
            }
        }

        return null;
    }

    public List<AnswerData> QureyAnswerv2BySetID(String setID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("setID", setID);

        String response = sendRequest(HOST_URL + "/answer/QureyAnswerv2BySetID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }

    public TeachingMaterialData QueryTeachingMaterialById(String teachingmaterialid) {
        String response = sendRequest(HOST_URL + "/teachingMaterial/QueryTeachingMaterialById/" + teachingmaterialid + "/" + mToken, METHOD.GET, (Map<String, String>) null);
        if (response != null) {
            List<TeachingMaterialData> list = jsonToList(TeachingMaterialData.class.getName(), response);
            if (list != null && list.size() > 0)
                return list.get(0);
        }

        return null;
    }

    public List<LessonSampleData> QureyLessonSampleByTeacherID(String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);

        String response = sendRequest(HOST_URL + "/teachingSample/QureyLessonSampleByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<LessonSampleData> QureyLessonSampleByClassID(String classID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);

        String response = sendRequest(HOST_URL + "/teachingSample/QureyLessonSampleByClassID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public SchoolData QureySchool() {
        String response = sendRequest(HOST_URL + "/school/QureySchool/" + mToken, METHOD.GET, (Map<String, String>) null);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                JSONArray array = json.getJSONArray("result");
                if (array.length() > 0) {
                    SchoolData schoolData = new SchoolData();
                    schoolData.parse(schoolData, array.getJSONObject(0));
                    return schoolData;
                }
            } catch (JSONException e) {

            }
        }

        return null;
    }

    public List<AnswerData> QureyAnswerv2ByTeacherIDAndQuestionID(String teacherID, String questionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);
        params.put("questionID", questionID);

        String response = sendRequest(HOST_URL + "/answer/QureyAnswerv2ByTeacherIDAndQuestionID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }
}
