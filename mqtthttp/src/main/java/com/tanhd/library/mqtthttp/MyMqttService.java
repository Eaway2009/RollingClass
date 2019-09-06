package com.tanhd.library.mqtthttp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Author       wildma
 * Github       https://github.com/wildma
 * CreateDate   2018/11/08
 * Desc         ${MQTT服务}
 */

public class MyMqttService extends Service {
    public static final String PARAM_CLIENT_ID = "PARAM_CLIENT_ID";
    public static final String PARAM_TOPIC = "PARAM_TOPIC";

    public static final String TAG = MyMqttService.class.getSimpleName();
    private static MqttAndroidClient mqttAndroidClient;
    private MqttConnectOptions mMqttConnectOptions;

    private static final String HOST = "tcp://mdm.sea-ai.com:1234";
    public String USERNAME = "admin";//用户名
    public String PASSWORD = "admin";//密码
    //    public static String PUBLISH_TOPIC = "tourist_enter";//发布主题
    public static String RESPONSE_TOPIC = "message_arrived";//响应主题

    private static final String PREF_CLIENT_ID = "GID_";
    private static final String PREF_TOPIC = "MQTT_QHMDM/";

    private String mClassId;
    private static HashSet<String> mTopicNames = new HashSet<>();
    private static String mClientId;
    public static String CLIENTID;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStart:");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("DemoLog", "MyServivce -> onBind");
        //获取Service自身Messenger所对应的IBinder，并将其发送共享给所有客户端
        mClientId = intent.getStringExtra(PARAM_CLIENT_ID);
        mClassId = intent.getStringExtra(PARAM_TOPIC);
        CLIENTID = Utils.getDeviceId();//客户端ID，一般以客户端唯一标识符表示，这里用设备序列号表示
        init(mClientId);
        return serviceMessenger.getBinder();
    }

    public static void publishMessage(PushMessage.COMMAND command, String to, Map<String, String> data) {
        if (to != null)
            publishMessage(command, Arrays.asList(new String[]{to}), data, null);
        else
            publishMessage(command, (List<String>) null, data, null);
    }

    public static void publishMessage(PushMessage.COMMAND command, String to, Map<String, String> data, Object objectJson) {
        if (to != null)
            publishMessage(command, Arrays.asList(new String[]{to}), data, objectJson);
        else
            publishMessage(command, (List<String>) null, data, objectJson);
    }

    public static void publishMessage(PushMessage.COMMAND command, List<String> to, Map<String, String> data, Object objectJson) {
        PushMessage message = new PushMessage();
        message.from = mClientId;
        message.command = command;
        message.to = to;
        message.parameters = data;
        message.objectJson = objectJson;

        String text = message.toString();

        if (to == null) {
            for (String topic : mTopicNames) {
                if (topic.equals(message.from))
                    continue;
                publish(topic, 1, text);
            }
        } else {
            for (String topic : to) {
                publish(topic, 1, text);
            }
        }
    }

    /**
     * 发布 （模拟其他客户端发布消息）
     *
     * @param message 消息
     * @param topic
     */
    private static void publish(String topic, int qos, String message) {
        Boolean retained = false;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos, retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static boolean subscribe(String topicName) {
        String topic = PREF_TOPIC + topicName;
        if (mTopicNames.contains(topic))
            return true;

        return subscribe(topic, 1);
    }

    private static boolean subscribe(String topic, int qos) {
        boolean flag = false;
        if (mqttAndroidClient.isConnected()) {
            try {
                mqttAndroidClient.subscribe(topic, qos);
                mTopicNames.add(topic);
                flag = true;
            } catch (MqttException e) {
                Log.e(TAG, "subscribe: " + e.getMessage());
            }
        }
        Log.i(TAG, "subscribe: " + flag + " topic:" + topic);
        return flag;
    }

    /**
     * 响应 （收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等）
     *
     * @param message 消息
     */
    public void response(String message) {
        String topic = RESPONSE_TOPIC;
        Integer qos = 2;
        Boolean retained = false;
        try {
            //参数分别为：主题、消息的字节数组、服务质量、是否在服务器保留断开连接后的最后一条消息
            mqttAndroidClient.publish(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void init(String clientId) {
        Log.i(TAG, "init:" + clientId);
        Log.i(TAG, "CLIENTID:" + CLIENTID);
        if (mqttAndroidClient != null) {
            return;
        }
        String serverURI = HOST; //服务器地址（协议+地址+端口号）
        mqttAndroidClient = new MqttAndroidClient(this, serverURI, PREF_CLIENT_ID + clientId);
        mqttAndroidClient.setCallback(mqttCallback); //设置监听订阅消息的回调
        mMqttConnectOptions = new MqttConnectOptions();
        mMqttConnectOptions.setCleanSession(true); //设置是否清除缓存
        mMqttConnectOptions.setConnectionTimeout(10); //设置超时时间，单位：秒
        mMqttConnectOptions.setKeepAliveInterval(20); //设置心跳包发送间隔，单位：秒
        mMqttConnectOptions.setUserName(USERNAME); //设置用户名
        mMqttConnectOptions.setPassword(PASSWORD.toCharArray()); //设置密码

        // last will message
        boolean doConnect = true;
        String message = "{\"terminal_uid\":\"" + PREF_CLIENT_ID + clientId + "\"}";
        String topic = PREF_TOPIC + clientId;
        mTopicNames.add(topic);
        if (!TextUtils.isEmpty(mClassId)) {
            mTopicNames.add(PREF_TOPIC + mClassId);
        }
        Integer qos = 2;
        Boolean retained = false;
        if ((!message.equals("")) || (!topic.equals(""))) {
            try {
                mMqttConnectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained.booleanValue());
            } catch (Exception e) {
                Log.i(TAG, "Exception Occured", e);
                doConnect = false;
                iMqttActionListener.onFailure(null, e);
            }
        }
        if (doConnect) {
            doClientConnection();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void doClientConnection() {
        if (!mqttAndroidClient.isConnected() && isConnectIsNomarl()) {
            try {
                mqttAndroidClient.connect(mMqttConnectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "没有可用网络");
            /*没有可用网络的时候，延迟3秒再尝试重连*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doClientConnection();
                }
            }, 3000);
            return false;
        }
    }

    public static void subscribe() {
        for (String topic : mTopicNames) {
            subscribe(topic, 1);
        }
    }

    //MQTT是否连接成功的监听
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            subscribe();
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            arg1.printStackTrace();
            Log.i(TAG, "连接失败 ");
            doClientConnection();//连接失败，重连（可关闭服务器进行模拟）
        }
    };

    //订阅主题的回调
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG, "收到消息： " + new String(message.getPayload()));
            //收到消息，这里弹出Toast表示。如果需要更新UI，可以使用广播或者EventBus进行发送
            String text = new String(message.getPayload());
            try {
                PushMessage pm = PushMessage.parse(text);
                distribute(pm);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //收到其他客户端的消息后，响应给对方告知消息已到达或者消息有问题等
            response("message arrived");
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            Log.i(TAG, "连接断开 ");
            doClientConnection();//连接断开，重连
        }
    };

    private void distribute(PushMessage pm) {
        if (pm.command == null)
            return;

        if (pm.from.equals(mClientId))
            return;

        boolean focus = false;
        if (pm.to != null && pm.to.size() > 0) {
            for (String str : pm.to) {
                if (str.equals(mClientId)) {
                    focus = true;
                    break;
                } else if (mTopicNames.contains(str)) {
                    focus = true;
                    break;
                }
            }
        } else {
            focus = true;
        }

        if (!focus) {
            return;
        }

        EventBus.getDefault().post(pm);
    }

    @Override
    public void onDestroy() {
        try {
            mqttAndroidClient.disconnect(); //断开连接
        } catch (MqttException e) {
            e.printStackTrace();
        }
        clientMessenger = null;
        super.onDestroy();
    }

    private static final int RECEIVE_MESSAGE_CODE = 0x0001;

    private static final int SEND_MESSAGE_CODE = 0x0002;

    //clientMessenger表示的是客户端的Messenger，可以通过来自于客户端的Message的replyTo属性获得，
    //其内部指向了客户端的ClientHandler实例，可以用clientMessenger向客户端发送消息
    private Messenger clientMessenger = null;

    //serviceMessenger是Service自身的Messenger，其内部指向了ServiceHandler的实例
    //客户端可以通过IBinder构建Service端的Messenger，从而向Service发送消息，
    //并由ServiceHandler接收并处理来自于客户端的消息
    private Messenger serviceMessenger = new Messenger(new ServiceHandler());

    //MyService用ServiceHandler接收并处理来自于客户端的消息
    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i("DemoLog", "ServiceHandler -> handleMessage");
            if (msg.what == RECEIVE_MESSAGE_CODE) {
                Bundle data = msg.getData();
                if (data != null) {
                    String str = data.getString("msg");
                    Log.i("DemoLog", "MyService收到客户端如下信息: " + str);
                }
                //通过Message的replyTo获取到客户端自身的Messenger，
                //Service可以通过它向客户端发送消息
                clientMessenger = msg.replyTo;
                if (clientMessenger != null) {
                    Log.i("DemoLog", "MyService向客户端回信");
                    Message msgToClient = Message.obtain();
                    msgToClient.what = SEND_MESSAGE_CODE;
                    //可以通过Bundle发送跨进程的信息
                    Bundle bundle = new Bundle();
                    bundle.putString("msg", "你好，客户端，我是MyService");
                    msgToClient.setData(bundle);
                    try {
                        clientMessenger.send(msgToClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        Log.e("DemoLog", "MyService向客户端发送信息失败: " + e.getMessage());
                    }
                }
            }
        }
    }
}