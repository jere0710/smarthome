package com.github.jhaucke.smarthome.washeragent.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectivityChangReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogWriter.appendLog("network state changed");
        MqttService.getClient().connect();
    }
}
