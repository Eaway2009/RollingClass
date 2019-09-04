package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class StudentFragment extends Fragment implements View.OnClickListener {
    private BackListener mListener;
    private View mClassPageView;
    private View mKnowledgePageView;
    private View mStaticsPageView;

    public static StudentFragment newInstance(BackListener listener) {
        StudentFragment fragment = new StudentFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(BackListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_student, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view){
        mClassPageView = view.findViewById(R.id.class_page_view);
        mKnowledgePageView = view.findViewById(R.id.knowledge_page_view);
        mStaticsPageView = view.findViewById(R.id.statics_page_view);

        mClassPageView.setOnClickListener(this);
        mKnowledgePageView.setOnClickListener(this);
        mStaticsPageView.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MQTT.register(mqttListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.unregister(mqttListener);
    }

    private void notifyEnterClass(String studentID) {
        ClassData classData = ExternalParam.getInstance().getClassData();

        //通知学生端打开学案
        TeacherData teacherData = (TeacherData) ExternalParam.getInstance().getUserData().getUserData();
        KnowledgeData knowledgeData = ExternalParam.getInstance().getKnowledge();
        LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
        HashMap<String, String> params = new HashMap<>();
        params.put("EnterClass", "1");
        params.put("ClassName", classData.ClassName);
        params.put("SubjectName", AppUtils.getSubjectNameByCode(teacherData.SubjectCode));
        params.put("TeacherName", teacherData.Username);
        params.put("KnowledgePointName", knowledgeData.KnowledgePointName);
        params.put("LessonSampleName", lessonSampleData.LessonSampleName);
        params.put("UrlContent", lessonSampleData.UrlContent);
        MQTT.publishMessage(PushMessage.COMMAND.CLASS_BEGIN, studentID, params);
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case OFFLINE:
                case ONLINE:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    ClassData classData = ExternalParam.getInstance().getClassData();
                    if (classData == null)
                        return;

                    classData.setStudentState(message.from, (message.command == PushMessage.COMMAND.ONLINE ? 1 : 0));
                    break;
                case QUERY_CLASS:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    notifyEnterClass(message.from);
                    break;
                case ANSWER_COMPLETED:
                    String content = message.parameters.get("content");
                    try {
                        JSONObject json = new JSONObject(content);
                        String examID = json.optString("examID");
                        Database.getInstance().setQuestioning(examID);
                        FrameDialog.show(getChildFragmentManager(), WaitAnswerFragment.newInstance(examID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.class_page_view:
//                DatasActivity.startMe(getActivity(), DatasActivity.PAGE_ID_DOCUMENTS, true);
                break;
            case R.id.knowledge_page_view:
                DatasActivity.startMe(getActivity(), DatasActivity.PAGE_ID_DOCUMENTS, true);
                break;
            case R.id.statics_page_view:
                DatasActivity.startMe(getActivity(), DatasActivity.PAGE_ID_STATISTICS, true);
                break;
        }
    }

    public interface BackListener {
        void showBack(boolean show);
    }
}
