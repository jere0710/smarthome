package com.github.jhaucke.smarthome.washeragent.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReconnectAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogWriter.appendLog("reconnect...");
        MqttService.getClient().connect();
    }
}
