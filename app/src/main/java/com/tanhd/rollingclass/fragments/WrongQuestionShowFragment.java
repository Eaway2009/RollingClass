package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.pages.ErrorQuestionPage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.CountClassLessonSampleData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WrongQuestionShowFragment extends Fragment {
    private ProgressBar mProgressBar;
    private TextView mTypeNameView;
    private TextView mPositionView;
    private TextView mTotalView;
    private ViewPager mViewPager;
    private ErrorPageAdapter mAdapter;
    private List<QuestionModel> mQuestionList;
    private String mStudentID;
    private HashMap<String, AnswerData> mAnswerMap = new HashMap<>();
    private HashMap<Integer, Fragment> mPageMap = new HashMap<>();
    private List<CountClassLessonSampleData> mCountDataList;

    public static WrongQuestionShowFragment newInstance(String studentID) {
        Bundle args = new Bundle();
        args.putString("studentID", studentID);
        WrongQuestionShowFragment fragment = new WrongQuestionShowFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mStudentID = getArguments().getString("studentID");
        View view = inflater.inflate(R.layout.fragment_wrong_question_show, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        mProgressBar = view.findViewById(R.id.progressbar);
        mTypeNameView = view.findViewById(R.id.type_name);
        mPositionView = view.findViewById(R.id.tv_sequence);
        mTotalView = view.findViewById(R.id.tv_total_count);
        mViewPager = view.findViewById(R.id.vp);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                refreshPage();
            }
        });

        new LoadDataTask().execute();
    }

    private void refreshPage() {
        if (mQuestionList == null || mQuestionList.isEmpty())
            return;

        int currentPage = mViewPager.getCurrentItem();
        mPositionView.setVisibility(View.VISIBLE);
        mTotalView.setVisibility(View.VISIBLE);
        QuestionModel questionData = mQuestionList.get(currentPage);
        mTypeNameView.setText(String.format("[%s]", questionData.context.QuestionCategoryName));
        mPositionView.setText(String.valueOf(currentPage + 1));
        mTotalView.setText("/" + String.valueOf(mQuestionList.size()));
    }

    private class ErrorPageAdapter extends FragmentStatePagerAdapter {

        public ErrorPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment page = mPageMap.get(i);
            return page;
        }

        @Override
        public int getCount() {
            return mPageMap.size();
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Integer ret) {
            mProgressBar.setVisibility(View.GONE);
            if (ret != 0) {
                ToastUtil.show(R.string.toast_wrong_recod_empty);
                DialogFragment dialog = (DialogFragment) getParentFragment();
                dialog.dismiss();
                return;
            }

            mAdapter = new ErrorPageAdapter(getChildFragmentManager());
            mViewPager.setAdapter(mAdapter);
            refreshPage();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();

            ClassData classData = ExternalParam.getInstance().getClassData();
            mCountDataList = ScopeServer.getInstance().CountClassLessonSample(classData.ClassID, lessonSampleData.LessonSampleID);
            List<AnswerData> answerList = ScopeServer.getInstance().QureyErrorAnswerv2ByStudentID(mStudentID);
            if (answerList == null)
                return -1;

            mQuestionList = new ArrayList<>();
            for (AnswerData answerData: answerList) {
                if (!answerData.LessonSampleID.equals(lessonSampleData.LessonSampleID))
                    continue;

                List<QuestionModel> list = ScopeServer.getInstance().QureyQuestionByID(answerData.QuestionID);
                if (list == null || list.isEmpty())
                    continue;
                QuestionModel questionData = list.get(0);
                mQuestionList.add(questionData);
                mAnswerMap.put(answerData.QuestionID, answerData);
            }

            for (int i=0; i<mQuestionList.size(); i++) {
                QuestionModel questionData = mQuestionList.get(i);
                mPageMap.put(i, ErrorQuestionPage.newInstance(questionData, mAnswerMap.get(questionData.question_id), queryCountData(questionData.question_id)));
            }

            if (mQuestionList.isEmpty())
                return -2;

            return 0;
        }
    }



    private String queryCountData(String questionID) {
        if (mCountDataList == null)
            return null;

        for (CountClassLessonSampleData data: mCountDataList) {
            if (data.QuestionID.equals(questionID)) {
                String text = String.format("【%d人正确, %d人错误, %d人未提交】", data.CorrectTotal, data.ErrorTotal, data.NoAnswerTotal);
                return text;
            }
        }

        return null;
    }
}
