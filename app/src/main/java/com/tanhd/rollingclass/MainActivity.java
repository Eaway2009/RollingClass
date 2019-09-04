package com.tanhd.rollingclass;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import static com.tanhd.library.mqtthttp.MyMqttService.PARAM_CLIENT_ID;
import static com.tanhd.library.mqtthttp.MyMqttService.PARAM_TOPIC;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 1;
    private static Intent mIntent;
    private TopbarView mTopbarView;
    private MediaPlayer mediaPlayer;
    private AlertDialog mNetworkDialog;
    private View mBackButton;
    private Fragment fragment;
    private int refreshData = 1;
    private RefreshTask mRefreshTask;

    private Handler mHandler = new Handler();
    private UserData mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: 从LoginActivity过来");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTopbarView = findViewById(R.id.topbar);
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
                mBackButton.setClickable(false);
                mBackButton.setVisibility(View.INVISIBLE);
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

        mUserData = ExternalParam.getInstance().getUserData();
        if (mUserData == null) {
            Log.i(TAG, "onCreate: on UserData");
            Toast.makeText(getApplicationContext(), "获取用户信息失败, 请重新登录!", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            Log.i(TAG, "onCreate: has UserData");
            new RefreshDataTask(mUserData).execute();
        }
        mTopbarView.setCallback(new TopbarView.Callback() {
            @Override
            public void connect_again() {
                if (ExternalParam.getInstance().getUserData() != null) {
//                    new ConnectMqttTask(ExternalParam.getInstance().getUserData()).execute();
                }
            }
        });

//        String ownerID = mUserData.getOwnerID();
//        Log.i(TAG, "onPostExecute: 初始化MQ");
//        //初始化MQ
//        if (MQTT.getInstance(ownerID, 8080) == null) {
//            Toast.makeText(getApplicationContext(), "连接消息服务器失败, 请重试!", Toast.LENGTH_LONG).show();
////            changeViewsStatus(false);
//            return;
//        }
//        Log.i(TAG, "onPostExecute: 初始化MQ完成");
//
//        boolean flag = MQTT.getInstance().connect();
//        if (!flag) {
//            Toast.makeText(getApplicationContext(), "连接消息服务器失败, 请重试!", Toast.LENGTH_LONG).show();
////            changeViewsStatus(false);
//            return;
//        }
//        Log.i(TAG, "onPostExecute: 连接MQ");
//        MQTT.register(mqttListener);
//        MQTT.getInstance().subscribe();
//
//        if (!mUserData.isTeacher()) {
//            StudentData studentData = (StudentData) mUserData.getUserData();
//            MQTT.getInstance().subscribe(studentData.ClassID);
//        } else {
//            MQTT.getInstance().subscribe();
//        }
        initUserUI();
        SmartPenService.getInstance().init(getApplicationContext());
        SmartPenService.getInstance().tryToConnect();
        startMqttService();
    }


    private void requestPermission() {

        List<String> permissionsList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.PHONE)
                != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission_group.PHONE);

        if (permissionsList.size() == 0) {
            startMqttService();
        } else {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_PERMISSION);
        }
    }

    private void startMqttService() {
        if (mUserData.isTeacher()) {
            startService(MainActivity.this, mUserData.getOwnerID());
        } else {
            StudentData studentData = (StudentData) mUserData.getUserData();
            startService(MainActivity.this, mUserData.getOwnerID(), studentData.ClassID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "请点击<允许>", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }

                startMqttService();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mIntent);
//        MQTT.unregister(mqttListener);
//        MQTT.getInstance().unsubscribe();
//        MQTT.getInstance().disconnect();
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
                case COMMENT_START: {
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

        if (ExternalParam.getInstance().getUserData().isTeacher()) {
            fragment = TeacherFragment.newInstance(new TeacherFragment.BackListener() {
                @Override
                public void showBack(boolean show) {
                    if (show) {
                        mBackButton.setVisibility(View.VISIBLE);
                        mBackButton.setClickable(true);
                    } else {
                        mBackButton.setVisibility(View.INVISIBLE);
                        mBackButton.setClickable(false);
                    }
                }
            });
        } else {
            final StudentData studentData = (StudentData) ExternalParam.getInstance().getUserData().getUserData();
            mHandler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    getRefreshTask(studentData.Token).execute();
                }
            }, 10000);

            fragment = new StudentFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }

    /**
     * 开启服务
     */
    public static void startService(Context mContext, String clientId) {
        Log.i(TAG, "startService:" + clientId);
        mIntent = new Intent(mContext, MyMqttService.class);
        mIntent.putExtra(PARAM_CLIENT_ID, clientId);
        mContext.startService(mIntent);
    }

    /**
     * 开启服务
     */
    public static void startService(Context mContext, String clientId, String topic) {
        Log.i(TAG, "startService:" + clientId + "  " + topic);
        mIntent = new Intent(mContext, MyMqttService.class);
        mIntent.putExtra(PARAM_CLIENT_ID, clientId);
        mIntent.putExtra(PARAM_TOPIC, topic);
        mContext.startService(mIntent);
    }

    private class RefreshDataTask extends AsyncTask<Void, Void, UserData> {

        private UserData mUserData;

        public RefreshDataTask(UserData userData) {
            mUserData = userData;
        }

        @Override
        protected UserData doInBackground(Void... voids) {
            String ownerID = mUserData.getOwnerID();
            Database.getInstance(getApplicationContext(), ownerID);

            ScopeServer.getInstance().initUserData(mUserData);
            return mUserData;
        }

        @Override
        protected void onPostExecute(UserData userData) {
            super.onPostExecute(userData);
        }
    }

    private RefreshTask getRefreshTask(String token) {
        if (mRefreshTask == null) {
            mRefreshTask = new RefreshTask(token);
        }
        return mRefreshTask;
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void> {

        private String mUserToken;

        public RefreshTask(String token) {
            mUserToken = token;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ScopeServer.getInstance().refreshExpiration(mUserToken);
            return null;
        }
    }


}
