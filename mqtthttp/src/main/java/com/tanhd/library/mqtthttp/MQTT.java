package com.tanhd.library.mqtthttp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MQTT {
    private class OutMsg {
        String topicName;
        int qos;
        String content;
    }
    private static final String TAG = "MQTT";
    private static final String PREF_TOPIC = "MQTT_QHMDM/";
    private static final String PREF_CLIENT_ID = "GID_";
    private static final String MQTT_SERVER = "tcp://mdm.sea-ai.com:1234";

    private MqttClient mClient;
    private HashSet<String> mTopicNames = new HashSet<>();
    private final String mClientID;
    private ArrayList<MqttListener> mCallbacks = new ArrayList<>();
    private ArrayList<MqttMessage> mQueue = new ArrayList<>();
    private ArrayList<OutMsg> mOutQueue = new ArrayList<>();
    private Handler mHandler;

    private static MQTT mInstance = null;

    public static MQTT getInstance(String clientID, int port) {
        if (mInstance == null) {
            mInstance = new MQTT(clientID, port);
        }
        return mInstance;
    }

    public static MQTT getInstance() {
        return mInstance;
    }

    public static void publishMessage(PushMessage.COMMAND command, List<String> to, Map<String, String> data) {
        MQTT mqtt = getInstance();
        if (mqtt == null)
            return;

        mqtt.publish(command, to, data);
    }

    public static void publishMessage(PushMessage.COMMAND command, String to, Map<String, String> data) {
        if (to != null)
            publishMessage(command, Arrays.asList(new String[] {to}), data);
        else
            publishMessage(command, (List<String>) null, data);
    }

    public static void register(MqttListener listener) {
        MQTT mqtt = getInstance();
        if (mqtt == null)
            return;

        mqtt._register(listener);
    }

    public static void unregister(MqttListener listener) {
        MQTT mqtt = getInstance();
        if (mqtt == null)
            return;

        mqtt._unregister(listener);
    }

    public MQTT(String clientID, int port) {
        mClientID = clientID;
        mHandler = new MsgHandler();
        mTopicNames.add(mClientID);
    }

    public boolean connect() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(false);
            options.setUserName("admin");
            options.setPassword("admin".toCharArray());
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(30000);
            options.setAutomaticReconnect(true);
            mClient = new MqttClient(MQTT_SERVER, PREF_CLIENT_ID + mClientID, new MemoryPersistence());
            mClient.setCallback( );
            mClient.connect(options);
            Log.i(TAG, "Connect: true");
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            Log.i(TAG, "Connect: false");
        }

        return false;
    }

    public void subscribe() {
        for (String topic: mTopicNames) {
            subscribe(topic, 1);
        }
    }

    public void disconnect() {
        try {
            mClient.disconnect();
            Log.i(TAG, "Disconnect: true");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.i(TAG, "Disconnect: false");
        }
        mInstance = null;
    }

    private boolean publish(String topicName, int qos, String content) {
        boolean flag = true;
        OutMsg outMsg = new OutMsg();
        outMsg.topicName = topicName;
        outMsg.qos = qos;
        outMsg.content = content;
        synchronized (mOutQueue) {
            mOutQueue.add(outMsg);
            mOutHandler.sendEmptyMessage(0);
        }
        return flag;
    }

    private boolean subscribe(String topicName, int qos) {
        boolean flag = false;
        String topic = PREF_TOPIC + topicName;
        if (mClient.isConnected()) {
            try {
                mClient.subscribe(topic, qos);
                mTopicNames.add(topicName);
                flag = true;
            } catch (MqttException e) {
                Log.e(TAG, "subscribe: " + e.getMessage());
            }
        }
        Log.i(TAG, "subscribe: " + flag + " topic:" + topic);
        return flag;
    }

    public boolean unsubscribe(String topicName){
        boolean flag = false;
        if (mClient.isConnected()) {
            try {
                mClient.unsubscribe(topicName);
                mTopicNames.remove(topicName);
                flag = true;
            } catch (MqttException e) {
                Log.e(TAG, "unSubscribe: " + e.getMessage());
            }
        }
        Log.i(TAG, "unSubscribe: " + flag + " topic:" + topicName);
        return flag;
    }

    public void unsubscribe() {
        String[] topics = new String[0];
        topics = mTopicNames.toArray(topics);
        for (String topic: topics) {
            unsubscribe(topic);
        }
    }

    public boolean subscribe(String topicName) {
        if (mTopicNames.contains(topicName))
            return true;

        return subscribe(topicName, 1);
    }

    private void publish(PushMessage.COMMAND command, List<String> to, Map<String, String> data) {
        PushMessage message = new PushMessage();
        message.from = mClientID;
        message.command = command;
        message.to = to;
        message.parameters = data;

        String text = message.toString();

        if (to == null) {
            for (String topic: mTopicNames) {
                if (topic.equals(message.from))
                    continue;
                publish(topic, 1, text);
            }
        } else {
            for (String topic: to) {
                publish(topic, 1, text);
            }
        }
    }

    private List<PushMessage> splitMessage(PushMessage message) {
        ArrayList<PushMessage> list = new ArrayList<>();
        if (message.to == null || message.to.size() == 0) {
            list.add(message);
        } else {
            for (String to: message.to) {
                PushMessage n = new PushMessage();
                n.from = message.from;
                n.command = message.command;
                n.parameters = message.parameters;
                n.to = Arrays.asList(new String[]{to});
                list.add(n);
            }
        }

        return list;
    }

    private void publish(PushMessage.COMMAND command, String to, Map<String, String> data) {
        if (to != null)
            publish(command, Arrays.asList(new String[] {to}), data);
        else
            publish(command, (List<String>) null, data);
    }

    private void _register(MqttListener listener) {
        mCallbacks.add(listener);
    }

    private void _unregister(MqttListener listener) {
        mCallbacks.remove(listener);
    }

    private MqttCallback mCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            synchronized (mQueue) {
                mQueue.add(message);
                mHandler.sendEmptyMessage(0);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    };

    private class MsgHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            synchronized (mQueue) {

                while(mQueue.size() > 0) {
                    if (mCallbacks.isEmpty()) {
                        sendEmptyMessageDelayed(0, 500);
                        return;
                    }

                    MqttMessage message = mQueue.remove(0);
                    String text = new String(message.getPayload());
                    try {
                        PushMessage pm = PushMessage.parse(text);
                        distribute(pm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Handler mOutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            synchronized (mOutQueue) {

                while(mOutQueue.size() > 0) {
                    if (!mClient.isConnected()) {
                        connectionLost(true);
                        sendEmptyMessageDelayed(0, 500);
                        return;
                    } else {
                        connectionLost(false);
                    }

                    OutMsg outMsg = mOutQueue.get(0);

                    byte[] payload = outMsg.content.getBytes();
                    String topic = PREF_TOPIC + outMsg.topicName;
                    if (mClient.isConnected()) {
                        MqttMessage message = new MqttMessage(payload);
                        message.setQos(outMsg.qos);

                        try {
                            mClient.publish(topic, message);
                        } catch (MqttException e) {
                        }

                        mOutQueue.remove(outMsg);
                    }
                }
                removeMessages(0);
            }
        }
    };

    private void distribute(PushMessage pm) {
        if (pm.command == null)
            return;

        if (pm.from.equals(mClientID))
            return;

        boolean focus = false;
        if (pm.to != null && pm.to.size() > 0) {
            for (String str: pm.to) {
                if (str.equals(mClientID)) {
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

        for (int i=0; i<mCallbacks.size(); i++) {
            try {
                MqttListener listener = mCallbacks.get(i);
                listener.messageArrived(pm);
            } catch (Exception e) {
                mCallbacks.remove(i);
                i = 0;
            }
        }
    }

    private void connectionLost(boolean flag) {
        for (int i=0; i<mCallbacks.size(); i++) {
            try {
                MqttListener listener = mCallbacks.get(i);
                listener.networkTimeout(flag);
            } catch (Exception e) {
                mCallbacks.remove(i);
                i = 0;
            }
        }
    }

}
