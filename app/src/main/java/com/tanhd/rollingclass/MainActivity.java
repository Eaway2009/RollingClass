package com.tanhd.rollingclass;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.library.smartpen.SmartPenService;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.fragments.ServerTesterFragment;
import com.tanhd.rollingclass.fragments.ShowAnswerCommentFragment;
import com.tanhd.rollingclass.fragments.StudentFragment;
import com.tanhd.rollingclass.fragments.TeacherFragment;
import com.tanhd.rollingclass.fragments.pages.CommentAnswerPage;
import com.tanhd.rollingclass.fragments.pages.ShowCommentPage;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.GlobalWork;
import com.tanhd.rollingclass.views.TopbarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TopbarView mTopbarView;
    private MediaPlayer mediaPlayer;
    private AlertDialog mNetworkDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTopbarView = findViewById(R.id.topbar);
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

        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData == null) {
            Toast.makeText(getApplicationContext(), "获取用户信息失败, 请重新登录!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (MQTT.getInstance() == null ) {
            MQTT.getInstance(userData.getOwnerID(), 8080);
        }
        MQTT.register(mqttListener);
        MQTT.getInstance().subscribe();

        if (!userData.isTeacher()) {
            StudentData studentData = (StudentData) userData.getUserData();
            MQTT.getInstance().subscribe(studentData.ClassID);
        } else {
            MQTT.getInstance().subscribe();
        }

        initUserUI();
        SmartPenService.getInstance().init(getApplicationContext());
        SmartPenService.getInstance().tryToConnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MQTT.unregister(mqttListener);
        MQTT.getInstance().unsubscribe();
        MQTT.getInstance().disconnect();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case MESSAGE: {
                    Map<String, String> params = message.parameters;
                    int type = Integer.valueOf(params.get("type"));
                    MSG_TYPE msgType = MSG_TYPE.values()[type];
                    if (msgType == MSG_TYPE.COMMENT_RESULT) {
                        GlobalWork work = new GlobalWork(getApplicationContext(), getSupportFragmentManager());
                        work.dealCommentResult(params.get("Question"), params.get("Answer"));
                    } else if (msgType == MSG_TYPE.COMMENT_REQUEST) {
                        GlobalWork work = new GlobalWork(getApplicationContext(), getSupportFragmentManager());
                        work.dealCommentRequest(params.get("Question"), params.get("Answer"));
                    } else {
                        Database.getInstance().newMessage(message.from, msgType, params.get("content"));
                        mTopbarView.refreshMessageCount();
                    }

                    AppUtils.playBeepSoundAndVibrate(getApplicationContext(), null);
                    break;
                }
                case PING: {
                    MQTT.publishMessage(PushMessage.COMMAND.PING_OK, message.from, null);
                    break;
                }
                case COMMENT_START:{
                    String question = message.parameters.get("Question");
                    String answer = message.parameters.get("Answer");
                    QuestionData questionData = new QuestionData();
                    questionData.parse(questionData, question);
                    AnswerData answerData = new AnswerData();
                    answerData.parse(answerData, answer);

                    FrameDialog.fullShow(getSupportFragmentManager(), ShowCommentPage.newInstance(questionData, answerData));
                    break;
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {
            if (flag) {
                if (mNetworkDialog != null)
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("网络异常")
                        .setMessage("网络连接失败, 正在进行重新连接, 请稍后...")
                        .setPositiveButton("关闭", null)
                        .setCancelable(false)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mNetworkDialog = null;
                            }
                        });
                mNetworkDialog = builder.create();
                mNetworkDialog.show();
            } else {
                if (mNetworkDialog != null && mNetworkDialog.isShowing()) {
                    mNetworkDialog.dismiss();
                    mNetworkDialog = null;
                }
            }
        }
    };


    private void initUserUI() {
        if (MQTT.getInstance() == null) {
            return;
        }

        Fragment fragment;
        if (ExternalParam.getInstance().getUserData().isTeacher()) {
            fragment = new TeacherFragment();
        } else {
            fragment = new StudentFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }

}
