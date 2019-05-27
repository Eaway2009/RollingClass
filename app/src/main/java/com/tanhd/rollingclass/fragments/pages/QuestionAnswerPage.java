package com.tanhd.rollingclass.fragments.pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;
import com.tanhd.rollingclass.views.ObjectiveAnswerView;
import com.tanhd.rollingclass.views.QuestionView;
import com.tanhd.rollingclass.views.SubjectiveAnswerView;

import java.util.ArrayList;

public class QuestionAnswerPage extends Fragment {
    public static interface QuestionAnswerListener {
        void onFinished(QuestionAnswerPage page);
    }

    private QuestionData mQuestionData;
    private ResultClass mResultClass = new ResultClass();
    private ObjectiveAnswerView mObjectiveView;
    private SubjectiveAnswerView mSubjectiveView;
    private QuestionAnswerListener mListener;
    private View mRootView;

    public static QuestionAnswerPage newInstance(QuestionData questionData, ResultClass resultClass) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        args.putSerializable("resultClass", resultClass);
        QuestionAnswerPage page = new QuestionAnswerPage();
        page.setArguments(args);
        return page;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mQuestionData = (QuestionData) getArguments().get("questionData");
        mResultClass = (ResultClass) getArguments().get("resultClass");
        mRootView = inflater.inflate(R.layout.page_question_answer, container, false);
        init();
        return mRootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mObjectiveView.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public ResultClass getData() {
        if (mSubjectiveView == null)
            return null;

        if (mQuestionData.isChoiceType()) {
            mResultClass.mode = 1;
            mResultClass.text = mSubjectiveView.getResult();
        } else {
            mResultClass.mode = mObjectiveView.getMode();
            switch (mResultClass.mode) {
                case 0:
                    Size size = mObjectiveView.getSmartPenSize();
                    mResultClass.smartPenViewWidth = size.getWidth();
                    mResultClass.smartPenViewHeight = size.getHeight();
                    mResultClass.smartPenViewData = mObjectiveView.getSmartPenData();
                    break;
                case 1:
                    mResultClass.text = mObjectiveView.getEditText();
                    break;
                case 2:
                    mResultClass.imagePath = mObjectiveView.getImagePath();
                    break;
            }
        }

        return mResultClass;
    }

    public void setListener(QuestionAnswerListener listener) {
        mListener = listener;
    }

    public void init() {
        if (mRootView == null)
            return;
        View view = mRootView;

        QuestionView questionView = view.findViewById(R.id.question_title);
        questionView.setData(mQuestionData);

        mObjectiveView = view.findViewById(R.id.objective_view);
        mObjectiveView.setListener(new ObjectiveAnswerView.ResultChangeListener() {
            @Override
            public void onResultChanged() {
                getData();
            }
        });
        mSubjectiveView = view.findViewById(R.id.subjective_view);
        mSubjectiveView.setListener(new SubjectiveAnswerView.AnswerListener() {
            @Override
            public void onNext() {
                getData();
                if (mListener != null)
                    mListener.onFinished(QuestionAnswerPage.this);
            }
        });

        if (mQuestionData.isChoiceType()) {
            mResultClass.mode = 1;
            mObjectiveView.setVisibility(View.GONE);
            mSubjectiveView.setVisibility(View.VISIBLE);

            ArrayList<String> options = new ArrayList<>();
            for (int i=0; i<mQuestionData.Context.Options.size(); i++)
                options.add(AppUtils.OPTION_NO[i]);

            mSubjectiveView.setData(true, options, mResultClass.text);
        } else {
            mObjectiveView.setVisibility(View.VISIBLE);
            mSubjectiveView.setVisibility(View.GONE);

            mObjectiveView.setFragment(this);
            switch (mResultClass.mode) {
                case 1:
                    mObjectiveView.initData(1, mResultClass.text, null);
                    break;
                case 0:
                    Size size = new Size(mResultClass.smartPenViewWidth, mResultClass.smartPenViewHeight);
                    mObjectiveView.initData(0, mResultClass.smartPenViewData, size);
                    break;
                case 2:
                    mObjectiveView.initData(2, mResultClass.imagePath, null);
                    break;
            }
        }

    }

    public void active() {
        if (mObjectiveView != null)
            mObjectiveView.active();
    }
}
