package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.base.BaseFragment;
import com.tanhd.rollingclass.fragments.pages.AnswerListFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.utils.AutoHideKeyboard;

import java.util.List;

/**
 * 习题问答页面
 */
public class QuestionDisplayFragment extends BaseFragment {

    private QuestionResourceFragment mQuestionResourceFragment;
    private AnswerListFragment mAnswerListFragment;

    private ResourceModel mResourceModel;
    private int mPageType;
    private String mLessonSampleName;
    private String mLessonSampleId;
    private View mShowAnswerButton;
    private String mKnowledgeName;
    private String mKnowledgeId;
    private FrameLayout answer_fragment;
    private boolean isShowNo = false; //是否显示题号
    private boolean isSubmitAnswer; //是否已提交过答案
    private View mContentView;

    public static QuestionDisplayFragment getInstance(int typeId, ResourceModel resourceModel, String knowledgeId, String knowledgeName) {
        QuestionDisplayFragment QuestionDisplayFragment = new QuestionDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, typeId);
        args.putSerializable(LearnCasesActivity.PARAM_RESOURCE_MODEL, resourceModel);

        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME, knowledgeName);
        QuestionDisplayFragment.setArguments(args);
        return QuestionDisplayFragment;
    }

    public static QuestionDisplayFragment getInstance(int typeId, ResourceModel resourceModel, String knowledgeId, String knowledgeName, String lessonSampleId, String lessonSampleName,boolean isSubmitAnswer) {
        QuestionDisplayFragment QuestionDisplayFragment = new QuestionDisplayFragment();
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, typeId);
        args.putSerializable(LearnCasesActivity.PARAM_RESOURCE_MODEL, resourceModel);

        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME, knowledgeName);
        args.putString(LearnCasesActivity.PARAM_LESSON_SAMPLE_ID, lessonSampleId);
        args.putString(LearnCasesActivity.PARAM_LESSON_SAMPLE_NAME, lessonSampleName);
        args.putBoolean(LearnCasesActivity.PARAM_SHOW_NO,true);
        args.putBoolean(LearnCasesActivity.PARAM_IS_SUBMIT_ANSWER,false);
        QuestionDisplayFragment.setArguments(args);
        return QuestionDisplayFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (this.mContentView == null) {
            mContentView = inflater.inflate(R.layout.fragment_question_display, null);
            initParams();
            initViews(mContentView);
            initData();
            AutoHideKeyboard.init(getActivity());
        }
        else{
            ViewGroup parent = (ViewGroup) this.mContentView.getParent();
            if (parent != null) {
                parent.removeView(this.mContentView);
            }
        }
        return mContentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mResourceModel = (ResourceModel) args.getSerializable(LearnCasesActivity.PARAM_RESOURCE_MODEL);
        mPageType = args.getInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE);
        mLessonSampleName = args.getString(LearnCasesActivity.PARAM_LESSON_SAMPLE_NAME);
        mLessonSampleId = args.getString(LearnCasesActivity.PARAM_LESSON_SAMPLE_ID);
        mKnowledgeId = args.getString(LearnCasesActivity.PARAM_KNOWLEDGE_ID);
        mKnowledgeName = args.getString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME);
        isShowNo = args.getBoolean(LearnCasesActivity.PARAM_SHOW_NO,false);
        isSubmitAnswer = args.getBoolean(LearnCasesActivity.PARAM_IS_SUBMIT_ANSWER,false);
    }

    private void initViews(View view) {
        answer_fragment = view.findViewById(R.id.answer_fragment);

        List<QuestionModel> questionDataList = mResourceModel.mResourceList;
        mQuestionResourceFragment = QuestionResourceFragment.newInstance(questionDataList,isShowNo);
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();
        mAnswerListFragment = AnswerListFragment.getInstance(mPageType, mKnowledgeId, mKnowledgeName, mLessonSampleId, mLessonSampleName,isSubmitAnswer);
        getFragmentManager().beginTransaction().replace(R.id.answer_fragment, mAnswerListFragment).commit();

    }

    public void resetData(ResourceModel resourceModel) {
        if (resourceModel != null) {
            mResourceModel = resourceModel;
            Log.d("AnswerCardAdapter", "mResourceModel: "+mResourceModel.mResourceList.size());
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
            mAnswerListFragment.resetData("", mLessonSampleId, questionDataList);
        } else {
            mQuestionResourceFragment.clearListData();
            mAnswerListFragment.clearListData();
        }
    }
}
