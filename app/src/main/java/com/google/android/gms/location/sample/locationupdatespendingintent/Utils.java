/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.locationupdatespendingintent;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.sample.locationupdatespendingintent.service.PahoMqttClient;
import com.google.android.gms.location.sample.locationupdatespendingintent.view.MainActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Utility methods used in this sample.
 */
public class Utils {

    public final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    public final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final static String KEY_LATITUDE = "latitude";
    final static String KEY_LONGITUDE = "longitude";
    final static String KEY_SPEED = "speed";
    final static String KEY_LOGGED_IN = "logged-state";
    final static String KEY_USERNAME = "username";
    final static String KEY_PASSWORD = "password";
    final static String KEY_FIRSTLOGIN = "firstlogin";

    final static String CHANNEL_ID = "channel_01";

    public static void setLoggedIn(Context context, boolean state) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOGGED_IN, state)
                .apply();
    }

    public static boolean getLoggedIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOGGED_IN, false);
    }

    public static void setFirstLoginState(Context context, boolean state) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_FIRSTLOGIN, state)
                .apply();
    }

    public static boolean getFirstLoginState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_FIRSTLOGIN, false);
    }

    public static void setUsername(Context context, int driverNumber) {
        if (driverNumber == 1) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(KEY_USERNAME, Constants.USERNAME_elf_1)
                    .apply();

            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(KEY_PASSWORD, Constants.PASSWORD_elf_1)
                    .apply();
        } else if (driverNumber == 2){
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(KEY_USERNAME, Constants.USERNAME_elf_2)
                    .apply();
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putString(KEY_PASSWORD, Constants.PASSWORD_elf_2)
                    .apply();
        }
    }

    public static String getUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_USERNAME, "");
    }

    public static String getPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_PASSWORD, "");
    }

   public static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    public static boolean getRequestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    public static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    static void sendToMQTT(Context context, Location location) {

        PahoMqttClient pahoMqttClient = new PahoMqttClient();

        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = pahoMqttClient.getMqttClient(context, Constants.MQTT_BROKER_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(Constants.USERNAME_elf_1);
        options.setPassword(Constants.PASSWORD_elf_1.toCharArray());
        options.setAutomaticReconnect(true);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("MQTT", " is onSucces");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTT", " is onFailure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date date = new Date(location.getTime());
        String dateString = date.toString();
        Double longitude = location.getLongitude();
        Double latitude = location.getLatitude();
        float speed = location.getSpeed();

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + ", " +
                Double.toString(location.getLongitude()) + " Speed: " +
                Double.toString(speed) + " km/h " + dateString;

        JSONObject latlong = new JSONObject();
        try {
            latlong.put("Latitude", latitude);
            latlong.put("Longitude", longitude);
            latlong.put("Speed", speed);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        if (!msg.isEmpty()) {
            try {
                pahoMqttClient.publishMessage(client, latlong.toString().trim(), 1, Constants.PUBLISH_TOPIC);
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    public static String getLocationResultTitle(Context context, List<Location> locations) {
        String numLocationsReported = context.getResources().getQuantityString(
                R.plurals.num_locations_reported, locations.size(), locations.size());
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());
    }

    static String getLatitude(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LATITUDE, "");    }

    static void setLatitude(Context context, Location location) {
        String latitude = String.valueOf(location.getLatitude());
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LATITUDE, latitude)
                .apply();
    }

    static String getSpeed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_SPEED, "");    }

    static void setSpeed(Context context, Location location) {
        String speed = String.valueOf(location.getSpeed());
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_SPEED, speed)
                .apply();
    }

    static String getLongitude(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LONGITUDE, "");    }

    static void setLongitude(Context context, Location location) {
        String longitude = String.valueOf(location.getLongitude());
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LONGITUDE, longitude)
                .apply();
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return context.getString(R.string.unknown_location);
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(", ");
            sb.append(location.getSpeed());
            sb.append(" km/h)");
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void setLocationUpdatesResult(Context context, List<Location> locations) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
                        + "\n" + getLocationResultText(context, locations))
                .apply();
    }

    public static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    public static ArrayList<DetectedActivity> detectedActivitiesFromJson(String jsonArray) {
        Type listType = new TypeToken<ArrayList<DetectedActivity>>(){}.getType();
        ArrayList<DetectedActivity> detectedActivities = new Gson().fromJson(jsonArray, listType);
        if (detectedActivities == null) {
            detectedActivities = new ArrayList<>();
        }
        return detectedActivities;
    }

    public static String detectedActivitiesToJson(ArrayList<DetectedActivity> detectedActivitiesList) {
        Type type = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        return new Gson().toJson(detectedActivitiesList, type);
    }

    public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }

    static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }
}
