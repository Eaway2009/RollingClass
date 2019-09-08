package com.tanhd.rollingclass.server;


import android.os.Handler;
import android.os.Message;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;

public class ConnectionStatus {
    public static interface ConnectionStatusListener {
        void onTimeout(int times);
    }
    private int times = 0;
    private boolean isRunning;
    private final String mTargetID;
    private final ConnectionStatusListener mListener;

    public ConnectionStatus(String targetID, ConnectionStatusListener listener) {
        mListener = listener;
        mTargetID = targetID;
        start();
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            if (message.command == PushMessage.COMMAND.PING_OK) {
                if (message.from.equals(mTargetID)) {
                    times = 0;
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (times > 0) {
                if (mListener != null) {
                    mListener.onTimeout(times);
                }
            }

            if (isRunning)
                ping();
        }
    };

    private void ping() {
        times++;
        try {
            MyMqttService.publishMessage(PushMessage.COMMAND.PING, mTargetID, null);
            mHandler.sendEmptyMessageDelayed(0, 3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (MQTT.getInstance() == null)
            return;

        MQTT.register(mqttListener);
        isRunning = true;
        ping();
    }

    public void stop() {
        if (MQTT.getInstance() == null)
            return;

        isRunning = false;
        MQTT.unregister(mqttListener);
    }
}
