package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.utils.AppUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class StudentFragment extends Fragment implements View.OnClickListener {
    private BackListener mListener;
    private View mClassPageView;
    private View mKnowledgePageView;
    private View mStaticsPageView;
    private TextView mClassStartedWarningView;

    private PushMessage mPushMessage;

    public static StudentFragment newInstance(BackListener listener) {
        StudentFragment fragment = new StudentFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(BackListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_student, container, false);

        EventBus.getDefault().register(this);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mClassPageView = view.findViewById(R.id.class_page_view);
        mKnowledgePageView = view.findViewById(R.id.knowledge_page_view);
        mStaticsPageView = view.findViewById(R.id.statics_page_view);
        mClassStartedWarningView = view.findViewById(R.id.class_started_warning);

        mClassPageView.setOnClickListener(this);
        mKnowledgePageView.setOnClickListener(this);
        mStaticsPageView.setOnClickListener(this);

        mClassPageView.setEnabled(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEventBus(PushMessage pushMessage) {
        if (pushMessage != null) {
            mqttListener.messageArrived(pushMessage);
        }
    }

    private MqttListener mqttListener = new MqttListener() {

        @Override
        public void messageArrived(final PushMessage message) {
            switch (message.command) {
                case CLASS_BEGIN: {
                    mPushMessage = message;
                    if (ExternalParam.getInstance().getStatus() == 0) {
                        mClassPageView.setEnabled(true);
                        if (message.parameters != null) {
                            String teacherName = message.parameters.get(PushMessage.TEACHER_NAME);
                            mClassStartedWarningView.setText(getResources().getString(R.string.class_started_warning, teacherName));
                            mClassStartedWarningView.setVisibility(View.VISIBLE);
                        }
                        ExternalParam.getInstance().setStatus(1);
                    }
                    break;
                }
                case CLASS_END: {
//                    classEnd(message.from);
                    break;
                }
                case QUESTIONING: {
                    String examID = message.parameters.get("examID");
                    final String teacherID = message.parameters.get("teacherID");
                    FrameDialog.fullShow(getChildFragmentManager(), ExamFragment.newInstance(teacherID, examID, new ExamFragment.ExamListener() {
                        @Override
                        public void onFinished() {
                            MyMqttService.publishMessage(PushMessage.COMMAND.ANSWER_COMPLETED, teacherID, null);
                        }
                    }));
                    break;
                }
                case OPEN_DOCUMENT: {
                    if (ExternalParam.getInstance().getStatus() == 2) {
                        int childItem = Integer.valueOf(message.parameters.get(PushMessage.PARAM_CHILD_ITEM));
                        int groupItem = Integer.valueOf(message.parameters.get(PushMessage.PARAM_GROUP_ITEM));
//                        showLessonSample(url, ShowDocumentFragment.SYNC_MODE.SLAVE);
                    }
                    break;
                }
                case SERVER_PING: {
                    FrameDialog.show(getChildFragmentManager(), ServerTesterFragment.newInstance());
                    break;
                }
                case PING: {
                    MyMqttService.publishMessage(PushMessage.COMMAND.QUERY_CLASS,  (List<String>) null, null);
                    break;
                }
                case QUERY_STATUS: {
                    if (ExternalParam.getInstance().getStatus() == 2)
                        MyMqttService.publishMessage(PushMessage.COMMAND.ONLINE,  (List<String>) null, null);
                    else
                        MyMqttService.publishMessage(PushMessage.COMMAND.OFFLINE,  (List<String>) null, null);
                    break;
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.class_page_view:
                MyMqttService.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                ExternalParam.getInstance().setStatus(2);
                if (mPushMessage != null && mPushMessage.parameters != null) {
                    String knowledgeId = mPushMessage.parameters.get(PushMessage.KNOWLEDGE_ID);
                    String knowledgeName = mPushMessage.parameters.get(PushMessage.KnowledgePointName);
                    String teacherName = mPushMessage.parameters.get(PushMessage.TEACHER_NAME);
                    LearnCasesActivity.startMe(getActivity(), knowledgeId, knowledgeName, KeyConstants.ClassPageType.STUDENT_CLASS_PAGE,teacherName);
                }
                break;
            case R.id.knowledge_page_view:
                DatasActivity.startMe(getActivity(), DatasActivity.PAGE_ID_DOCUMENTS, true);
                break;
            case R.id.statics_page_view:
                DatasActivity.startMe(getActivity(), DatasActivity.PAGE_ID_STATISTICS, true);
                break;
        }
    }

    public interface BackListener {
        void showBack(boolean show);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
