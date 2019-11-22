package com.tanhd.rollingclass.fragments;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ContextData;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ResultClass;

import java.util.List;

/**
 * 答题卡适配器
 */
public class AnswerCardAdapter extends BaseAdapter {
    private static final String TAG = "AnswerCardAdapter";
    private int mPageType;
    private boolean mTeacherShowAnswer;
    private boolean mAnswerCommitted;
    private Activity mContext;
    private List<QuestionModel> mDataList;
    private List<AnswerData> mAnswerDataList;

    private final String mDot;
    private final String mClosingCheron;
    private final String mOpeningCheron;
    private String mAnswer;

    public AnswerCardAdapter(Activity context, int pageType) {
        mContext = context;
        mClosingCheron = mContext.getResources().getString(R.string.closing_chevron);
        mOpeningCheron = mContext.getResources().getString(R.string.opening_chevron);
        mDot = mContext.getResources().getString(R.string.dot);

        mPageType = pageType;
    }

    public void setData(List<QuestionModel> dataList) {
        if(mDataList!=null) {
            Log.d(TAG, "setData: " + mDataList.size());
        }
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

    public void setAnswers(List<AnswerData> answerDataList) {
        mAnswerDataList = answerDataList;
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
            testIndexView.setText((position + 1) + mDot);

            ContextData contextData = data.context;
            for (int i = 0; i < contextData.Options.size(); i++) {
                OptionData option = contextData.Options.get(i);
                TextView optionView = (TextView) answerListLayout.getChildAt(i);
                optionView.setText(mClosingCheron + AppUtils.OPTION_NO[option.OrderIndex - 1] + mOpeningCheron);
                optionView.setTag(option);

                //公布正确答案
                if (option.OrderIndex == AppUtils.getAnswerIndex(contextData.Answer) + 1 && (mPageType == KeyConstants.ClassPageType.TEACHER_CLASS_PAGE || (mPageType == KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE&&mAnswerCommitted) || (mTeacherShowAnswer && mAnswerCommitted))) {
                    optionView.setTextColor(mContext.getResources().getColor(R.color.button_orange));
                } else {
                    optionView.setTextColor(mContext.getResources().getColor(R.color.lesson_text));
                }
                if (mPageType != KeyConstants.ClassPageType.TEACHER_CLASS_PAGE) {
                    optionView.setOnClickListener(onClickListener);
                    if(mAnswerCommitted) {
                        if (contextData.resultClass == null || TextUtils.isEmpty(contextData.resultClass.text)) {
                            data = getAnswerText(data);
                            mDataList.set(position, data);
                        }
                    }
                    if (contextData.resultClass != null) {
                        if (AppUtils.OPTION_NO[option.OrderIndex - 1].equals(contextData.resultClass.text)) {
                            optionView.setTextColor(mContext.getResources().getColor(R.color.button_blue_item_checked));
                        }
                    }
                }
                optionView.setVisibility(View.VISIBLE);
            }
        }
        return view;
    }

    private QuestionModel getAnswerText(QuestionModel questionModel){
        if(mAnswerDataList!=null){
            for (AnswerData answerData:mAnswerDataList){
                if(questionModel.question_id.equals(answerData.QuestionID)){
                    if (questionModel.context.resultClass == null) {
                        questionModel.context.resultClass = new ResultClass();
                    }
                    questionModel.context.resultClass.text = answerData.AnswerText;
                    return questionModel;
                }
            }
        }
        return questionModel;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Log.d(TAG, "onClick: "+mAnswerCommitted);
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
