package com.tanhd.rollingclass.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.rollingclass.base.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.ExamFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.fragments.ServerTesterFragment;
import com.tanhd.rollingclass.fragments.pages.ClassAnsweringFragment;
import com.tanhd.rollingclass.fragments.pages.LearnCasesFragment;
import com.tanhd.rollingclass.fragments.pages.StudentResponderFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.TopbarView;

import java.util.HashMap;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 学案界面
 */
public class LearnCasesActivity extends BaseActivity {

    public static final String PARAM_CLASS_STUDENT_PAGE = "PARAM_CLASS_STUDENT_PAGE";
    public static final String PARAM_TEACHING_MATERIAL_ID = "PARAM_TEACHING_MATERIAL_ID";
    public static final String PARAM_RESOURCE_MODEL = "PARAM_RESOURCE_MODEL";
    public static final String PARAM_KNOWLEDGE_ID = "PARAM_KNOWLEDGE_ID";
    public static final String PARAM_KNOWLEDGE_NAME = "PARAM_KNOWLEDGE_NAME";
    public static final String PARAM_LESSON_SAMPLE_ID = "PARAM_LESSON_SAMPLE_ID";
    public static final String PARAM_RESOURCE_ID = "PARAM_RESOURCE_ID";
    public static final String PARAM_LESSON_SAMPLE_NAME = "PARAM_LESSON_SAMPLE_NAME";
    public static final String PARAM_TEACHER_NAME = "PARAM_TEACHER_NAME";
    public static final String PARAM_SHOW_NO = "PARAM_SHOW_NO";
    public static final String PARAM_PPT_INDEX = "PARAM_PPT_INDEX";
    public static final String PARAM_IS_SUBMIT_ANSWER = "PARAM_IS_SUBMIT_ANSWER";

    private LearnCasesFragment mLearnCasesFragment;

    private TopbarView mTopbarView;
    private View mBackButton;
    private String mKnowledgeName;
    private String mKnowledgeId;
    private int mPageType;
    private UserData mUserData;
    private String mTeacherName;
    private String mTeachingMaterialId;
    private String mLessonSampleId;
    private String mResourceId;
    private int mPptIndex;
    private PowerManager.WakeLock mWakeLock;

    /**
     * 老师端
     *
     * @param context
     * @param knowledgeId
     * @param knowledgeName
     * @param teachingMaterialId
     * @param classPageType
     */
    public static void startMe(Fragment context, String knowledgeId, String knowledgeName, String teachingMaterialId, int classPageType) {
        Intent intent = new Intent();
        intent.setClass(context.getActivity(), LearnCasesActivity.class);
        intent.putExtra(PARAM_KNOWLEDGE_ID, knowledgeId);
        intent.putExtra(PARAM_KNOWLEDGE_NAME, knowledgeName);
        intent.putExtra(PARAM_CLASS_STUDENT_PAGE, classPageType);
        intent.putExtra(PARAM_TEACHING_MATERIAL_ID, teachingMaterialId);
        context.startActivity(intent);
    }

    /**
     * 学生端
     *
     * @param context
     * @param knowledgeId
     * @param knowledgeName
     * @param classPageType
     * @param teacherName
     */
    public static void startMe(Activity context, String knowledgeId, String knowledgeName, int classPageType, String teacherName) {
        Intent intent = new Intent(context, LearnCasesActivity.class);
        intent.putExtra(PARAM_KNOWLEDGE_ID, knowledgeId);
        intent.putExtra(PARAM_KNOWLEDGE_NAME, knowledgeName);
        intent.putExtra(PARAM_TEACHER_NAME, teacherName);
        intent.putExtra(PARAM_CLASS_STUDENT_PAGE, classPageType);
        context.startActivity(intent);
    }

    /**
     * 学生端
     *
     * @param context
     * @param knowledgeId
     * @param knowledgeName
     * @param classPageType
     * @param teacherName
     */
    public static void startMe(Activity context, String knowledgeId, String lessonSampleId, String resourceId, String knowledgeName, int classPageType, String teacherName) {
        Intent intent = new Intent(context, LearnCasesActivity.class);
        intent.putExtra(PARAM_KNOWLEDGE_ID, knowledgeId);
        intent.putExtra(PARAM_LESSON_SAMPLE_ID, lessonSampleId);
        intent.putExtra(PARAM_RESOURCE_ID, resourceId);
        intent.putExtra(PARAM_KNOWLEDGE_NAME, knowledgeName);
        intent.putExtra(PARAM_TEACHER_NAME, teacherName);
        intent.putExtra(PARAM_CLASS_STUDENT_PAGE, classPageType);
        context.startActivity(intent);
    }

    /**
     * 学生端
     *
     * @param context
     * @param knowledgeId
     * @param knowledgeName
     * @param classPageType
     * @param teacherName
     */
    public static void startMe(Activity context, String knowledgeId, String lessonSampleId, String resourceId, String knowledgeName, int classPageType, String teacherName, int pptIndex) {
        Intent intent = new Intent(context, LearnCasesActivity.class);
        intent.putExtra(PARAM_KNOWLEDGE_ID, knowledgeId);
        intent.putExtra(PARAM_LESSON_SAMPLE_ID, lessonSampleId);
        intent.putExtra(PARAM_RESOURCE_ID, resourceId);
        intent.putExtra(PARAM_KNOWLEDGE_NAME, knowledgeName);
        intent.putExtra(PARAM_TEACHER_NAME, teacherName);
        intent.putExtra(PARAM_CLASS_STUDENT_PAGE, classPageType);
        intent.putExtra(PARAM_PPT_INDEX, pptIndex);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        initParams();
        initViews();
        initFragment();
        initMqtt();
    }

    private void initMqtt() {
        mUserData = ExternalParam.getInstance().getUserData();

        if (mUserData.isTeacher()) {
            MyMqttService.startService(LearnCasesActivity.this, mConnection, mUserData.getOwnerID());
        } else {
            StudentData studentData = (StudentData) mUserData.getUserData();
            MyMqttService.startService(LearnCasesActivity.this, mConnection, mUserData.getOwnerID(), studentData.ClassID);
        }
    }

    private void initParams() {
        mPageType = getIntent().getIntExtra(PARAM_CLASS_STUDENT_PAGE, KeyConstants.ClassPageType.TEACHER_CLASS_PAGE);
        mKnowledgeId = getIntent().getStringExtra(PARAM_KNOWLEDGE_ID);
        mKnowledgeName = getIntent().getStringExtra(PARAM_KNOWLEDGE_NAME);
        mTeachingMaterialId = getIntent().getStringExtra(PARAM_TEACHING_MATERIAL_ID);
        mTeacherName = getIntent().getStringExtra(PARAM_TEACHER_NAME);
        mLessonSampleId = getIntent().getStringExtra(PARAM_LESSON_SAMPLE_ID);
        mResourceId = getIntent().getStringExtra(PARAM_RESOURCE_ID);
        mPptIndex = getIntent().getIntExtra(PARAM_PPT_INDEX, 0);
    }

    private void initFragment() {
        mLearnCasesFragment = LearnCasesFragment.newInstance(mKnowledgeId, mKnowledgeName, mTeachingMaterialId, mPageType, mTeacherName, new LearnCasesFragment.PagesListener() {
            @Override
            public void onFullScreen(boolean isFull) {
                mTopbarView.setVisibility(isFull == true ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onPageChange(int id) {

            }

            @Override
            public void onBack() {
                onFinish();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mLearnCasesFragment).commit();
        if (!TextUtils.isEmpty(mLessonSampleId)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPptIndex > 0) {
                        mLearnCasesFragment.showItem(mLessonSampleId, mResourceId, mPptIndex);
                    } else {
                        mLearnCasesFragment.showItem(mLessonSampleId, mResourceId);
                    }
                }
            }, 500);
        }
    }

    private void initViews() {

        setContentView(R.layout.activity_learn_cases);
        mTopbarView = findViewById(R.id.topbar);
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.show(getSupportFragmentManager(), InBoxFragment.newInstance(new InBoxFragment.SelectorListener() {
                    @Override
                    public void onMessageSelected(Message message) {
                        mTopbarView.refreshMessageCount();
                        if (message != null)
                            openMessage(message);
                    }
                }));
            }
        });
        mTopbarView.setCallback(new TopbarView.Callback() {
            @Override
            public void connect_again() {
                if (ExternalParam.getInstance().getUserData() != null) {
//                    new ConnectMqttTask(ExternalParam.getInstance().getUserData()).execute();
                }
            }

            @Override
            public void showPage(int modulePageId) {
                setResult(RESULT_OK);
                finish();
                MainActivity.startMe(LearnCasesActivity.this, modulePageId);
            }
        });
        // 获取WakeLock对象
        PowerManager powerManager= (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "keep_screen_on_tag");

    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }

    //serviceMessenger表示的是Service端的Messenger，其内部指向了MyService的ServiceHandler实例
    //可以用serviceMessenger向MyService发送消息
    private Messenger serviceMessenger = null;

    //clientMessenger是客户端自身的Messenger，内部指向了ClientHandler的实例
    //MyService可以通过Message的replyTo得到clientMessenger，从而MyService可以向客户端发送消息，
    //并由ClientHandler接收并处理来自于Service的消息
    private Messenger clientMessenger = new Messenger(new ClientHandler());

    //客户端用ClientHandler接收并处理来自于Service的消息
    private class ClientHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            Log.i("DemoLog", "ClientHandler -> handleMessage");
            if (msg.what == RECEIVE_MESSAGE_CODE) {
                PushMessage pushMessage = (PushMessage) msg.obj;
                if (pushMessage != null) {
                    ToastUtil.show(getString(R.string.toast_receive_two) + pushMessage.toString());
                    Log.i("DemoLog", "客户端收到Service的消息: " + pushMessage.toString());
                }
            }
        }
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
                case OFFLINE:
                case ONLINE:
                    if (mUserData.isTeacher()) {
                        if (ExternalParam.getInstance().getStatus() == 0)
                            return;

                        ClassData classData = ExternalParam.getInstance().getClassData();
                        if (classData == null)
                            return;

                        classData.setStudentState(message.from, (message.command == PushMessage.COMMAND.ONLINE ? 1 : 0));
                    }
                    break;
                case CLASS_END:
                    finish();
                    break;
                case OPEN_DOCUMENT: { //切换学案
                    if (ExternalParam.getInstance().getStatus() == 2 && !mUserData.isTeacher()) {
                        String lesson_sample_id = message.parameters.get(PushMessage.PARAM_LESSON_SAMPLE_ID);
                        String resourceId = message.parameters.get(PushMessage.PARAM_RESOURCE_ID);
//                        showLessonSample(url, ShowDocumentFragment.SYNC_MODE.SLAVE);
                        mLearnCasesFragment.showItem(lesson_sample_id, resourceId);
                    }
                    break;
                }
                case REFRESH_DATA: {
                    if (ExternalParam.getInstance().getStatus() == 2 && !mUserData.isTeacher()) {
                        mLearnCasesFragment.refreshData();
                    }
                    break;
                }
                case QUESTIONING: { //发起提问
                    if (ExternalParam.getInstance().getStatus() == 2 && !ExternalParam.getInstance().getUserData().isTeacher()) {
                        String examID = message.parameters.get("examID");
                        final String teacherID = message.parameters.get("teacherID");
                        FrameDialog.show(getSupportFragmentManager(), ClassAnsweringFragment.getInstance(mPageType, mKnowledgeId, mKnowledgeName, teacherID, examID, null));
                    }
                    break;
                }
                case RESPONDER: { //抢答
                    if (ExternalParam.getInstance().getStatus() == 2 && !ExternalParam.getInstance().getUserData().isTeacher()) {
                        String examID = message.parameters.get("examID");
                        final String teacherID = message.parameters.get("teacherID");

                        FrameDialog.show(getSupportFragmentManager(), StudentResponderFragment.newInstance(examID, new StudentResponderFragment.ExamListener() {
                            @Override
                            public void onFinished(String answer) {
                                message.parameters.put("answer", answer);
                                StudentData studentData = (StudentData) ExternalParam.getInstance().getUserData().getUserData();
                                message.parameters.put("askName", studentData.Username);
                                MyMqttService.publishMessage(PushMessage.COMMAND.ANSWER_COMPLETED, teacherID, message.parameters);
                            }
                        }), 0.5d);
                    }
                    break;
                }
                case SERVER_PING: {
                    FrameDialog.show(getSupportFragmentManager(), ServerTesterFragment.newInstance());
                    break;
                }
                case SHOW_RIGHT_ANSWER: {
                    if (ExternalParam.getInstance().getStatus() == KeyConstants.ClassLearningStatus.CLASSING && !mUserData.isTeacher() && mLearnCasesFragment != null) {
                        mLearnCasesFragment.showAnswers(true);
                    }
                    break;
                }
                case QUERY_CLASS:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    notifyEnterClass(message.from);
                    break;
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

    private void notifyEnterClass(String studentID) {
        ClassData classData = ExternalParam.getInstance().getClassData();

        //通知学生端打开学案
        TeacherData teacherData = (TeacherData) ExternalParam.getInstance().getUserData().getUserData();
        HashMap<String, String> params = new HashMap<>();
        params.put(PushMessage.ENTER_CLASS, "1");
        params.put(PushMessage.CLASS_NAME, classData.ClassName);
        params.put(PushMessage.SUBJECT_NAME, AppUtils.getSubjectNameByCode(teacherData.SubjectCode));
        params.put(PushMessage.TEACHER_NAME, teacherData.Username);
        params.put(PushMessage.KnowledgePointName, mKnowledgeName);
        params.put(PushMessage.KNOWLEDGE_ID, mKnowledgeId);
        params.put(PushMessage.LESSON_SAMPLE_NAME, mKnowledgeName);
//        params.put("UrlContent", mKnowledgeDetailMessage.UrlContent);
        MyMqttService.publishMessage(PushMessage.COMMAND.CLASS_BEGIN, studentID, params);
    }

    private boolean isBound = false;
    private static final int SEND_MESSAGE_CODE = 0x0001;
    private static final int RECEIVE_MESSAGE_CODE = 0x0002;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            //客户端与Service建立连接
            Log.i("DemoLog", "客户端 onServiceConnected");

            //我们可以通过从Service的onBind方法中返回的IBinder初始化一个指向Service端的Messenger
            serviceMessenger = new Messenger(binder);
            isBound = true;

            android.os.Message msg = android.os.Message.obtain();
            msg.what = SEND_MESSAGE_CODE;

            //此处跨进程Message通信不能将msg.obj设置为non-Parcelable的对象，应该使用Bundle
            //msg.obj = "你好，MyService，我是客户端";
            Bundle data = new Bundle();
            data.putString("msg", "你好，MyService，我是客户端");
            msg.setData(data);

            //需要将Message的replyTo设置为客户端的clientMessenger，
            //以便Service可以通过它向客户端发送消息
            msg.replyTo = clientMessenger;
            try {
                Log.i("DemoLog", "客户端向service发送信息");
                serviceMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.i("DemoLog", "客户端向service发送消息失败: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //客户端与Service失去连接
            serviceMessenger = null;
            isBound = false;
            Log.i("DemoLog", "客户端 onServiceDisconnected");
        }
    };

    @Override
    public void onBackPressed() {
        onFinish();
    }

    private void onFinish() {
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData.isTeacher() && ExternalParam.getInstance().getStatus() == KeyConstants.ClassLearningStatus.CLASSING) {
            MyMqttService.publishMessage(PushMessage.COMMAND.CLASS_END, (List<String>) null, null);

            ClassData classData = ExternalParam.getInstance().getClassData();
            ScopeServer.getInstance().UpdateKnowledgeStatus(KeyConstants.ClassLearningStatus.REST, classData.ClassID, mKnowledgeId, new RequestCallback() {
                @Override
                public void onProgress(boolean b) {

                }

                @Override
                public void onResponse(String body) {

                }

                @Override
                public void onError(String code, String message) {

                }
            });
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    //安卓重写返回键事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if (ExternalParam.getInstance().getStatus() == 2 && !ExternalParam.getInstance().getUserData().isTeacher()) {

            } else {
                finish();
            }
        }
        return true;
    }
}
