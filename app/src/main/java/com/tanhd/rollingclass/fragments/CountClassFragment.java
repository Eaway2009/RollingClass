package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.pages.CountClassMicroCoursePage;
import com.tanhd.rollingclass.fragments.pages.CountExamPage;
import com.tanhd.rollingclass.fragments.statistics.ClassStudentsFragment;
import com.tanhd.rollingclass.fragments.statistics.StatisticsActivity;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.StudentData;

import java.io.Serializable;

import static com.tanhd.rollingclass.fragments.statistics.StatisticsActivity.PAGE_ID_MICRO_COURSE;
import static com.tanhd.rollingclass.fragments.statistics.StatisticsActivity.PAGE_ID_QUESTION;

public class CountClassFragment extends Fragment {
    private CountExamPage examPage;
    private int mPageId;
    private int mCurrentShowModuleId = -1;
    private RadioGroup mStatisticsTypeRadioGroup;
    private PagesListener mPagesListener;
    private ClassData mClssData;
    private StudentData mStudentData;
    private ClassStudentsFragment mClassStudentsFragment;
    private CountClassMicroCoursePage microCourseInfoPage;
    private KnowledgeModel mKownledgeModel;

    public static CountClassFragment newInstance(KnowledgeModel knowledgeModel, int pageId, PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt(StatisticsActivity.PAGE_ID, pageId);
        args.putSerializable(StatisticsActivity.TEACHING_MATERIAL_ID, knowledgeModel);
        CountClassFragment page = new CountClassFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void initParams() {
        mPageId = getArguments().getInt(StatisticsActivity.PAGE_ID, PAGE_ID_MICRO_COURSE);
        mKownledgeModel = (KnowledgeModel) getArguments().getSerializable(StatisticsActivity.TEACHING_MATERIAL_ID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_count_class, container, false);
        initParams();
        showModulePage(mPageId);
        initViews(view);
        return view;
    }

    private void setListener(PagesListener listener) {
        mPagesListener = listener;
    }

    private void initViews(View view) {
        mStatisticsTypeRadioGroup = view.findViewById(R.id.statistics_type_rg);
        mStatisticsTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.micro_course:
//                        showModulePage(PAGE_ID_MICRO_COURSE);
                        break;
                    case R.id.question:
                        showModulePage(PAGE_ID_QUESTION);
                        break;
                }
            }
        });
        switch (mPageId) {
            case PAGE_ID_MICRO_COURSE:
//                mStatisticsTypeRadioGroup.check(R.id.micro_course);
                break;
            case PAGE_ID_QUESTION:
                mStatisticsTypeRadioGroup.check(R.id.question);
                break;
        }

        mClassStudentsFragment = ClassStudentsFragment.newInstance(new ClassStudentsFragment.Callback() {
            @Override
            public void onCheckClass(ClassData classData) {
                mClssData = classData;
                if (examPage != null) {
                    examPage.resetData(classData);
                }
            }

            @Override
            public void onCheckStudent(ClassData classData, StudentData studentData) {
                mClssData = classData;
                mStudentData = studentData;
                if (examPage != null) {
                    examPage.resetData(studentData);
                }
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.fragment_class_menu, mClassStudentsFragment).commit();

        view.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPagesListener != null) {
                    mPagesListener.onBack();
                }
            }
        });
    }


    /**
     * [展示指定Id的页面]<BR>
     */
    public void showModulePage(int moduleId) {
        if (mCurrentShowModuleId == moduleId) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment moduleFragment = null;
        if (moduleId == PAGE_ID_MICRO_COURSE) {
            if (microCourseInfoPage == null) {
                microCourseInfoPage = new CountClassMicroCoursePage();
                transaction.add(R.id.content_layout, microCourseInfoPage);
            }
            moduleFragment = microCourseInfoPage;
            if (examPage != null) {
                transaction.hide(examPage);
            }
        } else if (moduleId == PAGE_ID_QUESTION) {
            if (examPage == null) {
                examPage = CountExamPage.getInstance(mKownledgeModel);
                transaction.add(R.id.content_layout, examPage);
            }
            moduleFragment = examPage;
            if (microCourseInfoPage != null) {
                transaction.hide(microCourseInfoPage);
            }
        }
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = moduleId;
    }

    public interface PagesListener {
        void onPageChange(int id);

        void onBack();
    }
}
