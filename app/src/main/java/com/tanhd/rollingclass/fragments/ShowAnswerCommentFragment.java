package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.ScoreView;

public class ShowAnswerCommentFragment extends Fragment {
    private QuestionData mQuestionData;
    private AnswerData mAnswerData;
    private TextView mAnswerTextView;
    private String mScore = "0";

    public static ShowAnswerCommentFragment newInstance(QuestionData questionData, AnswerData answerData) {
        Bundle args = new Bundle();
        args.putSerializable("questionData", questionData);
        args.putSerializable("answerData", answerData);
        ShowAnswerCommentFragment fragment = new ShowAnswerCommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mQuestionData = (QuestionData) getArguments().get("questionData");
        mAnswerData = (AnswerData) getArguments().get("answerData");
        View view = inflater.inflate(R.layout.fragment_show_answer_comment, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        WebView mStemView = view.findViewById(R.id.question_stem);
        mAnswerTextView = view.findViewById(R.id.answer_text);
        ImageView imageView = view.findViewById(R.id.answer_image);
        View textLayout = view.findViewById(R.id.text_layout);
        ScoreView scoreView = view.findViewById(R.id.score_layout);
        ImageView commentImageView = view.findViewById(R.id.comment_image);
        TextView commentTextView = view.findViewById(R.id.comment_text);
        scoreView.setEnabled(false);
        scoreView.setScore(mAnswerData.Score + "");

        String html = AppUtils.dealHtmlText(mQuestionData.Context.Stem);
        mStemView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

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

        if (mAnswerData != null) {
            if (TextUtils.isEmpty(mAnswerData.AnswerUrl)) {
                textLayout.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                mAnswerTextView.setText(mAnswerData.AnswerText);
            } else {
                textLayout.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                Glide.with(getContext()).load(ScopeServer.RESOURCE_URL + mAnswerData.AnswerUrl).into(imageView);
            }
        }

        if (TextUtils.isEmpty(mAnswerData.Remark))
            return;

        if (mAnswerData.Remark.startsWith("/Resources/")) {
            commentTextView.setVisibility(View.GONE);
            commentImageView.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(ScopeServer.RESOURCE_URL + mAnswerData.Remark).into(commentImageView);
        } else {
            commentTextView.setVisibility(View.VISIBLE);
            commentImageView.setVisibility(View.GONE);
            commentTextView.setText(mAnswerData.Remark);
        }
    }
}
