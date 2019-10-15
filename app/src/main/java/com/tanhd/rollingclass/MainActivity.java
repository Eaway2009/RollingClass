package com.tanhd.rollingclass;

import android.app.Activity;
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
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.rollingclass.base.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.library.smartpen.SmartPenService;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.fragments.StudentFragment;
import com.tanhd.rollingclass.fragments.TeacherFragment;
import com.tanhd.rollingclass.fragments.UserInfoFragment;
import com.tanhd.rollingclass.fragments.pages.ShowCommentPage;
import com.tanhd.rollingclass.fragments.user.PasswordSettingFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.GlobalWork;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.TopbarView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final String PAGE_ID = "PAGE_ID";
    private static final int REQUEST_PERMISSION = 1;
    public static final int MODULE_ID_MAIN_PAGE = 1;
    public static final int MODULE_ID_SETTING_PAGE = 2;
    public static final int MODULE_ID_USER_PAGE = 3;
    private static final int FRAGMENT_LAYOUT_ID = R.id.framelayout;

    private TopbarView mTopbarView;
    private MediaPlayer mediaPlayer;
    private AlertDialog mNetworkDialog;
    private View mBackButton;
    private int refreshData = 1;
    private RefreshTask mRefreshTask;

    private Handler mHandler = new Handler();
    private UserData mUserData;
    private static MainActivity instance;
    private UserInfoFragment mUserInfoFragment;
    private int mPageId = MODULE_ID_MAIN_PAGE;

    public static MainActivity getInstance() {
        return instance;
    }

    private int mCurrentShowModuleId;
    private int mLastShowModuleId;

    private Fragment mMainFragment;
    private Fragment mModuleFragment;
    private PasswordSettingFragment mSettingFragment;

    public static void startMe(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void startMe(Context context, int pageId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(PAGE_ID, pageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: 从LoginActivity过来");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        EventBus.getDefault().register(this);

        mTopbarView = findViewById(R.id.topbar);
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(FRAGMENT_LAYOUT_ID, mMainFragment).commit();
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
            ToastUtil.show(R.string.toast_token_lose);
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

            @Override
            public void showPage(int modulePageId) {
                showModulePage(modulePageId);
            }
        });
        initPageUI();
        SmartPenService.getInstance().init(getApplicationContext());
        SmartPenService.getInstance().tryToConnect();
        startMqttService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            showModulePage(MODULE_ID_MAIN_PAGE);
        }
    }

    private void startMqttService() {
        if (mUserData.isTeacher()) {
            MyMqttService.startService(MainActivity.this, mConnection, mUserData.getOwnerID());
        } else {
            StudentData studentData = (StudentData) mUserData.getUserData();
            MyMqttService.startService(MainActivity.this, mConnection, mUserData.getOwnerID(), studentData.ClassID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        ToastUtil.show(getResources().getString(R.string.toast_allow));
                        finish();
                        return;
                    }
                }

                startMqttService();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initParams();
        initPageUI();
    }

    private void initParams() {
        mPageId = getIntent().getIntExtra(PAGE_ID, MODULE_ID_MAIN_PAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (mCurrentShowModuleId == MODULE_ID_SETTING_PAGE) {
            showModulePage(mLastShowModuleId);
        } else {
            moveTaskToBack(true);
        }
    }

    private void initPageUI() {
        showModulePage(mPageId);
    }

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showModulePage(int moduleId) {
        if (mCurrentShowModuleId == moduleId) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (moduleId == MODULE_ID_MAIN_PAGE) { //学案
            if (mMainFragment == null) {
                if (ExternalParam.getInstance().getUserData().isTeacher()) {
                    mMainFragment = TeacherFragment.newInstance(new TeacherFragment.BackListener() {
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

                    mMainFragment = new StudentFragment();
                }
                transaction.add(FRAGMENT_LAYOUT_ID, mMainFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mMainFragment;
        } else if (moduleId == MODULE_ID_SETTING_PAGE) {
            if (mSettingFragment == null) {
                mSettingFragment = PasswordSettingFragment.newInstance(new PasswordSettingFragment.Callback() {
                    @Override
                    public void onBack() {
                        showModulePage(MODULE_ID_MAIN_PAGE);
                    }
                });
                transaction.add(FRAGMENT_LAYOUT_ID, mSettingFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mSettingFragment;
        } else if (moduleId == MODULE_ID_USER_PAGE) {
            if (mUserInfoFragment == null) {
                mUserInfoFragment = UserInfoFragment.getInstance(new UserInfoFragment.Callback() {
                    @Override
                    public void onBack() {
                        showModulePage(MODULE_ID_MAIN_PAGE);
                    }
                });
                transaction.add(FRAGMENT_LAYOUT_ID, mUserInfoFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mUserInfoFragment;
        }
        transaction.show(mModuleFragment);
        transaction.commitAllowingStateLoss();

        mLastShowModuleId = mCurrentShowModuleId;
        mCurrentShowModuleId = moduleId;
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
                    QuestionModel questionData = new QuestionModel();
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
                        .setTitle(getResources().getString(R.string.dialog_net_err))
                        .setMessage(getResources().getString(R.string.dialog_reconnect_hint))
                        .setPositiveButton(getResources().getString(R.string.lbl_close), null)
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
