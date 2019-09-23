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
    private AnswerListFragment mAnswerListFragment;

    private String mTeacherID;
    private String mQuestionSetID;
    private Button mCommitButton;
    private ExamListener mListener;
    private String mKnowledgeID;
    private String mKnowledgeName;
    private int mPageType;

    public static ClassAnsweringFragment getInstance(int pageType, String KnowledgeID, String KnowledgeName, String teacherID, String questionSetID, ExamListener examListener) {
        ClassAnsweringFragment classAnsweringFragment = new ClassAnsweringFragment();
        Bundle args = new Bundle();
        args.putInt("pageType", pageType);
        args.putString("teacherID", teacherID);
        args.putString("KnowledgeName", KnowledgeName);
        args.putString("KnowledgeID", KnowledgeID);
        if (questionSetID != null)
            args.putString("questionSetID", questionSetID);
        classAnsweringFragment.setArguments(args);
        classAnsweringFragment.setListener(examListener);
        return classAnsweringFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_class_answering, null);
        initParams();
        initViews(contentView);
        initData();
        return contentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mPageType = args.getInt("pageType");
        mTeacherID = args.getString("teacherID");
        mQuestionSetID = args.getString("questionSetID");
        mKnowledgeID = args.getString("KnowledgeID");
        mKnowledgeName = args.getString("KnowledgeName");
    }

    private void initViews(View view) {
        mCommitButton = view.findViewById(R.id.commit_button);
        mCommitButton.setOnClickListener(this);

        mQuestionResourceFragment = QuestionResourceFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();
        mAnswerListFragment = AnswerListFragment.getInstance(mPageType, mKnowledgeID, mKnowledgeName);
        getFragmentManager().beginTransaction().replace(R.id.answer_fragment, mAnswerListFragment).commit();
    }

    private void initData() {
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
                List<QuestionModel> questionList = ScopeServer.getInstance().QureyQuestionSetByKnowledgeID(questionSetID);
                return questionList;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<QuestionModel> questionDataList) {
            if (questionDataList != null && questionDataList.size() > 0) {
                mQuestionResourceFragment.setListData(questionDataList);
                mAnswerListFragment.resetData(questionSetID, questionDataList);
            } else {
                mQuestionResourceFragment.clearListData();
                mAnswerListFragment.clearListData();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commit_button:
                if (mListener != null) {
                    mListener.onFinished();
                }
                dismiss();
                break;
        }
    }

    private void dismiss() {
        DialogFragment dialog = (DialogFragment) getParentFragment();
        dialog.dismiss();
    }

    public void setListener(ExamListener examListener) {
        mListener = examListener;
    }

    public static interface ExamListener {
        void onFinished();
    }
}