package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.StudentSelectorFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.fragments.resource.ResourceBaseFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassAnsweringFragment extends Fragment implements View.OnClickListener {

    private QuestionResourceFragment mQuestionResourceFragment;
    private List<QuestionModel> mQuestionList;
    private String mTeacherID;
    private String mQuestionSetID;
    private StudentSelectorFragment mStudentSelectorFragment;
    private List<StudentData> mStudentList;
    private Button mCommitButton;
    private String mKnowledgeId;

    public static ClassAnsweringFragment getInstance(String teacherID, String questionSetID) {
        ClassAnsweringFragment classAnsweringFragment = new ClassAnsweringFragment();
        Bundle args = new Bundle();
        args.putString("teacherID", teacherID);
        if (questionSetID != null)
            args.putString("questionSetID", questionSetID);
        classAnsweringFragment.setArguments(args);
        return classAnsweringFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_class_answering, null);
        initParams();
        initViews(contentView);
        return contentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mTeacherID = args.getString("teacherID");
        mQuestionSetID = args.getString("questionSetID");
    }

    private void initViews(View view) {
        mCommitButton = view.findViewById(R.id.commit_button);
        mCommitButton.setOnClickListener(this);

        mQuestionResourceFragment = QuestionResourceFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();


        getFragmentManager().beginTransaction().replace(R.id.student_select_layout_fragment, mStudentSelectorFragment).commit();
        resetData(KeyConstants.LevelType.SCHOOL_LEVEL);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commit_button:
                if(mQuestionList==null||mQuestionList.size()==0){
                    Toast.makeText(getActivity(), "请先选择题目",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mStudentList==null||mStudentList.size()==0){
                    Toast.makeText(getActivity(), "请选择需要参与测评的学生",Toast.LENGTH_SHORT).show();
                    return;
                }
                new ClassAnsweringFragment.NewSetTask(mQuestionList, mStudentList);
                break;
        }
    }
    private class NewSetTask extends AsyncTask<List, Void, String> {
        private List<StudentData> studentList;
        private List<QuestionModel> questionList;

        NewSetTask(List<QuestionModel> questionModelList, List<StudentData> studentDataList){
            studentList = studentDataList;
            questionList = questionModelList;
        }

        @Override
        protected String doInBackground(List... lists) {

            QuestionSetData questionSetData = new QuestionSetData();
            questionSetData.TeacherID = ExternalParam.getInstance().getUserData().getOwnerID();
            questionSetData.SetName = "提问";

            questionSetData.QuestionList = new ArrayList<>();
            for (int i=0; i<questionList.size(); i++) {
                QuestionModel questionData = (QuestionModel) questionList.get(i);
                questionSetData.QuestionList.add(questionData.question_id);
            }

            questionSetData.StudentList = new ArrayList<>();
            for (int i=0; i<studentList.size(); i++) {
                StudentData studentData = (StudentData) studentList.get(i);
                questionSetData.StudentList.add(studentData.StudentID);
            }
            questionSetData.knowledge_id = mKnowledgeId;
            questionSetData.class_id = mClassData.ClassID;
            String result = ScopeServer.getInstance().InsertQuestionSet(questionSetData.toJSON().toString());
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null || studentList == null || studentList.isEmpty()) {
                Toast.makeText(getContext().getApplicationContext(), "发起提问失败!", Toast.LENGTH_LONG).show();
                return;
            }

            List<String> to = new ArrayList<>();
            for (StudentData studentData: studentList) {
                to.add(studentData.StudentID);
            }
            HashMap<String, String> params = new HashMap<>();
            params.put("examID", result);
            params.put("teacherID", ExternalParam.getInstance().getUserData().getOwnerID());
            MyMqttService.publishMessage(PushMessage.COMMAND.QUESTIONING, to, params);
            ExternalParam.getInstance().setQuestionSetID(result);
//            showFragment(WaitAnswerFragment.newInstance(result));
            dismiss();
        }
    }

    private void dismiss(){
        DialogFragment dialog = (DialogFragment) getParentFragment();
        dialog.dismiss();
    }

    private void resetData() {
        new InitQuestionDataTask(mQuestionSetID).execute();
    }

    private class InitQuestionDataTask extends AsyncTask<Void, Void, List<QuestionModel>> {

        private String questionSetID;

        public InitQuestionDataTask(String questionSetID) {
            this.questionSetID = questionSetID;
        }

        @Override
        protected List<QuestionModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (!userData.isTeacher()) {
                if (questionSetID == null) {
                    List<QuestionModel> questionList = ScopeServer.getInstance().QureyQuestionSetByKnowledgeID(questionSetID);
                    return questionList;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<QuestionModel> questionDataList) {
            if (questionDataList != null && questionDataList.size() > 0) {
                mQuestionResourceFragment.setListData(questionDataList);
            } else {
                mQuestionResourceFragment.clearListData();
            }
        }
    }
}