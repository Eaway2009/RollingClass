package com.tanhd.rollingclass.fragments;

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

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.pages.AnswerListFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.List;

public class QuestionDisplayFragment extends Fragment{

    private QuestionResourceFragment mQuestionResourceFragment;
    private AnswerListFragment mAnswerListFragment;

    private String mQuestionSetID;

    public static QuestionDisplayFragment getInstance(String questionSetID) {
        QuestionDisplayFragment QuestionDisplayFragment = new QuestionDisplayFragment();
        Bundle args = new Bundle();
        if (questionSetID != null)
            args.putString("questionSetID", questionSetID);
        QuestionDisplayFragment.setArguments(args);
        return QuestionDisplayFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_question_display, null);
        initParams();
        initViews(contentView);
        initData();
        return contentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mQuestionSetID = args.getString("questionSetID");
    }

    private void initViews(View view) {


        mQuestionResourceFragment = QuestionResourceFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();
        mAnswerListFragment = AnswerListFragment.getInstance();
        getFragmentManager().beginTransaction().replace(R.id.student_select_layout_fragment, mAnswerListFragment).commit();
    }

    private void initData() {
        new QuestionDisplayFragment.InitQuestionDataTask(mQuestionSetID).execute();
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
                mAnswerListFragment.resetData(questionDataList);
            } else {
                mQuestionResourceFragment.clearListData();
                mAnswerListFragment.clearListData();
            }
        }
    }
}
