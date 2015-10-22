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

    public static final String STATE_OFF = "OFF";
    public static final String STATE_ON = "ON";
    public static final String STATE_ACTIVE = "ACTIVE";
    public static final String STATE_FINISHED = "FINISHED";

    private Context serviceContext;
    private String state = null;
    private NotificationManager notifyMgr;

    public NotificationHelper(Context context) {
        this.serviceContext = context;
        notifyMgr = (NotificationManager) serviceContext.getSystemService(serviceContext.NOTIFICATION_SERVICE);
    }

    public void createNotification(String message) {
        String[] splittedMessage = message.split("-");
        String newState = splittedMessage[1].trim();
        if (state == null || !state.equals(newState)) {
            state = newState;
        } else {
            LogWriter.appendLog("### duplicate message ###");
            return;
        }

        switch (newState) {
            case STATE_OFF:
                notifyMgr.cancel(Constants.NOTIFICATION_ID);
                break;
            case STATE_ON:
                buildAndFireNotification(serviceContext, serviceContext.getResources().getString(R.string.notify_washer_on), R.drawable.ic_stat_on, true);
                break;
            case STATE_ACTIVE:
                buildAndFireNotification(serviceContext, serviceContext.getResources().getString(R.string.notify_washer_active), R.drawable.ic_stat_active, true);
                break;
            case STATE_FINISHED:
                buildAndFireNotification(serviceContext, serviceContext.getResources().getString(R.string.notify_washer_finished), R.drawable.ic_stat_finished, true);
                break;

            default:
                buildAndFireNotification(serviceContext, message, R.mipmap.ic_launcher, false);
                break;
        }
    }

    private void buildAndFireNotification(Context serviceContext, String message, int icon, boolean isOngoing) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] pattern = {500, 500, 500, 500, 500, 500, 500};
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(serviceContext)
                        .setSmallIcon(icon)
                        .setContentTitle(serviceContext.getResources().getString(R.string.app_name))
                        .setContentText(message)
                        .setOngoing(isOngoing)
                        .setSound(alarmSound)
                        .setVibrate(pattern)
                        .setLights(Color.YELLOW, 1000, 2000);

        // Builds the notification and issues it.
        notifyMgr.notify(Constants.NOTIFICATION_ID, mBuilder.build());
    }
}
