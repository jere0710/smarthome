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

    private Context serviceContext;
    private String state = null;
    private NotificationManager notifyMgr;

    public NotificationHelper(Context context) {
        this.serviceContext = context;
        notifyMgr = (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
    }

    public void createNotification(String message) {
        String[] splitedMessage = message.split("-");
        String newState = splitedMessage[1].trim();
        if (state == null || !state.equals(newState)) {
            state = newState;
        } else {
            LogWriter.appendLog("### duplicate message ###");
            return;
        }

        if (newState.equals("OFF")) {
            notifyMgr.cancel(Constants.NOTIFICATION_ID);
        }
        if (newState.equals("ON")) {
            buildAndFireNotification(serviceContext, message, R.drawable.ic_stat_on);
        }
        if (newState.equals("ACTIVE")) {
            buildAndFireNotification(serviceContext, message, R.drawable.ic_stat_active);
        }
        if (newState.equals("FINISHED")) {
            buildAndFireNotification(serviceContext, message, R.drawable.ic_stat_finished);
        }
    }

    private void buildAndFireNotification(Context serviceContext, String message, int icon) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(icon)
                        .setContentTitle("My little smarthome")
                        .setContentText(message)
                        .setOngoing(true)
                        .setSound(alarmSound)
                        .setVibrate(pattern)
                        .setLights(Color.YELLOW, 1000, 2000);

        // Builds the notification and issues it.
        notifyMgr.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }
}
