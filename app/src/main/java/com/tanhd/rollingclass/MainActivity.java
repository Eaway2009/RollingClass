package com.tanhd.rollingclass;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.tanhd.rollingclass.fragments.StudentFragment;
import com.tanhd.rollingclass.fragments.TeacherFragment;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PERMISSION = 1;

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

        EventBus.getDefault().register(this);

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
        initUserUI();
        SmartPenService.getInstance().init(getApplicationContext());
        SmartPenService.getInstance().tryToConnect();
        startMqttService();
    }

    private void startMqttService() {
        if (mUserData.isTeacher()) {
            MyMqttService.startService(MainActivity.this, mConnection, mUserData.getOwnerID());
        } else {
            StudentData studentData = (StudentData) mUserData.getUserData();
            MyMqttService.startService(MainActivity.this, mConnection, mUserData.getOwnerID(), studentData.ClassID);
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!MyMqttService.StartedNormal){
                    MyMqttService.dis
                }
            }
        }, 5000);
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
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

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
                    Toast.makeText(MainActivity.this, "收到2：" + pushMessage.toString(), Toast.LENGTH_SHORT).show();
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
                    MyMqttService.publishMessage(PushMessage.COMMAND.PING_OK, message.from, null);
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
}
