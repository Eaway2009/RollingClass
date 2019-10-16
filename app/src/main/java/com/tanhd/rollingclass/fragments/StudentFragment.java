package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.rollingclass.base.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassStatusInfo;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeLessonSample;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 学生主界面
 */
public class StudentFragment extends Fragment implements View.OnClickListener {
    private BackListener mListener;
    private View mClassPageView;
    private View mKnowledgePageView;
    private View mStaticsPageView;
    private TextView mClassStartedWarningView;

    private PushMessage mPushMessage;
    private ClassStatusInfo mClassStatusInfo;

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
        new CheckClassTask().execute();
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
                case CLASS_BEGIN: { //上课开始
                    mPushMessage = message;
                    new InitDataTask(message).execute();
                    break;
                }
                case CLASS_END: //下课了
                    mClassPageView.setEnabled(false);
                    MQTT.publishMessage(PushMessage.COMMAND.OFFLINE, message.from, null);
                    ExternalParam.getInstance().setStatus(KeyConstants.ClassLearningStatus.REST);
                    mClassStartedWarningView.setVisibility(View.GONE);
                    break;
                case SERVER_PING: {
                    FrameDialog.show(getChildFragmentManager(), ServerTesterFragment.newInstance());
                    break;
                }
                case QUERY_STATUS: {
                    if (ExternalParam.getInstance().getStatus() == 2)
                        MyMqttService.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                    else
                        MyMqttService.publishMessage(PushMessage.COMMAND.OFFLINE, (List<String>) null, null);
                    break;
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };


    private class InitDataTask extends AsyncTask<Void, Void, ClassStatusInfo> {

        private String mKnowledgeId;

        public InitDataTask(PushMessage message) {
            if (mPushMessage != null && mPushMessage.parameters != null) {
                mKnowledgeId = mPushMessage.parameters.get(PushMessage.KNOWLEDGE_ID);
            }
        }

        @Override
        protected ClassStatusInfo doInBackground(Void... voids) {
            StudentData studentData = (StudentData) ExternalParam.getInstance().getUserData().getUserData();
            return ScopeServer.getInstance().GetKnowledgeStatus(mKnowledgeId, studentData.StudentID);
        }

        @Override
        protected void onPostExecute(ClassStatusInfo classStatusInfo) {
            if (classStatusInfo != null) {
                mClassStatusInfo = classStatusInfo;
                mClassPageView.setEnabled(true);
                String knowledgeId = classStatusInfo.knowledge_id;
                String knowledgeName = classStatusInfo.knowledge_point_name;
                mClassStartedWarningView.setText(getResources().getString(R.string.class_started_warning, classStatusInfo.teacher_name));
                mClassStartedWarningView.setVisibility(View.VISIBLE);
                if (ExternalParam.getInstance().getStatus() == KeyConstants.ClassLearningStatus.REST) {
                    MyMqttService.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                    LearnCasesActivity.startMe(getActivity(), knowledgeId, classStatusInfo.lessonsample_id, classStatusInfo.resource_id, knowledgeName, KeyConstants.ClassPageType.STUDENT_CLASS_PAGE, classStatusInfo.teacher_name);
                    ExternalParam.getInstance().setStatus(KeyConstants.ClassLearningStatus.CLASSING);
                }
            }
        }
    }


    private class CheckClassTask extends AsyncTask<Void, Void, ClassStatusInfo> {

        public CheckClassTask() {
        }

        @Override
        protected ClassStatusInfo doInBackground(Void... voids) {
            StudentData studentData = (StudentData) ExternalParam.getInstance().getUserData().getUserData();
            return ScopeServer.getInstance().GetKnowledgeCourseInfo(studentData.ClassID);
        }

        @Override
        protected void onPostExecute(ClassStatusInfo classStatusInfo) {
            if (classStatusInfo != null) {
                mClassStatusInfo = classStatusInfo;
                mClassPageView.setEnabled(true);
                String knowledgeId = classStatusInfo.knowledge_id;
                String knowledgeName = classStatusInfo.knowledge_point_name;
                mClassStartedWarningView.setText(getResources().getString(R.string.class_started_warning, classStatusInfo.teacher_name));
                mClassStartedWarningView.setVisibility(View.VISIBLE);
                if (ExternalParam.getInstance().getStatus() == KeyConstants.ClassLearningStatus.REST) {
                    MyMqttService.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                    LearnCasesActivity.startMe(getActivity(), knowledgeId, classStatusInfo.lessonsample_id, classStatusInfo.resource_id, knowledgeName, KeyConstants.ClassPageType.STUDENT_CLASS_PAGE, classStatusInfo.teacher_name);
                    ExternalParam.getInstance().setStatus(KeyConstants.ClassLearningStatus.CLASSING);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.class_page_view: //进入课堂
                MyMqttService.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                ExternalParam.getInstance().setStatus(KeyConstants.ClassStatus.CLASS_ING);
                if (mClassStatusInfo != null) {
                    LearnCasesActivity.startMe(getActivity(), mClassStatusInfo.knowledge_id, mClassStatusInfo.lessonsample_id, mClassStatusInfo.resource_id, mClassStatusInfo.knowledge_point_name, KeyConstants.ClassPageType.STUDENT_CLASS_PAGE, mClassStatusInfo.teacher_name);
                }
                break;
            case R.id.knowledge_page_view: //自学
                DatasActivity.startMe(getActivity(), DatasActivity.PAGE_ID_DOCUMENTS, true);
                break;
            case R.id.statics_page_view: //学情
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
