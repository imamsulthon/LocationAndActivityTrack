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
package com.google.android.gms.location.sample.locationupdatespendingintent.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.sample.locationupdatespendingintent.Constants;
import com.google.android.gms.location.sample.locationupdatespendingintent.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Handles incoming location updates and displays a notification with the location data.
 *
 * For apps targeting API level 25 ("Nougat") or lower, location updates may be requested
 * using {@link android.app.PendingIntent#getService(Context, int, Intent, int)} or
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)}. For apps targeting
 * API level O, only {@code getBroadcast} should be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
public class LocationUpdatesIntentService extends IntentService implements IMqttActionListener {

    public static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();

    String latitude;
    String longitude;
    String speed;

    //Mqtt
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;

    public LocationUpdatesIntentService() {
        // Name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        pahoMqttClient = new PahoMqttClient();
        String clientId = MqttClient.generateClientId();
        client = pahoMqttClient.getMqttClient(getApplicationContext(), Constants.MQTT_BROKER_URL,clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(Utils.getUsername(getApplicationContext()));
        options.setPassword(Utils.getPassword(getApplicationContext()).toCharArray());
        options.setAutomaticReconnect(true);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(this);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    Location location = locations.get(0);
                    Utils.setLocationUpdatesResult(this, locations);
                    Utils.sendNotification(this, Utils.getLocationResultTitle(this, locations));
                    Log.e(TAG, Utils.getLocationUpdatesResult(this));

                    setLocation(location);
                }
            }
        }
    }

    private void setLocation(Location location) {
        this.latitude = String.valueOf(location.getLatitude());
        this.longitude = String.valueOf(location.getLongitude());
        this.speed = String.valueOf(location.getSpeed());
    }

    public void sendToMQTT(String latitude, String longitude, String speed ) {
        JSONObject latlong = new JSONObject();
        try {
            latlong.put("Latitude", latitude);
            latlong.put("Longitude", longitude);
            latlong.put("Speed", speed);
        }catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            pahoMqttClient.publishMessage(client, latlong.toString().trim(), 1, Constants.PUBLISH_TOPIC);
            Log.d(TAG, "location sent to Mqtt");
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.d(TAG, "Mqtt is onSuccess connected");
        if (latitude == null && longitude == null && speed == null) {
            Log.e(TAG, "Location is null");
        } else {
            sendToMQTT(latitude, longitude, speed);
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.d(TAG, "Mqtt is onFailure connected");
        exception.printStackTrace();
    }
}
