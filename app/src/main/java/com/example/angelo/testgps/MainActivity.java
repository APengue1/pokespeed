package com.example.angelo.testgps;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {

    private LocationManager locationManager;
    private NotificationManager notificationManager;
    private TextView kmh;
    private Location lastLocation;
    private Long lastTime;
    private NotificationCompat.Builder mBuilder;
    private static final int NOTIFY_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kmh = (TextView)findViewById(R.id.kmh);
        lastLocation = null;
        lastTime = null;
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.poke_speed)
                .setContentTitle("Pokespeed")
                .setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        getPermissions();
        requestLocation();
    }

    private void requestLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500l,
                    0.5f,
                    this);
        }
        catch(SecurityException e) {
            kmh.setText(e.getMessage());
        }
    }

    private void removeLocation() {
        try {
            locationManager.removeUpdates(this);
        }
        catch(SecurityException e) {
            kmh.setText(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocation();
        notificationManager.notify(this.NOTIFY_ID, mBuilder.build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLocation();
        notificationManager.cancelAll();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.hasSpeed()) {
            Float speed = location.getSpeed(); // m/s
            speed = speed * 60 * 60 / 1000; // km/h
            //kmh.setText(speed.toString());
            setKm(speed);
        }
        else if(this.lastLocation == null || this.lastTime == null) {
            this.lastLocation = location;
            this.lastTime = location.getTime(); // ms
        }
        else {
            float distanceCovered = location.distanceTo(this.lastLocation); // m
            long timeElapsed = location.getTime() - this.lastTime; //ms
            this.lastLocation = location;
            this.lastTime = location.getTime();
            Float speed = distanceCovered / timeElapsed; // m/ms
            speed = speed * 60 * 60 * 60 / 1000; // km/h
            //kmh.setText(speed.toString());
            setKm(speed);
        }
    }

    private void setKm(Float speed) {
        String speedStr = speed.toString();
        kmh.setText(speedStr);
        mBuilder.setContentText(speedStr);
        notificationManager.notify(this.NOTIFY_ID, mBuilder.build());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void getPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1616);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
