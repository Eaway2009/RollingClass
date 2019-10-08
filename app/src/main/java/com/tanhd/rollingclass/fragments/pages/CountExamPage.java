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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.statistics.StudentExamStatisticsFragment;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.StudentData;

public class CountExamPage extends Fragment {
    private static final String PARAM_TEACHING_MATERIAL = "PARAM_TEACHING_MATERIAL";
    private static final int MODULE_ID_STUDENT_PAGE = 1;
    private static final int MODULE_ID_CLASS_PAGE = 2;
    private static final int ROOT_LAYOUT_ID = R.id.content_layout;

    private Spinner mKnowledgeSpinner;
    private TextView mKnowledgeSpinnerTextView;
    private ArrayAdapter<KnowledgeDetailMessage> mDocumentSpinnerAdapter;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;
    private int mCurrentShowModuleId = -1;
    private StudentExamStatisticsFragment mStatisticsFragment;
    private KnowledgeModel mKnowledgeModel;
    private ClassExamStatisticsFragment mClassFragment;
    private ClassData mClassData;
    private StudentData mStudentData;

    public static CountExamPage getInstance(KnowledgeModel knowledgeModel) {
        CountExamPage countExamPage = new CountExamPage();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_TEACHING_MATERIAL, knowledgeModel);
        countExamPage.setArguments(bundle);
        return countExamPage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_count_exam, container, false);
        initParams();
        initViews(view);
        initFragments();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(PARAM_TEACHING_MATERIAL);
    }

    private void initViews(View view) {
        mKnowledgeSpinner = view.findViewById(R.id.knowledge_spinner);
        mKnowledgeSpinnerTextView = view.findViewById(R.id.spinner_textview);

        mDocumentSpinnerAdapter = new ArrayAdapter<KnowledgeDetailMessage>(getContext(), R.layout.spinner_check_textview);
        mDocumentSpinnerAdapter.setDropDownViewResource(R.layout.spinner_down_layout);
        mKnowledgeSpinner.setAdapter(mDocumentSpinnerAdapter);
        mKnowledgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mKnowledgeDetailMessage = (KnowledgeDetailMessage) mDocumentSpinnerAdapter.getItem(pos);
                mKnowledgeSpinnerTextView.setVisibility(View.GONE);
                if (mCurrentShowModuleId == MODULE_ID_STUDENT_PAGE) {
                    if (mStatisticsFragment != null && mStudentData != null) {
                        mStatisticsFragment.resetData(mStudentData, mKnowledgeModel, mKnowledgeDetailMessage);
                    }
                } else if (mCurrentShowModuleId == MODULE_ID_STUDENT_PAGE) {
                    if (mClassFragment != null && mClassData != null) {
                        mClassFragment.resetData(mClassData, mKnowledgeDetailMessage);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mKnowledgeSpinnerTextView.setVisibility(View.VISIBLE);
            }
        });

    }

    private void initFragments() {
        showModulePage(MODULE_ID_CLASS_PAGE);
        showModulePage(MODULE_ID_STUDENT_PAGE);
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
        if (moduleId == MODULE_ID_STUDENT_PAGE) {
            if (mStatisticsFragment == null) {
                mStatisticsFragment = StudentExamStatisticsFragment.newInstance(mKnowledgeModel);
                transaction.add(ROOT_LAYOUT_ID, mStatisticsFragment);
            }
            moduleFragment = mStatisticsFragment;
            if (mClassFragment != null) {
                transaction.hide(mClassFragment);
            }
        } else if (moduleId == MODULE_ID_CLASS_PAGE) {
            if (mClassFragment == null) {
                mClassFragment = ClassExamStatisticsFragment.newInstance();
                transaction.add(ROOT_LAYOUT_ID, mClassFragment);
            }
            moduleFragment = mClassFragment;
            if (mStatisticsFragment != null) {
                transaction.hide(mStatisticsFragment);
            }
        }
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();
    }

    public void resetData(ClassData classData) {
        mClassData = classData;
        showModulePage(MODULE_ID_CLASS_PAGE);
        mClassFragment.resetData(classData, mKnowledgeDetailMessage);
    }

    public void resetData(StudentData studentData) {
        mStudentData = studentData;
        showModulePage(MODULE_ID_STUDENT_PAGE);
        mStatisticsFragment.resetData(studentData, mKnowledgeModel, mKnowledgeDetailMessage);
    }
}
