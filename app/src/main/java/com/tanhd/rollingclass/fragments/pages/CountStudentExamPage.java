package com.tanhd.rollingclass.fragments.pages;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.ExamMarkFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.ShowQuestionResultFragment;
import com.tanhd.rollingclass.fragments.WrongQuestionShowFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.views.ExamPieChartView;
import com.tanhd.rollingclass.views.QuestionAnswerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CountStudentExamPage extends Fragment {
    private StudentData mStudentData;
    private ListView mListView;
    private ItemAdapter mAdapter;
    private ProgressBar mProgressBar;
    private List<QuestionData> mQuestionList;
    private HashMap<String, AnswerData> mAnswerMap;
    private View mChartView;

    public static CountStudentExamPage newInstance(StudentData studentData) {
        Bundle args = new Bundle();
        args.putSerializable("studentData", studentData);
        CountStudentExamPage page = new CountStudentExamPage();
        page.setArguments(args);
        return page;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mStudentData = (StudentData) getArguments().get("studentData");
        View view = inflater.inflate(R.layout.page_count_student_exam, container, false);
        mListView = view.findViewById(R.id.list);
        mProgressBar = view.findViewById(R.id.progressbar);
        new LoadDataTask().execute();
        return view;
    }

    private class ItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mQuestionList == null)
                return 0;

            return mQuestionList.size() + 1;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                if (mChartView == null) {
                    mChartView = getLayoutInflater().inflate(R.layout.item_count_student_exam, parent, false);
                    ExamPieChartView pieChartView = mChartView.findViewById(R.id.piechart_view);
                    pieChartView.setData(mStudentData, new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                            ArrayList<String> list = (ArrayList<String>) e.getData();
                            if (list == null)
                                return;

                            FrameDialog.fullShow(getChildFragmentManager(), ExamMarkFragment.newInstance(mStudentData.StudentID, null, list));
                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });
                }

                return mChartView;
            }

            View view = convertView;
            if (view == null || view == mChartView) {
                view = getLayoutInflater().inflate(R.layout.item_count_student_exam1, parent, false);
            }

            QuestionData questionData = mQuestionList.get(position - 1);
            AnswerData answerData = mAnswerMap.get(questionData.QuestionID);

            QuestionAnswerView questionAnswerView = view.findViewById(R.id.question);
            questionAnswerView.setData(questionData, answerData);

            return view;
        }
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
            mQuestionList = ScopeServer.getInstance().QureyQuestionByLessonSampleID(lessonSampleData.LessonSampleID);
            if (mQuestionList == null)
                return null;

            mAnswerMap = new HashMap<>();
            for (int i=0; i<mQuestionList.size(); i++) {
                QuestionData questionData = mQuestionList.get(i);
                List<AnswerData> answerDataList = ScopeServer.getInstance().QureyAnswerv2ByStudentIDAndQuestionID(mStudentData.StudentID, questionData.QuestionID);
                if (answerDataList != null && answerDataList.size() > 0) {
                    for (AnswerData answerData: answerDataList) {
                        if (answerData.QuestionSetID == null)
                            mAnswerMap.put(questionData.QuestionID, answerData);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.GONE);
            mAdapter = new ItemAdapter();
            mListView.setAdapter(mAdapter);
        }
    }
}
