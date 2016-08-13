package com.example.angelo.testgps;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class SpeedService extends Service implements LocationListener{

    private NotificationCompat.Builder mBuilder;
    private static final int NOTIFY_ID = 1;
    private static final String STOP_SERVICE_ACTION = "Stop Service Action";
    private LocationManager locationManager;
    private Location lastLocation;
    private Long lastTime;

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        initNotification();
        requestLocation();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null &&
                intent.getAction().compareTo(SpeedService.STOP_SERVICE_ACTION) == 0) {
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void initNotification() {
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.poke_speed)
                .setContentTitle("Pokespeed").setColor(Color.GREEN);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        Intent stopIntent = new Intent(this, SpeedService.class);
        stopIntent.setAction(SpeedService.STOP_SERVICE_ACTION);
        PendingIntent resultStopIntent= PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        mBuilder.addAction(android.R.drawable.ic_media_pause, "Stop", resultStopIntent);
        startForeground(SpeedService.NOTIFY_ID, mBuilder.build());
    }

    private void requestLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500L,
                    0.5F,
                    this);
        }
        catch(SecurityException e) {
        }
    }

    private void removeLocation() {
        try {
            locationManager.removeUpdates(this);
        }
        catch(SecurityException e) {
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.hasSpeed()) {
            Float speed = location.getSpeed(); // m/s
            speed = speed * 60 * 60 / 1000; // km/h
            setSpeed(speed);
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
            setSpeed(speed);
        }
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

    private void setSpeed(Float speed) {
        Integer speedInt = Math.round(speed);
        int argb = Color.GREEN;
        if(speedInt > 14)
            argb= Color.YELLOW;
        else if(speedInt > 17)
            argb = Color.RED;
        mBuilder.setContentTitle(speedInt.toString());
        mBuilder.setColor(argb);
        startForeground(SpeedService.NOTIFY_ID, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        removeLocation();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
