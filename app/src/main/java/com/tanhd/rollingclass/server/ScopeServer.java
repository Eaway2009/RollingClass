package com.tanhd.rollingclass.server;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tanhd.rollingclass.MainApp;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.db.model.Course;
import com.tanhd.rollingclass.db.model.Result;
import com.tanhd.rollingclass.server.data.AnswerModel;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ChaptersResponse;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ChapterData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.CountClassLessonSampleData;
import com.tanhd.rollingclass.server.data.CountMicroCourseStudentData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GradeData;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.LessonSampleModel;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.QuestionStatistics;
import com.tanhd.rollingclass.server.data.RequestShareKnowledge;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.ResourceUpload;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.SectionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.StudySectionData;
import com.tanhd.rollingclass.server.data.SubjectData;
import com.tanhd.rollingclass.server.data.SyncSampleToClassRequest;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.server.data.WrongAnswerList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScopeServer extends ServerRequest {

    private static final String HOST_URL_HTTP = "http://";
    private static final String HOST_URL_PORT = ":8001/flip";
    public static final String RESOURCE_URL_PORT = ":8002/";
    private String mHostUrl = "sea-ai.com";
    //    private String mHostUrl = "10.1.1.123";
    private static final String TAG = "ScopeServer";

    private String mToken;

    private static ScopeServer mInstance = null;

    public static ScopeServer getInstance() {
        if (mInstance == null) {
            mInstance = new ScopeServer();
        }

        return mInstance;
    }

    private String getHostUrl() {
        return HOST_URL_HTTP + mHostUrl + HOST_URL_PORT;
    }

    public String getResourceUrl() {
        return HOST_URL_HTTP + mHostUrl + RESOURCE_URL_PORT;
    }

    public String getHost() {
        return mHostUrl;
    }

    public void setHost(String hostUrl) {
        mHostUrl = hostUrl;
    }

    public String loginToServer(String username, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("account", username);
        params.put("password", password);

        String response = sendRequest(getHostUrl() + "/student/Studentlogin", METHOD.POST, params);
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

        response = sendRequest(getHostUrl() + "/teacher/TeacherLogin", METHOD.POST, params);
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

    public String studentLoginToServer(String username, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("account", username);
        params.put("password", password);

        String response = sendRequest(getHostUrl() + "/student/Studentlogin", METHOD.POST, params);
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
                return json.toString();
            }
        }
        return null;
    }

    public String teacherLoginToServer(String username, String password) {
        HashMap<String, String> params = new HashMap<>();
        params.put("account", username);
        params.put("password", password);

        String response = sendRequest(getHostUrl() + "/teacher/TeacherLogin", METHOD.POST, params);
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

    public void initToken(UserData userData) {
        if (userData.isTeacher()) {
            TeacherData teacherData = (TeacherData) userData.getUserData();
            mToken = teacherData.Token;
        } else {
            StudentData studentData = (StudentData) userData.getUserData();
            mToken = studentData.Token;
        }
    }

    public void initUserData(UserData userData) {
        if (userData.isTeacher()) {
            Log.i(TAG, "initUserData: teacher");
            TeacherData teacherData = (TeacherData) userData.getUserData();
            mToken = teacherData.Token;

            SchoolData schoolData = getSchoolData();
            ExternalParam.getInstance().setSchoolData(schoolData);

            List<ClassData> teachingClass = getTeachingClass(teacherData.SchoolID, teacherData.TeacherID);
            ExternalParam.getInstance().setTeachingClass(teachingClass);
        } else {
            Log.i(TAG, "initUserData: student");
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
        String response = sendRequest(getHostUrl() + "/class/QureyClassByTeacherID/" + mToken, METHOD.GET, params);
        List<ClassData> list = jsonToList(ClassData.class.getName(), response);
        if (list == null)
            return null;

        for (ClassData classData : list) {
            for (GroupData groupData : classData.Groups) {
                ArrayList<StudentData> studentList = new ArrayList<>();
                if (groupData != null && groupData.Students != null) {
                    for (String studentID : groupData.Students) {
                        StudentData studentData = getStudentData(studentID);
                        if (studentData != null)
                            studentList.add(studentData);
                    }
                }
                groupData.StudentList = studentList;
            }
        }

        return list;
    }

    public List<ClassData> QureyClassByTeacherIDV2(String schoolID, String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        params.put("teacherID", teacherID);
        String response = sendRequest(getHostUrl() + "/class/QureyClassByTeacherIDV2/" + mToken, METHOD.GET, params);
        List<ClassData> list = jsonToList(ClassData.class.getName(), response);
        if (list == null)
            return null;

        for (ClassData classData : list) {
            for (GroupData groupData : classData.Groups) {
                ArrayList<StudentData> studentList = new ArrayList<>();
                if (groupData != null && groupData.Students != null) {
                    for (String studentID : groupData.Students) {
                        StudentData studentData = getStudentData(studentID);
                        if (studentData != null)
                            studentList.add(studentData);
                    }
                }
                groupData.StudentList = studentList;
            }
        }

        return list;
    }

    public StudentData getStudentData(String studentID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        String response = sendRequest(getHostUrl() + "/student/QureyStudentByStudentID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<StudentData> list = jsonToList(StudentData.class.getName(), response);
            if (list != null && list.size() > 0)
                return list.get(0);
        }

        return null;
    }

    public SchoolData getSchoolData() {
        String response = sendRequest(getHostUrl() + "/school/QureySchool/" + mToken, METHOD.GET, (Map<String, String>) null);
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
        String response = sendRequest(getHostUrl() + "/studysection/QureyStudySection/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<StudySectionData> list = jsonToList(StudySectionData.class.getName(), response);
            for (StudySectionData sectionData : list) {
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
        String response = sendRequest(getHostUrl() + "/subject/QureySubjectByStudySection/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<SubjectData> list = jsonToList(SubjectData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<SubjectData> qureySubject(String schoolID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/subject/QureySubject/" + mToken, METHOD.GET, params);
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
        String response = sendRequest(getHostUrl() + "/grade/QureyGradeBySchoolID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<GradeData> list = jsonToList(GradeData.class.getName(), response);
            for (GradeData gradeData : list) {
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
        String response = sendRequest(getHostUrl() + "/class/QureyClass/" + mToken, METHOD.GET, params);
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

        new RequestTask(getHostUrl() + "/teacher/UpdataTeacherPasswd/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    public void StoreResource(String teacherID, String resourceID, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);
        params.put("resourceID", resourceID);

        new RequestTask(getHostUrl() + "/resource/StoreResource/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    public Map<String, String> refreshExpiration(String token) {
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        Log.i("refreshExpiration", ": token" + token);

        String response = sendRequest(getHostUrl() + "/student/RefreshExpiration", METHOD.POST, params);
        Map<String, String> map = new HashMap<>();
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                Log.i("refreshExpiration", ": errorCode" + json.getInt("errorCode"));
                Log.i("refreshExpiration", ": errorMessage" + json.getString("errorMessage"));
                map.put("errorCode", json.optString("errorCode"));
                map.put("errorMessage", json.getString("errorMessage"));
                return map;
            } catch (JSONException e) {
            }
        }

        return map;
    }

    public void UpdataStudentPasswd(String studentID, String oldpasswd, String newpasswd, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        params.put("oldpasswd", oldpasswd);
        params.put("newpasswd", newpasswd);

        new RequestTask(getHostUrl() + "/student/UpdataStudentPasswd/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    public List<TeachingMaterialData> QueryTeachingMaterialVersionList() {
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QueryTeachingMaterialVersionList/" + mToken, METHOD.GET, (Map<String, String>) null);
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
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QueryTeachingMaterial/1/100/" + mToken, METHOD.GET, params);
        if (response != null) {

            List<TeachingMaterialData> list = jsonToList(TeachingMaterialData.class.getName(), response);
            if (list == null)
                return null;

            for (TeachingMaterialData materialData : list) {
                if (materialData.Chapters == null)
                    continue;

                for (ChapterData chapterData : materialData.Chapters) {
                    if (chapterData.Sections == null)
                        continue;

                    for (SectionData sectionData : chapterData.Sections) {
                        if (sectionData.PointIDs == null)
                            continue;

                        ArrayList<KnowledgeData> arrayList = new ArrayList<>();
                        for (String knowledgeID : sectionData.PointIDs) {
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
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QureyKnowledgeByID/" + mToken, METHOD.GET, params);
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
        String response = sendRequest(getHostUrl() + "/teachingSample/QureyLessonSampleByknowledgeID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseByknowledgeID(String knowledgeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", knowledgeID);
        String response = sendRequest(getHostUrl() + "/microcourse/QureyMicroCourseByknowledgeID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseByClassID(String classID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);
        String response = sendRequest(getHostUrl() + "/microcourse/QureyMicroCourseByClassID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseBySubjectCode(int subjectcode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("subjectcode", "" + subjectcode);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/microcourse/QureyMicroCourseBySubjectCode/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    /**
     * 获取老师学案列表
     * @param teacherId
     * @param teaching_material_id
     * @return
     */
    public List<KnowledgeDetailMessage> QureyKnowledgeByChapterAndTeacherID(String teacherId, String teaching_material_id) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teaching_material_id", "" + teaching_material_id);
        params.put("teacherID", "" + teacherId);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QureyKnowledgeByChapterAndTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<KnowledgeDetailMessage> list = jsonToList(KnowledgeDetailMessage.class.getName(), response);
            return list;
        }

        return null;
    }

    /**
     * 学生查询已经发布的课时
     * @param classID
     * @param teaching_material_id
     * @return
     */
    public List<KnowledgeDetailMessage> QureyKnowledgeByClassID(String classID, String teaching_material_id) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teaching_material_id", "" + teaching_material_id);
        params.put("classID", "" + classID);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QureyKnowledgeByClassID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<KnowledgeDetailMessage> list = jsonToList(KnowledgeDetailMessage.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<ResourceModel> QureyResourceByTeacherID(String teacherId, String teaching_material_id,
                                                        int level, int resourceType, int page, int pagesize) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", "" + teacherId);
        params.put("teaching_material_id", "" + teaching_material_id);
        params.put("level", "" + level);
        params.put("resourceType", "" + resourceType);
        params.put("page", "" + page);
        params.put("pagesize", "" + pagesize);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/resource/QureyResourceByTeacherID" +
                appenUrl(page + "") + appenUrl(pagesize + "") + appenUrl(mToken), METHOD.GET, params);
        if (response != null) {
            List<ResourceModel> list = jsonToList(ResourceModel.class.getName(), response);
            return list;
        }
        return null;
    }

    public List<QuestionModel> QureyQuestionResourceByTeacherID(String teacherId, String teaching_material_id,
                                                                int level, int page, int pagesize) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", "" + teacherId);
        params.put("teaching_material_id", "" + teaching_material_id);
        params.put("level", "" + level);
        params.put("resourceType", "" + KeyConstants.ResourceType.QUESTION_TYPE);
        params.put("page", "" + page);
        params.put("pagesize", "" + pagesize);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/resource/QureyResourceByTeacherID" +
                appenUrl(page + "") + appenUrl(pagesize + "") + appenUrl(mToken), METHOD.GET, params);
        if (response != null) {
            List<QuestionModel> list = jsonToList(QuestionModel.class.getName(), response);
            return list;
        }
        return null;
    }

    public WrongAnswerList QureyAnswerv2ByStudentIDAndCourseID(String studentID, String knowlegeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", "" + studentID);
        params.put("knowlegeID", "" + knowlegeID);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/answer/QureyAnswerv2ByStudentIDAndCourseID/" +
               appenUrl(mToken), METHOD.GET, params);
        if (response != null) {
            WrongAnswerList list = (WrongAnswerList) jsonToModel(WrongAnswerList.class.getName(), response);
            return list;
        }
        return null;
    }

    public QuestionStatistics QureyAnswerv2ByClassIDAndCourseID(String classID, String knowlegeID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", "" + classID);
        params.put("knowlegeID", "" + knowlegeID);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/answer/QureyAnswerv2ByClassIDAndCourseID/" +
               appenUrl(mToken), METHOD.GET, params);
        if (response != null) {
            //QuestionStatistics list = (QuestionStatistics) jsonToModel(QuestionStatistics.class.getName(), response);

            Type resultType = new TypeToken<Result<QuestionStatistics>>(){}.getType();
            QuestionStatistics list = null;
            Result<QuestionStatistics> result = new Gson().fromJson(response, resultType);
            if (result.getStatus() == 0){
                list = result.getData();
            }
            return list;
        }
        return null;
    }

    private String appenUrl(String data) {
        return "/" + data;
    }

    /**
     * 删除课时(即删除识点)
     *
     * @param teaching_material_id 教材ID
     * @param knowledgeid          课时信息
     * @return
     */
    public void DeleteKnowledge(String teaching_material_id, String knowledgeid, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teaching_material_id", "" + teaching_material_id);
        params.put("knowledgeid", "" + knowledgeid);
        params.put("token", mToken);

        new RequestTask(getHostUrl() + "/teachingMaterial/DeleteKnowledge/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    /**
     * 复制课时
     *
     * @return
     */
    public void DumpKnowledge(String knowledgeID, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", "" + knowledgeID);
        params.put("token", mToken);

        new RequestTask(getHostUrl() + "/teachingMaterial/DumpKnowledge/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    /**
     * 分享课时
     *
     * @return
     */
    public void ShareKnowledgeToTeachers(RequestShareKnowledge request, RequestCallback callback) {
        new RequestTask(getHostUrl() + "/teachingMaterial/ShareKnowledgeToTeachers/" + mToken, METHOD.POST, null, request.toJSON().toString(), callback).execute();
    }

    /**
     * 修改课时名称(即删除识点)
     *
     * @param name        新教材名称
     * @param knowledgeID 课时信息
     * @return
     */
    public void UpdateKnowledgeName(String name, String knowledgeID, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("name", "" + name);
        params.put("knowledgeID", "" + knowledgeID);
        params.put("token", mToken);

        new RequestTask(getHostUrl() + "/teachingMaterial/UpdateKnowledgeName/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    /**
     * 修改课时状态  status
     *
     * @param status      0 未上课 1: 上课中 2: 上课结束
     * @param classID     对应班级
     * @param knowledgeID 课时信息
     * @return
     */
    public void UpdateKnowledgeStatus(int status, String classID, String knowledgeID, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("status", "" + status);
        params.put("classID", classID);
        params.put("knowledgeID", knowledgeID);
        params.put("token", mToken);

        new RequestTask(getHostUrl() + "/teachingMaterial/UpdateKnowledgeStatus/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    /**
     * 课时发布
     *
     * @param teacherID      老师ID
     * @param knowledgeID    课时IO
     * @param releasebefore  课前发布
     * @param releaseafter   课后发布
     * @param releaseprocess 课中发布
     * @return
     */
    public String ReleaseKnowledgeToClass(String knowledgeID, String teacherID, int releasebefore, int releaseprocess, int releaseafter) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", "" + knowledgeID);
        params.put("teacherID", "" + teacherID);
        params.put("releasebefore", "" + releasebefore);
        params.put("releaseprocess", "" + releaseprocess);
        params.put("releaseafter", "" + releaseafter);
        params.put("token", mToken);

        String response = sendRequest(getHostUrl() + "/teachingMaterial/ReleaseKnowledgeToClass/" + mToken, METHOD.POST, params);
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                int result = json.getInt("errorCode");
                if (result == 0) {
                    return null;
                } else {
                    return json.getString("errorMessage");
                }
            } catch (JSONException e) {
            }
        }

        return MainApp.getInstance().getString(R.string.lbl_publish_fail);
    }

    /**
     * 任务同步
     *
     * @param request
     * @return
     */
    public String SyncSampleToClass(SyncSampleToClassRequest request) {
        String response = sendRequest(getHostUrl() + "/teachingMaterial/SyncSampleToClass/" + mToken, METHOD.POST, request.toJSON().toString());
        if (response != null) {
            JSONObject json;
            try {
                json = new JSONObject(response);
                int result = json.getInt("errorCode");
                if (result == 0) {
                    return "";
                } else {
                    return json.getString("errorMessage");
                }
            } catch (JSONException e) {
            }
        }

        return MainApp.getInstance().getString(R.string.lbl_synchronization_fail);
    }

    /**
     * 添加课时(即增加知识点)
     *
     * @param data 课时信息
     * @return
     */
    public void InsertKnowledge(KnowledgeModel data, RequestCallback callback) {
        new RequestTask(getHostUrl() + "/teachingMaterial/InsertKnowledge/" + mToken, METHOD.POST, null, data.toJSON().toString(), callback).execute();
    }

    /**
     * 添加任务
     *
     * @param data 课时信息
     * @return
     */
    public void InsertLessonSample(LessonSampleModel data, RequestCallback callback) {
        new RequestTask(getHostUrl() + "/teachingSample/InsertLessonSample/" + mToken, METHOD.POST, null, data.toJSON().toString(), callback).execute();
    }

    /**
     * 修改任务
     *
     * @param data 任务信息
     * @return
     */
    public void EditLessonSample(LessonSampleModel data, RequestCallback callback) {
        new RequestTask(getHostUrl() + "/teachingSample/EditLessonSample/" + mToken, METHOD.POST, null, data.toJSON().toString(), callback).execute();
    }

    /**
     * 删除任务 （删除指定教材数据 ）
     *
     * @param sampleID 任务信息
     * @return
     */
    public void DeleteLessonSample(String sampleID, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("sampleID", "" + sampleID);
        params.put("token", mToken);

        new RequestTask(getHostUrl() + "/teachingSample/DeleteLessonSample/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    /**
     * 查询教材列表
     *
     * @param schoolid         学校ID option(1小学 2初中 3高中)
     * @param studysectioncode 学段代码 option(1小学 2初中 3高中)
     * @param subjectcode      学科代码 option(1数学 2语文 ……20道德与法制)
     * @return
     */
    public List<ChaptersResponse> QueryTeachingMaterial(String schoolid, int studysectioncode, int subjectcode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolid", schoolid + "");
        params.put("studysectioncode", studysectioncode + "");
        params.put("subjectcode", subjectcode + "");
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QueryTeachingMaterial/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<ChaptersResponse> responseList = jsonToList(ChaptersResponse.class.getName(), response);
            return responseList;
        }

        return null;
    }

    /**
     * 学生查询教材列表通过年级ID
     *
     * @param schoolid  学校id
     * @param gradecode 年级代码
     * @return
     */
    public List<ChaptersResponse> QueryTeachingMaterialByGradeID(String schoolid, String gradecode) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolid", "" + schoolid);
        params.put("gradecode", gradecode);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QueryTeachingMaterialByGradeID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<ChaptersResponse> list = jsonToList(ChaptersResponse.class.getName(), response);
            return list;
        }

        return null;
    }

    /**
     * 查绚指定课时
     *
     * @param knowledgeID
     * @return
     */
    public List<KnowledgeLessonSample> QuerySampleByKnowledge(String knowledgeID, int class_type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", knowledgeID);
        params.put("class_type", class_type + "");
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/teachingSample/QuerySampleByKnowledge/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<KnowledgeLessonSample> list = jsonToList(KnowledgeLessonSample.class.getName(), response);
            return list;
        }

        return null;
    }

    /**
     * 查绚指定课时
     *
     * @param lesson_sample_id
     * @return
     */
    public void AddLessonSampleResource(String lesson_sample_id,String resource_id, int resource_type, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("lesson_sample_id", lesson_sample_id);
        params.put("resource_id", resource_id);
        params.put("resource_type", resource_type + "");
        params.put("token", mToken);
        new RequestTask(getHostUrl() + "/teachingSample/AddLessonSampleResource/" + mToken, METHOD.POST, params, null, callback).execute();
    }

    /**
     * 查绚指定学科知识点
     *
     * @param knowledgeID
     * @return
     */
    public List<MicroCourseData> QureyKnowledgeByID(String knowledgeID, RequestCallback callback) {
        HashMap<String, String> params = new HashMap<>();
        params.put("knowledgeID", knowledgeID);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QureyKnowledgeByID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<MicroCourseData> QureyMicroCourseByTeacherID(String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);
        String response = sendRequest(getHostUrl() + "/microcourse/QureyMicroCourseByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<MicroCourseData> list = jsonToList(MicroCourseData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<QuestionModel> QureyQuestionByLessonSampleID(String lessonsampleID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("lessonsampleID", lessonsampleID);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/question/QureyQuestionByLessonSampleID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionModel> list = jsonToList(QuestionModel.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<QuestionModel> QureyQuestionSetByKnowledgeID(String setId) {
        HashMap<String, String> params = new HashMap<>();
        params.put("setID", setId);
        params.put("token", mToken);
        String response = sendRequest(getHostUrl() + "/questionset/QureyQuestionSetByKnowledgeID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionModel> list = jsonToList(QuestionModel.class.getName(), response);
            return list;
        }

        return null;
    }

    public String uploadResourceFile(String filePath, int type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("type", String.valueOf(type));
        String url = uploadFile(getHostUrl() + "/resource/image/upload/" + mToken, params, filePath);
        return url;
    }

    /**
     * @param filePath
     * @param teahcerID
     * @param fileName
     * @param resource_type 1. ppt 2. doc 3. image 4. 微课 5. 习题
     * @param level         资源类别: 1 公共资源 2 校本资源 3 私藏资源
     * @return
     */
    public String resourceUpload(String filePath, String teahcerID, String teaching_material_id, String fileName, int resource_type, int level) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teahcerID", teahcerID);
        params.put("name", fileName);
        params.put("resource_type", resource_type + "");
        params.put("level", level + "");
        params.put("teacher_id", teahcerID);
        params.put("teaching_material_id", teaching_material_id);
        params.put("token", mToken);
        String response = uploadFile(getHostUrl() + "/resource/resource/upload/" + mToken, params, filePath, true);
        return response;
    }

    public int InsertAnswerv2(String question) {
        String response = sendRequest(getHostUrl() + "/answer/InsertAnswerv2/" + mToken, METHOD.POST, question);
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

    /**
     * 答题信息查绚通过老师ID和课时ID(用于统计信息)
     * @param teacherID
     * @param knowlegeID
     * @param answertype
     * @param querytype
     * @return
     */
    public List<AnswerData> QureyAnswerv2ByTeacherIDAndCourseID(String teacherID, String knowlegeID, int answertype, int querytype) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);
        params.put("knowlegeID", knowlegeID);
        params.put("answertype", answertype+"");
        params.put("querytype", querytype+"");
        String response = sendRequest(getHostUrl() + "/answer/QureyAnswerv2ByTeacherIDAndCourseID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<AnswerData> QureyAnswerv2ByStudentIDAndQuestionID(String studentID, String questionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        params.put("questionID", questionID);
        String response = sendRequest(getHostUrl() + "/answer/QureyAnswerv2ByStudentIDAndQuestionID/" + mToken, METHOD.GET, params);
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

        String response = sendRequest(getHostUrl() + "/answer/UpdataAnswerv2ByTeacher/" + mToken, METHOD.POST, params);
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
        String response = sendRequest(getHostUrl() + "/answer/UpdataAnswerv2ByStudent/" + mToken, METHOD.POST, params);
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
        String response = sendRequest(getHostUrl() + "/microCourseStatistic/InsertMicroCourseStatistic/" + mToken, METHOD.POST, data);
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
        String response = sendRequest(getHostUrl() + "/answer/QureyErrorAnswerv2ByStudentID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<QuestionModel> QureyQuestionByID(String questionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("questionID", questionID);
        String response = sendRequest(getHostUrl() + "/question/QureyQuestionByID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionModel> list = jsonToList(QuestionModel.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<QuestionData> QureyQuestionByID2(String questionID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("questionID", questionID);
        String response = sendRequest(getHostUrl() + "/question/QureyQuestionByID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionData> list = jsonToList(QuestionData.class.getName(), response);
            return list;
        }

        return null;
    }

    public void downloadFile(String url, String filePath, RequestCallback callback) {
        String allUrl = ScopeServer.getInstance().getResourceUrl() + url;
        new DownloadTask(allUrl, filePath, callback).execute();
    }

    public TeacherData QureyTeacherByTeacherID(String teacherID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("teacherID", teacherID);

        String response = sendRequest(getHostUrl() + "/teacher/QureyTeacherByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<TeacherData> list = jsonToList(TeacherData.class.getName(), response);
            if (list == null)
                return null;

            return list.get(0);
        }

        return null;
    }

    public List<TeacherData> QureyTeacherBySchoolID(String schoolID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("schoolID", schoolID);

        String response = sendRequest(getHostUrl() + "/teacher/QureyTeacherBySchoolID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<TeacherData> list = jsonToList(TeacherData.class.getName(), response);
            if (list == null)
                return null;

            return list;
        }

        return null;
    }

    public List<CountClassLessonSampleData> CountClassLessonSample(String classID, String lessonSampleID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);
        params.put("lessonsampleID", lessonSampleID);

        String response = sendRequest(getHostUrl() + "/question/CountClassLessonSample/" + mToken, METHOD.GET, params);
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

        String response = sendRequest(getHostUrl() + "/microCourseStatistic/CountClassMicorcourseTimes/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<Integer> list = jsonToList(Integer.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<Integer> CountStudentMicorcourseTimes(String studentID, String courseID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("studentID", studentID);
        params.put("courseID", courseID);

        String response = sendRequest(getHostUrl() + "/microCourseStatistic/CountStudentMicorcourseTimes/" + mToken, METHOD.GET, params);
        List<Integer> list = new ArrayList<>();
        if (response != null) {
            JSONObject resp = null;
            try {
                resp = new JSONObject(response);
                int errorCode = resp.getInt("errorCode");
                if (errorCode != 0)
                    return null;

                JSONObject result = resp.getJSONObject("result");
                Iterator it = result.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    //得到value的值
                    Integer value = (Integer) result.get(key);
                    int index = Integer.valueOf(key);
                    if (list.size() > index) {
                        list.set(index, value);
                    } else {
                        int add = list.size();
                        while (add < index) {
                            list.add(add, 0);
                            add++;
                        }
                        list.add(index, value);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        return null;
    }

    public List<CountMicroCourseStudentData> QureyMicroCourseStatisticByCoureseID(String courseID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("courseID", courseID);

        String response = sendRequest(getHostUrl() + "/microCourseStatistic/QureyMicroCourseStatisticByCoureseID/" + mToken, METHOD.GET, params);
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

        String response = sendRequest(getHostUrl() + "/question/CountStudentLessonSample/" + mToken, METHOD.GET, params);
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
                    for (int i = 0; i < CorrectArray.length(); i++) {
                        list.add(CorrectArray.getString(i));
                    }
                }
                HashMap<String, List> out = new HashMap<>();
                out.put("CorrectArray", list);

                JSONArray ErrorArray = result.optJSONArray("ErrorArray");
                list = new ArrayList<>();
                if (ErrorArray != null) {
                    for (int i = 0; i < ErrorArray.length(); i++) {
                        list.add(ErrorArray.getString(i));
                    }
                }
                out.put("ErrorArray", list);

                JSONArray UnAnswerArray = result.optJSONArray("UnAnswerArray");
                list = new ArrayList<>();
                if (UnAnswerArray != null) {
                    for (int i = 0; i < UnAnswerArray.length(); i++) {
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
        String response = sendRequest(getHostUrl() + "/questionset/InsertQuestionSet/" + mToken, METHOD.POST, text);
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

        String response = sendRequest(getHostUrl() + "/questionset/QureyQuestionSetByTeacherID/" + mToken, METHOD.GET, params);
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

        String response = sendRequest(getHostUrl() + "/questionset/QureyQuestionSetByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<QuestionSetData> list = jsonToList(QuestionSetData.class.getName(), response);
            if (list == null)
                return null;
            for (QuestionSetData questionSetData : list) {
                if (questionSetData.QuestionSetID.equals(questionSetID))
                    return questionSetData;
            }
        }

        return null;
    }

    public List<AnswerData> QureyAnswerv2BySetID(String setID) {
        HashMap<String, String> params = new HashMap<>();
        params.put("setID", setID);

        String response = sendRequest(getHostUrl() + "/answer/QureyAnswerv2BySetID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<AnswerData> list = jsonToList(AnswerData.class.getName(), response);
            return list;
        }

        return null;
    }

    public TeachingMaterialData QueryTeachingMaterialById(String teachingmaterialid) {
        String response = sendRequest(getHostUrl() + "/teachingMaterial/QueryTeachingMaterialById/" + teachingmaterialid + "/" + mToken, METHOD.GET, (Map<String, String>) null);
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

        String response = sendRequest(getHostUrl() + "/teachingSample/QureyLessonSampleByTeacherID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<LessonSampleData> QureyLessonSampleByClassID(String classID) {
        Log.i("LessonSampleByClassID", "classID:" + classID);
        Log.i("LessonSampleBySubject", "mToken:" + mToken);
        HashMap<String, String> params = new HashMap<>();
        params.put("classID", classID);

        String response = sendRequest(getHostUrl() + "/teachingSample/QureyLessonSampleByClassID/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public List<LessonSampleData> QureyLessonSampleBySubject(int subjectCode) {
        Log.i("LessonSampleBySubject", "mToken:" + mToken);
        Log.i("LessonSampleBySubject", "subjectCode:" + subjectCode);
        HashMap<String, String> params = new HashMap<>();
        params.put("subjectcode", String.valueOf(subjectCode));
        params.put("token", mToken);

        String response = sendRequest(getHostUrl() + "/teachingSample/QureyLessonSampleBySubject/" + mToken, METHOD.GET, params);
        if (response != null) {
            List<LessonSampleData> list = jsonToList(LessonSampleData.class.getName(), response);
            return list;
        }

        return null;
    }

    public SchoolData QureySchool() {
        String response = sendRequest(getHostUrl() + "/school/QureySchool/" + mToken, METHOD.GET, (Map<String, String>) null);
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

        String response = sendRequest(getHostUrl() + "/answer/QureyAnswerv2ByTeacherIDAndQuestionID/" + mToken, METHOD.GET, params);
        List<AnswerData> list = null;
        if (response != null) {
            list = jsonToList(AnswerData.class.getName(), response);
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }
}
