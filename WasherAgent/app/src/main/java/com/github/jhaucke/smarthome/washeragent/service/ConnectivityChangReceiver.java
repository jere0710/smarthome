package com.github.jhaucke.smarthome.washeragent.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.github.jhaucke.smarthome.washeragent.R;

import java.util.UUID;

/**
 * Created by jeremias.haucke on 22.09.2015.
 */
public class ConnectivityChangReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        createNotification("network state changed");
        MqttService.getClient().connect();
    }

    private void createNotification(String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setLights(Color.MAGENTA, 1000, 2000);

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(UUID.randomUUID().hashCode(), mBuilder.build());
    }
}
