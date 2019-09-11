package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.StudentSelectorFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.fragments.resource.ResourceBaseFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassTestingFragment extends Fragment implements View.OnClickListener {

    private static final String PARAM_CLASS_DATA = "PARAM_CLASS_DATA";
    private static final String PARAM_TEACHING_MATERIAL_ID = "PARAM_TEACHING_MATERIAL_ID";
    private ClassData mClassData;
    private TextView mSchoolResourceTextView;
    private TextView mPublicResourceTextView;
    private TextView mMyResourceTextView;
    private QuestionResourceFragment mQuestionResourceFragment;
    private QuestionModel mQuestionModel;
    private String mTeachingMaterialId;
    private StudentSelectorFragment mStudentSelectorFragment;
    private ArrayList<String> mStudentList;
    private Button mCancelButton;
    private Button mCommitButton;

    public static ClassTestingFragment getInstance(ClassData classData, String teachingMaterialId) {
        ClassTestingFragment classTestingFragment = new ClassTestingFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_CLASS_DATA, classData);
        bundle.putString(PARAM_TEACHING_MATERIAL_ID, teachingMaterialId);
        classTestingFragment.setArguments(bundle);
        return classTestingFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_class_testing, null);
        initParams();
        initViews(contentView);
        return contentView;
    }

    private void initViews(View view) {
        mSchoolResourceTextView = view.findViewById(R.id.school_resource);
        mMyResourceTextView = view.findViewById(R.id.my_resource);
        mPublicResourceTextView = view.findViewById(R.id.public_resource);
        mCancelButton = view.findViewById(R.id.cancel_button);
        mCommitButton = view.findViewById(R.id.commit_button);
        mSchoolResourceTextView.setOnClickListener(this);
        mMyResourceTextView.setOnClickListener(this);
        mPublicResourceTextView.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mCommitButton.setOnClickListener(this);

        mQuestionResourceFragment = QuestionResourceFragment.newInstance(new ResourceBaseFragment.Callback() {
            @Override
            public void itemChecked(ResourceModel resourceModel, QuestionModel questionModel) {
                mQuestionModel = questionModel;
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();

        mStudentSelectorFragment = StudentSelectorFragment.newInstance(false, null, mClassData, new StudentSelectorFragment.StudentSelectListener() {
            @Override
            public void onStudentSelected(ArrayList<StudentData> studentList) {
                mStudentList = new ArrayList<>();
                for (StudentData studentData : studentList) {
                    if (studentData != null) {
                        mStudentList.add(studentData.StudentID);
                    }
                }
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.student_select_layout_fragment, mStudentSelectorFragment).commit();
        resetData(KeyConstants.LevelType.SCHOOL_LEVEL);
    }

    private void initParams() {
        Bundle args = getArguments();
        mClassData = (ClassData) args.getSerializable(PARAM_CLASS_DATA);
        mTeachingMaterialId = args.getString(PARAM_TEACHING_MATERIAL_ID);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.school_resource:
                mSchoolResourceTextView.setEnabled(false);
                mMyResourceTextView.setEnabled(true);
                mPublicResourceTextView.setEnabled(true);
                resetData(KeyConstants.LevelType.SCHOOL_LEVEL);
                break;
            case R.id.public_resource:
                mSchoolResourceTextView.setEnabled(true);
                mMyResourceTextView.setEnabled(true);
                mPublicResourceTextView.setEnabled(false);
                resetData(KeyConstants.LevelType.PUBLIC_LEVEL);
                break;
            case R.id.my_resource:
                mSchoolResourceTextView.setEnabled(true);
                mMyResourceTextView.setEnabled(false);
                mPublicResourceTextView.setEnabled(true);
                resetData(KeyConstants.LevelType.PRIVATE_LEVEL);
                break;
            case R.id.cancel_button:
                dismiss();
                break;
            case R.id.commit_button:
                if(mQuestionModel==null){
                    Toast.makeText(getActivity(), "请先选择题目",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mStudentList==null||mStudentList.size()==0){
                    Toast.makeText(getActivity(), "请选择需要参与测评的学生",Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<String, String> params = new HashMap<>();
                params.put("examID", mQuestionModel.question_id);
                params.put("teacherID", ExternalParam.getInstance().getUserData().getOwnerID());
                MyMqttService.publishMessage(PushMessage.COMMAND.QUESTIONING, mStudentList, params);
                dismiss();
                break;
        }
    }

    private void dismiss(){
        DialogFragment dialog = (DialogFragment) getParentFragment();
        dialog.dismiss();
    }

    private void resetData(int level) {
        new InitQuestionDataTask(level).execute();
    }

    private class InitQuestionDataTask extends AsyncTask<Void, Void, List<QuestionModel>> {

        private int level;

        public InitQuestionDataTask(int level) {
            this.level = level;
        }

        @Override
        protected List<QuestionModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                TeacherData teacherData = (TeacherData) userData.getUserData();
                return ScopeServer.getInstance().QureyQuestionResourceByTeacherID(
                        teacherData.TeacherID, mTeachingMaterialId,
                        level, 1, 50);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<QuestionModel> questionDataList) {
            if (questionDataList != null && questionDataList.size() > 0) {
                mQuestionResourceFragment.setListData(questionDataList);
            } else {
                mQuestionResourceFragment.clearListData();
            }
        }
    }
}
