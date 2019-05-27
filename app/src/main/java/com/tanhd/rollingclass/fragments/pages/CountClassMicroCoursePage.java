package com.tanhd.rollingclass.fragments.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.views.MicroCourseBarChartView;
import com.tanhd.rollingclass.views.MicroCourseListView;
import com.tanhd.rollingclass.views.StudentListView;

public class CountClassMicroCoursePage extends Fragment {
    private MicroCourseBarChartView mBarChartView;
    private MicroCourseListView mMicroCourseListView;
    private StudentListView mStudentListView;
    private MicroCourseData mSelectedCourseData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_count_class_microcourse, container, false);
        mBarChartView = view.findViewById(R.id.micro_course);
        mMicroCourseListView = view.findViewById(R.id.micro_view);
        mMicroCourseListView.setListener(new MicroCourseListView.MicroCourseListViewListener() {
            @Override
            public void onSelectedItem(MicroCourseData microCourseData) {
                mStudentListView.setSelectedItem(null);
                mSelectedCourseData = microCourseData;
                mBarChartView.setData(mSelectedCourseData, null);
            }
        });

        mStudentListView = view.findViewById(R.id.student_view);
        mStudentListView.enableClass(false);
        mStudentListView.setListener(new StudentListView.StudentListViewListener() {
            @Override
            public void onSelectedItem(StudentData studentData) {
                if (mSelectedCourseData == null) {
                    Toast.makeText(getContext().getApplicationContext(), "请先选择微课!", Toast.LENGTH_LONG).show();
                    return;
                }

                mBarChartView.setData(mSelectedCourseData, studentData);
            }
        });

        return view;
    }
}
