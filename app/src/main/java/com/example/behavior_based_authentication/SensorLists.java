package com.example.behavior_based_authentication;

import java.util.HashMap;
import android.hardware.Sensor;


public class SensorLists {
    public static final HashMap<Integer, String> oneValue = new HashMap<>();
    static {
        oneValue.put(Sensor.TYPE_LIGHT, "Light");
        oneValue.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient_Temperature");
        oneValue.put(Sensor.TYPE_PRESSURE, "Pressure");
        oneValue.put(Sensor.TYPE_RELATIVE_HUMIDITY, "Relative_Humidity");
        oneValue.put(Sensor.TYPE_PROXIMITY, "Proximity");
        oneValue.put(Sensor.TYPE_HEART_BEAT, "Heart_Beat");
        oneValue.put(Sensor.TYPE_HEART_RATE, "Heart_Rate");
        oneValue.put(Sensor.TYPE_HINGE_ANGLE, "Hinge_Angle");
        oneValue.put(Sensor.TYPE_STEP_COUNTER, "Step_Counter");
        oneValue.put(Sensor.TYPE_STEP_DETECTOR, "Step_Detector");

    }

    public static final HashMap<Integer, String> threeValues = new HashMap<>();
    static {
        threeValues.put(Sensor.TYPE_MAGNETIC_FIELD, "Magnetic_Field");
        threeValues.put(Sensor.TYPE_ORIENTATION, "Orientation");
        threeValues.put(Sensor.TYPE_ACCELEROMETER, "Accelerometer");
        threeValues.put(Sensor.TYPE_GRAVITY, "Gravity");
        threeValues.put(Sensor.TYPE_GYROSCOPE, "Gyroscope");
        threeValues.put(Sensor.TYPE_ROTATION_VECTOR, "Rotation");
    }
}
