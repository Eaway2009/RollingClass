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
    private MicroCourseData mCourseData;
    private StudentData mStudentData;

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
    }

    public void setData(MicroCourseData courseData, StudentData studentData) {
        mCourseData = courseData;
        mStudentData = studentData;

        new LoadDataTask().execute();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List> {
        List<List<Entry>> mLineValues;

        @Override
        protected List doInBackground(Void... voids) {
            if (mStudentData == null) {
                ClassData classData = ExternalParam.getInstance().getClassData();
                List<Integer> list = ScopeServer.getInstance().CountClassMicorcourseTimes(classData.ClassID, mCourseData.MicroCourseID);
                if (list == null)
                    return null;

                int mostData = 0;
                int allVal = 0;
                List<Entry> entries = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    int val = list.get(i);
                    int j = i % 10;
                    if (val > 0) {
                        allVal += val;

                        mostData = i;
                    }
                    if (j == 0) {
                        entries.add(new Entry(i, allVal));
                        allVal = 0;
                    }
                }
                int lastData = (mostData / 10 + 2) * 10;
                if (lastData < entries.size()) {
                    return entries.subList(0, lastData);
                } else {
                    return entries;
                }
            } else {
                List<CountMicroCourseStudentData> list = ScopeServer.getInstance().QureyMicroCourseStatisticByCoureseID(mCourseData.MicroCourseID);
                List<BarEntry> entries = new ArrayList<>();
                int pos = 1;
                mLineValues = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    CountMicroCourseStudentData data = list.get(i);
                    if (!data.StudentID.equals(mStudentData.StudentID))
                        continue;

                    int count = data.VideoEndTime - data.VideoStartTime;
                    entries.add(new BarEntry(pos, count));

                    List<Entry> values = new ArrayList<>();
                    for (int j = data.VideoStartTime; j <= data.VideoEndTime; j++) {
                        values.add(new Entry(j, pos));
                    }
                    pos++;
                    mLineValues.add(values);
                }

                return entries;
            }
        }

        @Override
        protected void onPostExecute(List list) {
            if (list == null)
                return;

            if (mStudentData == null) {
                initClassView(list);
            } else {
                initStudentVieww(list, mLineValues);
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
