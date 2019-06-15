package com.tanhd.rollingclass.views;

import android.content.Context;
import android.os.AsyncTask;

import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.CountMicroCourseStudentData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.utils.MyValueFormatter;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MicroCourseBarChartView extends LinearLayout {
    private TextView mTitleView;
    private TextView mStaticsResultView;
    private MicroCourseData mCourseData;
    private StudentData mStudentData;
    private int mLargestValue;
    private String mTime;

    public MicroCourseBarChartView(Context context) {
        super(context);
    }

    public MicroCourseBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MicroCourseBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTitleView = findViewById(R.id.chart_title);
        mStaticsResultView = findViewById(R.id.statics_result);
    }

    public void setData(MicroCourseData courseData, StudentData studentData) {
        mCourseData = courseData;
        mStudentData = studentData;

        new LoadDataTask().execute();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List> {

        @Override
        protected List doInBackground(Void... voids) {
            List<Integer> list = null;
            if (mStudentData == null) {
                ClassData classData = ExternalParam.getInstance().getClassData();
                list = ScopeServer.getInstance().CountClassMicorcourseTimes(classData.ClassID, mCourseData.MicroCourseID);
            } else {
                list = ScopeServer.getInstance().CountStudentMicorcourseTimes(mStudentData.StudentID, mCourseData.MicroCourseID);
            }

            List<Entry> entries = new ArrayList<>();
            if (list == null) {
                return entries;
            }

            int mostData = 0;
            int allVal = 0;
            int lastVal = 0;
            mLargestValue = 0;
            mTime = new String();
            for (int i = 0; i < list.size(); i++) {
                int val = list.get(i);
                int remainder = i % 10;
                if (val > 0) {
                    allVal += val;

                    mostData = i;
                }
                if (remainder == 9) {
                    if ((allVal != lastVal) || i - 9 == 0) {
                        entries.add(new Entry(i - 9, allVal));
                    }
                    entries.add(new Entry(i, allVal));

                    lastVal = allVal;

                    if (allVal > mLargestValue) {
                        mLargestValue = allVal;
                        mTime = String.valueOf(i - 9) + "s - " + i + "s";
                    }

                    allVal = 0;
                }else if(i == list.size()-1){
                    if ((allVal != lastVal) || i - remainder == 0) {
                        entries.add(new Entry(i - remainder, allVal));
                    }
                    int lastData = (mostData / 10 + 1) * 10;
                    entries.add(new Entry(lastData, allVal));

                    lastVal = allVal;

                    if (allVal > mLargestValue) {
                        mLargestValue = allVal;
                        mTime = String.valueOf(i - remainder) + "s - " + i + "s";
                    }

                    allVal = 0;

                }
            }
            int lastData = (mostData / 10 + 2) * 10;
            if (lastData < entries.size()) {
                return entries.subList(0, lastData);
            } else {
                return entries;
            }
        }

        @Override
        protected void onPostExecute(List list) {

            initClassView(list);
            if(mLargestValue>0){
                if (mStudentData == null) {
                    mStaticsResultView.setText("同学们在" + mTime + "区间停顿多，达到" + mLargestValue + "人次");
                } else {
                    mStaticsResultView.setText(mStudentData.Username + "同学在" + mTime + "区间停顿多，达到" + mLargestValue + "次");
                }
            }else{
                if (mStudentData == null) {
                    mStaticsResultView.setText("同学们不曾看过此微课");
                } else {
                    mStaticsResultView.setText(mStudentData.Username + "同学不曾看过此微课");
                }
            }
        }
    }

    private void initClassView(List entries) {
        LineChartView lineChartView = findViewById(R.id.linechart_view);
        View studentChartLayout = findViewById(R.id.student_chart_layout);
        lineChartView.setVisibility(VISIBLE);
        studentChartLayout.setVisibility(GONE);

        ClassData classData = ExternalParam.getInstance().getClassData();
        lineChartView.setData(classData.ClassName + "微课情况统计", entries);
    }

    private void initStudentView(List entries) {
        LineChartView lineChartView = findViewById(R.id.linechart_view);
        View studentChartLayout = findViewById(R.id.student_chart_layout);
        lineChartView.setVisibility(VISIBLE);
        studentChartLayout.setVisibility(GONE);

        ClassData classData = ExternalParam.getInstance().getClassData();
        lineChartView.setData(classData.ClassName + "微课情况统计", entries);
    }

    private void initStudentVieww(List entries, List lineValues) {
        LineChartView lineChartView = findViewById(R.id.linechart_view);
        View studentChartLayout = findViewById(R.id.student_chart_layout);
        lineChartView.setVisibility(GONE);
        studentChartLayout.setVisibility(VISIBLE);

        BarChartView barChartView = findViewById(R.id.barchart_view);
        barChartView.setData(mStudentData.Username + "微课情况统计", null, entries, new MyValueFormatter("第", "次"), new MyValueFormatter("", "秒"), "秒");

        MultiLineChartView multiLineChartView = findViewById(R.id.multi_linechart_view);
        multiLineChartView.clearData();
        if (lineValues == null) {
            multiLineChartView.setVisibility(GONE);
        } else {
            multiLineChartView.setVisibility(VISIBLE);
            for (int i = 0; i < lineValues.size(); i++) {
                multiLineChartView.addData((List<Entry>) lineValues.get(i), ColorTemplate.VORDIPLOM_COLORS[i % 5], String.format("%d", i + 1), mCourseData.Duration);
            }
        }
        multiLineChartView.invalidate();
    }

}
