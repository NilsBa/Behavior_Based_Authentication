package com.example.behavior_based_authentication;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyForegroundService extends Service implements SensorEventListener {

    private SensorManager sensorManager;

    private final long secondToTimeStamp = 1000000000;
    private final long lastTimeMaxDelayInSeconds = 2;

    private HashMap<Integer, Long> lastTimeStamps = new HashMap<>();
    private HashMap<Integer, ThreeDPoints> sensorPoints = new HashMap<>();
    private ArrayList<Touch> touchArrayList = new ArrayList<>();

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    public static ArrayList<LatLng> locationArrayList = new ArrayList<>();

    private final long toastTimeoutMs = 2000;
    private long lastToastTime = 0;

    private final long timerDelay = 15 * 1000;
    private Timer myTimer;

    private WifiManager wifiManager;



    @Override
    public void onCreate() {
        super.onCreate();

        createLocationRequest();
        startLocationUpdates();


        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        IntentFilter filter = new IntentFilter();
        filter.addAction("touch_event_has_occurred");
        registerReceiver(receiver, filter);

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    parseJson();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, 0, timerDelay);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChanel() {
        String notificationChannelId = "Location channel id";
        String channelName = "Background Service";

        NotificationChannel chan = new NotificationChannel(
                notificationChannelId,
                channelName,
                NotificationManager.IMPORTANCE_NONE
        );
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, notificationChannelId);

        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Location updates:")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        // Log.d("SensorQuantity: ", String.valueOf(sensorList.size()));
        // Log.d("AllSensors: ", sensorManager.getSensorList(Sensor.TYPE_ALL).toString());

        for (Sensor sensorElement : sensorList) {
            int sensorType = sensorElement.getType();

            if (SensorLists.oneValue.containsKey(sensorType) || SensorLists.threeValues.containsKey(sensorType)) {
                // Log.d("Sensor Registered", sensorElement.getStringType());
                registerSensor(sensorType);
                sensorPoints.put(sensorType, new ThreeDPoints());
            }

        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final String channelID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                channelID,
                channelID,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, channelID)
                .setContentText("Service is running")
                .setContentTitle("Service enabled")
                .setSmallIcon(R.drawable.ic_launcher_background);

        startForeground(1001, notification.build());

        return super.onStartCommand(intent, flags, startId);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    protected void createLocationRequest() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel();
        else startForeground(
                1,
                new Notification()
        );

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setMaxWaitTime(6000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                long now = System.currentTimeMillis();
                Location location = locationResult.getLastLocation();

                // can be deleted, only for logging purposes
                if (lastToastTime + toastTimeoutMs < now) {
                    Toast.makeText(getApplicationContext(),
                            "Lat: " + Double.toString(location.getLatitude()) + '\n' +
                                    "Long: " + Double.toString(location.getLongitude()), Toast.LENGTH_LONG).show();
                    lastToastTime = now;
                }


                locationArrayList.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        };
    }

    private void registerSensor(int sensorType) {
        lastTimeStamps.put(sensorType, 0L);
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        fusedLocationClient.removeLocationUpdates(locationCallback);

        if (myTimer != null) {
            myTimer.cancel();
            myTimer.purge();
        }

        unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {


        int sensorType = sensorEvent.sensor.getType();
        long sensorTimeStamp = sensorEvent.timestamp;
        boolean is3D;

        if (SensorLists.oneValue.containsKey(sensorType)) {
            is3D = false;
        } else if (SensorLists.threeValues.containsKey(sensorType)) {
            is3D = true;
        } else {
            return;
        }

        // Only get a save a new Value every lastTimeMaxDelayInSeconds
        if (lastTimeStamps.get(sensorType) + lastTimeMaxDelayInSeconds * secondToTimeStamp < sensorTimeStamp) {
            lastTimeStamps.put(sensorType, sensorTimeStamp);
        } else {
            return;
        }

        long currentTime = System.currentTimeMillis();

        if (!is3D) {
            float x = sensorEvent.values[0];
            sensorPoints.get(sensorType).Add3DPoint(new ThreeDPoint(currentTime, x, 0f, 0f));
            Log.d(SensorLists.oneValue.get(sensorType), String.valueOf(sensorEvent.values[0]));
        } else {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            sensorPoints.get(sensorType).Add3DPoint(new ThreeDPoint(currentTime, x, y, z));
            Log.d(SensorLists.threeValues.get(sensorType), String.valueOf(sensorEvent.values[0]) + " " + String.valueOf(sensorEvent.values[1]) + " " + String.valueOf(sensorEvent.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void parseJson() throws JSONException {
        JSONObject jsonObject = createJsonObject();

        // Beautify Json Output
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(String.valueOf(jsonObject)).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);
        // Log.d("jsonObject", prettyJson);

        try {
            File file = new File(this.getFilesDir(), "Behavior_Based_Authentication/");

            FileOutputStream output = new FileOutputStream(file);
            output.write(prettyJson.getBytes());
            Log.d("parseJson", "Saved to " + getFilesDir());
            output.close();


            clearLists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        jsonObject = new JSONObject(prettyJson);
        sendData(jsonObject);
    }

    public void sendData(JSONObject jsonObject) {
        // can be deleted, only for logging purposes
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://httpbin.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        JsonApi jsonApi = retrofit.create(JsonApi.class);
        JsonDataSet jsonDataSet = new JsonDataSet(jsonObject);
        Call<JsonDataSet> call = jsonApi.PostData(jsonDataSet);

        call.enqueue(new Callback<JsonDataSet>() {
            @Override
            public void onResponse(Call<JsonDataSet> call, Response<JsonDataSet> response) {
                //
            }

            @Override
            public void onFailure(Call<JsonDataSet> call, Throwable t) {
                //
            }
        });
    }

    public JSONObject createJsonObject() throws JSONException {
        JSONObject object = new JSONObject();

        object.put("Wifi", getWifiJSON());
        object.put("Coordinates", getLocationJSON());
        object.put("Touches", getTouchJSON());

        for (Integer key : sensorPoints.keySet()) {
            JSONArray sensorArray = new JSONArray();
            boolean is3D = SensorLists.oneValue.containsKey(key) ? false : true;
            String sensorName = is3D ? SensorLists.threeValues.get(key) : SensorLists.oneValue.get(key);
            // Log.d("KEY", sensorName);

            if (is3D) {
                for (ThreeDPoint threeDPoint : sensorPoints.get(key).getThreeDPoints()) {
                    JSONObject jsonPointObject = new JSONObject();
                    jsonPointObject.put("time", threeDPoint.getTimestamp());
                    jsonPointObject.put("x", threeDPoint.getX());
                    jsonPointObject.put("y", threeDPoint.getY());
                    jsonPointObject.put("z", threeDPoint.getZ());

                    sensorArray.put(jsonPointObject);
                }
            } else {

                for (ThreeDPoint threeDPoint : sensorPoints.get(key).getThreeDPoints()) {
                    JSONObject jsonPointObject = new JSONObject();
                    jsonPointObject.put("time", threeDPoint.getTimestamp());
                    jsonPointObject.put("x", threeDPoint.getX());

                    sensorArray.put(jsonPointObject);
                }
            }

            object.put(sensorName, sensorArray);
        }

        return object;
    }



    private void clearLists() {
        for (int key : sensorPoints.keySet()) {
            sensorPoints.get(key).clearList();
        }
        locationArrayList.clear();
        touchArrayList.clear();
    }


    public JSONObject getWifiJSON() throws JSONException {
        JSONObject object = new JSONObject();
        boolean isWifiEnabled = wifiManager.isWifiEnabled();
        object.put("WifiEnabled", isWifiEnabled);

        if (isWifiEnabled) {

            wifiManager.startScan();
            // Only works for Android API >= 32
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Log.d("wifi", String.valueOf(wifiManager.getMaxSignalLevel()));
                // Log.d("wifi", String.valueOf(wifiManager.getNetworkSuggestions()));
                object.put("MaxSignalLevel", wifiManager.getMaxSignalLevel());
                object.put("NetworkSuggestions", wifiManager.getNetworkSuggestions());
            }

            // Log.d("wifi", String.valueOf(wifiManager.getConnectionInfo()));
            // Log.d("wifi", String.valueOf(wifiManager.getScanResults()));
            object.put("ConnectionInfo", String.valueOf(wifiManager.getConnectionInfo()));
            object.put("ScanResults", String.valueOf(wifiManager.getScanResults()));
        }

        return object;
    }

    public JSONArray getLocationJSON() throws JSONException {
        JSONArray locationArray = new JSONArray();
        for (LatLng location : locationArrayList) {
            JSONObject locationObject = new JSONObject();
            locationObject.put("Latitude", location.latitude);
            locationObject.put("Longitude", location.longitude);
            locationArray.put(locationObject);
        }
        return locationArray;
    }

    public JSONArray getTouchJSON() throws JSONException {
        JSONArray touchArray = new JSONArray();
        for (Touch touch : touchArrayList) {
            JSONObject touchObject = new JSONObject();
            touchObject.put("X", touch.getX());
            touchObject.put("Y", touch.getY());
            touchObject.put("FingerSize", touch.getFingersize());
            touchObject.put("Pressure", touch.getPressure());
            touchObject.put("DownTime", touch.getDowntime());
            touchObject.put("EventTime", touch.getEventtime());
            touchArray.put(touchObject);
        }
        return touchArray;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("touch_event_has_occurred")){
                Touch touch = intent.getExtras().getParcelable("touch");
                touchArrayList.add(touch);
            }
        }
    };

}
