package com.tanhd.rollingclass.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.OptionData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.AppUtils;

import java.util.List;

public class AnswerCardAdapter extends BaseAdapter {
    private Activity mContext;
    private List<QuestionModel> mDataList;

    private final String mDot;
    private final String mClosingCheron;
    private final String mOpeningCheron;

    public AnswerCardAdapter(Activity context) {
        mContext = context;
        mClosingCheron = mContext.getResources().getString(R.string.closing_chevron);
        mOpeningCheron = mContext.getResources().getString(R.string.opening_chevron);
        mDot = mContext.getResources().getString(R.string.dot);
    }

    public void setData(List<QuestionModel> dataList) {
        mDataList = dataList;
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
            TextView testIndexView = view.findViewById(R.id.test_index);
            testIndexView.setText(data.context.OrderIndex + mDot);
            for (OptionData option : data.context.Options) {
                TextView optionView = (TextView) mContext.getLayoutInflater().inflate(R.layout.answer_textview, null);
                optionView.setText(mClosingCheron + AppUtils.OPTION_NO[option.OrderIndex-1] + mOpeningCheron);
                optionView.setTag(option);
                if(option.OrderIndex ==data.context.OrderIndex){
                    optionView.setTextColor(mContext.getResources().getColor(R.color.button_orange));
                }
                answerLayout.addView(optionView);
            }
        }
        return view;
    }
}
