package com.tanhd.library.mqtthttp;

import java.util.UUID;

public class Utils {

    public static String getDeviceId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
