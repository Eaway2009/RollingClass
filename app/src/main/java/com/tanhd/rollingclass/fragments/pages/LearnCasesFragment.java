package com.tanhd.rollingclass.fragments.pages;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.ClassSelectorFragment;
import com.tanhd.rollingclass.fragments.ExamFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.WaitAnswerFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.LessonItemAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class LearnCasesFragment extends Fragment implements OnClickListener, ExpandableListView.OnChildClickListener {

    private RelativeLayout mRlInnerTitle;
    private RelativeLayout mRlMenuLayout;
    private LinearLayout mLlMenuContainer;
    private TextView mTvInsertResource;
    private TextView mTvExerciseResult;
    private TextView mTvClassBegin;
    private TextView mTvClassStatus;
    private PagesListener mListener;

    private LearnCasesContainerFragment mLearnCasesContainerFragment;
    private TextView mKnowledgeNameTextView;
    private ExpandableListView mExpandableListView;
    private LessonItemAdapter mAdapter;
    private String mKnowledgeId;
    private String mKnowledgeDetailName;
    private String mTeacherName;
    private int mClassPageType;
    private String mTeachingMaterialId;

    private boolean init = true;
    private int mKnowledgeStatus = KeyConstants.KnowledgeStatus.FRE_CLASS;
    private Button mPreClassLearningButton;
    private Button mAfterClassLearningButton;
    private View mLearningButtonsLayout;
    private ClassData mClassData;

    public static LearnCasesFragment newInstance(String knowledgeId, String knowledgeName, String teachingMaterialId, int pageType, String teacherName, LearnCasesFragment.PagesListener listener) {
        Bundle args = new Bundle();
        LearnCasesFragment page = new LearnCasesFragment();
        args.putSerializable(LearnCasesActivity.PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putSerializable(LearnCasesActivity.PARAM_KNOWLEDGE_NAME, knowledgeName);
        args.putSerializable(LearnCasesActivity.PARAM_TEACHER_NAME, teacherName);
        args.putSerializable(LearnCasesActivity.PARAM_TEACHING_MATERIAL_ID, teachingMaterialId);
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, pageType);
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(PagesListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_learncases_pages, container, false);
        initParams();
        initViews(view);
        setData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mClassPageType = args.getInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, KeyConstants.ClassPageType.TEACHER_CLASS_PAGE);
        mKnowledgeId = args.getString(LearnCasesActivity.PARAM_KNOWLEDGE_ID);
        mTeacherName = args.getString(LearnCasesActivity.PARAM_TEACHER_NAME);
        mKnowledgeDetailName = args.getString(LearnCasesActivity.PARAM_KNOWLEDGE_NAME);
        mTeachingMaterialId = args.getString(LearnCasesActivity.PARAM_TEACHING_MATERIAL_ID);
    }

    private void initViews(View view) {
        mKnowledgeNameTextView = view.findViewById(R.id.knowledge_name_tv);
        mRlMenuLayout = view.findViewById(R.id.rl_cases_menu);
        mRlInnerTitle = view.findViewById(R.id.rl_inner_title);
        mLlMenuContainer = view.findViewById(R.id.ll_menu_container);
        mTvInsertResource = view.findViewById(R.id.tv_insert_resource);
        mTvExerciseResult = view.findViewById(R.id.tv_exercise_result);
        mTvClassBegin = view.findViewById(R.id.tv_class_begin);
        mTvClassStatus = view.findViewById(R.id.tv_in_class);
        mLearningButtonsLayout = view.findViewById(R.id.learning_buttons_layout);
        mPreClassLearningButton = view.findViewById(R.id.pre_class_learning_tv);
        mAfterClassLearningButton = view.findViewById(R.id.after_class_learning_tv);
        initListViews(view);

        mTvInsertResource.setOnClickListener(this);
        mTvExerciseResult.setOnClickListener(this);
        mTvClassBegin.setOnClickListener(this);
        mPreClassLearningButton.setOnClickListener(this);
        mAfterClassLearningButton.setOnClickListener(this);
        view.findViewById(R.id.back_button).setOnClickListener(this);

        mLearnCasesContainerFragment = LearnCasesContainerFragment.newInstance(mClassPageType, mPagesListener);
        getFragmentManager().beginTransaction().replace(R.id.content_layout, mLearnCasesContainerFragment).commit();
        if (mClassPageType == KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE) {
            mTvClassBegin.setVisibility(View.GONE);
            mTvClassStatus.setVisibility(View.GONE);
            mLearningButtonsLayout.setVisibility(View.VISIBLE);
        } else if (mClassPageType == KeyConstants.ClassPageType.TEACHER_CLASS_PAGE) {
            mTvClassBegin.setVisibility(View.VISIBLE);
            mTvClassStatus.setVisibility(View.GONE);
            mLearningButtonsLayout.setVisibility(View.GONE);
        } else {
            mTvClassBegin.setVisibility(View.GONE);
            mTvClassStatus.setVisibility(View.GONE);
            mLearningButtonsLayout.setVisibility(View.GONE);
        }
    }

    private void initListViews(View view) {
        mExpandableListView = view.findViewById(R.id.expandable_listview);
        mAdapter = new LessonItemAdapter(getActivity());
        mExpandableListView.setVerticalScrollBarEnabled(false);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setHeaderDividersEnabled(false);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(this);
    }


    private void setData() {
        mKnowledgeNameTextView.setText(mKnowledgeDetailName);
        if(init) {
            new InitDataTask().execute();
        }
    }

    LearnCasesContainerFragment.PagesListener mPagesListener = new LearnCasesContainerFragment.PagesListener() {

        @Override
        public void onFullScreen(boolean isFull) {
            if (mListener != null) {
                mListener.onFullScreen(isFull);
                mRlMenuLayout.setVisibility(isFull == true ? View.GONE : View.VISIBLE);
                mRlInnerTitle.setVisibility(isFull == true ? View.GONE : View.VISIBLE);
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_insert_resource:
                break;
            case R.id.tv_exercise_result:
                break;
            case R.id.after_class_learning_tv:
                mPreClassLearningButton.setEnabled(true);
                mAfterClassLearningButton.setEnabled(false);
                mKnowledgeStatus = KeyConstants.KnowledgeStatus.AFTER_CLASS;
                init = true;
                new InitDataTask().execute();
                break;
            case R.id.pre_class_learning_tv:
                mPreClassLearningButton.setEnabled(false);
                mAfterClassLearningButton.setEnabled(true);
                mKnowledgeStatus = KeyConstants.KnowledgeStatus.FRE_CLASS;
                init = true;
                new InitDataTask().execute();
                break;
            case R.id.tv_class_begin:
                FrameDialog.showLittleDialog(getChildFragmentManager(), ClassSelectorFragment.newInstance(new ClassSelectorFragment.OnClassListener() {
                    @Override
                    public void onClassSelected(ClassData classData) {
//                        MQTT.getInstance().subscribe(classData.ClassID);
                        mClassData = classData;
                        MyMqttService.subscribe(classData.ClassID);
                        ExternalParam.getInstance().setStatus(2);
                        classData.resetStudentState(0);
                        ExternalParam.getInstance().setClassData(classData);
                        notifyEnterClass(null);
                        if(mLearnCasesContainerFragment!=null){
                            mLearnCasesContainerFragment.setParam(mClassData, mTeachingMaterialId);
                        }

                        mTvClassBegin.setVisibility(View.GONE);
                        mTvClassStatus.setVisibility(View.VISIBLE);
                        mTvClassStatus.setText(getResources().getString(R.string.class_started, classData.ClassName));
                    }
                }));
                break;
            case R.id.back_button:
                if (mListener != null) {
                    mListener.onBack();
                }
                break;
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MQTT.register(mqttListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.unregister(mqttListener);
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case OFFLINE:
                case ONLINE:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    ClassData classData = ExternalParam.getInstance().getClassData();
                    if (classData == null)
                        return;

                    classData.setStudentState(message.from, (message.command == PushMessage.COMMAND.ONLINE ? 1 : 0));
                    break;
                case ANSWER_COMPLETED:
                    String content = message.parameters.get("content");
                    try {
                        JSONObject json = new JSONObject(content);
                        String examID = json.optString("examID");
                        Database.getInstance().setQuestioning(examID);
                        FrameDialog.show(getChildFragmentManager(), WaitAnswerFragment.newInstance(examID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case QUESTIONING: {
                    if(ExternalParam.getInstance().getStatus() == 2 && !ExternalParam.getInstance().getUserData().isTeacher()) {
                        String examID = message.parameters.get("examID");
                        final String teacherID = message.parameters.get("teacherID");
                        FrameDialog.fullShow(getFragmentManager(), ExamFragment.newInstance(teacherID, examID, new ExamFragment.ExamListener() {
                            @Override
                            public void onFinished() {
                                MyMqttService.publishMessage(PushMessage.COMMAND.ANSWER_COMPLETED, teacherID, null);
                            }
                        }));
                    }
                    break;
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private void notifyEnterClass(String studentID) {
        ClassData classData = ExternalParam.getInstance().getClassData();

        //通知学生端打开学案
        TeacherData teacherData = (TeacherData) ExternalParam.getInstance().getUserData().getUserData();
        HashMap<String, String> params = new HashMap<>();
        params.put(PushMessage.ENTER_CLASS, "1");
        params.put(PushMessage.CLASS_NAME, classData.ClassName);
        params.put(PushMessage.SUBJECT_NAME, AppUtils.getSubjectNameByCode(teacherData.SubjectCode));
        params.put(PushMessage.TEACHER_NAME, teacherData.Username);
        params.put(PushMessage.KnowledgePointName, mKnowledgeDetailName);
        params.put(PushMessage.KNOWLEDGE_ID, mKnowledgeId);
        params.put(PushMessage.LESSON_SAMPLE_NAME, mKnowledgeDetailName);
//        params.put("UrlContent", mKnowledgeDetailMessage.UrlContent);
        MyMqttService.publishMessage(PushMessage.COMMAND.CLASS_BEGIN, studentID, params);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (groupPosition < mAdapter.getGroupCount() && mAdapter.getGroup(groupPosition) != null) {
            callClickItem(groupPosition, childPosition);
        }
        return true;
    }

    public void callClickItem(int groupPosition, int childPosition) {
        KnowledgeLessonSample group = mAdapter.getGroup(groupPosition);
        if (childPosition < group.getChildren().size() && group.getChildren().get(childPosition) != null) {
            ResourceModel item = group.getChildren().get(childPosition);
            mLearnCasesContainerFragment.showResource(item);
            if (ExternalParam.getInstance().getStatus() == KeyConstants.ClassStatus.CLASS_ING) {
                HashMap<String, String> params = new HashMap<>();
                params.put(PushMessage.PARAM_GROUP_ITEM, groupPosition + "");
                params.put(PushMessage.PARAM_CHILD_ITEM, childPosition + "");
                MyMqttService.publishMessage(PushMessage.COMMAND.OPEN_DOCUMENT,  (List<String>) null, params);
            }
        }
    }

    public void showItem(int groupPosition, int childPosition){
        KnowledgeLessonSample group = mAdapter.getGroup(groupPosition);
        if (childPosition < group.getChildren().size() && group.getChildren().get(childPosition) != null) {
            ResourceModel item = group.getChildren().get(childPosition);
            mLearnCasesContainerFragment.showResource(item);
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeLessonSample>> {

        @Override
        protected List<KnowledgeLessonSample> doInBackground(Void... voids) {
            if (mClassPageType == KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE) {
                return ScopeServer.getInstance().QuerySampleByKnowledge(mKnowledgeId, mKnowledgeStatus);
            } else {
                return ScopeServer.getInstance().QuerySampleByKnowledge(mKnowledgeId, 2);
            }
        }

        @Override
        protected void onPostExecute(List<KnowledgeLessonSample> documentList) {
            if (documentList != null && documentList.size() > 0) {
                mAdapter.setDataList(documentList);
                mExpandableListView.expandGroup(0);
                if (init) {
                    callClickItem(0, 0);
                }
                init = false;
            }
        }
    }

    public interface PagesListener {

        void onFullScreen(boolean isFull);

        void onPageChange(int id);

        void onBack();
    }

}
