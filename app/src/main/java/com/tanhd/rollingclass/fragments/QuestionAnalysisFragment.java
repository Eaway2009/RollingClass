package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.utils.AppUtils;

public class QuestionAnalysisFragment extends Fragment {
    private QuestionData mQuestionData;

    public static QuestionAnalysisFragment newInstance(QuestionData questionData) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        QuestionAnalysisFragment fragment = new QuestionAnalysisFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mQuestionData = (QuestionData) getArguments().get("questionData");
        View view = inflater.inflate(R.layout.fragment_question_analysis, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        WebView stemView = view.findViewById(R.id.question_stem);

        //题目
        String html = AppUtils.dealHtmlText(mQuestionData.Context.Stem);
        stemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        //选择题选项
        LinearLayout optionsLayout = view.findViewById(R.id.options);
        if (mQuestionData.Context.QuestionCategoryId == 1) {
            optionsLayout.setVisibility(View.VISIBLE);
            optionsLayout.removeAllViews();

            for (OptionData optionData: mQuestionData.Context.Options) {
                View optionView = getLayoutInflater().inflate(R.layout.layout_question_option, optionsLayout, false);
                TextView noView = optionView.findViewById(R.id.no);
                WebView textView = optionView.findViewById(R.id.option_text);

                noView.setText(AppUtils.OPTION_NO[optionData.OrderIndex-1] + ".");
                html = AppUtils.dealHtmlText(optionData.OptionText);
                textView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                optionsLayout.addView(optionView);
            }
        } else {
            optionsLayout.setVisibility(View.GONE);
        }

        //问题分析
        WebView analysisView = view.findViewById(R.id.analysis);
        html = AppUtils.dealHtmlText(mQuestionData.Context.Analysis);
        analysisView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
