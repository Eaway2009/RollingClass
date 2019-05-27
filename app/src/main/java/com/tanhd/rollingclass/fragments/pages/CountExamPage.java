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

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.views.StudentListView;

public class CountExamPage extends Fragment {
    private StudentListView mStudentListView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_count_exam, container, false);
        mStudentListView = view.findViewById(R.id.student_view);
        mStudentListView.setListener(new StudentListView.StudentListViewListener() {
            @Override
            public void onSelectedItem(StudentData studentData) {
                if (studentData == null) {
                    CountClassLessonSamplePage page = new CountClassLessonSamplePage();
                    showFragment(page);
                } else {
                    CountStudentExamPage page = CountStudentExamPage.newInstance(studentData);
                    showFragment(page);
                }
            }
        });

        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.replace(R.id.framelayout, fragment);
        beginTransaction.addToBackStack("exam");
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        beginTransaction.commit();
    }
}
