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

import com.tanhd.rollingclass.base.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.StudentSelectorFragment;
import com.tanhd.rollingclass.fragments.WaitAnswerFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.fragments.resource.ResourceBaseFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 测学
 */
public class ClassTestingFragment extends Fragment implements View.OnClickListener {

    private static final String PARAM_CLASS_DATA = "PARAM_CLASS_DATA";
    private static final String PARAM_TEACHING_MATERIAL_ID = "PARAM_TEACHING_MATERIAL_ID";
    private static final String PARAM_KNOWLEDGE_ID = "PARAM_KNOWLEDGE_ID";
    private static final String PARAM_IS_RESPONDER = "PARAM_IS_RESPONDER";
    private ClassData mClassData;
    private TextView mSchoolResourceTextView;
    private TextView mPublicResourceTextView;
    private TextView mMyResourceTextView;
    private QuestionResourceFragment mQuestionResourceFragment;
    private List<QuestionModel> mQuestionList;
    private String mTeachingMaterialId;
    private StudentSelectorFragment mStudentSelectorFragment;
    private List<QuestionSetData.Group> mStudentList;
    private Button mCancelButton;
    private Button mCommitButton;
    private String mKnowledgeId;
    private boolean mIsResponder;

    public static ClassTestingFragment getInstance(ClassData classData, String teachingMaterialId, String knowledgeId, boolean isResponder) {
        ClassTestingFragment classTestingFragment = new ClassTestingFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_CLASS_DATA, classData);
        bundle.putString(PARAM_TEACHING_MATERIAL_ID, teachingMaterialId);
        bundle.putString(PARAM_KNOWLEDGE_ID, knowledgeId);
        bundle.putBoolean(PARAM_IS_RESPONDER, isResponder);
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

    private void initParams() {
        Bundle args = getArguments();
        mClassData = (ClassData) args.getSerializable(PARAM_CLASS_DATA);
        mTeachingMaterialId = args.getString(PARAM_TEACHING_MATERIAL_ID);
        mKnowledgeId = args.getString(PARAM_KNOWLEDGE_ID);
        mIsResponder = args.getBoolean(PARAM_IS_RESPONDER);
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

        mQuestionResourceFragment = QuestionResourceFragment.newInstance(new ResourceBaseFragment.ListCallback() {

            @Override
            public void onListChecked(List<ResourceModel> resourceList, List<QuestionModel> questionList) {
                mQuestionList = questionList;
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.question_layout_fragment, mQuestionResourceFragment).commit();

        mStudentSelectorFragment = StudentSelectorFragment.newInstance(false, null, mClassData, new StudentSelectorFragment.StudentSelectListener() {
            @Override
            public void onStudentSelected(ArrayList<QuestionSetData.Group> studentList) {
                mStudentList = studentList;
            }
        });
        getFragmentManager().beginTransaction().replace(R.id.student_select_layout_fragment, mStudentSelectorFragment).commit();
        resetData(KeyConstants.LevelType.SCHOOL_LEVEL);
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
                if(mQuestionList==null||mQuestionList.size()==0){
                    ToastUtil.show(R.string.toast_select_topic);
                    return;
                }
                if(mStudentList==null||mStudentList.size()==0){
                    ToastUtil.show(R.string.toast_select_student);
                    return;
                }
                new NewSetTask(mQuestionList, mStudentList).execute();
                break;
        }
    }
    private class NewSetTask extends AsyncTask<List, Void, String> {
        private List<QuestionSetData.Group> groupList;
        private List<QuestionModel> questionList;

        NewSetTask(List<QuestionModel> questionModelList, List<QuestionSetData.Group> groups){
            groupList = groups;
            questionList = questionModelList;
        }

        @Override
        protected String doInBackground(List... lists) {

            QuestionSetData questionSetData = new QuestionSetData();
            questionSetData.TeacherID = ExternalParam.getInstance().getUserData().getOwnerID();
            questionSetData.SetName = getResources().getString(R.string.lbl_ask);

            questionSetData.QuestionList = new ArrayList<>();
            for (int i=0; i<questionList.size(); i++) {
                QuestionModel questionData = (QuestionModel) questionList.get(i);
                questionSetData.QuestionList.add(questionData.question_id);
            }

            questionSetData.groups = groupList;
            questionSetData.knowledge_id = mKnowledgeId;
            questionSetData.class_id = mClassData.ClassID;
            String result = ScopeServer.getInstance().InsertQuestionSet(questionSetData.toJSON().toString());
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                ToastUtil.show(getResources().getString(R.string.toast_quiz_fail));
                return;
            }

            List<String> to = new ArrayList<>();
            for (QuestionSetData.Group group : groupList) {
                to.addAll(group.students);
            }
            HashMap<String, String> params = new HashMap<>();
            params.put("examID", result);
            params.put("teacherID", ExternalParam.getInstance().getUserData().getOwnerID());
            if(mIsResponder) {
                MyMqttService.publishMessage(PushMessage.COMMAND.RESPONDER, to, params);
            } else {
                MyMqttService.publishMessage(PushMessage.COMMAND.QUESTIONING, (List<String>) null, params);
            }
            ExternalParam.getInstance().setQuestionSetID(result);
//            showFragment(WaitAnswerFragment.newInstance(result));
            dismiss();
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
