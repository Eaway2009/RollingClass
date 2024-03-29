package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.QuerstionTypeShow;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.CountClassLessonSampleData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.utils.MyValueFormatter;
import com.tanhd.rollingclass.views.BarChartView;
import com.tanhd.rollingclass.views.QuestionView;

import java.util.ArrayList;
import java.util.List;

public class CountClassLessonSamplePage extends Fragment {
    private BarChartView mBarChartView;
    private ListView mListView;
    private View mLayoutItemView0;

    private List<QuestionModel> mQuestionList;
    private List<CountClassLessonSampleData> mCountDataList;
    private QuestionAdapter mAdapter;

    private ArrayList<BarEntry> yVals;
    private List<String> xAxisValue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_count_class_lessonsample, container, false);
        init(view);
        return view;
    }

    public void init(View view) {
        mListView = view.findViewById(R.id.list);
        new LoadDataTask().execute();
    }

    private class QuestionAdapter extends BaseAdapter {

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
                if (mLayoutItemView0 == null) {
                    mLayoutItemView0 = getLayoutInflater().inflate(R.layout.item_count_class_lessonsample0, parent, false);

                    mBarChartView = mLayoutItemView0.findViewById(R.id.chartView);
                    mBarChartView.setData(null, new String[]{getResources().getString(R.string.lbl_exactness), getResources().getString(R.string.lbl_err), getResources().getString(R.string.lbl_un_submit)}, yVals, new MyValueFormatter(getResources().getString(R.string.lbl_di), getResources().getString(R.string.lbl_topic)), new MyValueFormatter("", getResources().getString(R.string.lbl_people)), getResources().getString(R.string.lbl_people));
                    mBarChartView.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                        @Override
                        public void onValueSelected(Entry e, Highlight h) {
                            String questionID = (String) e.getData();
                            int stackIndex = h.getStackIndex();
                            QuerstionTypeShow.TYPE type = QuerstionTypeShow.TYPE.CORRECT;
                            if (stackIndex == 1)
                                type = QuerstionTypeShow.TYPE.ERROR;
                            else if (stackIndex == 2)
                                type = QuerstionTypeShow.TYPE.NOANSWER;

                            QuerstionTypeShow fragment = QuerstionTypeShow.newInstance(questionID, ExternalParam.getInstance().getUserData().getOwnerID(), type);
                            FrameDialog.show(getChildFragmentManager(), fragment);
                        }

                        @Override
                        public void onNothingSelected() {

                        }
                    });
                }

                return mLayoutItemView0;
            }

            View view = convertView;
            if (view == null || view == mLayoutItemView0) {
                view = getLayoutInflater().inflate(R.layout.item_count_class_lessonsample, parent, false);
            }

            QuestionModel questionData = mQuestionList.get(position - 1);
            QuestionView questionView = view.findViewById(R.id.question_view);
            questionView.setData(questionData);

            TextView descriptionView = view.findViewById(R.id.description);

            CountClassLessonSampleData data = queryCountData(questionData.question_id);
            if (data != null) {
                String text = String.format(getResources().getString(R.string.lbl_class_lesson_tj), data.CorrectTotal, data.ErrorTotal, data.NoAnswerTotal);
                descriptionView.setText(text);
            } else {
                descriptionView.setText(null);
            }

            return view;
        }
    }

    private CountClassLessonSampleData queryCountData(String questionID) {
        if (mCountDataList == null)
            return null;

        for (CountClassLessonSampleData data: mCountDataList) {
            if (data.QuestionID.equals(questionID))
                return data;
        }

        return null;
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List> {

        private QuestionModel queryQuestionByID(String questionID) {
            if (mQuestionList == null)
                return null;

            for (QuestionModel questionData: mQuestionList) {
                if (questionData.question_id.equals(questionID))
                    return questionData;
            }

            return null;
        }

        @Override
        protected List doInBackground(Void... voids) {
            ClassData classData = ExternalParam.getInstance().getClassData();
            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
            mCountDataList = ScopeServer.getInstance().CountClassLessonSample(classData.ClassID, lessonSampleData.LessonSampleID);
            mQuestionList = ScopeServer.getInstance().QureyQuestionByLessonSampleID(lessonSampleData.LessonSampleID);

            if (mCountDataList != null) {
                yVals = new ArrayList<>();
                xAxisValue = new ArrayList<>();
                for (int i=0; i<mCountDataList.size(); i++) {
                    CountClassLessonSampleData data = mCountDataList.get(i);
                    QuestionModel questionData = queryQuestionByID(data.QuestionID);
                    if (questionData == null)
                        continue;

                    BarEntry entry = new BarEntry(questionData.context.OrderIndex, new float[]{data.CorrectTotal, data.ErrorTotal, data.NoAnswerTotal}, questionData.context.OrderIndex + "");
                    yVals.add(entry);
                    entry.setData(questionData.question_id);
                    xAxisValue.add(String.format(getResources().getString(R.string.lbl_question_no), questionData.context.OrderIndex));
                }
            }


            return mCountDataList;
        }

        @Override
        protected void onPostExecute(List list) {
            mAdapter = new QuestionAdapter();
            mListView.setAdapter(mAdapter);
        }
    }
}
