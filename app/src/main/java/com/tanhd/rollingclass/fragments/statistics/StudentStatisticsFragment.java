package com.tanhd.rollingclass.fragments.statistics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.List;

public class StudentStatisticsFragment extends Fragment implements View.OnClickListener {

    private static final int MODULE_ID_QUESTION_LIST = 0;
    private static final int ROOT_LAYOUT_ID = R.id.content_layout;
    private KnowledgeModel mKnowledgeModel;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;

    private int mPageSize = 100;
    private boolean mIsRequesting = false;
    private StudentStatisticsFragment.Callback mCallback;

    private StudentExamStatisticsFragment mStatisticsFragment;
    private Spinner mKnowledgeSpinner;
    private ArrayAdapter mDocumentSpinnerAdapter;
    private View mBackButton;
    private int mCurrentShowModuleId = -1;
    private TextView mKnowledgeSpinnerTextView;

    public static StudentStatisticsFragment newInstance(KnowledgeModel knowledgeModel, StudentStatisticsFragment.Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        StudentStatisticsFragment page = new StudentStatisticsFragment();
        page.setArguments(args);
        page.setCallback(callback);
        return page;
    }

    public void setCallback(StudentStatisticsFragment.Callback callback) {
        this.mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_student_answer_statistics, container, false);
        initParams();
        initViews(view);
        initFragment();
        initData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
    }

    private void initViews(View view) {
        mBackButton = view.findViewById(R.id.back_button);
        RadioButton microCourse = view.findViewById(R.id.micro_course);
        microCourse.setChecked(false);

        mKnowledgeSpinner = view.findViewById(R.id.knowledge_spinner);
        mKnowledgeSpinner = view.findViewById(R.id.knowledge_spinner);
        mKnowledgeSpinnerTextView = view.findViewById(R.id.spinner_textview);

        mDocumentSpinnerAdapter = new ArrayAdapter<KnowledgeDetailMessage>(getContext(),R.layout.spinner_check_textview);
        mDocumentSpinnerAdapter.setDropDownViewResource(R.layout.spinner_down_layout);
        mKnowledgeSpinner.setAdapter(mDocumentSpinnerAdapter);
        mKnowledgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mKnowledgeDetailMessage = (KnowledgeDetailMessage) mDocumentSpinnerAdapter.getItem(pos);
                mKnowledgeSpinnerTextView.setVisibility(View.GONE);
                mStatisticsFragment.resetData(mKnowledgeModel, mKnowledgeDetailMessage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mKnowledgeSpinnerTextView.setVisibility(View.VISIBLE);
            }
        });
        mBackButton.setOnClickListener(this);
    }

    private void initData(){
        resetData(mKnowledgeModel);
    }

    public void resetData(KnowledgeModel module){
        mIsRequesting = true;
        mKnowledgeModel = module;
        mDocumentSpinnerAdapter.clear();
        new InitDataTask().execute();
    }

    private void initFragment(){
        showModulePage(MODULE_ID_QUESTION_LIST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_button:
                if(mCallback!=null){
                    mCallback.onBack();
                }
                break;
        }
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
        if (moduleId == MODULE_ID_QUESTION_LIST) { //习题
            if (mStatisticsFragment == null) {
                mStatisticsFragment = StudentExamStatisticsFragment.newInstance(mKnowledgeModel);
                transaction.add(ROOT_LAYOUT_ID, mStatisticsFragment);
            }
            moduleFragment = mStatisticsFragment;
        }
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeDetailMessage>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<KnowledgeDetailMessage> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (mKnowledgeModel != null) {
                StudentData studentData = (StudentData) userData.getUserData();
                return ScopeServer.getInstance().QureyKnowledgeByClassID(studentData.ClassID, mKnowledgeModel.teaching_material_id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<KnowledgeDetailMessage> documentList) {
            if (documentList != null && documentList.size() > 0) {
                mDocumentSpinnerAdapter.addAll(documentList);
            } else {
                mDocumentSpinnerAdapter.clear();
            }
        }
    }

    public interface Callback{
        void onBack();
    }
}
