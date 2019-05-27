package com.tanhd.rollingclass.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class FakeServer extends NanoHTTPD {

    public FakeServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        HashMap<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        Map<String, List<String>> parms = session.getParameters();
        String path = session.getUri();
        String msg = "{\n" +
                "  \"errorCode\": 1002,\n" +
                "  \"errorMessage\": \"失败\",\n" +
                "}";
        if (path.contains("TeacherLogin")) {
            msg = "{\"errorCode\":0,\"errorMessage\":\"success\",\"result\":{\"CreateTime\":1545553312005,\"Mobile\":\"18818561292\",\"Name\":\"\\u674e\\u8001\\u5e08\",\"Password\":\"123\",\"Remark\":\"\",\"SchoolID\":\"5c1f3b1a1d41c8aa676d22fd\",\"SchoolName\":\"\\u6d4b\\u8bd5\\u5b66\\u6821\",\"Sex\":1,\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"TeachingClass\":[{\"ClassID\":\"5c1f42a51d41c8ae517a380d\",\"ClassName\":\"\\u6d4b\\u8bd5\\u73ed\\u7ea7\",\"CreateTime\":0,\"GradeID\":\"5c1f3b1a1d41c8aa676d22fd\",\"Remark\":\"\",\"SchoolID\":\"5c1f38f71d41c8a8613bc853\",\"StudentIDs\":null,\"TopicName\":\"\",\"UpdateTime\":0}],\"UpdateTime\":0,\"Username\":\"teacher\"}}";
        } else if (path.contains("QureyStudentArryByClassID")) {
            msg = "{\"errorCode\":0,\"errorMessage\":\"success\",\"result\":[{\"User1\":{\"ClassID\":\"5c1f42a51d41c8ae517a380d\",\"CreateTime\":1545553644323,\"GradeID\":\"5c1f3b1a1d41c8aa676d22fd\",\"Mobile\":\"88888888\",\"Name\":\"\\u6d4b\\u8bd5\\u5b66\\u751f1\",\"Password\":\"123\",\"Remark\":\"\",\"SchoolID\":\"5c1f38f71d41c8a8613bc853\",\"Sex\":1,\"StudentCode\":\"11\",\"StudentID\":\"5c1f46ec1d41c8aeb4a5e537\",\"UpdateTime\":0,\"Username\":\"student1\"},\"User2\":null}]}";
        } else if (path.contains("QureyLessonSampleByTeacherID")) {
            msg = "{\"errorCode\":0,\"errorMessage\":\"success\",\"result\":[{\"ChapterName\":\"\\u7b2c\\u4e00\\u7ae0\",\"ClassIds\":[\"5c1f42a51d41c8ae517a380d\"],\"CreateTime\":1545563899543,\"GradeIds\":[\"5c1f3b1a1d41c8aa676d22fd\"],\"LessonSampleID\":\"5c1f6efb1d41c8bd2db038a6\",\"LessonSampleName\":\"\\u6d4b\\u8bd5\",\"PointName\":\"\\u7b2c\\u4e00\\u77e5\\u8bc6\\u70b9\",\"Remark\":\"\",\"SectionName\":\"\\u5065\\u5eb7\",\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"TextContent\":[{\"Text\":\"test\",\"Title\":\"\\u4f60\\u4eec\\u597d\\u5065\\u5eb7\"}],\"UpdateTime\":0,\"UrlContent\":\"\"},{\"ChapterName\":\"\\u7b2c\\u4e00\\u7ae0\",\"ClassIds\":[\"5c1f42a51d41c8ae517a380d\"],\"CreateTime\":1545719548364,\"GradeIds\":[\"5c1f3b1a1d41c8aa676d22fd\"],\"LessonSampleID\":\"5c21cefce6cf0010ffb92c0a\",\"LessonSampleName\":\"\\u7b2c\\u4e00\\u4e2a\\u5b66\\u6848\",\"PointName\":\"\\u4eba\\u4e0e\\u81ea\\u7136\",\"Remark\":\"\\u5907\\u6ce8\",\"SectionName\":\"\\u7b2c\\u4e00\\u8282\",\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"TextContent\":[{\"Text\":\"\\u6d4b\\u8bd5\\u5b66\\u6848\",\"Title\":\"1+1=\\uff1f\"}],\"UpdateTime\":0,\"UrlContent\":\"\"}]}";
        } else if (path.contains("QureyQuestionByLessonSampleID")) {
            msg = "{\"errorCode\":0,\"errorMessage\":\"success\",\"result\":[{\"ConsultSolution\":\"it is consult solution\",\"ConsultSolutionUrl\":\"url address\",\"CreateTime\":1545567006184,\"Image\":\"\",\"LessonSampleID\":\"5c1f6efb1d41c8bd2db038a6\",\"LessonSampleName\":\"\\u6d4b\\u8bd5\",\"QuestionAnalysis\":\"it is annlysis\",\"QuestionAnalysisUrl\":\"it is annlysis url\",\"QuestionCoordinate\":null,\"QuestionID\":\"5c1f7b1e1d41c8c082e69459\",\"QuestionName\":\"how are you\",\"QuestionType\":\"\\u9009\\u62e9\\u9898\",\"Remark\":\"\",\"Solutions\":[\"yes\",\"yes or no\",\"no\",\"unkown\"],\"Stem\":\"more apple\",\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"TeacherName\":\"\\u674e\\u8001\\u5e08\",\"UpdateTime\":0},{\"ConsultSolution\":\"it is consult solution two\",\"ConsultSolutionUrl\":\"url address two\",\"CreateTime\":1545567115039,\"Image\":\"\",\"LessonSampleID\":\"5c1f6efb1d41c8bd2db038a6\",\"LessonSampleName\":\"\\u6d4b\\u8bd5\",\"QuestionAnalysis\":\"it is annlysis two \",\"QuestionAnalysisUrl\":\"it is annlysis url two\",\"QuestionCoordinate\":null,\"QuestionID\":\"5c1f7b8b1d41c8c082e6945a\",\"QuestionName\":\"how are you And two\",\"QuestionType\":\"\\u9009\\u62e9\\u9898\",\"Remark\":\"\",\"Solutions\":[\"yes two\",\"yes or no two\",\"no\",\"unkown\"],\"Stem\":\"more apple two\",\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"TeacherName\":\"\\u674e\\u8001\\u5e08\",\"UpdateTime\":0}]}";
        } else if (path.contains("QureyAnswerv2ByTeacherIDAndQuestionID")) {
            msg = "{\"errorCode\":0,\"errorMessage\":\"success\",\"result\":[{\"AnswerID\":\"5c1f83b01d41c8c2072ccd0e\",\"AnswerName\":\"my annswer\",\"AnswerUserID\":\"5c1f46ec1d41c8aeb4a5e537\",\"AnswerUserName\":\"student1\",\"ConsultSolution\":\"\",\"ConsultSolutionUrl\":\"\",\"CreateTime\":1545569200497,\"ErrCode\":true,\"LessonSampleID\":\"5c1f6efb1d41c8bd2db038a6\",\"LessonSampleName\":\"\\u6d4b\\u8bd5\",\"QuestionCoordinate\":null,\"QuestionID\":\"5c1f7b1e1d41c8c082e69459\",\"QuestionName\":\"how are you\",\"QuestionType\":\"\\u9009\\u62e9\\u9898\",\"Remark\":\"is is very good\",\"Score\":60,\"Solution\":[\"A\"],\"SolutionUrl\":\"i am fine url\",\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"TeacherName\":\"\\u674e\\u8001\\u5e08\",\"UpdateTime\":0}]}";
        } else if (path.contains("QureyMicroCourseBySampleID")) {
            msg = "{\"errorCode\":0,\"errorMessage\":\"success\",\"result\":[{\"ChapterName\":\"\\u7b2c\\u4e00\\u7ae0\",\"CourseName\":\"test course_name\",\"CreateTime\":1545575311323,\"LessonSampleID\":\"5c1f6efb1d41c8bd2db038a6\",\"LessonSampleName\":\"\\u6d4b\\u8bd5\",\"MicroCourseID\":\"5c1f9b8f1d41c8c91590ca76\",\"PointName\":\"\\u7b2c\\u4e00\\u77e5\\u8bc6\\u70b9\",\"Remark\":\"\",\"ResourceAddr\":\"upload/video.mp4\",\"SectionName\":\"\\u7b2c\\u4e00\\u77e5\\u8bc6\\u70b9\",\"TeacherID\":\"5c1f45a01d41c8aeabb15fff\",\"UpdateTime\":0}]}";
        } else if (path.contains("/common/Studentlogin")) {
            msg = "{\n" +
                    "  \"errorCode\": 0,\n" +
                    "  \"errorMessage\": \"success\",\n" +
                    "  \"result\": {\n" +
                    "    \"ClassID\": \"5c1f42a51d41c8ae517a380d\",\n" +
                    "    \"CreateTime\": 1545553644323,\n" +
                    "    \"GradeID\": \"5c1f3b1a1d41c8aa676d22fd\",\n" +
                    "    \"Mobile\": \"88888888\",\n" +
                    "    \"Name\": \"测试学生1\",\n" +
                    "    \"Password\": \"123\",\n" +
                    "    \"Remark\": \"\",\n" +
                    "    \"SchoolID\": \"5c1f38f71d41c8a8613bc853\",\n" +
                    "    \"Sex\": 1,\n" +
                    "    \"StudentCode\": \"11\",\n" +
                    "    \"StudentID\": \"5c1f46ec1d41c8aeb4a5e537\",\n" +
                    "    \"UpdateTime\": 0,\n" +
                    "    \"Username\": \"student1\"\n" +
                    "  }\n" +
                    "}";
        } else if (path.contains("/common/QureyQuestionByID/")) {
            msg = "{\n" +
                    "  \"errorCode\": 0,\n" +
                    "  \"errorMessage\": \"success\",\n" +
                    "  \"result\": [\n" +
                    "    {\n" +
                    "      \"ConsultSolution\": \"it is consult solution\",\n" +
                    "      \"ConsultSolutionUrl\": \"url address\",\n" +
                    "      \"CreateTime\": 1545567006184,\n" +
                    "      \"ExamType\": 1,\n" +
                    "      \"Image\": \"\",\n" +
                    "      \"LessonSampleID\": \"5c1f6efb1d41c8bd2db038a6\",\n" +
                    "      \"LessonSampleName\": \"测试\",\n" +
                    "      \"QuestionAnalysis\": \"it is annlysis\",\n" +
                    "      \"QuestionAnalysisUrl\": \"it is annlysis url\",\n" +
                    "      \"QuestionCoordinate\": null,\n" +
                    "      \"QuestionID\": \"5c1f7b1e1d41c8c082e69459\",\n" +
                    "      \"QuestionName\": \"如右图所示的阴影部分﹙包括边界﹚对应的二元一次不等式组为(   )\",\n" +
                    "      \"QuestionType\": \"单选题\",\n" +
                    "      \"Remark\": \"\",\n" +
                    "      \"QuestionOption\": [\"右上方\",\"右下方\",\"左下方\",\"左上方\"],\n" +
                    "      \"Stem\": \"more apple\",\n" +
                    "      \"TeacherID\": \"5c1f45a01d41c8aeabb15fff\",\n" +
                    "      \"TeacherName\": \"李老师\",\n" +
                    "      \"UpdateTime\": 0\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }

        return newFixedLengthResponse(msg);
    }

}
