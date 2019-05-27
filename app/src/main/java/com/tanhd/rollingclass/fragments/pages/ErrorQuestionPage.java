package com.tanhd.rollingclass.fragments.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.QuestionAnalysisFragment;
import com.tanhd.rollingclass.fragments.ShowAnswerCommentFragment;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.views.QuestionAnswerView;
import com.tanhd.rollingclass.views.ScoreView;

public class ErrorQuestionPage extends Fragment {

    private QuestionData mQuestionData;
    private AnswerData mAnswerData;

    public static ErrorQuestionPage newInstance(QuestionData questionData, AnswerData answerData) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        args.putSerializable("answerData", answerData);
        ErrorQuestionPage page = new ErrorQuestionPage();
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mQuestionData = (QuestionData) getArguments().get("questionData");
        mAnswerData = (AnswerData) getArguments().get("answerData");
        View view = inflater.inflate(R.layout.page_error_question, container, false);
        QuestionAnswerView answerView = view.findViewById(R.id.question_answer);
        answerView.setData(mQuestionData, mAnswerData);

        ScoreView scoreView = view.findViewById(R.id.score_layout);
        scoreView.setEnabled(false);
        scoreView.setScore(String.valueOf(mAnswerData.Score));

        View analysisView = view.findViewById(R.id.btn_analysis);
        if (TextUtils.isEmpty(mQuestionData.Context.Analysis)) {
            analysisView.setVisibility(View.GONE);
        } else {
            analysisView.setVisibility(View.VISIBLE);
            analysisView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FrameDialog.show(getChildFragmentManager(), QuestionAnalysisFragment.newInstance(mQuestionData));
                }
            });
        }

        View teacherMarkView = view.findViewById(R.id.teacher_mark);
        if (TextUtils.isEmpty(mAnswerData.Remark)) {
            teacherMarkView.setVisibility(View.GONE);
        } else {
            teacherMarkView.setVisibility(View.VISIBLE);
            teacherMarkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FrameDialog.fullShow(getChildFragmentManager(), ShowAnswerCommentFragment.newInstance(mQuestionData, mAnswerData));
                }
            });
        }
        return view;
    }
}
