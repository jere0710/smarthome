package com.github.jhaucke.smarthome.washeragent.service;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.github.jhaucke.smarthome.washeragent.Constants;
import com.github.jhaucke.smarthome.washeragent.R;

public class NotificationHelper {

    public static void createNotificationWithSound(Context serviceContext, String message) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setSound(alarmSound)
                        .setVibrate(pattern)
                        .setLights(Color.YELLOW, 1000, 2000);

        NotificationManager mNotifyMgr =
                (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }
}
