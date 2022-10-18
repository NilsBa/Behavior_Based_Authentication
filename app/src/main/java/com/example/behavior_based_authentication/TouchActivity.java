package com.example.behavior_based_authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MotionEvent;

public class TouchActivity extends AppCompatActivity {

    private float x;
    private float y;
    private float pressure;
    private float fingersize;
    private long downtime;
    private long eventtime;
    private long time;
    private float touchmajor;
    private float touchminor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        touchmajor = ev.getTouchMajor();
        touchminor = ev.getTouchMinor();
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            x = ev.getX();
            y = ev.getY();
            pressure = ev.getPressure();
            fingersize = ev.getSize();
            downtime = ev.getDownTime();
            eventtime = ev.getEventTime();
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            time = ev.getEventTime() - ev.getDownTime();
            Intent intent = new Intent("touch_event_has_occurred");
            Touch touch = new Touch(x, y, pressure, fingersize, downtime, eventtime);
            intent.putExtra("touch", touch);
            sendBroadcast(intent);
            logTouchData(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void logTouchData(MotionEvent event) {
        Log.d("Spacer", "------------------------");
        Log.d("Time", Float.toString(time));
        Log.d("Pressure", Float.toString(pressure));
        Log.d("Fingersize", Float.toString(fingersize));
        Log.d("Downtime", Float.toString(downtime));
        Log.d("Eventtime", Float.toString(eventtime));
        Log.d("Touchmajor", Float.toString(touchmajor));
        Log.d("Touchminor", Float.toString(touchminor));
        Log.d("X", Float.toString(x));
        Log.d("Y", Float.toString(y));
    }
}