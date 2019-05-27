package com.tanhd.rollingclass.fragments;

import android.content.Context;
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
import com.tanhd.rollingclass.fragments.pages.CountClassLessonSamplePage;
import com.tanhd.rollingclass.fragments.pages.CountClassMicroCoursePage;
import com.tanhd.rollingclass.fragments.pages.CountExamPage;

public class CountClassFragment extends Fragment {
    private CountClassMicroCoursePage microCourseInfoPage;
    private CountExamPage examPage;

    public static CountClassFragment newInstance() {
        CountClassFragment fragment = new CountClassFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_count_class, container, false);
        microCourseInfoPage = new CountClassMicroCoursePage();
        examPage = new CountExamPage();

        view.findViewById(R.id.micro_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(microCourseInfoPage);
            }
        });
        view.findViewById(R.id.question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(examPage);
            }
        });

        view.findViewById(R.id.micro_course).callOnClick();
        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.replace(R.id.framelayout, fragment);
        beginTransaction.addToBackStack("count");
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        beginTransaction.commit();
    }
}
