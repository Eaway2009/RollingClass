package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.AnswerCardAdapter;
import com.tanhd.rollingclass.server.data.QuestionModel;

import java.util.List;

public class AnswerListFragment extends Fragment {
    private ListView mAnswerListView;

    private List<QuestionModel> mQuestionModelList;
    private AnswerCardAdapter mAdapter;

    private Handler mHandler = new Handler();

    public static AnswerListFragment getInstance() {
        AnswerListFragment answerListFragment = new AnswerListFragment();
        Bundle args = new Bundle();
        answerListFragment.setArguments(args);
        return answerListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_answer_list, null);
        initViews(contentView);
        return contentView;
    }

    private void initViews(View contentView) {
        mAnswerListView = contentView.findViewById(R.id.listview);
        mAdapter = new AnswerCardAdapter(getActivity());
        mAnswerListView.setAdapter(mAdapter);
    }

    public void resetData(List<QuestionModel> questionModelList) {
        mQuestionModelList = questionModelList;
        if(mAdapter!=null) {
            mAdapter.setData(mQuestionModelList);
        }else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        mAdapter.setData(mQuestionModelList);
                    }
                }
            }, 500);
        }
    }

    public void clearListData() {
        if (mAdapter != null) {
            mAnswerListView.smoothScrollToPosition(0);
            mAdapter.setData(null);
            mAdapter.notifyDataSetChanged();
        }
    }
}
