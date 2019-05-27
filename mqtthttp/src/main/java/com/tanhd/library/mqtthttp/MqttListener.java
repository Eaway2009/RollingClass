package com.tanhd.library.mqtthttp;

public interface MqttListener {
    void messageArrived(PushMessage message);
    void networkTimeout(boolean flag);
}
