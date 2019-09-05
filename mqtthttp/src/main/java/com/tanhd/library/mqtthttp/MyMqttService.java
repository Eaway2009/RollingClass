package com.tanhd.library.mqtthttp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStart:");
        mClientId = intent.getStringExtra(PARAM_CLIENT_ID);
        mClassId = intent.getStringExtra(PARAM_TOPIC);
        init(mClientId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void publishMessage(PushMessage.COMMAND command, String to, Map<String, String> data) {
        if (to != null)
            publishMessage(command, Arrays.asList(new String[]{to}), data);
        else
            publishMessage(command, (List<String>) null, data);
    }

    public static void publishMessage(PushMessage.COMMAND command, List<String> to, Map<String, String> data) {
        PushMessage message = new PushMessage();
        message.from = mClientId;
        message.command = command;
        message.to = to;
        message.parameters = data;

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
            Toast.makeText(getApplicationContext(), "messageArrived: " + new String(message.getPayload()), Toast.LENGTH_LONG).show();
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

    @Override
    public void onDestroy() {
        try {
            mqttAndroidClient.disconnect(); //断开连接
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}