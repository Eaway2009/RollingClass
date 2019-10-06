package com.tanhd.rollingclass.fragments;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;

import java.util.List;

public class AnswerCardAdapter extends BaseAdapter {
    private boolean mStudentAnswer;
    private boolean mTeacherShowAnswer;
    private boolean mAnswerCommitted;
    private Activity mContext;
    private List<QuestionModel> mDataList;

    private final String mDot;
    private final String mClosingCheron;
    private final String mOpeningCheron;
    private String mAnswer;

    public AnswerCardAdapter(Activity context, boolean answer) {
        mContext = context;
        mClosingCheron = mContext.getResources().getString(R.string.closing_chevron);
        mOpeningCheron = mContext.getResources().getString(R.string.opening_chevron);
        mDot = mContext.getResources().getString(R.string.dot);

        mStudentAnswer = answer;
    }

    public void setData(List<QuestionModel> dataList) {
        mDataList = dataList;
        notifyDataSetChanged();
    }

    public List<QuestionModel> getData() {
        return mDataList;
    }

    public void setShowAnswer(boolean teacherShowAnswer) {
        mTeacherShowAnswer = teacherShowAnswer;
        notifyDataSetChanged();
    }

    public void setAnswerCommitted(boolean answerCommitted) {
        mAnswerCommitted = answerCommitted;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mDataList == null) {
            return 0;
        }
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mDataList == null) {
            return null;
        }
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) convertView;
        if (view == null) {
            view = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.answer_card, parent, false);
        }

        QuestionModel data = (QuestionModel) getItem(position);
        if (data != null && data.context != null) {
            LinearLayout answerLayout = view.findViewById(R.id.answer_layout);
            LinearLayout answerListLayout = view.findViewById(R.id.answer_list_layout);
            answerListLayout.setTag(position);
            TextView testIndexView = view.findViewById(R.id.test_index);
            testIndexView.setText(data.context.OrderIndex + mDot);
            for (int i = 0; i < data.context.Options.size(); i++) {
                OptionData option = data.context.Options.get(i);
                TextView optionView = (TextView) answerListLayout.getChildAt(i);
                optionView.setText(mClosingCheron + AppUtils.OPTION_NO[option.OrderIndex - 1] + mOpeningCheron);
                optionView.setTag(option);
                if (option.OrderIndex == data.context.OrderIndex && (!mStudentAnswer || (mTeacherShowAnswer && mAnswerCommitted))) {
                    optionView.setTextColor(mContext.getResources().getColor(R.color.button_orange));
                } else {
                    optionView.setTextColor(mContext.getResources().getColor(R.color.lesson_text));
                }
                if (mStudentAnswer) {
                    optionView.setOnClickListener(onClickListener);
                    if (data.context.resultClass != null) {
                        if (AppUtils.OPTION_NO[option.OrderIndex - 1].equals(data.context.resultClass.text)) {
                            optionView.setTextColor(mContext.getResources().getColor(R.color.button_blue_item_checked));
                        }
                    }
                }
                optionView.setVisibility(View.VISIBLE);
            }
        }
        return view;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mAnswerCommitted){
                return;
            }
            OptionData option = (OptionData) v.getTag();
            View parentView = (View) v.getParent();
            int index = (int) parentView.getTag();
            QuestionModel question = mDataList.get(index);
            ResultClass resultClass = new ResultClass();

            resultClass.mode = 1;
            resultClass.text = AppUtils.OPTION_NO[option.OrderIndex - 1];
            mAnswer = resultClass.text;
            question.context.resultClass = resultClass;
            notifyDataSetChanged();
        }
    };

    public String getAnswer(){
        return mAnswer;
    }
}
