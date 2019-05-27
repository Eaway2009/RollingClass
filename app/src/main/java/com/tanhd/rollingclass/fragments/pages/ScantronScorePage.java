package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.QuestionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScantronScorePage extends Fragment {
    public static interface ScantronScoreListener {
        void onScrollToPage(int page);
        void onDismiss();
    }

    private GridView mGridView;
    private ItemAdapter mAdapter;
    private ScantronScoreListener mListener;

    private HashMap<String, AnswerData> mAnswerMap;
    private List<QuestionData> mQuestionList;

    public static ScantronScorePage newInstance(ArrayList<QuestionData> questionDataList, HashMap<String, AnswerData> answerDataHashMap, ScantronScoreListener listener) {
        Bundle args = new Bundle();
        args.putSerializable("questionDataList", questionDataList);
        args.putSerializable("answerDataHashMap", answerDataHashMap);
        ScantronScorePage page = new ScantronScorePage();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(ScantronScoreListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAnswerMap = (HashMap<String, AnswerData>) getArguments().get("answerDataHashMap");
        mQuestionList = (List<QuestionData>) getArguments().get("questionDataList");
        View view = inflater.inflate(R.layout.page_scantron_score, container, false);
        mGridView = view.findViewById(R.id.grid_view);
        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onDismiss();
            }
        });
        mAdapter = new ItemAdapter();
        mGridView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.notifyDataSetChanged();
    }

    private class ItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mQuestionList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_scantron_score, null);
            }

            QuestionData questionData = mQuestionList.get(position);
            AnswerData answerData = mAnswerMap.get(questionData.QuestionID);

            final String title = String.format("第%d题", questionData.Context.OrderIndex);
            TextView titleView = view.findViewById(R.id.title);
            titleView.setText(title);

            if (answerData != null) {
                TextView scoreView = view.findViewById(R.id.score_view);
                scoreView.setText(answerData.Score + "分");
                scoreView.setTextColor(getResources().getColor(R.color.white));

                if (answerData.Score == 5) {
                    scoreView.setBackgroundResource(R.drawable.circle_green_background);
                } else if (answerData.Score == 0){
                    scoreView.setBackgroundResource(R.drawable.circle_red_background);
                } else {
                    scoreView.setBackgroundResource(R.drawable.circle_yellow_background);
                }
            } else {
                TextView scoreView = view.findViewById(R.id.score_view);
                scoreView.setText("0分");
                scoreView.setTextColor(getResources().getColor(R.color.white));
                scoreView.setBackgroundResource(R.drawable.circle_red_background);
            }


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onScrollToPage(position);
                }
            });

            return view;
        }
    }
}
