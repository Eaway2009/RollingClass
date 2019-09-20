package com.tanhd.rollingclass.fragments.kowledge;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class AnswerDisplayLayout extends LinearLayout {
    private List<QuestionModel> mQuestionModelList;
    private LinearLayout mDisplayLayout;
    private String mClosingCheron;
    private String mOpeningCheron;
    private String mDot;

    public AnswerDisplayLayout(Context context) {
        super(context);
    }

    public AnswerDisplayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public AnswerDisplayLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnswerDisplayLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs);
    }

    void initView(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        inflate(this.getContext(), R.layout.fragment_answers_display, this);
        initParams();
        initViews();
    }

    private void initParams() {
        mClosingCheron = getResources().getString(R.string.closing_chevron);
        mOpeningCheron = getResources().getString(R.string.opening_chevron);
        mDot = getResources().getString(R.string.dot);
    }

    private void initViews() {
        mDisplayLayout = findViewById(R.id.display_layout);
    }

    public void resetData(List<QuestionModel> questionModelList) {
        mQuestionModelList = questionModelList;
        if (mQuestionModelList != null && mQuestionModelList.size() > 0) {
            for (QuestionModel questionModel : questionModelList) {
                addModelView(questionModel);
            }
        }
    }

    public void addData(QuestionModel questionModel) {
        if (mQuestionModelList == null) {
            mQuestionModelList = new ArrayList<>();
        }
        if (questionModel != null) {
            mQuestionModelList.add(questionModel);
        }
        addModelView(questionModel);
    }

    private void addModelView(QuestionModel data) {
        View view =  LayoutInflater.from(getContext()).inflate(R.layout.answer_card, null);
        if (data != null && data.context != null) {
            LinearLayout answerLayout = view.findViewById(R.id.answer_layout);
            TextView testIndexView = view.findViewById(R.id.test_index);
            testIndexView.setText(data.context.OrderIndex + mDot);
            for (OptionData option : data.context.Options) {
                TextView optionView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.answer_textview, null);
                optionView.setText(mClosingCheron + AppUtils.OPTION_NO[option.OrderIndex - 1] + mOpeningCheron);
                if(option.OrderIndex ==data.context.OrderIndex){
                    optionView.setTextColor(getResources().getColor(R.color.button_orange));
                }
                optionView.setTag(option);
                answerLayout.addView(optionView);
            }
        }
        mDisplayLayout.addView(view);
    }
}
