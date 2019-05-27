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

import com.github.mikephil.charting.data.BarEntry;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.CountMicroCourseStudentData;
import com.tanhd.rollingclass.utils.MyValueFormatter;
import com.tanhd.rollingclass.views.BarChartView;
import com.tanhd.rollingclass.views.MultiLineChartView;

import java.util.ArrayList;
import java.util.List;

public class CountStudentMicroCoursePage extends Fragment {
    private String mStudentID;
    private String mCourseID;
    private BarChartView mBarChartView;
    private MultiLineChartView mMultiLineChartView;

    public static CountStudentMicroCoursePage newInstance(String courseID, String studentID) {
        Bundle args = new Bundle();
        args.putString("courseID", courseID);
        args.putString("studentID", studentID);
        CountStudentMicroCoursePage page = new CountStudentMicroCoursePage();
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mStudentID = getArguments().getString("studentID");
        mCourseID = getArguments().getString("courseID");

        View view = inflater.inflate(R.layout.page_count_student_microcourse, container, false);
        mBarChartView = view.findViewById(R.id.barchart_view);
        mMultiLineChartView = view.findViewById(R.id.multi_linechart_view);
        new LoadDataTask().execute();
        return view;
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List> {

        @Override
        protected List doInBackground(Void... voids) {
            List<CountMicroCourseStudentData> list = ScopeServer.getInstance().QureyMicroCourseStatisticByCoureseID(mCourseID);
            List<BarEntry> entries = new ArrayList<>();
            int pos = 0;
            for (int i=0; i<list.size(); i++) {
                CountMicroCourseStudentData data = list.get(i);
                if (!data.StudentID.equals(mStudentID))
                    continue;

                int count = data.VideoEndTime - data.VideoStartTime;
                entries.add(new BarEntry(pos++, count));
            }

            return entries;
        }

        @Override
        protected void onPostExecute(List list) {
            if (list == null || list.size() == 0)
                return;

            mBarChartView.setData("", null, list, new MyValueFormatter("第", "次"), new MyValueFormatter("", "秒"), "秒");
        }
    }
}
