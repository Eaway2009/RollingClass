package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.MicroCourseBarChartView;
import com.tanhd.rollingclass.views.MicroCourseListView;
import com.tanhd.rollingclass.views.StudentListView;

/**
 * Created by eaway on 19/6/7.
 */

public class LearningStaticsFragment extends Fragment {

    private MicroCourseListView mMicroCourseListView;
    private StudentListView mStudentListView;

    private MicroCourseData mSelectedCourseData;

    private MicroCourseBarChartView mBarChartView;

    public static LearningStaticsFragment newInstance() {
        LearningStaticsFragment fragment = new LearningStaticsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learning_statics, container, false);
        mBarChartView = view.findViewById(R.id.micro_course_data);

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
                    ToastUtil.show(R.string.toast_select_video);
                    return;
                }

                mBarChartView.setData(mSelectedCourseData, studentData);
            }
        });

        view.findViewById(R.id.micro_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showFragment(mMicroCoursePage);
            }
        });
        view.findViewById(R.id.question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showFragment(examPage);
            }
        });

        view.findViewById(R.id.micro_course).callOnClick();
        return view;
    }
}
