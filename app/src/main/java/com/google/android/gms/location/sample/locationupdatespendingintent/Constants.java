package com.google.android.gms.location.sample.locationupdatespendingintent;

import com.google.android.gms.location.DetectedActivity;

import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class Constants {

    private Constants() {}

    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000; // 30 seconds

    static final String KEY_DETECTED_ACTIVITIES = PACKAGE_NAME + ".DETECTED_ACTIVITIES";

    static final String KEY_ACTIVITY_UPDATES_REQUESTED = PACKAGE_NAME +
            ".ACTIVITY_UPDATES_REQUESTED";

    static final int[] MONITORED_ACTIVITIES = {
            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };

    public static final String MQTT_BROKER_URL = "tcp://35.202.49.101:1883";
    //public static final String MQTT_BROKER_URL = "tcp://m10.cloudmqtt.com:11310";

    public static final String PUBLISH_TOPIC = "v1/devices/me/telemetry";
    //public static final String PUBLISH_TOPIC = "test";

    // Elf 1
    public static final String USERNAME = "vc7XV9yojXt1NxwPv423";
    public static final String PASSWORD = "";

//    elf2
//    public static final String USERNAME = "kxrs3W3C1z1wi7BAGw5W";
//    public static final String PASSWORD = "";

}
