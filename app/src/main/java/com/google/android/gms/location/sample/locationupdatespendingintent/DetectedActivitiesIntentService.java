package com.google.android.gms.location.sample.locationupdatespendingintent;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DetectedActivitiesIntentService extends IntentService implements IMqttActionListener {

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();
    public static final String ACTION_PROCESS_UPDATES_ACTIVITY =
            "com.google.android.gms.location.sample.locationupdatespendingintent.action" +
                    ".PROCESS_UPDATES" + ".Activity";

    String bestDetectedActivity;
    String bestActivityConfidence;

    //Mqtt
    private MqttAndroidClient client;
    private PahoMqttClient pahoMqttClient;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
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
            if (ACTION_PROCESS_UPDATES_ACTIVITY.equals(action)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

                ArrayList<DetectedActivity> detectedActivities = (ArrayList<DetectedActivity>) result.getProbableActivities();

                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString(Constants.KEY_DETECTED_ACTIVITIES,
                                Utils.detectedActivitiesToJson(detectedActivities))
                        .apply();

                // Log each activity.
                HashMap<Integer, Integer> map = new HashMap<>();
                Log.e(TAG, "activities detected");
                for (DetectedActivity da: detectedActivities) {
                    Log.e(TAG, Utils.getActivityString(
                            getApplicationContext(),
                            da.getType()) + " " + da.getConfidence() + "%"
                    );
                    map.put(da.getType(), da.getConfidence());
                }

                int maxValue = Collections.max(map.values());
                for (Map.Entry<Integer, Integer> entry: map.entrySet()) {
                    if (entry.getValue() == maxValue) {
                        setBestActivityDetected(Utils.getActivityString(this, entry.getKey()), String.valueOf(entry.getValue()));
                    }
                }
            }
        }
    }

    private void setBestActivityDetected(String activityName, String confidence) {
        this.bestDetectedActivity = activityName;
        this.bestActivityConfidence = confidence;
    }

    public void sendToMqtt(String activityName, String confidence) {
        JSONObject activity = new JSONObject();

        try {
            activity.put("Activity", activityName);
            activity.put("Confidence", confidence);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            pahoMqttClient.publishMessage(client, activity.toString().trim(), 1, Constants.PUBLISH_TOPIC);
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
        if (bestDetectedActivity == null && bestActivityConfidence == null) {
            Log.e(TAG, "Detected Activity is null");
        } else {
            sendToMqtt(bestDetectedActivity, bestActivityConfidence);
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.d(TAG, "Mqtt is onFailure connected");
        exception.printStackTrace();
    }
}
