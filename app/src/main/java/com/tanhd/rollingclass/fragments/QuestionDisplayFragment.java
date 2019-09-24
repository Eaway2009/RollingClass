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

import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.pages.AnswerListFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.UserData;

import java.io.Serializable;
import java.util.List;

public class QuestionDisplayFragment extends Fragment {

    private QuestionResourceFragment mQuestionResourceFragment;
    private AnswerListFragment mAnswerListFragment;

    private ResourceModel mResourceModel;
    private int mPageType;
    private String mLessonSampleName;
    private String mLessonSampleId;
    private View mShowAnswerButton;

    public static QuestionDisplayFragment getInstance(int typeId, ResourceModel resourceModel, String knowledgeId, String knowledgeName) {
        QuestionDisplayFragment QuestionDisplayFragment = new QuestionDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, typeId);
        args.putSerializable("resourceModel", resourceModel);

        args.putString("KnowledgeID", knowledgeId);
        args.putString("KnowledgeName", knowledgeName);
        QuestionDisplayFragment.setArguments(args);
        return QuestionDisplayFragment;
    }

    public static QuestionDisplayFragment getInstance(int typeId, ResourceModel resourceModel, String knowledgeId, String knowledgeName, String lessonSampleId, String lessonSampleName) {
        QuestionDisplayFragment QuestionDisplayFragment = new QuestionDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, typeId);
        args.putSerializable("resourceModel", resourceModel);

        args.putString("KnowledgeID", knowledgeId);
        args.putString("KnowledgeName", knowledgeName);
        args.putString("lessonSampleId", lessonSampleId);
        args.putString("lessonSampleName", lessonSampleName);
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
        mResourceModel = (ResourceModel) args.getSerializable("resourceModel");
        mPageType = args.getInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE);
        mLessonSampleName = args.getString("lessonSampleName");
        mLessonSampleId = args.getString("lessonSampleId");
    }

    private void initViews(View view) {
        mQuestionResourceFragment = QuestionResourceFragment.newInstance();
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();
        mAnswerListFragment = AnswerListFragment.getInstance(mPageType, getArguments().getString("KnowledgeID"), getArguments().getString("KnowledgeName"), mLessonSampleId, mLessonSampleName);
        getFragmentManager().beginTransaction().replace(R.id.answer_fragment, mAnswerListFragment).commit();

        mShowAnswerButton = view.findViewById(R.id.show_answers_button);
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData.isTeacher() && ExternalParam.getInstance().getStatus() == KeyConstants.ClassLearningStatus.CLASSING) {
            mShowAnswerButton.setVisibility(View.VISIBLE);
        } else {
            mShowAnswerButton.setVisibility(View.GONE);
        }
        mShowAnswerButton.setOnClickListener(onClickListener);
    }

    public void resetData(ResourceModel resourceModel) {
        if (resourceModel != null) {
            mResourceModel = resourceModel;
            initData();
        }
    }

    public void showAnswer(boolean show) {
        if (mAnswerListFragment != null) {
            mAnswerListFragment.setShowAnswer(show);
        }
    }

    public void resetData(ResourceModel resourceModel, String lessonSampleId, String lessonSampleName) {
        if (resourceModel != null) {
            mResourceModel = resourceModel;
            mLessonSampleId = lessonSampleId;
            mLessonSampleName = lessonSampleName;
            initData();
        }
    }

    private void initData() {
        List<QuestionModel> questionDataList = mResourceModel.mResourceList;
        if (questionDataList != null && questionDataList.size() > 0) {
            mQuestionResourceFragment.setListData(questionDataList);
            mAnswerListFragment.resetData("", questionDataList);
        } else {
            mQuestionResourceFragment.clearListData();
            mAnswerListFragment.clearListData();
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.show_answers_button) {
                MyMqttService.publishMessage(PushMessage.COMMAND.CLASS_BEGIN, (List<String>) null, null);
            }
        }
    };
}
