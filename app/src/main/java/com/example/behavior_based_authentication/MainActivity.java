package com.example.behavior_based_authentication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;

public class MainActivity extends TouchActivity {

    private static int MY_FINE_LOCATION_REQUEST = 99;
    private static int MY_BACKGROUND_LOCATION_REQUEST = 100;

    MyForegroundService myForegroundService = new MyForegroundService();
    Intent mServiceIntent;

    Button startServiceBtn, stopServiceBtn, newActivityBtn;



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startServiceBtn = findViewById(R.id.StartButton);
        stopServiceBtn = findViewById(R.id.StopButton);
        newActivityBtn = findViewById(R.id.NewActivityButton);

        startServiceBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupPermissions();
            }
        });

        stopServiceBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopServiceFunc();
            }
        });

        newActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(MainActivity.this, NewActivity.class));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast.makeText(this, Integer.toString(requestCode), Toast.LENGTH_LONG).show();

        if ( requestCode == MY_FINE_LOCATION_REQUEST){

            if (grantResults.length !=0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    requestBackgroundLocationPermission();
                }

            } else {
                Toast.makeText(this, "ACCESS_FINE_LOCATION permission denied", Toast.LENGTH_LONG).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:com.trickyworld.locationupdates")
                    ));

                }
            }
            return;

        }else if (requestCode == MY_BACKGROUND_LOCATION_REQUEST){

            if (grantResults.length!=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Background location Permission Granted", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Background location permission denied", Toast.LENGTH_LONG).show();
            }
            return;
        }

    }

    private void setupPermissions() {
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 101);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 102);
        }

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED){
            //ask for permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 103);
        }

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {


                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Background permission");
                    alertDialog.setMessage(getString(R.string.background_location_permission_message));

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Start service anyway",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    starServiceFunc();
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Grant background Permission",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    requestBackgroundLocationPermission();
                                    dialog.dismiss();
                                }
                            });

                    alertDialog.show();


                }else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED){
                    starServiceFunc();
                }
            }else{
                starServiceFunc();
            }

        }else if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {


                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("ACCESS_FINE_LOCATION");
                alertDialog.setMessage("Location permission required");

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                requestFineLocationPermission();
                                dialog.dismiss();
                            }
                        });


                alertDialog.show();

            } else {
                requestFineLocationPermission();
            }
        }
    }

    private void starServiceFunc(){
        myForegroundService = new MyForegroundService();
        mServiceIntent = new Intent(this, myForegroundService.getClass());
        if (!Util.isMyServiceRunning(myForegroundService.getClass(), this)) {
            startService(mServiceIntent);
            Toast.makeText(this, getString(R.string.service_start_successfully), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.service_already_running), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopServiceFunc(){
        myForegroundService = new MyForegroundService();
        mServiceIntent = new Intent(this, myForegroundService.getClass());
        if (Util.isMyServiceRunning(myForegroundService.getClass(), this)) {
            stopService(mServiceIntent);
            Toast.makeText(this, "Service stopped!!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Service is already stopped!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestBackgroundLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, MY_BACKGROUND_LOCATION_REQUEST);
    }

    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_FINE_LOCATION_REQUEST);
    }

}