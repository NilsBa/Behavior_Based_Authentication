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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        logTouchData(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            Intent intent = new Intent("touch_event_has_occurred");
            Touch touch = new Touch(x, y, pressure, fingersize, downtime, eventtime);
            intent.putExtra("touch", touch);
            sendBroadcast(intent);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void logTouchData(MotionEvent event) {
        float touchmajor = event.getTouchMajor();
        float touchminor = event.getTouchMinor();
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            x = event.getX();
            y = event.getY();
            pressure = event.getPressure();
            fingersize = event.getSize();
            downtime = event.getDownTime();
            eventtime = event.getEventTime();
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            Log.d("Spacer", "------------------------");
            long time = event.getEventTime() - event.getDownTime();
            Log.d("Time", Float.toString(time));
            Log.d("Pressure", Float.toString(pressure));
            Log.d("Fingersize", Float.toString(fingersize));
            Log.d("Downtime", Float.toString(downtime));
            Log.d("Eventtime", Float.toString(eventtime));
            Log.d("Touchmajor", Float.toString(touchmajor));
            Log.d("Touchminor", Float.toString(touchminor));
            Log.d("X", Float.toString(x));
            Log.d("Y", Float.toString(y));
            // username = name.getText().toString();
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                break;
        }
    }
}