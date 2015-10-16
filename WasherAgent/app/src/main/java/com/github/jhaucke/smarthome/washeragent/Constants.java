package com.github.jhaucke.smarthome.washeragent;

public interface Constants {

    public static final String APP = "com.github.jhaucke.smarthome.WasherAgent";

    public static final String SHARED_PREFERENCES_NAME = APP + ".SHARED_PREFERENCES_NAME";

    public static final String KEY_BROKER_HOST = APP + ".KEY_BROKER_HOST";

    public static final String RECONNECT_ALARM_ACTION = APP + ".RECONNECT_ALARM_ACTION";

    public static final String ACTION_KEEP_ALIVE_PING_SENDER = APP + ".ACTION_KEEP_ALIVE_PING_SENDER";

    public static final String WAKE_LOCK_TAG_MQTT_CLIENT = APP + ".WAKE_LOCK_TAG_MQTT_CLIENT";

    public static final String WIFI_LOCK_TAG_MQTT_CLIENT = APP + ".WIFI_LOCK_TAG_MQTT_CLIENT";

    public static final int NOTIFICATION_ID = 824397105;

    /**
     * Keep alive interval in seconds.
     */
    public static final int KEEP_ALIVE_INTERVAL = 1200;

    /**
     * Alarm window to execute the keep alive ping in milliseconds.
     */
    public static final long ALARM_WINDOW_KEEP_ALIVE_PING = KEEP_ALIVE_INTERVAL * 1000 / 2 - 60000;
}
