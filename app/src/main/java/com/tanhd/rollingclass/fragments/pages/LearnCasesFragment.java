package com.tanhd.rollingclass.fragments.pages;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.tanhd.rollingclass.fragments.ClassBeginFragment;
import com.tanhd.rollingclass.fragments.ClassSelectorFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.ShowDocumentFragment;
import com.tanhd.rollingclass.fragments.WaitAnswerFragment;
import com.tanhd.rollingclass.fragments.kowledge.KnowledgeEditingFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.ClassStudentsAdapter;
import com.tanhd.rollingclass.views.LessonItemAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LearnCasesFragment extends Fragment implements OnClickListener, ExpandableListView.OnChildClickListener {

    private RelativeLayout mRlInnerTitle;
    private RelativeLayout mRlMenuLayout;
    private LinearLayout mLlMenuContainer;
    private TextView mTvInsertResource;
    private TextView mTvExerciseResult;
    private TextView mTvClassBegin;
    private PagesListener mListener;

    private LearnCasesContainerFragment mLearnCasesContainerFragment;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;
    private TextView mKnowledgeNameTextView;
    private ExpandableListView mExpandableListView;
    private LessonItemAdapter mAdapter;

    public static LearnCasesFragment newInstance(KnowledgeDetailMessage data, LearnCasesFragment.PagesListener listener) {
        Bundle args = new Bundle();
        LearnCasesFragment page = new LearnCasesFragment();
        args.putSerializable(LearnCasesActivity.PARAM_KNOWLEDGE_DETAIL_MESSAGE, data);
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
        mKnowledgeDetailMessage = (KnowledgeDetailMessage) args.getSerializable(LearnCasesActivity.PARAM_KNOWLEDGE_DETAIL_MESSAGE);
    }

    private void initViews(View view) {
        mKnowledgeNameTextView = view.findViewById(R.id.knowledge_name_tv);
        mRlMenuLayout = view.findViewById(R.id.rl_cases_menu);
        mRlInnerTitle = view.findViewById(R.id.rl_inner_title);
        mLlMenuContainer = view.findViewById(R.id.ll_menu_container);
        mTvInsertResource = view.findViewById(R.id.tv_insert_resource);
        mTvExerciseResult = view.findViewById(R.id.tv_exercise_result);
        mTvClassBegin = view.findViewById(R.id.tv_class_begin);
        initListViews(view);

        mTvInsertResource.setOnClickListener(this);
        mTvExerciseResult.setOnClickListener(this);
        mTvClassBegin.setOnClickListener(this);
        view.findViewById(R.id.back_button).setOnClickListener(this);

        mLearnCasesContainerFragment = LearnCasesContainerFragment.newInstance(1, mPagesListener);
        getFragmentManager().beginTransaction().replace(R.id.content_layout, mLearnCasesContainerFragment).commit();
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
        if (mKnowledgeDetailMessage != null) {
            mKnowledgeNameTextView.setText(mKnowledgeDetailMessage.knowledge_point_name);
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
            case R.id.tv_class_begin:
                FrameDialog.showLittleDialog(getChildFragmentManager(), ClassSelectorFragment.newInstance(new ClassSelectorFragment.OnClassListener() {
                    @Override
                    public void onClassSelected(ClassData classData) {
//                        MQTT.getInstance().subscribe(classData.ClassID);
                        MyMqttService.subscribe(classData.ClassID);
                        ExternalParam.getInstance().setStatus(2);
                        classData.resetStudentState(0);
                        ExternalParam.getInstance().setClassData(classData);
                        notifyEnterClass(null);
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
                case QUERY_CLASS:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    notifyEnterClass(message.from);
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
        params.put("EnterClass", "1");
        params.put("ClassName", classData.ClassName);
        params.put("SubjectName", AppUtils.getSubjectNameByCode(teacherData.SubjectCode));
        params.put("TeacherName", teacherData.Username);
        params.put("KnowledgePointName", mKnowledgeDetailMessage.knowledge_point_name);
        params.put("knowledge_id", mKnowledgeDetailMessage.knowledge_id);
        params.put("LessonSampleName", mKnowledgeDetailMessage.knowledge_point_name);
//        params.put("UrlContent", mKnowledgeDetailMessage.UrlContent);
        MyMqttService.publishMessage(PushMessage.COMMAND.CLASS_BEGIN, studentID, params);
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if(groupPosition<mAdapter.getGroupCount()&&mAdapter.getGroup(groupPosition)!=null){
            KnowledgeLessonSample group = mAdapter.getGroup(groupPosition);
            if(childPosition<group.getChildren().size()&&group.getChildren().get(childPosition)!=null){
                ResourceModel item = group.getChildren().get(childPosition);
                mLearnCasesContainerFragment.showResource(item);
            }
        }
        return false;
    }


    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeLessonSample>> {

        @Override
        protected List<KnowledgeLessonSample> doInBackground(Void... voids) {
            return ScopeServer.getInstance().QuerySampleByKnowledge(mKnowledgeDetailMessage.knowledge_id, 2);
        }

        @Override
        protected void onPostExecute(List<KnowledgeLessonSample> documentList) {
            if (documentList != null && documentList.size() > 0) {
                mAdapter.setDataList(documentList);
            }
        }
    }

    public interface PagesListener {

        void onFullScreen(boolean isFull);

        void onPageChange(int id);

        void onBack();
    }

}
